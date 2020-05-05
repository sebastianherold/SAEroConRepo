package org.processmining.log.parameters;

import java.util.Date;

public class MergeLogsParameters {

	private String traceId;
	private String dateFormat;
	private Date fromDate;
	private Date toDate;
	private Date specificDate;
	private String requiredWords;
	private String forbiddenWords;
	private int minMatches;
	private boolean maxMatch;
	private boolean multi;
	private int related;
	

	public MergeLogsParameters() {
		setTraceId(null);
		setDateFormat("MM/dd/yyyy HH:mm");
		setFromDate(null);
		setToDate(null);
		setSpecificDate(null);
		setRequiredWords(null);
		setForbiddenWords(null);
		setMinMatches(0);
		setMaxMatch(true);
		setMulti(false);
		setRelated(0);
	}
	
	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String id) {
		this.traceId = id;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Date getSpecificDate() {
		return specificDate;
	}

	public void setSpecificDate(Date specificDate) {
		this.specificDate = specificDate;
	}

	public String getForbiddenWords() {
		return forbiddenWords;
	}

	public void setForbiddenWords(String remove) {
		this.forbiddenWords = remove;
	}

	public int getRelated() {
		return related;
	}

	public void setRelated(int related) {
		this.related = related;
	}

	public int getMinMatches() {
		return minMatches;
	}

	public void setMinMatches(int minMatches) {
		this.minMatches = minMatches;
	}

	public boolean isMaxMatch() {
		return maxMatch;
	}

	public void setMaxMatch(boolean maxMatch) {
		this.maxMatch = maxMatch;
	}

	public boolean isMulti() {
		return multi;
	}

	public void setMulti(boolean multi) {
		this.multi = multi;
	}

	public String getRequiredWords() {
		return requiredWords;
	}

	public void setRequiredWords(String requiredWords) {
		this.requiredWords = requiredWords;
	}
}
