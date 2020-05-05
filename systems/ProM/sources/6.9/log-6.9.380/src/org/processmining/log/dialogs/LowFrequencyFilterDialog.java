package org.processmining.log.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.LowFrequencyFilterParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class LowFrequencyFilterDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1217093745565528989L;

	public LowFrequencyFilterDialog(XLog eventLog, final LowFrequencyFilterParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30 } };
		setLayout(new TableLayout(size));
		
		add(new ClassifierPanel(eventLog.getClassifiers(), parameters), "0, 0");

		final NiceSlider thresholdSlider = SlickerFactory.instance().createNiceIntegerSlider("Frequency threshold", 0, 100,
				parameters.getThreshold(), Orientation.HORIZONTAL);
		thresholdSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setThreshold(thresholdSlider.getSlider().getValue());
			}
		});
		add(thresholdSlider, "0, 1");
	}
}
