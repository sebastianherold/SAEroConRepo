/*
 * SVG Salamander Copyright (c) 2004, Mark McKay All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Mark McKay can be contacted at mark@kitfox.com. Salamander and other projects
 * can be found at http://www.kitfox.com
 * 
 * Created on January 26, 2004, 5:25 PM
 */
package com.kitfox.svg;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kitfox.svg.xml.StyleAttribute;

// import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
/**
 * @author Mark McKay
 * @author <a href="mailto:mark@kitfox.com">Mark McKay</a>
 */
public class Path extends ShapeElement {

	public static final String TAG_NAME = "path";
	//    PathCommand[] commands = null;
	int fillRule = GeneralPath.WIND_NON_ZERO;
	String d = "";
	//    ExtendedGeneralPath path;
	GeneralPath path;

	/**
	 * Creates a new instance of Rect
	 */
	public Path() {
	}

	public String getTagName() {
		return TAG_NAME;
	}

	protected void build() throws SVGException {
		super.build();

		StyleAttribute sty = new StyleAttribute();

		String fillRuleStrn = (getStyle(sty.setName("fill-rule"))) ? sty.getStringValue() : "nonzero";
		fillRule = fillRuleStrn.equals("evenodd") ? GeneralPath.WIND_EVEN_ODD : GeneralPath.WIND_NON_ZERO;

		if (getPres(sty.setName("d"))) {
			d = sty.getStringValue();
		}

		path = buildPath(d, fillRule);
	}

	//inserted by Sander Leemans
	@Override
	void pick(Point2D point, boolean boundingBox, List retVec) throws SVGException {
		if (boundingBox) {
			//do nothing special, just call the super function
			super.pick(point, true, retVec);
			return;
		} else {
			//check whether we are on the stroke
			Shape strokeShape = getStrokeShape(new StyleAttribute(), getShape(), null);
			if (strokeShape.contains(point)) {
				//we are on the stroke; call super function to add stuff
				super.pick(point, true, retVec);
				return;
			}

			//check whether we are in the path
			GeneralPath closedPath = buildPath(closePath(d), fillRule);
			if (closedPath.contains(point)) {
				//delegate to super function to add stuff
				super.pick(point, true, retVec);
				return;
			}
		}
		return;
	}

	//inserted by Sander Leemans
	public Shape getStrokeShape(StyleAttribute styleAttrib, Shape shape, AffineTransform cacheXform)
			throws SVGException {

		float[] strokeDashArray = null;
		if (getStyle(styleAttrib.setName("stroke-dasharray"))) {
			strokeDashArray = styleAttrib.getFloatList();
			if (strokeDashArray.length == 0)
				strokeDashArray = null;
		}

		float strokeDashOffset = 0f;
		if (getStyle(styleAttrib.setName("stroke-dashoffset"))) {
			strokeDashOffset = styleAttrib.getFloatValueWithUnits();
		}

		int strokeLinecap = BasicStroke.CAP_BUTT;
		if (getStyle(styleAttrib.setName("stroke-linecap"))) {
			String val = styleAttrib.getStringValue();
			if (val.equals("round")) {
				strokeLinecap = BasicStroke.CAP_ROUND;
			} else if (val.equals("square")) {
				strokeLinecap = BasicStroke.CAP_SQUARE;
			}
		}

		int strokeLinejoin = BasicStroke.JOIN_MITER;
		if (getStyle(styleAttrib.setName("stroke-linejoin"))) {
			String val = styleAttrib.getStringValue();
			if (val.equals("round")) {
				strokeLinejoin = BasicStroke.JOIN_ROUND;
			} else if (val.equals("bevel")) {
				strokeLinejoin = BasicStroke.JOIN_BEVEL;
			}
		}

		float strokeMiterLimit = 4f;
		if (getStyle(styleAttrib.setName("stroke-miterlimit"))) {
			strokeMiterLimit = Math.max(styleAttrib.getFloatValueWithUnits(), 1);
		}

		float strokeOpacity = 1;
		if (getStyle(styleAttrib.setName("stroke-opacity"))) {
			strokeOpacity *= styleAttrib.getRatioValue();
		}

		if (strokeOpacity == 0f) {
			return null;
		}

		float strokeWidth = 1f;
		if (getStyle(styleAttrib.setName("stroke-width"))) {
			strokeWidth = styleAttrib.getFloatValueWithUnits();
		}
		//        if (strokeWidthScalar != 1f)
		//        {
		strokeWidth *= strokeWidthScalar;
		//        }

		BasicStroke stroke;
		if (strokeDashArray == null) {
			StrokeCacheIndex index = new StrokeCacheIndex();
			index.strokeWidth = strokeWidth;
			index.strokeLinecap = strokeLinecap;
			index.strokeLinejoin = strokeLinejoin;
			index.strokeMiterLimit = strokeMiterLimit;
			index.strokeDashArray = null;
			index.strokeDashOffset = Float.MIN_VALUE;
			stroke = strokeCache.get(index);
			if (stroke == null) {
				stroke = new BasicStroke(strokeWidth, strokeLinecap, strokeLinejoin, strokeMiterLimit);
				strokeCache.put(index, stroke);
			}
		} else {
			StrokeCacheIndex index = new StrokeCacheIndex();
			index.strokeWidth = strokeWidth;
			index.strokeLinecap = strokeLinecap;
			index.strokeLinejoin = strokeLinejoin;
			index.strokeMiterLimit = strokeMiterLimit;
			index.strokeDashArray = strokeDashArray;
			index.strokeDashOffset = strokeDashOffset;
			stroke = strokeCache.get(index);
			if (stroke == null) {
				stroke = new BasicStroke(strokeWidth, strokeLinecap, strokeLinejoin, strokeMiterLimit, strokeDashArray,
						strokeDashOffset);
				strokeCache.put(index, stroke);
			}
		}

		Shape strokeShape;
		if (vectorEffect == VECTOR_EFFECT_NON_SCALING_STROKE) {
			strokeShape = cacheXform.createTransformedShape(shape);
			strokeShape = stroke.createStrokedShape(strokeShape);
		} else {
			strokeShape = stroke.createStrokedShape(shape);
		}

		return strokeShape;
	}

	private static final Pattern pattern = Pattern.compile("-?(\\d*\\.)?\\d+,-?(\\d*\\.)?\\d+");

	public static String closePath(String path) {

		//get the points from the path
		Matcher matcher = pattern.matcher(path);

		List<String> points = new ArrayList<String>();
		while (matcher.find()) {
			points.add(matcher.group());
		}

		if (path.toLowerCase().endsWith("z")) {
			//the path is already closed; return the original path
			return path;
		} else if (points.get(0).equals(points.get(points.size() - 1))) {
			//the path begins and ends at the same points; it is already closed; return the original path
			return path;
		}

		//reverse the list of points
		Collections.reverse(points);

		//output as a new path
		StringBuilder result = new StringBuilder();
		Iterator<String> it = points.iterator();

		result.append("L");
		result.append(it.next());

		try {
			while (it.hasNext()) {
				result.append("C");
				result.append(it.next());
				result.append(" ");
				result.append(it.next());
				result.append(" ");
				result.append(it.next());
			}
		} catch (NoSuchElementException e) {
			return path;
		}

		return path + result.toString();
	}

	public void render(Graphics2D g) throws SVGException {
		beginLayer(g);
		renderShape(g, path);
		finishLayer(g);
	}

	public Shape getShape() {
		return shapeToParent(path);
	}

	public Rectangle2D getBoundingBox() throws SVGException {
		return boundsToParent(includeStrokeInBounds(path.getBounds2D()));
	}

	/**
	 * Updates all attributes in this diagram associated with a time event. Ie,
	 * all attributes with track information.
	 *
	 * @return - true if this node has changed state as a result of the time
	 *         update
	 */
	public boolean updateTime(double curTime) throws SVGException {
		//        if (trackManager.getNumTracks() == 0) return false;
		boolean changeState = super.updateTime(curTime);

		//Get current values for parameters
		StyleAttribute sty = new StyleAttribute();
		boolean shapeChange = false;

		if (getStyle(sty.setName("fill-rule"))) {
			int newVal = sty.getStringValue().equals("evenodd") ? GeneralPath.WIND_EVEN_ODD : GeneralPath.WIND_NON_ZERO;
			if (newVal != fillRule) {
				fillRule = newVal;
				changeState = true;
			}
		}

		if (getPres(sty.setName("d"))) {
			String newVal = sty.getStringValue();
			if (!newVal.equals(d)) {
				d = newVal;
				shapeChange = true;
			}
		}

		if (shapeChange) {
			build();
			//            path = buildPath(d, fillRule);
			//            return true;
		}

		return changeState || shapeChange;
	}
}
