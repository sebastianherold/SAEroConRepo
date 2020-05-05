package org.processmining.framework.util.ui.widgets;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import org.deckfour.uitopia.ui.components.ImageButton;

import com.fluxicon.slickerbox.components.SlickerTabbedPane;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Inspector-style window. You should probably use an InspectorPanel instead of
 * this class directly.
 * 
 * @author mwesterg
 * 
 */
public abstract class Inspector extends JWindow {
	private static final Icon closed;
	private static final Image minimize;
	private static final Icon opened;
	private static final Image options;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static {
		Image i = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
		Graphics g = i.getGraphics();
		g.setColor(Color.RED);
		g.fillPolygon(new int[] { 0, 8, 0 }, new int[] { 0, 4, 8 }, 3);
		closed = new ImageIcon(i);

		i = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
		g = i.getGraphics();
		g.setColor(Color.LIGHT_GRAY);
		g.fillPolygon(new int[] { 0, 4, 8 }, new int[] { 0, 8, 0 }, 3);
		opened = new ImageIcon(i);

		minimize = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		g = Inspector.minimize.getGraphics();
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(WidgetColors.HEADER_COLOR);
		g2d.setStroke(new BasicStroke(3));
		g2d.drawArc(0, 0, 15, 15, 90, 270);
		g2d.drawLine(7, 8, 12, 3);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(5, 10, 5, 5);
		g2d.drawLine(5, 10, 10, 10);

		options = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		g = Inspector.options.getGraphics();
		g2d = (Graphics2D) g.create();
		g2d.setColor(WidgetColors.HEADER_COLOR);
		g2d.setStroke(new BasicStroke(3));
		g2d.drawArc(0, 0, 15, 15, 110, 220);
		g2d.drawArc(8, 1, 7, 7, 90, 270);
		g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.drawLine(5, 10, 8, 7);

		//g2d.setColor(Color.LIGHT_GRAY);
		//g2d.drawLine(15, 0, 11, 4);
	}
	private final JPanel buttons;
	private final JPanel main;
	private final JPanel settings;
	private final SlickerTabbedPane tabbedPane;

	/**
	 * @deprecated, use Inspector(Frame owner) instead.
	 */
	@Deprecated
	public Inspector() {
		this(null);
	}

	public Inspector(Frame owner) {
		super(owner);
		final MouseAdapter listener = new MouseAdapter() {
			private int x, y;

			@Override
			public void mouseDragged(final MouseEvent e) {
				setLocation(getLocation().x + e.getX() - x, getLocation().y + e.getY() - y);
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				x = e.getX();
				y = e.getY();
			}
		};
		addMouseListener(listener);
		addMouseMotionListener(listener);
		setBackground(null);
		setFocusable(false);
		setMinimumSize(new Dimension(200, 50));
		setMaximumSize(new Dimension(200, 2000));
		setSize(200, 600);
		setAlwaysOnTop(true);
		//Toolkit.getDefaultToolkit().addAWTEventListener(new Fader(), AWTEvent.MOUSE_EVENT_MASK);
		settings = new BorderPanel(3, 3);
		settings.add(SlickerFactory.instance().createCheckBox("Only one open group", false));
		final JPanel padding = new JPanel(new BorderLayout());
		padding.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
		padding.add(settings);
		padding.setOpaque(false);
		main = new JPanel(new BorderLayout());
		padding.setVisible(false);
		main.add(padding, BorderLayout.NORTH);
		main.setOpaque(false);
		tabbedPane = SlickerFactory.instance().createTabbedPane("", Color.BLACK, Color.WHITE, Color.BLACK);
		final BorderPanel border = new BorderPanel(5, 5);
		border.setOpaque(true);
		border.setBackground(Color.LIGHT_GRAY);
		border.setLayout(new BorderLayout());
		main.add(tabbedPane);
		border.add(main);
		final JPanel header = new JPanel();
		header.setLayout(new BorderLayout());
		header.setOpaque(false);
		final LeftAlignedHeader headerText = new LeftAlignedHeader("Inspector");
		headerText.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		header.add(headerText);
		buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
		buttons.setOpaque(false);
		buttons.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 2));
		final ImageButton minimizeButton = new ImageButton(Inspector.minimize, Color.LIGHT_GRAY, Color.GRAY, 0);
		minimizeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
				main.remove(tabbedPane);
				minimized(tabbedPane);
			}

		});
		buttons.add(minimizeButton);
		final ImageButton optionsButton = new ImageButton(Inspector.options, Color.LIGHT_GRAY, Color.GRAY, 0);
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				padding.setVisible(!padding.isVisible());
				invalidate();
				repaint();
				pack();
			}
		});
		buttons.add(optionsButton);
		header.add(buttons, BorderLayout.EAST);
		border.add(header, BorderLayout.NORTH);
		border.setForeground(Color.BLACK);
		add(border);

		tabbedPane.setBackground(null);

		// EWEWEW! SlickerBox insists on setting a preferred size instead of minimum and maximum sizes, so for pack to work, we need to reset this
		tabbedPane.getComponent(0).setPreferredSize(null);
		// Another EW! SlickerBox provides a veryrudimentary event-system when tabs are switched (events are only passed before); this allows us to receive an event when a tab has been switched
		((Container) tabbedPane.getComponent(1)).addContainerListener(new ContainerListener() {

			@Override
			public void componentAdded(final ContainerEvent arg0) {
				pack();

			}

			@Override
			public void componentRemoved(final ContainerEvent arg0) {
			}
		});

		pack();
		//FM, Updated to Java 7 commands as explained here: https://docs.oracle.com/javase/tutorial/uiswing/misc/trans_shaped_windows.html
		if (getGraphicsConfiguration().getDevice().isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
			this.setBackground(new Color(0, 0, 0,1.0f));
			this.setOpacity(0.85f);
		}
	}

	/**
	 * @param tab
	 * @param header
	 * @param component
	 */
	public void addGroup(final JPanel tab, final String header, final JComponent component) {
		addGroup(tab, header, component, false);
	}

	/**
	 * @param tab
	 * @param header
	 * @param component
	 * @param open
	 */
	public void addGroup(final JPanel tab, final String header, final JComponent component, final boolean open) {
		final JPanel group = new JPanel();
		group.setBackground(Color.BLACK);
		group.setLayout(new BorderLayout());
		final JLabel headerComponent = new JLabel() {
			private static final long serialVersionUID = 1L;
			private Color c;

			@Override
			public void paintComponent(final Graphics g) {
				final Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(new GradientPaint(new Point(getWidth() / 2, getHeight()), c, new Point(getWidth(),
						getHeight()), Color.LIGHT_GRAY));
				g2d.fillRect(0, 0, getWidth(), getHeight());
				super.paintComponent(g);
			}

			@Override
			public void setBackground(final Color c) {
				this.c = c;
				super.setOpaque(false);
				super.setBackground(null);
			}
		};
		headerComponent.setBackground(Color.BLACK);
		headerComponent.setText(header);
		if (open) {
			headerComponent.setIcon(Inspector.opened);
		} else {
			headerComponent.setIcon(Inspector.closed);
		}
		headerComponent.setFont(headerComponent.getFont().deriveFont(Font.BOLD + Font.ITALIC));
		headerComponent.setForeground(Color.LIGHT_GRAY);
		group.add(headerComponent, BorderLayout.NORTH);

		final JPanel main = SlickerFactory.instance().createRoundedPanel(10, Color.LIGHT_GRAY);
		main.setLayout(new BorderLayout());
		main.add(component);
		if (open) {
			group.add(main);
		}
		headerComponent.addMouseListener(new MouseAdapter() {
			private boolean opened = open;

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (opened) {
					headerComponent.setIcon(Inspector.closed);
					group.remove(main);
					opened = false;
				} else {
					headerComponent.setIcon(Inspector.opened);
					group.add(main);
					opened = true;
				}
				group.invalidate();
				group.validate();
				group.repaint();
				pack();
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				headerComponent.setBackground(Color.GRAY);
				headerComponent.setForeground(Color.WHITE);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				headerComponent.setBackground(Color.BLACK);
				headerComponent.setForeground(Color.LIGHT_GRAY);
			}
		});
		tab.add(group);
		invalidate();
		repaint();
		pack();
	}

	/**
	 * @param name
	 * @return
	 */
	public JPanel addTab(final String name) {
		final JPanel tab = new JPanel();
		tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
		tabbedPane.addTab(name, tab);
		return tab;
	}

	/**
	 * 
	 */
	public void restore() {
		if (tabbedPane.getParent() != null) {
			tabbedPane.getParent().remove(tabbedPane);
		}
		main.add(tabbedPane);
		setVisible(true);
		invalidate();
		repaint();
		pack();
	}

	abstract void minimized(SlickerTabbedPane tabbedPane);

}
