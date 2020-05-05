package org.processmining.contexts.uitopia.packagemanager;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.processmining.framework.packages.PackageDescriptor;

public class PMIconCache {

	/*
	 * Cache the retrieved icons. Prevents unnecessary access over the network.
	 */
	private static Map<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();
	private static Map<String, ImageIcon> iconPreviewMap = new HashMap<String, ImageIcon>();

	public static ImageIcon getIcon(PackageDescriptor pack) {
		synchronized (iconMap) {
			/*
			 * Check whether icon already in cache
			 */
			if (iconMap.containsKey(pack.getLogoURL())) {
				/*
				 * Yes, it is. Return cached icon.
				 */
				return iconMap.get(pack.getLogoURL());
			}
			/*
			 * No, it is not. Retrieve icon and put in cache.
			 */
			System.out.println("[PMIconCache] Retrieving icon for URL " + pack.getLogoURL());
			ImageIcon icon = null;
			try {
				URL logoURL = new URL(pack.getLogoURL());
				icon = new ImageIcon(logoURL);
			} catch (MalformedURLException e) {
				System.err.println("[PMIconCache] Retrieving icon for URL " + pack.getLogoURL() + " failed: "
						+ e.getMessage());
			}
			iconMap.put(pack.getLogoURL(), icon);
			return icon;
		}
	}

	public static ImageIcon getIcon(PMPackage pack) throws MalformedURLException {
		return getIcon(pack.getDescriptor());
	}

	public static ImageIcon getIconPreview(PackageDescriptor pack) {
		synchronized (iconPreviewMap) {
			/*
			 * Check whether icon preview already in cache
			 */
			ImageIcon icon = iconPreviewMap.get(pack.getLogoURL());
			if (icon != null) {
				/*
				 * Yes, it is. Return cached icon preview.
				 */
				return icon;
			}
			/*
			 * No, it is not. Get icon preview and put in cache.
			 */
			Image image = getPreview(getIcon(pack), 150, 150);
			if (image != null) {
				icon = new ImageIcon(image);
				iconPreviewMap.put(pack.getLogoURL(), icon);
			}
			return icon;
		}
	}

	public static ImageIcon getIconPreview(PMPackage pack) {
		return getIconPreview(pack.getDescriptor());
	}

	public static Image getPreview(ImageIcon icon, int w, int h) {
		if (icon != null) {
			Image img = icon.getImage();
			int width = icon.getIconWidth();
			int height = icon.getIconHeight();
			float xScale = w / (float) width;
			float yScale = h / (float) height;
			float scale = (xScale < yScale ? xScale : yScale);
			return img.getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_SMOOTH);
		}
		return null;
	}
}
