package org.processmining.framework.util.ui.widgets;

import java.awt.Color;

/**
 * Some nice color schemes for visualization. Mostly taken from colorbrewer2.org
 * under Apache 2.0 license.
 * 
 * @author F. Mannhardt
 *
 */
public enum ColorScheme {

	BLACK(new Color[] { Color.BLACK }, Color.BLACK),

	COLOR_BREWER_5CLASS_SET1(new Color[] { new Color(228, 26, 28), new Color(55, 126, 184), new Color(77, 175, 74),
			new Color(152, 78, 163), new Color(255, 127, 0) }, Color.GRAY), //

	COLOR_BREWER_9CLASS_SET1(new Color[] { new Color(228, 26, 28), new Color(55, 126, 184), new Color(77, 175, 74),
			new Color(152, 78, 163), new Color(255, 127, 0), new Color(255, 255, 51), new Color(166, 86, 40),
			new Color(247, 129, 191), new Color(153, 153, 153) }, Color.LIGHT_GRAY), //

	COLOR_BREWER_9CLASS_SET21(new Color[] { new Color(141, 211, 199), new Color(255, 255, 179),
			new Color(190, 186, 218), new Color(251, 128, 114), new Color(128, 177, 211), new Color(253, 180, 98),
			new Color(179, 222, 105), new Color(252, 205, 229), new Color(217, 217, 217) }, Color.LIGHT_GRAY), //

	COLOR_BREWER_12CLASS_PAIRED(new Color[] { new Color(166, 206, 227), new Color(31, 120, 180),
			new Color(178, 223, 138), new Color(51, 160, 44), new Color(251, 154, 153), new Color(227, 26, 28),
			new Color(253, 191, 111), new Color(255, 127, 0), new Color(202, 178, 214), new Color(106, 61, 154),
			new Color(255, 255, 153), new Color(177, 89, 40) }, Color.GRAY), //
	
	SEQ_PALETTE_YlOrRd(new Color[] {
			new Color(255, 255, 229), new Color(255, 255, 204), new Color(255, 237, 160), new Color(254, 217, 118), new Color(254, 178, 76),
			new Color(253, 141, 60), new Color(252, 78, 42), new Color(227, 26, 28), new Color(189, 0, 38),
			new Color(128, 0, 38)}, Color.GRAY);

	private final Color[] scheme;
	private final Color defaultColor;

	private ColorScheme(Color[] scheme, Color defaultColor) {
		this.scheme = scheme;
		this.defaultColor = defaultColor;
	}

	public Color[] getColors() {
		return scheme;
	}

	public Color getColor(int index) {
		return getColor(index, defaultColor);
	}

	public Color getColor(int index, Color defaultColor) {
		if (index >= scheme.length) {
			return defaultColor;
		}
		return scheme[index];
	}
	
	public Color getColorFromGradient(float factor) {
		return getColorFromGradient(factor, defaultColor);
	}
	
	public Color getColorFromGradient(float factor, Color defaultColor) {
		return getColorFromGradient(factor, scheme, defaultColor);
	}

	public static Color getColorFromGradient(float factor, Color[] colorScheme, Color defaultColor) {
		if (factor >= 1.0) {
			// Special case
			return defaultColor;
		}
		float bucketSize = 1.0f / colorScheme.length;
		int maxIndex = colorScheme.length-1;
		int minIndex = 0;
		int bucket = Math.min(maxIndex, Math.max(minIndex, (int) Math.floor(factor / bucketSize)));
		return colorScheme[bucket];
	}

}