package org.processmining.logskeleton.models;

import org.deckfour.xes.model.XLog;

public class ClassificationProblem {

	private XLog trainingLog;
	private XLog testLog;
	
	public ClassificationProblem(XLog trainingLog, XLog testLog) {
		this.trainingLog = trainingLog;
		this.testLog = testLog;
	}
	
	public XLog getTrainingLog() {
		return trainingLog;
	}
	
	public void setTrainingLog(XLog trainingLog) {
		this.trainingLog = trainingLog;
	}

	public XLog getTestLog() {
		return testLog;
	}

	public void setTestLog(XLog testLog) {
		this.testLog = testLog;
	}
	
}
