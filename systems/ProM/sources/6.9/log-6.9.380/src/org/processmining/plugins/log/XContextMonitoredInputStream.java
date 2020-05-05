package org.processmining.plugins.log;

import java.io.InputStream;

import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.progress.XMonitoredInputStream;
import org.processmining.framework.util.progress.XProgressListener;

public class XContextMonitoredInputStream extends XMonitoredInputStream {

	public XContextMonitoredInputStream(InputStream input, long fileSizeInBytes, final Progress progress) {
		super(input, fileSizeInBytes, new XProgressListener() {

			public void updateProgress(int p, int max) {
				if (progress.isIndeterminate() && (max > 0)) {
					progress.setMinimum(0);
					progress.setMaximum(max);
					progress.setIndeterminate(false);
				} else if (progress.getMaximum() != max) {
					progress.setMaximum(max);
				}
				progress.setValue(p);
			}

			public boolean isAborted() {
				return progress.isCancelled();
			}

		});
	}

}
