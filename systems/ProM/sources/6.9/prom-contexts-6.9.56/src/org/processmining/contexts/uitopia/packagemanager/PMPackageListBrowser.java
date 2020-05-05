package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.uitopia.ui.components.ImageLozengeButton;
import org.deckfour.uitopia.ui.components.ImageRadioButton;
import org.deckfour.uitopia.ui.util.ArrangementHelper;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.framework.util.OsUtil;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerDecorator;

public class PMPackageListBrowser extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8111058383691281752L;

	public enum Type {
		TOUNINSTALL, TOUPDATE, TOINSTALL, SELECTION
	}

	private static final int VIEWPORT_WIDTH = 500;

	private final PMController controller;
	private List<? extends PMPackage> packages = new ArrayList<PMPackage>();

	private final Type type;

	private PMPackageListModel listModel;
	private JList<? extends PMPackage> packageList;
	private JComponent viewport;
	private JLabel header;

	private JRadioButton sortByPackageName;
	private JRadioButton sortByAuthorName;

	private PMPackageView pmPackageView = null;

	private boolean is64bit = OsUtil.is64Bit();
	private long mem = OsUtil.getPhysicalMemory() / (1024 * 1024);
	private int selectedMem = (is64bit ? 2 : 1);
	private int oldSelectedMem = 1;

	private RoundedPanel memPanel = new RoundedPanel(100, 0, 0);
	private ImageLozengeButton button1gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"),
			"1 GB");
	private ImageLozengeButton button2gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"),
			"2 GB");
	private ImageLozengeButton button4gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"),
			"4 GB");
	private ImageLozengeButton button8gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"),
			"4 GB");
	private ImageLozengeButton button16gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"),
			"4 GB");
	private ImageLozengeButton button1gbNotSelected = new ImageLozengeButton(
			ImageLoader.load("action_30x30_black.png"), "1 GB");
	private ImageLozengeButton button2gbNotSelected = new ImageLozengeButton(
			ImageLoader.load("action_30x30_black.png"), "2 GB");
	private ImageLozengeButton button4gbNotSelected = new ImageLozengeButton(
			ImageLoader.load("action_30x30_black.png"), "4 GB");
	private ImageLozengeButton button8gbNotSelected = new ImageLozengeButton(
			ImageLoader.load("action_30x30_black.png"), "8 GB");
	private ImageLozengeButton button16gbNotSelected = new ImageLozengeButton(
			ImageLoader.load("action_30x30_black.png"), "16 GB");

	public PMPackageListBrowser(PMController controller, Type type) {
		this.controller = controller;
		this.type = type;
		setupUI();
		updateData();
	}

	public boolean isEmpty() {
		return packages.isEmpty();
	}

	public void selectPackage(String name) {
		for (int i = 0; i < listModel.getSize(); i++) {
			if (((PMPackage) listModel.getElementAt(i)).getPackageName().equals(name)) {
				packageList.setSelectedIndex(i);
				packageList.ensureIndexIsVisible(i);
				return;
			}
		}
	}

	public void setSelectionContent(PMPackage reference, boolean showParents) {
		if (showParents) {
			packages = controller.getParentPackages(reference);
		} else {
			packages = controller.getChildPackages(reference);
		}
		String rel = showParents ? "Parents of " : "Children of ";
		header.setText(rel + reference.getPackageName());
	}

	public void showPackage(PMPackage res) {
		if (res == null) {
			packageList.setSelectedIndex(0);
			packageList.ensureIndexIsVisible(0);
			return;
		}
		for (int i = 0; i < listModel.getSize(); i++) {
			if (listModel.getElementAt(i).equals(res)) {
				packageList.setSelectedIndex(i);
				packageList.ensureIndexIsVisible(i);
				break;
			}
		}
	}

	private void setupUI() {
		setOpaque(true);
		setBackground(new Color(180, 180, 180));
		setBorder(BorderFactory.createEmptyBorder());
		JPanel browser = new JPanel();
		browser.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		browser.setLayout(new BorderLayout());
		browser.setOpaque(true);
		browser.setBackground(new Color(180, 180, 180));
		listModel = new PMPackageListModel(packages);
		packageList = new JList(listModel);
		packageList.setBackground(new Color(160, 160, 160));
		packageList.setCellRenderer(new PMPackageListCellRenderer());
		packageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		packageList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateViewport();
			}
		});
		sortByPackageName = new ImageRadioButton(ImageLoader.load("sortByPackage_27x20_white.png"));
		sortByAuthorName = new ImageRadioButton(ImageLoader.load("sortByAuthor_27x20_white.png"));
		ButtonGroup sortGroup = new ButtonGroup();
		sortGroup.add(sortByPackageName);
		sortGroup.add(sortByAuthorName);
		sortByPackageName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						sortList(true);
					}
				});
			}
		});
		sortByPackageName.setToolTipText(PMTooltips.SORTBYPACKAGENAME);
		sortByAuthorName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						sortList(true);
					}
				});
			}
		});
		sortByAuthorName.setToolTipText(PMTooltips.SORTBYAUTHORNAME);
		sortByPackageName.setSelected(true);
		// assemble header
		header = new JLabel("No selection to show");
		header.setOpaque(false);
		header.setFont(header.getFont().deriveFont(16f));
		header.setForeground(new Color(60, 60, 60));
		// assemble sort panel
		JPanel sortPanel = new JPanel();
		sortPanel.setMinimumSize(new Dimension(180, 40));
		sortPanel.setMaximumSize(new Dimension(20000, 40));
		sortPanel.setPreferredSize(new Dimension(200, 40));
		sortPanel.setOpaque(false);
		sortPanel.setBorder(BorderFactory.createEmptyBorder());
		sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.X_AXIS));
		JLabel sortLabel = new JLabel("sort by");
		sortLabel.setFont(sortLabel.getFont().deriveFont(10f));
		sortLabel.setForeground(new Color(80, 80, 80));
		sortPanel.add(sortLabel);
		sortPanel.add(Box.createHorizontalStrut(10));
		sortPanel.add(sortByPackageName);
		sortPanel.add(Box.createHorizontalStrut(5));
		sortPanel.add(sortByAuthorName);
		sortPanel.add(Box.createHorizontalGlue());
		// assemble list view
		JScrollPane listScrollPane = new JScrollPane(packageList);
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		SlickerDecorator.instance().decorate(listScrollPane, new Color(180, 180, 180), new Color(40, 40, 40),
				new Color(100, 100, 100));
		listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScrollPane.setViewportBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
		// top panel
		JPanel topPanel = new JPanel();
		topPanel.setMinimumSize(new Dimension(180, 60));
		topPanel.setMaximumSize(new Dimension(20000, 60));
		topPanel.setPreferredSize(new Dimension(200, 60));
		topPanel.setOpaque(false);
		topPanel.setBorder(BorderFactory.createEmptyBorder());
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		// assemble browser view
		if (type.equals(Type.SELECTION)) {
			topPanel.add(ArrangementHelper.pushLeft(header));
			topPanel.add(sortPanel);
			browser.add(topPanel, BorderLayout.NORTH);
		} else {
			browser.add(sortPanel, BorderLayout.NORTH);
		}
		browser.add(listScrollPane, BorderLayout.CENTER);
		// assemble all
		viewport = new JPanel();
		viewport.setOpaque(false);
		viewport.setLayout(new BorderLayout());
		viewport.setBorder(BorderFactory.createEmptyBorder(40, 10, 5, 5));
		viewport.setMinimumSize(new Dimension(VIEWPORT_WIDTH, 10));
		viewport.setMaximumSize(new Dimension(VIEWPORT_WIDTH, 10000));
		viewport.setPreferredSize(new Dimension(VIEWPORT_WIDTH, 400));
		setLayout(new BorderLayout());
		this.add(browser, BorderLayout.CENTER);
		this.add(viewport, BorderLayout.EAST);
	}

	private void sortList(boolean keepSelection) {
		final Object selected = packageList.getSelectedValue();
		if (sortByPackageName.isSelected()) {
			listModel.sortByPackageName();
		} else if (sortByAuthorName.isSelected()) {
			listModel.sortByAuthorName();
		}
		if (keepSelection) {
			for (int i = 0; i < listModel.getSize(); i++) {
				if (listModel.getElementAt(i).equals(selected)) {
					packageList.setSelectedIndex(i);
					packageList.ensureIndexIsVisible(i);
					break;
				}
			}
		}
		revalidate();
		repaint();
	}

	public void updateData() {
		if (type.equals(Type.TOUNINSTALL)) {
			packages = controller.getToUninstallPackages();
		} else if (type.equals(Type.TOUPDATE)) {
			packages = controller.getToUpdatePackages();
		} else if (type.equals(Type.TOINSTALL)) {
			packages = controller.getToInstallPackages();
		}

		// Filter packages to keep only those that match the given query
		String query = controller.getQuery();
		List<PMPackage> filteredPackages = new ArrayList<PMPackage>();
		if (query.isEmpty()) {
			filteredPackages.addAll(packages);
		} else {
			for (PMPackage pack : packages) {
				if (pack.getAuthorName().toLowerCase().matches(query) || pack.getPackageName().toLowerCase().matches(query)
						|| pack.getDescription().toLowerCase().matches(query) || pack.getVersion().toLowerCase().matches(query)
						|| pack.getDescriptor().getOrganisation().toLowerCase().matches(query)
						|| pack.getDescriptor().getAuthor().toLowerCase().matches(query)
						|| pack.getDescriptor().getDescription().toLowerCase().matches(query)
						|| pack.getDescriptor().getMaintainer().toLowerCase().matches(query)
						|| pack.getDescriptor().getLicense().toLowerCase().matches(query)
						|| pack.getDescriptor().getName().toLowerCase().matches(query)
						|| pack.getDescriptor().getKeywords().toLowerCase().matches(query)) {
					filteredPackages.add(pack);
				}
			}
		}
		listModel = new PMPackageListModel(filteredPackages);
		final Object selected = packageList.getSelectedValue();
		int index = packageList.getSelectedIndex();

		packageList.setModel(listModel);
		sortList(false);
		packageList.setSelectedValue(selected, true);
		if (packageList.isSelectionEmpty()) {
			int i = Math.max(0, Math.min(index, filteredPackages.size() - 1));
			packageList.setSelectedIndex(i);
			packageList.ensureIndexIsVisible(i);
		}

	}

	private void updateViewport() {
		viewport.removeAll();
		List<? extends PMPackage> selected = packageList.getSelectedValuesList();
		Collection<PMPackage> selectedPacks = new HashSet<PMPackage>();
		for (int i = 0; i < selected.size(); i++) {
			selectedPacks.add(selected.get(i));
		}
		if (!selectedPacks.isEmpty()) {
			pmPackageView = new PMPackageView(selectedPacks, controller);
			viewport.add(pmPackageView, BorderLayout.CENTER);
		} else {
			viewport.add(new JLabel("No packages selected"), BorderLayout.CENTER);
		}
		viewport.revalidate();
	}

	public PMPackageView getPackageView() {
		return pmPackageView;
	}
}
