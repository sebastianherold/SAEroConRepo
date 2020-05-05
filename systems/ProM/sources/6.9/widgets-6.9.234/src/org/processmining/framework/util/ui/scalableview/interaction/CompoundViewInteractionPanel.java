package org.processmining.framework.util.ui.scalableview.interaction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.processmining.framework.util.Pair;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class CompoundViewInteractionPanel extends JPanel implements ViewInteractionPanel, MouseMotionListener {
	protected final static int BUTTONHEIGHT = (ScalableViewPanel.TAB_HEIGHT * 5) / 6;
	protected final static int BUTTONWIDTH = (ScalableViewPanel.TAB_WIDTH * 5) / 6;

	private static final long serialVersionUID = -4871727332152661398L;
	private final String name;
	private JList list;
	private ScalableComponent scalable;
	private List<ViewInteractionPanel> panelList = new ArrayList<ViewInteractionPanel>();
	private Map<ViewInteractionPanel, Pair<JPanel, JPanel>> panels = new HashMap<ViewInteractionPanel, Pair<JPanel, JPanel>>();
	protected SlickerFactory factory = SlickerFactory.instance();
	private ScalableViewPanel parentPanel;
	private JScrollPane scroll;
	private CompoundListCellRenderer renderer;

	public CompoundViewInteractionPanel(String name) {
		super();
		setLayout(null);
		this.name = name;
		this.list = new JList(new DefaultListModel());
		renderer = new CompoundListCellRenderer(panels);
		list.setCellRenderer(renderer);

		list.setEnabled(false);

		list.addMouseMotionListener(this);
		this.scroll = new JScrollPane(list);
		SlickerDecorator.instance().decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);

		this.list.setOpaque(false);

		// add the title
		JLabel panelTitle = factory.createLabel(name);
		panelTitle.setHorizontalTextPosition(SwingConstants.CENTER);
		panelTitle.setVerticalTextPosition(SwingConstants.CENTER);
		panelTitle.setForeground(Color.WHITE);
		panelTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
		panelTitle.setOpaque(false);
		this.add(panelTitle, BorderLayout.NORTH);
		this.setOpaque(false);

		scroll.getViewport().setOpaque(false);
		scroll.setOpaque(false);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setOpaque(false);

		this.add(scroll);

		validate();
		repaint();

	}

	public void mouseDragged(MouseEvent e) {

	}

	private ViewInteractionPanel oldPanel = null;

	public synchronized void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		if (e.getComponent() == scroll) {
			Point p2 = scroll.getViewport().getViewPosition();
			p.setLocation(p.getX() - p2.getX(), p.getY() - p2.getY());
		}
		if (oldPanel != null) {
			oldPanel.willChangeVisibility(false);
			JPanel panelOn = panels.get(oldPanel).getFirst();
			JPanel panelOff = panels.get(oldPanel).getSecond();
			panelOn.setEnabled(false);
			panelOn.setVisible(false);
			panelOff.setEnabled(true);
			panelOff.setVisible(true);
			oldPanel = null;

		}
		int x = p.x;
		int y = p.y;
		if (x < 00 || x > BUTTONWIDTH + 50) {
			return;
		}
		int index = y / (BUTTONHEIGHT);
		if (index >= list.getModel().getSize()) {
			return;
		}
		Point offset = CompoundViewInteractionPanel.this.getParent().getLocation();
		ViewInteractionPanel panel = (ViewInteractionPanel) list.getModel().getElementAt(index);

		JPanel panelOn = panels.get(panel).getFirst();
		JPanel panelOff = panels.get(panel).getSecond();

		parentPanel.setSize(panel, panelOn, panelOff);
		panelOn.setPreferredSize(new Dimension(panel.getComponent().getWidth() + 20,
				panel.getComponent().getHeight() + 20));
		int w = panel.getComponent().getWidth() + 20;
		int h = panel.getComponent().getHeight() + 20;
		panelOn.setSize(new Dimension(w, h));
		int l_x = offset.x + BUTTONWIDTH + 40;
		int l_y = offset.y + 10 + index * BUTTONHEIGHT;
		if ((l_x + w > parentPanel.getWidth()) && (offset.x + 10 >= w)) {
			l_x = offset.x - w + 10;

		}
		if ((l_y + h > parentPanel.getHeight()) && (parentPanel.getHeight() >= h)) {
			l_y = parentPanel.getHeight() - h;
		}

		panelOn.setLocation(l_x, l_y);

		oldPanel = panel;
		panel.willChangeVisibility(true);
		panelOn.setEnabled(true);
		panelOn.setVisible(true);
		panelOff.setEnabled(false);
		panelOff.setVisible(false);

	}

	public void updated() {
		for (ViewInteractionPanel p : panelList) {
			p.updated();
		}
	}

	public String getPanelName() {
		return name;
	}

	public JComponent getComponent() {
		return this;
	}

	public void setScalableComponent(ScalableComponent scalable) {
		this.scalable = scalable;
		for (ViewInteractionPanel p : panelList) {
			p.setScalableComponent(scalable);
		}
	}

	public void setParent(ScalableViewPanel parent) {
		parentPanel = parent;
		for (ViewInteractionPanel p : panelList) {
			p.setParent(parent);
			parentPanel.add(panels.get(p).getFirst(), JLayeredPane.MODAL_LAYER);
		}
	}

	public double getHeightInView() {
		return BUTTONHEIGHT * length();
	}

	public double getWidthInView() {
		return BUTTONWIDTH + 10;
	}

	public void willChangeVisibility(boolean to) {
		if (to) {
			scroll.setBounds(0, 0, BUTTONWIDTH + 30, getHeight());
			list.setSize(list.getPreferredSize());
			invalidate();
			repaint();
		} else {
			if (oldPanel != null) {
				// hmm, moved out of the list and out of the panel:
				JPanel panelOn = panels.get(oldPanel).getFirst();
				JPanel panelOff = panels.get(oldPanel).getSecond();
				panelOn.setEnabled(false);
				panelOn.setVisible(false);
				panelOff.setEnabled(true);
				panelOff.setVisible(true);
				oldPanel = null;
			}
		}
	}

	public void addViewInteractionPanel(ViewInteractionPanel panel) {
		JPanel panelOn = factory.createRoundedPanel(15, Color.LIGHT_GRAY);
		JPanel panelOff = factory.createRoundedPanel(15, Color.DARK_GRAY);
		panelOn.setLayout(null);
		panelOff.setLayout(null);

		panelOn.add(panel.getComponent());
		panelOn.setVisible(false);
		panelOn.setEnabled(false);
		panelOff.setVisible(true);
		panelOff.setEnabled(true);
		panelOff.setToolTipText(panel.getPanelName());
		JLabel panelTitle = factory.createLabel(panel.getPanelName());
		panelTitle.setHorizontalTextPosition(SwingConstants.CENTER);
		panelTitle.setVerticalTextPosition(SwingConstants.CENTER);
		panelTitle.setForeground(Color.WHITE);
		panelTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		panelTitle.setBounds(10, 0, BUTTONWIDTH, BUTTONHEIGHT);

		panelOff.add(panelTitle);
		//		panelOff.setBounds(0, 0, 120, BUTTONHEIGHT);
		panelOff.setPreferredSize(new Dimension(BUTTONWIDTH, BUTTONHEIGHT));

		panel.getComponent().setLocation(10, 10);

		panels.put(panel, new Pair<JPanel, JPanel>(panelOn, panelOff));
		panelList.add(panel);
		((DefaultListModel) list.getModel()).addElement(panel);

		panelOn.setLocation(0, 0);
		if (parentPanel != null) {
			parentPanel.add(panelOn, JLayeredPane.MODAL_LAYER);
			panel.setParent(parentPanel);
		}
		if (scalable != null) {
			panel.setScalableComponent(scalable);
		}
		//		this.add(panelOff, JLayeredPane.DRAG_LAYER);

		panel.updated();
	}

	public int length() {
		return panelList.size();
	}
}

class CompoundListCellRenderer extends DefaultListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -808355468668630456L;
	private final Map<ViewInteractionPanel, Pair<JPanel, JPanel>> panels;

	public CompoundListCellRenderer(Map<ViewInteractionPanel, Pair<JPanel, JPanel>> panels) {
		this.panels = panels;
	}

	public Component getListCellRendererComponent(JList component, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		ViewInteractionPanel panel = (ViewInteractionPanel) value;
		JPanel off = panels.get(panel).getSecond();
		return off;
	}
}
