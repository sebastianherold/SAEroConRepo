package org.processmining.log.algorithms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityFilterParameters;

import com.fluxicon.slickerbox.components.DistributionUI;
import com.fluxicon.slickerbox.components.RoundedPanel;

public class LogCentralityVisualizerAlgorithm {

	protected Color colorEnclosureBg = new Color(250, 250, 250, 105);
	protected Color colorTitleFg = new Color(20, 20, 20, 230);
	protected Color colorInfoBg = new Color(60, 60, 60, 160);
	protected Color colorInfoBgMouseOver = new Color(60, 60, 60, 240);
	protected Color colorInfoLabel = new Color(210, 210, 210);
	protected Color colorInfoValue = new Color(255, 255, 255);

	private int factor = 100;

	public JComponent apply(LogCentrality centrality, LogCentralityFilterParameters parameters) {
		double sumCentrality = 0.0;
		int j = ((parameters == null) ? centrality.getCentralities().size() : (parameters.getPercentage() * centrality
				.getCentralities().size()) / 100);
		if (j >= centrality.getCentralities().size()) {
			j = centrality.getCentralities().size() - 1;
		}
		while ((j < centrality.getCentralities().size() - 1)
				&& (centrality.getCentralities().get(j) == centrality.getCentralities().get(j + 1))) {
			j++;
		}
		int left, right;
		if (parameters == null || parameters.isFilterIn()) {
			left = 0;
			right = j;
		} else {
			left = j + 1;
			right = centrality.getCentralities().size() - 1;
			if (left > right) {
				left = right;
			}
		}
		int minCentrality = (int) Math.round(factor * centrality.getCentralities().get(left));
		int maxCentrality = (int) Math.round(factor * centrality.getCentralities().get(right));
		int[] centralityArray = new int[right + 1 - left];
		for (int k = left; k <= right; k++) {
			Double value = centrality.getCentralities().get(k);
			centralityArray[k - left] = (int) Math.round(factor * value);
			sumCentrality += value;
		}
		int meanCentrality = (int) Math.round(factor * sumCentrality / (right + 1 - left));
		JComponent[] components = new JComponent[4];
		JComponent panel = getDistributionPanel("Trace happiness", centralityArray, meanCentrality, components);
		((JLabel) components[0]).setText("" + minCentrality / (double) factor);
		((JLabel) components[1]).setText("" + meanCentrality / (double) factor);
		((JLabel) components[2]).setText("" + maxCentrality / (double) factor);
		return panel;
	}

	private RoundedPanel getDistributionPanel(String title, int[] values, int meanValue, JComponent[] result) {

		// create distribution panel
		RoundedPanel instancePanel = new RoundedPanel(15, 0, 0);
		instancePanel.setBackground(colorEnclosureBg);
		instancePanel.setLayout(new BoxLayout(instancePanel, BoxLayout.Y_AXIS));
		instancePanel.add(getLeftAlignedHeader(title));
		instancePanel.add(Box.createVerticalStrut(6));
		if (values.length == 0) {
			return instancePanel;
		}
		RoundedPanel instanceDistPanel = new RoundedPanel(10, 0, 0);
		result[3] = instanceDistPanel;
		instanceDistPanel.setBackground(new Color(20, 20, 20));
		instanceDistPanel.setLayout(new BorderLayout());
		DistributionUI instanceDistUI = new DistributionUI(values);
		instanceDistPanel.add(instanceDistUI, BorderLayout.CENTER);
		JPanel keyPanel = new JPanel();
		keyPanel.setOpaque(false);
		keyPanel.setBorder(BorderFactory.createEmptyBorder());
		keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.X_AXIS));
		result[0] = packInfo(keyPanel, "Min");
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createHorizontalGlue());
		result[1] = packInfo(keyPanel, "Mean");
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createHorizontalGlue());
		result[2] = packInfo(keyPanel, "Max");
		instancePanel.add(instanceDistPanel);
		instancePanel.add(Box.createVerticalStrut(4));
		instancePanel.add(keyPanel);
		return instancePanel;
	}

	private JLabel packInfo(JPanel panel, String name) {
		String value = "Initializing ...";

		RoundedPanel packed = new RoundedPanel(10, 0, 0);
		packed.setBackground(colorInfoBg);
		final RoundedPanel target = packed;
		packed.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) { /* ignore */
			}

			public void mouseEntered(MouseEvent arg0) {
				target.setBackground(colorInfoBgMouseOver);
				target.repaint();
			}

			public void mouseExited(MouseEvent arg0) {
				target.setBackground(colorInfoBg);
				target.repaint();
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		});
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		JLabel nameLabel = new JLabel(name);
		nameLabel.setOpaque(false);
		nameLabel.setForeground(colorInfoLabel);
		nameLabel.setFont(nameLabel.getFont().deriveFont(12f));
		JLabel valueLabel = new JLabel(value);
		valueLabel.setOpaque(false);
		valueLabel.setForeground(colorInfoValue);
		valueLabel.setFont(valueLabel.getFont().deriveFont(14f));
		packed.add(Box.createHorizontalStrut(5));
		packed.add(nameLabel);
		packed.add(Box.createHorizontalGlue());
		packed.add(valueLabel);
		packed.add(Box.createHorizontalStrut(5));
		packed.revalidate();

		panel.add(packed);

		return valueLabel;
	}

	private JPanel getLeftAlignedHeader(String title) {
		JLabel hLabel = new JLabel(title);
		hLabel.setOpaque(false);
		hLabel.setForeground(colorTitleFg);
		hLabel.setFont(hLabel.getFont().deriveFont(15f));
		return alignLeft(hLabel);
	}

	private JPanel alignLeft(JComponent component) {
		JPanel hPanel = new JPanel();
		hPanel.setBorder(BorderFactory.createEmptyBorder());
		hPanel.setOpaque(false);
		hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.X_AXIS));
		hPanel.add(component);
		hPanel.add(Box.createHorizontalGlue());
		return hPanel;
	}

}
