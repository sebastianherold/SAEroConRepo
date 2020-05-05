package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * TextField with SlickerBox L&F
 * 
 * @author mwesterg
 * @author F. Mannhardt (hints & expose underlying {@link JTextField})
 * 
 */
public class ProMTextField extends BorderPanel {

	private final class JTextFieldWithHint extends JTextField implements FocusListener {
		
		private static final long serialVersionUID = -4392477239934637206L;
		
		private String hint;
		private boolean hintVisible = true;

		public JTextFieldWithHint() {
			super();
			super.addFocusListener(this);
		}

		@Override
		public void focusGained(FocusEvent e) {
			hintVisible = false;
			repaint();
		}

		@Override
		public void focusLost(FocusEvent e) {
			hintVisible = true;
			repaint();
		}

		public void setHint(String hint) {
			this.hint = hint;
		}
		
		@Override
	    public void paint(Graphics g) {
	        super.paint(g);
	        if (getText().length() == 0 && hintVisible && hint != null && !hint.isEmpty()) {
	            int h = getHeight();
	            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	            Insets ins = getInsets();
	            FontMetrics fm = g.getFontMetrics();
	            int c0 = getBackground().getRGB();
	            int c1 = getForeground().getRGB();
	            int m = 0xfefefefe;
	            int c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1);
	            g.setColor(new Color(c2, true));
	            g.drawString(hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
	        }
	    }

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final JTextFieldWithHint textField;
	
	/**
	 * 
	 */
	public ProMTextField() {
		super(15, 3);
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(WidgetColors.COLOR_LIST_BG);
		setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		textField = new JTextFieldWithHint();
		add(textField, BorderLayout.CENTER);
		textField.setBorder(null);
		textField.setOpaque(true);
		textField.setBackground(WidgetColors.COLOR_LIST_BG);
		textField.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		textField.setSelectionColor(WidgetColors.COLOR_LIST_SELECTION_BG);
		textField.setSelectedTextColor(WidgetColors.COLOR_LIST_SELECTION_FG);
		textField.setCaretColor(WidgetColors.COLOR_LIST_SELECTION_FG);
		setMinimumSize(new Dimension(200, 30));
		setMaximumSize(new Dimension(1000, 30));
		setPreferredSize(new Dimension(1000, 30));
	}

	/**
	 * @param initial
	 */
	public ProMTextField(final String initial) {
		this();
		setText(initial);
	}

	/**
	 * @param initial
	 * @param hint
	 *            displayed in light gray font
	 */
	public ProMTextField(final String initial, final String hint) {
		this();
		setText(initial);
		setHint(hint);
	}

	/**
	 * @param listener
	 */
	public void addActionListener(final ActionListener listener) {
		textField.addActionListener(listener);
	}

	/**
	 * @see java.awt.Component#addFocusListener(java.awt.event.FocusListener)
	 */
	@Override
	public void addFocusListener(final FocusListener listener) {
		textField.addFocusListener(listener);
	}

	/**
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(final KeyListener listener) {
		textField.addKeyListener(listener);
	}

	/**
	 * @return
	 */
	public Document getDocument() {
		return textField.getDocument();
	}

	/**
	 * @return
	 */
	public String getText() {
		return textField.getText();
	}

	/**
	 * @param text
	 */
	public void insertText(final String text) {
		final int position = textField.getCaretPosition();
		textField.setText(textField.getText().substring(0, position) + text + textField.getText().substring(position));
	}

	/**
	 * @param listener
	 */
	public void removeActionListener(final ActionListener listener) {
		textField.removeActionListener(listener);
	}

	/**
	 * @see java.awt.Component#removeFocusListener(java.awt.event.FocusListener)
	 */
	@Override
	public void removeFocusListener(final FocusListener listener) {
		textField.removeFocusListener(listener);
	}

	/**
	 * @param editable
	 */
	public void setEditable(final boolean editable) {
		textField.setEditable(editable);
	}

	/**
	 * @param text
	 */
	public void setText(final String text) {
		textField.setText(text);
	}

	/**
	 * @param isOkay
	 */
	public void visualizeStatus(final boolean isOkay) {
		final Color bg = isOkay ? WidgetColors.COLOR_LIST_BG : WidgetColors.COLOR_LIST_SELECTION_BG;
		super.setBackground(bg);
		textField.setBackground(bg);
		getParent().invalidate();
		getParent().repaint();
	}

	/**
	 * @return get underlying {@link JTextField}
	 */
	public JTextField getTextField() {
		return textField;
	}

	/**
	 * @param hint
	 *            gray text that appears when text field is empty
	 */
	public void setHint(String hint) {
		textField.setHint(hint);
	}

}
