package org.processmining.log.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.log.algorithms.LogCentralityVisualizerAlgorithm;
import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityFilterParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class LogCentralityFilterDialog extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1184378045381402638L;

	private JComponent holder;
	
	public LogCentralityFilterDialog(final UIPluginContext context, final LogCentrality centrality, final LogCentralityFilterParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30 } };
		setLayout(new TableLayout(size));

		holder = new JPanel();
		add(holder, "0, 0");
		double holderSize[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		holder.setLayout(new TableLayout(holderSize));
		holder.add((new LogCentralityVisualizerAlgorithm()).apply(centrality, parameters), "0, 0");
		
		final NiceSlider percSlider = SlickerFactory.instance().createNiceIntegerSlider("Select Happy Percentage", 0, 100,  parameters.getPercentage(),
				Orientation.HORIZONTAL);
		percSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setPercentage(percSlider.getSlider().getValue());
				holder.removeAll();
				holder.add((new LogCentralityVisualizerAlgorithm()).apply(centrality, parameters), "0, 0");
				revalidate();
				repaint();
			}
		});
		add(percSlider, "0, 1");

		final JCheckBox filterInBox = SlickerFactory.instance().createCheckBox("Select if Happy (otherwise unhappy)",
				false);
		filterInBox.setSelected(parameters.isFilterIn());
		filterInBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setFilterIn(filterInBox.isSelected());
				holder.removeAll();
				holder.add((new LogCentralityVisualizerAlgorithm()).apply(centrality, parameters), "0, 0");
				revalidate();
				repaint();
			}

		});
		filterInBox.setOpaque(false);
		add(filterInBox, "0, 2");
		
	}
}
