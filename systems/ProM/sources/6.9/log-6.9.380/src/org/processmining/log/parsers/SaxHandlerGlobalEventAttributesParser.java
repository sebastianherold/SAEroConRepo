package org.processmining.log.parsers;

import java.util.List;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxHandlerGlobalEventAttributesParser extends DefaultHandler {

	private static final String TAG = "global", KEY = "key", SCOPE = "scope", EVENT = "event";

	private final List<String> globals = new Vector<String>();

	private boolean isInGlobalEvent = false;

	@Override
	public void startElement(String uri, String local, String qName, Attributes attributes) throws SAXException {
		if (isInGlobalEvent == false) {
			if (qName.toLowerCase().equals(TAG)) {
				String scope = attributes.getValue(SCOPE);
				if (scope != null && scope.equals(EVENT)) {
					isInGlobalEvent = true;
				}
			}
		} else {
			if (qName.toLowerCase().equals(TAG))
				throw new SAXException();
			String key = attributes.getValue(KEY);
			if (key != null)
				globals.add(key);
		}
	}
	
	@Override
	public void endElement(String uri, String local, String qName) {
		if(isInGlobalEvent)
			if(qName.toLowerCase().equals(TAG))
				isInGlobalEvent = false;
	}
	
	public List<String> getGlobalEventAttributes(){
		return globals;
	}

}
