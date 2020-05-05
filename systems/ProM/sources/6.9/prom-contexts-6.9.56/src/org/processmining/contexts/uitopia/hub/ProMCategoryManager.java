package org.processmining.contexts.uitopia.hub;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.uitopia.api.hub.CategoryManager;
import org.deckfour.uitopia.api.model.Category;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.model.ProMCategory;
import org.processmining.framework.plugin.annotations.PluginCategory;

public class ProMCategoryManager implements CategoryManager {

	private static ProMCategoryManager instance = null;
	private List<Category> categories;

	public ProMCategoryManager() {
		categories = new ArrayList<>();

		for (PluginCategory c : PluginCategory.values()) {
			ProMCategory newCat = new ProMCategory(c);
			categories.add(newCat);
		}

	}

	public List<Category> getCategories() {

		return categories;
	}

	public static CategoryManager initialize(UIContext uiContext) {
		if(instance == null){
			instance = new ProMCategoryManager();
		}
		return instance;
	}

}