package org.processmining.contexts.uitopia.model;

import org.deckfour.uitopia.api.model.Category;
import org.processmining.framework.plugin.annotations.PluginCategory;

public class ProMCategory implements Category {

	private PluginCategory c;

	public ProMCategory(PluginCategory c){
		this.c = c;
	}
	public String getName() {
		return c.getName();
	}

	public String getDescription() {
		return c.getDescription();
	}

	public String getFilterImage() {
		return c.getImageFilterFilename();
	}
	
}
