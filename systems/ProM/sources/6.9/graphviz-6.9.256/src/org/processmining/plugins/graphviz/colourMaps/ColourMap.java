package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public abstract class ColourMap {
	public abstract Color colour(long weight, long maxWeight);

	public abstract Color colour(double value);

	@Deprecated
	public Color colour3(long weight, long min, long max) {
		if (max == min) {
			return colour(weight, weight);
		}
		return colour(weight - min, max - min);
	}

	public Color colour(long weight, long min, long max) {
		if (max == min) {
			return colour(weight, weight);
		}
		return colour(weight - min, max - min);
	}

	public Color colour(double weight, double min, double max) {
		if (max == min) {
			return colour(1);
		}
		double x = (weight - min) / (max-min);
		
		return colour(x);
	}

	public String colourString(long weight, long min, long max) {
		return toHexString(colour(weight, min, max));
	}

	public static String toHexString(Color colour) {
		return "#" + toHex(colour.getRed()) + toHex(colour.getGreen()) + toHex(colour.getBlue());
	}

	public static String toHex(int i) {
		String s = Integer.toHexString(i);
		if (s.length() == 2) {
			return s;
		} else {
			return "0" + s;
		}
	}
}
