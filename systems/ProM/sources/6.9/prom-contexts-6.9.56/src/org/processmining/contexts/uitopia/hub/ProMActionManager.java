package org.processmining.contexts.uitopia.hub;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.uitopia.api.hub.ActionManager;
import org.deckfour.uitopia.api.model.ActionType;
import org.deckfour.uitopia.api.model.Parameter;
import org.deckfour.uitopia.api.model.ResourceType;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.contexts.uitopia.model.ProMAction;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginManager.PluginManagerListener;
import org.processmining.framework.plugin.PluginParameterBinding;

public class ProMActionManager implements ActionManager<ProMAction>, PluginManagerListener {

	private final SortedSet<ProMAction> actions = new TreeSet<ProMAction>();
	private final UIContext context;

	private ProMActionManager(UIContext context) {

		this.context = context;
		for (PluginDescriptor plugin : context.getPluginManager().getAllPlugins(true)) {
			addPlugin(plugin);
		}
		context.getPluginManager().addListener(this);
	}

	private void addPlugin(PluginDescriptor plugin) {
		for (int i = 0; i < plugin.getNumberOfMethods(); i++) {
			// See if this is an action
			if (checkPlugin(context, plugin, i)) {
				// Construct an action
				ProMAction action = new ProMAction(context.getResourceManager(), context.getPluginManager(), plugin, i);
				actions.add(action);
			}
		}

	}

	private static ProMActionManager instance = null;

	public static ProMActionManager initialize(UIContext context) {
		if (instance == null) {
			instance = new ProMActionManager(context);
		}
		return instance;
	}

	/**
	 * Returns a list of actions, such that these actions accepts all given
	 * input resources and result in at least on of the output resources.
	 */
	public List<ProMAction> getActions(List<ResourceType> input, List<ResourceType> output) {
		return getActions(input, output, null);
	}

	private Collection<ResourceType> getResourcesTypesFor(List<? extends Parameter> parameters) {
		Collection<ResourceType> types = new HashSet<ResourceType>();
		for (Parameter par : parameters) {
			types.add(par.getType());
		}

		return types;
	}

	public List<ProMAction> getActions(List<ResourceType> parameters, List<ResourceType> requiredOutput, ActionType type) {

		List<ProMAction> enabledActions = new ArrayList<ProMAction>();

		actionLoop: for (ProMAction action : actions) {

			// Check for type match
			if ((type != null) && !action.getType().equals(type)) {
				// types don't match
				continue;
			}

			// Check if all given output types are present in the plugin's output
			Collection<ResourceType> outputs = getResourcesTypesFor(action.getOutput());
			for (ResourceType required : requiredOutput) {
				boolean found = false;
				for (ResourceType output : outputs) {
					found |= required.isAssignableFrom(output);
					if (found) {
						break;
					}
				}
				if (!found) {
					continue actionLoop;
				}
			}

			// Check for enabledness using a pluginparamterbinding
			Class<?>[] types = new Class<?>[parameters.size()];
			int i = 0;
			for (ResourceType r : parameters) {
				types[i++] = r.getTypeClass();
			}
			List<PluginParameterBinding> bindings = PluginParameterBinding.Factory.tryToBind(
					context.getPluginManager(), action.getPlugin(), action.getMethodIndex(), false, false, types);
			bindings.addAll(PluginParameterBinding.Factory.tryToBind(context.getPluginManager(), action.getPlugin(),
					action.getMethodIndex(), true, false, types));

			if (bindings.isEmpty()) {
				continue;
			}

			enabledActions.add(action);
		}

		return enabledActions;
	}

	public List<ProMAction> getActions() {
		return new ArrayList<ProMAction>(actions);
	}

	private boolean checkPlugin(UIContext context, PluginDescriptor plugin, int methodIndex) {
		if (plugin.getAnnotation(Visualizer.class) != null) {
			return false;
		}
		if (plugin.getAnnotation(UIImportPlugin.class) != null) {
			return false;
		}
		if (plugin.getAnnotation(UIExportPlugin.class) != null) {
			return false;
		}
		if (plugin.getAnnotation(UITopiaVariant.class, methodIndex) == null) {
			return false;
		}
		for (int p = 0; p < plugin.getParameterNames().size(); p++) {
			Class<?> type = plugin.getPluginParameterType(methodIndex, p);
			if ((type != null) && !context.getResourceManager().isResourceType(type)) {
				return false;
			}
		}
		return true;
	}

	public void error(URL source, Throwable t, String className) {
		System.err.println("Error while adding plugin from " + className);
	}

	public void newPlugin(PluginDescriptor plugin, Collection<Class<?>> newTypes) {
		// First, register the new resource types with the resource manager
		for (Class<?> newType : newTypes) {
			context.getResourceManager().addType(newType);
		}
		// Now add the plugins.
		addPlugin(plugin);

		if (plugin.getAnnotation(Visualizer.class) != null) {
			HashSet<Class<?>> parTypes = new HashSet<Class<?>>();
			for (int i = 0; i < plugin.getNumberOfMethods(); i++) {
				parTypes.addAll(plugin.getParameterTypes(i));
			}
			for (Class<?> parType : parTypes) {
				// this plugin might be a visualizes for any of its parameter types, so
				// notify the viewmanager of this.
				context.getViewManager().registerResourceType(context.getResourceManager().getResourceTypeFor(parType));
			}
		}
		if (plugin.getAnnotation(UIImportPlugin.class) != null) {
			// added an import plugin, signal the resource manager.
			context.getResourceManager().addedImportPlugins();
		}
		//		if (plugin.getAnnotation(UIExportPlugin.class) != null) {
		// Export plugins are built on the fly anyway.
		//		}

	}
}
