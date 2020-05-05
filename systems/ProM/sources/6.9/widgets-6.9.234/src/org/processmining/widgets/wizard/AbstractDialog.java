package org.processmining.widgets.wizard;

import javax.swing.JOptionPane;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMHeaderPanel;

public abstract class AbstractDialog<P> extends ProMHeaderPanel implements Dialog<P> {

	private static final long serialVersionUID = -1559194274412334852L;

	private final UIPluginContext context;

	private P parameters;

	private Dialog<P> previous = null;

	private final String title;

	public AbstractDialog(final UIPluginContext context, final String title, final P parameters,
			final Dialog<P> previous) {
		super(title);
		this.parameters = parameters;
		this.previous = previous;
		this.context = context;
		this.title = title;
	}

	protected abstract boolean canProceedToNext();

	protected abstract Dialog<P> determineNextDialog();

	public final Dialog<P> getNext() {
		if (canProceedToNext()) {
			updateParametersOnGetNext();
			return determineNextDialog();
		} else {
			JOptionPane.showMessageDialog(this, getUserInputProblems(), "Incomplete Parameter Selection",
					JOptionPane.WARNING_MESSAGE);
			return this;
		}
	}

	public P getParameters() {
		return parameters;
	}

	public Dialog<P> getPrevious() {
		updateParametersOnGetPrevious();
		return previous;
	}

	public String getTitle() {
		return title;
	}

	public UIPluginContext getUIPluginContext() {
		return context;
	}

	protected abstract String getUserInputProblems();

	public boolean hasPreviousDialog() {
		return previous != null;
	}

	public void setParameters(P parameters) {
		this.parameters = parameters;
	}
}
