package org.processmining.plugins.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.deckfour.uitopia.api.model.ResourceType;
import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.api.model.ViewType;
import org.deckfour.uitopia.ui.UITopiaController;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.model.ProMPOResource;
import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.contexts.uitopia.model.ProMTask;
import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.framework.providedobjects.ProvidedObjectManager;

/**
 * @author michael
 * @author F. Mannhardt
 * 
 */
public class ProvidedObjectHelper {
	/**
	 * Will publish object as a provided object with the class specified by
	 * clazz. If the context is a UIPluginContext and favorite is true, it will
	 * be marked as a favorite object as well. Finally, if it is marked as
	 * favorite, it will also be show and ProM will switch to the Views tab.
	 * Encapsulation is for scared little boys/girls who still believe in the
	 * invisible pink unicorn (IPU).
	 * 
	 * @param <T>
	 * @param context
	 * @param name
	 * @param object
	 * @param clazz
	 * @param favorite
	 */
	public static <T> void publish(final PluginContext context, final String name, final T object,
			final Class<? super T> clazz, final boolean favorite) {
		final ProvidedObjectID id = context.getProvidedObjectManager().createProvidedObject(name, object, clazz,
				context);
		// Do not look at the rest of this method.  Boudewijn was busy and I just decided to do what is necessary.
		// It works.  It probably breaks if somebody changes anything.  Here or elsewhere.

		if (context instanceof UIPluginContext) {
			final GlobalContext gcontext = ((UIPluginContext) context).getGlobalContext();
			if (gcontext instanceof UIContext) {
				final UIContext uicontext = (UIContext) gcontext;
				final ResourceType resType = uicontext.getResourceManager().getResourceTypeFor(clazz);
				if (resType != null) {
					ProMTask task = null;
					try {
						final Field taskField = context.getClass().getDeclaredField("task");
						taskField.setAccessible(true);
						task = (ProMTask) taskField.get(context);
					} catch (final Exception _) {
						// Guess it wasn't meant to be, then...
					}
					final List<Collection<ProMPOResource>> lst = Collections.emptyList();
					ProMPOResource res = new ProMPOResource(uicontext, task == null ? null : task.getAction(), resType,
							id, lst);
					res = uicontext.getResourceManager().addResource(id, res);
				}
			}
		}
		if (favorite) {
			ProvidedObjectHelper.setFavorite(context, object);
			ProvidedObjectHelper.raise(context, object);
		}
	}

	/**
	 * Show the visualizer for the provided object
	 * 
	 * @param context
	 * @param object
	 */
	public static void raise(final PluginContext context, final Object object) {
		if (context instanceof UIPluginContext) {
			final GlobalContext gcontext = ((UIPluginContext) context).getGlobalContext();
			if (gcontext instanceof UIContext) {
				final UIContext uicontext = (UIContext) gcontext;
				final ProMResource<?> res = uicontext.getResourceManager().getResourceForInstance(object);
				final List<ViewType> viewTypes = uicontext.getViewManager().getViewTypes(res);
				if (viewTypes.size() > 0) {
					final ViewType viewType = viewTypes.get(0);
					final View view = viewType.createView(res);
					uicontext.getViewManager().addView(view);
					final UITopiaController controller = uicontext.getController();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							controller.getMainView().showViewsView();
							controller.getMainView().getViewsView().showFullScreen(view);
						}
					});
				}
			}
		}
	}

	/**
	 * @param context
	 * @param object
	 */
	public static void setFavorite(final PluginContext context, final Object object) {
		ProvidedObjectHelper.setFavorite(context, object, true);
	}

	/**
	 * @param context
	 * @param object
	 * @param favorite
	 */
	public static void setFavorite(final PluginContext context, final Object object, final boolean favorite) {
		if (context instanceof UIPluginContext) {
			final GlobalContext gcontext = ((UIPluginContext) context).getGlobalContext();
			if (gcontext instanceof UIContext) {
				final UIContext uicontext = (UIContext) gcontext;
				final ProMResource<?> res = uicontext.getResourceManager().getResourceForInstance(object);
				try {
					res.setFavorite(favorite);
				} catch (final Exception _) {
					// Ignore
				}
			}
		}
	}

	/**
	 * List all the provided objects of the type
	 * 
	 * @param context
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> getProvidedObjects(final PluginContext context, final Class<T> clazz) {
		Collection<T> filteredObjects = new ArrayList<>();
		ProvidedObjectManager pom = context.getProvidedObjectManager();
		List<ProvidedObjectID> allProvidedObjects = pom.getProvidedObjects();
		for (ProvidedObjectID id : allProvidedObjects) {
			try {
				Class<?> type = pom.getProvidedObjectType(id);
				if (clazz.equals(type)) {
					filteredObjects.add((T) pom.getProvidedObjectObject(id, false));
				}
			} catch (ProvidedObjectDeletedException e) {
				//Ignore
			}
		}
		return filteredObjects;
	}

	/**
	 * Rename the provided object
	 * 
	 * @param context
	 * @param obj
	 *            the object instance
	 * @param newName
	 */
	public static void changeProvidedObjectName(final PluginContext context, final Object obj, final String newName) {
		ProvidedObjectManager pom = context.getProvidedObjectManager();
		for (ProvidedObjectID id : pom.getProvidedObjects()) {
			try {
				Object providedObj = pom.getProvidedObjectObject(id, false);
				if (obj != null && obj.equals(providedObj)) {
					pom.relabelProvidedObject(id, newName);
				}
			} catch (ProvidedObjectDeletedException e) {
			}
		}
	}

	/**
	 * Returns the label of the provided object
	 * 
	 * @param context
	 * @param obj
	 * @return
	 */
	public static String getProvidedObjectLabel(final PluginContext context, Object obj) {
		ProvidedObjectManager pom = context.getProvidedObjectManager();
		for (ProvidedObjectID id : pom.getProvidedObjects()) {
			try {
				Object providedObj = pom.getProvidedObjectObject(id, false);
				if (obj != null && obj.equals(providedObj)) {
					return pom.getProvidedObjectLabel(id);
				}
			} catch (ProvidedObjectDeletedException e) {
			}
		}
		return null;
	}

}
