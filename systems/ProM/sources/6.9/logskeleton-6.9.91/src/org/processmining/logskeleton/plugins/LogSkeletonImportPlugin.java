package org.processmining.logskeleton.plugins;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.logskeleton.models.LogSkeleton;

import com.csvreader.CsvReader;

@Plugin(name = "Import Log Skeleton", parameterLabels = { "Filename" }, returnLabels = { "Log Skeleton" }, returnTypes = { LogSkeleton.class })
@UIImportPlugin(description = "Log Skeleton files", extensions = { "lsk" })
public class LogSkeletonImportPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("Log Skeleton files", "lsk");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		Reader streamReader = new InputStreamReader(input);
		CsvReader csvReader = new CsvReader(streamReader);
		LogSkeleton logSkeleton = new LogSkeleton();
		logSkeleton.importFromStream(csvReader);
		return logSkeleton;
	}
	
}
