package org.processmining.contexts.uitopia.model;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.uitopia.api.model.Action;
import org.deckfour.uitopia.api.model.Resource;
import org.deckfour.uitopia.api.model.ResourceType;
import org.deckfour.uitopia.api.model.View;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.framework.ProMID;

public abstract class ProMResource<I extends ProMID> implements Resource {

	public static interface Listener {
		public void changed(ProMResource<?> resource, boolean fullUpdate);
	}

	protected final ResourceType resType;
	protected boolean favorite = false;
	protected long creationTime;
	protected long lastAccessTime = System.currentTimeMillis();
	protected final I id;
	protected final Set<ProMPOResource> parents;
	protected final UIContext context;
	protected View view = null;
	protected final Action sourceAction;
	protected boolean isDestroyed = false;
	protected Listener listener;

	private static Long currentTime = System.currentTimeMillis();

	public ProMResource(UIContext context, Action sourceAction, ResourceType resType, I id,
			List<Collection<ProMPOResource>> parameterValues) {
		assert resType != null;
		this.sourceAction = sourceAction;
		this.resType = resType;
		this.id = id;
		synchronized (currentTime) {
			currentTime = Math.max(currentTime + 1, System.currentTimeMillis());
			creationTime = currentTime;
		}
		parents = new HashSet<ProMPOResource>();
		this.context = context;

		for (Collection<ProMPOResource> col : parameterValues) {
			parents.addAll(col);
		}

	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void setParents(Set<ProMPOResource> parents) {
		this.parents.clear();
		this.parents.addAll(parents);
	}

	public Date getCreationTime() {
		return new Date(creationTime);
	}

	public Date getLastAccessTime() {
		return new Date(lastAccessTime);
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public Collection<ProMPOResource> getParents() {
		return Collections.unmodifiableCollection(parents);
	}

	public Image getPreview(int maxWidth, int maxHeight) {
		if (view == null) {
			return resType.getTypeIcon();
		}
		return view.getPreview(maxWidth, maxHeight);
	}

	public void setView(ProMView view) {
		this.view = view;
	}

	public Action getSourceAction() {
		return sourceAction;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
		if (listener != null) {
			listener.changed(this, false);
		}
	}

	public void updateLastAccessTime() {
		lastAccessTime = System.currentTimeMillis();
	}

	public ResourceType getType() {
		return resType;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ProMResource)) {
			return false;
		}
		ProMResource<?> p = (ProMResource<?>) o;
		return p.id.equals(id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return getName();
	}

	public ProMID getID() {
		return id;
	}

	public boolean isDestroyed() {
		return isDestroyed;
	}

	public abstract Object getInstance();
}
