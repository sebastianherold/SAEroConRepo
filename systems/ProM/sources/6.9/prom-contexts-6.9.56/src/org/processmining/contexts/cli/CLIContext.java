package org.processmining.contexts.cli;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.impl.AbstractGlobalContext;

public class CLIContext extends AbstractGlobalContext {

	private final CLIPluginContext mainPluginContext;

	public CLIContext() {
		super();

		mainPluginContext = new CLIPluginContext(this, "Main Plugin Context");
	}

	@Override
	protected CLIPluginContext getMainPluginContext() {
		return mainPluginContext;
	}

	@Override
	public Class<? extends PluginContext> getPluginContextType() {
		return CLIPluginContext.class;
	}

}
