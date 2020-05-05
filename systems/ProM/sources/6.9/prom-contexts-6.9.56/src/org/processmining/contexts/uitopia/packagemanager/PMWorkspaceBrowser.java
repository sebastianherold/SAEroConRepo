package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.framework.boot.Boot;

import com.fluxicon.slickerbox.components.IconVerticalTabbedPane;

public class PMWorkspaceBrowser extends JPanel {

	private static final long serialVersionUID = -1467890361679445697L;
	private static final Color BG = new Color(180, 180, 180);
	private static final Color FG = new Color(60, 60, 60);

	private IconVerticalTabbedPane tabs;
	private final PMController controller;

	private PMPackageListBrowser browserToUninstall;
	private PMPackageListBrowser browserToUpdate;
	private PMPackageListBrowser browserToInstall;
	private PMPackageListBrowser browserSelection;

	private PMPackage selectionPack;
	private boolean selectionParent;

	private final static String TOUNINSTALL = "Up to date";
	private final static String TOPUDATE = "Out of date";
	private final static String TOINSTALL = "Not installed";
	private final static String SELECTION = "Selection";

	public PMWorkspaceBrowser(PMController controller) {
		this.controller = controller;
		setLayout(new BorderLayout());
		setOpaque(false);
		setupUI();
		selectionPack = null;
		selectionParent = true;
		//		PackageManager.getInstance().addListener(
		//				new UpdateListener() {
		//					public void updated() {
		//						updatePackages();
		//					}
		//				});
	}

	public PMPackageListBrowser getSelectedBrowser() {
		return (PMPackageListBrowser) tabs.getSelected();
	}

	public void showPackage(PMPackage pack) {
		if (pack.getStatus() == PMPackage.PMStatus.TOUNINSTALL) {
			tabs.selectTab(TOUNINSTALL);
			browserToUninstall.showPackage(pack);
		} else if (pack.getStatus() == PMPackage.PMStatus.TOUPDATE) {
			tabs.selectTab(TOPUDATE);
			browserToUpdate.showPackage(pack);
		} else {
			tabs.selectTab(TOINSTALL);
			browserToInstall.showPackage(pack);
		}
	}

	public void updatePackages() {
		if (selectionPack != null) {
			browserSelection.setSelectionContent(selectionPack, selectionParent);
		}
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				JComponent tab = tabs.getSelected();
				if ((tab != null) && (tab instanceof PMPackageListBrowser)) {
					((PMPackageListBrowser) tab).updateData();
				}
			}
		});
	}

	public void showParentsOf(PMPackage pack) {
		selectionPack = pack;
		selectionParent = true;
		browserSelection.setSelectionContent(selectionPack, selectionParent);
		tabs.selectTab(SELECTION);
	}

	public void showChildrenOf(PMPackage pack) {
		selectionPack = pack;
		selectionParent = false;
		browserSelection.setSelectionContent(selectionPack, selectionParent);
		tabs.selectTab(SELECTION);
	}

	private void setupUI() {
		this.add(new PMSearchView(this, controller), BorderLayout.NORTH);

		browserToUninstall = new PMPackageListBrowser(controller, PMPackageListBrowser.Type.TOUNINSTALL);
		browserToUpdate = new PMPackageListBrowser(controller, PMPackageListBrowser.Type.TOUPDATE);
		browserToInstall = new PMPackageListBrowser(controller, PMPackageListBrowser.Type.TOINSTALL);
		browserSelection = new PMPackageListBrowser(controller, PMPackageListBrowser.Type.SELECTION);

		tabs = new IconVerticalTabbedPane(FG, BG, 100);
		tabs.setPassiveBackground(new Color(140, 140, 140));
		tabs.setMouseOverFadeColor(new Color(90, 90, 90));

		tabs.addTab(TOUNINSTALL, ImageLoader.load("uptodate_60x60_black.png"), browserToUninstall,
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						browserToUninstall.updateData();
					}
				});
		tabs.addTab(TOPUDATE, ImageLoader.load("outofdate_60x60_black.png"), browserToUpdate, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browserToUpdate.updateData();
			}
		});
		tabs.addTab(TOINSTALL, ImageLoader.load("notinstalled_60x60_black.png"), browserToInstall,
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						browserToInstall.updateData();
					}
				});
		tabs.addTab(SELECTION, ImageLoader.load("selection_60x60_black.png"), browserSelection, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browserSelection.updateData();
			}
		});

		/*
		 * Pre-select the tab that is most likely of use to the user
		 */
		PMPackageListBrowser currentBrowser = browserToUninstall;
		if (currentBrowser.isEmpty()) {
			currentBrowser = browserToUpdate;
			if (currentBrowser.isEmpty()) {
				currentBrowser = browserToInstall;
				if (currentBrowser.isEmpty()) {
					/*
					 * No packages at all. User may have no internet conenction.
					 */
					JOptionPane.showMessageDialog(controller.getMainView(),
							"No packages were found. Please check your internet connection.");
				}
			}
		}
		/*
		 * And pre-select the most important package.
		 */
		currentBrowser.selectPackage(Boot.RELEASE_PACKAGE);
		tabs.selectTab(currentBrowser);

		this.add(tabs, BorderLayout.CENTER);
		this.add(new PMMemoryView(controller), BorderLayout.SOUTH);
	}

	public void update() {
		browserToUninstall.updateData();
		browserToUpdate.updateData();
		browserToInstall.updateData();
		browserSelection.updateData();
	}
}
