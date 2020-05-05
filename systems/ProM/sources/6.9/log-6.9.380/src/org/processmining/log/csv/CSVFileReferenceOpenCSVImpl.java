package org.processmining.log.csv;

import java.io.IOException;
import java.nio.file.Path;

import org.processmining.log.csv.config.CSVConfig;

/**
 * {@link CSVFile} implementation that holds a reference to a CSV file on disk.
 * The {@link CSVFileReferenceUnivocityImpl} is recommended!
 *
 * @author F. Mannhardt
 *
 */
public final class CSVFileReferenceOpenCSVImpl extends AbstractCSVFile {

	private final CSVOpenCSVImpl csv;

	public CSVFileReferenceOpenCSVImpl(Path file) {
		super(file);
		csv = new CSVOpenCSVImpl();
	}

	@Deprecated
	public CSVFileReferenceOpenCSVImpl(Path file, String filename, long fileSizeInBytes) {
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
		return createReader(importConfig).readNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.log.csv.CSVFile#createReader(org.processmining.log.
	 * csv.CSVConfig)
	 */
	@Override
	public ICSVReader createReader(CSVConfig config) throws IOException {
		return csv.createReader(getInputStream(), config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.log.csv.CSVFile#getCSV()
	 */
	@Override
	public ICSV getCSV() {
		return csv;
	}

}
