package org.processmining.logskeleton.algorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;
import org.processmining.logskeleton.parameters.SplitterParameters;

public class SplitterAlgorithm {

	public XLog apply(XLog log, XEventClassifier classifier, SplitterParameters parameters) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault()
				.createLog((XAttributeMap) log.getAttributes().clone());
		for (XTrace trace : log) {
			XTrace filteredTrace = XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());
			int milestone = 0;
			for (XEvent event : trace) {
				String activity = classifier.getClassIdentity(event);
				if (activity.equals(parameters.getDuplicateActivity())) {
					XEvent filteredEvent = (XEvent) event.clone(); //XFactoryRegistry.instance().currentDefault().createEvent();
					if (event.getAttributes().containsKey(LogSkeletonClassifier.SUFFIX)) {
						((XAttributeLiteral) filteredEvent.getAttributes().get(LogSkeletonClassifier.SUFFIX)).setValue(event.getAttributes().get(LogSkeletonClassifier.SUFFIX)+ "." + milestone);
					} else {
						filteredEvent.getAttributes().put(LogSkeletonClassifier.SUFFIX, new XAttributeLiteralImpl(LogSkeletonClassifier.SUFFIX, "." + milestone));
					}
					filteredTrace.add(filteredEvent);
				} else {
					filteredTrace.add(event);
				}
				if (parameters.getMilestoneActivities().contains(activity)) {
					if (milestone == 0) {
						milestone++;
					}
				}
			}
			filteredLog.add(filteredTrace);
		}
		//		System.out.println("[SplitterAlgorithm] Split log contains " + filteredLog.size() + " traces");
		return filteredLog;
	}

	public XLog apply7B(XLog log, XEventClassifier classifier) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault()
				.createLog((XAttributeMap) log.getAttributes().clone());
		for (XTrace trace : log) {
			XTrace filteredTrace = XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());
			for (int i = 0; i < trace.size(); i++) {
				if (i == trace.size() - 1 && classifier.getClassIdentity(trace.get(i)).equals("b")) {
					XEvent filteredEvent = (XEvent) trace.get(i).clone();
					trace.get(i).getAttributes().put(LogSkeletonClassifier.SUFFIX, new XAttributeLiteralImpl(LogSkeletonClassifier.SUFFIX, ".1"));
					filteredTrace.add(filteredEvent);
				} else if (i == trace.size() - 2 && classifier.getClassIdentity(trace.get(i)).equals("b")
						&& classifier.getClassIdentity(trace.get(i + 1)).equals("s")) {
					XEvent filteredEvent = (XEvent) trace.get(i).clone();
					trace.get(i).getAttributes().put(LogSkeletonClassifier.SUFFIX, new XAttributeLiteralImpl(LogSkeletonClassifier.SUFFIX, ".1"));
					filteredTrace.add(filteredEvent);
				} else if (classifier.getClassIdentity(trace.get(i)).equals("b")) {
					XEvent filteredEvent = (XEvent) trace.get(i).clone();
					trace.get(i).getAttributes().put(LogSkeletonClassifier.SUFFIX, new XAttributeLiteralImpl(LogSkeletonClassifier.SUFFIX, ".0"));
					filteredTrace.add(filteredEvent);
				} else {
					filteredTrace.add(trace.get(i));
				}
			}
			filteredLog.add(filteredTrace);
		}
		//		System.out.println("[SplitterAlgorithm] Split log contains " + filteredLog.size() + " traces");
		return filteredLog;
	}

	public XLog apply20194(XLog log) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault()
				.createLog((XAttributeMap) log.getAttributes().clone());
		for (XTrace trace : log) {
			XTrace filteredTrace = XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());

			for (int i = 0; i < trace.size(); i++) {
				XEvent filteredEvent = XFactoryRegistry.instance().currentDefault().createEvent();
				XConceptExtension.instance().assignName(filteredEvent,
						XConceptExtension.instance().extractName(trace.get(i)));
				if (XConceptExtension.instance().extractName(trace.get(i)).equals("c")) {
					if (i == 4 || i == 5) {
						XConceptExtension.instance().assignName(filteredEvent, "c.0");
					} else if (i + 1 < trace.size() && new String(" w f ac ag aj r ").contains(" " + XConceptExtension.instance().extractName(trace.get(i + 1)) + " ")) {
						XConceptExtension.instance().assignName(filteredEvent, "c.3");
					} else if (i + 1 < trace.size() && XConceptExtension.instance().extractName(trace.get(i + 1)).equals("as")) {
						XConceptExtension.instance().assignName(filteredEvent, "c.1");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "c.2");
					}
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("t")) {
					if (new String(" am c b q ").contains(" " + XConceptExtension.instance().extractName(trace.get(i - 1)) + " ")) {
						XConceptExtension.instance().assignName(filteredEvent, "t.0");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "t.1");
					}
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("ad")) {
					if (i == trace.size() - 1) {
						XConceptExtension.instance().assignName(filteredEvent, "ad.1");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "ad.0");
					}
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("as")) {
					if (XConceptExtension.instance().extractName(trace.get(i - 1)).equals("c")) {
						XConceptExtension.instance().assignName(filteredEvent, "as.0");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "as.1");
					}
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("e")) {
					if (XConceptExtension.instance().extractName(trace.get(i - 1)).equals("l")) {
						XConceptExtension.instance().assignName(filteredEvent, "e.0");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "e.1");
					}
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("aj")) {
					if (i == 0 || i == 1) {
						XConceptExtension.instance().assignName(filteredEvent, "aj.0");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "aj.1");
					}
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("f")) {
					if (i == 6) {
						XConceptExtension.instance().assignName(filteredEvent, "f.0");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "f.1");
					}
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("w")) {
					if (i == 6) {
						XConceptExtension.instance().assignName(filteredEvent, "w.0");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "w.1");
					}
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("r")) {
					if (XConceptExtension.instance().extractName(trace.get(i - 1)).equals("as")) {
						XConceptExtension.instance().assignName(filteredEvent, "r.0");
					} else {
						XConceptExtension.instance().assignName(filteredEvent, "r.1");
					}
				}
				filteredTrace.add(filteredEvent);
			}
			filteredLog.add(filteredTrace);
		}
		//		System.out.println("[SplitterAlgorithm] Split log contains " + filteredLog.size() + " traces");
		return filteredLog;
	}
	
}
