package org.processmining.contexts.uitopia.model;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;

import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.uitopia.api.model.ResourceType;
import org.deckfour.uitopia.api.model.Task;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.events.Logger;
import org.processmining.framework.plugin.events.PluginLifeCycleEventListener;
import org.processmining.framework.plugin.events.ProgressEventListener;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;

public class ProMTask implements Task<ProMPOResource>, ProgressEventListener, Logger, PluginLifeCycleEventListener {

	private final ProMAction action;
	private PluginExecutionResult result;
	private int lowbo = 0, upbo = 0, progress = 0;
	private final TaskListener listener;
	private final UIContext context;
	private final java.util.List<Collection<ProMPOResource>> parameterValues;
	private final java.util.List<ProvidedObjectID> providedObjectIds;
	private boolean active;
	private final UIPluginContext pluginContext;

	public ProMTask(UIContext context, ProMAction action, java.util.List<Collection<ProMPOResource>> parameterValues,
			UIPluginContext pluginContext, TaskListener listener) {
		this.context = context;
		this.action = action;
		this.parameterValues = parameterValues;
		this.pluginContext = pluginContext;
		this.listener = listener;
		changeProgressIndeterminate(true);

		// In this constructor, the task should set up a context for the plugin
		// carried by the action. All resources of the action should have values,
		// i.e. the action should be executable. 
		//		assert (action.getStatus(parameterValues) == ActionStatus.EXECUTABLE);

		// Then, using the resources attached to the input parameters of the 
		// action, possibly some other input parameters need to be found, for example
		// be scanning for connections. This is up to the implementing classes.
		pluginContext.getProgressEventListeners().add(this);
		pluginContext.getLoggingListeners().add(this);
		pluginContext.getPluginLifeCycleEventListeners().add(this);

		Object[] args = unpackResourceCollections(parameterValues);

		// The plugin should be invoked and this task should attach itself as 
		// a progress listener
		pluginContext.setTask(this);
		synchronized (this) {
			active = true;
			pluginContext.getParentContext().getPluginLifeCycleEventListeners().firePluginCreated(pluginContext);
			result = action.getPlugin().invoke(action.getMethodIndex(), pluginContext, args);
			providedObjectIds = context.getProvidedObjectManager().createProvidedObjects(pluginContext);
		}
	}

	public void destroy() {
		if (result != null) {
			if (pluginContext.getProgress() != null) {
				// We have a progress. Have it take care of the futures and such.
				pluginContext.getProgress().cancel();
			} else {
				// We cancel all futures that might exist in this
				// result. Canceling the first only would also work,
				// but this is potentially faster.
				try {
					for (int i = 0; i < result.getSize(); i++) {
						Object o = result.getResult(i);
						if (o instanceof ProMFuture<?>) {
							result.<ProMFuture<?>>getResult(i).cancel(!action.handlesCancel());
						}
					}
				} catch (NullPointerException _) {
					// Happens when task terminates before we can cancel all children
				}
			}
			for (ProvidedObjectID id : getProvidedObjectIds()) {
				try {
					context.getProvidedObjectManager().deleteProvidedObject(id);
				} catch (ProvidedObjectDeletedException e) {
					//Ignore
				}
			}
			result = null;
		}
		progress = upbo;
		listener.updateProgress(1.0);
	}

	public ProMAction getAction() {
		return action;
	}

	public double getProgress() {
		return (double) progress / (double) (upbo - lowbo);
	}

	public InteractionResult showConfiguration(String title, JComponent configuration) {
		return listener.showConfiguration(title, configuration);
	}

	public InteractionResult showWizard(String title, boolean first, boolean last, JComponent configuration) {
		return listener.showWizard(title, first, last, configuration);
	}

	public java.util.List<Collection<ProMPOResource>> getParameterValues() {
		//		java.util.List<Collection<ProMResource<?>>> list = new ArrayList<Collection<ProMResource<?>>>();
		//		for (Collection<ProMPOResource> v : parameterValues) {
		//			Collection<ProMResource<?>> l = new ArrayList<ProMResource<?>>(v);
		//			list.add(l);
		//		}
		return parameterValues;
	}

	private Object[] unpackResourceCollections(java.util.List<Collection<ProMPOResource>> parameters) {
		Object[] objects = new Object[parameters.size()];

		for (int i = 0; i < parameters.size(); i++) {
			if (action.getInput().get(i).getMaxCardinality() == 1) {
				objects[i] = parameters.get(i).iterator().next().getInstance();
			} else {
				Object[] list = (Object[]) Array.newInstance(action.getInput().get(i).getType().getTypeClass(),
						parameters.get(i).size());
				Iterator<ProMPOResource> it = parameters.get(i).iterator();
				int j = 0;
				while (it.hasNext()) {
					list[j++] = it.next().getInstance();
				}
				objects[i] = list;
			}
		}

		return objects;

	}

	//****************ProgressEventListener Interface************************

	public void changeProgress(int progress) {
		this.progress = progress;
		lowbo = Math.min(lowbo, progress);
		upbo = Math.max(upbo, progress);
		listener.updateProgress(getProgress());
	}

	public void changeProgressBounds(int lowBo, int upBo) {
		lowbo = lowBo;
		upbo = upBo;
		progress = 0;
		listener.updateProgress(Math.max(Math.min(lowBo, progress), upBo));
	}

	public void changeProgressCaption(String newCaption) {
		// Gracefully ignore.
	}

	public void changeProgressIndeterminate(boolean indeterminate) {
		if (indeterminate) {
			lowbo = 0;
			upbo = 1;
			progress = 0;
			listener.updateProgress(0.5);
		}
	}

	//****************Logger Interface************************
	public void log(String message, PluginContextID contextID, MessageLevel messageLevel) {
		switch (messageLevel) {
			case DEBUG :
				listener.debug(message);
				break;
			case NORMAL :
				listener.info(message);
				break;
			case ERROR :
				listener.error(message);
				break;
			case WARNING :
				listener.warning(message);
				break;
			case TEST :
				listener.debug(message);
				break;
		}
	}

	public void log(Throwable t, PluginContextID contextID) {
		listener.error(t.getMessage(), t);
	}

	public void pluginCancelled(PluginContext context) {
		// Gracefully ignore. Only the completed is of interest
		listener.completed();
		active = false;
		result = null;
		changeProgress(upbo);
	}

	public void pluginCompleted(PluginContext pluginContext) {
		ProMPOResource[] setResult = null;
		int sig = action.getMostSignificantResultIndex();
		if (sig < 0) {
			// One of the inputs should be returned as the most significant result
			// find the corresponding parameter for this plugin variant.
			sig = action.getPlugin().getIndexInMethod(action.getMethodIndex(), -sig - 1);

			Collection<ProMPOResource> par = parameterValues.get(sig);
			// Visualize the first resource parameter in the end.
			setResult = par.toArray(new ProMPOResource[0]);
			sig = -1;
		}
		for (int i = 0; i < result.getSize(); i++) {
			ProvidedObjectID id = result.getProvidedObjectID(i);
			Class<?> type;
			try {
				type = context.getProvidedObjectManager().getProvidedObjectType(id);
			} catch (ProvidedObjectDeletedException e) {
				// If the object has been deleted, try the next one
				continue;
			}
			ResourceType resType = context.getResourceManager().getResourceTypeFor(type);
			if (resType != null) {
				ProMPOResource res = new ProMPOResource(context, action, resType, id, parameterValues);
				res = context.getResourceManager().addResource(id, res);

				if (i + 1 == sig) {
					res.setFavorite(true);
					setResult = new ProMPOResource[] { res };
				}
			}
		}
		listener.completed(setResult);
		active = false;
		result = null;
		changeProgress(upbo);
	}

	public void pluginCreated(PluginContext pluginContext) {
		// Gracefully ignore. Only the completed is of interest
	}

	public void pluginDeleted(PluginContext pluginContext) {
		// Gracefully ignore. Only the completed is of interest
	}

	public void pluginFutureCreated(PluginContext pluginContext) {
		// Gracefully ignore. Only the completed is of interest
	}

	public void pluginResumed(PluginContext pluginContext) {
		// Gracefully ignore. Only the completed is of interest
	}

	public void pluginStarted(PluginContext pluginContext) {
		// Gracefully ignore. Only the completed is of interest
	}

	public void pluginSuspended(PluginContext pluginContext) {
		// Gracefully ignore. Only the completed is of interest
	}

	public void pluginTerminatedWithError(PluginContext pluginContext, Throwable t) {
		// Gracefully ignore. Only the completed is of interest
		listener.completed();
		active = false;
		result = null;
		changeProgress(upbo);
	}

	public java.util.List<ProvidedObjectID> getProvidedObjectIds() {
		return providedObjectIds;
	}

	public String toString() {
		return (active ? "[A]" : "[I]") + action.toString();
	}

}
