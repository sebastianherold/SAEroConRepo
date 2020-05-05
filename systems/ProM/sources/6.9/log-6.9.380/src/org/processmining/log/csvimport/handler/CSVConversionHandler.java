package org.processmining.log.csvimport.handler;

import java.util.Date;

import org.processmining.log.csv.CSVFile;
import org.processmining.log.csvimport.exception.CSVConversionException;

/**
 * Handler for the conversion following a visitor-like pattern.
 * 
 * @author F. Mannhardt
 *
 * @param <R>
 */
public interface CSVConversionHandler<R> {

	/**
	 * Called upon start parsing the {@link CSVFile}.
	 * 
	 * @param inputFile
	 */
	void startLog(CSVFile inputFile);

	/**
	 * Called when a new trace is encountered. Traces are assumed to be sorted
	 * by caseId, therefore, this is only called once per trace.
	 * 
	 * @param caseId
	 */
	void startTrace(String caseId);

	/**
	 * Called after a traces has been fully parsed.
	 * 
	 * @param caseId
	 */
	void endTrace(String caseId);

	/**
	 * Called when parsing a row, thereby creating an event.
	 * 
	 * @param eventClass
	 *            the name (class) of the event
	 * @param completionTime
	 *            the completion time possibly NULL
	 * @param startTime
	 *            the start time possibly NULL
	 */
	void startEvent(String eventClass, Date completionTime, Date startTime);

	/**
	 * Called when a string attribute is parsed
	 * 
	 * @param name
	 * @param value
	 */
	void startAttribute(String name, String value);

	/**
	 * Called when a long attribute is parsed
	 * 
	 * @param name
	 * @param value
	 */
	void startAttribute(String name, long value);

	/**
	 * Called when a double attribute is parsed
	 * 
	 * @param name
	 * @param value
	 */
	void startAttribute(String name, double value);

	/**
	 * Called when a date attribute is parsed
	 * 
	 * @param name
	 * @param value
	 */
	void startAttribute(String name, Date value);

	/**
	 * Called when a boolean attribute is parsed
	 * 
	 * @param name
	 * @param value
	 */
	void startAttribute(String name, boolean value);

	/**
	 * Called after the attribute has been parsed
	 */
	void endAttribute();

	/**
	 * Called when the full row (including all attributes) have been parsed.
	 */
	void endEvent();

	/**
	 * Detected an error in the conversion.
	 * 
	 * @param lineNumber
	 * @param columnIndex
	 * @param attributeName
	 * @param cellContent
	 * @param e
	 * @throws CSVConversionException
	 */
	void errorDetected(int lineNumber, int columnIndex, String attributeName, Object cellContent, Exception e) throws CSVConversionException;

	/**
	 * @return the result of the parse
	 */
	R getResult();

	/**
	 * @return whether any errors have been encountered during the conversion
	 */
	boolean hasConversionErrors();

	/**
	 * @return a descriptive String of the error
	 */
	String getConversionErrors();

}
