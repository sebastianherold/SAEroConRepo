package org.processmining.widgets.wizard;

import org.processmining.contexts.uitopia.UIPluginContext;

public abstract class AbstractRoutableDialog<P> extends AbstractDialog<P> {

	private static final long serialVersionUID = 191742936970950461L;
	private final Route<P> route;

	public AbstractRoutableDialog(UIPluginContext context, String title, P parameters, Dialog<P> previous,
			Route<P> route) {
		super(context, title, parameters, previous);
		this.route = route;
	}

	public boolean hasNextDialog() {
		return route == null ? false : route.getNext(this) != null;
	}

	protected Dialog<P> determineNextDialog() {
		return route == null ? null : route.getNext(this);
	}
}
