package org.processmining.contexts.uitopia.packagemanager;

import java.util.Set;

import org.processmining.framework.packages.PackageDescriptor;

public class PMPackage {

	public static enum PMStatus {
		TOUNINSTALL, TOUPDATE, TOINSTALL, DEAD
	};

	private final PackageDescriptor descriptor;
	private boolean isFavorite;
	private PMStatus status;

	public PMPackage(PackageDescriptor descriptor) {
		this.descriptor = descriptor;
		isFavorite = false;
		status = PMStatus.TOINSTALL;
	}

	public PackageDescriptor getDescriptor() {
		return descriptor;
	}

	public PMStatus getStatus() {
		return status;
	}

	public void setStatus(PMStatus status) {
		this.status = status;
	}

	public Set<String> getDependencies() {
		return descriptor.getDependencies();
	}

	public String getPackageName() {
		return descriptor.getName();
	}

	public String getAuthorName() {
		return descriptor.getAuthor();
	}

	public String getDescription() {
		return descriptor.getDescription();
	}

	public String getVersion() {
		return "Version " + descriptor.getVersion();
	}

	public String getLicense() {
		return descriptor.getLicense();
	}
	
	public boolean isFavorite() {
		return isFavorite;
	}

	public void setFavorite(boolean f) {
		isFavorite = f;
	}

//	public Image getPreview(int w, int h) {
//		try {
//			ImageIcon icon = PMIconCache.getIcon(this);
//			Image img = icon.getImage();
//			int width = icon.getIconWidth();
//			int height = icon.getIconHeight();
//			float xScale = w / (float) width;
//			float yScale = h / (float) height;
//			float scale = (xScale < yScale ? xScale : yScale);
//			return img.getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_SMOOTH);
//		} catch (MalformedURLException e) {
//		}
//		return null;
//	}
//
	public boolean equals(Object o) {
		if (o instanceof PMPackage) {
			return ((PMPackage) o).descriptor.equals(descriptor);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return descriptor.hashCode();
	}

}
