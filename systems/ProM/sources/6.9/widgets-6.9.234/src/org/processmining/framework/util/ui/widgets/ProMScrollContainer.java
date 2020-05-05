package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * This class implements a Scroll Container. In the container, childs with a
 * minimize / maximize button can be added. In this way, one can group
 * properties.
 * 
 * Furthermore, childs can be added during run time.
 * 
 * @author jvdwerf
 * 
 */
public class ProMScrollContainer extends BorderPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3112303906580355823L;

	private final List<ProMScrollContainerChild> children = new ArrayList<ProMScrollContainerChild>();

	private JPanel scrollablePanel;

	private JScrollPane scrollPane;

	/**
	 * 
	 */
	public ProMScrollContainer() {
		this(10, 10);

	}

	/**
	 * @param size
	 * @param borderWidth
	 */
	public ProMScrollContainer(final int size, final int borderWidth) {
		super(size, borderWidth);

		initComponents();
	}

	/**
	 * @param child
	 */
	public void addChild(final ProMScrollContainerChild child) {
		addChild(child, children.size());
	}

	/**
	 * @param child
	 * @param index
	 */
	public void addChild(final ProMScrollContainerChild child, final int index) {
		children.add(child);

		if (children.size() > 0) {
			scrollablePanel.add(Box.createVerticalStrut(3));
		}

		scrollablePanel.add(child, index);
		scrollablePanel.revalidate();

		revalidate();
	}

	/**
	 * 
	 */
	public void clearChildren() {
		for (final ProMScrollContainerChild child : children) {
			scrollablePanel.remove(child);
		}

		children.clear();

		revalidate();
	}

	/**
	 * @param i
	 * @return
	 */
	public ProMScrollContainerChild getChild(final int i) {
		return children.get(i);
	}

	/**
	 * @return
	 */
	public List<ProMScrollContainerChild> getChildren() {
		return children;
	}

	/**
	 * @param child
	 */
	public void removeChild(final ProMScrollContainerChild child) {
		scrollablePanel.remove(child);
		children.remove(child);

		revalidate();
	}

	/**
	 * @param show
	 */
	public void showScrollbar(final boolean show) {
		if (show) {
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		} else {
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		}
	}

	private void initComponents() {

		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		setOpaque(true);
		setBackground(WidgetColors.COLOR_ENCLOSURE_BG);

		setLayout(new BorderLayout());

		scrollablePanel = new ProMScrollablePanel();
		scrollablePanel.setOpaque(true);
		scrollablePanel.setBackground(WidgetColors.COLOR_LIST_FG);
		scrollablePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		scrollablePanel.setLayout(new BoxLayout(scrollablePanel, BoxLayout.Y_AXIS));

		scrollPane = new ProMScrollPane(scrollablePanel);

		this.add(scrollPane, BorderLayout.CENTER);

		revalidate();
	}
}
