package org.processmining.contexts.uitopia.model;

import java.util.Collection;
import java.util.List;

import org.deckfour.uitopia.api.model.Action;
import org.deckfour.uitopia.api.model.ResourceType;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;

public class ProMPOResource extends ProMResource<ProvidedObjectID> {

	public ProMPOResource(UIContext context, Action sourceAction, ResourceType resType, ProvidedObjectID id,
			List<Collection<ProMPOResource>> parameterValues) {
		super(context, sourceAction, resType, id, parameterValues);
	}

	public void destroy() {
		isDestroyed = true;
		try {
			context.getProvidedObjectManager().deleteProvidedObject(id);
			for (ProMResource<?> r : context.getResourceManager().getAllResources()) {
				r.parents.remove(this);
			}
		} catch (ProvidedObjectDeletedException e) {
			// THat's fine
		}
	}

	public String getName() {
		try {
			return context.getProvidedObjectManager().getProvidedObjectLabel(id);
		} catch (ProvidedObjectDeletedException e) {
			return "This object is no longer available";
		}
	}

	public void setName(String name) {
		try {
			context.getProvidedObjectManager().relabelProvidedObject(id, name);
			if (listener != null) {
				listener.changed(this, false);
			}
		} catch (ProvidedObjectDeletedException e) {
			// Should not happen, unless destroy has been called
			assert (false);
		}
	}

	/**
	 * Provides access to the object of the type specified by this resource.
	 * 
	 * @return The object carried by this resource.
	 */
	public Object getInstance() {
		try {
			return context.getProvidedObjectManager().getProvidedObjectObject(id, true);
		} catch (ProvidedObjectDeletedException e) {
			return null;
		}
	}

}
