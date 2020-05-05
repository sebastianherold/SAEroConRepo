package org.processmining.log.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.processmining.framework.annotations.AuthoredType;
import org.processmining.framework.annotations.Icon;
import org.processmining.framework.plugin.PluginContext;

@AuthoredType(typeName = "Event log array", affiliation = AuthoredType.TUE, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
@Icon(icon = "resourcetype_ela_30x35.png")
public interface EventLogArray {

	/**
	 * Initializes the event log array.
	 */
	void init();

	/**
	 * Adds the given log to the array.
	 * 
	 * @param log
	 *            The given log.
	 * @return The index of the added log in the array.
	 */
	int addLog(XLog log);

	/**
	 * Removes the first occurrence of the given log from the array,
	 * 
	 * @param log
	 *            The given log.
	 * @return The index of the removed log, if present. -1 if not present.
	 */
	int removeLog(XLog log);

	/**
	 * Adds the given log at the given index to the array.
	 * 
	 * @param index
	 *            The given index.
	 * @param log
	 *            The given log.
	 */
	void addLog(int index, XLog log);

	/**
	 * Removes the log from the given index.
	 * 
	 * @param index
	 *            The given index.
	 */
	void removeLog(int index);

	/**
	 * Returns the log at the given index.
	 * 
	 * @param index The given index.
	 * @return The log at the given index, if valid. null if not valid.
	 */
	XLog getLog(int index);
	
	int getSize();
	
	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception;
	public void exportToFile(PluginContext context, File file, XSerializer logSerializer) throws IOException;
}
