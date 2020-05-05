package org.processmining.log.csv;

import java.io.IOException;

/**
 * Reader of a CSV file
 * 
 * @author F. Mannhardt
 *
 */
public interface ICSVReader extends AutoCloseable {

	/**
	 * @return the next line or NULL in case of EOF
	 * @throws IOException
	 */
	String[] readNext() throws IOException;

	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	void close() throws IOException;

}
