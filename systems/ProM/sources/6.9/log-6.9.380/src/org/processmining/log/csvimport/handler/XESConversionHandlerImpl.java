package org.processmining.log.csvimport.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csvimport.config.CSVConversionConfig;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVErrorHandlingMode;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVMapping;
import org.processmining.log.csvimport.config.CSVConversionConfig.ExtensionAttribute;
import org.processmining.log.csvimport.exception.CSVConversionException;
import org.processmining.log.utils.XUtils;

import com.google.common.collect.Ordering;

/**
 * Handler that creates an XLog from a CSV
 * 
 * @author F. Mannhardt
 *
 */
public class XESConversionHandlerImpl implements CSVConversionHandler<XLog> {

	private static final int MAX_ERROR_LENGTH = 1 * 1024 * 1024;

	private static final Comparator<? super XEvent> TIME_COMPARATOR = new Comparator<XEvent>() {

		public int compare(XEvent o1, XEvent o2) {
			// assumes stable sorting so start events will be always before complete events
			Date time1 = XUtils.getTimestamp(o1);
			Date time2 = XUtils.getTimestamp(o2);
			return Ordering.natural() // use Date built-in comparator
					.nullsFirst() // null aware since some events might not have times
					.compare(time1, time2);
		}
	};

	private final XFactory factory;
	private final CSVConversionConfig conversionConfig;
	private final StringBuilder conversionErrors;

	private XLog log = null;

	private XTrace currentTrace = null;
	private List<XEvent> currentEvents = new ArrayList<>();

	private int instanceCounter = 0;

	private XEvent currentEvent = null;
	private XEvent currentStartEvent;

	private boolean errorDetected = false;

	public XESConversionHandlerImpl(CSVConfig importConfig, CSVConversionConfig conversionConfig) {
		this.conversionConfig = conversionConfig;
		this.factory = conversionConfig.getFactory();
		this.conversionErrors = new StringBuilder();
	}

	@Override
	public String getConversionErrors() {
		if (conversionErrors.length() >= MAX_ERROR_LENGTH) {
			return conversionErrors.toString()
					.concat("... (multiple error messages have been omitted to avoid running out of memory)");
		} else {
			return conversionErrors.toString();
		}
	}

	@Override
	public boolean hasConversionErrors() {
		return conversionErrors.length() != 0;
	}

	@Override
	public void startLog(CSVFile inputFile) {
		log = factory.createLog();
		if (conversionConfig.getEventNameColumns() != null) {
			log.getExtensions().add(XConceptExtension.instance());
			log.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);
		}
		if (conversionConfig.getCompletionTimeColumn() != null || conversionConfig.getStartTimeColumn() != null) {
			log.getExtensions().add(XTimeExtension.instance());
			log.getExtensions().add(XLifecycleExtension.instance());
			log.getClassifiers().add(XUtils.STANDARDCLASSIFIER);
		}
		assignName(factory, log, inputFile.getFilename());
	}

	@Override
	public void startTrace(String caseId) {
		currentEvents.clear();
		errorDetected = false;
		currentTrace = factory.createTrace();
		assignName(factory, currentTrace, caseId);
	}

	@Override
	public void endTrace(String caseId) {
		if (errorDetected && conversionConfig.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_TRACE_ON_ERROR) {
			// Skip the entire trace
			return;
		}
		sortEventsByTimestamp();
		currentTrace.addAll(currentEvents);
		log.add(currentTrace);
	}

	private void sortEventsByTimestamp() {
		Collections.sort(currentEvents, TIME_COMPARATOR);
	}

	@Override
	public void startEvent(String eventClass, Date completionTime, Date startTime) {
		if (conversionConfig.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_EVENT_ON_ERROR) {
			// Include the other events in that trace
			errorDetected = false;
		}

		currentEvent = factory.createEvent();
		if (eventClass != null) {
			assignName(factory, currentEvent, eventClass);
		}

		if (startTime == null && completionTime == null) {
			// Both times are unknown only create an event assuming it is the completion event
			assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);
		} else if (startTime != null && completionTime != null) {
			// Both start and complete are present
			String instance = String.valueOf((instanceCounter++));

			// Assign attribute for complete event (currentEvent)			
			assignTimestamp(factory, currentEvent, completionTime);
			assignInstance(factory, currentEvent, instance);
			assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);

			// Add additional start event
			currentStartEvent = factory.createEvent();
			if (eventClass != null) {
				assignName(factory, currentStartEvent, eventClass);
			}
			assignTimestamp(factory, currentStartEvent, startTime);
			assignInstance(factory, currentStartEvent, instance);
			assignLifecycleTransition(factory, currentStartEvent, XLifecycleExtension.StandardModel.START);

		} else {
			// Either start or complete are present
			if (completionTime != null) {
				// Only create Complete
				assignTimestamp(factory, currentEvent, completionTime);
				assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);
			} else if (startTime != null) {
				// Only create Start
				assignTimestamp(factory, currentEvent, startTime);
				assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.START);
			} else {
				throw new IllegalStateException(
						"Both start and complete time are NULL. This should never be the case here!");
			}
		}
	}

	@Override
	public void startAttribute(String name, String value) {
		if (!specialColumn(name)) {
			assignAttribute(currentEvent, createLiteral(name, value));
			if (isShouldAddStartEventAttributes() && currentStartEvent != null) {
				assignAttribute(currentStartEvent, createLiteral(name, value));
			}
		}
	}

	private XAttributeLiteral createLiteral(String name, String value) {
		return factory.createAttributeLiteral(getNameFromConfig(name), value, getExtensionFromConfig(name));
	}

	@Override
	public void startAttribute(String name, long value) {
		if (!specialColumn(name)) {
			assignAttribute(currentEvent, createDiscrete(name, value));
			if (isShouldAddStartEventAttributes() && currentStartEvent != null) {
				assignAttribute(currentStartEvent, createDiscrete(name, value));
			}
		}
	}

	private XAttributeDiscrete createDiscrete(String name, long value) {
		return factory.createAttributeDiscrete(getNameFromConfig(name), value, getExtensionFromConfig(name));
	}

	@Override
	public void startAttribute(String name, double value) {
		if (!specialColumn(name)) {
			assignAttribute(currentEvent, createContinuous(name, value));
			if (isShouldAddStartEventAttributes() && currentStartEvent != null) {
				assignAttribute(currentStartEvent, createContinuous(name, value));
			}
		}
	}

	private XAttributeContinuous createContinuous(String name, double value) {
		return factory.createAttributeContinuous(getNameFromConfig(name), value, getExtensionFromConfig(name));
	}

	@Override
	public void startAttribute(String name, Date value) {
		if (!specialColumn(name)) {
			assignAttribute(currentEvent, createDate(name, value));
			if (isShouldAddStartEventAttributes() && currentStartEvent != null) {
				assignAttribute(currentStartEvent, createDate(name, value));
			}
		}
	}

	private XAttributeTimestamp createDate(String name, Date value) {
		return factory.createAttributeTimestamp(getNameFromConfig(name), value, getExtensionFromConfig(name));
	}

	@Override
	public void startAttribute(String name, boolean value) {
		if (!specialColumn(name)) {
			assignAttribute(currentEvent, createBoolean(name, value));
			if (isShouldAddStartEventAttributes() && currentStartEvent != null) {
				assignAttribute(currentStartEvent, createBoolean(name, value));
			}
		}
	}

	private XAttributeBoolean createBoolean(String name, boolean value) {
		return factory.createAttributeBoolean(getNameFromConfig(name), value, getExtensionFromConfig(name));
	}

	private XExtension getExtensionFromConfig(String name) {
		ExtensionAttribute extensionAttribute = getExtensionAttribute(name);
		return extensionAttribute == null ? null : extensionAttribute.extension;
	}

	private String getNameFromConfig(String columnName) {
		CSVMapping csvMapping = getMapping(columnName);
		if (csvMapping.getEventExtensionAttribute() != null
				&& csvMapping.getEventExtensionAttribute() != CSVConversionConfig.NO_EXTENSION_ATTRIBUTE) {
			return csvMapping.getEventExtensionAttribute().key;
		} else if (csvMapping.getEventAttributeName() != null && !csvMapping.getEventAttributeName().isEmpty()) {
			return csvMapping.getEventAttributeName();
		} else {
			return columnName;
		}
	}

	private ExtensionAttribute getExtensionAttribute(String name) {
		return getMapping(name).getEventExtensionAttribute();
	}

	private CSVMapping getMapping(String name) {
		return conversionConfig.getConversionMap().get(name);
	}

	@Override
	public void endAttribute() {
		//No-op
	}

	@Override
	public void endEvent() {
		if (errorDetected && conversionConfig.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_EVENT_ON_ERROR) {
			// Do not include the event
			return;
		}
		// Add start event before complete event to guarantee order for events with same time-stamp
		if (currentStartEvent != null) {
			currentEvents.add(currentStartEvent);
			currentStartEvent = null;
		}
		currentEvents.add(currentEvent);
		currentEvent = null;
	}

	public XLog getResult() {
		return log;
	}

	private static void assignAttribute(XAttributable a, XAttribute value) {
		XUtils.putAttribute(a, value);
	}

	private static void assignLifecycleTransition(XFactory factory, XAttributable a, StandardModel lifecycle) {
		assignAttribute(a, factory.createAttributeLiteral(XLifecycleExtension.KEY_TRANSITION, lifecycle.getEncoding(),
				XLifecycleExtension.instance()));
	}

	private static void assignInstance(XFactory factory, XAttributable a, String value) {
		assignAttribute(a,
				factory.createAttributeLiteral(XConceptExtension.KEY_INSTANCE, value, XConceptExtension.instance()));
	}

	private static void assignTimestamp(XFactory factory, XAttributable a, Date value) {
		assignAttribute(a,
				factory.createAttributeTimestamp(XTimeExtension.KEY_TIMESTAMP, value, XTimeExtension.instance()));
	}

	private static void assignName(XFactory factory, XAttributable a, String value) {
		assignAttribute(a,
				factory.createAttributeLiteral(XConceptExtension.KEY_NAME, value, XConceptExtension.instance()));
	}

	@Override
	public void errorDetected(int lineNumber, int columnIndex, String attributeName, Object content, Exception e)
			throws CSVConversionException {
		CSVErrorHandlingMode errorMode = conversionConfig.getErrorHandlingMode();
		errorDetected = true;
		String columnInfo = String.format("Attribute '%s' with column index %s ", attributeName, columnIndex);
		switch (errorMode) {
			case BEST_EFFORT :
				if (conversionErrors.length() < MAX_ERROR_LENGTH) {
					conversionErrors.append("Line: " + lineNumber + ", " + columnInfo + "\n" + "Skipping attribute "
							+ nullSafeToString(content) + "\nError: " + e + "\n\n");
				}
				break;
			case OMIT_EVENT_ON_ERROR :
				if (conversionErrors.length() < MAX_ERROR_LENGTH) {
					conversionErrors.append(
							"Line: " + lineNumber + ", " + columnInfo + "\n" + "Skipping event, could not convert "
									+ nullSafeToString(content) + "\nError: " + e + "\n\n");
				}
				break;
			case OMIT_TRACE_ON_ERROR :
				if (conversionErrors.length() < MAX_ERROR_LENGTH) {
					conversionErrors.append("Line: " + lineNumber + ", " + columnInfo + "\n" + "Skipping trace "
							+ XUtils.getConceptName(currentTrace) + ", could not convert " + nullSafeToString(content)
							+ "\nError: " + e + "\n\n");
				}
				break;
			default :
			case ABORT_ON_ERROR :
				throw new CSVConversionException(
						"Error converting " + content + " at line " + lineNumber + "and column " + columnIndex, e);
		}
	}

	private static String nullSafeToString(Object obj) {
		if (obj == null) {
			return "NULL";
		} else if (obj.getClass().isArray()) {
			return Arrays.toString((Object[]) obj);
		} else {
			return obj.toString();
		}
	}

	private boolean specialColumn(String columnName) {
		return columnName == null
				|| (XConceptExtension.KEY_NAME.equals(columnName) && !conversionConfig.getEventNameColumns().isEmpty())
				|| (XTimeExtension.KEY_TIMESTAMP.equals(columnName)
						&& conversionConfig.getCompletionTimeColumn() != null)
				|| (XConceptExtension.KEY_INSTANCE.equals(columnName) && conversionConfig.getStartTimeColumn() != null);
	}

	public boolean isShouldAddStartEventAttributes() {
		return conversionConfig.isShouldAddStartEventAttributes();
	}

}