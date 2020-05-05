package org.processmining.contexts.uitopia.model;

import java.util.Collection;
import java.util.List;

import org.deckfour.uitopia.api.model.Action;
import org.deckfour.uitopia.api.model.ResourceType;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;

public class ProMCResource extends ProMResource<ConnectionID> {

	public ProMCResource(UIContext context, Action sourceAction, ResourceType resType, ConnectionID id,
			List<Collection<ProMPOResource>> parameterValues) {
		super(context, sourceAction, resType, id, parameterValues);
		// TODO Auto-generated constructor stub
	}

	public void destroy() {
		isDestroyed = true;
		try {
			for (ProMResource<?> r : context.getResourceManager().getAllResources()) {
				r.parents.remove(this);
			}
			context.getConnectionManager().getConnection(id).remove();
		} catch (ConnectionCannotBeObtained e) {
		}

	}

	public String getName() {
		try {
			return context.getConnectionManager().getConnection(id).getLabel();
		} catch (ConnectionCannotBeObtained e) {
			isDestroyed = true;
			return "This object is no longer available";
		}
	}

	public void setName(String name) {
		try {
			context.getConnectionManager().getConnection(id).setLabel(name);
			if (listener != null) {
				listener.changed(this, false);
			}
		} catch (ConnectionCannotBeObtained e) {
			isDestroyed = true;
		}
	}

	/**
	 * Provides access to the object of the type specified by this resource.
	 * 
	 * @return The object carried by this resource.
	 */
	public Connection getInstance() {
		try {
			return context.getConnectionManager().getConnection(id);
		} catch (ConnectionCannotBeObtained e) {
			isDestroyed = true;
			return null;
		}
	}
}
