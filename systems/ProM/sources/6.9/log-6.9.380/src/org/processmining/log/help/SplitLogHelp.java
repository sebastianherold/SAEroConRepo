package org.processmining.log.help;

public class SplitLogHelp {

	public final static String TEXT = ""
			+ "Splits traces in a log on a selected event attribute. "
			+ "If the attribute exists for an event, then it is assumed to be a white-space spearated list of values. "
			+ "A trace will be split into as many subtraces as there are values in its events for the selected attribute. "
			+ "The subtrace for a given value contains (in the same order as in the trace) all events that: "
			+ "(1) either contain this value in the attribute value or (2) have no such attribute.";
}
