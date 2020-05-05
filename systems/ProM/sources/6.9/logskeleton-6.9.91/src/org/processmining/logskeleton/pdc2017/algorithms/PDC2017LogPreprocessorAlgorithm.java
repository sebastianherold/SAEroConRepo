package org.processmining.logskeleton.pdc2017.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.models.ClassificationProblem;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log10FilterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log10SplitterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log1FilterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log2FilterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log2SplitterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log4SplitterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log5FilterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log5SplitterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log7SplitterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log9FilterPlugin;
import org.processmining.logskeleton.pdc2017.plugins.PDC2017Log9SplitterPlugin;

public class PDC2017LogPreprocessorAlgorithm extends LogPreprocessorAlgorithm {

	private boolean useFilters;
	private boolean useExtenders;
	private boolean useSplitters;
	
	public PDC2017LogPreprocessorAlgorithm(boolean useFilters, boolean useExtenders, boolean useSplitters) {
		this.useFilters = useFilters;
		this.useExtenders = useExtenders;
		this.useSplitters = useSplitters;
	}
	
	public PDC2017LogPreprocessorAlgorithm() {
		this.useFilters = true;
		this.useExtenders = true;
		this.useSplitters = true;
	}
	
	public ClassificationProblem preprocess(PluginContext context, ClassificationProblem problem) {

		String name = XConceptExtension.instance().extractName(problem.getTrainingLog());

		if (useFilters) {
			/*
			 * Filter out the assumed noise.
			 */
			System.out.println("====== Filter " + name + " ======");
			if (name.equals("log1")) {
				problem.setTrainingLog((new PDC2017Log1FilterPlugin()).run(context, problem.getTrainingLog()));
			} else if (name.equals("log2")) {
				problem.setTrainingLog((new PDC2017Log2FilterPlugin()).run(context, problem.getTrainingLog()));
			} else if (name.equals("log5")) {
				problem.setTrainingLog((new PDC2017Log5FilterPlugin()).run(context, problem.getTrainingLog()));
			} else if (name.equals("log9")) {
				problem.setTrainingLog((new PDC2017Log9FilterPlugin()).run(context, problem.getTrainingLog()));
			} else if (name.equals("log10")) {
				problem.setTrainingLog((new PDC2017Log10FilterPlugin()).run(context, problem.getTrainingLog()));
			}
		}

		if (useExtenders) {
			/*
			 * Extend log with assumed false negatives from test log. Assumption
			 * is here that the test log is not that complete :-(.
			 */
			if (name.equals("log1")) {
				addTrace(
						problem.getTrainingLog(),
						new ArrayList<String>(Arrays.asList("g", "w", "p", "c", "v", "m", "b", "u", "t", "s", "f", "r",
								"l", "k", "j")));
			} else if (name.equals("log6")) {
				addTrace(problem.getTrainingLog(), new ArrayList<String>(Arrays.asList("d", "n", "a", "f", "k")));
				addTrace(problem.getTrainingLog(),
						new ArrayList<String>(Arrays.asList("c", "t", "q", "c", "a", "t", "r")));
			}
		}

		if (useSplitters) {
			/*
			 * Split the assumed reoccurring activities.
			 */
			System.out.println("====== Split " + name + " ======");
			if (name.equals("log2")) {
				PDC2017Log2SplitterPlugin splitter = new PDC2017Log2SplitterPlugin();
				problem.setTrainingLog(splitter.run(context, problem.getTrainingLog()));
				problem.setTestLog(splitter.run(context, problem.getTestLog()));
			} else if (name.equals("log4")) {
				PDC2017Log4SplitterPlugin splitter = new PDC2017Log4SplitterPlugin();
				problem.setTrainingLog(splitter.run(context, problem.getTrainingLog()));
				problem.setTestLog(splitter.run(context, problem.getTestLog()));
			} else if (name.equals("log5")) {
				PDC2017Log5SplitterPlugin splitter = new PDC2017Log5SplitterPlugin();
				problem.setTrainingLog(splitter.run(context, problem.getTrainingLog()));
				problem.setTestLog(splitter.run(context, problem.getTestLog()));
			} else if (name.equals("log7")) {
				PDC2017Log7SplitterPlugin splitter = new PDC2017Log7SplitterPlugin();
				problem.setTrainingLog(splitter.run(context, problem.getTrainingLog()));
				problem.setTestLog(splitter.run(context, problem.getTestLog()));
			} else if (name.equals("log9")) {
				PDC2017Log9SplitterPlugin splitter = new PDC2017Log9SplitterPlugin();
				problem.setTrainingLog(splitter.run(context, problem.getTrainingLog()));
				problem.setTestLog(splitter.run(context, problem.getTestLog()));
			} else if (name.equals("log10")) {
				PDC2017Log10SplitterPlugin splitter = new PDC2017Log10SplitterPlugin();
				problem.setTrainingLog(splitter.run(context, problem.getTrainingLog()));
				problem.setTestLog(splitter.run(context, problem.getTestLog()));
			}
		}

		return problem;
	}

	public String toString() {
		if (useFilters && useExtenders && useSplitters) {
			return "PDC 2017";
		}
		List<String> selected = new ArrayList<String>();
		if (useFilters) {
			selected.add("Filter");
		}
		if (useExtenders) {
			selected.add("Extend");
		}
		if (useSplitters) {
			selected.add("Split");
		}
		return "PDC 2017 " + selected;
	}

	private static void addTrace(XLog log, List<String> activities) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XTrace trace = factory.createTrace();
		for (String activity : activities) {
			XEvent event = factory.createEvent();
			XConceptExtension.instance().assignName(event, activity);
			trace.add(event);
		}
		log.add(trace);
	}

}
