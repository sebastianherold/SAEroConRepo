package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapFixedOpacity extends ColourMap {
	private final ColourMap base;
	private final int opacity;

	public ColourMapFixedOpacity(ColourMap base, float opacity) {
		this.base = base;
		this.opacity = (int) (opacity * 255);
	}

	public Color colour(long weight, long maxWeight) {
		Color colour = base.colour(weight, maxWeight);
		return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), opacity);
	}

	public Color colour(double value) {
		Color colour = base.colour(value);
		return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), opacity);
	}

}
