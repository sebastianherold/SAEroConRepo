package org.processmining.logskeleton.classifiers;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XVisitor;

public class LogSkeletonClassifier implements XEventClassifier {

	private XEventClassifier prefixClassifier;
	public final static String SUFFIX = ".suffix";
	
	public LogSkeletonClassifier() {
		this(new XEventNameClassifier());
	}
	
	public LogSkeletonClassifier(XEventClassifier classifier) {
		this.prefixClassifier = classifier;
	}
	
	public void accept(XVisitor arg0, XLog arg1) {
	}

	public String getClassIdentity(XEvent event) {
		if (event.getAttributes().containsKey(SUFFIX)) {
			return prefixClassifier.getClassIdentity(event) + event.getAttributes().get(SUFFIX).toString();
		}
		return prefixClassifier.getClassIdentity(event);
	}

	public String[] getDefiningAttributeKeys() {
		return prefixClassifier.getDefiningAttributeKeys();
	}

	public String name() {
		return null;
	}

	public boolean sameEventClass(XEvent arg0, XEvent arg1) {
		return false;
	}

	public void setName(String arg0) {
	}

}
