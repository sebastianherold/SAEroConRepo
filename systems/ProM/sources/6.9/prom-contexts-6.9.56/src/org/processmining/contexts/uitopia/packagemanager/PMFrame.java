package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.deckfour.uitopia.ui.conf.ConfigurationSet;
import org.deckfour.uitopia.ui.conf.UIConfiguration;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.util.CommandLineArgumentList;

public class PMFrame extends JFrame {

	private static final long serialVersionUID = 3000058846006966241L;

	private static final String CONF_X = "window_x";
	private static final String CONF_Y = "window_y";
	private static final String CONF_WIDTH = "window_width";
	private static final String CONF_HEIGHT = "window_height";

	private final ConfigurationSet conf;

	private final PMController controller;

	public PMFrame() {
		controller = new PMController(Boot.VERBOSE);

		// register closing action..
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitApplication(true);
			}

			public void windowClosed(WindowEvent e) {
				windowClosing(e);
			}
		});
		addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				saveWindowState();
			}

			public void componentResized(ComponentEvent e) {
				saveWindowState();
			}
		});
		// restore window position and size
		conf = UIConfiguration.master().getChild(this.getClass().getCanonicalName());
		restoreWindowState();
		// set up window contents
		setLayout(new BorderLayout());
		this.add(controller.getMainView(), BorderLayout.CENTER);
		setTitle("ProM UITopia Package Manager");
		// show frame
	}

	protected void saveWindowState() {
		Point p = getLocation();
		conf.setInteger(CONF_X, p.x);
		conf.setInteger(CONF_Y, p.y);
		conf.setInteger(CONF_WIDTH, getWidth());
		conf.setInteger(CONF_HEIGHT, getHeight());
	}

	protected void restoreWindowState() {
		int x = conf.getInteger(CONF_X, 10);
		int y = conf.getInteger(CONF_Y, 10);
		int width = conf.getInteger(CONF_WIDTH, 1024);
		int height = conf.getInteger(CONF_HEIGHT, 750);
		x = Math.max(0, x);
		y = Math.max(0, y);
		width = Math.min(width, Toolkit.getDefaultToolkit().getScreenSize().width);
		height = Math.min(height, Toolkit.getDefaultToolkit().getScreenSize().height);
		this.setLocation(x, y);
		this.setSize(width, height);
	}

	protected void exitApplication(boolean askUser) {
		saveConfig();
		System.exit(0);
	}

	public void saveConfig() {
		try {
			UIConfiguration.save();
		} catch (IOException e) {
			System.err.println("ERROR: Could not save UITopia configuration!");
			e.printStackTrace();
		}
	}

	@Bootable
	public Object main(CommandLineArgumentList args) {
		return this;
	}

	public static void main(String[] args) throws Exception {
		PMFrame frame = (PMFrame) Boot.boot(PMFrame.class, args);
		frame.setIconImage(ImageLoader.load("prom_icon_32x32.png"));
		frame.setVisible(true);
	}

	public PMController getController() {
		return controller;
	}
}
