package org.processmining.framework.util.ui.widgets.helper;

/**
 * Thrown by most methods in {@link ProMUIHelper} in case the user cancels an
 * operation.
 * 
 * @author F. Mannhardt
 * 
 */
public class UserCancelledException extends Exception {

	private static final long serialVersionUID = 1L;

	public UserCancelledException() {
		super("User Cancelled");
	}

	public UserCancelledException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserCancelledException(String message) {
		super(message);
	}

	public UserCancelledException(Throwable cause) {
		super(cause);
	}

}
