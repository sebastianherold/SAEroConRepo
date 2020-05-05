package org.processmining.log.filters.ui;

import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.log.filters.ExtensionFilter;

@Plugin(name = "Extension Filter", returnLabels = { "Filtered log" }, returnTypes = { XLog.class }, parameterLabels = {
		"Log" })
public class ExtensionFilterUI {

	@UITopiaVariant(uiLabel = "Remove extensions from log", affiliation = UITopiaVariant.EHV, author = "J.M.E.M. van der Werf", email = "j.m.e.m.v.d.werf@tue.nl", pack = "Ontologies")
	@PluginVariant(variantLabel = "Remove extensions from log", requiredParameterLabels = { 0 })
	public XLog removeExtensions(final UIPluginContext context, final XLog log) {

		Step wizardStep = new Step(log);
		ListWizard<Set<String>> wizard = new ListWizard<Set<String>>(wizardStep);

		Set<String> extensions = ProMWizardDisplay.show(context, wizard, new HashSet<String>());

		if (extensions == null || extensions.size() == 0) {
			context.getFutureResult(0).cancel(true);
			return null;
		}

		return ExtensionFilter.removeExtensions(context.getProgress(), log, extensions);
	}

	/*
	 * ProMPropertiesPanel props = new ProMPropertiesPanel("Remove extensions");
	 * //ProMList extList = new ProMList("Extensions", listModel);
	 * props.add(extList);
	 * 
	 */
	private class Step implements ProMWizardStep<Set<String>> {

		public String getTitle() {
			return "Remove extensions";
		}

		private ProMPropertiesPanel panel;
		private ProMList listbox;

		public Step(XLog log) {
			panel = new ProMPropertiesPanel(null);

			DefaultListModel model = new DefaultListModel();

			for (XExtension ext : log.getExtensions()) {
				model.addElement(ext.getName());
			}

			//add a list
			listbox = new ProMList("Extensions", model);
			panel.addProperty("Extensions to be removed", listbox);
		}

		public JComponent getComponent(Set<String> model) {

			listbox.setSelection(model);

			return panel;
		}

		public Set<String> apply(Set<String> model, JComponent component) {
			//set the model to all selected items.
			Set<String> items = new HashSet<String>();
			for (Object o : listbox.getSelectedValues()) {
				items.add((String) o);
			}
			return items;
		}

		public boolean canApply(Set<String> model, JComponent component) {
			return true;
		}

	}

}
