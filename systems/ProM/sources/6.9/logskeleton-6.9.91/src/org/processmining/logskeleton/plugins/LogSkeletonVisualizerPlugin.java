package org.processmining.logskeleton.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;

import com.fluxicon.slickerbox.components.SlickerButton;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

@Plugin(name = "Log Skeleton Visualizer", parameterLabels = { "Event Log" }, returnLabels = { "Log Skeleton Visualizer" }, returnTypes = { JComponent.class }, userAccessible = true, help = "Log Skeleton Visualizer")
@Visualizer
public class LogSkeletonVisualizerPlugin {

	private UIPluginContext context;
	private XLog log;
	private XEventClassifier classifier;
	private JComponent mainPanel;
	private JComponent bottomPanel = null;
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public JComponent run(UIPluginContext context, XLog log) {
		this.context = context;
		this.log = log;
		
		mainPanel = new JPanel();
		double size[][] = { { TableLayoutConstants.FILL }, { 30, TableLayoutConstants.FILL } };
		mainPanel.setLayout(new TableLayout(size));
		mainPanel.setOpaque(false);

		classifier = new LogSkeletonClassifier();

		mainPanel.add(getControlPanel(), "0, 0");

		update();

		return mainPanel;
	}
	
	private void update() {
		LogSkeletonFilterBrowserPlugin filterBrowser = new LogSkeletonFilterBrowserPlugin();
		if (bottomPanel != null) {
			mainPanel.remove(bottomPanel);
		}
		bottomPanel = filterBrowser.run(context, log, classifier);
		mainPanel.add(bottomPanel, "0, 1");
		mainPanel.validate();
		mainPanel.repaint();
	}
	
	private JComponent getControlPanel() {
		JPanel controlPanel = new JPanel();
		double size[][] = { { 250, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		controlPanel.setLayout(new TableLayout(size));
		controlPanel.setOpaque(false);
		
		String[] keys = classifier.getDefiningAttributeKeys();
		String text = "";
		String sep = "";
		for (int i = 0; i < keys.length; i++) {
			text += sep + keys[i];
			sep = " ";
		}
		final ProMTextField input = new ProMTextField(text);
		controlPanel.add(input, "1, 0");

		final SlickerButton button = new SlickerButton("Apply Classifier");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String keys[] = input.getText().split(" ");
				classifier = new LogSkeletonClassifier(new XEventAttributeClassifier("classifier", keys));
				update();
			}

		});
		controlPanel.add(button, "0, 0");

		return controlPanel;
	}
}
