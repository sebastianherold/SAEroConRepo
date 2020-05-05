package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

/**
 * Interpolates a list of colours.
 * 
 * @author sander
 *
 */
public abstract class ColourMapInterpolate extends ColourMap {

	protected abstract float[][] getData();

	public Color colour(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;
		return colour(x);
	}

	public Color colour(double x) {
		float[][] data = getData();
		int indexBelow = (int) Math.floor((data.length - 1) * x);
		int indexAbove = (int) Math.ceil((data.length - 1) * x);
		if (indexBelow == indexAbove) {
			return new Color(data[indexAbove][0], data[indexAbove][1], data[indexAbove][2]);
		}

		float r = interpolate(data[indexBelow][0], data[indexAbove][0], indexBelow, indexAbove, (data.length - 1) * x);
		float g = interpolate(data[indexBelow][1], data[indexAbove][1], indexBelow, indexAbove, (data.length - 1) * x);
		float b = interpolate(data[indexBelow][2], data[indexAbove][2], indexBelow, indexAbove, (data.length - 1) * x);
		return new Color(r, g, b);
	}

	public float interpolate(float fromY, float toY, float fromX, float toX, double X) {
		return (float) (fromY - ((fromY - toY) * (fromX - X) / (fromX - toX)));
	}
}
