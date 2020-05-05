package org.processmining.framework.util.ui.scalableview.interaction;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableComponent.UpdateListener;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;

/**
 * This interaction panel is used in the ProMScalableView. It is interacting
 * with the view.
 * 
 * 
 * @author bfvdonge
 * 
 */
public interface ViewInteractionPanel extends UpdateListener {

	/**
	 * Returns the name of the panel. This name is shown in the user interface
	 * when the component is not shown.
	 * 
	 * @return
	 */
	public String getPanelName();

	/**
	 * Returns the component that is shown if this panel is activated by the
	 * user.
	 * 
	 * Most implementations will actually override JComponent and return
	 * <code>this</code> in this method.
	 * 
	 * @return a component
	 */
	public JComponent getComponent();

	/**
	 * This method is called by the ProM scalable view as soon as the panel is
	 * added to this view. The scalable provided can be used for interaction.
	 */
	public void setScalableComponent(ScalableComponent scalable);

	/**
	 * This method is called by the ProM scalable view as soon as the panel is
	 * added to this view. The scalable provided can be used for interaction.
	 * 
	 * @param viewPanel
	 */
	public void setParent(ScalableViewPanel viewPanel);

	/**
	 * Returns the preferred height of this component when shown to the user. A
	 * value between 0 and 1 (including 1) indicates a requested height relative
	 * to the height of the screen.
	 * 
	 * A value greater than 1 indicates a fixed height in pixels .
	 * 
	 * @return
	 */
	public double getHeightInView();

	/**
	 * Returns the preferred width of this component when shown to the user. A
	 * value between 0 and 1 (including 1) indicates a requested width relative
	 * to the width of the screen.
	 * 
	 * A value greater than 1 indicates a fixed width in pixels.
	 * 
	 * @return
	 */
	public double getWidthInView();

	/**
	 * This method is called by the ProMScalableView panel just before the
	 * component of this interaction panel will be shown to the user or hidden
	 * from the user.
	 * 
	 * The height and width of the component are set to what is requested by
	 * getHeightInView() and getWidthInView();
	 * 
	 * @param to
	 *            TODO
	 * 
	 */
	public void willChangeVisibility(boolean to);
}
