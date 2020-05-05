package org.processmining.framework.util.ui.widgets.helper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;

class MultipleChoiceQueryPanel<T> extends JPanel {

	private static final long serialVersionUID = -6547392010448275699L;

	private final ProMList<T> choiceField;

	public MultipleChoiceQueryPanel(String title, Iterable<T> choices) {
		super(new BorderLayout());
		DefaultListModel<T> listModel = new DefaultListModel<>();
		for (T c : choices) {
			listModel.addElement(c);
		}
		choiceField = new ProMList<>(title, listModel);
		choiceField.setPreferredSize(null);
		add(choiceField, BorderLayout.CENTER);
	}

	public InteractionResult getUserChoice(UIPluginContext context, String query) {
		return context.showConfiguration(query, this);
	}

	public List<T> getResult() {
		return choiceField.getSelectedValuesList();
	}

	public InteractionResult getUserChoice(Component view, String query) {
		String[] options = new String[]{ "Confirm", "Cancel" };
		int result = JOptionPane.showOptionDialog(view, this, query, JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		return result == 0 ? InteractionResult.CONTINUE : InteractionResult.CANCEL;  
	}

}