package org.processmining.log.csvimport;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deckfour.xes.model.XLog;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.CSVFileReferenceUnivocityImpl;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csvimport.CSVConversion.ConversionResult;
import org.processmining.log.csvimport.CSVConversion.ProgressListener;
import org.processmining.log.csvimport.config.CSVConversionConfig;
import org.processmining.log.csvimport.exception.CSVConversionConfigException;
import org.processmining.log.csvimport.exception.CSVConversionException;
import org.processmining.log.utils.XUtils;

import com.google.common.collect.ImmutableList;

/**
 * CLI interface to the ProM XES conversion
 * 
 * @author F. Mannhardt
 *
 */
public final class CSVConversionCLI {

	private static final class ProgressListenerPrintStreamImpl extends CSVConversion.NoOpProgressListenerImpl {

		private final PrintStream out;

		public ProgressListenerPrintStreamImpl(PrintStream out) {
			this.out = out;
		}

		public void log(String message) {
			out.println(message);
		}

	}

	private static final Options OPTIONS = new Options();

	private static final Option HELP = OptionBuilder.withDescription("help").create('h');
	private static final Option XES = OptionBuilder.hasArg().withArgName("filename").create("xes");
	private static final Option TRACE = OptionBuilder.hasArg().withArgName("traceColumn").create("trace");
	private static final Option EVENT = OptionBuilder.hasArg().withArgName("eventColumn").create("event");
	private static final Option START = OptionBuilder.hasArg().withArgName("startColumn").create("start");
	private static final Option COMPLETE = OptionBuilder.hasArg().withArgName("completionColumn").create("complete");

	static {
		OPTIONS.addOption(HELP);
		OPTIONS.addOption(XES);
		OPTIONS.addOption(TRACE);
		OPTIONS.addOption(EVENT);
		OPTIONS.addOption(START);
		OPTIONS.addOption(COMPLETE);
	}

	public static void main(String[] args) {

		try {
			CommandLineParser parser = new PosixParser();
			CommandLine commandLine = parser.parse(OPTIONS, args);

			if (commandLine.hasOption(HELP.getOpt())) {
				printUsage();
				return;
			}

			if (commandLine.getArgs().length != 1) {
				printUsage();
				System.err.println("Missing filename of the CSV file!");
				return;
			}

			File logFile = new File(commandLine.getArgs()[0]);

			try {
				XLog log = parseCSV(logFile, commandLine);

				if (commandLine.hasOption(XES.getOpt())) {
					XUtils.saveLogGzip(log, new File(commandLine.getOptionValue(XES.getOpt())));
				} else {
					XUtils.saveLogGzip(log, new File(logFile.getAbsolutePath() + ".xes.gz"));
				}
			} catch (CSVConversionException | IOException e) {
				if (e.getMessage() != null) {
					System.err.println(e.getMessage());
				}
				e.printStackTrace();
			}

			System.out.println("Log converted successfully!");

		} catch (ParseException e) {
			printUsage();
			if (e.getMessage() != null) {
				System.err.println(e.getMessage());
			}
		}
		
		System.exit(0);

	}

	private static XLog parseCSV(File inputFile, CommandLine commandLine) throws CSVConversionException, CSVConversionConfigException {
		CSVConversion conversion = new CSVConversion();
		CSVFile csvFile = new CSVFileReferenceUnivocityImpl(inputFile.toPath());
		CSVConfig importConfig = new CSVConfig(csvFile);
		CSVConversionConfig conversionConfig = new CSVConversionConfig(csvFile, importConfig);
		conversionConfig.autoDetect();

		if (commandLine.hasOption(TRACE.getOpt())) {
			conversionConfig.setCaseColumns(ImmutableList.of(commandLine.getOptionValue(TRACE.getOpt())));
		}
		
		if (commandLine.hasOption(EVENT.getOpt())) {
			conversionConfig.setEventNameColumns(ImmutableList.of(commandLine.getOptionValue(EVENT.getOpt())));
		}
		
		if (commandLine.hasOption(START.getOpt())) {
			conversionConfig.setStartTimeColumn(commandLine.getOptionValue(START.getOpt()));
		}
		
		if (commandLine.hasOption(COMPLETE.getOpt())) {
			conversionConfig.setCompletionTimeColumn(commandLine.getOptionValue(COMPLETE.getOpt()));
		}
		
		ProgressListener cmdLineProgressListener = new ProgressListenerPrintStreamImpl(System.out);
		ConversionResult<XLog> result = conversion.doConvertCSVToXES(cmdLineProgressListener, csvFile, importConfig,
				conversionConfig);
		return result.getResult();
	}

	private static void printUsage() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("mpe [CSVFILE]", OPTIONS, true);
		return;
	}

}