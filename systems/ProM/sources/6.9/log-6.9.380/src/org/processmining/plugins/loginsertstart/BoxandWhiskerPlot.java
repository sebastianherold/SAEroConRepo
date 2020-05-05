package org.processmining.plugins.loginsertstart;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

/**
 * Box- and Whisker Plot class
 * 
 * @author jnakatumba
 * 
 */

public class BoxandWhiskerPlot {

	private static Color colorBg = new Color(120, 120, 120);

	private ChartPanel chartPanel;
	private final UIPluginContext context;
	private final Set<String> resNamesList;
	private final Set<String> resourceList;
	private String resourceName;
	private final List<Pair<String, List<Double>>> resListPair;
	private List<Double> serviceTimes;
	private final List<Pair<String, Long>> resDurationList;

	private Double maxValue = 0.0;
	private String rChoice;

	private boolean bLegend = true;

	public BoxandWhiskerPlot(UIPluginContext context, List<Pair<String, Long>> resDurationLists, Set<String> resNames) {
		this.context = context;
		resDurationList = resDurationLists;
		resNamesList = resNames;

		resListPair = new ArrayList<Pair<String, List<Double>>>();
		serviceTimes = new ArrayList<Double>();

		resourceList = new TreeSet<String>();
		getOutliers();

		if (resourceList.size() > 40) {
			bLegend = false;
		} else {
			bLegend = true;
		}
		constructBoxandWhisker();

	}

	private void getOutliers() {
		Object[] reList = resNamesList.toArray();
		for (int resIndex = 0; resIndex < resNamesList.size(); resIndex++) {
			resourceName = (String) reList[resIndex];
			getPerResource();
		}
	}

	private void getPerResource() {
		List<Double> valuesList = new ArrayList<Double>();
		Double dValue = 0.0;
		for (int i = 0; i < resDurationList.size(); i++) {
			Pair<String, Long> currentPair = resDurationList.get(i);
			String rName = currentPair.getFirst();
			if (resourceName.equals(rName)) {
				long value = currentPair.getSecond();
				dValue = Double.valueOf(value);
				valuesList.add(dValue);
				resourceList.add(rName);

			}

			if (dValue > maxValue) {
				maxValue = dValue;
			}
		}
		Pair<String, List<Double>> resPair = new Pair<String, List<Double>>(resourceName, valuesList);
		resListPair.add(resPair);

	}

	@SuppressWarnings("deprecation")
	private void constructBoxandWhisker() {

		DefaultBoxAndWhiskerCategoryDataset datasets = createSampleDataset();

		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart("Box and Whisker Chart", "Service Time",
				"Resource Names", datasets, true);

		chart.setBackgroundPaint(new Color(249, 231, 236));

		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));

		CategoryAxis xAxis = new CategoryAxis("Resources");
		NumberAxis yAxis = new NumberAxis("Service Time (minutes)");
		yAxis.setAutoRangeIncludesZero(false);
		BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
		renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		CategoryPlot plot = new CategoryPlot(datasets, xAxis, yAxis, renderer);

		chart = new JFreeChart(null, new Font("SansSerif", Font.BOLD, 14), plot, bLegend);
		chartPanel = new ChartPanel(chart);
		JScrollPane scrollPane = new JScrollPane(chartPanel);
		chartPanel.setBackground(colorBg);
		scrollPane.setBackground(colorBg);

		AddStPanel stPanel = new AddStPanel(context, chartPanel, maxValue);
		rChoice = stPanel.getChoice();

	}

	private DefaultBoxAndWhiskerCategoryDataset createSampleDataset() {
		serviceTimes = new ArrayList<Double>();
		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		for (int i = 0; i < resListPair.size(); i++) {
			Pair<String, List<Double>> resList = resListPair.get(i);
			String rName = resList.getFirst();
			serviceTimes = resList.getSecond();
			dataset.add(serviceTimes, rName, "");

		}

		return dataset;

	}

	public String getOutlierRange() {
		return rChoice;
	}

}
