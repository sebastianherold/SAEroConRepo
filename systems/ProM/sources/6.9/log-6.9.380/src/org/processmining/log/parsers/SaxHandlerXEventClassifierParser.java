package org.processmining.log.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.log.models.XEventClassifierList;
import org.processmining.log.models.impl.XEventClassifierListImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This parser tries to read the classifiers contained in a log, based on the
 * global event attributes. In order to find the classifiers, the elements of
 * the list of globals are checked if they are contained in the classifier
 * definition. This is done greedily based on the global´s sting length:
 * longest first. If globals are contained in the classifier definition, then we
 * return the corresponding XEventClassifier.
 * 
 * @author abolt *
 */
public class SaxHandlerXEventClassifierParser extends DefaultHandler {

	private static final String CLASSIFIER_TAG = "classifier", CLASSIFIER_TAG_ATTRIBUTE_NAME = "name",
			CLASSIFIER_TAG_ATTRIBUTEKEY = "keys";

	private final XEventClassifierList classifierList = new XEventClassifierListImpl();

	public XEventClassifierList getClassifierList() {
		return classifierList;
	}

	private final List<String> globalEventAttributes;
	private XEventClassifier current = null;
	private boolean insideClassifierTag = false;

	public SaxHandlerXEventClassifierParser(List<String> globalEventAttributes) {
		Collections.sort(globalEventAttributes, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o2.length() - o1.length();
			}
		});
		this.globalEventAttributes = globalEventAttributes;
	}

	@Override
	public void startElement(String uri, String local, String qName, Attributes attributes) throws SAXException {
		if (!insideClassifierTag) {
			if (qName.toLowerCase().equals(CLASSIFIER_TAG)) {
				insideClassifierTag = true;
				String name = attributes.getValue(CLASSIFIER_TAG_ATTRIBUTE_NAME) == null ? ""
						: attributes.getValue(CLASSIFIER_TAG_ATTRIBUTE_NAME);
				String keys = attributes.getValue(CLASSIFIER_TAG_ATTRIBUTEKEY) == null ? ""
						: attributes.getValue(CLASSIFIER_TAG_ATTRIBUTEKEY);
				List<String> parsedKeys = getGlobalsInClassifier(globalEventAttributes, keys);
				if (parsedKeys != null) {
					current = new XEventAttributeClassifier(name, parsedKeys.toArray(new String[parsedKeys.size()]));
				} else {
					current = null;
				}
			}
		} else {
			if (qName.toLowerCase().equals(CLASSIFIER_TAG)) {
				throw new SAXException("Nested xml tag in classifier tag.");
			}
			// the code here is actually not described by the standard, so we  disable this for now.
			//			if (qName.toLowerCase().equals(STRING_TAG) && attributes.getValue(STRING_TAG_ATTRIBUTE_KEY) != null) {
			//				if (attributes.getValue(STRING_TAG_ATTRIBUTE_KEY).equals(CLASSIFIER_TAG_ATTRIBUTE_NAME)) {
			//					if (attributes.getValue(STRING_TAG_ATTRIBUTE_VALUE) != null) {
			//						current.setName(attributes.getValue(STRING_TAG_ATTRIBUTE_VALUE));
			//					}
			//				} else if (attributes.getValue(STRING_TAG_ATTRIBUTE_KEY).equals(CLASSIFIER_TAG_ATTRIBUTEKEY)) {
			//					if (attributes.getValue(STRING_TAG_ATTRIBUTE_VALUE) != null) {
			//						List<String> parsedKeys = getGlobalsInClassifier(globalEventAttributes,
			//								attributes.getValue(STRING_TAG_ATTRIBUTE_VALUE));
			//						current = new XEventAttributeClassifier(current.name(),
			//								parsedKeys.toArray(new String[parsedKeys.size()]));
			//					}
			//				}
			//			}
		}
	}

	@Override
	public void endElement(String uri, String local, String qName) {
		if (qName.toLowerCase().equals(CLASSIFIER_TAG)) {
			insideClassifierTag = false;
			if (current != null) {
				classifierList.add(current);
			}
		}

	}

	private List<String> getGlobalsInClassifier(List<String> globals, String keysInClassifier) {
		List<String> classifierKeys = new ArrayList<String>();
		for (String globalElement : globalEventAttributes) {
			if (keysInClassifier.contains(globalElement)) {
				classifierKeys.add(globalElement);
				// This will replace all matches, but since the list is ordered greedily, it should not replace strings used by other globals
				keysInClassifier = keysInClassifier.replace(globalElement, "");
			}
		}
		keysInClassifier = keysInClassifier.replace(" ", "");
		keysInClassifier = keysInClassifier.replace("'", "");
		return keysInClassifier.isEmpty() ? classifierKeys : null;
	}

}
