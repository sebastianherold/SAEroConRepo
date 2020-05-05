package org.processmining.framework.util.ui.scalableview;

import javax.swing.JComponent;

public interface ScalableComponent {

	public static interface UpdateListener {
		/**
		 * This method should be called if the component is updated (layout,
		 * content, etc.)
		 */
		public void updated();
	}

	/**
	 * Returns the component that will be scaled using getScale and setScale
	 * methods
	 * 
	 * Most implementations will actually override JComponent and return
	 * <code>this</code> in this method.
	 * 
	 * @return a component
	 */
	public JComponent getComponent();

	/**
	 * Returns the current scale.
	 * 
	 * @return the current scale as a double
	 */
	public double getScale();

	/**
	 * Sets the current scale.
	 * <p>
	 * 
	 * @param newValue
	 *            the new scale
	 */
	public void setScale(double newScale);

	/**
	 * Adds an updatelistener to this scalable component. This listener will be
	 * updated as soon as the contents of the component change, i.e. if the
	 * component
	 * 
	 * @param listener
	 */
	public void addUpdateListener(UpdateListener listener);

	/**
	 * Removes an updatelistener from this scalable component.
	 * 
	 * @param listener
	 */
	public void removeUpdateListener(UpdateListener listener);

}
