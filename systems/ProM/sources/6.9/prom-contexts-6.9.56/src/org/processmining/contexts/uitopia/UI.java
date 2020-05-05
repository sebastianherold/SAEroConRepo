package org.processmining.contexts.uitopia;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.deckfour.uitopia.ui.UITopiaController;
import org.deckfour.uitopia.ui.main.Overlayable;
import org.deckfour.uitopia.ui.overlay.TwoButtonOverlayDialog;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.contexts.uitopia.packagemanager.PMController;
import org.processmining.contexts.uitopia.packagemanager.PMFrame;
import org.processmining.contexts.uitopia.packagemanager.PMMainView;
import org.processmining.contexts.uitopia.packagemanager.PMPackage;
import org.processmining.contexts.uitopia.packagemanager.PMPackage.PMStatus;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.packages.events.PackageManagerListener;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;

public class UI {

	@Plugin(name = "UITopia", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) {
		UIContext globalContext;
		globalContext = new UIContext();
		globalContext.initialize();
		final UITopiaController controller = new UITopiaController(globalContext);
		globalContext.setController(controller);
		globalContext.setFrame(controller.getFrame());
		controller.getFrame().setIconImage(ImageLoader.load("prom_icon_32x32.png"));
		controller.getFrame().setVisible(true);
		controller.getMainView().showWorkspaceView();
		controller.getMainView().getWorkspaceView().showFavorites();

		globalContext.startup();

		for (String cmd : commandlineArguments) {
			File f = new File(cmd);
			if (f.exists() && f.isFile()) {
				globalContext.getResourceManager().importResource(null, f);
			}
		}

		return controller;
	}

	public static void main(String[] args) throws Exception {

		if (Boot.AUTO_UPDATE.equals("auto") || Boot.AUTO_UPDATE.equals("user") || !Boot.isLatestReleaseInstalled()) {
			Boot.setReleaseInstalled("", "");
			PMFrame frame = (PMFrame) Boot.boot(PMFrame.class);
			frame.setIconImage(ImageLoader.load("prom_icon_32x32.png"));
			// Now select the release package
			PMPackage releasePackage = frame.getController().selectPackage(Boot.RELEASE_PACKAGE);
			if (releasePackage == null) {
				Boot.boot(UI.class, UIPluginContext.class, args);
				throw new Exception("Cannot find release package defined in ProM.ini file: " + Boot.RELEASE_PACKAGE
						+ ". Continuing to load ProM.");
			}

			if (releasePackage.getStatus() == PMStatus.TOUNINSTALL) {

				int option = JOptionPane.NO_OPTION;

				if (Boot.AUTO_UPDATE.equals("auto") || Boot.AUTO_UPDATE.equals("user")) {
					/*
					 * HV: Check for packages to install or update.
					 */
					PMController pmController = frame.getController();
					if (Boot.AUTO_UPDATE.equals("user")) {
						if (!pmController.getToInstallPackages().isEmpty()) {
							if (!pmController.getToUpdatePackages().isEmpty()) {
								option = JOptionPane.showConfirmDialog(frame,
										"New packages and package updates are available.\nDo you want ProM to install and/or update them now?",
										"Install and update packages?", JOptionPane.YES_NO_OPTION);
							} else {
								option = JOptionPane.showConfirmDialog(frame,
										"New packages are available.\nDo you want ProM to install them now?",
										"Install packages?", JOptionPane.YES_NO_OPTION);
							}
						} else if (!pmController.getToUpdatePackages().isEmpty()) {
							option = JOptionPane.showConfirmDialog(frame,
									"Package updates are available.\nDo you want ProM to update them now?",
									"Update packages?", JOptionPane.YES_NO_OPTION);
						}
					} else { // auto
						option = JOptionPane.YES_OPTION;
					}
					if (option == JOptionPane.YES_OPTION) {
						// Start listening
						UIPackageManagerListener listener = new UIPackageManagerListener(frame, args);
						PackageManager.getInstance().addListener(listener);

						// Show the package manager
						frame.setVisible(true);
						Collection<PMPackage> toUpdate = new ArrayList<PMPackage>();
						toUpdate.addAll(pmController.getToInstallPackages());
						toUpdate.addAll(pmController.getToUpdatePackages());

						frame.getController().update(toUpdate, frame.getController().getMainView().getWorkspaceView());

						// ProM will be started as soon as the package manager finishes.

						synchronized (listener) {
							while (!listener.isDone()) {
								listener.wait();
							}
						}

					}
				}

				if (option == JOptionPane.NO_OPTION) {
					// Package is upToDate and installed.
					// Do not show package manager and start ProM
					Boot.setLatestReleaseInstalled();
					Boot.boot(UI.class, UIPluginContext.class, args);
				}

			} else {

				// Start listening
				UIPackageManagerListener listener = new UIPackageManagerListener(frame, args);
				PackageManager.getInstance().addListener(listener);

				// Show the package manager
				frame.setVisible(true);

				// And install the release package!
				frame.getController().update(releasePackage, frame.getController().getMainView().getWorkspaceView());

				// ProM will be started as soon as the package manager finishes.

				synchronized (listener) {
					while (!listener.isDone()) {
						listener.wait();
					}
				}

			}

			//Boot.setLatestReleaseInstalled();
		} else {
			Boot.boot(UI.class, UIPluginContext.class, args);
		}

	}
}

class FirstTimeOverlay extends TwoButtonOverlayDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 494237962617678531L;

	public FirstTimeOverlay(Overlayable mainView) {
		super(mainView, "Starting ProM", "Cancel", "  OK  ", //
				new JLabel("<html>All packages have been installed and/or updated.<BR>"
						+ "Please wait while starting ProM.<BR><BR>"
						+ "If this is the first time you run ProM on this computer, please be patient.</html>"));

		getCancelButton().setEnabled(true);
		getOKButton().setEnabled(false);

	}

	@Override
	public void close(boolean okayed) {
		if (!okayed) {
			System.exit(0);
		}
		super.close(okayed);
	}

}

class UIPackageManagerListener implements PackageManagerListener {

	private final String[] args;
	private final PMFrame frame;
	private boolean done = false;

	public UIPackageManagerListener(PMFrame frame, String[] args) {
		this.frame = frame;
		this.args = args;
	}

	public void exception(Throwable t) {
	}

	public void exception(String exception) {
	}

	public void finishedInstall(String packageName, File folder, PackageDescriptor pack) {
	}

	public void sessionComplete(boolean error) {
		synchronized (this) {
			done = true;
			this.notifyAll();
		}
		PackageManager.getInstance().removeListener(this);
		showOverlayAfterInstall();
	}

	public void sessionStart() {
	}

	public void startDownload(String packageName, URL url, PackageDescriptor pack) {
	}

	public void startInstall(String packageName, File folder, PackageDescriptor pack) {
	}

	public boolean isDone() {
		return done;
	}

	private void showOverlayAfterInstall() {
		PMMainView overlayable = frame.getController().getMainView();

		FirstTimeOverlay dialog = new FirstTimeOverlay(overlayable);

		overlayable.showOverlay(dialog);
		frame.saveConfig();

		try {
			Boot.boot(UI.class, UIPluginContext.class, args);
			Boot.setLatestReleaseInstalled();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		} finally {
			frame.setVisible(false);
		}

	}

}