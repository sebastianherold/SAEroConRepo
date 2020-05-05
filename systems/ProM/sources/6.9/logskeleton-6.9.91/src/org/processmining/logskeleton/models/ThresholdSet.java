package org.processmining.logskeleton.models;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class ThresholdSet implements Set<String> {

	private Map<String, Integer> countMap;
	private Map<String, Integer> totalMap;
	private int threshold;
	
	public ThresholdSet(Collection<String> set, int threshold) {
		countMap = new HashMap<String, Integer>();
		totalMap = new HashMap<String, Integer>();
		for (String activity : set) {
			countMap.put(activity, 0);
			totalMap.put(activity, 0);
		}
		this.threshold = threshold; 
	}

	public int size() {
		int size = 0;
		for (String activity : countMap.keySet()) {
			if (contains(activity)) {
				size++;
			}
		}
		return size;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean contains(Object o) {
		return (countMap.get(o) * 100 >= totalMap.get(o) * threshold);
	}

	public Iterator<String> iterator() {
		Set<String> set = new HashSet<String>();
		for (String activity : countMap.keySet()) {
			if (contains(activity)) {
				set.add(activity);
			}
		}
		return set.iterator();
	}
	
	public Object[] toArray() {
		// Not supported
		return null;
	}
	
	public <String> String[] toArray(String[] a) {
		// Not supported
		return null;
	}
	
	public boolean add(String e) {
		// Not supported
		return false;
	}
	
	public boolean remove(Object o) {
		// Not supported
		return false;
	}
	
	public boolean containsAll(Collection<?> c) {
		Set<String> set = new HashSet<String>();
		for (String activity : countMap.keySet()) {
			if (contains(activity)) {
				set.add(activity);
			}
		}
		return set.containsAll(c);
	}
	
	public boolean addAll(Collection<? extends String> c) {
		return retainAll(c);
	}
	
	public boolean retainAll(Collection<?> c) {
		for (String activity : countMap.keySet()) {
			if (c.contains(activity)) {
				countMap.put(activity, countMap.get(activity) + 1);
			}
			totalMap.put(activity, totalMap.get(activity) + 1);
		}
		return true;
	}
	
	public boolean removeAll(Collection<?> c) {
		for (String activity : countMap.keySet()) {
			if (c.contains(activity)) {
				countMap.put(activity, -Math.abs(countMap.get(activity))); 
			}
		}
		return true;
	}
	
	public void clear() {
		// Not supported		
	}

	public void reset() {
		for (String activity : countMap.keySet()) {
			countMap.put(activity, Math.abs(countMap.get(activity)));
		}
	}
	
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	
	public int getThreshold() {
		return threshold;
	}
	
	public int getMaxThreshold(Object o) {
		return countMap.get(o) * 100 / totalMap.get(o);
	}
	
	public void exportToFile(CsvWriter writer) throws IOException {
		writer.write("" + threshold);
		for (String activity : countMap.keySet()) {
			writer.write("" + activity);
			writer.write("" + countMap.get(activity));
			writer.write("" + totalMap.get(activity));
		}
	}
	
	public void importFromFile(CsvReader reader) throws IOException {
		threshold = Integer.valueOf(reader.get(1));
		for (int column = 2; column + 2 < reader.getColumnCount(); column += 3) {
			String activity = reader.get(column);
			int count = Integer.valueOf(reader.get(column + 1));
			int total = Integer.valueOf(reader.get(column + 2));
			countMap.put(activity, count);
			totalMap.put(activity, total);
		}
	}
}
