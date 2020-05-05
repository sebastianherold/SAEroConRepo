package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMapLightBlue extends ColourMap {

	public Color colour(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * 0.25f) + 0.1f;
		return new Color(1 - x, 1 - x, 1);
	}
	
	public Color colour(double x) {
		x = (x * 0.25f) + 0.1f;
		return new Color((float) (1 - x), (float) (1 - x), 1);
	}

}
