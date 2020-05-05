package org.processmining.log.models.impl;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.log.models.LogFrequency;

public class LogFrequencyImpl implements LogFrequency {

	private String label;
	private int[] frequencies;
	private int factor;
	private int buckets = 20;
	
	public LogFrequencyImpl(XLog log) {
		label = XConceptExtension.instance().extractName(log);
		if (label == null) {
			label = "<Not specified>";
		}
		factor = (log.size() / buckets) + (log.size() % buckets > 0 ? 1 : 0);
		frequencies = new int[buckets];
		for (int i = 0; i < buckets; i++) {
			frequencies[i] = 0;
		}
	}
	
	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buf = new StringBuffer();
		if (includeHTMLTags) {
			buf.append("<html>");
		}
		buf.append("<h1>");
		buf.append(label);
		buf.append("</h1>");
		buf.append("<table>");
		buf.append("<tr><th>Frequency</th><th>Log</th></tr>");
		for (int i = 0 ; i < buckets ; i++) {
			buf.append("<tr><th>");
			buf.append(i * factor + 1);
			buf.append("</th><td>");
			buf.append(frequencies[i]);
			buf.append("</td></tr>");
		}
		buf.append("</table>");
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}

	public void add(int frequency) {
		frequencies[(frequency - 1) / factor] += frequency;
	}

	public int get(int frequency) {
		return frequencies[(frequency - 1) / factor];
	}
}
