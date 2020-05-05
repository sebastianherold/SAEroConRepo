package org.processmining.log.csvimport.exception;


/**
 * @author F. Mannhardt
 *
 */
public class CSVConversionConfigException extends CSVConversionException {

	private static final long serialVersionUID = 4329858720296484283L;

	public CSVConversionConfigException() {
	}

	public CSVConversionConfigException(String message) {
		super(message);
	}

	public CSVConversionConfigException(Throwable cause) {
		super(cause);
	}

	public CSVConversionConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSVConversionConfigException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
