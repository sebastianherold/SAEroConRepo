package org.processmining.log.algorithms;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.parameters.SplitLogParameters;

public class SplitLogAlgorithm {

	public XLog apply(PluginContext context, XLog log, SplitLogParameters parameters) {

		XFactory factory = XFactoryRegistry.instance().currentDefault();

		XLog splittedLog = factory.createLog(log.getAttributes());
		splittedLog.getExtensions().addAll(log.getExtensions());
		splittedLog.getGlobalTraceAttributes().addAll(log.getGlobalTraceAttributes());
		splittedLog.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
		splittedLog.getClassifiers().addAll(log.getClassifiers());

		for (XTrace trace : log) {
			Set<String> splitValues = new TreeSet<String>();
			for (XEvent event : trace) {
				if (event.getAttributes().containsKey(parameters.getKey())) {
					String value = event.getAttributes().get(parameters.getKey()).toString();
					if (value != null && !value.trim().isEmpty()) {
						splitValues.addAll(Arrays.asList(value.trim().split(" ")));
					}
				}
			}
			int ctr = 1;

			for (String splitValue : splitValues) {
				XTrace splittedTrace = factory.createTrace((XAttributeMap) trace.getAttributes().clone());
				XAttributeLiteral attr = factory.createAttributeLiteral("oldname",
						XConceptExtension.instance().extractName(trace), null);
				splittedTrace.getAttributes().put("oldname", attr);
				XConceptExtension.instance().assignName(splittedTrace,
						XConceptExtension.instance().extractName(trace) + "-" + ctr);
				ctr++;
				for (XEvent event : trace) {
					XAttribute attribute = event.getAttributes().get(parameters.getKey());
					String value = attribute == null ? null : attribute.toString();
					if (value == null || value.trim().isEmpty()
							|| Arrays.asList(value.trim().split(" ")).contains(splitValue)) {
						XEvent splittedEvent = factory.createEvent((XAttributeMap) event.getAttributes().clone());
						attr = factory.createAttributeLiteral(parameters.getKey(), splitValue, null);
						splittedEvent.getAttributes().put(parameters.getKey(), attr);
						splittedTrace.add(splittedEvent);
					}
				}
				splittedLog.add(splittedTrace);
			}
		}
		return splittedLog;
	}

	/*
	 * private CSVReader reader;
	 * 
	 * public ArrayList<ArrayList<String>> read(String file, final int index) {
	 * try {
	 * 
	 * reader = new CSVReader(new FileReader(file));
	 * 
	 * String[] nextLine; ArrayList<String> bucket = new ArrayList<String>();
	 * ArrayList<ArrayList<String>> container = new
	 * ArrayList<ArrayList<String>>(); ArrayList<ArrayList<ArrayList<String>>>
	 * super_container = new ArrayList<ArrayList<ArrayList<String>>>(); int
	 * counter = 0; String CaseID = null; while ((nextLine = reader.readNext())
	 * != null) { if (counter == 0) { CaseID = nextLine[0]; } String[] splitted
	 * = nextLine[index].trim().split(" "); for (int i = 0; i < splitted.length;
	 * i++) { bucket = new ArrayList<String>(); bucket.add(nextLine[0]); if
	 * (!CaseID.equals(nextLine[0])) { super_container.add(container); container
	 * = new ArrayList<ArrayList<String>>(); counter = 0; } else { counter = 1;
	 * } for (int j = 0; j < index; j++) { bucket.add(nextLine[j]); }
	 * bucket.add(splitted[i]); if (index < nextLine.length - 1) { for (int j =
	 * index + 1; j < nextLine.length; j++) { bucket.add(nextLine[j]); } }
	 * container.add(bucket); }
	 * 
	 * } super_container.add(container); for (int i = 0; i <
	 * super_container.size(); i++) { Collections.sort(super_container.get(i),
	 * new Comparator<ArrayList<String>>() {
	 * 
	 * @Override public int compare(ArrayList<String> a, ArrayList<String> b) {
	 * // TODO Auto-generated method stub return a.get(index +
	 * 1).compareTo(b.get(index + 1)); } }); }
	 * 
	 * container = new ArrayList<ArrayList<String>>(); for (int i = 0; i <
	 * super_container.size(); i++) { int counter2 = 1; for (int j = 0; j <
	 * super_container.get(i).size(); j++) {
	 * 
	 * if (j + 1 < super_container.get(i).size() &&
	 * super_container.get(i).get(j).get(index + 1)
	 * .equals(super_container.get(i).get(j + 1).get(index + 1))) {
	 * super_container.get(i).get(j).set(0, super_container.get(i).get(j).get(0)
	 * + "-" + counter2); } else { if (j - 1 >= 0 &&
	 * super_container.get(i).get(j).get(index + 1)
	 * .equals(super_container.get(i).get(j - 1).get(index + 1))) {
	 * super_container.get(i).get(j).set(0, super_container.get(i).get(j).get(0)
	 * + "-" + counter2); counter2++; } else {
	 * super_container.get(i).get(j).set(0, super_container.get(i).get(j).get(0)
	 * + "-" + counter2); } }
	 * 
	 * }
	 * 
	 * } for (int i = 0; i < super_container.size(); i++) { for (int j = 0; j <
	 * super_container.get(i).size(); j++) {
	 * container.add(super_container.get(i).get(j)); } } return container;
	 * 
	 * } catch (FileNotFoundException e) {
	 * 
	 * } catch (IOException e) {
	 * 
	 * } return null; }
	 * 
	 * public void write(ArrayList<ArrayList<String>> sorted) { try { CSVWriter
	 * writer = new CSVWriter(new FileWriter("./SplitFile.csv"), ',',
	 * CSVWriter.NO_QUOTE_CHARACTER);
	 * 
	 * for (int i = 0; i < sorted.size(); i++) { String[] entries = new
	 * String[sorted.get(i).size()]; for (int j = 0; j < sorted.get(i).size();
	 * j++) { entries[j] = sorted.get(i).get(j); } writer.writeNext(entries);
	 * 
	 * } System.out.println("Written to the File Successfully"); writer.close();
	 * } catch (IOException e) { System.out.println("File Name is Already There"
	 * ); } }
	 */
}
