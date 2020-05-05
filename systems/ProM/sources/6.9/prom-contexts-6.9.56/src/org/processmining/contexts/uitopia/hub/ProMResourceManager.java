package org.processmining.contexts.uitopia.hub;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.deckfour.uitopia.api.hub.ResourceManager;
import org.deckfour.uitopia.api.model.Resource;
import org.deckfour.uitopia.api.model.ResourceFilter;
import org.deckfour.uitopia.api.model.ResourceType;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.hub.overlay.ProgressOverlayDialog;
import org.processmining.contexts.uitopia.model.ProMCResource;
import org.processmining.contexts.uitopia.model.ProMPOResource;
import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.contexts.uitopia.model.ProMResourceType;
import org.processmining.framework.ProMID;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionAnnotation;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.events.ConnectionObjectListener;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.plugin.events.PluginLifeCycleEventListener;
import org.processmining.framework.plugin.events.ProvidedObjectLifeCycleListener;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.framework.providedobjects.ProvidedObjectManager;

public class ProMResourceManager extends UpdateSignaller implements ResourceManager<ProMResource<?>>,
		ProvidedObjectLifeCycleListener, PluginLifeCycleEventListener, ConnectionObjectListener {

	private static final String LASTIMPORTFILE = "last import file location";

	private static final String LASTEXPORTFILE = "last export file location";

	private static final String FAVORITEIMPORT = "favorite import for type ";

	private static final String FAVORITEEXPORT = "favorite export for type ";

	private static ProMResourceManager instance = null;

	private final Map<Class<?>, ProMResourceType> resourceClasses = new ConcurrentHashMap<Class<?>, ProMResourceType>();
	private final Map<ProMID, ProMResource<?>> resources = new ConcurrentHashMap<ProMID, ProMResource<?>>();

	private final UIContext context;

	private Map<FileFilter, PluginParameterBinding> importplugins;

	private final ProvidedObjectManager poManager;

	// HV Remember last file imported and exported.
	private File lastImportedFile;
	private File lastExportedFile;

	private Boolean importPluginAdded = true;

	private final Preferences preferences;

	private ConnectionManager connectionManager;

	private ProMResourceManager(UIContext context) {

		this.context = context;
		for (Class<?> type : context.getPluginManager().getKnownObjectTypes()) {
			addType(type);
		}
		for (Class<?> type : context.getPluginManager().getKnownClassesAnnotatedWith(ConnectionAnnotation.class)) {
			addType(type);
		}
		poManager = context.getProvidedObjectManager();
		poManager.getProvidedObjectLifeCylceListeners().add(this);

		connectionManager = context.getConnectionManager();
		connectionManager.getConnectionListeners().add(this);

		// HV No last file imported or exported yet.
		preferences = Preferences.userNodeForPackage(getClass());

		String name = preferences.get(LASTIMPORTFILE, null);
		lastImportedFile = name == null ? null : new File(name);
		while (lastImportedFile != null && !lastImportedFile.exists()) {
			lastImportedFile = lastImportedFile.getParentFile();
		}
		name = preferences.get(LASTEXPORTFILE, null);
		lastExportedFile = name == null ? null : new File(name);
		while (lastExportedFile != null && !lastExportedFile.exists()) {
			lastExportedFile = lastExportedFile.getParentFile();
		}
	}

	public ResourceType addType(Class<?> type) {
		return resourceClasses.put(type, new ProMResourceType(type));
	}

	public static ProMResourceManager initialize(UIContext context) {
		if (instance == null) {
			instance = new ProMResourceManager(context);
		}
		return instance;
	}

	/*
	 * HV: Return a default exporter for known types. Fortunately, this handling
	 * of favorite is all String based, so we do not need to know the actual
	 * types (only their names).
	 */
	private String getDefaultExport(String typeName) {
		// The typeName matches whatever the ProM workspace shows in the second line of an object (basically, this is the class name). 
		// The returned label should match with whatever is in the @UIExportPlugin declaration.
		if (typeName.equals("XLog")) {
			return "XES files";
		} else if (typeName.equals("Petrinet")) {
			return "PNML files";
		} else if (typeName.equals("AcceptingPetriNet")) {
			return "Accepting Petri Net";
		} else if (typeName.equals("PetriNetWithData")) {
			// Particularly useful, as this may prevent novice users from using the usual PNML export, which does not export data.
			return "Data-aware PNML files";
		}
		return "";
	}

	public Collection<FileFilter> getExportFilters(Resource resource) {
		Collection<FileFilter> exportfilters = new HashSet<FileFilter>();
		Set<PluginParameterBinding> potentialExportPlugins = context.getPluginManager().getPluginsAcceptingInAnyOrder(
				UIPluginContext.class, true, File.class, resource.getType().getTypeClass());

		for (PluginParameterBinding binding : potentialExportPlugins) {
			if (binding.getPlugin().getAnnotation(UIExportPlugin.class) != null) {
				String description = binding.getPlugin().getAnnotation(UIExportPlugin.class).description();
				String extension = binding.getPlugin().getAnnotation(UIExportPlugin.class).extension();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extension);
				exportfilters.add(filter);
			}
		}
		return exportfilters;
	}

	public boolean exportResource(Resource resource) throws IOException {
		assert(resource instanceof ProMResource<?>);

		String lastChosenExportPlugin = preferences.get(FAVORITEEXPORT + resource.getType().getTypeName(), "");
		if (lastChosenExportPlugin.isEmpty()) {
			// HV: No favorite set yet by user. Use reasonable default values for known types.
			lastChosenExportPlugin = getDefaultExport(resource.getType().getTypeName());
		}
		FileFilter lastChosenFilter = null;

		Map<FileFilter, PluginParameterBinding> exportplugins = new TreeMap<FileFilter, PluginParameterBinding>(
				new Comparator<FileFilter>() {

					public int compare(FileFilter f1, FileFilter f2) {
						return f1.getDescription().toLowerCase().compareTo(f2.getDescription().toLowerCase());
					}
				});
		Set<PluginParameterBinding> potentialExportPlugins = context.getPluginManager().getPluginsAcceptingInAnyOrder(
				UIPluginContext.class, true, File.class, resource.getType().getTypeClass());

		for (PluginParameterBinding binding : potentialExportPlugins) {
			if (binding.getPlugin().getAnnotation(UIExportPlugin.class) != null) {
				String description = binding.getPlugin().getAnnotation(UIExportPlugin.class).description();
				String extension = binding.getPlugin().getAnnotation(UIExportPlugin.class).extension();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extension);
				exportplugins.put(filter, binding);
				if (description.equals(lastChosenExportPlugin)) {
					lastChosenFilter = filter;
				}
			}
		}

		// HV Start at location last file was exported.
		JFileChooser fc = (lastExportedFile != null ? new JFileChooser(lastExportedFile) : new JFileChooser());
		for (FileFilter filter : exportplugins.keySet()) {
			fc.addChoosableFileFilter(filter);
		}
		fc.setAcceptAllFileFilterUsed(false);
		if (lastChosenFilter != null) {
			fc.setFileFilter(lastChosenFilter);
		}

		askForFile: while (true) {
			int returnVal = fc.showSaveDialog(context.getUI());

			if ((returnVal == JFileChooser.APPROVE_OPTION) && (fc.getSelectedFile() != null)) {
				File file = fc.getSelectedFile();
				FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) fc.getFileFilter();
				if (selectedFilter == null) {
					selectedFilter = (FileNameExtensionFilter) exportplugins.keySet().iterator().next();
				}

				String postfix = "." + selectedFilter.getExtensions()[0];
				if (!file.getAbsolutePath().endsWith(postfix)) {
					String name = file.getAbsolutePath() + postfix;
					file = new File(name);
				}
				if (!file.createNewFile()) {
					int ow = JOptionPane.showConfirmDialog(context.getUI(),
							"Are you sure you want to overwrite " + file.getName(), "Confirm overwrite",
							JOptionPane.YES_NO_OPTION);
					if (ow == JOptionPane.NO_OPTION) {
						continue askForFile;
					}
				}

				// HV Remember last file exported (and imported if not initialized yet).
				lastExportedFile = file.getParentFile();
				preferences.put(LASTEXPORTFILE, lastExportedFile.getAbsolutePath());
				if (lastImportedFile == null) {
					lastImportedFile = lastExportedFile;
					preferences.put(LASTIMPORTFILE, lastImportedFile.getAbsolutePath());
				}

				PluginParameterBinding binding = exportplugins.get(selectedFilter);

				preferences.put(FAVORITEEXPORT + resource.getType().getTypeName(),
						binding.getPlugin().getAnnotation(UIExportPlugin.class).description());

				UIPluginContext importContext = context.getMainPluginContext()
						.createChildContext("Saving file with " + binding.getPlugin().getName());

				PluginExecutionResult result = binding.invoke(importContext, file,
						((ProMResource<?>) resource).getInstance());
				context.getProvidedObjectManager().createProvidedObjects(importContext);

				try {
					result.synchronize();
				} catch (CancellationException e) {
					context.getMainPluginContext().log("Export of " + file + " cancelled.");
					return false;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(context.getUI(),
							"<html>Error with export of " + file + ":<br>" + e.getMessage() + "</html>",
							"Error while exporting", JOptionPane.ERROR_MESSAGE);
					return false;
				} finally {
					importContext.getParentContext().deleteChild(importContext);
				}
				return true;
			}

			return false;
		}
	}

	public java.util.List<ProMResource<?>> getAllResources() {
		return new ArrayList<ProMResource<?>>(resources.values());
	}

	public java.util.List<ProMResource<?>> getAllResources(ResourceFilter filter) {
		return filterList(getAllResources(), filter);
	}

	public java.util.List<ResourceType> getAllSupportedResourceTypes() {
		java.util.List<ResourceType> types = new ArrayList<ResourceType>();
		types.addAll(resourceClasses.values());
		return types;
	}

	public java.util.List<ProMResource<?>> getChildrenOf(final Resource parent) {
		return getAllResources(new ResourceFilter() {
			public boolean accept(Resource res) {
				return getParentsOf(res).contains(parent);
			}
		});
	}

	public java.util.List<ProMResource<?>> getChildrenOf(final Resource parent, final ResourceFilter filter) {
		return getAllResources(new ResourceFilter() {
			public boolean accept(Resource res) {
				return filter.accept(res) && getParentsOf(res).contains(parent);
			}
		});
	}

	public java.util.List<ProMResource<?>> getFavoriteResources() {
		return getAllResources(new ResourceFilter() {
			public boolean accept(Resource res) {
				return res.isFavorite();
			}
		});
	}

	public java.util.List<ProMResource<?>> getFavoriteResources(final ResourceFilter filter) {
		return getAllResources(new ResourceFilter() {
			public boolean accept(Resource res) {
				return filter.accept(res) && res.isFavorite();
			}
		});
	}

	public java.util.List<ProMResource<?>> getImportedResources() {
		return getAllResources(new ResourceFilter() {
			public boolean accept(Resource res) {
				return getParentsOf(res).isEmpty();
			}
		});
	}

	public java.util.List<ProMResource<?>> getImportedResources(final ResourceFilter filter) {
		return getAllResources(new ResourceFilter() {
			public boolean accept(Resource res) {
				return filter.accept(res) && getParentsOf(res).isEmpty();
			}
		});
	}

	public java.util.List<ProMResource<?>> getParentsOf(Resource child) {
		return new ArrayList<ProMResource<?>>(((ProMResource<?>) child).getParents());
	}

	public java.util.List<ProMResource<?>> getParentsOf(Resource child, ResourceFilter filter) {
		java.util.List<ProMResource<?>> filtered = new ArrayList<ProMResource<?>>(
				((ProMResource<?>) child).getParents());
		return filterList(filtered, filter);
	}

	/**
	 * Start the import dialog for a resource. Can be called from the EDT or any
	 * other thread. Makes sure that the dialog-part of the actual import is run in the EDT.
	 */
	public boolean importResource() {
		if (EventQueue.isDispatchThread()) {
			/*
			 * Called from the EDT. OK.
			 */
			importResourceInEDT();
		} else {
			/*
			 * Not called from the EDT. Have the EDT take care of it.
			 */
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						importResourceInEDT();
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	/*
	 * This method should only be called from the EDT thread.
	 */
	private synchronized boolean importResourceInEDT() {
		if (!EventQueue.isDispatchThread()) {
			System.err.println("Method should only be called from EDT");
			return false;
		}
		synchronized (importPluginAdded) {
			if (importPluginAdded) {
				buildImportPlugins();
				importPluginAdded = false;
			}
		}
		// HV Start from the location of the last file imported.
		final JFileChooser fc = (lastImportedFile != null ? new JFileChooser(lastImportedFile) : new JFileChooser());
		for (FileFilter filter : importplugins.keySet()) {
			fc.addChoosableFileFilter(filter);
		}
		fc.setAcceptAllFileFilterUsed(true);
		/*
		 * Disable multi-selection, as this allows for the user to select two
		 * files simultaneously that cannot be handled by a single importer. The
		 * user can only use one importer...
		 */
		fc.setMultiSelectionEnabled(true);
		fc.setFileFilter(fc.getAcceptAllFileFilter());

		/*
		 * HV: This method does run in the EDT. 
		 */
		int returnVal = fc.showOpenDialog(context.getUI());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			/*
			 * As a result of disabling the multi-selection, use a different way
			 * to get the selected file(s).
			 */
			File[] files = fc.getSelectedFiles();
			//			File[] files = new File[]{ fc.getSelectedFile() };

			// XL: parse file one for one
			boolean importedSuccessfully = true;
			for (final File f : files) {
				PluginParameterBinding binding = importplugins.get(fc.getFileFilter());
				importedSuccessfully &= importResourceInEDT(binding, f);
			}
			return importedSuccessfully;

		} else {
			return false;

		}
	}

	/**
	 * Can be called from the EDT or any other thread.
	 */
	public synchronized boolean importResources(File... files) {
		return importResource(null, files);
	}

	/**
	 * Can be called from the EDT or any other thread. Makes sure that the
	 * dialog-par tof the actual import is run in the EDT.
	 */
	public boolean importResource(final PluginParameterBinding binding, final File... files) {
		if (EventQueue.isDispatchThread()) {
			/*
			 * Called from the EDT. OK.
			 */
			importResourceInEDT(binding, files);
		} else {
			/*
			 * Not called from the EDT. Have the EDZT take care of it.
			 */
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						importResourceInEDT(binding, files);
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	/*
	 * This method should be called from the EDT.
	 */
	private synchronized boolean importResourceInEDT(PluginParameterBinding binding, final File... files) {
		if (!EventQueue.isDispatchThread()) {
			System.err.println("Method should only be called from EDT");
			return false;
		}
		synchronized (importPluginAdded) {
			if (importPluginAdded) {
				buildImportPlugins();
				importPluginAdded = false;
			}
		}

		// HV Remember the location of the last file imported (and exported if not initialized yet).
		lastImportedFile = files[0].getParentFile();
		preferences.put(LASTIMPORTFILE, lastImportedFile.getAbsolutePath());
		if (lastExportedFile == null) {
			lastExportedFile = lastImportedFile;
			preferences.put(LASTEXPORTFILE, lastExportedFile.getAbsolutePath());
		}

		if (binding == null) {
			// user chose the "all files" option
			Map<String, PluginParameterBinding> bindings = new HashMap<String, PluginParameterBinding>();
			for (FileFilter filter : importplugins.keySet()) {
				// HV: Only show plug-ins that can handle all files, as all files will be imported by it.
				boolean ok = true;
				for (File file : files) {
					if (!filter.accept(file)) {
						ok = false;
					}
				}
				if (ok) {
					bindings.put(filter.getDescription(), importplugins.get(filter));
				}
			}
			if (bindings.size() == 0) {
				// 	TODO: No plugins available based on filetype
				// 	show all plugins
				for (FileFilter filter : importplugins.keySet()) {
					bindings.put(filter.getDescription(), importplugins.get(filter));
				}
			}
			if (bindings.size() == 0) {
				/*
				 * HV: This method does run in the EDT. 
				 */
				JOptionPane.showMessageDialog(context.getUI(), "No import plugins available",
								"No input plugins available!", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			binding = bindings.values().iterator().next();
			if (bindings.size() > 1) {

				String key = FAVORITEIMPORT + extractFileType(files[0].getAbsolutePath());
				final String[] possibilities = bindings.keySet().toArray(new String[0]);
				final String preferredImport = preferences.get(key, possibilities[0]);

				String selected = (String) JOptionPane.showInputDialog(context.getUI(),
									"Available Import Plugins for file " + files[0].getName() + ":",
									"Select an import plugin...", JOptionPane.PLAIN_MESSAGE, null, possibilities,
									preferredImport);

				if (selected == null) {
					return false;
				}

				preferences.put(key, selected);
				binding = bindings.get(selected);
			}
		}
		if (binding == null) {
			// No import was selected.
			return false;
		}

		// HV: Dialog-part is now done, hence we can off-load the actual importing to another thread.
		// This frees the EDT for showing the progress bar while doing the import.
		final PluginParameterBinding finalBinding = binding;
		Runnable importThread = new Runnable() {
			public void run() {
				importResourceNotInEDT(finalBinding, files);
			}
		};
		(new Thread(importThread)).start();
		return true;
	}
	
	private boolean importResourceNotInEDT(final PluginParameterBinding binding, final File... files) {
		if (EventQueue.isDispatchThread()) {
			System.err.println("Method should never be called from EDT");
			return false;
		}
		/*
		 * Synchronize on the provided object manager to prevent multiple imports 
		 * talking at the same time to this manager.
		 */
		synchronized(context.getProvidedObjectManager()) {
		for (File f : files) {
			UIPluginContext importContext = context.getMainPluginContext()
					.createChildContext("Opening file with " + binding.getPlugin().getName());
			importContext.getPluginLifeCycleEventListeners().add(this);

			ProgressOverlayDialog progress = new ProgressOverlayDialog(context.getController().getMainView(),
					importContext, "Importing " + binding.getPlugin().getName());
			context.getController().getMainView().showOverlay(progress);
//			Thread.yield();

			PluginExecutionResult result = binding.invoke(importContext, f);
			context.getProvidedObjectManager().createProvidedObjects(importContext);

			try {
				result.synchronize();
			} catch (CancellationException e) {
				context.getController().getMainView().hideOverlay();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(context.getUI(), "Import of " + files + " cancelled.", "Import cancelled",
								JOptionPane.WARNING_MESSAGE);
					}
				});
				context.getMainPluginContext().log("Import of " + files + " cancelled.");
				return false;
			} catch (final Exception e) {
				context.getController().getMainView().hideOverlay();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(context.getUI(),
								"<html>Error with import of " + files + ":<br>" + e.getMessage() + "</html>", "Import failed",
								JOptionPane.ERROR_MESSAGE);
					}
				});
				context.getMainPluginContext().log("Error with import of " + files + ".", MessageLevel.ERROR);
				context.getMainPluginContext().log(e);
				return false;
			} finally {
				importContext.getParentContext().deleteChild(importContext);
			}

			context.getController().getMainView().hideOverlay();
		}
		}
		return true;

	}

	private String extractFileType(String filename) {
		/*
		 * HV: Restrict the length of the extension to 9 characters.
		 */
		int indexName = Math.max(filename.lastIndexOf(File.separatorChar), filename.length() - 10);
		int indexDot = filename.indexOf('.', indexName);
		if (indexDot < 0) {
			// No dot in filename, hence no extension.
			return "";
		}
		return filename.substring(indexDot);
	}

	private void buildImportPlugins() {
		importplugins = new TreeMap<FileFilter, PluginParameterBinding>(new Comparator<FileFilter>() {

			public int compare(FileFilter f1, FileFilter f2) {
				return f1.getDescription().toLowerCase().compareTo(f2.getDescription().toLowerCase());
			}
		});

		Set<PluginParameterBinding> potentialImportPlugins = context.getPluginManager()
				.getPluginsAcceptingOrdered(UIPluginContext.class, true, File.class);

		for (PluginParameterBinding binding : potentialImportPlugins) {
			if (binding.getPlugin().hasAnnotation(UIImportPlugin.class)) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						binding.getPlugin().getAnnotation(UIImportPlugin.class).description(),
						binding.getPlugin().getAnnotation(UIImportPlugin.class).extensions());
				importplugins.put(filter, binding);
			}
		}

	}

	public ResourceType getResourceTypeFor(Class<?> type) {
		return resourceClasses.get(type);
	}

	public boolean isResourceType(Class<?> type) {
		return resourceClasses.keySet().contains(type);
	}

	private java.util.List<ProMResource<?>> filterList(java.util.List<ProMResource<?>> filtered,
			ResourceFilter filter) {
		synchronized (filtered) {
			Iterator<ProMResource<?>> it = filtered.iterator();
			while (it.hasNext()) {
				if (!filter.accept(it.next())) {
					it.remove();
				}
			}
		}
		return filtered;
	}

	public java.util.List<ResourceType> getResourceTypes(java.util.List<? extends Resource> res) {
		ArrayList<ResourceType> types = new ArrayList<ResourceType>(res.size());
		for (Resource r : res) {
			types.add(r.getType());
		}
		return types;
	}

	public void providedObjectCreated(ProvidedObjectID objectID, PluginContext context) {
		// Ignore. Only respond when a future of an object is ready.
	}

	public void providedObjectDeleted(ProvidedObjectID id) {
		if (resources.remove(id) != null) {
			signalUpdate();
		}
	}

	public void providedObjectFutureReady(ProvidedObjectID id) {
		Class<?> type;
		try {
			type = context.getProvidedObjectManager().getProvidedObjectType(id);
		} catch (ProvidedObjectDeletedException e) {
			// If the object has been deleted, try the next one
			return;
		}
		ResourceType resType = getResourceTypeFor(type);
		if (resType != null) {
			ProMResource<?> res;
			try {
				res = context.getResourceManager()
						.getResourceForInstance(context.getProvidedObjectManager().getProvidedObjectObject(id, true));
				if (res == null) {
					res = new ProMPOResource(context, null, resType, id,
							Collections.<Collection<ProMPOResource>>emptyList());
					addResource(id, res);
				}
			} catch (ProvidedObjectDeletedException e) {
				// If the object has been deleted, try the next one
				return;
			}
		}
	}

	public void providedObjectNameChanged(ProvidedObjectID id) {
		// Ignore. Access to the object is provided using late-binding, hence
		// the last version of the label is always returned by the resource
		if (resources.containsKey(id)) {
			signalUpdate();
		}
	}

	public void providedObjectObjectChanged(ProvidedObjectID id) {
		// Ignore. Access to the object is provided using late-binding, hence
		// the last version of the object is always returned by the resource
		if (resources.containsKey(id)) {
			signalUpdate();
		}
	}

	@SuppressWarnings("unchecked")
	public <R extends ProMResource<?>> R addResource(ProvidedObjectID id, R res) {
		if (resources.containsKey(id)) {
			return (R) resources.get(id);
		} else {
			resources.put(id, res);
			signalUpdate();
			return res;
		}
	}

	public ProMResource<?> getResourceForInstance(Object o) {
		for (ProMResource<?> resource : getAllResources()) {
			if (resource.getInstance() == o) {
				return resource;
			}
		}
		return null;
	}

	//********************************************************************
	// PluginLifeCycle.
	//

	public void pluginCancelled(PluginContext context) {
		// TODO Auto-generated method stub

	}

	public void pluginCompleted(PluginContext pluginContext) {
		PluginExecutionResult result = pluginContext.getResult();
		for (int i = 0; i < result.getSize(); i++) {
			ProvidedObjectID id = result.getProvidedObjectID(i);
			Class<?> type;
			try {
				type = context.getProvidedObjectManager().getProvidedObjectType(id);
			} catch (ProvidedObjectDeletedException e) {
				// If the object has been deleted, try the next one
				continue;
			}
			ResourceType resType = getResourceTypeFor(type);
			if (resType != null) {
				ProMResource<?> res = new ProMPOResource(context, null, resType, id,
						Collections.<Collection<ProMPOResource>>emptyList());
				res = addResource(id, res);
				if (i + 1 == result.getPlugin().getMostSignificantResult()) {
					res.setFavorite(true);
				}
			}
		}
	}

	public void pluginCreated(PluginContext context) {
		// gracefully ignore
	}

	public void pluginDeleted(PluginContext context) {
		// gracefully ignore
	}

	public void pluginFutureCreated(PluginContext context) {
		// gracefully ignore
	}

	public void pluginResumed(PluginContext context) {
		// gracefully ignore
	}

	public void pluginStarted(PluginContext context) {
		// gracefully ignore
	}

	public void pluginSuspended(PluginContext context) {
		// gracefully ignore
	}

	public void pluginTerminatedWithError(PluginContext context, Throwable t) {
		// gracefully ignore
	}

	public void addedImportPlugins() {
		synchronized (importPluginAdded) {
			importPluginAdded = true;
		}
	}

	//********************************************************************
	// ConnectionListener.
	//

	public void connectionCreated(ConnectionID id) {
		if (!resources.containsKey(id)) {
			Connection conn;
			try {
				conn = connectionManager.getConnection(id);
				if (!conn.isRemoved()) {
					// TODO
					java.util.List<Collection<ProMPOResource>> values = new ArrayList<Collection<ProMPOResource>>();
					for (Object o : conn.getObjects().baseSet()) {
						ProMResource<?> r = context.getResourceManager().getResourceForInstance(o);
						if (r != null) {
							assert(r instanceof ProMPOResource);
							Collection<ProMPOResource> c = new LinkedList<ProMPOResource>();
							c.add((ProMPOResource) r);
							values.add(c);
						}
					}

					ProMCResource res = new ProMCResource(context, null, getResourceTypeFor(conn.getClass()), id,
							values);
					resources.put(id, res);
					signalUpdate();
				}
			} catch (ConnectionCannotBeObtained e) {
				// do nothing
			}
		}
	}

	public void connectionDeleted(ConnectionID id) {
		resources.remove(id);
		// No need to alert serializer, the connection will not be serialized if it has
		// been removed/destroyed.
	}

	public void connectionUpdated(ConnectionID id) {
		assert(resources.containsKey(id));
		// No need to alert the serializer, the connections is not serialized yet, it will
		// be on exit, not earlier.
		signalUpdate();
	}

}
