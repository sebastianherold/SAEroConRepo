package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapRed extends ColourMap {
	
	public Color colour(long weight, long maxWeight) {
		return ColourMaps.colourMapRed(weight, maxWeight);
	}
	
	public Color colour(double value) {
		return ColourMaps.colourMapRed(value);
	}

}
