package org.processmining.log.models.impl;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.LogFrequency;
import org.processmining.log.models.LogFrequencyArray;

public class LogFrequencyArrayImpl implements LogFrequencyArray {

	private String label;
	private LogFrequency[] subFrequencies;
	private int index;
	private int factor;
	private int buckets = 20;

	public LogFrequencyArrayImpl(EventLogArray logs) {
		label = null;
		if (logs.getSize() > 0) {
			label = XConceptExtension.instance().extractName(logs.getLog(0));
		}
		if (label == null) {
			label = "<Not specified>";
		}
		factor = (logs.getLog(0).size() / buckets) + (logs.getLog(0).size() % buckets > 0 ? 1 : 0);
		subFrequencies = new LogFrequency[logs.getSize()];
		for (int i = 0; i < logs.getSize(); i++) {
			subFrequencies[i] = LogFrequencyFactory.createLogFrequency(logs.getLog(i));
		}
		index = -1;
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
		buf.append("<tr><th>Frequency</th>");
		for (int i = 0; i < subFrequencies.length ; i++) {
			buf.append("<th>Log " + (i + 1) + "</th>");
			
		}
		buf.append("</tr>");
		for (int i = 0; i < buckets; i++) {
			buf.append("<tr><th>");
			buf.append(i * factor + 1);
			buf.append("</th>");
			for (int j = 0; j < subFrequencies.length ; j++) {
				buf.append("<td>" + subFrequencies[j].get(i * factor + 1) + "</td>");
			}
			buf.append("</tr>");
		}
		buf.append("</table>");
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}

	public void set(int index) {
		this.index = index;
	}

	public void add(int frequency) {
		subFrequencies[index].add(frequency);
	}

	public int get(int frequency) {
		// TODO Auto-generated method stub
		return subFrequencies[index].get(frequency);
	}
}
