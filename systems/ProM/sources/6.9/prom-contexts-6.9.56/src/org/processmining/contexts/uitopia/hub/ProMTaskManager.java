package org.processmining.contexts.uitopia.hub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.uitopia.api.hub.TaskManager;
import org.deckfour.uitopia.api.model.Action;
import org.deckfour.uitopia.api.model.Resource;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.model.ProMAction;
import org.processmining.contexts.uitopia.model.ProMPOResource;
import org.processmining.contexts.uitopia.model.ProMTask;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.events.PluginLifeCycleEventListener;

public class ProMTaskManager implements TaskManager<ProMTask, ProMPOResource>, PluginLifeCycleEventListener {

	private final Set<ProMTask> tasks = new HashSet<ProMTask>();
	private final Set<ProMTask> activeTasks = new HashSet<ProMTask>();
	private final UIContext context;

	private final Map<PluginContextID, ProMTask> context2task = new HashMap<PluginContextID, ProMTask>();

	private ProMTaskManager(UIContext context) {
		this.context = context;
	}

	private static ProMTaskManager instance = null;

	public static ProMTaskManager initialize(UIContext context) {
		if (instance == null) {
			instance = new ProMTaskManager(context);
		}
		return instance;
	}

	public ProMTask execute(Action action, java.util.List<Collection<? extends Resource>> parameterValues,
			TaskListener listener) throws Exception {

		assert (action instanceof ProMAction);
		java.util.List<Collection<ProMPOResource>> list = new ArrayList<Collection<ProMPOResource>>();
		for (Collection<? extends Resource> v : parameterValues) {
			Collection<ProMPOResource> l = new ArrayList<ProMPOResource>();
			for (Resource r : v) {
				if (!(r instanceof ProMPOResource)) {
					throw new Exception("Cannot instantiate plugins on Connections");
				}
				l.add((ProMPOResource) r);
			}
			list.add(l);
		}

		UIPluginContext pluginContext = context.getMainPluginContext().createChildContext(action.getName());

		pluginContext.getPluginLifeCycleEventListeners().add(this);

		ProMTask task;
		synchronized (context2task) {
			task = new ProMTask(context, (ProMAction) action, list, pluginContext, listener);
			context2task.put(pluginContext.getID(), task);
		}
		return task;
	}

	public java.util.List<ProMTask> getActiveTasks() {
		return new ArrayList<ProMTask>(activeTasks);
	}

	public java.util.List<ProMTask> getAllTasks() {
		return new ArrayList<ProMTask>(tasks);
	}

	public void pluginCancelled(PluginContext context) {
		synchronized (context2task) {
			activeTasks.remove(context2task.get(context.getID()));
			context.getParentContext().deleteChild(context);
		}
	}

	public void pluginCompleted(PluginContext context) {
		synchronized (context2task) {
			activeTasks.remove(context2task.get(context.getID()));
			context.getParentContext().deleteChild(context);
		}
	}

	public void pluginCreated(PluginContext context) {
		synchronized (context2task) {
			tasks.add(context2task.get(context.getID()));
		}
	}

	public void pluginDeleted(PluginContext context) {
		synchronized (context2task) {
			activeTasks.remove(context2task.get(context.getID()));
			context.getParentContext().deleteChild(context);
		}
	}

	public void pluginFutureCreated(PluginContext context) {
		// Gracefully ignore
	}

	public void pluginResumed(PluginContext context) {
		synchronized (context2task) {
			activeTasks.add(context2task.get(context.getID()));
		}
	}

	public void pluginStarted(PluginContext context) {
		synchronized (context2task) {
			activeTasks.add(context2task.get(context.getID()));
		}
	}

	public void pluginSuspended(PluginContext context) {
		synchronized (context2task) {
			activeTasks.remove(context2task.get(context.getID()));
		}
	}

	public void pluginTerminatedWithError(PluginContext context, Throwable t) {
		synchronized (context2task) {
			activeTasks.remove(context2task.get(context.getID()));
			context.getParentContext().deleteChild(context);
		}
	}

	public boolean isActionableResource(Resource r) {
		return r instanceof ProMPOResource;
	}
	
	public boolean isActionableResource(java.util.List<Resource> resources) {
		for(Resource r : resources){
			if(!(r instanceof ProMPOResource)){
				return false;
			}
		}
		return true;
	}

	public boolean isAnActionableResource(java.util.List<Resource> resources) {
		for(Resource r : resources){
			if(r instanceof ProMPOResource){
				return true;
			}
		}
		return false;
	}

}
