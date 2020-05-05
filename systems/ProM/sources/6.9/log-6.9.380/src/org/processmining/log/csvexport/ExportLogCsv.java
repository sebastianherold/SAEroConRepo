package org.processmining.log.csvexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Export Log to CSV File", level= PluginLevel.PeerReviewed, parameterLabels = { "Log", "File" }, returnLabels = {}, returnTypes = {}, userAccessible = true)
@UIExportPlugin(description = "CSV files", extension = "csv")
public final class ExportLogCsv {
	
//TODO: Export plug-in cannot show any Dialog :(
	
/*	private class DateFormatPanel extends BorderPanel {

		private static final long serialVersionUID = -6547392010448275699L;
		private final ProMTextField dateFormatTextField;

		public DateFormatPanel() {
			super(0, 0);
			dateFormatTextField = new ProMTextField("yyyy-MM-dd'T'HH:mm:ssZ");
			add(dateFormatTextField);
		}	
		
		public String getDateFormat() {
			return dateFormatTextField.getText().trim();		
		}

		public InteractionResult getUserChoice(UIPluginContext context) {
			return context.showConfiguration("Specify date format", this);
		}

	}*/
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt, M. de Leoni", email = "m.d.leoni@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export Log to CSV File")
	public void export(UIPluginContext context, XLog log, File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);

		long instanceNumber=1;

		//final XLog result = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		final XLifecycleExtension lfExt = XLifecycleExtension.instance();
		final XFactory factory=XFactoryRegistry.instance().currentDefault();
		final XConceptExtension cpExt=XConceptExtension.instance();
		final HashMap<String,List<Long>> map=new HashMap<String, List<Long>>();
		String activityName;
		
		for (XTrace trace : log) {
			map.clear();
			for (XEvent event : trace) {
				switch(lfExt.extractStandardTransition(event))
				{
					case START :
						activityName=cpExt.extractName(event);
						if (activityName!=null)
						{
							//event=factory.createEvent(e.getAttributes());
							if (cpExt.extractInstance(event)==null)
							{						
								List<Long> listInstances=map.get(activityName);
								if (listInstances==null)
								{
									listInstances=new LinkedList<Long>();
									map.put(activityName, listInstances);
								}
								cpExt.assignInstance(event, String.valueOf(instanceNumber));
								listInstances.add(instanceNumber++);
							}
						}
						break;					
					case COMPLETE :
						activityName=cpExt.extractName(event);
						if (activityName!=null)
						{
							event=factory.createEvent(event.getAttributes());							
							if (cpExt.extractInstance(event)==null)
							{
								List<Long> listInstances=map.get(activityName);
								if (listInstances==null || listInstances.isEmpty())									
									cpExt.assignInstance(event, String.valueOf(instanceNumber++));
								else
								{
									cpExt.assignInstance(event, String.valueOf(listInstances.remove(0)));
								}
							}
						}
						break;
					default :
						//event=null;
						break;
				}
				//copy.add(event);
			}
		}
		XSerializer logSerializer = new XesCsvSerializer("yyyy/MM/dd HH:mm:ss.SSS");
		logSerializer.serialize(log, out);
		out.close();	
	}
}
