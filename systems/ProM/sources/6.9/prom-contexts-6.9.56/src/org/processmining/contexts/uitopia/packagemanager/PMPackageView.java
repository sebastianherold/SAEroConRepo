package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.uitopia.ui.components.ImageLozengeButton;
import org.deckfour.uitopia.ui.util.ArrangementHelper;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.contexts.uitopia.packagemanager.PMPackage.PMStatus;

import com.fluxicon.slickerbox.components.RoundedPanel;

public class PMPackageView extends RoundedPanel {

	private static final long serialVersionUID = 8110954844773778705L;

	private final Collection<PMPackage> packs;
	private final PMPackage pack;
	private final PMController controller;

	//	private AbstractButton actionButton;
	private AbstractButton installButton;
	private AbstractButton updateButton;
	private AbstractButton removeButton;
	private AbstractButton parentButton;
	private AbstractButton childrenButton;

	public PMPackageView(PMPackage pack, PMController controller) {
		super(20, 5, 15);
		this.packs = new HashSet<PMPackage>();
		this.packs.add(pack);
		this.pack = pack;
		this.controller = controller;
		setupUI();
	}

	public PMPackageView(Collection<PMPackage> packs, PMController controller) {
		super(20, 5, 15);
		this.packs = packs;
		this.pack = (packs.isEmpty() ? null : packs.iterator().next());
		this.controller = controller;
		setupUI();
	}

	private void setupUI() {
		setBackground(new Color(160, 160, 160));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		if (packs.size() == 1) {
			// assemble info panel
			JPanel infoPanel = new JPanel();
			infoPanel.setMaximumSize(new Dimension(500, 180));
			infoPanel.setOpaque(false);
			infoPanel.setLayout(new BorderLayout());
			//			Image icon = pack.getPreview(150, 150);
			JLabel preview = null;
			//			if (icon != null) {
			ImageIcon icon = PMIconCache.getIconPreview(pack);
			if (icon != null) {
				preview = new JLabel(icon);
				//				preview.setSize(150, 150);
				preview.setOpaque(false);
			}
			JPanel detailsPanel = new JPanel();
			detailsPanel.setOpaque(false);
			detailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 0));
			detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
			detailsPanel.add(styleLabel(pack.getPackageName(), new Color(10, 10, 10), 18));
			detailsPanel.add(Box.createVerticalStrut(3));
			detailsPanel.add(styleLabel(pack.getAuthorName(), new Color(30, 30, 30), 14));
			detailsPanel.add(Box.createVerticalStrut(12));
			detailsPanel.add(styleLabel(pack.getVersion(), new Color(60, 60, 60), 12));
			detailsPanel.add(Box.createVerticalStrut(5));
			detailsPanel.add(styleLinkedLabel("<html><i>License: " + pack.getLicense() + "</i></html>",
					getLink2License(pack.getLicense()), new Color(60, 60, 60), 12));
			detailsPanel.add(Box.createVerticalStrut(5));
			String text = "<html><i>";
			if (pack.getDescription() == null) {
				text += "No description";
			} else {
				text += "Description: " + pack.getDescription();
			}
			text += "</i></html>";
			detailsPanel.add(styleLabel(text, new Color(60, 60, 60), 12));
			detailsPanel.add(Box.createVerticalGlue());
			if (preview != null) {
				infoPanel.add(preview, BorderLayout.WEST);
			}
			infoPanel.add(detailsPanel, BorderLayout.CENTER);
			this.add(infoPanel);
			this.add(Box.createVerticalStrut(25));
		}

		// assemble actions panel
		RoundedPanel actionsPanel = new RoundedPanel(50, 0, 0);
		actionsPanel.setBackground(new Color(80, 80, 80));
		actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.X_AXIS));
		actionsPanel.setBorder(BorderFactory.createEmptyBorder());
		installButton = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "Install", new Color(140,
				140, 140), new Color(140, 40, 40), 2);
		installButton.setToolTipText(PMTooltips.INSTALLBUTTON);
		installButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				installPacks();
			}
		});
		updateButton = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "Update ", new Color(140,
				140, 140), new Color(40, 140, 40), 2);
		updateButton.setToolTipText(PMTooltips.UPDATEBUTTON);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePacks();
			}
		});
		removeButton = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), "Remove ", new Color(140,
				140, 140), new Color(40, 140, 40), 2);
		removeButton.setToolTipText(PMTooltips.REMOVEBUTTON);
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removePacks();
			}
		});
		boolean showInstall = false;
		boolean showUpdate = false;
		boolean showRemove = false;
		for (PMPackage p : packs) {
			switch (p.getStatus()) {
				case TOINSTALL :
					showInstall = true;
					break;
				case TOUPDATE :
					showUpdate = true;
					break;
				case TOUNINSTALL :
					showRemove = true;
					break;
				default :
			}
		}
		int width = 10;
		if (showInstall) {
			actionsPanel.add(Box.createHorizontalGlue());
			actionsPanel.add(installButton);
			width += 110;
		}
		if (showUpdate) {
			actionsPanel.add(Box.createHorizontalGlue());
			actionsPanel.add(updateButton);
			width += 110;
		}
		if (showRemove) {
			actionsPanel.add(Box.createHorizontalGlue());
			actionsPanel.add(removeButton);
			width += 110;
		}
		if (!showInstall && !showUpdate && !showRemove) {
			actionsPanel.add(Box.createHorizontalGlue());
			actionsPanel.add(styleLabel("No actions available", new Color(160, 160, 160), 12));
			width += 110;
		}
		actionsPanel.setMinimumSize(new Dimension(width, 50));
		actionsPanel.setMaximumSize(new Dimension(width, 50));
		actionsPanel.setPreferredSize(new Dimension(width, 50));
		//		switch (pack.getStatus()) {
		//			case TOUNINSTALL :
		//				actionButton = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"),
		//						"Remove              ", new Color(140, 140, 140), new Color(140, 40, 40), 2);
		//				actionButton.setToolTipText(PMTooltips.REMOVEBUTTON);
		//				break;
		//			case TOUPDATE :
		//				actionButton = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"),
		//						"Update              ", new Color(140, 140, 140), new Color(40, 140, 40), 2);
		//				actionButton.setToolTipText(PMTooltips.UPDATEBUTTON);
		//				break;
		//			default :
		//				actionButton = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"),
		//						"Install             ", new Color(140, 140, 140), new Color(40, 140, 40), 2);
		//				actionButton.setToolTipText(PMTooltips.INSTALLBUTTON);
		//				break;
		//		}
		//		actionButton.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				action();
		//			}
		//		});
		//		actionsPanel.add(Box.createHorizontalGlue());
		//		actionsPanel.add(actionButton);
		actionsPanel.add(Box.createHorizontalGlue());
		this.add(ArrangementHelper.pushLeft(actionsPanel));
		this.add(Box.createVerticalStrut(25));

		if (packs.size() == 1) {
			// assemble family panel
			RoundedPanel familyPanel = new RoundedPanel(50, 0, 0) {
				private static final long serialVersionUID = 6739005088069438989L;

				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					// add fancy arrowhead
					int yMid = getHeight() / 2;
					int x[] = { 15, 45, 42, 45 };
					int y[] = { yMid, yMid - 15, yMid, yMid + 15 };
					g.setColor(new Color(120, 120, 120));
					g.fillPolygon(x, y, 4);
				}
			};
			familyPanel.setBackground(new Color(80, 80, 80));
			familyPanel.setLayout(new BoxLayout(familyPanel, BoxLayout.Y_AXIS));
			familyPanel.setMinimumSize(new Dimension(220, 100));
			familyPanel.setMaximumSize(new Dimension(220, 100));
			familyPanel.setPreferredSize(new Dimension(220, 100));
			familyPanel.setBorder(BorderFactory.createEmptyBorder(5, 55, 5, 15));
			parentButton = new ImageLozengeButton(ImageLoader.load("parent_30x30_black.png"), "Show parents");
			parentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showParents();
				}
			});
			parentButton.setToolTipText(PMTooltips.PARENTBUTTON);
			childrenButton = new ImageLozengeButton(ImageLoader.load("children_30x30_black.png"), "Show children");
			childrenButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showChildren();
				}
			});
			childrenButton.setToolTipText(PMTooltips.CHILDRENBUTTON);
			familyPanel.add(Box.createVerticalGlue());
			familyPanel.add(parentButton);
			familyPanel.add(Box.createVerticalStrut(5));
			familyPanel.add(childrenButton);
			familyPanel.add(Box.createVerticalGlue());
			this.add(ArrangementHelper.pushLeft(familyPanel));
			this.add(Box.createVerticalGlue());
		}
	}

	/**
	 * Returns true if the package was installed or updated, false otherwise.
	 * 
	 * @return
	 */
	//	public boolean action() {
	//		if (pack.getStatus() == PMStatus.TOUNINSTALL) {
	//			controller.remove(pack, controller.getMainView().getWorkspaceView());
	//			return false;
	//		} else {
	//			// (pack.getStatus() == PMStatus.TOUPDATE)  ||
	//			// (pack.getStatus() == PMStatus.TOINSTALL)
	//			controller.update(pack, controller.getMainView().getWorkspaceView());
	//			return true;
	//		}
	//	}

	public boolean installPacks() {
		boolean allDone = true;
		Collection<PMPackage> pp = new HashSet<PMPackage>();
		for (PMPackage p : packs) {
			if (pack.getStatus() == PMStatus.TOINSTALL) {
				pp.add(p);
			} else {
				allDone = false;
			}
		}
		if (!pp.isEmpty()) {
			controller.update(pp, controller.getMainView().getWorkspaceView());
			return allDone;
		}
		return false;
	}

	public boolean updatePacks() {
		boolean allDone = true;
		Collection<PMPackage> pp = new HashSet<PMPackage>();
		for (PMPackage p : packs) {
			if (pack.getStatus() == PMStatus.TOUPDATE) {
				pp.add(p);
			} else {
				allDone = false;
			}
		}
		if (!pp.isEmpty()) {
			controller.update(pp, controller.getMainView().getWorkspaceView());
			return allDone;
		}
		return false;
	}

	public boolean removePacks() {
		boolean allDone = true;
		Collection<PMPackage> pp = new HashSet<PMPackage>();
		for (PMPackage p : packs) {
			if (pack.getStatus() != PMStatus.TOINSTALL) {
				pp.add(p);
			} else {
				allDone = false;
			}
		}
		if (!pp.isEmpty()) {
			controller.remove(pp, controller.getMainView().getWorkspaceView());
			return allDone;
		}
		return false;
	}

	private void showParents() {
		controller.getMainView().getWorkspaceView().showParentsOf(pack);
	}

	private void showChildren() {
		controller.getMainView().getWorkspaceView().showChildrenOf(pack);
	}

	private JLabel styleLabel(String text, Color color, float size) {
		JLabel label = new JLabel(text);
		label.setOpaque(false);
		label.setForeground(color);
		label.setFont(label.getFont().deriveFont(size));
		return label;
	}

	private JLabel styleLinkedLabel(String text, final String link, Color color, float size) {
		JLabel label = styleLabel(text, color, size);
		if (link != null) {
			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			label.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 0) {
						if (Desktop.isDesktopSupported()) {
							Desktop desktop = Desktop.getDesktop();
							try {
								URI uri = new URI(link);
								desktop.browse(uri);
							} catch (IOException ex) {
								ex.printStackTrace();
							} catch (URISyntaxException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			});
		}
		return label;
	}

	private String getLink2License(String license) {
		if (license.equals("LGPL") || license.equals("L-GPL") || license.equals("Lesser GPL")) {
			return "http://www.gnu.org/licenses/lgpl.html";
		}
		if (license.equals("ProM"))	{
			return "http://www.promtools.org/doku.php?id=license";
		}
		if (license.equals("Apache 2.0")) {
			return "http://www.apache.org/licenses/LICENSE-2.0.html";
		}
		if (license.equals("Apache")) {
			return "http://www.apache.org/licenses/LICENSE-1.0";
		}
		if (license.equals("Apache 1.1")) {
			return "http://www.apache.org/licenses/LICENSE-1.1";
		}
		if (license.equals("BSD 3-Clause")) {
			return "http://opensource.org/licenses/BSD-3-Clause";
		}
		if (license.equals("BSD")) {
			return "https://opensource.org/licenses/BSD-2-Clause";
		}
		if (license.equals("CPL")) {
			return "https://opensource.org/licenses/cpl1.0.php";
		}
		if (license.equals("Daikon-specific license")) {
			return "http://plse.cs.washington.edu/daikon/download/doc/daikon.html#License";
		}
		if (license.equals("dev.java.net \"Other\" License")) {
			return "https://tablelayout.dev.java.net/servlets/LicenseDetails?licenseID=18";
		}
		if (license.equals("EPL 1.0") || license.equals("EPL")) {
			return "http://www.eclipse.org/legal/epl-v10.html";
		}
		if (license.equals("GPL")) {
			return "http://www.gnu.org/licenses/gpl.html";
		}
		if (license.equals("GPL v2")) {
			return "http://www.gnu.org/licenses/gpl-2.0.html";
		}
		if (license.equals("AGPL") || license.equals("A-GPL") || license.equals("Affero GPL")) {
			return "https://www.gnu.org/licenses/agpl.html";
		}
		if (license.equals("MIT")) {
			return "http://www.opensource.org/licenses/mit-license.php";
		}
		if (license.equals("Mozilla Public License v1.0")) {
			return "https://www-archive.mozilla.org/MPL/MPL-1.0.html";
		}
		return null;
	}
}
