package org.processmining.log.repair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XCostExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.util.XAttributeUtils;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public final class RepairGlobalAttributesPlugin {

	private static final Function<XAttribute, XAttribute> PROTOTYPE_TRANSFORMER = new Function<XAttribute, XAttribute>() {

		public XAttribute apply(XAttribute firstAttr) {
			return XAttributeUtils.derivePrototype(firstAttr);
		}
	};

	public interface GlobalInfo {
		
		Collection<XAttribute> getEventAttributes();

		Collection<XAttribute> getTraceAttributes();
		
	}

	@Plugin(name = "Repair Log: Globals, Classifiers, Extensions (In Place)", level = PluginLevel.Regular, parameterLabels = { "Event Log" },//
			returnLabels = {}, returnTypes = {}, userAccessible = true, mostSignificantResult = -1, categories = { PluginCategory.Enhancement }, //
	help = "Repairs the Event Log by detecting which attributes are global, updating the information about global attributes, adding possible classifiers, and adding correct extensions to certain attributes (time:timestamp, etc). This plug-ins changes the input event log to be able to deal with large event logs!")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt", email = "f.mannhardt@tue.nl")
	public void repairLogInPlace(PluginContext context, XLog log) {

		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(log.size());
		doRepairLog(log);
	}

	@Plugin(name = "Repair Log: Globals, Classifiers, Extensions", level = PluginLevel.PeerReviewed, parameterLabels = { "Event Log" }, //
			returnLabels = { "Repaired Log with Globals" }, returnTypes = { XLog.class }, userAccessible = true, mostSignificantResult = 1, categories = { PluginCategory.Enhancement }, //
	help = "Repairs the Event Log by detecting which attributes are global, updating the information about global attributes, adding possible classifiers, and adding correct extensions to certain attributes (time:timestamp, etc).")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt", email = "f.mannhardt@tue.nl")
	public XLog repairLog(PluginContext context, XLog log) {

		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(log.size());

		XLog newLog = (XLog) log.clone();

		doRepairLog(newLog);

		return newLog;
	}

	@Plugin(name = "Repair Log: Globals, Classifiers, Extensions (In Place)", level = PluginLevel.Regular, parameterLabels = { "Event Log" }, returnLabels = {}, returnTypes = {}, userAccessible = true, mostSignificantResult = -1, categories = { PluginCategory.Enhancement }, //
	help = "Repairs the Event Log by detecting which attributes are global, updating the information about global attributes, adding possible classifiers, and adding correct extensions to certain attributes (time:timestamp, etc). This plug-ins changes the input event log to be able to deal with large event logs!")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt", email = "f.mannhardt@tue.nl")
	public void repairLogInPlaceUI(UIPluginContext context, XLog log) {

		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(log.size());

		try {
			doRepairLogUI(context, log);
		} catch (UserCancelledException e) {
			context.getFutureResult(0).cancel(false);
		}
	}

	@Plugin(name = "Repair Log: Globals, Classifiers, Extensions", level = PluginLevel.PeerReviewed, parameterLabels = { "Event Log" }, returnLabels = { "Repaired Log with Globals" }, returnTypes = { XLog.class }, userAccessible = true, mostSignificantResult = 1, categories = { PluginCategory.Enhancement }, //
	help = "Repairs the Event Log by detecting which attributes are global, updating the information about global attributes, adding possible classifiers, and adding correct extensions to certain attributes (time:timestamp, etc).")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt", email = "f.mannhardt@tue.nl")
	public XLog repairLogUI(UIPluginContext context, XLog log) {

		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(log.size());

		XLog newLog = (XLog) log.clone();

		try {
			doRepairLogUI(context, newLog);
		} catch (UserCancelledException e) {
			context.getFutureResult(0).cancel(false);
		}

		return newLog;
	}

	public void doRepairLogUI(UIPluginContext context, XLog log) throws UserCancelledException {

		GlobalInfo globals = detectGlobals(log);

		final Set<String> classifierAttribute = ImmutableSet
				.copyOf(ProMUIHelper.queryForObjects(context,
						"Which of the following global attributes should be added as classifier?",
						Iterables.transform(globals.getEventAttributes(), new Function<XAttribute, String>() {

							public String apply(XAttribute a) {
								return a.getKey();
							}
						})));

		doRepairLog(log, globals, new Predicate<XAttribute>() {

			public boolean apply(XAttribute a) {
				return classifierAttribute.contains(a.getKey());
			}
		});
	}

	public static void doRepairLog(XLog log) {
		doRepairLog(log, detectGlobals(log), new Predicate<XAttribute>() {

			public boolean apply(XAttribute a) {
				return isClassifierAttribute(a);
			}
		});
	}

	public static void doRepairLog(XLog log, GlobalInfo info, Predicate<XAttribute> useForClassifier) {
		for (XAttribute attr : info.getEventAttributes()) {
			if (useForClassifier.apply(attr)) {
				if (!hasClassifier(attr, log.getClassifiers())) {
					log.getClassifiers().add(new XEventAttributeClassifier(attr.getKey(), attr.getKey()));
				}
			}
			switch (attr.getKey()) {
				case XConceptExtension.KEY_NAME :
				case XConceptExtension.KEY_INSTANCE :
					if (!log.getExtensions().contains(XConceptExtension.instance())) {
						log.getExtensions().add(XConceptExtension.instance());
					}
					break;
				case XTimeExtension.KEY_TIMESTAMP :
					if (!log.getExtensions().contains(XTimeExtension.instance())) {
						log.getExtensions().add(XTimeExtension.instance());
					}
					break;
				case XLifecycleExtension.KEY_MODEL :
				case XLifecycleExtension.KEY_TRANSITION :
					if (!log.getExtensions().contains(XLifecycleExtension.instance())) {
						log.getExtensions().add(XLifecycleExtension.instance());
					}
					break;
				case XOrganizationalExtension.KEY_GROUP :
				case XOrganizationalExtension.KEY_RESOURCE :
				case XOrganizationalExtension.KEY_ROLE :
					if (!log.getExtensions().contains(XOrganizationalExtension.instance())) {
						log.getExtensions().add(XOrganizationalExtension.instance());
					}
					break;
				case XCostExtension.KEY_AMOUNT :
				case XCostExtension.KEY_CURRENCY :
				case XCostExtension.KEY_DRIVER :
				case XCostExtension.KEY_TOTAL :
				case XCostExtension.KEY_TYPE :
					if (!log.getExtensions().contains(XCostExtension.instance())) {
						log.getExtensions().add(XCostExtension.instance());
					}
					break;
			}
			if (!hasGlobalAttribute(attr, log.getGlobalEventAttributes())) {
				log.getGlobalEventAttributes().add(attr);
			}
		}

		for (XAttribute attr : info.getTraceAttributes()) {
			if (!hasGlobalAttribute(attr, log.getGlobalTraceAttributes())) {
				log.getGlobalTraceAttributes().add(attr);
			}
		}
	}

	private static boolean hasGlobalAttribute(XAttribute attribute, List<XAttribute> globalAttributes) {
		for (XAttribute globalAttribute : globalAttributes) {
			if (globalAttribute.getKey().equals(attribute.getKey())) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasClassifier(XAttribute attrribute, List<XEventClassifier> classifierList) {
		for (XEventClassifier classifier : classifierList) {
			for (String key : classifier.getDefiningAttributeKeys()) {
				if (key.equals(attrribute.getKey())) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isClassifierAttribute(XAttribute attribute) {
		switch (attribute.getKey()) {
			case XConceptExtension.KEY_INSTANCE :
			case XTimeExtension.KEY_TIMESTAMP :
			case XLifecycleExtension.KEY_MODEL :
			case XLifecycleExtension.KEY_TRANSITION :
			case XCostExtension.KEY_AMOUNT :
			case XCostExtension.KEY_CURRENCY :
			case XCostExtension.KEY_DRIVER :
			case XCostExtension.KEY_TOTAL :
			case XCostExtension.KEY_TYPE :
				return false;
		}
		return true;
	}

	public static GlobalInfo detectGlobals(XLog log) {

		Set<XAttribute> eventAttributes = new HashSet<>();
		Set<XAttribute> traceAttributes = new HashSet<>();

		for (ListIterator<XTrace> logIter = log.listIterator(); logIter.hasNext();) {
			int traceIndex = logIter.nextIndex();
			XTrace trace = logIter.next();
			if (traceIndex == 0) {
				traceAttributes.addAll(trace.getAttributes().values());
			} else {
				Iterator<XAttribute> it = traceAttributes.iterator();
				while (it.hasNext()) {
					if (!trace.getAttributes().containsKey(it.next().getKey())) {
						it.remove();
					}
				}
			}
			for (ListIterator<XEvent> eventIter = trace.listIterator(); eventIter.hasNext();) {
				int eventIndex = eventIter.nextIndex();
				XEvent event = eventIter.next();
				if (traceIndex == 0 && eventIndex == 0) {
					eventAttributes.addAll(event.getAttributes().values());
				} else {
					Iterator<XAttribute> it = eventAttributes.iterator();
					while (it.hasNext()) {
						if (!event.getAttributes().containsKey(it.next().getKey())) {
							it.remove();
						}
					}
				}
			}
		}

		final Collection<XAttribute> defaultEventAttributes = Collections2.transform(eventAttributes,
				PROTOTYPE_TRANSFORMER);
		final Collection<XAttribute> defaultTraceAttributes = Collections2.transform(traceAttributes,
				PROTOTYPE_TRANSFORMER);

		return new GlobalInfo() {

			public Collection<XAttribute> getEventAttributes() {
				return defaultEventAttributes;
			}

			public Collection<XAttribute> getTraceAttributes() {
				return defaultTraceAttributes;
			}

		};
	}

}
