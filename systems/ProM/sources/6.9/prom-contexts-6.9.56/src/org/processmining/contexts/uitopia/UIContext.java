package org.processmining.contexts.uitopia;

import java.util.Date;
import java.util.List;

import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.uitopia.api.hub.CategoryManager;
import org.deckfour.uitopia.api.hub.FrameworkHub;
import org.deckfour.uitopia.api.model.Author;
import org.deckfour.uitopia.ui.UITopiaController;
import org.deckfour.uitopia.ui.UITopiaFrame;
import org.processmining.contexts.uitopia.hub.ProMActionManager;
import org.processmining.contexts.uitopia.hub.ProMCategoryManager;
import org.processmining.contexts.uitopia.hub.ProMResourceManager;
import org.processmining.contexts.uitopia.hub.ProMTaskManager;
import org.processmining.contexts.uitopia.hub.ProMViewManager;
import org.processmining.contexts.uitopia.model.ProMAction;
import org.processmining.contexts.uitopia.model.ProMPOResource;
import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.contexts.uitopia.model.ProMTask;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.impl.AbstractGlobalContext;

public class UIContext extends AbstractGlobalContext implements
		FrameworkHub<ProMAction, ProMTask, ProMResource<?>, ProMPOResource> {

	private final UIPluginContext mainPluginContext;
	private UITopiaFrame frame;
	private ProMResourceManager resourceManager;
	private ProMActionManager actionManager;
	private ProMTaskManager taskManager;
	private ProMViewManager viewManager;
	private CategoryManager categoryManager;
	private UITopiaController controller;

	public UIContext() {
		super();
		mainPluginContext = new UIPluginContext(this, "Main Plugin Context");

	}

	public void initialize() {
		resourceManager = ProMResourceManager.initialize(this);
		taskManager = ProMTaskManager.initialize(this);
		actionManager = ProMActionManager.initialize(this);
		viewManager = ProMViewManager.initialize(this);
		categoryManager = ProMCategoryManager.initialize(this);
	}

	public void setFrame(UITopiaFrame frame) {
		this.frame = frame;
	}

	public CategoryManager getCategoryManager() {
		return categoryManager;
	}

	@Override
	public UIPluginContext getMainPluginContext() {
		return mainPluginContext;
	}

	@Override
	public Class<? extends PluginContext> getPluginContextType() {
		return UIPluginContext.class;
	}

	public List<Author> getFrameworkAuthors() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getFrameworkReleaseDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFrameworkVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public ProMResourceManager getResourceManager() {
		return resourceManager;
	}

	public ProMActionManager getActionManager() {
		return actionManager;
	}

	public ProMTaskManager getTaskManager() {
		return taskManager;
	}

	public ProMViewManager getViewManager() {
		return viewManager;
	}

	public void shutdown(TaskListener arg0) {
	}

	public void startup() {
		// Nothing to do (since (de)serialization was abandoned).
	}

	public UITopiaFrame getUI() {
		return frame;
	}

	public UITopiaController getController() {
		return controller;
	}

	public void setController(UITopiaController controller) {
		this.controller = controller;
	}

}
