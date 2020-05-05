package org.processmining.framework.util.ui.scalableview.interaction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ZoomInteractionPanel extends JPanel implements ViewInteractionPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8415559591750873766L;
	private final JSlider slider;
	private JLabel sliderMinValue, sliderMaxValue;
	JButton sliderFitValue;
	JLabel sliderValue;

	protected int fitZoom;
	protected ScalableComponent scalable;
	protected final ScalableViewPanel panel;
	protected boolean recalculateFit = true;

	public ZoomInteractionPanel(ScalableViewPanel panel, int maximumZoom) {

		super(null);
		this.panel = panel;

		this.slider = SlickerFactory.instance().createSlider(1);

		this.slider.setMinimum(1);
		this.slider.setMaximum(maximumZoom);
		this.slider.setValue(fitZoom);

		this.slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				update();
			}
		});

		this.sliderMinValue = SlickerFactory.instance().createLabel("0%");
		this.sliderMaxValue = SlickerFactory.instance().createLabel(maximumZoom + "%");
		this.sliderFitValue = SlickerFactory.instance().createButton("Fit >");
		this.sliderFitValue.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fit();
			}
		});
		this.sliderValue = SlickerFactory.instance().createLabel(fitZoom + "%");

		this.sliderMinValue.setHorizontalAlignment(SwingConstants.CENTER);
		this.sliderMaxValue.setHorizontalAlignment(SwingConstants.CENTER);
		this.sliderFitValue.setHorizontalAlignment(SwingConstants.RIGHT);
		this.sliderValue.setHorizontalAlignment(SwingConstants.LEFT);

		this.sliderMinValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		this.sliderMaxValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		this.sliderFitValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		this.sliderValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));

		this.sliderMinValue.setForeground(Color.GRAY);
		this.sliderMaxValue.setForeground(Color.GRAY);
		this.sliderFitValue.setForeground(Color.GRAY);
		this.sliderValue.setForeground(Color.DARK_GRAY);

		this.add(this.slider);
		this.add(this.sliderMinValue);
		this.add(this.sliderMaxValue);
		this.add(this.sliderFitValue);
		this.add(this.sliderValue);

		this.setBackground(Color.LIGHT_GRAY);
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
		setHeight(height);
	}

	public void setHeight(int height) {

		int sliderHeight = height - 60;

		// this.title.setBounds(0, (int) (height * 0.5) - 25, 30, 50);

		this.slider.setBounds(35, 30, 30, sliderHeight);
		this.sliderMaxValue.setBounds(0, 10, 100, 20);
		this.sliderMinValue.setBounds(0, height - 30, 100, 20);

		int value = this.slider.getValue();
		int span = this.slider.getMaximum() - this.slider.getMinimum();
		int position = 33 + (int) ((float) (this.slider.getMaximum() - this.fitZoom) / (float) span * (sliderHeight - 28));
		this.sliderFitValue.setBounds(0, position, 40, 20);

		if (value == this.fitZoom) {
			this.sliderValue.setBounds(65, position, 60, 20);
		} else {
			position = 33 + (int) ((float) (this.slider.getMaximum() - value) / (float) span * (sliderHeight - 28));
			this.sliderValue.setBounds(65, position, 60, 20);
		}
	}

	private void update() {

		int value = this.slider.getValue();

		int span = this.slider.getMaximum() - this.slider.getMinimum();
		int position = 33 + (int) ((float) (this.slider.getMaximum() - value) / (float) span * (this.slider.getBounds().height - 28));

		this.sliderValue.setText(value + "%");
		this.sliderValue.setBounds(65, position, 60, 20);

		scalable.setScale(getZoomValue());

	}

	public double getZoomValue() {
		return this.slider.getValue() / 100.;
	}

	public void setValue(int value) {
		this.slider.setValue(value);
	}

	public void setFitValue(int value) {

		this.fitZoom = value;

		int span = this.slider.getMaximum() - this.slider.getMinimum();
		int position = (int) (33 + Math.floor(((float) (this.slider.getMaximum() - value) / (float) span * (this.slider
				.getBounds().height - 28))));
		this.sliderFitValue.setBounds(0, position, 40, 20);
	}

	public void fit() {
		setValue(fitZoom);
	}

	public void setScalableComponent(ScalableComponent scalable) {
		this.scalable = scalable;
	}

	public void setParent(ScalableViewPanel parent) {

	}

	public JComponent getComponent() {
		return this;
	}

	public int getPosition() {
		return SwingConstants.WEST;
	}

	public String getPanelName() {
		return "Zoom";
	}

	public void updated() {
		recalculateFit = true;
	}

	public double getHeightInView() {
		return 0.66;
	}

	public double getWidthInView() {
		return 100;
	}

	protected void computeFitScale() {
		double scale = scalable.getScale();
		Dimension b = scalable.getComponent().getPreferredSize();
		double w = b.getWidth() / scale;
		double h = b.getHeight() / scale;
		double rx = panel.getViewport().getExtentSize().getWidth() / w;
		double ry = panel.getViewport().getExtentSize().getHeight() / h;

		setFitValue((int) (Math.min(rx, ry) * 100));
		recalculateFit = false;
	}

	public void willChangeVisibility(boolean to) {
		setValue((int) Math.floor(100 * scalable.getScale()));
		if (fitZoom <= 0 || recalculateFit) {
			computeFitScale();
		}
	}
}
