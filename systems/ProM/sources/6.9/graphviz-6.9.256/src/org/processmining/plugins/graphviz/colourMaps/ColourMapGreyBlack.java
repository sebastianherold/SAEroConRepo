package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapGreyBlack extends ColourMap {

	public Color colour(long weight, long maxWeight) {
		return ColourMaps.colourMapGreyBlack(weight, maxWeight);
	}

	public Color colour(double value) {
		return ColourMaps.colourMapGreyBlack(value);
	}

}
