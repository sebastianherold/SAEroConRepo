package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;

import javax.swing.JPasswordField;
import javax.swing.text.Document;

import org.processmining.framework.util.ui.widgets.BorderPanel;
import org.processmining.framework.util.ui.widgets.WidgetColors;

/**
 * PasswordField with SlickerBox L&F
 * Built using the PromTextField as pattern
 * 
 * @author shernandez
 * 
 */
public class ProMPasswordField extends BorderPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPasswordField passwordField;

	/**
	 * 
	 */
	public ProMPasswordField() {
		super(15, 3);
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(WidgetColors.COLOR_LIST_BG);
		setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		passwordField = new JPasswordField();
		add(passwordField, BorderLayout.CENTER);
		passwordField.setBorder(null);
		passwordField.setOpaque(true);
		passwordField.setBackground(WidgetColors.COLOR_LIST_BG);
		passwordField.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		passwordField.setSelectionColor(WidgetColors.COLOR_LIST_SELECTION_BG);
		passwordField.setSelectedTextColor(WidgetColors.COLOR_LIST_SELECTION_FG);
		passwordField.setCaretColor(WidgetColors.COLOR_LIST_SELECTION_FG);
		setMinimumSize(new Dimension(200, 30));
		setMaximumSize(new Dimension(1000, 30));
		setPreferredSize(new Dimension(1000, 30));
	}

	/**
	 * @param initial
	 */
	public ProMPasswordField(final String initial) {
		this();
		setText(initial);
	}

	/**
	 * @param listener
	 */
	public void addActionListener(final ActionListener listener) {
		passwordField.addActionListener(listener);
	}

	/**
	 * @see java.awt.Component#addFocusListener(java.awt.event.FocusListener)
	 */
	@Override
	public void addFocusListener(final FocusListener listener) {
		passwordField.addFocusListener(listener);
	}

	/**
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(final KeyListener listener) {
		passwordField.addKeyListener(listener);
	}

	/**
	 * @return
	 */
	public Document getDocument() {
		return passwordField.getDocument();
	}

	/**
	 * @return
	 */
	public String getText() {
		return passwordField.getText();
	}

	/**
	 * @param text
	 */
	public void insertText(final String text) {
		final int position = passwordField.getCaretPosition();
		passwordField.setText(passwordField.getText().substring(0, position) + text + passwordField.getText().substring(position));
	}

	/**
	 * @param listener
	 */
	public void removeActionListener(final ActionListener listener) {
		passwordField.removeActionListener(listener);
	}

	/**
	 * @see java.awt.Component#removeFocusListener(java.awt.event.FocusListener)
	 */
	@Override
	public void removeFocusListener(final FocusListener listener) {
		passwordField.removeFocusListener(listener);
	}

	/**
	 * @param editable
	 */
	public void setEditable(final boolean editable) {
		passwordField.setEditable(editable);
	}

	/**
	 * @param text
	 */
	public void setText(final String text) {
		passwordField.setText(text);
	}

	/**
	 * @param isOkay
	 */
	public void visualizeStatus(final boolean isOkay) {
		final Color bg = isOkay ? WidgetColors.COLOR_LIST_BG : WidgetColors.COLOR_LIST_SELECTION_BG;
		super.setBackground(bg);
		passwordField.setBackground(bg);
		getParent().invalidate();
		getParent().repaint();
	}
}
