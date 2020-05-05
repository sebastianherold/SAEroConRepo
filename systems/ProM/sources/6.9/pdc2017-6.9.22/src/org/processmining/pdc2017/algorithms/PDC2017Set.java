package org.processmining.pdc2017.algorithms;

public enum PDC2017Set {

	TRAIN("Training log", "log"),
	CAL1("Calibration log 1", "test_log_may_"),
	CAL2("Calibration log 2", "test_log_june_"),
	TEST("Test log", "test_log_july_");
	
	private String prefix;
	private String label;
	
	private PDC2017Set(String label, String prefix) {
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
