package org.processmining.plugins.graphviz.visualisation.listeners;

import java.awt.geom.AffineTransform;

public interface ImageTransformationChangedListener {
	public void imageTransformationChanged(AffineTransform image2user, AffineTransform user2image);
}
