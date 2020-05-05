package org.processmining.log.models.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.basicutils.models.impl.ObjectArrayImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.plugins.log.OpenLogFilePlugin;

import com.csvreader.CsvWriter;

public class EventLogArrayImpl extends ObjectArrayImpl<XLog> implements EventLogArray {

	@Deprecated 
	public int addLog(XLog log) {
		return addElement(log);
	}

	@Deprecated
	public int removeLog(XLog log) {
		return removeElement(log);
	}

	@Deprecated
	public void addLog(int index, XLog log) {
		addElement(log);
	}

	@Deprecated
	public void removeLog(int index) {
		removeElement(index);
	}

	@Deprecated
	public XLog getLog(int index) {
		return getElement(index);
	}

	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception {
		importFromStream(context, input, parent, new OpenLogFilePlugin());
	}

	public void exportToFile(PluginContext context, File file, XSerializer logSerializer) throws IOException {
		Writer fileWriter = new FileWriter(file);
		CsvWriter csvWriter = new CsvWriter(fileWriter, ',');
		int n = 1;
		for (XLog log: list) {
			String fileName = file.getName();
			File dir = file.getParentFile();
			String prefix = fileName.substring(0, fileName.indexOf("."));
			File netFile = File.createTempFile(prefix + "." + n + ".", "." + logSerializer.getSuffices()[0], dir);
			csvWriter.write(netFile.getName());
			csvWriter.endRecord();
			System.out.println("Exporting Accepting Petri Net to " + netFile.getName());
			FileOutputStream out = new FileOutputStream(netFile);
			logSerializer.serialize(log, out);
			out.close();
			n++;
		}
		csvWriter.close();
	}

	public void exportToFile(PluginContext context, File file) throws Exception {
		exportToFile(context, file, new XesXmlSerializer());
	}


}
