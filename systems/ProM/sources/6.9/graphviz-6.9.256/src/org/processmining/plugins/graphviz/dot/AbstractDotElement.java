package org.processmining.plugins.graphviz.dot;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;

public abstract class AbstractDotElement implements DotElement {
	private final String id;
	private Map<String, String> optionsMap;
	private String label;

	private boolean selectable = false;
	private final List<DotElementSelectionListener> selectionListeners = new CopyOnWriteArrayList<>();
	private final List<MouseListener> mouseListeners = new CopyOnWriteArrayList<MouseListener>();

	public AbstractDotElement() {
		id = "e" + UUID.randomUUID().toString();
		label = "";
		optionsMap = new HashMap<>();
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String labelToString() {
		return escapeString(label);
	}
	
	protected String escapeString(String value) {
		if (value.length() > 2 && value.substring(0, 1).equals("<")
				&& value.substring(value.length() - 1, value.length()).equals(">")) {
			return value;
		} else {
			String value2 = value.replace("\"", "\\\"");
			return "\"" + value2 + "\"";
		}
	}

	@Override
	public void setOption(String key, String value) {
		optionsMap.put(key, value);
	}
	
	@Override
	public String getOption(String key) {
		if (optionsMap.containsKey(key)) {
			return optionsMap.get(key);
		}
		return null;
	}
	
	@Override
	public Set<String> getOptionKeySet() {
		return Collections.unmodifiableSet(optionsMap.keySet());
	}

	public void addMouseListener(MouseListener l) {
		mouseListeners.add(l);
	}

	public void mouseClicked(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mouseClicked(e);
		}
	}

	public void mouseEntered(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mouseEntered(e);
		}
	}

	public void mouseExited(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mouseExited(e);
		}
	}

	public void mousePressed(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mousePressed(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		for (MouseListener l : mouseListeners) {
			l.mouseReleased(e);
		}
	}

	//selection stuff
	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public boolean isSelectable() {
		return selectable;
	}
	
	public void addSelectionListener(DotElementSelectionListener listener) {
		setSelectable(true);
		selectionListeners.add(listener);
	}
	
	public List<DotElementSelectionListener> getSelectionListeners() {
		return Collections.unmodifiableList(selectionListeners);	
	}
}
