package org.processmining.framework.util.ui.widgets.logging;

import java.util.logging.Logger;

/**
 * @author michael
 * 
 */
public final class ProMLoggable implements Loggable {
	private transient final Logger l;

	/**
	 * @param l
	 */
	public ProMLoggable(final Logger l) {
		this.l = l;
	}

	/**
	 * @see org.processmining.framework.util.ui.widgets.logging.Loggable#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return l;
	}
}