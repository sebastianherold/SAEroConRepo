package org.processmining.logskeleton.algorithms;

import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;

public class LogSkeletonCheckerAlgorithm {

	public XLog apply(LogSkeleton skeleton, XLog log, XEventClassifier classifier, Set<String> messages, boolean[] checks) {
		XLog classifiedLog = XFactoryRegistry.instance().currentDefault().createLog();
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog();
		LogSkeletonBuilderAlgorithm algorithm = new LogSkeletonBuilderAlgorithm();
		
		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount traceModel = algorithm.count(traceLog, classifier);
			traceModel.print("Trace " + XConceptExtension.instance().extractName(trace));
			if (skeleton.check(trace, traceModel, messages, checks)) {
				classifiedLog.add(trace);
			}
		}
		return classifiedLog;
	}
}
