package org.processmining.log.csv.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csvimport.exception.CSVConversionException;

/**
 * Configuration for the import of the CSV
 * 
 * @author F. Mannhardt
 *
 */
public final class CSVConfig {
	
	private static final int SEPARATOR_DETECTION_ROW_LIMIT = 10;
	
	private String charset = Charset.defaultCharset().name();
	private CSVSeperator separator = CSVSeperator.COMMA;
	private CSVQuoteCharacter quoteChar = CSVQuoteCharacter.DOUBLE_QUOTE;
	private CSVEscapeCharacter escapeChar = CSVEscapeCharacter.QUOTE;
	
	public CSVConfig() {
	}
	
	public CSVConfig(final CSVFile csvFile) throws CSVConversionException {
		try {
			charset = autoDetectCharset(csvFile);
			separator = autoDetectSeparator(csvFile, charset);
			quoteChar = autoDetectQuote(csvFile, charset);
		} catch (IOException e) {
			throw new CSVConversionException("Could not auto-detect CSV import parameters.", e);
		}
	}
	
	private static String autoDetectCharset(final CSVFile csvFile) throws FileNotFoundException, IOException {

		final UniversalDetector detector = new UniversalDetector(null);
		
		try (FileInputStream fis = new FileInputStream(csvFile.getFile().toFile())) {
			byte[] buf = new byte[4096];
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			detector.dataEnd();
		}
		
		if (detector.getDetectedCharset() != null) {
			return detector.getDetectedCharset();
		} else {
			// Nothing detected, assume OS default
			return Charset.defaultCharset().name();
		}
	}

	private static CSVSeperator autoDetectSeparator(final CSVFile csvFile, String charset) throws FileNotFoundException, IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile.getFile().toFile()), Charset.forName(charset)))) {
			Map<CSVSeperator, Integer> counter = new HashMap<>();
			for (int i = 0; i < SEPARATOR_DETECTION_ROW_LIMIT; i++) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				updateCounter(counter, CSVSeperator.COMMA, ",", line);
				updateCounter(counter, CSVSeperator.TAB, "\t", line);
				updateCounter(counter, CSVSeperator.SEMICOLON, ";", line);
			}
			// now check which are still fine
			for (CSVSeperator seperator : counter.keySet()) {
				if (counter.get(seperator) > 1) {
					return seperator;
				}
			}
			// if none of them was properly detected go with inconsistent ones
			for (CSVSeperator seperator : counter.keySet()) {
				if (counter.get(seperator) == -1) {
					return seperator;
				}
			}
		}
		
		// Fall back to default
		return CSVSeperator.COMMA;
	}

	private static void updateCounter(Map<CSVSeperator, Integer> counter, CSVSeperator separator, String token,
			String line) {
		// Remove all text in between quotes as it should be ignored for separator detection 
		String lineWithoutQuotes = removeTextInQuotes(line);
		int matchCount = StringUtils.countMatches(lineWithoutQuotes, token);
		if (counter.get(separator) == null) {
			counter.put(separator, matchCount);
		} else if (counter.get(separator) != matchCount) {
			// Inconsistent number of separator characters
			counter.put(separator, -1);
		}
	}

	private static String removeTextInQuotes(String line) {
		String internalLine = line;
		while (internalLine.contains("\"")) {
			int startIndex = internalLine.indexOf("\"");
			int endIndex = internalLine.substring(startIndex + 1, internalLine.length()).indexOf("\"");
			// now remove the in between part of the string and replace it with some placeholder text
			internalLine = internalLine.substring(0, startIndex) + "placeholder"
					+ internalLine.substring(startIndex + 1 + endIndex + 1, internalLine.length());
		}
		return internalLine;
	}

	private static CSVQuoteCharacter autoDetectQuote(CSVFile csvFile, String charset) {
		//TODO implement
		return CSVQuoteCharacter.DOUBLE_QUOTE;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public CSVSeperator getSeparator() {
		return separator;
	}

	public void setSeparator(CSVSeperator separator) {
		this.separator = separator;
	}

	public CSVQuoteCharacter getQuoteChar() {
		return quoteChar;
	}

	public void setQuoteChar(CSVQuoteCharacter quoteChar) {
		this.quoteChar = quoteChar;
	}

	public CSVEscapeCharacter getEscapeChar() {
		return escapeChar;
	}

	public void setEscapeChar(CSVEscapeCharacter escapeChar) {
		this.escapeChar = escapeChar;
	}
	
}