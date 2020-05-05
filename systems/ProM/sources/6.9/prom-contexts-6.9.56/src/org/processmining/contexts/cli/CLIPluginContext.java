package org.processmining.contexts.cli;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.impl.AbstractPluginContext;

public class CLIPluginContext extends AbstractPluginContext {

	private final Executor executor;

	public CLIPluginContext(GlobalContext context, String label) {
		super(context, label);
		// This context is NOT a child of another context,
		// hence should behave in an asynchronous way.
		executor = Executors.newCachedThreadPool();
		progress = new CLIProgressBar(this);
	}

	protected CLIPluginContext(CLIPluginContext context, String label) {
		super(context, label);
		progress = new CLIProgressBar(this);
		// This context is a child of another context,
		// hence should behave in a synchronous way.
		if (context/*.getParentContext()*/ == null) {
			// this context is on the first level below the user-initiated
			// plugins
			executor = Executors.newCachedThreadPool();
		} else {
			// all subtasks take the pool of the parent.
			executor = context.getExecutor();
		}
	}

	@Override
	protected synchronized PluginContext createTypedChildContext(String label) {
		return new CLIPluginContext(this, label);
	}

	public Executor getExecutor() {
		return executor;
	}

	@Override
	public Progress getProgress() {
		return progress;
	}

	@Override
	public CLIContext getGlobalContext() {
		return (CLIContext) super.getGlobalContext();
	}

	@Override
	public CLIPluginContext getRootContext() {
		return (CLIPluginContext) super.getRootContext();
	}
}
