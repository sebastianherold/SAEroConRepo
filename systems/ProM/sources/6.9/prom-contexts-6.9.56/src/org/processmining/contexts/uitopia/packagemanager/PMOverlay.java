package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.deckfour.uitopia.ui.main.Overlayable;
import org.deckfour.uitopia.ui.overlay.TwoButtonOverlayDialog;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;

import com.fluxicon.slickerbox.factory.SlickerDecorator;

public class PMOverlay extends TwoButtonOverlayDialog implements PackageManager.Canceller {

	/**
	 * Timeout in seconds after which dialog disappears
	 */
	private final static int TIMEOUT = 10;

	private static final long serialVersionUID = 4721480656622194553L;
	private final JTextArea log;
	private final JLabel label = new JLabel("");
	private final JLabel iconLabel = new JLabel("");
	private boolean cancelled;

	public PMOverlay(Overlayable view) {
		super(view, "External Packages Required", "Cancel", "  OK  ", null);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		iconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		log = new JTextArea();
		log.setOpaque(false);
		log.setBorder(BorderFactory.createEmptyBorder());
		log.setLineWrap(true);
		log.setWrapStyleWord(true);

		JScrollPane scrollpane = new JScrollPane(log);
		scrollpane.setOpaque(false);
		scrollpane.getViewport().setOpaque(false);
		scrollpane.setBorder(BorderFactory.createEmptyBorder());
		scrollpane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		SlickerDecorator.instance().decorate(scrollpane.getVerticalScrollBar(), new Color(0, 0, 0, 0),
				new Color(20, 20, 20), new Color(60, 60, 60));
		scrollpane.getVerticalScrollBar().setOpaque(false);

		scrollpane.setPreferredSize(new Dimension(800, 100));
		scrollpane.setMinimumSize(new Dimension(0, 100));
		scrollpane.setSize(new Dimension(800, 100));

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setOpaque(false);
		southPanel.setBorder(BorderFactory.createEmptyBorder());

		//		JCheckBox check = new JCheckBox(
		//				"Automatically install new versions of packages (requires an internet connection (xx))");
		//		check.setOpaque(false);
		//		check.setSelected(PackageManager.getInstance().doAutoUpdate());
		//		check.addItemListener(new ItemListener() {
		//
		//			public void itemStateChanged(ItemEvent e) {
		//				if (e.getStateChange() == ItemEvent.DESELECTED) {
		//					PackageManager.getInstance().setAutoUpdate(false);
		//				} else if (e.getStateChange() == ItemEvent.SELECTED) {
		//					PackageManager.getInstance().setAutoUpdate(true);
		//				}
		//			}
		//		});

		southPanel.add(scrollpane, BorderLayout.CENTER);
		//		southPanel.add(check, BorderLayout.SOUTH);

		mainPanel.add(label, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		mainPanel.add(iconLabel, BorderLayout.EAST);

		setPayload(mainPanel);
		cancelled = false;
		getCancelButton().setEnabled(true);
		getOKButton().setEnabled(false);
	}

	public void setPackage(PackageDescriptor pack) {
		label.setText(pack.toHTML());
		ImageIcon icon = PMIconCache.getIcon(pack);
		if (icon != null) {
			Image img = icon.getImage();
			int m = icon.getIconHeight() > icon.getIconWidth() ? 200 : -200;
			iconLabel.setIcon(new ImageIcon(img.getScaledInstance(-m, m, Image.SCALE_SMOOTH)));
		} else {
			iconLabel.setIcon(null);
			return;
		}
	}

	public void addText(String text) {
		log.append(text);
		log.append("\n");
		log.setCaretPosition(log.getText().length());
	}

	public void clear() {
		log.setText("");
	}

	public void finishedInstall(boolean error) {
		getOKButton().setEnabled(true);
		getCancelButton().setEnabled(false);
		if (error) {
			// HV: Do not auto-close on an error, as the error message will get lost.
			// Wait for the user to close the overlay.
			//close(true);

			// Indicate that an error has occurred.
			addText("An error has occurred. Please select OK to continue.");
			log.setForeground(new Color(90, 0, 0));
		} else {
			final String message = "Closing in " + TIMEOUT + " seconds.";
			log.append(message);
			final Timer t = new Timer();
			t.schedule(new TimerTask() {

				private int remaining = TIMEOUT - 1;
				private int lastlength = message.length();

				public void run() {
					if (remaining == 0) {
						cancel();
						return;
					}
					String s = "Closing in " + (remaining--) + " seconds.";
					int l = log.getText().length();
					log.replaceRange(s, l - lastlength, l);
					lastlength = s.length();
				}

			}, 1000L, 1000L);
			t.schedule(new TimerTask() {

				public void run() {
					close(true);
				}

			}, 1000L * TIMEOUT);
		}

	}

	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void close(boolean okayed) {
		if (!okayed) {
			cancelled = true;
		}
		super.close(okayed);
	}

}
