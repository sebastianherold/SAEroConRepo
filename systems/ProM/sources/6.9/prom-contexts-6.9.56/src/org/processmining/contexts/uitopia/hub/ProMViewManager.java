package org.processmining.contexts.uitopia.hub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import org.deckfour.uitopia.api.hub.ViewManager;
import org.deckfour.uitopia.api.model.Resource;
import org.deckfour.uitopia.api.model.ResourceType;
import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.api.model.ViewType;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.contexts.uitopia.model.ProMViewType;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.util.Pair;

public class ProMViewManager extends UpdateSignaller implements ViewManager {

	private static ProMViewManager instance = null;
	private final UIContext context;
	private final Map<Class<?>, List<ViewType>> viewClasses = new HashMap<Class<?>, List<ViewType>>();
	private final List<View> views;

	private ProMViewManager(UIContext context) {
		this.context = context;
		views = new ArrayList<View>();

		for (ResourceType type : context.getResourceManager().getAllSupportedResourceTypes()) {
			registerResourceType(type);
		}
	}

	public void registerResourceType(final ResourceType type) {
		viewClasses.put(type.getTypeClass(), new ArrayList<ViewType>(0));
		Set<Pair<Integer, PluginParameterBinding>> visualizers = context.getPluginManager().find(Visualizer.class,
				JComponent.class, UIPluginContext.class, true, false, true, type.getTypeClass());
		Set<Pair<Integer, PluginParameterBinding>> cancellableVisualizers = context.getPluginManager().find(Visualizer.class,
				JComponent.class, UIPluginContext.class, true, false, true, type.getTypeClass(), ProMCanceller.class);
		for (Pair<Integer, PluginParameterBinding> binding : visualizers) {
			viewClasses.get(type.getTypeClass()).add(new ProMViewType(this, type, binding));
		}
		for (Pair<Integer, PluginParameterBinding> binding : cancellableVisualizers) {
			viewClasses.get(type.getTypeClass()).add(new ProMViewType(this, type, binding));
		}
		Collections.sort(viewClasses.get(type.getTypeClass()), new Comparator<ViewType>() {

			public int compare(ViewType o1, ViewType o2) {
				if (o1 == o2 || o1.equals(o2)) {
					return 0;
				}
				boolean isO1ExactMatch = o1.getPrimaryType() == type.getTypeClass();
				boolean isO2ExactMatch = o2.getPrimaryType() == type.getTypeClass();
				if (isO1ExactMatch && !isO2ExactMatch) {
					return -1;
				} else if (!isO1ExactMatch && isO2ExactMatch) {
					return 1;
				} else {
					return 0;	
				} 				
			}
		});
	}

	public static ProMViewManager initialize(UIContext context) {
		if (instance == null) {
			instance = new ProMViewManager(context);
		}
		return instance;
	}

	public List<ViewType> getViewTypes(Resource resource) {
		List<ViewType> result = viewClasses.get(resource.getType().getTypeClass());
		return (result == null ? Collections.<ViewType>emptyList() : result);
	}
	
	public List<ViewType> getViewTypes(List<Resource> resources) {
		List<ViewType> views = new ArrayList<ViewType>();
		for(Resource r : resources){
			views.addAll(getViewTypes(r));
		}
		return views;
	}

	public List<View> getViews() {
		return views;
	}

	public void addView(View view) {
		synchronized (views) {
			views.add(view);
			signalUpdate();
		}
	}

	public void removeView(View view) {
		synchronized (views) {
			views.remove(view);
			view.destroy();
			signalUpdate();
		}
	}

	public UIContext getContext() {
		return context;
	}



}
