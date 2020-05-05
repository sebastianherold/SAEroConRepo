package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;


public class ColourMapBlue extends ColourMap {

	public Color colour(long weight, long maxWeight) {
		return ColourMaps.colourMapBlue(weight, maxWeight);
	}
	
	public Color colour(double value) {
		return ColourMaps.colourMapBlue(value);
	}
}
