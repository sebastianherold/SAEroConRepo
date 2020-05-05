package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapBlackBody extends ColourMap {

	public Color colour(long weight, long maxWeight) {
		return ColourMaps.colourMapBlackBody(weight, maxWeight);
	}

	public Color colour(double value) {
		return ColourMaps.colourMapBlackBody(value);
	}

}
