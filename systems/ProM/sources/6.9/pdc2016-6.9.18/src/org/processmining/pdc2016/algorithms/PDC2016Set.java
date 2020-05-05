package org.processmining.pdc2016.algorithms;

public enum PDC2016Set {

	TRAIN("Training log", "training_log_"),
	CAL1("Calibration log 1", "test_log_april_"),
	CAL2("Calibration log 2", "test_log_may_"),
	TEST("Test log", "test_log_june_");
	
	private String prefix;
	private String label;
	
	private PDC2016Set(String label, String prefix) {
		this.label = label;
		this.prefix = prefix;
	}
	
	public String getFileName(int nr) {
		return prefix + nr;
	}
	
	public String toString() {
		return label;
	}
}
