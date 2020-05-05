package org.processmining.plugins.loginsertstart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

/**
 * Add Start Events
 * 
 * The main class
 * 
 * @author jnakatumba
 * 
 */

public class AddSt {

	private ObtainDurationsWithoutStart withoutStart;
	private ObtainDurationWithStart withStart;
	private AddStMain addStart;
	private AddStSecPanel inPanel;

	private BoxandWhiskerPlot graphing;
	private List<Pair<String, Long>> resDurationList;
	private Set<String> resNamesList;
	private final ArrayList<Date> startDatesList;
	private int addCounter, removeCounter;
	private String displayChoice, outlierChoice;

	private JPanel jPanel;

	private final UIPluginContext context;
	private XLog log;
	private XLog changedLog;

	public AddSt(final UIPluginContext context, XLog log) {
		this.context = context;
		this.log = log;

		jPanel = new JPanel();
		resNamesList = new TreeSet<String>();
		startDatesList = new ArrayList<Date>();
	}

	public AddStVisualizer mine() {

		context.getProgress().setMinimum(0);
		context.getProgress().setIndeterminate(false);

		inPanel = new AddStSecPanel(context);
		jPanel = inPanel.getPanel();

		/**
		 * Display the first Panel
		 */
		context.showConfiguration("Insert Missing Events", jPanel);
		displayChoice = inPanel.getDisplayChoice();

		/**
		 * Check if the log has any start events
		 */
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				if (XLifecycleExtension.instance().extractTransition(event).equals("start")) {
					Date startDate = XTimeExtension.instance().extractTimestamp(event);
					startDatesList.add(startDate);

				}
			}
		}

		context.getProgress().inc();
		/**
		 * if the log does not have any start events
		 */
		if (startDatesList.size() == 0) {
			/**
			 * Obtain the durations per activity executed for each resource
			 */
			withoutStart = new ObtainDurationsWithoutStart(log, displayChoice);
			resDurationList = withoutStart.getResDateDetails();
			resNamesList = withoutStart.getResNames();
			/**
			 * Construct a Box-and-Whisker Graph showing the durations per
			 * resource and any outliers
			 */

			graphing = new BoxandWhiskerPlot(context, resDurationList, resNamesList);
			outlierChoice = graphing.getOutlierRange();

			if (outlierChoice != null) {

				/**
				 * Add the start events to the log
				 */
				addStart = new AddStMain(context, log, displayChoice, outlierChoice);
				addCounter = addStart.getAddCounter();
				removeCounter = addStart.getRemoveCounter();
				context.log("The Number of Start Events Added is: " + addCounter);
				context.log("The Number of Events Removed is: " + removeCounter);
				/**
				 * Get the edited log
				 */
				log = addStart.getLog();
			} else {
				context.getFutureResult(0).cancel(true);

			}

			changedLog = addStart.getLog();
			/**
			 * if the log has some start events
			 */
		} else {
			withStart = new ObtainDurationWithStart(context, log);
			resDurationList = withStart.getResDateDetails();
			resNamesList = withStart.getResNames();
			/**
			 * Construct a Box-and-Whisker Graph showing the durations per
			 * resource and any outliers
			 */

			graphing = new BoxandWhiskerPlot(context, resDurationList, resNamesList);
			outlierChoice = graphing.getOutlierRange();
			if (outlierChoice != null) {
				/**
				 * Add the start events to the log
				 */
				addStart = new AddStMain(context, log, displayChoice, outlierChoice);
				addCounter = addStart.getAddCounter();
				removeCounter = addStart.getRemoveCounter();
				context.log("The Number of Start Events Added is: " + addCounter);
				context.log("The Number of Events Removed is: " + removeCounter);
				changedLog = addStart.getLog();

			} else {
				context.getFutureResult(0).cancel(true);

			}

		}

		AddStVisualizer output = new AddStVisualizer();
		output.setLog(changedLog);
		return output;
	}

}
