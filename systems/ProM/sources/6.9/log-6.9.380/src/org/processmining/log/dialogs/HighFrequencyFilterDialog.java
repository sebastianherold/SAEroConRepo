package org.processmining.log.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.HighFrequencyFilterParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class HighFrequencyFilterDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5152781639823196889L;

	public HighFrequencyFilterDialog(XLog eventLog, final HighFrequencyFilterParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30 } };
		setLayout(new TableLayout(size));
		
		add(new ClassifierPanel(eventLog.getClassifiers(), parameters), "0, 0");

		final NiceSlider frequenceThresholdSlider = SlickerFactory.instance().createNiceIntegerSlider("Frequency threshold", 0, 100,
				parameters.getFrequencyThreshold(), Orientation.HORIZONTAL);
		frequenceThresholdSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setFrequencyThreshold(frequenceThresholdSlider.getSlider().getValue());
			}
		});
		add(frequenceThresholdSlider, "0, 1");

		final NiceSlider distanceThresholdSlider = SlickerFactory.instance().createNiceIntegerSlider("Distance threshold", 0, 100,
				parameters.getDistanceThreshold(), Orientation.HORIZONTAL);
		distanceThresholdSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setDistanceThreshold(distanceThresholdSlider.getSlider().getValue());
			}
		});
		add(distanceThresholdSlider, "0, 2");
	}

}
