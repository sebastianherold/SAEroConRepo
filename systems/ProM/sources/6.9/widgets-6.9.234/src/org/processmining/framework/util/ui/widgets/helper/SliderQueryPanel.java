package org.processmining.framework.util.ui.widgets.helper;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class SliderQueryPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final NiceDoubleSlider slider;

	public SliderQueryPanel(String queryText, double min, double max, double defaultValue) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		if (queryText != null) {
			add(new JLabel(queryText));
		}
		slider = SlickerFactory.instance().createNiceDoubleSlider(queryText, min, max, defaultValue,
				Orientation.HORIZONTAL);
		add(slider);
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

	public double getResult() {
		return slider.getValue();
	}

}
