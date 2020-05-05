package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Child that can be displayed in the ProMScrollContainer. To update the user
 * interface of a child, one can use the functions
 * 
 * - getTitlePanel() Returns the top panel to display a title of the child
 * 
 * - getContentPanel() Returns the panel in which one can add the real content.
 * This panel will be hidden when the min/max button is hit.
 * 
 * - setContentSize() Sets the size of the content panel when maximized.
 * 
 * @author jvdwerf
 * 
 */
public class ProMScrollContainerChild extends BorderPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -81027020923152280L;

	private JPanel buttonPanel;
	private JPanel contentPanel;

	private int contentSize = 100;
	private JButton deleteButton;
	private JButton minimizeButton;

	private boolean minimized = false;

	private final ProMScrollContainer parent;

	private JPanel topContentPanel;

	private JPanel topPanel;

	/**
	 * @param parent
	 */
	public ProMScrollContainerChild(final ProMScrollContainer parent) {
		this(parent, false);
	}

	/**
	 * @param parent
	 * @param startminimized
	 */
	public ProMScrollContainerChild(final ProMScrollContainer parent, final boolean startminimized) {
		this(parent, startminimized, true);
	}

	/**
	 * @param parent
	 * @param startminimized
	 * @param addDeleteButton
	 */
	public ProMScrollContainerChild(final ProMScrollContainer parent, final boolean startminimized,
			final boolean addDeleteButton) {
		super(10, 10);
		this.parent = parent;
		initComponents(addDeleteButton);

		setMinimized(startminimized);
	}

	/**
	 * @return
	 */
	public JPanel getContentPanel() {
		return contentPanel;
	}

	/**
	 * @return
	 */
	public int getContentSize() {
		return contentSize;
	}

	/**
	 * @return
	 */
	public JPanel getTitlePanel() {
		return topContentPanel;
	}

	/**
	 * @return
	 */
	public boolean isMinimized() {
		return minimized;
	}

	/**
	 * @see javax.swing.JComponent#revalidate()
	 */
	@Override
	public void revalidate() {
		super.revalidate();

		if (parent != null) {
			parent.revalidate();
		}
	}

	/**
	 * @param size
	 */
	public void setContentSize(final Dimension size) {
		//// lelijk :-S
		setContentSize((int) size.getHeight());
	}

	/**
	 * @param minimalContentSize
	 */
	public void setContentSize(final int minimalContentSize) {
		contentSize = minimalContentSize;

		updateHeights();
	}

	/**
	 * @param minimized
	 */
	public void setMinimized(final boolean minimized) {
		this.minimized = minimized;

		if (contentPanel != null && minimizeButton != null) {
			if (isMinimized()) {
				contentPanel.setVisible(false);
				minimizeButton.setText("+");
			} else {
				contentPanel.setVisible(true);
				minimizeButton.setText("-");
			}
		}

		revalidate();
	}

	private void initComponents(final boolean addDeleteButton) {

		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(WidgetColors.COLOR_LIST_BG);
		//this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setOpaque(false);

		topContentPanel = new JPanel();
		topContentPanel.setLayout(new BorderLayout());
		topContentPanel.setOpaque(false);

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.setOpaque(false);

		topPanel.add(topContentPanel, BorderLayout.WEST);

		minimizeButton = SlickerFactory.instance().createButton("-");
		minimizeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				toggleMinimized();
			}
		});

		if (addDeleteButton) {
			deleteButton = SlickerFactory.instance().createButton("x");
			deleteButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					removeMe();
				}
			});

			buttonPanel.add(deleteButton, BorderLayout.WEST);
		}

		buttonPanel.add(minimizeButton, BorderLayout.EAST);

		topPanel.add(buttonPanel, BorderLayout.EAST);

		this.add(topPanel, BorderLayout.NORTH);

		contentPanel = SlickerFactory.instance().createRoundedPanel(8, WidgetColors.COLOR_LIST_FG);
		contentPanel.setLayout(new BorderLayout());

		updateHeights();

		this.add(contentPanel, BorderLayout.CENTER);

	}

	private void removeMe() {
		parent.removeChild(this);
	}

	private void toggleMinimized() {
		setMinimized(!isMinimized());
	}

	private void updateHeights() {
		if (contentSize > contentPanel.getMaximumSize().getHeight()) {
			contentPanel.setMaximumSize(new Dimension(100, getContentSize()));
		}

		contentPanel.setPreferredSize(new Dimension(100, getContentSize()));
		contentPanel.setSize(new Dimension(100, getContentSize()));

		revalidate();

	}

}
