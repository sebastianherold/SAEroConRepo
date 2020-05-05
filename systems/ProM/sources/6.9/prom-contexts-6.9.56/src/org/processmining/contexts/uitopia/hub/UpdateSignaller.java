package org.processmining.contexts.uitopia.hub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.deckfour.uitopia.api.event.UpdateListener;

public class UpdateSignaller {

	private final List<UpdateListener> listeners = new ArrayList<UpdateListener>();

	public UpdateSignaller() {
		super();
	}

	public void addListener(UpdateListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
			listener.updated();
		}
	}

	public Collection<UpdateListener> getListeners() {
		return Collections.unmodifiableCollection(listeners);
	}

	public void removeAllListeners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}

	public void removeListener(UpdateListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void signalUpdate() {
		synchronized (listeners) {
			for (UpdateListener listener : listeners) {
				listener.updated();
			}
		}
	}

}