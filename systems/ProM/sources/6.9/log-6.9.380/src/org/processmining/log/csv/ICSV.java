package org.processmining.log.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.log.csv.config.CSVConfig;

/**
 * Wrapper around some CSV parsing library.
 * 
 * @author F. Mannhardt
 *
 */
public interface ICSV {

	/**
	 * Returns a new {@link ICSVWriter} that can be used to write data to a new
	 * {@link OutputStream} in CSV format. The caller is responsible for calling
	 * {@link ICSVWriter#close()} on the writer.
	 * 
	 * @param os
	 * @param config
	 * @return
	 * @throws IOException
	 */
	ICSVWriter createWriter(OutputStream os, CSVConfig config) throws IOException;

	/**
	 * Returns a new {@link ICSVReader} that can be used to read data from the
	 * {@link InputStream} in CSV format. The caller is responsible for calling
	 * {@link ICSVReader#close()} on the writer.
	 * 
	 * @param os
	 * @param config
	 * @return
	 * @throws IOException
	 */
	ICSVReader createReader(InputStream is, CSVConfig config) throws IOException;

}
