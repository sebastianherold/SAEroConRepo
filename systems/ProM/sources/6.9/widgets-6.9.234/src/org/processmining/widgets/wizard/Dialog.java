package org.processmining.widgets.wizard;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.UIPluginContext;

/**
 * @author svzelst
 *
 * @param
 * 			<P>
 *            parameters object
 */
public interface Dialog<P> {

	Dialog<P> getNext();

	P getParameters();

	Dialog<P> getPrevious();

	String getTitle();

	UIPluginContext getUIPluginContext();

	boolean hasNextDialog();

	boolean hasPreviousDialog();

	void setParameters(P parameters);

	void updateParametersOnGetNext();

	void updateParametersOnGetPrevious();

	JComponent visualize();

}
