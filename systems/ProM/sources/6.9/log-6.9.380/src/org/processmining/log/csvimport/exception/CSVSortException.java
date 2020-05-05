package org.processmining.log.csvimport.exception;

/**
 * @author F. Mannhardt
 *
 */
public class CSVSortException extends CSVConversionException {

	private static final long serialVersionUID = 5352796938595731289L;

	public CSVSortException() {
	}

	public CSVSortException(String message) {
		super(message);
	}

	public CSVSortException(Throwable cause) {
		super(cause);
	}

	public CSVSortException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSVSortException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
