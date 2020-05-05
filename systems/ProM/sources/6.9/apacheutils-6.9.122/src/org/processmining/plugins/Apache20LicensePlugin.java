package org.processmining.plugins;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.help.Apache20LicenseHelp;
import org.processmining.models.Apache20License;

@Plugin(name = "Show Apache License, Version 2.0", parameterLabels = { }, returnLabels = { "Apache License, Version 2.0" }, returnTypes = { Apache20License.class }, help = Apache20LicenseHelp.TEXT)
public class Apache20LicensePlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Show Apache License, Version 2.0", requiredParameterLabels = { })
	public Apache20License show(UIPluginContext context) {
		return new Apache20License();
	}
}
