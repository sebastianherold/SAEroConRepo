package org.processmining.log.csvimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.ICSVReader;
import org.processmining.log.csv.ICSVWriter;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csvimport.CSVConversion.ProgressListener;
import org.processmining.log.csvimport.exception.CSVSortException;

import com.fasterxml.sort.DataReader;
import com.fasterxml.sort.DataReaderFactory;
import com.fasterxml.sort.DataWriter;
import com.fasterxml.sort.DataWriterFactory;
import com.fasterxml.sort.IteratingSorter;
import com.fasterxml.sort.SortConfig;
import com.fasterxml.sort.SortingState.Phase;
import com.fasterxml.sort.TempFileProvider;
import com.ning.compress.lzf.LZFInputStream;
import com.ning.compress.lzf.parallel.PLZFOutputStream;

/**
 * Sorts an {@link CSVFile}
 * 
 * @author F. Mannhardt
 * 
 */
final class CSVSorter {

	private static final class UncompressedCSVReaderWithoutHeader extends DataReader<String[]> {

		private static final int MAX_COLUMNS_FOR_ERROR_REPORTING = 32;
		private static final int MAX_FIELD_LENGTH_FOR_ERROR_REPORTING = 64;

		private final ICSVReader reader;
		private final int numColumns;
		private int currentRow = 1;

		private UncompressedCSVReaderWithoutHeader(CSVFile csvFile, CSVConfig importConfig, int numColumns)
				throws IOException {
			this.numColumns = numColumns;
			this.reader = csvFile.createReader(importConfig);
			// Skip header line
			this.reader.readNext();
		}

		public void close() throws IOException {
			reader.close();
		}

		public int estimateSizeInBytes(String[] val) {
			return estimateSize(val);
		}

		public String[] readNext() throws IOException {
			String[] val = reader.readNext();
			if (val != null && val.length != numColumns) {
				String offendingLine = safeToString(val);
				throw new IOException(
						MessageFormat
								.format("The number of fields in rows of the CSV file is inconsistent. There should be {0} fields in each row according to the header, but there was a row with {1} fields in the CSV file! Row {2} is invalid: {3}",
										numColumns, val.length, currentRow, offendingLine));
			}
			currentRow++;
			return val;
		}

		private String safeToString(String[] valueArray) {
			if (valueArray == null) {
				return "NULL";
			} else if (valueArray.length == 0) {
				return "[]";
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append('[');
				for (int i = 0; i < valueArray.length; i++) {
					String value = valueArray[i];
					if (value != null) {
						if (value.length() < MAX_FIELD_LENGTH_FOR_ERROR_REPORTING) {
							sb.append(value);
						} else {
							sb.append(value.substring(0, MAX_FIELD_LENGTH_FOR_ERROR_REPORTING - 1));
						}
						if (i > MAX_COLUMNS_FOR_ERROR_REPORTING) {
							return sb.append(String.format("[... omitted %s further columns]", valueArray.length - i))
									.toString();
						}
						if (i < valueArray.length - 1) {
							sb.append(", ");
						}
					}
				}
				return sb.append(']').toString();
			}
		}
	}

	private static final class CompressedCSVDataWriterFactory extends DataWriterFactory<String[]> {

		private final CSVConfig importConfig;
		private final CSVFile csvFile;

		private CompressedCSVDataWriterFactory(CSVFile csvFile, CSVConfig importConfig) {
			this.csvFile = csvFile;
			this.importConfig = importConfig;
		}

		public DataWriter<String[]> constructWriter(OutputStream os) throws IOException {
			final ICSVWriter writer = csvFile.getCSV().createWriter(new PLZFOutputStream(os), importConfig);
			// Write Header
			return new DataWriter<String[]>() {

				public void close() throws IOException {
					writer.close(); // catch IllegalStateException
				}

				public void writeEntry(String[] val) throws IOException {
					writer.writeNext(val);
				}
			};
		}
	}

	private static final class CompressedCSVDataReaderFactory extends DataReaderFactory<String[]> {

		private final CSVConfig importConfig;
		private final CSVFile csvFile;

		private CompressedCSVDataReaderFactory(CSVFile csvFile, CSVConfig importConfig) {
			this.csvFile = csvFile;
			this.importConfig = importConfig;
		}

		public DataReader<String[]> constructReader(InputStream is) throws IOException {
			final ICSVReader reader = csvFile.getCSV().createReader(new LZFInputStream(is), importConfig);
			return new DataReader<String[]>() {

				public void close() throws IOException {
					reader.close();
				}

				public int estimateSizeInBytes(String[] item) {
					return estimateSize(item);
				}

				public String[] readNext() throws IOException {
					return reader.readNext();
				}
			};
		}
	}

	private CSVSorter() {
	}

	/**
	 * Sorts an {@link CSVFile} using only a configurable, limited amount of
	 * memory.
	 * 
	 * @param csvFile
	 * @param rowComparator
	 * @param importConfig
	 * @param maxMemory
	 * @param numOfColumnsInCSV
	 * @param progress
	 * @return a {@link File} containing the sorted CSV
	 * @throws CSVSortException
	 */
	public static File sortCSV(final CSVFile csvFile, final Comparator<String[]> rowComparator,
			final CSVConfig importConfig, final int maxMemory, final int numOfColumnsInCSV,
			final ProgressListener progress) throws CSVSortException {

		// Create Sorter
		final CompressedCSVDataReaderFactory dataReaderFactory = new CompressedCSVDataReaderFactory(csvFile,
				importConfig);
		final CompressedCSVDataWriterFactory dataWriterFactory = new CompressedCSVDataWriterFactory(csvFile,
				importConfig);
		final IteratingSorter<String[]> sorter = new IteratingSorter<>(new SortConfig().withMaxMemoryUsage(
				maxMemory * 1024l * 1024l).withTempFileProvider(new TempFileProvider() {

			public File provide() throws IOException {
				return Files.createTempFile(csvFile.getFilename() + "-merge-sort", ".lzf").toFile();
			}
		}), dataReaderFactory, dataWriterFactory, rowComparator);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<File> future = executorService.submit(new Callable<File>() {

			public File call() throws Exception {

				// Read uncompressed CSV
				DataReader<String[]> inputDataReader = new UncompressedCSVReaderWithoutHeader(csvFile, importConfig,
						numOfColumnsInCSV);
				try {
					Iterator<String[]> result = sorter.sort(inputDataReader);

					// Write sorted result to compressed file
					if (result != null) {
						File sortedCsvFile = Files.createTempFile(csvFile.getFilename() + "-sorted", ".lzf").toFile();
						DataWriter<String[]> dataWriter = dataWriterFactory.constructWriter(new FileOutputStream(
								sortedCsvFile));
						try {
							while (result.hasNext()) {
								dataWriter.writeEntry(result.next());
							}
						} finally {
							try {
								dataWriter.close();
							} catch (IllegalStateException e) {
								// already closed - ignore here to propagate the real exception
							}
						}
						return sortedCsvFile;
					} else {
						throw new CSVSortException("Could not sort file! Unkown error while sorting.");
					}

				} finally {
					sorter.close();
				}

			}
		});

		try {
			executorService.shutdown();
			int sortRound = -1;
			int preSortFiles = -1;
			while (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
				if (progress.getProgress().isCancelled()) {
					progress.log("Cancelling sorting, this might take a while ...");
					sorter.cancel(new RuntimeException("Cancelled"));
					throw new CSVSortException("User cancelled sorting");
				}
				if (sorter.getPhase() == Phase.PRE_SORTING) {
					if (sorter.getSortRound() != sortRound) {
						sortRound = sorter.getSortRound();
						progress.log(MessageFormat.format("Pre-sorting finished segment {0} in memory ...",
								sortRound + 1));
					}
					if (sorter.getNumberOfPreSortFiles() != preSortFiles) {
						preSortFiles = sorter.getNumberOfPreSortFiles();
						progress.log(MessageFormat.format("Pre-sorting finished segment {0} ...", preSortFiles + 1));
					}
				} else if (sorter.getPhase() == Phase.SORTING) {
					if (sorter.getSortRound() != sortRound) {
						sortRound = sorter.getSortRound();
						progress.log(MessageFormat.format("Sorting finished round {0}/{1} ...", sortRound + 1,
								sorter.getNumberOfSortRounds() + 1));
					}
				}
			}
			return future.get();
		} catch (InterruptedException e) {
			progress.log("Cancelling sorting, this might take a while ...");
			sorter.cancel();
			throw new CSVSortException("Cancelled sorting", e);
		} catch (ExecutionException e) {
			throw new CSVSortException("Could not sort file.", e);
		}
	}

	private static int estimateSize(String[] item) {
		int size = 8 * ((item.length * 4 + 12) / 8);
		for (String s : item) {
			if (s != null) {
				size += 8 * ((((s.length()) * 4) + 45) / 8);
			}
		}
		return size;
	}

}
