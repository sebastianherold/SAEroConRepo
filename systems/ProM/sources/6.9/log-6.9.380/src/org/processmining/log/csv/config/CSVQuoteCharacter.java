package org.processmining.log.csv.config;


public enum CSVQuoteCharacter {
	
	SINGLE_QUOTE("QUOTE (')", '\''),
	DOUBLE_QUOTE("DOUBLE QUOTE (\")", '"'), 
	NONE("NONE", '\0');

	private final String description;
	private final char quoteChar;

	private CSVQuoteCharacter(String description, char quoteCharacter) {
		this.description = description;
		this.quoteChar = quoteCharacter;
	}

	public String toString() {
		return description;
	}

	public char getQuoteChar() {
		return quoteChar;
	}

}
