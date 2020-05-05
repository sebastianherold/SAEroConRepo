package org.processmining.log.csvimport;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csvimport.CSVConversion.ConversionResult;
import org.processmining.log.csvimport.CSVConversion.ProgressListener;
import org.processmining.log.csvimport.config.CSVConversionConfig;
import org.processmining.log.csvimport.exception.CSVConversionConfigException;
import org.processmining.log.csvimport.exception.CSVConversionException;
import org.processmining.log.csvimport.handler.XESConversionHandlerImpl;
import org.processmining.log.csvimport.ui.ConversionConfigUI;
import org.processmining.log.csvimport.ui.ExpertConfigUI;
import org.processmining.log.csvimport.ui.ImportConfigUI;

import com.google.common.base.Throwables;

/**
 * CSV to XES XLog conversion plug-in
 * 
 * @author F. Mannhardt
 * 
 */
public final class CSVConversionPlugin {

	@Plugin(name = "Convert CSV to XES", level = PluginLevel.PeerReviewed, parameterLabels = { "CSV" }, returnLabels = {
			"XES Event Log" }, // 
			returnTypes = { XLog.class }, userAccessible = true, mostSignificantResult = 1, // 
			keywords = { "CSV", "OpenXES", "Conversion",
					"Import" }, help = "Converts the CSV file to a OpenXES XLog object.")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = " F. Mannhardt, N. Tax, D.M.M. Schunselaar", // 
			email = "f.mannhardt@tue.nl, n.tax@tue.nl, d.m.m.schunselaar@vu.nl", pack = "Log")
	public XLog convertCSVToXES(final UIPluginContext context, CSVFile csvFile) {

		InteractionResult result = InteractionResult.CONTINUE;

		try {
			CSVConfig importConfig = new CSVConfig(csvFile);
			CSVConversionConfig csvConversionConfig = null;

			int i = 0;
			wizardLoop: while (result != InteractionResult.FINISHED) {
				switch (i) {
					case 0 :
						result = queryImportConfig(context, csvFile, importConfig);
						try {
							csvConversionConfig = new CSVConversionConfig(csvFile, importConfig);
							csvConversionConfig.autoDetect();
						} catch (CSVConversionException e) {								
							// Due to the strange wizard framework, we cannot cancel this dialog. So show again. The only way to cancel is through the user.
							if (result != InteractionResult.CANCEL) {
								ProMUIHelper.showErrorMessage(context, e.getMessage(), "CSV Conversion Failed");
								continue wizardLoop;	
							}
						}
						break;
					case 1 :
						result = queryConversionConfig(context, csvFile, importConfig, csvConversionConfig);
						if (result == InteractionResult.NEXT || result == InteractionResult.CONTINUE) {
							boolean reconfigure = queryMissingConfiguration(context, csvConversionConfig);
							if (reconfigure) {
								// Show same dialog again
								continue wizardLoop;
							}
						}
						break;
					case 2 :
						result = queryExpertConfig(context, csvFile, importConfig, csvConversionConfig);
						break;
				}
				if (result == InteractionResult.NEXT || result == InteractionResult.CONTINUE) {
					i++;
				} else if (result == InteractionResult.PREV) {
					i--;
				} else if (result == InteractionResult.CANCEL) {
					return cancel(context);
				}
			}

			CSVConversion csvConversion = new CSVConversion();
			ConversionResult<XLog> conversionResult = doConvertCSVToXes(context, csvFile, importConfig,
					csvConversionConfig, csvConversion);
			if (conversionResult.hasConversionErrors()) {
				ProMUIHelper.showWarningMessage(context, conversionResult.getConversionErrors(),
						"Warning: Some issues have been detected during conversion");
			}
			return conversionResult.getResult();
		} catch (CSVConversionException e) {
			Throwable rootCause = Throwables.getRootCause(e);
			String errorMessage;
			if (rootCause != null) {
				errorMessage = rootCause.getMessage();
			} else {
				errorMessage = e.toString();
			}
			String stackTrace = Throwables.getStackTraceAsString(e);
			ProMUIHelper.showErrorMessage(context, errorMessage + "\n\nDebug information:\n" + stackTrace,
					"CSV Conversion Failed");
			return cancel(context);
		}

	}

	private boolean queryMissingConfiguration(final UIPluginContext context, CSVConversionConfig csvConversionConfig) {
		boolean noCase = csvConversionConfig.getCaseColumns().isEmpty();
		boolean noEvents = csvConversionConfig.getEventNameColumns().isEmpty();
		Object[] options = { "Continue", "Reconfigure" };
		String message;
		String title;
		if (noCase) {
			message = "<HTML>You did not select a column containing the case identifier. This will result in an event log with a single trace.<BR/> "
					+ "Do you want to continue without case identifier or reconfigure the conversion?</HTML>";
			title = "Missing event column";
		} else if (noEvents) {
			message = "<HTML>You did not select a column containing the event name. This will result in an event log with unamed events.<BR/> "
					+ "Do you want to continue without event name or reconfigure the conversion?</HTML>";
			title = "Missing event column";
		} else if (noEvents && noCase) {
			message = "You did not select columns containing the case identifier and event name. This will result in an event log with a single trace and unnamed events. "
					+ "Do you want to continue or reconfigure the conversion?";
			title = "Missing case and event columns";
		} else {
			return false;
		}
		int warningResult = JOptionPane.showOptionDialog(context.getGlobalContext().getUI(), message, title,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		return warningResult == 1; // reconfigure
	}

	private XLog cancel(final UIPluginContext context) {
		context.getFutureResult(0).cancel(false);
		return null;
	}

	public ConversionResult<XLog> doConvertCSVToXes(final PluginContext context, CSVFile csvFile,
			CSVConfig importConfig, CSVConversionConfig conversionConfig, CSVConversion csvConversion)
			throws CSVConversionConfigException, CSVConversionException {

		ProgressListener progressListener = new ProgressListener() {

			public Progress getProgress() {
				return context.getProgress();
			}

			public void log(String message) {
				context.log(message);

			}
		};

		XESConversionHandlerImpl xesHandler = new XESConversionHandlerImpl(importConfig, conversionConfig);
		final ConversionResult<XLog> conversionResult = csvConversion.convertCSV(progressListener, importConfig,
				conversionConfig, csvFile, xesHandler);
		final XLog convertedLog = conversionResult.getResult();

		if (xesHandler.hasConversionErrors()) {
			context.log(xesHandler.getConversionErrors(), MessageLevel.WARNING);
		}

		return new ConversionResult<XLog>() {

			public boolean hasConversionErrors() {
				return conversionResult.hasConversionErrors();
			}

			public XLog getResult() {
				return convertedLog;
			}

			public String getConversionErrors() {
				return conversionResult.getConversionErrors();
			}
		};

	}

	public static InteractionResult queryExpertConfig(UIPluginContext context, CSVFile csv, CSVConfig importConfig,
			CSVConversionConfig converionConfig) {
		ExpertConfigUI expertConfigUI = new ExpertConfigUI(csv, importConfig, converionConfig);
		return context.showWizard("Configure Additional Conversion Settings", false, true, expertConfigUI);
	}

	public static InteractionResult queryImportConfig(UIPluginContext context, CSVFile csv, CSVConfig importConfig) {
		ImportConfigUI importConfigUI = new ImportConfigUI(csv, importConfig);
		return context.showWizard("Configure CSV Parser Settings", true, false, importConfigUI);
	}

	public static InteractionResult queryConversionConfig(UIPluginContext context, CSVFile csv, CSVConfig importConfig,
			CSVConversionConfig conversionConfig) throws CSVConversionException {
		try (ConversionConfigUI conversionConfigUI = new ConversionConfigUI(csv, importConfig, conversionConfig)) {
			return context.showWizard("Configure Conversion from CSV to XES", false, false, conversionConfigUI);
		} catch (IOException e) {
			throw new CSVConversionConfigException("Could not query conversion config.", e);
		}
	}

}
