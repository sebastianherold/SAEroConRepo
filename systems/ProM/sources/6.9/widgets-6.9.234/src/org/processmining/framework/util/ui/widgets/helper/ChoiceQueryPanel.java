package org.processmining.framework.util.ui.widgets.helper;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMComboBox;

class ChoiceQueryPanel<T> extends JPanel {

	private static final long serialVersionUID = -6547392010448275699L;

	private final ProMComboBox<T> choiceField;

	public ChoiceQueryPanel(Iterable<T> choices) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		choiceField = new ProMComboBox<>(choices);
		choiceField.setPreferredSize(null);
		add(choiceField);
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

	@SuppressWarnings("unchecked")
	public T getResult() {
		return (T) choiceField.getModel().getSelectedItem();
	}

}