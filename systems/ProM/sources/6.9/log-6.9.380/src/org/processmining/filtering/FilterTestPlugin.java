package org.processmining.filtering;
//
//import org.deckfour.xes.extension.std.XConceptExtension;
//import org.deckfour.xes.model.XAttributeMap;
//import org.deckfour.xes.model.XEvent;
//import org.deckfour.xes.model.XLog;
//import org.deckfour.xes.model.XTrace;
//import org.processmining.contexts.uitopia.UIPluginContext;
//import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
//import org.processmining.filtering.filter.factories.FilterFactory;
//import org.processmining.filtering.filter.interfaces.Filter;
//import org.processmining.filtering.xfilter.factories.XFilterFactory;
//import org.processmining.filtering.xfilter.interfaces.XAttributableFilter;
//import org.processmining.filtering.xfilter.interfaces.XFilter;
//import org.processmining.filtering.xflog.implementations.XFLogImpl;
//import org.processmining.filtering.xflog.interfaces.XFLog;
//import org.processmining.framework.plugin.annotations.Plugin;
//import org.processmining.framework.plugin.annotations.PluginVariant;
//
//@Plugin(name = "Filter test", parameterLabels = { "Event log" }, returnLabels = { "Log" }, returnTypes = { XFLog.class })
//public class FilterTestPlugin {
//
//	@UITopiaVariant(affiliation = "Eindhoven University of Technology", author = "S.J. van Zelst", email = "s.j.v.zelst@tue.nl")
//	@PluginVariant(variantLabel = "Filter test", requiredParameterLabels = { 0 })
//	/**
//	 * ProM entry point for filter testing.
//	 *
//	 * @param context
//	 * @param log
//	 * @return
//	 */
//	public XFLog filter(final UIPluginContext context, final XLog log) {
//		XAttributableFilter<XEvent> eventFilter = XFilterFactory.containsKeyValuePairFilter(XConceptExtension.KEY_NAME, "e");
//		XFilter<XTrace> traceFilter = XFilterFactory.hideXEvent(eventFilter);
//		Filter<XAttributeMap> attributeMapFilter = FilterFactory.mirrorFilter();
//		
//		XFLog fLog = new XFLogImpl(log, traceFilter, attributeMapFilter);
//
//		return fLog;
//	}
//
//}
