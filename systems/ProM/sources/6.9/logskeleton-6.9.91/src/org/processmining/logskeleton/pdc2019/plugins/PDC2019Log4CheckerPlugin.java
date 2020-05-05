package org.processmining.logskeleton.pdc2019.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.pdc2019.algorithms.PDC2019LogAlgorithm;
import org.processmining.pdc2019.algorithms.PDC2019Set;
import org.processmining.pdc2019.parameters.PDC2019Parameters;

@Plugin(name = "PDC 2019 Log 4 Checker", parameterLabels = {}, returnLabels = { "Checked Log 4" }, returnTypes = {
		XLog.class }, userAccessible = true, help = "PDC 2019 Plug-in")
public class PDC2019Log4CheckerPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public XLog run(PluginContext context) {
		try {
			PDC2019LogAlgorithm algorithm = new PDC2019LogAlgorithm();
			PDC2019Parameters parameters = new PDC2019Parameters();
			parameters.setSet(PDC2019Set.TRAIN);
			parameters.setNr(4);
			Map<List<String>, Long> traceCounts = new HashMap<List<String>, Long>();
			XLog log = algorithm.apply(context, parameters);
			for (XTrace trace : log) {
				List<String> activities = new ArrayList<String>();
				for (XEvent event : trace) {
					activities.add(XConceptExtension.instance().extractName(event));
				}
				long count = 1 + (traceCounts.containsKey(activities) ? traceCounts.get(activities) : 0);
				traceCounts.put(activities, count);
			}
			parameters.setSet(PDC2019Set.CAL2);
			log = algorithm.apply(context, parameters);
			XLog newLog = XFactoryRegistry.instance().currentDefault()
					.createLog((XAttributeMap) log.getAttributes().clone());
			for (XTrace trace : log) {
				XTrace newTrace = XFactoryRegistry.instance().currentDefault()
						.createTrace((XAttributeMap) trace.getAttributes().clone());
				List<String> activities = new ArrayList<String>();
				for (XEvent event : trace) {
					XEvent newEvent = XFactoryRegistry.instance().currentDefault()
							.createEvent((XAttributeMap) event.getAttributes().clone());
					newTrace.add(newEvent);
					activities.add(XConceptExtension.instance().extractName(event));
				}
				newTrace.getAttributes().put("count", XFactoryRegistry.instance().currentDefault()
						.createAttributeDiscrete("count", traceCounts.containsKey(activities) ? traceCounts.get(activities) : -1, null));
				for (List<String> key : traceCounts.keySet()) {
					if (key.size() >= activities.size()) {
						boolean isPrefix = true;
						for (int i = 0; i < activities.size(); i++) {
							if (!key.get(i).equals(activities.get(i))) {
								isPrefix = false;
							}
						}
						if (isPrefix) {
							newTrace.getAttributes().put("prefix", XFactoryRegistry.instance().currentDefault()
									.createAttributeLiteral("prefix", activities.toString(), null));
						}
					}
				}
				newLog.add(newTrace);
			}
			return newLog;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
