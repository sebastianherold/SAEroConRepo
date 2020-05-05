package org.processmining.plugins.log.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.collection.AlphanumComparator;
import org.processmining.framework.util.ui.widgets.BorderPanel;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.WidgetColors;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.components.SlickerTabbedPane;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class AttributeLogFilter_UI extends BorderPanel {

	private static final long serialVersionUID = 1L;

	public final static String DIALOG_TITLE = "Filter Log by Properties";
	public final static String ATTRIBUTE_DIALOG_TITLE = "Filter Log by Attributes";
	public final static String LENGTH_DIALOG_TITLE = "Filter Log by Trace Length";

	private XLog log;

	private JCheckBox attribute_filter_include_box;
	private ProMComboBox<String> attribute_filter_filter_on;
	private ProMComboBox<String> attribute_filter_log_attributes;
	private ProMComboBox<String> attribute_filter_log_values;

	private NiceSlider length_filter_min;
	private NiceSlider length_filter_max;

	@SuppressWarnings("unchecked")
	public AttributeLogFilter_UI(AttributeLogFilter filter) {
		super(0, 0);

		this.log = filter.log;

		SlickerTabbedPane tabs = SlickerFactory.instance().createTabbedPane("", WidgetColors.COLOR_LIST_BG,
				WidgetColors.COLOR_LIST_FG, Color.GREEN);
		setLayout(new BorderLayout());
		add(tabs);

		ProMPropertiesPanel attributePanel = new ProMPropertiesPanel(ATTRIBUTE_DIALOG_TITLE);
		tabs.addTab(ATTRIBUTE_DIALOG_TITLE, attributePanel);
		attribute_filter_filter_on = new ProMComboBox<String>(new Object[] { AttributeLogFilter.NONE,
				AttributeLogFilter.TRACE_ATTRIBUTE, AttributeLogFilter.EVENT_ATTRIBUTE });
		attributePanel.addProperty("filter on", attribute_filter_filter_on);
		attribute_filter_filter_on.addActionListener(new FilterOnListener());

		// use
		attribute_filter_log_attributes = new ProMComboBox<String>(
				getAttributes((String) attribute_filter_filter_on.getSelectedItem()));
		attributePanel.addProperty("attribute", attribute_filter_log_attributes);
		attribute_filter_log_attributes.addActionListener(new AttributeListener());

		Set<String> values = getValues((String) attribute_filter_filter_on.getSelectedItem(),
				(String) attribute_filter_log_attributes.getSelectedItem());
		attribute_filter_log_values = new ProMComboBox<String>(values);
		MyComboBoxModel model = new MyComboBoxModel(MyComboBoxModel.NONE);
		attribute_filter_log_values.setModel(model);
		model.setSelectedItem(null);
		attributePanel.addProperty("value", attribute_filter_log_values);

		attribute_filter_include_box = SlickerFactory.instance().createCheckBox(null, true);
		attributePanel.addProperty("keep matching traces", attribute_filter_include_box);

		ProMPropertiesPanel lengthPanel = new ProMPropertiesPanel(LENGTH_DIALOG_TITLE);
		tabs.addTab(LENGTH_DIALOG_TITLE, lengthPanel);
		int min_length = Integer.MAX_VALUE;
		int max_length = Integer.MIN_VALUE;
		for (XTrace t : log) {
			if (t.size() < min_length)
				min_length = t.size();
			if (t.size() > max_length)
				max_length = t.size();
		}

		length_filter_min = new NiceSlider(null, min_length, max_length, min_length, Orientation.HORIZONTAL) {
			private static final long serialVersionUID = 1L;

			protected String formatValue(int arg0) {
				return Integer.toString(arg0);
			}
		};
		length_filter_min.remove(0); // remove the slider's label we already have one
		// set size and text of value label
		length_filter_min.getComponent(0).setForeground(WidgetColors.TEXT_COLOR);
		length_filter_min.getComponent(0).setMinimumSize(new Dimension(200, 16));
		length_filter_min.getComponent(0).setPreferredSize(new Dimension(200, 16));
		((JLabel) length_filter_min.getComponent(0)).setHorizontalAlignment(JLabel.RIGHT);
		// and put slider into the panel
		lengthPanel.addProperty("minimum trace length", length_filter_min);

		length_filter_max = new NiceSlider(null, min_length, max_length, max_length, Orientation.HORIZONTAL) {
			private static final long serialVersionUID = 1L;

			protected String formatValue(int arg0) {
				return Integer.toString(arg0);
			}
		};
		length_filter_max.remove(0); // remove the slider's label we already have one
		// set size and text of value label
		length_filter_max.getComponent(0).setForeground(WidgetColors.TEXT_COLOR);
		length_filter_max.getComponent(0).setMinimumSize(new Dimension(200, 16));
		length_filter_max.getComponent(0).setPreferredSize(new Dimension(200, 16));
		((JLabel) length_filter_max.getComponent(0)).setHorizontalAlignment(JLabel.RIGHT);
		// and put slider into the panel
		lengthPanel.addProperty("maximum trace length", length_filter_max);

		setFilterValues(filter);
	}

	/**
	 * Set values of controls based on values in the filter.
	 * 
	 * @param filter
	 */
	protected void setFilterValues(AttributeLogFilter filter) {
		attribute_filter_filter_on.setSelectedItem(filter.attribute_filterOn);
		attribute_filter_include_box.setSelected(filter.attribute_include);
		if (filter.attribute_key != null)
			attribute_filter_log_attributes.setSelectedItem(filter.attribute_key);
		if (filter.attribute_values != null)
			for (Object o : filter.attribute_values) {
				attribute_filter_log_values.setSelectedItem(o);
			}

		length_filter_min.getSlider().setValue(filter.length_min_value);
		length_filter_max.getSlider().setValue(filter.length_max_value);
	}

	/**
	 * display a dialog to ask user what to do
	 * 
	 * @param context
	 * @return
	 */
	protected InteractionResult getUserChoice(UIPluginContext context) {
		return context.showConfiguration(DIALOG_TITLE, this);
	}

	/**
	 * Populate filter object from settings in the panel.
	 * 
	 * @param filter
	 */
	@SuppressWarnings("unchecked")
	protected void getFilterValues(AttributeLogFilter filter) {
		filter.attribute_include = attribute_filter_include_box.isSelected();
		filter.attribute_filterOn = (String) attribute_filter_filter_on.getSelectedItem();
		filter.attribute_key = (String) attribute_filter_log_attributes.getSelectedItem();
		filter.attribute_values.clear();
		filter.attribute_values.addAll((List<String>) attribute_filter_log_values.getSelectedItem());

		filter.length_min_value = length_filter_min.getSlider().getValue();
		filter.length_max_value = length_filter_max.getSlider().getValue();
	}

	/**
	 * Open UI dialogue to populate the given configuration object with settings
	 * chosen by the user.
	 * 
	 * @param context
	 * @param config
	 * @return result of the user interaction
	 */
	public InteractionResult setParameters(UIPluginContext context, AttributeLogFilter filter) {
		InteractionResult wish = getUserChoice(context);
		if (wish != InteractionResult.CANCEL)
			getFilterValues(filter);
		return wish;
	}

	/**
	 * Listener to watch
	 * {@link AttributeLogFilter_UI#attribute_filter_filter_on} and store
	 * attribute names in
	 * {@link AttributeLogFilter_UI#attribute_filter_log_attributes} and updated
	 * {@link AttributeLogFilter_UI#attribute_filter_log_values} accordingly
	 */
	private class FilterOnListener implements ActionListener {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void actionPerformed(ActionEvent e) {
			if (e.getID() == ActionEvent.ACTION_PERFORMED && e.getSource() == attribute_filter_filter_on) {

				TreeSet<String> attributeNames = getAttributes((String) attribute_filter_filter_on.getSelectedItem());
				attribute_filter_log_attributes.setModel(new DefaultComboBoxModel(attributeNames.toArray()));

				Set<String> values = getValues((String) attribute_filter_filter_on.getSelectedItem(),
						(String) attribute_filter_log_attributes.getSelectedItem());
				MyComboBoxModel model = new MyComboBoxModel(values.toArray());
				attribute_filter_log_values.setModel(model);
				model.setSelectedItem(null);

				//				attribute_filter_log_values.setModel(new DefaultComboBoxModel(values.toArray()));
			}
		}
	}

	/**
	 * Listener to watch
	 * {@link AttributeLogFilter_UI#attribute_filter_log_attributes} and store
	 * attribute names in
	 * {@link AttributeLogFilter_UI#attribute_filter_log_values}.
	 */
	private class AttributeListener implements ActionListener {

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			if (e.getID() == ActionEvent.ACTION_PERFORMED && e.getSource() == attribute_filter_log_attributes) {

				Set<String> values = getValues((String) attribute_filter_filter_on.getSelectedItem(),
						(String) attribute_filter_log_attributes.getSelectedItem());
				MyComboBoxModel model = new MyComboBoxModel(values.toArray());
				attribute_filter_log_values.setModel(model);
				model.setSelectedItem(null);

				//				attribute_filter_log_values.setModel(new DefaultComboBoxModel(values.toArray()));
			}
		}
	}

	/**
	 * Collect all attribute names of the selected category from
	 * {@link AttributeLogFilter_UI#log}
	 * 
	 * @param category
	 * @return
	 */
	private TreeSet<String> getAttributes(String category) {
		TreeSet<String> attributeNames = new TreeSet<String>();
		if (category == AttributeLogFilter.TRACE_ATTRIBUTE) {
			for (XTrace t : log) {
				XAttributeMap attributes = t.getAttributes();
				attributeNames.addAll(attributes.keySet());
			}

		} else if (category == AttributeLogFilter.EVENT_ATTRIBUTE) {
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					XAttributeMap attributes = event.getAttributes();
					attributeNames.addAll(attributes.keySet());
				}
			}
		} else if (category == AttributeLogFilter.NONE) {
			attributeNames.add("<none>");
		}
		return attributeNames;
	}

	/**
	 * Collect all attribute values of the selected category and key from
	 * {@link AttributeLogFilter_UI#log}
	 * 
	 * @param category
	 * @return
	 */
	private Set<String> getValues(String category, String key) {
		TreeSet<String> values = new TreeSet<String>(new AlphanumComparator());
		if (category == AttributeLogFilter.TRACE_ATTRIBUTE) {
			for (XTrace t : log) {
				XAttributeMap attributes = t.getAttributes();
				if (attributes.containsKey(key))
					values.add(attributes.get(key).toString());
			}

		} else if (category == AttributeLogFilter.EVENT_ATTRIBUTE) {
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					XAttributeMap attributes = event.getAttributes();
					if (attributes.containsKey(key))
						values.add(attributes.get(key).toString());
				}
			}
		} else if (category == AttributeLogFilter.NONE) {
			values.add("<none>");
		}
		return values;
	}

}

class MyComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

	private static final long serialVersionUID = 1759722993311195116L;
	public static Object NONE = "none";
	List<String> values = new ArrayList<String>();
	List<String> selected = new ArrayList<String>();

	public MyComboBoxModel(Object... values) {
		for (Object object : values) {
			if (object == null || object == NONE || !(object instanceof String)) {
				continue;
			}
			this.values.add((String)object);
		}
	}

	@Override
	public int getSize() {
		return values.size();
	}

	@Override
	public String getElementAt(int index) {
		return values.get(index);
	}

	public void setSelectedItem(Object anItem) {
		if (anItem == null || anItem == NONE || !(anItem instanceof String)) {
			if (selected.isEmpty())
				return;
			selected.clear();
		} else {
			boolean removed = selected.remove(anItem);
			if (!removed) {
				selected.add((String)anItem);
			}
		}
		fireContentsChanged(this, -1, -1);
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}


}
