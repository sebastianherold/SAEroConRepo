package org.processmining.log.models;

import org.processmining.framework.util.HTMLToString;

public interface LogFrequency extends HTMLToString {

	public void add(int frequency);

	public int get(int index);
}
