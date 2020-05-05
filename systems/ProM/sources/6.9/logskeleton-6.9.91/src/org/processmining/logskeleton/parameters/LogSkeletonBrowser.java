package org.processmining.logskeleton.parameters;

public enum LogSkeletonBrowser {
	
//	ALWAYSTOGETHER("Always Together"),
	ALWAYSAFTER("Response"),
	ALWAYSBEFORE("Precedence"),
	NEVERAFTER("Not Response"),
	NEVERBEFORE("Not Precedence"),
	NEVERTOGETHER("Not Co-Existence");
//	OFTENNEXT("Often Next"),
//	OFTENPREVIOUS("Often Previous"),
//	NEVERTOGETHERSELF("Never Together (Self)"),
//	SOMETIMESBEFORE("Sometimes Before"),
//	SOMETIMESAFTER("Sometimes After"),
//	NEXTONEWAY("Next (One Way)"),
//	NEXTBOTHWAYS("Next (Both Ways)");
	
	private String label;

	private LogSkeletonBrowser(String label) {
		this.label = label;
	}
	
	public String toString() {
		return label;
	}
}
