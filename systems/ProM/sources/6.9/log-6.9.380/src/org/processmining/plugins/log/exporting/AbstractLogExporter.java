package org.processmining.plugins.log.exporting;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.utils.XUtils;

abstract class AbstractLogExporter {

	/**
	 * Temporarily changes the name of the XLog object, then calls the
	 * {@link #doExport(XLog, File)} method and, afterwards, changes the name of
	 * the XLog object back to the original one.
	 * 
	 * @param context
	 * @param log
	 * @param file
	 * @throws IOException
	 */
	protected void exportWithNameFromContext(PluginContext context, XLog log, File file) throws IOException {
		if (context == null) {
			doExport(log, file);
		} else {
			String originalName = XUtils.renameLogWithProMLabel(context, log);
			try {
				doExport(log, file);
			} finally {
				// Re-assign the original name to avoid changing the XLog object as ProMs connection framework might depend on it
				XUtils.assignConceptName(log, originalName);
			}
		}
	}

	/**
	 * Do the actual export
	 * 
	 * @param log
	 * @param file
	 * @throws IOException
	 */
	abstract void doExport(XLog log, File file) throws IOException;

}