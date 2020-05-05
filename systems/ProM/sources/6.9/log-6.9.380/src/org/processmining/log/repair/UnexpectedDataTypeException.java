package org.processmining.log.repair;

public final class UnexpectedDataTypeException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnexpectedDataTypeException() {
	}

	public UnexpectedDataTypeException(String message) {
		super(message);
	}

	public UnexpectedDataTypeException(Throwable cause) {
		super(cause);
	}

	public UnexpectedDataTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
