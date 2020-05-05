package org.processmining.log.algorithms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.parameters.MergeLogsParameters;

public class MergeLogsAlgorithm {

	private final String STOPWORDS = "a about above above across after afterwards again against "
			+ "all almost alone along already also although always am among amongst amoungst "
			+ "amount an and another any anyhow anyone anything anyway anywhere are around as at "
			+ "back be became because become becomes becoming been before beforehand behind being "
			+ "below beside besides between beyond bill both bottom but by call can cannot cant co "
			+ "con could couldnt cry de describe detail do done down due during each eg eight either "
			+ "eleven else elsewhere empty enough etc even ever every everyone everything everywhere "
			+ "except few fifteen fify fill find fire first five for former formerly forty found four "
			+ "from front full further get give go had has hasnt have he hence her here hereafter hereby "
			+ "herein hereupon hers herself him himself his how however hundred ie if in inc indeed "
			+ "interest into is it its itself keep last latter latterly least less ltd made many may me "
			+ "meanwhile might mill mine more moreover most mostly move much must my myself name namely "
			+ "neither never nevertheless next nine no nobody none noone nor not nothing now nowhere of off "
			+ "often on once one only onto or other others otherwise our ours ourselves out over own part "
			+ "per perhaps please put rather re same see seem seemed seeming seems serious several she should "
			+ "show side since sincere six sixty so some somehow someone something sometime sometimes "
			+ "somewhere still such system take ten than that the their them themselves then thence there "
			+ "thereafter thereby therefore therein thereupon these they thickv thin third this those though "
			+ "three through throughout thru thus to together too top toward towards twelve twenty two un "
			+ "under until up upon us very via was we well were what whatever when whence whenever where "
			+ "whereafter whereas whereby wherein whereupon wherever whether which while whither who whoever "
			+ "whole whom whose why will with within without would yet you your yours yourself yourselves the";

	public XLog apply(PluginContext context, XLog mainLog, XLog subLog, MergeLogsParameters parameters) {
		XLog log = XFactoryRegistry.instance().currentDefault().createLog();
		DateFormat df = new SimpleDateFormat(parameters.getDateFormat());

		long time = -System.currentTimeMillis();

		for (XTrace mainTrace : mainLog) {
			boolean doApply = true;
			if (doApply && parameters.getTraceId() != null) {
				/*
				 * User has selected specific main trace. Filter in only the
				 * trace that has that id as concept:name.
				 */
				String id = XConceptExtension.instance().extractName(mainTrace);
				doApply = (id != null && id.equals(parameters.getTraceId()));
			}
			if (doApply && parameters.getFromDate() != null && parameters.getToDate() != null) {
				/*
				 * User has selected from date and to date. Filter in only those
				 * traces that occur entirely in that interval.
				 */
				doApply = isBetween(mainTrace, parameters.getFromDate(), parameters.getToDate());
			}
			if (doApply && parameters.getSpecificDate() != null) {
				/*
				 * User has selected a specific date. Only filter in those
				 * traces that have this exact date.
				 */
				doApply = false;
				for (XEvent event : mainTrace) {
					Date date = XTimeExtension.instance().extractTimestamp(event);
					if (date.equals(parameters.getSpecificDate())) {
						doApply = true;
						continue;
					}
				}
			}
			if (doApply && parameters.getRequiredWords() != null) {
				/*
				 * User has selected required words. Filter in those traces that
				 * match one of these words.
				 */
				doApply = false;
				Collection<String> required = new HashSet<String>(
						Arrays.asList(parameters.getRequiredWords().split(",")));
				for (XEvent event : mainTrace) {
					for (XAttribute attribute : event.getAttributes().values()) {
						if (attribute instanceof XAttributeLiteral) {
							String value = ((XAttributeLiteral) attribute).getValue();
							if (required.contains(value)) {
								doApply = true;
								continue;
							}
						} else if (attribute instanceof XAttributeDiscrete) {
							long value = ((XAttributeDiscrete) attribute).getValue();
							if (required.contains(String.valueOf(value))) {
								doApply = true;
								continue;
							}
						} else if (attribute instanceof XAttributeContinuous) {
							double value = ((XAttributeContinuous) attribute).getValue();
							if (required.contains(String.valueOf(value))) {
								doApply = true;
								continue;
							}
						} else if (attribute instanceof XAttributeTimestamp) {
							Date value = ((XAttributeTimestamp) attribute).getValue();
							if (required.contains(df.format(value))) {
								doApply = true;
								continue;
							}
						}
					}
					if (doApply) {
						continue;
					}
				}
			}
			if (doApply && parameters.getForbiddenWords() != null) {
				/*
				 * User has selected forbidden words. Filter out those traces
				 * that match one of these words.
				 */
				doApply = true;
				Collection<String> forbidden = new HashSet<String>(
						Arrays.asList(parameters.getForbiddenWords().split(",")));
				for (XEvent event : mainTrace) {
					for (XAttribute attribute : event.getAttributes().values()) {
						if (attribute instanceof XAttributeLiteral) {
							String value = ((XAttributeLiteral) attribute).getValue();
							if (forbidden.contains(value)) {
								doApply = false;
								continue;
							}
						} else if (attribute instanceof XAttributeDiscrete) {
							long value = ((XAttributeDiscrete) attribute).getValue();
							if (forbidden.contains(String.valueOf(value))) {
								doApply = false;
								continue;
							}
						} else if (attribute instanceof XAttributeContinuous) {
							double value = ((XAttributeContinuous) attribute).getValue();
							if (forbidden.contains(String.valueOf(value))) {
								doApply = false;
								continue;
							}
						} else if (attribute instanceof XAttributeTimestamp) {
							Date value = ((XAttributeTimestamp) attribute).getValue();
							if (forbidden.contains(df.format(value))) {
								doApply = false;
								continue;
							}
						}
					}
					if (!doApply) {
						continue;
					}
				}
			}
			if (doApply) {
				/*
				 * Main trace has passed all filters. Add it with all
				 * corresponding sub traces to the resulting log.
				 */
				apply(context, mainTrace, mainLog, subLog, log, parameters);
			}
		}

		time += System.currentTimeMillis();
		context.log("Merging time :" + convet_MS(time));
		return log;
	}

	private void apply(PluginContext context, XTrace mainTrace, XLog mainLog, XLog subLog, XLog log,
			MergeLogsParameters parameters) {
		for (XTrace subTrace : subLog) {
			if (isBetween(mainTrace, subTrace)) {
				boolean doApply = true;
				if (doApply && (checkMatch(mainTrace, subTrace) < parameters.getRelated())) {
					doApply = false;
				}
				if (doApply && (checkWordMatch(mainTrace, subTrace) < parameters.getMinMatches())) {
					doApply = false;
				}
				if (doApply) {
					XTrace trace = XFactoryRegistry.instance().currentDefault().createTrace(mainTrace.getAttributes());
					int mainCtr = 0;
					int subCtr = 0;
					while (mainCtr < mainTrace.size() && subCtr < subTrace.size()) {
						Date mainDate = XTimeExtension.instance().extractTimestamp(mainTrace.get(mainCtr));
						if (mainDate == null) {
							trace.add(mainTrace.get(mainCtr));
							mainCtr++;
						} else {
							Date subDate = XTimeExtension.instance().extractTimestamp(subTrace.get(subCtr));
							if (subDate == null) {
								trace.add(subTrace.get(subCtr));
								subCtr++;
							} else if (subDate.before(mainDate)) {
								trace.add(subTrace.get(subCtr));
								subCtr++;
							} else {
								trace.add(mainTrace.get(mainCtr));
								mainCtr++;
							}
						}
					}
					log.add(trace);
				}
			}
		}
	}

	private XTrace previousMainTrace = null;
	private Collection<String> mainWords = null;
	private Collection<String> stopWords = null;

	private int checkWordMatch(XTrace mainTrace, XTrace subTrace) {
		/*
		 * Build the stop words.
		 */
		if (stopWords == null) {
			stopWords = new HashSet<String>();
			stopWords.addAll(Arrays.asList(STOPWORDS.split(" ")));
		}
		/*
		 * Build (or reuse) the main words.
		 */
		if (mainTrace != previousMainTrace) {
			mainWords = new HashSet<String>();
			for (XEvent event : mainTrace) {
				for (XAttribute attribute : event.getAttributes().values()) {
					/*
					 * Exclude date/time from comparision.
					 */
					if (!(attribute instanceof XAttributeTimestamp)) {
						mainWords.addAll(Arrays.asList(attribute.toString().split(" ")));
					}
				}
			}
			mainWords.removeAll(stopWords);
			previousMainTrace = mainTrace;			
		}
		/*
		 * Build the sub words.
		 */
		Collection<String> subWords = new HashSet<String>();
		for (XEvent event : subTrace) {
			for (XAttribute attribute : event.getAttributes().values()) {
				/*
				 * Exclude date/time from comparision.
				 */
				if (!(attribute instanceof XAttributeTimestamp)) {
					subWords.addAll(Arrays.asList(attribute.toString().split(" ")));
				}
			}
		}
		subWords.removeAll(stopWords);
		/*
		 * Remove all words not in the main words.
		 */
		subWords.retainAll(mainWords);
		/*
		 * Return number of matching words.
		 */
		return subWords.size();
	}

	private int checkMatch(XTrace mainTrace, XTrace subTrace) {
		int match = 0;
		for (XEvent mainEvent : mainTrace) {
			for (XEvent subEvent : mainTrace) {
				match += checkMatch(mainEvent, subEvent);
			}
		}
		return match;
	}

	private int checkMatch(XEvent mainEvent, XEvent subEvent) {
		int match = 0;
		for (XAttribute mainAttribute : mainEvent.getAttributes().values()) {
			if (!(mainAttribute instanceof XAttributeTimestamp)) {
				for (XAttribute subAttribute : subEvent.getAttributes().values()) {
					if (mainAttribute.equals(subAttribute)) {
						match++;
					}
				}
			}
		}
		return match;
	}

	private boolean isBetween(XTrace trace, Date firstDate, Date lastDate) {
		Date firstTraceDate = getFirstDate(trace);
		if (firstTraceDate == null) {
			return false;
		}
		/*
		 * Update on June 6, 2016: the subtrace should start after the main trace
		 * has started, but not after it has started. 
		 * There is no requirement on when the subtrace ends.
		 */
		return (firstTraceDate.after(firstDate) && !firstTraceDate.after(lastDate));
	}

	private boolean isBetween(XTrace mainTrace, XTrace subTrace) {
		Date firstTraceDate = getFirstDate(mainTrace);
		Date lastTraceDate = getLastDate(mainTrace);
		if (firstTraceDate == null || lastTraceDate == null) {
			return false;
		}
		return isBetween(subTrace, firstTraceDate, lastTraceDate);
	}

	private Date getFirstDate(XTrace trace) {
		Date firstDate = null;
		for (XEvent event : trace) {
			Date date = XTimeExtension.instance().extractTimestamp(event);
			if (firstDate == null) {
				firstDate = date;
			} else if (date.before(firstDate)) {
				firstDate = date;
			}
		}
		return firstDate;
	}

	private Date getLastDate(XTrace trace) {
		Date lastDate = null;
		for (XEvent event : trace) {
			Date date = XTimeExtension.instance().extractTimestamp(event);
			if (lastDate == null) {
				lastDate = date;
			} else if (date.after(lastDate)) {
				lastDate = date;
			}
		}
		return lastDate;
	}

	private String convet_MS(long millis) {

		return String.format("%d min, %d sec %d ms", TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
				millis - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis)));
	}

}
