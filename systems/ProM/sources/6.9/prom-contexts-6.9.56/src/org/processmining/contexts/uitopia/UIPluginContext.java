package org.processmining.contexts.uitopia;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.uitopia.api.model.ActionType;
import org.processmining.contexts.uitopia.model.ProMTask;
import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.impl.AbstractPluginContext;

public class UIPluginContext extends AbstractPluginContext {

	/*
	 * The field UIPluginContext.PREF is never read locally private static final
	 * String PREF = "guiplugincontext";
	 */
	private final Executor executor;
	private ProMTask task;

	UIPluginContext(GlobalContext context, String label) {
		super(context, label);
		// This context is NOT a child of another context,
		// hence should behave in an asynchronous way.
		executor = Executors.newCachedThreadPool();

		//TODO: Setup progressbar
		// progress = new GUIProgressBar(this);

	}

	protected UIPluginContext(UIPluginContext context, String label) {
		super(context, label);

		// TODO: Setup Progress
		// progress = new GUIProgressBar(this);

		// This context is a child of another context,
		// hence should behave in a synchronous way.
		if (context/*.getParentContext()*/ == null) {
			// this context is on the first level below the user-initiated
			// plugins
			executor = Executors.newCachedThreadPool();
		} else {
			// all subtasks take the pool of the parent.
			executor = context.getExecutor();
		}

		setTask(context.task);
	}

	@Override
	protected synchronized PluginContext createTypedChildContext(String label) {
		return new UIPluginContext(this, label);
	}

	public Executor getExecutor() {
		return executor;
	}

	/*
	 * [HV] The method getPreferences() from the type UIPluginContext is never
	 * used locally
	 */
	@SuppressWarnings("unused")
	private Preferences getPreferences() {
		return Preferences.userNodeForPackage(UIPluginContext.class);
	}

	/*
	 * [HV] The declared exception IOException is not actually thrown by the
	 * method openFile(FileFilter) from type UIPluginContext
	 */
	@SuppressWarnings("unused")
	public File openFile(final FileFilter filter) throws IOException {
		// TODO
		return null;
	}

	/*
	 * [HV] The declared exception IOException is not actually thrown by the
	 * method saveFile(String, String...) from type UIPluginContext
	 */
	@SuppressWarnings("unused")
	public File saveFile(String defaultExtension, String... extensions) throws IOException {
		// TODO
		return null;
	}

	/*
	 * [HV] The declared exception IOException is not actually thrown by the
	 * method openFiles(FileFilter) from type UIPluginContext
	 */
	@SuppressWarnings("unused")
	public File[] openFiles(FileFilter filter) throws IOException {
		// TODO
		return null;
	}

	public Progress getProgress() {
		return progress;
	}

	public UIContext getGlobalContext() {
		return (UIContext) super.getGlobalContext();
	}

	public UIPluginContext getRootContext() {
		return (UIPluginContext) super.getRootContext();
	}

	public void setTask(ProMTask task) {
		this.task = task;
	}

	/**
	 * Return the task being executed in this PluginContext; useful for registering new resources with this task.
	 * @return the task
	 */
	public ProMTask getTask() {
		return task;
	}

	public InteractionResult showConfiguration(String title, JComponent configuration) {
		if (task == null) {
			return InteractionResult.CANCEL;
		}
		assert (task.getAction().getType() == ActionType.INTERACTIVE);
		return task.showConfiguration(title, configuration);
	}

	public InteractionResult showWizard(String title, boolean first, boolean last, JComponent configuration) {
		if (task == null) {
			return InteractionResult.CANCEL;
		}
		assert (task.getAction().getType() == ActionType.INTERACTIVE);
		return task.showWizard(title, first, last, configuration);
	}

	@Override
	public UIPluginContext createChildContext(String label) {
		return (UIPluginContext) super.createChildContext(label);
	}
}

class FileChooserPropertyListener implements PropertyChangeListener {

	public FileChooserPropertyListener(String ext) {
		this.ext = ext.toLowerCase();
	}

	public String ext;

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
			ext = ((FileNameExtensionFilter) evt.getNewValue()).getExtensions()[0].toLowerCase();
		}
	}
}
