package org.processmining.log.csv;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.io.Files;

public abstract class AbstractCSVFile implements CSVFile {

	private final Path file;

	public AbstractCSVFile(Path file) {
		this.file = file;	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.log.csvimport.ICSVFile#getFile()
	 */
	@Override
	public Path getFile() {
		return file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.log.csvimport.ICSVFile#getFilename()
	 */
	@Override
	public String getFilename() {
		return file.getFileName().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.log.csv.CSVFile#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		String ext = Files.getFileExtension(getFile().toFile().getName());
		if (ext.equalsIgnoreCase("csv") || ext.equalsIgnoreCase("txt")) {
			return new FileInputStream(getFile().toFile());
		} else if (ext.equalsIgnoreCase("zip")) {
			ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(getFile().toFile()));
			ZipEntry nextEntry = zipInputStream.getNextEntry();
			if (nextEntry == null) {
				throw new IOException("ZIP files does not contain any files");
			}
			return zipInputStream;
		} else if (ext.equalsIgnoreCase("gz")) {
			return new GZIPInputStream(new FileInputStream(getFile().toFile()));
		}
		throw new UnsupportedOperationException("Unsupported file type " + ext);
	}

}
