package org.processmining.framework.util.ui.widgets;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;

import com.fluxicon.slickerbox.components.RoundedPanel;

/**
 * List (JList) with SlickerBox L&F
 * 
 * @author mwesterg
 * @param <E>
 * 
 */
public class ProMList<E> extends RoundedPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("rawtypes")
	private final JList/* <E> */jList;

	/**
	 * @param title
	 */
	@SuppressWarnings("rawtypes")
	public ProMList(final String title) {
		super(10, 5, 0);
		jList = new JList/* <E> */();
		setup(title);
	}

	/**
	 * @param title
	 * @param providers
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ProMList(final String title, final ListModel/* <E> */providers) {
		super(10, 5, 0);
		jList = new JList/* <E> */(providers);
		setup(title);
	}

	/**
	 * @param l
	 */
	public void addListSelectionListener(final ListSelectionListener l) {
		jList.addListSelectionListener(l);
	}

	/**
	 * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void addMouseListener(final MouseListener l) {
		jList.addMouseListener(l);
	}

	/**
	 * @return
	 */
	@Deprecated
	public Object[] getSelectedValues() {
		return jList.getSelectedValues();
	}

	/**
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<E> getSelectedValuesList() {
		// Major fuck you to Hudson using an antiquated version of Java
		try {
			return (List<E>) JList.class.getMethod("getSelectedValuesList").invoke(this);
		} catch (final Exception e) {
			return (List<E>) Arrays.asList(jList.getSelectedValues());
		}
	}

	/**
	 * @param l
	 */
	public void removeListSelectionListener(final ListSelectionListener l) {
		jList.removeListSelectionListener(l);
	}

	/**
	 * @see java.awt.Component#removeMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void removeMouseListener(final MouseListener l) {
		jList.removeMouseListener(l);
	}

	/**
	 * @param index
	 */
	public void setSelectedIndex(final int index) {
		jList.setSelectedIndex(index);
	}

	public void setSelectedIndices(final int[] index) {
		jList.setSelectedIndices(index);
	}

	/**
	 * @param selectedValues
	 */
	public void setSelection(final Iterable<Object> selectedValues) {
		jList.clearSelection();
		for (final Object value : selectedValues) {
			jList.setSelectedValue(value, true);
		}
	}

	/**
	 * @param selectedValues
	 */
	public void setSelection(final Object... selectedValues) {
		jList.clearSelection();
		for (final Object value : selectedValues) {
			jList.setSelectedValue(value, true);
		}
	}

	/**
	 * @param selectionMode
	 */
	public void setSelectionMode(final int selectionMode) {
		jList.setSelectionMode(selectionMode);
	}
	
	@SuppressWarnings("rawtypes") // this class is still java 6 :(
	public JList getList() {
		return jList;
	}

	private void setup(final String title) {
		jList.setBackground(WidgetColors.COLOR_LIST_BG);
		jList.setForeground(WidgetColors.COLOR_LIST_FG);
		jList.setSelectionBackground(WidgetColors.COLOR_LIST_SELECTION_BG);
		jList.setSelectionForeground(WidgetColors.COLOR_LIST_SELECTION_FG);

		final ProMScrollPane scroller = new ProMScrollPane(jList);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		final JLabel providersLabel = new JLabel(title);
		providersLabel.setOpaque(false);
		providersLabel.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		providersLabel.setFont(providersLabel.getFont().deriveFont(13f));
		providersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		providersLabel.setHorizontalAlignment(SwingConstants.CENTER);
		providersLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(providersLabel);
		add(Box.createVerticalStrut(8));
		add(scroller);
		setMinimumSize(new Dimension(200, 100));
		setMaximumSize(new Dimension(1000, 1000));
		setPreferredSize(new Dimension(1000, 200));
	}
}
