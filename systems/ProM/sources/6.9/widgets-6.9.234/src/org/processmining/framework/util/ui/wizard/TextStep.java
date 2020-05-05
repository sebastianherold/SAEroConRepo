package org.processmining.framework.util.ui.wizard;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.framework.util.ui.widgets.WidgetColors;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author michael
 * 
 * @param <M>
 */
public class TextStep<M> extends InformationStep<M> {

	/**
	 * @param title
	 * @param text
	 * @return
	 */
	public static <T> TextStep<T> create(final String title, final String text) {
		return new TextStep<T>(title, text);
	}

	private final String text;

	/**
	 * @param title
	 * @param text
	 */
	public TextStep(final String title, final String text) {
		super(title);

		this.text = text;
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizardStep#getComponent(java.lang.Object)
	 */
	@Override
	public JComponent getComponent(final Object model) {
		final JLabel label = SlickerFactory.instance().createLabel(text);
		final RoundedPanel panel = new RoundedPanel(15, 0, 3);

		panel.setOpaque(true);
		panel.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.CENTER);

		return panel;
	}

}
