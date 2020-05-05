package org.processmining.contexts.uitopia.packagemanager;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.ui.components.ImageLozengeButton;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.util.OsUtil;

import com.fluxicon.slickerbox.components.RoundedPanel;

public class PMMemoryView extends RoundedPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2483996283422006055L;

	/*
	 * Get whether we are running in 64 bit mode, this allows allocating more
	 * than 1300 MB of memory.
	 */
	private static boolean is64bit = OsUtil.is64Bit();
	/*
	 * Get the available memory.
	 */
	private static long mem = OsUtil.getPhysicalMemory() / (1024 * 1024);

	/*
	 * Start with using 1 GB of memory.
	 */
	private static MemoryOption selectedMem = MemoryOption.XMX1G;
	private static MemoryOption oldSelectedMem = MemoryOption.XMX1G;

	private PMController controller;

	private enum MemoryOption implements ActionListener {
		XMX1G("1G", true), // 1 GB
		XMX1300M("1300M", true), // 1300 MB, about the limit in 32 bit mode
		XMX2G("2G", is64bit && mem >= 2 * 1024), // 2 GB
		XMX3G("3G", is64bit && mem >= 3 * 1024), // 3 GB
		XMX4G("4G", is64bit && mem >= 4 * 1024), // 4 GB
		XMX6G("6G", is64bit && mem >= 6 * 1024), // 6 GB
		XMX8G("8G", is64bit && mem >= 8 * 1024), // 8 GB
		XMX12G("12G", is64bit && mem >= 12 * 1024), // 12 GB
		XMX16G("16G", is64bit && mem >= 16 * 1024); // 16 GB

		/*
		 * Text to be added to "-Xmx"
		 */
		private String size;
		/*
		 * Whether this memory option is available on this computer.
		 */
		private boolean isAvailable;
		/*
		 * Button to show when this option is selected.
		 */
		private ImageLozengeButton selected;
		/*
		 * Button to show when this option is not selected.
		 */
		private ImageLozengeButton notSelected;

		/*
		 * Creates a memory option.
		 */
		private MemoryOption(String size, boolean isAllowed) {
			this.size = size;
			this.isAvailable = isAllowed;
		}

		/*
		 * Returns the string to add to "-Xmx".
		 */
		public String toString() {
			return size;
		}

		/*
		 * Returns the string to put on the buttons.
		 */
		public String toDisplay() {
			return size.replace("G", " GB").replace("M", " MB");
		}

		/*
		 * Returns whether this option is available.
		 */
		public boolean isAvailable() {
			return isAvailable;
		}

		/*
		 * Returns the current shown button.
		 */
		public JButton getButton(boolean isSelected) {
			return isSelected ? selected : notSelected;
		}

		/*
		 * Initialize this memory option. Creates the buttons, sets the tool
		 * tip, and the action listener.
		 */
		public void init(ActionListener actionListener) {
			selected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), toDisplay());
			selected.setEnabled(false);
			notSelected = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), toDisplay());
			notSelected.setToolTipText("Use " + toDisplay() + " of memory when running ProM");
			notSelected.addActionListener(actionListener);
		}

		/*
		 * Act on a button being selected.
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(ActionEvent e) {
			/*
			 * Check whether my button was selected.
			 */
			if (e.getSource() == notSelected) {
				/*
				 * Yes, it was. Set my memory size.
				 */
				selectedMem = this;
			}
		}
	};

	/*
	 * Creates the memory view panel.
	 */
	public PMMemoryView(PMController controller) {
		super(20, 5, 0);
		this.controller = controller;
		setBackground(new Color(160, 160, 160));
		setLayout(new BorderLayout());
		try {
			/*
			 * Try to read the current memory option from file, and set the
			 * default accordingly.
			 */
			String version = Boot.PROM_VERSION.replaceAll("\\.", "");
			FileReader reader = new FileReader("ProM" + version + ".l4j.ini");
			char[] a = new char[10];
			reader.read(a);
			reader.close();
			String b = String.valueOf(a);
			for (MemoryOption size : MemoryOption.values()) {
				if (b.startsWith("-Xmx" + size)) {
					/*
					 * Found a match. Set this option as default.
					 */
					selectedMem = size;
					continue;
				}
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		setupUI();
		update();
	}

	/*
	 * Sets up the buttons.
	 */
	private void setupUI() {
		/*
		 * Initialize all buttons.
		 */
		for (MemoryOption size : MemoryOption.values()) {
			size.init(this);
		}
	}

	/*
	 * A button was pressed. Check every option, and then update.
	 */
	public void actionPerformed(ActionEvent e) {
		for (MemoryOption size : MemoryOption.values()) {
			/*
			 * Check whether the button belongs to this option.
			 */
			size.actionPerformed(e);
		}
		update();
	}

	/*
	 * Update the memory view.
	 */
	private void update() {
		updateFiles();
		double layoutSize[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL }, { 30, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(layoutSize));
		
		/*
		 * Create new buttonPanel.
		 */
		JPanel buttonPanel = new RoundedPanel(20, 5, 0);
		buttonPanel.setBackground(new Color(80, 80, 80));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		for (MemoryOption size : MemoryOption.values()) {
			/*
			 * Only add button if available.
			 */
			if (size.isAvailable()) {
				/*
				 * Use selected button if selected, not-selected button
				 * otherwise.
				 */
				buttonPanel.add(size.getButton(selectedMem == size));
			}
		}
		/*
		 * Refresh with new button panel.
		 */
		removeAll();
		add(new JLabel("Manage memory setting"), "0, 0, 1, 0");
		add(buttonPanel, "0, 1, 1, 1");

		JPanel gaButtonPanel = new RoundedPanel(20, 5, 0);
		gaButtonPanel.setBackground(new Color(80, 80, 80));
		gaButtonPanel.setLayout(new BoxLayout(gaButtonPanel, BoxLayout.X_AXIS));

		final ImageLozengeButton enableGAButton = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "Enable");
		final ImageLozengeButton disableGAButton = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), "Disable");
		if (Preferences.userNodeForPackage(Boot.class).get(Boot.TRACKING_BY_GA_ALLOWED, "false").equals("true")) {
			enableGAButton.setEnabled(false);
			disableGAButton.setEnabled(true);
		} else {
			enableGAButton.setEnabled(true);
			disableGAButton.setEnabled(false);
		}
		enableGAButton.setToolTipText("Select this button to enable tracking with GA");
		disableGAButton.setToolTipText("Select this button to disable tracking with GA");
		enableGAButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Preferences.userNodeForPackage(Boot.class).put(Boot.TRACKING_BY_GA_ALLOWED, "true");
				enableGAButton.setEnabled(false);
				disableGAButton.setEnabled(true);
			}			
		});
		disableGAButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Preferences.userNodeForPackage(Boot.class).put(Boot.TRACKING_BY_GA_ALLOWED, "false");
				enableGAButton.setEnabled(true);
				disableGAButton.setEnabled(false);
			}			
		});
		gaButtonPanel.add(enableGAButton);
		gaButtonPanel.add(disableGAButton);
		add(new JLabel("Manage Google Analytics (GA) setting"), "2, 0");
		add(gaButtonPanel, "2, 1");
		
		JPanel cacheButtonPanel = new RoundedPanel(20, 5, 0);
		cacheButtonPanel.setBackground(new Color(80, 80, 80));
		cacheButtonPanel.setLayout(new BoxLayout(cacheButtonPanel, BoxLayout.X_AXIS));

		ImageLozengeButton cleanCacheButton = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"),
				"Clear", new Color(140, 140, 140), new Color(40, 140, 40), 2);
		cleanCacheButton.setToolTipText(PMTooltips.REMOVEBUTTON);
		cleanCacheButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cleanCache();
			}
		});

		cacheButtonPanel.add(cleanCacheButton);
		add(new JLabel("Manage Plugin Cache"), "3, 0");
		add(cacheButtonPanel, "3, 1");

		revalidate();
	}

	protected void cleanCache() {
		try {
			controller.cleanPackageCache();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Updates the necessary files with the new memory option.
	 */
	private void updateFiles() {
		if (oldSelectedMem == selectedMem) {
			return;
		}
		String version = Boot.PROM_VERSION.replaceAll("\\.", "");
		if (OsUtil.isRunningWindows()) {
			/*
			 * Windows. Need to update ini file and bat file.
			 */
			if (!updateIniFile() || !updateBatFile()) {
				/*
				 * Something failed. Possibly, the PM was not run in
				 * Administrator mode.
				 */
				JOptionPane
						.showMessageDialog(
								null,
								"Unable to set memory limit (-Xmx"
										+ selectedMem
										+ ") in ProM"
										+ version
										+ ".l4j.ini and/or ProM"
										+ version
										+ ".bat file.\nPlease run the Package Manager as administrator, or set the memory limit manually.");
				selectedMem = oldSelectedMem;
				return;
			}
		} else if (OsUtil.isRunningLinux() || OsUtil.isRunningUnix()) {
			/*
			 * Linux or UNIX. Need to update sh file (and init file for reading
			 * default option next time).
			 */
			if (!updateIniFile() || !updateShFile()) {
				JOptionPane.showMessageDialog(null, "Unable to set memory limit (-Xmx" + selectedMem + ") in ProM"
						+ version + ".l4j.ini and/or ProM" + version
						+ ".sh file.\nPlease set the memory limit manually.");
				selectedMem = oldSelectedMem;
				return;
			}
		}
		/*
		 * No MAC support (yet), sorry.
		 */
		oldSelectedMem = selectedMem;
	}

	private boolean updateFile(String ext) {
		try {
			String version = Boot.PROM_VERSION.replaceAll("\\.", "");
			Path path = Paths.get("ProM" + version + ext);
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(path), charset);
			String oldMem = "-Xmx" + oldSelectedMem;
			String newMem = "-Xmx" + selectedMem;
			content = content.replaceAll(oldMem, newMem);
			Files.write(path, content.getBytes(charset));
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private boolean updateIniFile() {
		return updateFile(".l4j.ini");
	}

	private boolean updateBatFile() {
		return updateFile(".bat");
	}

	private boolean updateShFile() {
		return updateFile(".sh");
	}

}
