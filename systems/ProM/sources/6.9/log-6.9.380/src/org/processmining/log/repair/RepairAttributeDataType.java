package org.processmining.log.repair;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.ui.widgets.ProMScrollPane;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.log.formats.StandardDateFormats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

/**
 * Tries to automatically guess the data type of all XES attributes and updates
 * the log accordingly.
 * <p>
 * PLEASE NOTE: This filter will update the original XLog instead of creating a
 * new XLog, to be able to process huge logs without exhausting the available
 * memory.
 * 
 * @author F. Mannhardt
 * 
 */
public final class RepairAttributeDataType {

	private static class ReviewTable {

		private Map<String, Class<? extends XAttribute>> attributeDataType;
		private final JTable datatypeTable;
		private final DefaultTableModel tableModel;

		@SuppressWarnings({ "unchecked", "serial" })
		public ReviewTable(final Map<String, Class<? extends XAttribute>> attributeDataType) {
			super();
			this.attributeDataType = attributeDataType;
			this.tableModel = new DefaultTableModel() {

				public void setValueAt(Object aValue, int row, int column) {
					super.setValueAt(aValue, row, column);
					attributeDataType.put(getColumnName(column), (Class<? extends XAttribute>) aValue);
				}

			};

			for (String attributeKey : Ordering.natural().immutableSortedCopy(attributeDataType.keySet())) {
				Class<? extends XAttribute> dataType = attributeDataType.get(attributeKey);
				tableModel.addColumn(attributeKey, new Class[] { dataType });
			}

			this.datatypeTable = new JTable(tableModel);
			JComboBox<Class<? extends XAttribute>> comboBox = new JComboBox<>(
					new DefaultComboBoxModel<Class<? extends XAttribute>>(new Class[] { XAttributeBoolean.class,
							XAttributeContinuous.class, XAttributeDiscrete.class, XAttributeLiteral.class,
							XAttributeTimestamp.class }));
			comboBox.setRenderer(new DefaultListCellRenderer() {

				@SuppressWarnings("rawtypes")
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					JLabel superComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
					superComponent.setText(((Class) value).getSimpleName());
					return superComponent;
				}

			});
			datatypeTable.setDefaultEditor(Object.class, new DefaultCellEditor(comboBox));
			datatypeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

				@SuppressWarnings("rawtypes")
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
							column);
					c.setText(((Class) value).getSimpleName());
					return c;
				}

			});

			datatypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			resizeColumnWidth(datatypeTable);
		}

		public Map<String, Class<? extends XAttribute>> getDataTypeMap() {
			return attributeDataType;
		}

		public JComponent getDatatypeTable() {
			JPanel workaroundPanel = new JPanel(new BorderLayout());
			ProMScrollPane scrollPane = new ProMScrollPane(datatypeTable);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			workaroundPanel.add(scrollPane, BorderLayout.CENTER);
			workaroundPanel.setPreferredSize(new Dimension(400, 200));
			return workaroundPanel;
		}

		public void resizeColumnWidth(JTable table) {
			final TableColumnModel columnModel = table.getColumnModel();
			for (int column = 0; column < table.getColumnCount(); column++) {
				int width = 150; // Min width
				for (int row = 0; row < table.getRowCount(); row++) {
					TableCellRenderer renderer = table.getCellRenderer(row, column);
					Component comp = table.prepareRenderer(renderer, row, column);
					width = Math.max(comp.getPreferredSize().width * 2, width);
				}
				columnModel.getColumn(column).setPreferredWidth(width);
			}
		}

	}

	public interface ReviewCallback {
		Map<String, Class<? extends XAttribute>> reviewDataTypes(
				Map<String, Class<? extends XAttribute>> guessedDataTypes);
	}

	public RepairAttributeDataType() {
		super();
	}

	public void doRepairEventAttributes(PluginContext context, XLog log, Iterable<? extends DateFormat> dateFormats) {
		doRepairEventAttributes(context, log, dateFormats, null);
	}

	public void doRepairEventAttributes(PluginContext context, XLog log, Iterable<? extends DateFormat> dateFormats,
			ReviewCallback reviewCallback) {

		Progress progBar = context.getProgress();
		progBar.setMinimum(0);
		progBar.setMaximum(log.size() * 2); // two pass
		progBar.setValue(0);

		Map<String, Class<? extends XAttribute>> guessedDataType = new HashMap<>();

		// Determine best datatype
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				buildDataTypeMap(event.getAttributes(), guessedDataType, dateFormats);
			}
			if (progBar.isCancelled()) {
				return;
			}
			progBar.inc();
		}

		boolean isDefinite = false;

		if (reviewCallback != null) {
			guessedDataType = reviewCallback.reviewDataTypes(guessedDataType);
			isDefinite = true; // Always obey user input
		}

		XFactory factory = XFactoryRegistry.instance().currentDefault();
		ListIterator<XTrace> traceIterator = log.listIterator();

		while (traceIterator.hasNext()) {

			XTrace trace = traceIterator.next();
			ListIterator<XEvent> eventIterator = trace.listIterator();

			while (eventIterator.hasNext()) {
				int eventIndex = eventIterator.nextIndex();
				XEvent event = eventIterator.next();
				XAttributeMap eventAttr = event.getAttributes();
				repairAttributes(context, factory, eventAttr, dateFormats, guessedDataType, isDefinite);
				trace.set(eventIndex, event);
			}
			if (progBar.isCancelled()) {
				return;
			}
			progBar.inc();
		}

	}

	public void doRepairTraceAttributes(PluginContext context, XLog log, Iterable<? extends DateFormat> dateFormats) {
		doRepairTraceAttributes(context, log, dateFormats, null);
	}

	public void doRepairTraceAttributes(PluginContext context, XLog log, Iterable<? extends DateFormat> dateFormats,
			ReviewCallback reviewCallback) {

		Progress progBar = context.getProgress();
		progBar.setMinimum(0);
		progBar.setMaximum(log.size() * 2); // two pass
		progBar.setValue(0);

		Map<String, Class<? extends XAttribute>> guessedDataType = new HashMap<>();

		// Determine best datatype
		for (XTrace trace : log) {
			buildDataTypeMap(trace.getAttributes(), guessedDataType, dateFormats);
			if (progBar.isCancelled()) {
				return;
			}
			progBar.inc();
		}

		boolean isDefinite = false;

		if (reviewCallback != null) {
			guessedDataType = reviewCallback.reviewDataTypes(guessedDataType);
			isDefinite = true;
		}

		XFactory factory = XFactoryRegistry.instance().currentDefault();

		ListIterator<XTrace> traceIterator = log.listIterator();

		while (traceIterator.hasNext()) {

			XTrace trace = traceIterator.next();
			XAttributeMap traceAttr = trace.getAttributes();
			repairAttributes(context, factory, traceAttr, dateFormats, guessedDataType, isDefinite);

			if (progBar.isCancelled()) {
				return;
			}
			progBar.inc();
		}

	}

	/**
	 * Shows a wizard that allows the user to specify an additional custom date
	 * format.
	 * 
	 * @param context
	 * @return a set of DateFormats including the user specified format
	 */
	public static Iterable<? extends DateFormat> queryDateFormats(UIPluginContext context) {

		try {
			String dateFormat = ProMUIHelper
					.queryForString(
							context,
							"Specify a custom DateFormat pattern (Format as defined in Java SimpleDateFormat) that is used to parse literal attributes that contain dates (LEAVE BLANK OR CANCEL TO USE DEFAULTS)");
			SimpleDateFormat userDateFormat;
			if (dateFormat != null && !dateFormat.isEmpty()) {
				try {
					userDateFormat = new SimpleDateFormat(dateFormat);
					return Iterables.concat(ImmutableList.of(userDateFormat),
							StandardDateFormats.getStandardDateFormats());
				} catch (IllegalArgumentException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Wrong Date Format", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (UserCancelledException e) {
		}

		return StandardDateFormats.getStandardDateFormats();
	}

	/**
	 * 
	 * 
	 * @param context
	 * @param attributeDataType
	 * @return
	 */
	public static Map<String, Class<? extends XAttribute>> queryCustomDataTypes(UIPluginContext context,
			Map<String, Class<? extends XAttribute>> attributeDataType) {
		ReviewTable reviewPanel = new ReviewTable(attributeDataType);
		InteractionResult reviewResult = context.showConfiguration(
				"Review/Adjust the automatically determined data types", reviewPanel.getDatatypeTable());
		if (reviewResult == InteractionResult.FINISHED) {
			return reviewPanel.getDataTypeMap();
		}
		return attributeDataType;
	}

	private static void repairAttributes(PluginContext context, XFactory factory, XAttributeMap attributes,
			Iterable<? extends DateFormat> dateFormats, Map<String, Class<? extends XAttribute>> attributeDataType,
			boolean isDefinite) {
		// Use entrySet here, to avoid a lot of 'put' operations, maybe the underlying map can optimize the replacement operation using 'entry.setValue'
		Iterator<Entry<String, XAttribute>> traceAttr = attributes.entrySet().iterator();
		while (traceAttr.hasNext()) {
			Entry<String, XAttribute> entry = traceAttr.next();

			if (!isExtensionAttribute(entry.getValue())) {
				if (!(entry.getValue() instanceof XAttributeTimestamp)) {
					Class<? extends XAttribute> dataType = attributeDataType.get(entry.getKey());
					if (dataType != null) {
						try {
							XAttribute newAttribute = createAttribute(dataType, entry, factory, dateFormats);
							if (newAttribute != null) {
								entry.setValue(newAttribute);
							} else {
								throw new RuntimeException(String.format(
										"Could convert of attribute %s to %s NULL value returned.", entry.getKey(),
										dataType));
							}
						} catch (UnexpectedDataTypeException e) {
							if (isDefinite) {
								// remove non-matching entries
								traceAttr.remove();
								context.log("Removing non-matching value " + entry.getValue().toString());
							} else {
								throw new RuntimeException(String.format(
										"Could convert of attribute %s to data type %s.", entry.getKey(), dataType), e);
							}
						}
					} else {
						throw new RuntimeException(String.format(
								"Could not find datatype of attribute %s. Available data types are %s.",
								entry.getKey(), attributeDataType));
					}
				}
			}
		}
	}

	private static boolean isExtensionAttribute(XAttribute value) {
		return value.getExtension() != null;
	}

	private static void buildDataTypeMap(XAttributeMap attributes,
			Map<String, Class<? extends XAttribute>> attributeDataType, Iterable<? extends DateFormat> dateFormats) {
		for (XAttribute attribute : attributes.values()) {

			if (!(attribute instanceof XAttributeTimestamp)) {

				try {
					String value = getAttrAsString(attribute);
					Class<? extends XAttribute> currentDataType = inferDataType(value, dateFormats);
					Class<? extends XAttribute> lastDataType = attributeDataType.get(attribute.getKey());

					if (lastDataType == null) {
						// First occurrence
						attributeDataType.put(attribute.getKey(), currentDataType);
					} else if (!lastDataType.equals(currentDataType)) {
						// Stored data type does not match new occurrence

						if (checkChangeBothWays(currentDataType, lastDataType, XAttributeBoolean.class,
								XAttributeDiscrete.class)) {
							// Mixed Boolean (e.g. 0,1) & Integer -> XAttributeDiscrete
							if (lastDataType != XAttributeDiscrete.class) {
								attributeDataType.put(attribute.getKey(), XAttributeDiscrete.class);
							}
						} else if (checkChangeBothWays(currentDataType, lastDataType, XAttributeBoolean.class,
								XAttributeContinuous.class)) {
							// Mixed Boolean  (e.g. 0,1) & Float -> XAttributeContinuous
							if (lastDataType != XAttributeContinuous.class) {
								attributeDataType.put(attribute.getKey(), XAttributeContinuous.class);
							}
						} else if (checkChangeBothWays(currentDataType, lastDataType, XAttributeDiscrete.class,
								XAttributeContinuous.class)) {
							// Mixed Integer & Float -> XAttributeContinuous
							if (lastDataType != XAttributeContinuous.class) {
								attributeDataType.put(attribute.getKey(), XAttributeContinuous.class);
							}
						} else {
							// Fallback to Literal
							if (lastDataType != XAttributeLiteral.class) {
								attributeDataType.put(attribute.getKey(), XAttributeLiteral.class);
							}
						}
					}
				} catch (UnexpectedDataTypeException e) {
					// Ignore this attribute
				}

			}

		}
	}

	private static boolean checkChangeBothWays(Class<? extends XAttribute> dataType,
			Class<? extends XAttribute> lastDataType, Class<? extends XAttribute> class1,
			Class<? extends XAttribute> class2) {
		return (class1.equals(lastDataType) && class2.equals(dataType))
				|| (class2.equals(lastDataType) && class1.equals(dataType));
	}

	private static XAttribute createAttribute(Class<? extends XAttribute> dataType, Entry<String, XAttribute> entry,
			XFactory factory, Iterable<? extends DateFormat> dateFormats) throws UnexpectedDataTypeException {
		if (XAttributeDiscrete.class.equals(dataType)) {
			return factory.createAttributeDiscrete(entry.getKey(), getAttrAsLong(entry.getValue()), null);
		} else if (XAttributeContinuous.class.equals(dataType)) {
			return factory.createAttributeContinuous(entry.getKey(), getAttrAsDouble(entry.getValue()), null);
		} else if (XAttributeBoolean.class.equals(dataType)) {
			return factory.createAttributeBoolean(entry.getKey(), getAttrAsBoolean(entry.getValue()), null);
		} else if (XAttributeLiteral.class.equals(dataType)) {
			return factory.createAttributeLiteral(entry.getKey(), getAttrAsString(entry.getValue()), null);
		} else if (XAttributeTimestamp.class.equals(dataType)) {
			return factory.createAttributeTimestamp(entry.getKey(), getAttrAsDate(entry.getValue(), dateFormats), null);
		} else {
			throw new IllegalArgumentException(String.format("Unexpected Attribute %s: Type %s instead %s",
					entry.getValue(), entry.getValue().getClass().getSimpleName(), dataType.getSimpleName()));
		}
	}

	private static Date getAttrAsDate(XAttribute value, Iterable<? extends DateFormat> dateFormats)
			throws UnexpectedDataTypeException {
		if (value instanceof XAttributeLiteral) {
			Date date = tryParseDate(((XAttributeLiteral) value).getValue(), dateFormats);
			if (date == null) {
				throw new UnexpectedDataTypeException("Unexpected date format " + value);
			}
			return date;
		} else {
			throw new UnexpectedDataTypeException("Unexpected attribute type " + value);
		}
	}

	private static Date tryParseDate(String value, Iterable<? extends DateFormat> dateFormats) {
		ParsePosition pos = new ParsePosition(0);
		for (DateFormat formatter : dateFormats) {
			pos.setIndex(0);
			Date date = formatter.parse(value, pos);
			if (date != null && pos.getIndex() == value.length()) {
				return date;
			}
		}
		return null;
	}

	private static String getAttrAsString(XAttribute value) throws UnexpectedDataTypeException {
		if (value instanceof XAttributeDiscrete) {
			return Long.toString(((XAttributeDiscrete) value).getValue());
		} else if (value instanceof XAttributeContinuous) {
			return Double.toString(((XAttributeContinuous) value).getValue());
		} else if (value instanceof XAttributeBoolean) {
			return Boolean.toString(((XAttributeBoolean) value).getValue());
		} else if (value instanceof XAttributeLiteral) {
			return ((XAttributeLiteral) value).getValue();
		} else {
			throw new UnexpectedDataTypeException("Unexpected attribute type " + value);
		}
	}

	private static boolean getAttrAsBoolean(XAttribute value) throws UnexpectedDataTypeException {
		if (value instanceof XAttributeBoolean) {
			return ((XAttributeBoolean) value).getValue();
		} else if (value instanceof XAttributeLiteral) {
			String val = ((XAttributeLiteral) value).getValue();
			if ("0".equals(val) || "N".equalsIgnoreCase(val)) {
				return false;
			} else if ("1".equals(val) || "J".equalsIgnoreCase(val) || "Y".equalsIgnoreCase(val)) {
				return true;
			} else {
				return Boolean.valueOf(val);
			}
		} else if (value instanceof XAttributeDiscrete) {
			long val = ((XAttributeDiscrete) value).getValue();
			if (val != 0 && val != 1) {
				throw new UnexpectedDataTypeException("Unexpected value " + val);
			}
			return Boolean.valueOf(val == 0 ? false : true);
		} else {
			throw new UnexpectedDataTypeException("Unexpected attribute type " + value);
		}
	}

	private static double getAttrAsDouble(XAttribute value) throws UnexpectedDataTypeException {
		try {
			if (value instanceof XAttributeDiscrete) {
				return ((XAttributeDiscrete) value).getValue();
			} else if (value instanceof XAttributeContinuous) {
				return ((XAttributeContinuous) value).getValue();
			} else if (value instanceof XAttributeLiteral) {
				return Double.valueOf(((XAttributeLiteral) value).getValue());
			} else {
				throw new UnexpectedDataTypeException("Unexpected attribute type " + value);
			}
		} catch (NumberFormatException e) {
			throw new UnexpectedDataTypeException(e);
		}
	}

	private static long getAttrAsLong(XAttribute value) throws UnexpectedDataTypeException {
		try {
			if (value instanceof XAttributeDiscrete) {
				return ((XAttributeDiscrete) value).getValue();
			} else if (value instanceof XAttributeLiteral) {
				return Long.valueOf(((XAttributeLiteral) value).getValue());
			} else {
				throw new UnexpectedDataTypeException("Unexpected attribute type " + value);
			}
		} catch (NumberFormatException e) {
			throw new UnexpectedDataTypeException(e);
		}
	}

	private static Pattern DISCRETE_PATTERN = Pattern.compile("(-)?[0-9]{1,19}");
	private static Pattern CONTINUOUS_PATTERN = Pattern
			.compile("((-)?[0-9]*\\.[0-9]+)|((-)?[0-9]+(\\.[0-9]+)?(e|E)\\+[0-9]+)");
	private static Pattern BOOLEAN_PATTERN = Pattern.compile("(true)|(false)|(TRUE)|(FALSE)|(0)|(1)|(Y)|(N)|(J)");

	private static Class<? extends XAttribute> inferDataType(String value, Iterable<? extends DateFormat> dateFormats) {
		if (BOOLEAN_PATTERN.matcher(value).matches()) {
			return XAttributeBoolean.class;
		} else if (DISCRETE_PATTERN.matcher(value).matches()) {
			try {
				Long.parseLong(value);
				return XAttributeDiscrete.class;
			} catch (NumberFormatException e) {
				return XAttributeLiteral.class;
			}
		} else if (CONTINUOUS_PATTERN.matcher(value).matches()) {
			return XAttributeContinuous.class;
		} else if (tryParseDate(value, dateFormats) != null) {
			return XAttributeTimestamp.class;
		} else {
			return XAttributeLiteral.class;
		}
	}

}
