package org.processmining.logskeleton.algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.models.ClassificationProblem;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.plugins.LogSkeletonBuilderPlugin;
import org.processmining.logskeleton.plugins.LogSkeletonCheckerPlugin;

public class LogSkeletonClassifierAlgorithm {

	public XLog apply(PluginContext context, XLog trainingLog, XLog testLog, XEventClassifier classifier, LogPreprocessorAlgorithm preprocessor) {
		String name = XConceptExtension.instance().extractName(trainingLog);

		ClassificationProblem problem = new ClassificationProblem(trainingLog, testLog);

		/*
		 * Preprocess the logs.
		 */
		problem = preprocessor.preprocess(context, problem);
		XLog filteredTrainingLog = problem.getTrainingLog();
		XLog filteredTestLog = problem.getTestLog();
		

		/*
		 * Build the log skeleton.
		 */
		LogSkeletonBuilderPlugin createPlugin = new LogSkeletonBuilderPlugin();
		LogSkeleton model = createPlugin.run(context, filteredTrainingLog);
		context.getProvidedObjectManager().createProvidedObject("Model for " + name, model, LogSkeleton.class, context);

		/*
		 * Use the log skeleton to classify the test traces.
		 */
		System.out.println("[LogSkeletonClassifierAlgorithm] Classify " + name + " ======");
		XLog classifiedTestLog = classify(context, model, filteredTrainingLog, filteredTestLog, classifier, name);
		context.getProvidedObjectManager().createProvidedObject("Classified Log " + name, classifiedTestLog,
				XLog.class, context);

		/*
		 * Return the log containing all assumed positive test traces.
		 */
		return classifiedTestLog;
	}

	private static XLog classify(PluginContext context, LogSkeleton trainingModel, XLog trainingLog, XLog testLog,
			XEventClassifier classifier, String name) {
		LogSkeletonBuilderPlugin createPlugin = new LogSkeletonBuilderPlugin();
		LogSkeletonCheckerPlugin checkPlugin = new LogSkeletonCheckerPlugin();
		Set<String> messages = new HashSet<String>();
		boolean[] checks = new boolean[] { true, true, false };
		XLog classifiedTestLog = checkPlugin.run(context, trainingModel, testLog, messages, checks);
		Set<String> positiveTestTraces = new HashSet<String>();
		int threshold = 0;
		for (XTrace trace : classifiedTestLog) {
			positiveTestTraces.add(XConceptExtension.instance().extractName(trace));
		}
		for (String message : messages) {
			System.out.println("[LogSkeletonClassifierAlgorithm] " + message);
		}
		for (int i = 0; i < 3; i++) {
			checks[0] = (i == 0);
			checks[1] = (i == 1);
			checks[2] = (i == 2);
			for (String activity : trainingModel.getActivities()) {
				if (positiveTestTraces.size() <= threshold) {
					continue;
				}
				if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
					continue;
				}
				if (!trainingModel.getSameCounts(activity).iterator().next().equals(activity)) {
					continue;
				}

				for (int f = 0; f < 2; f++) {
					if (positiveTestTraces.size() <= threshold) {
						continue;
					}
					Set<String> positiveFilters = new HashSet<String>();
					Set<String> negativeFilters = new HashSet<String>();
					if (f == 0) {
						positiveFilters.add(activity);
					} else {
						negativeFilters.add(activity);
					}
										System.out.println("[LogSkeletonClassifierAlgorithm] Positive = " + positiveFilters + ", Negative = " + negativeFilters);
					XLog filteredTrainingLog = filter(trainingLog, classifier, positiveFilters, negativeFilters);
					XLog filteredTestLog = filter(testLog, classifier, positiveFilters, negativeFilters);
					if (filteredTestLog.isEmpty() || filteredTrainingLog.isEmpty() || filteredTrainingLog.size() < 16) {
						continue;
					}
					//					System.out.println("[PDC2017TestPlugin] Remaining traces 1: " + filteredTrainingLog.size());
					LogSkeleton filteredTrainingModel = createPlugin.run(context, filteredTrainingLog);
					messages = new HashSet<String>();
					XLog classifiedFilteredTestLog = checkPlugin.run(context, filteredTrainingModel, filteredTestLog,
							messages, checks);
					for (XTrace subTrace : filteredTestLog) {
						//						if (positiveTestTraces.size() <= threshold) {
						//							continue;
						//						}
						if (!classifiedFilteredTestLog.contains(subTrace)) {
							String caseId = XConceptExtension.instance().extractName(subTrace);
							if (positiveTestTraces.remove(caseId)) {
								System.out.println("[LogSkeletonClassifierAlgoritmm] Case "
										+ XConceptExtension.instance().extractName(subTrace)
										+ " excluded by positive filter " + positiveFilters + " and negative filter "
										+ negativeFilters + ", support = " + filteredTrainingLog.size());
								for (String message : messages) {
									System.out.println("[LogSkeletonClassifierAlgoritmm] " + message);
								}
							}
						}
					}
				}
			}
			for (String activity : trainingModel.getActivities()) {
				if (positiveTestTraces.size() <= threshold) {
					continue;
				}
				if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
					continue;
				}
				if (!trainingModel.getSameCounts(activity).iterator().next().equals(activity)) {
					continue;
				}
				for (String activity2 : trainingModel.getActivities()) {
					if (positiveTestTraces.size() <= threshold) {
						continue;
					}
					if (activity2 == LogSkeletonCount.STARTEVENT || activity2 == LogSkeletonCount.ENDEVENT) {
						continue;
					}
					if (!trainingModel.getSameCounts(activity2).iterator().next().equals(activity2)) {
						continue;
					}
					if (trainingModel.getSameCounts(activity).contains(activity2)) {
						continue;
					}
					for (int f = 0; f < 4; f++) {
						if (positiveTestTraces.size() <= threshold) {
							continue;
						}
						Set<String> positiveFilters = new HashSet<String>();
						Set<String> negativeFilters = new HashSet<String>();
						if (f == 0 || f == 1) {
							positiveFilters.add(activity);
						} else {
							negativeFilters.add(activity);
						}
						if (f == 0 || f == 2) {
							positiveFilters.add(activity2);
						} else {
							negativeFilters.add(activity2);
						}
												System.out.println("[LogSkeletonClassifierAlgorithm] Positive = " + positiveFilters + ", Negative = " + negativeFilters);
						XLog filteredTrainingLog = filter(trainingLog, classifier, positiveFilters, negativeFilters);
						XLog filteredTestLog = filter(testLog, classifier, positiveFilters, negativeFilters);
						if (filteredTestLog.isEmpty() || filteredTrainingLog.isEmpty()
								|| filteredTrainingLog.size() < 16) {
							continue;
						}
						//						System.out.println("[PDC2017TestPlugin] Remaining traces 2: " + filteredTrainingLog.size());
						LogSkeleton filteredTrainingModel = createPlugin.run(context, filteredTrainingLog);
						messages = new HashSet<String>();
						XLog classifiedFilteredTestLog = checkPlugin.run(context, filteredTrainingModel,
								filteredTestLog, messages, checks);
						for (XTrace subTrace : filteredTestLog) {
							//							if (positiveTestTraces.size() <= threshold) {
							//								continue;
							//							}
							if (!classifiedFilteredTestLog.contains(subTrace)) {
								String caseId = XConceptExtension.instance().extractName(subTrace);
								if (positiveTestTraces.remove(caseId)) {
									System.out.println("[LogSkeletonClassifierAlgoritmm] Case "
											+ XConceptExtension.instance().extractName(subTrace)
											+ " excluded by positive filter " + positiveFilters
											+ " and negative filter " + negativeFilters + ", support = "
											+ filteredTrainingLog.size());
									for (String message : messages) {
										System.out.println("[LogSkeletonClassifierAlgoritmm] " + message);
									}
								}
							}
						}
					}
				}
			}
			for (String activity : trainingModel.getActivities()) {
				if (positiveTestTraces.size() <= threshold) {
					continue;
				}
				if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
					continue;
				}
				if (!trainingModel.getSameCounts(activity).iterator().next().equals(activity)) {
					continue;
				}
				for (String activity2 : trainingModel.getActivities()) {
					if (positiveTestTraces.size() <= threshold) {
						continue;
					}
					if (activity2 == LogSkeletonCount.STARTEVENT || activity2 == LogSkeletonCount.ENDEVENT) {
						continue;
					}
					if (!trainingModel.getSameCounts(activity2).iterator().next().equals(activity2)) {
						continue;
					}
					if (trainingModel.getSameCounts(activity).contains(activity2)) {
						continue;
					}
					for (String activity3 : trainingModel.getActivities()) {
						if (positiveTestTraces.size() <= threshold) {
							continue;
						}
						if (activity3 == LogSkeletonCount.STARTEVENT || activity3 == LogSkeletonCount.ENDEVENT) {
							continue;
						}
						if (!trainingModel.getSameCounts(activity3).iterator().next().equals(activity3)) {
							continue;
						}
						if (trainingModel.getSameCounts(activity).contains(activity3)) {
							continue;
						}
						if (trainingModel.getSameCounts(activity2).contains(activity3)) {
							continue;
						}
						for (int f = 0; f < 8; f++) {
							if (positiveTestTraces.size() <= threshold) {
								continue;
							}
							Set<String> positiveFilters = new HashSet<String>();
							Set<String> negativeFilters = new HashSet<String>();
							if (f == 0 || f == 1 || f == 2 || f == 3) {
								positiveFilters.add(activity);
							} else {
								negativeFilters.add(activity);
							}
							if (f == 0 || f == 1 || f == 4 || f == 5) {
								positiveFilters.add(activity2);
							} else {
								negativeFilters.add(activity2);
							}
							if (f == 0 || f == 2 || f == 4 || f == 6) {
								positiveFilters.add(activity3);
							} else {
								negativeFilters.add(activity3);
							}
														System.out.println("[LogSkeletonClassifierAlgorithm] Positive = " + positiveFilters + ", Negative = " + negativeFilters);
							XLog filteredTrainingLog = filter(trainingLog, classifier, positiveFilters, negativeFilters);
							XLog filteredTestLog = filter(testLog, classifier, positiveFilters, negativeFilters);
							if (filteredTestLog.isEmpty() || filteredTrainingLog.isEmpty()
									|| filteredTrainingLog.size() < 16) {
								continue;
							}
							//						System.out.println("[PDC2017TestPlugin] Remaining traces 2: " + filteredTrainingLog.size());
							LogSkeleton filteredTrainingModel = createPlugin.run(context, filteredTrainingLog);
							messages = new HashSet<String>();
							XLog classifiedFilteredTestLog = checkPlugin.run(context, filteredTrainingModel,
									filteredTestLog, messages, checks);
							for (XTrace subTrace : filteredTestLog) {
								//							if (positiveTestTraces.size() <= threshold) {
								//								continue;
								//							}
								if (!classifiedFilteredTestLog.contains(subTrace)) {
									String caseId = XConceptExtension.instance().extractName(subTrace);
									if (positiveTestTraces.remove(caseId)) {
										System.out.println("[LogSkeletonClassifierAlgoritmm] Case "
												+ XConceptExtension.instance().extractName(subTrace)
												+ " excluded by positive filter " + positiveFilters
												+ " and negative filter " + negativeFilters + ", support = "
												+ filteredTrainingLog.size());
										for (String message : messages) {
											System.out.println("[LogSkeletonClassifierAlgoritmm] " + message);
										}
									}
								}
							}
						}

					}
				}
			}
		}
		XLog newClassifiedTestLog = XFactoryRegistry.instance().currentDefault().createLog();
		XConceptExtension.instance().assignName(newClassifiedTestLog, name + " (classified)");
		for (XTrace trace : classifiedTestLog) {
			if (positiveTestTraces.contains(XConceptExtension.instance().extractName(trace))) {
				newClassifiedTestLog.add(trace);
			}
		}
		return newClassifiedTestLog;
	}

	private static XLog filter(XLog log, XEventClassifier classifier, Set<String> positiveFilters, Set<String> negativeFilters) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace trace : log) {
			boolean ok = true;
			Set<String> toMatch = new HashSet<String>(positiveFilters);
			for (XEvent event : trace) {
				String activity = classifier.getClassIdentity(event);
				if (negativeFilters.contains(activity)) {
					ok = false;
					;
				}
				toMatch.remove(activity);
			}
			if (ok && toMatch.isEmpty()) {
				filteredLog.add(trace);
			}
		}
		return filteredLog;
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
