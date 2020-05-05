package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JTextArea;

import com.fluxicon.slickerbox.components.RoundedPanel;

/**
 * TextAres with SlickerBox L&F
 * 
 * @author mwesterg
 * 
 */
public class ProMTextArea extends RoundedPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTextArea textArea;

	/**
	 * 
	 */
	public ProMTextArea() {
		super(10, 5, 0);
		textArea = new JTextArea();
		final ProMScrollPane logarea = new ProMScrollPane(textArea);
		textArea.setOpaque(true);
		textArea.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		textArea.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		textArea.setSelectedTextColor(WidgetColors.COLOR_LIST_SELECTION_FG);
		textArea.setSelectionColor(WidgetColors.COLOR_LIST_SELECTION_BG);
		textArea.setCaretColor(WidgetColors.COLOR_LIST_SELECTION_FG);

		setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(200, 100));
		setMaximumSize(new Dimension(1000, 1000));
		setPreferredSize(new Dimension(1000, 200));

		add(Box.createHorizontalStrut(5), BorderLayout.WEST);
		add(logarea, BorderLayout.CENTER);
		add(Box.createHorizontalStrut(5), BorderLayout.EAST);
	}

	/**
	 * @param editable
	 */
	public ProMTextArea(final boolean editable) {
		this();
		if (!editable) {
			setEditable(false);
			setLineWrap(true);
		}
	}

	/**
	 * @param text
	 */
	public void append(final String text) {
		textArea.append(text);
	}

	/**
	 * @return
	 */
	public int getLength() {
		return textArea.getDocument().getLength();
	}

	/**
	 * @return
	 */
	public boolean getLineWrap() {
		return textArea.getLineWrap();
	}

	/**
	 * @return
	 */
	public int getTabSize() {
		return textArea.getTabSize();
	}

	/**
	 * @return
	 */
	public String getText() {
		return textArea.getText();
	}

	/**
	 * 
	 */
	public void scrollToEnd() {
		setCaretPosition(getLength());
	}

	/**
	 * 
	 */
	public void selectAll() {
		textArea.selectAll();
	}

	/**
	 * @param position
	 */
	public void setCaretPosition(final int position) {
		textArea.setCaretPosition(position);
	}

	/**
	 * @param editable
	 */
	public void setEditable(final boolean editable) {
		textArea.setEditable(editable);
	}

	/**
	 * @param wrap
	 */
	public void setLineWrap(final boolean wrap) {
		textArea.setLineWrap(wrap);
		textArea.setWrapStyleWord(wrap);
	}

	/**
	 * @param size
	 */
	public void setTabSize(final int size) {
		textArea.setTabSize(size);
	}

	/**
	 * @param text
	 */
	public void setText(final String text) {
		textArea.setText(text);
	}

	public JTextArea getTextArea() {
		return textArea;
	}
}
