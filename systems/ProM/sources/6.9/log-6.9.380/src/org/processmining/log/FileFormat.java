package org.processmining.log;

/**
 * @see LogFileFormat
 */
@Deprecated
public enum FileFormat {
	XES("xes"), XES_GZ("xes.gz"), MXML("mxml"), MXML_GZ("mxml.gz");

	private String desc;

	private FileFormat(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return desc;
	}
}
