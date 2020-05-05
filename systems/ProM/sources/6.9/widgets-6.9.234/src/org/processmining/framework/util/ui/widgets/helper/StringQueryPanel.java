package org.processmining.framework.util.ui.widgets.helper;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMTextField;

class StringQueryPanel extends JPanel {

	private static final long serialVersionUID = -6547392010448275699L;

	private final ProMTextField textField;
	
	public StringQueryPanel(String queryText, String defaultValue) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		if (queryText != null) {
			add(new JLabel(queryText));
		}
		if (defaultValue != null) {
			textField = new ProMTextField(defaultValue);	
		} else {
			textField = new ProMTextField();
		}
		textField.setPreferredSize(null);
		add(textField);
	}
	
	public StringQueryPanel(String text) {
		this(text, null);
	}

	public StringQueryPanel() {
		this(null);
	}

	public InteractionResult getUserChoice(UIPluginContext context, String query) {
		return context.showConfiguration(query, this);
	}
	
	public InteractionResult getUserChoice(Component view, String query) {
		String[] options = new String[] { "Confirm", "Cancel" };
		int result = JOptionPane.showOptionDialog(view, this, query, JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		return result == 0 ? InteractionResult.CONTINUE : InteractionResult.CANCEL;
	}

	public String getResult() {
		return textField.getText();
	}

}