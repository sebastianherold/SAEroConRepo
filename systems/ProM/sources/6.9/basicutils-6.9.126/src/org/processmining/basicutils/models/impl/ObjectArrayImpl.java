package org.processmining.basicutils.models.impl;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.processmining.basicutils.models.ObjectArray;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;

import com.csvreader.CsvReader;

public abstract class ObjectArrayImpl<K> implements ObjectArray<K>  {
	protected List<K> list = new ArrayList<K>();
	
	public void init() {
		list = new ArrayList<K>();
	}

	public int addElement(K element) {
		list.add(element);
		return list.size() - 1;
	}

	public int removeElement(K element) {
		int ret = list.indexOf(element);
		list.remove(element);
		return ret;
	}

	public void addElement(int index, K element) {
		list.add(index, element);
	}

	public void removeElement(int index) {
		list.remove(index);
	}

	public K getElement(int index) {
		return list.get(index);
	}

	public int getSize() {
		return list.size();
	}
	
	@SuppressWarnings("unchecked")
	public void importFromStream(PluginContext context, InputStream input, String parent, AbstractImportPlugin importer) throws Exception {
		Reader streamReader = new InputStreamReader(input);
		CsvReader csvReader = new CsvReader(streamReader);
		init();
		while (csvReader.readRecord()) {
			String fileName = csvReader.get(0);
			if (parent != null && fileName.indexOf(File.separator) == -1) {
				fileName = parent + File.separator + fileName;
			}
			System.out.println("Importing element " + fileName);
			K element = (K) importer.importFile(context, fileName);
			this.addElement(element);
		}
		csvReader.close();
	}
}
