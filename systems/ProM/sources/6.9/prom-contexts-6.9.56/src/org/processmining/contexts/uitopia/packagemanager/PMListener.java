package org.processmining.contexts.uitopia.packagemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipException;

import javax.xml.parsers.ParserConfigurationException;

import org.deckfour.uitopia.ui.main.Overlayable;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.packages.UnknownPackageException;
import org.processmining.framework.packages.events.PackageManagerListener;
import org.processmining.framework.packages.impl.CancelledException;
import org.xml.sax.SAXException;

public class PMListener implements PackageManagerListener {

	private final Overlayable overlayable;

	public PMListener(Overlayable overlayable) {
		this.overlayable = overlayable;

	}

	private PMOverlay pmOverlay;

	public synchronized void exception(Throwable t) {
		if (t instanceof IOException) {
			exception("I/O Exception: " + t, true);
		} else if (t instanceof SAXException) {
			exception("SAX Exception: " + t, true);
		} else if (t instanceof ParserConfigurationException) {
			exception("Parser Configuration Exception: " + t, true);
		} else if (t instanceof UnknownPackageException) {
			exception("Unknown Package Exception: " + t, true);
		} else if (t instanceof MalformedURLException) {
			exception("Malformed URL Exception: " + t, true);
		} else if (t instanceof FileNotFoundException) {
			exception("File Not Found Exception: " + t, true);
		} else if (t instanceof CancelledException) {
			exception("Cancelled Exception: " + t, true);
		} else if (t instanceof SecurityException) {
			exception("Security Exception: " + t, true);
		} else if (t instanceof ZipException) {
			exception("ZIP Exception: " + t, true);
		} else {
			exception(t.getMessage());
		}
	}

	public synchronized void exception(String exception) {
		exception(exception, false);
	}

	private void exception(String exception, boolean hasPrefix) {
		pmOverlay.addText((hasPrefix ? "" : "Error: ") + exception);	
	}
	
	public synchronized void startDownload(String packageName, URL url, PackageDescriptor pack) {
		pmOverlay.setPackage(pack);
		pmOverlay.addText("Downloading: " + packageName);
	}

	public synchronized void startInstall(String packageName, File folder, PackageDescriptor pack) {
		pmOverlay.setPackage(pack);
		pmOverlay.addText("Installing: " + packageName);
	}

	public synchronized void sessionComplete(boolean error) {
		PackageManager.getInstance().setCanceller(null);
		pmOverlay.finishedInstall(error);
		pmOverlay.getResultBlocking();
	}

	public synchronized void sessionStart() {
		pmOverlay = new PMOverlay(overlayable);
		PackageManager.getInstance().setCanceller(pmOverlay);
		pmOverlay.addText("Started package manager session");
		overlayable.showOverlay(pmOverlay);

	}

	public synchronized void finishedInstall(String packageName, File folder, PackageDescriptor pack) {
		pmOverlay.addText("Succesfully installed: " + packageName);
	}
}
