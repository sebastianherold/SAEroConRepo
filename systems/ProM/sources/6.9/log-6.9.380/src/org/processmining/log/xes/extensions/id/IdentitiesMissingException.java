package org.processmining.log.xes.extensions.id;

import java.util.Collection;

import org.deckfour.xes.model.XAttributable;

public class IdentitiesMissingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8593596865075088429L;
	
	private Collection<XAttributable> missing;

	public IdentitiesMissingException() {
		super("Not all elements in the log have an identifier");
	}
	
	public IdentitiesMissingException(Collection<XAttributable> missing) {
		this();
		
		this.missing = missing;
	}
	
}
