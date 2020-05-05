package org.processmining.basicutils.plugins;

import org.processmining.basicutils.help.LGPLLicenseHelp;
import org.processmining.basicutils.models.LGPLLicense;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Show L-GPL License", parameterLabels = { }, returnLabels = { "Apache License, Version 2.0" }, returnTypes = { LGPLLicense.class }, help = LGPLLicenseHelp.TEXT)
public class LGPLLicensePlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Show L-GPL License", requiredParameterLabels = { })
	public LGPLLicense runUI(UIPluginContext context) {
		return new LGPLLicense();
	}

	/**
	 * @deprecated Use runUI instead.
	 */
	@Deprecated
	public LGPLLicense show(UIPluginContext context) {
		return runUI(context);
	}
}
