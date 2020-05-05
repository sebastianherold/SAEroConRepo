package org.processmining.basicutils.models;

import java.io.File;
import java.io.InputStream;

import org.processmining.framework.plugin.PluginContext;

public interface ObjectArray<K> {
	/**
	 * Initializes the object array.
	 */
	void init();

	/**
	 * Add the element to the array
	 * @param element
	 * @return the index of the element
	 */
	int addElement(K element);

	/**
	 * Remove the element from the array
	 * @param element
	 * @return
	 */
	int removeElement(K element);

	/**
	 * Add the element at the specific index
	 * @param index
	 * @param element
	 */
	void addElement(int index, K element);

	/**
	 * Remove the element at the specific index
	 * @param index
	 */
	void removeElement(int index);

	/**
	 * Return the element at the specific index
	 * @param index
	 * @return
	 */
	K getElement(int index);
	
	int getSize();
	
	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception;
	public void exportToFile(PluginContext context, File file) throws Exception;
}
