package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapGreen extends ColourMap {

	public Color colour(long weight, long maxWeight) {
		return ColourMaps.colourMapGreen(weight, maxWeight);
	}

	public Color colour(double value) {
		return ColourMaps.colourMapGreen(value);
	}

}
