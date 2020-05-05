package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapWhiteRed extends ColourMap {
	public Color colour(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;
		return new Color(1, x, x);
	}

	public Color colour(double value) {
		return new Color(1, (float) value, (float) value);
	}
}
