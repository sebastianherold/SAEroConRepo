package org.processmining.pdc2019.algorithms;

public enum PDC2019Set {

	TRAIN("Training log", "log", "-training"),
	CAL1("Calibration log 1", "log", "-15February"),
	CAL2("Calibration log 2", "log", "-15March"),
	TEST("Test log", "log", "-May");
	
	private String prefix;
	private String postfix;
	private String label;
	
	private PDC2019Set(String label, String prefix, String postfix) {
		this.label = label;
		this.prefix = prefix;
		this.postfix = postfix;
	}
	
	public String getFileName(int nr) {
		return prefix + nr + postfix;
	}
	
	public String toString() {
		return label;
	}
}
