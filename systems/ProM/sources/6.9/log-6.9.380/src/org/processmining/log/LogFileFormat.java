package org.processmining.log;

/**
 * Specifies the different possible Log File Formats. Mainly used within
 * RapidProM.
 *
 */
public enum LogFileFormat {
	XES("xes"), XES_GZ("xes.gz"), MXML("mxml"), MXML_GZ("mxml.gz");

	private String desc;

	private LogFileFormat(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return desc;
	}
}
