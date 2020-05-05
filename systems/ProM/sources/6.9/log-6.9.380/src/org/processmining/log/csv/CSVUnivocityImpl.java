package org.processmining.log.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.input.BOMInputStream;
import org.processmining.log.csv.config.CSVConfig;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public class CSVUnivocityImpl implements ICSV {
	
	private static final int MAX_CHARS_PER_COLUMN = 65536;
	
	private static final int BUFFER_SIZE = 8192 * 4;

	private static CsvParser createCSVReader(InputStream is, CSVConfig importConfig) throws UnsupportedEncodingException{
		CsvParserSettings settings = new CsvParserSettings();
		settings.setMaxCharsPerColumn(MAX_CHARS_PER_COLUMN);
		settings.setLineSeparatorDetectionEnabled(true);
		settings.getFormat().setDelimiter(importConfig.getSeparator().getSeperatorChar());
		settings.getFormat().setQuote(importConfig.getQuoteChar().getQuoteChar());
		settings.getFormat().setCharToEscapeQuoteEscaping(importConfig.getEscapeChar().getEscapeChar());
		CsvParser parser = new CsvParser(settings);
		BOMInputStream bomExcludingStream = new BOMInputStream(is); // exclude BOM byte for UTF-BOM encoded files as those mess up with pretty much everything
		parser.beginParsing(new BufferedReader(new InputStreamReader(bomExcludingStream, importConfig.getCharset()), BUFFER_SIZE));
		return parser;
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
		final CsvParser csvReader = createCSVReader(is, importConfig);
		return new ICSVReader() {
			
			public String[] readNext() throws IOException {
				return csvReader.parseNext();
			}

			public void close() throws IOException {
				csvReader.stopParsing();
			}
			
		};
	}
	
	private static CsvWriter createCSVWriter(OutputStream os, CSVConfig importConfig) throws UnsupportedEncodingException {
		CsvWriterSettings settings = new CsvWriterSettings();
		settings.getFormat().setDelimiter(importConfig.getSeparator().getSeperatorChar());
		settings.getFormat().setQuote(importConfig.getQuoteChar().getQuoteChar());
		settings.getFormat().setCharToEscapeQuoteEscaping(importConfig.getEscapeChar().getEscapeChar());
		CsvWriter writer = new CsvWriter(new OutputStreamWriter(os, importConfig.getCharset()), settings);
		return writer;
	}

	/* (non-Javadoc)
	 * @see org.processmining.log.csv.CSVFile#createWriter(java.io.OutputStream, org.processmining.log.csvimport.config.CSVImportConfig)
	 */
	@Override
	public ICSVWriter createWriter(OutputStream os, CSVConfig config) throws IOException {
		final CsvWriter writer = createCSVWriter(os, config);
		return new ICSVWriter() {
			
			public void writeNext(String[] value) {
				writer.writeRow((Object[])value);
			}
			
			public void close() throws IOException {				
				writer.close();				
			}
		};
	}

}
