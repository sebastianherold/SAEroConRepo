package org.processmining.log.csv.config;


public enum CSVSeperator {
	COMMA("Comma (,)", ','), 
	SEMICOLON("Semicolon (;)",';'),
	TAB("Tab", '\t'),
	WHITESPACE("Whitespace",' ');

	private final String description;
	private final char seperatorChar;

	private CSVSeperator(String description, char seperatorChar) {
		this.description = description;
		this.seperatorChar = seperatorChar;
	}

	public String toString() {
		return description;
	}

	public char getSeperatorChar() {
		return seperatorChar;
	}

}