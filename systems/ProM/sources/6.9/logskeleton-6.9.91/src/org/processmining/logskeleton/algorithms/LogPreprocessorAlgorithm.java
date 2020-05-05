package org.processmining.logskeleton.algorithms;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.models.ClassificationProblem;

public class LogPreprocessorAlgorithm {

	public ClassificationProblem preprocess(PluginContext context, ClassificationProblem problem) {
		return problem;
	}
	
	public String toString() {
		return "None";
	}
}
