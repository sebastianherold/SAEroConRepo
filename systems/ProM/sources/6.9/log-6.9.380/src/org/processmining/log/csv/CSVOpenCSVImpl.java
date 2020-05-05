package org.processmining.log.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.processmining.log.csv.config.CSVConfig;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CSVOpenCSVImpl implements ICSV {
	
	private static final int BUFFER_SIZE = 8192 * 4;

	private static CSVReader createCSVReader(InputStream is, CSVConfig importConfig)
			throws UnsupportedEncodingException {
		if (importConfig.getQuoteChar() == null) {
			return new CSVReader(new BufferedReader(new InputStreamReader(is, importConfig.getCharset()), BUFFER_SIZE),
					importConfig.getSeparator().getSeperatorChar(), CSVParser.DEFAULT_QUOTE_CHARACTER,
					CSVParser.DEFAULT_ESCAPE_CHARACTER, 0, false, false, true);
		} else {
			return new CSVReader(new BufferedReader(new InputStreamReader(is, importConfig.getCharset()), BUFFER_SIZE),
					importConfig.getSeparator().getSeperatorChar(), importConfig.getQuoteChar().getQuoteChar());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.log.csv.CSVFile#createReader(org.processmining.log.
	 * csvimport.CSVImportConfig)
	 */
	@Override
	public ICSVReader createReader(InputStream is, CSVConfig importConfig) throws IOException {
		final CSVReader csvReader = createCSVReader(is, importConfig);
		return new ICSVReader() {

			/* (non-Javadoc)
			 * @see org.processmining.log.csv.AbstractCSVReader#readNext()
			 */
			public String[] readNext() throws IOException {
				return csvReader.readNext();
			}

			/* (non-Javadoc)
			 * @see org.processmining.log.csv.AbstractCSVReader#close()
			 */
			public void close() throws IOException {
				csvReader.close();
			}
			
		};
	}
	
	private static CSVWriter createCSVWriter(OutputStream os, CSVConfig importConfig)
			throws UnsupportedEncodingException {
		return new CSVWriter(new BufferedWriter(new OutputStreamWriter(os, importConfig.getCharset()), BUFFER_SIZE),
				importConfig.getSeparator().getSeperatorChar(), importConfig.getQuoteChar().getQuoteChar());
	}

	public ICSVWriter createWriter(OutputStream os, CSVConfig importConfig) throws IOException {
		final CSVWriter csvWriter = createCSVWriter(os, importConfig);
		return new ICSVWriter() {

			public void writeNext(String[] value) {
				csvWriter.writeNext(value, false);
			}

			public void close() throws IOException {
				csvWriter.close();
			}

			
		};
	}

}
