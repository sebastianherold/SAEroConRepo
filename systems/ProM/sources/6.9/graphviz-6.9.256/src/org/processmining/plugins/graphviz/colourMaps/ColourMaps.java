package org.processmining.plugins.graphviz.colourMaps;

import java.awt.Color;

public class ColourMaps {
	public static Color colourMapBlackBody(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		/*
		 * //blue-yellow x = (x * (float) 0.5) + (float) 0.5; return new
		 * Color(x, x, 1-x);
		 */
		x = (x * (float) 0.75) + (float) 0.25;

		//black-body
		return new Color(Math.min(Math.max((1 - x) * 3, 0), 1),
				Math.min(Math.max((((1 - x) - 1 / (float) 3) * 3), 0), 1),
				Math.min(Math.max((((1 - x) - 2 / (float) 3) * 3), 0), 1));
	}

	public static Color colourMapBlackBody(double x) {
		/*
		 * //blue-yellow x = (x * (float) 0.5) + (float) 0.5; return new
		 * Color(x, x, 1-x);
		 */
		x = (x * (float) 0.75) + (float) 0.25;

		//black-body
		return new Color((float) (Math.min(Math.max((1 - x) * 3, 0), 1)),
				(float) (Math.min(Math.max((((1 - x) - 1 / 3) * 3), 0), 1)),
				(float) (Math.min(Math.max((((1 - x) - 2 / 3) * 3), 0), 1)));
	}

	public static Color colourMapRed(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.5) + (float) 0.5;
		return new Color(1, 1 - x, 1 - x);
	}

	public static Color colourMapRed(double x) {
		x = (x * 0.5) + 0.5;
		return new Color(1, (float) (1 - x), (float) (1 - x));
	}

	public static Color colourMapGreen(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.75) + (float) 0.25;
		return new Color(1 - x, 1, 1 - x);
	}

	public static Color colourMapGreen(double x) {
		x = (x * 0.75) + 0.25;
		return new Color((float) (1 - x), 1, (float) (1 - x));
	}

	public static Color colourMapBlue(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.75) + (float) 0.25;
		return new Color(1 - x, 1 - x, 1);
	}

	public static Color colourMapBlue(double x) {
		x = (x * 0.75) + 0.25;
		return new Color((float) (1 - x), (float) (1 - x), 1);
	}

	public static Color colourMapGreyBlack(long weight, long maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.6) + (float) 0.4;
		return new Color(1 - x, 1 - x, 1 - x);
	}

	public static Color colourMapGreyBlack(double x) {
		x = (x * 0.6) + 0.4;
		return new Color((float) (1 - x), (float) (1 - x), (float) (1 - x));
	}

	public static double getLuma(Color colour) {
		int R = colour.getRed();
		int G = colour.getGreen();
		int B = colour.getBlue();
		return 0.299 * R + 0.587 * G + 0.114 * B;
	}
}
