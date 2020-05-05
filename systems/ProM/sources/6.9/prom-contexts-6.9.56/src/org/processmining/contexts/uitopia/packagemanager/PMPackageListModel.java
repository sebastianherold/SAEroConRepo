package org.processmining.contexts.uitopia.packagemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

public class PMPackageListModel extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4483191311607087069L;

	private static final Comparator<PMPackage> COMP_PACKAGE_NAME = new Comparator<PMPackage>() {
		public int compare(PMPackage r1, PMPackage r2) {
			return r1.getPackageName().compareTo(r2.getPackageName());
		}
	};

	private static final Comparator<PMPackage> COMP_AUTHOR_NAME = new Comparator<PMPackage>() {
		public int compare(PMPackage r1, PMPackage r2) {
			return r1.getAuthorName().compareTo(r2.getAuthorName());
		}
	};

	private final List<? extends PMPackage> fullList;
	private List<? extends PMPackage> filteredList;
	private boolean filterFavorites;
	private Comparator<PMPackage> comparator;

	public PMPackageListModel(List<? extends PMPackage> packages) {
		fullList = packages;
		filteredList = new ArrayList<PMPackage>(packages);
		filterFavorites = false;
		comparator = PMPackageListModel.COMP_PACKAGE_NAME;
	}

	public void sortByPackageName() {
		comparator = PMPackageListModel.COMP_PACKAGE_NAME;
		updateList();
	}

	public void sortByAuthorName() {
		comparator = PMPackageListModel.COMP_AUTHOR_NAME;
		updateList();
	}

	public void setFilterFavorites(boolean isFiltered) {
		filterFavorites = isFiltered;
		updateList();
	}

	private void updateList() {
		filteredList = new ArrayList<PMPackage>(fullList);
		if (filterFavorites) {
			ArrayList<PMPackage> filtered = new ArrayList<PMPackage>();
			for (PMPackage r : filteredList) {
				if (r.isFavorite()) {
					filtered.add(r);
				}
			}
			filteredList = filtered;
		}
		Collections.sort(filteredList, comparator);
		fireContentsChanged(this, 0, getSize() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return filteredList.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return filteredList.size();
	}
}
