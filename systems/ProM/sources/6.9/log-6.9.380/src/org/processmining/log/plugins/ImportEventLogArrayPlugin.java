package org.processmining.log.plugins;

import java.io.File;
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.impl.EventLogArrayFactory;

@Plugin(name = "Import Event Log Array from ELA file", parameterLabels = { "Filename" }, returnLabels = { "Event Log Array" }, returnTypes = { EventLogArray.class })
@UIImportPlugin(description = "ELA Event Log Array files", extensions = { "ela" })
public class ImportEventLogArrayPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("ELA files", "ela");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		EventLogArray logs = EventLogArrayFactory.createEventLogArray();
		File file = getFile();
		String parent = (file == null ? null : file.getParent());
		logs.importFromStream(context, input, parent);
		setLabel(context, logs, filename);
		return logs;
	}

	/*
	 * Sets a proper default name for the event log array.
	 */
	private void setLabel(PluginContext context, EventLogArray logs, String filename) {
		String prefix = null;
		String postfix = null;
		boolean allSame = true;
		for (int i = 0; i < logs.getSize(); i++) {
			XLog log = logs.getLog(i);
			String name = XConceptExtension.instance().extractName(log);
			if (name != null) {
				if (prefix == null) {
					prefix = name;
				} else {
					if (!name.equals(prefix)) {
						allSame = false;
						prefix = greatestCommonPrefix(prefix, name);
					}
				}
				if (postfix == null) {
					postfix = name;
				} else {
					if (!name.equals(postfix)) {
						allSame = false;
						postfix = new StringBuilder(greatestCommonPrefix(
								new StringBuilder(prefix).reverse().toString(), new StringBuilder(name).reverse()
										.toString())).reverse().toString();
					}
				}
			}
		}
		if ((prefix != null && prefix.length() > 0) || (postfix != null && postfix.length() > 0)) {
			StringBuffer buf = new StringBuffer();
			if (prefix != null) {
				buf.append(prefix);
			}
			if (!allSame) {
				buf.append(" ... ");
				if (postfix != null) {
					buf.append(postfix);
				}
			}
			context.getFutureResult(0).setLabel(buf.toString());
		} else {
			context.getFutureResult(0).setLabel("Event log array from file '" + filename + "'");
		}
	}

	private String greatestCommonPrefix(String a, String b) {
		int minLength = Math.min(a.length(), b.length());
		for (int i = 0; i < minLength; i++) {
			if (a.charAt(i) != b.charAt(i)) {
				return a.substring(0, i);
			}
		}
		return a.substring(0, minLength);
	}
}
