package org.processmining.log.help;

public class HighFrequencyFilterHelp {

	public final static String TEXT = ""
			+ "Filters a log using a frequency threshold (a percentage) and a distance threshold (edit distance). "
			+ "Any trace that either<ol>"
			+ "<li>has a high-enough frequency or</li>"
			+ "<li>is close enough to a trace with high-enough frequency</li>"
			+ "</ol>will be filtered in. "
			+ "A trace has high-enough frequency if its frequency is required to reach the overall frequency threshold.";

}
