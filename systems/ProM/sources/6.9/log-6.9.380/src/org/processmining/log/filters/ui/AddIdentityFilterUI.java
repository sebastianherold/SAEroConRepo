package org.processmining.log.filters.ui;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.filters.AddIdentityFilter;

@Plugin(name = "Add identity attribute", returnLabels = { "Log with identities" }, returnTypes = {
		XLog.class }, parameterLabels = { "Log" })
public class AddIdentityFilterUI {

	@UITopiaVariant(uiLabel = "Add identities to log", affiliation = UITopiaVariant.EHV, author = "J.M.E.M. van der Werf", email = "j.m.e.m.v.d.werf@tue.nl", pack = "Ontologies")
	@PluginVariant(variantLabel = "Add identities to log", requiredParameterLabels = { 0 })
	public XLog addIdentitiesUI(UIPluginContext context, XLog log) {
		return addIdentities(context, log);
	}

	@PluginVariant(variantLabel = "Add identities to log", requiredParameterLabels = { 0 })
	public XLog addIdentities(PluginContext context, XLog log) {
		XLog filtered = AddIdentityFilter.addIdentities(context, log);
		if (filtered != null) {
			String name = XConceptExtension.instance().extractName(log);
			if (name == null) {
				name = "Log";
			}
			context.getFutureResult(0).setLabel(name + " with identities");
			return filtered;
		} else {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}

}
