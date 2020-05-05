package org.processmining.log.csv;

import java.io.IOException;
import java.nio.file.Path;

import org.processmining.log.csv.config.CSVConfig;

import com.univocity.parsers.common.TextParsingException;

/**
 * {@link CSVFile} implementation that holds a reference to a CSV file on disk.
 *
 * @author N. Tax
 *
 */
public final class CSVFileReferenceUnivocityImpl extends AbstractCSVFile {
	
	private final CSVUnivocityImpl csv;

	public CSVFileReferenceUnivocityImpl(Path file) {
		super(file);
		csv = new CSVUnivocityImpl();
	}
	
	@Deprecated
	public CSVFileReferenceUnivocityImpl(Path file, String filename, long fileSizeInBytes) {
		this(file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.log.csv.CSVFile#readHeader(org.processmining.log.csvimport
	 * .CSVImportConfig)
	 */
	@Override
	public String[] readHeader(CSVConfig importConfig) throws IOException {
		try {
			return createReader(importConfig).readNext();
		} catch (TextParsingException | IllegalStateException e) {
			// Wrap unchecked Univocity exceptions into a IOException for consistency
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.processmining.log.csv.CSVFile#createReader(org.processmining.log.csv.CSVConfig)
	 */
	@Override
	public ICSVReader createReader(CSVConfig config) throws IOException {
		return csv.createReader(getInputStream(), config);
	}

	/* (non-Javadoc)
	 * @see org.processmining.log.csv.CSVFile#getCSV()
	 */
	@Override
	public ICSV getCSV() {
		return csv;
	}
	
}
