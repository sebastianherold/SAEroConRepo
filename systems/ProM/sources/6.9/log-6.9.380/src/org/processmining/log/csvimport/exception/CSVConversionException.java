package org.processmining.log.csvimport.exception;


/**
 * @author F. Mannhardt
 *
 */
public class CSVConversionException extends Exception {

	private static final long serialVersionUID = -4532347650248107292L;

	public CSVConversionException() {
		super();
	}

	public CSVConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CSVConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSVConversionException(String message) {
		super(message);
	}

	public CSVConversionException(Throwable cause) {
		super(cause);
	}
	

}
