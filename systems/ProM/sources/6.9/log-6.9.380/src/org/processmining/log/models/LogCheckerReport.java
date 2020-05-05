package org.processmining.log.models;

import org.processmining.framework.util.HTMLToString;

public class LogCheckerReport implements HTMLToString {

	private String report;
	
	public LogCheckerReport() {
		report = "";
	}
	
	public void add(String message) {
		report += message;
	}
	
	public String toHTMLString(boolean includeHTMLTags) {
		return includeHTMLTags ? "<html>" + report + "</html>" : report;
	}

}
