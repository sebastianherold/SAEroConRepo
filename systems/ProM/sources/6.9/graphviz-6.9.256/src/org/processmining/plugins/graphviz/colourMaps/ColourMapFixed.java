package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapFixed extends ColourMap {
	private Color colour;

	public ColourMapFixed(Color colour) {
		this.colour = colour;
	}

	public Color colour(long weight, long maxWeight) {
		return colour;
	}

	public Color colour(double value) {
		return colour;
	}
}
