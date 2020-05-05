package org.processmining.framework.util.ui.widgets.helper;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMTextArea;

public class MultilineStringQueryPanel extends JPanel {

	private static final long serialVersionUID = -8701706429341608153L;
	
	private final ProMTextArea textArea;
	
	public MultilineStringQueryPanel(String text) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		textArea = new ProMTextArea();
		if (text != null) {
			textArea.setText(text);
		}
		textArea.setPreferredSize(null);
		add(textArea);
	}

	public MultilineStringQueryPanel() {
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
		return textArea.getText();
	}

	
}
