package org.processmining.logskeleton.pdc2017.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.HTMLToString;
import org.processmining.logskeleton.pdc2017.parameters.PDC2017TestParameters;
import org.processmining.pdc2017.algorithms.PDC2017Set;

public class PDC2017TestModel implements HTMLToString {

	private List<Integer> numbers;
	private List<XLog> cal1Logs;
	private List<XLog> cal2Logs;
	private List<XLog> testLogs;
	private PDC2017TestParameters parameters;

	public PDC2017TestModel(PDC2017TestParameters parameters) {
		numbers = new ArrayList<Integer>();
		cal1Logs = new ArrayList<XLog>();
		cal2Logs = new ArrayList<XLog>();
		testLogs = new ArrayList<XLog>();
		this.parameters = parameters;
	}

	public void add(int i, XLog cal1Log, XLog cal2Log, XLog testLog) {
		numbers.add(i);
		if (cal1Log != null) {
			cal1Logs.add(cal1Log);
		}
		if (cal2Log != null) {
			cal2Logs.add(cal2Log);
		}
		if (testLog != null) {
			testLogs.add(testLog);
		}
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buf = new StringBuffer();
		if (includeHTMLTags) {
			buf.append("<html>");
		}
		List<XLog> logs;
		for (int l = 0; l < 3; l++) {
			if (l == 0 && cal1Logs.isEmpty()) {
				continue;
			}
			if (l == 1 && cal2Logs.isEmpty()) {
				continue;
			}
			if (l == 2 && testLogs.isEmpty()) {
				continue;
			}
			if (l == 0) {
				buf.append("<h1>"+ PDC2017Set.CAL1 + "? using " + parameters.getPreprocessor() + "</h1>");
				logs = cal1Logs;
			} else if (l == 1) {
				buf.append("<h1>"+ PDC2017Set.CAL2 + "? using " + parameters.getPreprocessor() + "</h1>");
				logs = cal2Logs;
			} else {
				buf.append("<h1>"+ PDC2017Set.TEST + "? using " + parameters.getPreprocessor() + "</h1>");
				logs = testLogs;
			}
			buf.append("<table><tr><th></th>");
			for (int n = 1; n < 21; n++) {
				buf.append("<th>trace_" + n + "</th>");
			}
			buf.append("<th>#True</th>");
			buf.append("</tr>");
			for (int i = 0; i < numbers.size(); i++) {
				Set<String> acceptedTraces = new HashSet<String>();
				for (XTrace trace : logs.get(i)) {
					acceptedTraces.add(XConceptExtension.instance().extractName(trace));
				}
				buf.append("<tr><td>model_" + numbers.get(i) + "</td>");
				for (int n = 1; n < 21; n++) {
					buf.append("<td>" + (acceptedTraces.contains("" + n) ? "TRUE" : "FALSE") + "</td>");
				}
				buf.append("<td>" + logs.get(i).size() + "</td>");
				buf.append("</tr>");
			}
			buf.append("</table>");
		}
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}

}
