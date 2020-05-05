package org.processmining.contexts.uitopia.packagemanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.packages.UnknownPackageTypeException;
import org.processmining.framework.packages.impl.CancelledException;

public class PMController {

	private final PMMainView mainView;
	private final PackageManager manager;
	private String query;

	public PMController(Boot.Level verbose) {
		manager = PackageManager.getInstance();
		manager.initialize(verbose);
		try {
			manager.update(false, verbose);
		} catch (CancelledException e) {
			e.printStackTrace();
		} catch (UnknownPackageTypeException e) {
			e.printStackTrace();
		}

		mainView = new PMMainView(this);
	}
	
	public void cleanPackageCache() throws BackingStoreException {
		manager.cleanPackageCache();
	}

	/**
	 * Select a package, select the right tab and return the selected PMPackage
	 * 
	 * @param packageName
	 * @return null if no package with such name exists.
	 */
	public PMPackage selectPackage(String packageName) {
		Set<PackageDescriptor> available = manager.getAvailablePackages();
		for (PackageDescriptor d : available) {
			if (d.getName().equals(packageName)) {
				// package descriptor d is available
				PMPackage pack = new PMPackage(d);
				setStatus(pack, d);
				mainView.showWorkspaceView(pack);
				return pack;
			}
		}
		return null;
	}

	public PMMainView getMainView() {
		return mainView;
	}

	public java.util.List<PMPackage> getToUninstallPackages() {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor descriptor : descriptors) {
			PMPackage pack = new PMPackage(descriptor);
			PackageDescriptor installed = manager.findInstalledVersion(descriptor);
			if ((installed != null) && installed.equals(descriptor)) {
				list.add(pack);
				setStatus(pack, descriptor);
			}
		}
		return list;
	}

	public java.util.List<? extends PMPackage> getToUpdatePackages() {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor available : descriptors) {
			PMPackage pack = new PMPackage(available);
			PackageDescriptor installed = manager.findInstalledVersion(available);
			if ((installed != null && manager.isAvailable(available)) && //
					installed.getVersion().lessThan(available.getVersion())) {
				list.add(pack);
				setStatus(pack, available);
			}
		}
		return list;
	}

	public java.util.List<? extends PMPackage> getToInstallPackages() {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor descriptor : descriptors) {
			PMPackage pack = new PMPackage(descriptor);
			PackageDescriptor installed = manager.findInstalledVersion(descriptor);
			if (installed == null && manager.isAvailable(descriptor)) {
				list.add(pack);
				setStatus(pack, descriptor);
			}
		}
		return list;
	}

	public java.util.List<PMPackage> getParentPackages(PMPackage reference) {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor descriptor : descriptors) {
			if (reference.getDependencies().contains(descriptor.getName())) {
				PMPackage pack = new PMPackage(descriptor);
				list.add(pack);
				setStatus(pack, descriptor);
			}
		}
		return list;
	}

	public java.util.List<PMPackage> getChildPackages(PMPackage reference) {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor descriptor : descriptors) {
			if (descriptor.getDependencies().contains(reference.getPackageName())) {
				PMPackage pack = new PMPackage(descriptor);
				list.add(pack);
				setStatus(pack, descriptor);
			}
		}
		return list;
	}

	public void setStatus(PMPackage pack, PackageDescriptor descriptor) {
		PackageDescriptor installed = manager.findInstalledVersion(descriptor);
		if (installed != null) {
			if (manager.isAvailable(descriptor) && !installed.equals(descriptor)) {
				pack.setStatus(PMPackage.PMStatus.TOUPDATE);
			} else {
				pack.setStatus(PMPackage.PMStatus.TOUNINSTALL);
			}
		} else {
			if (manager.isAvailable(descriptor)) {
				pack.setStatus(PMPackage.PMStatus.TOINSTALL);
			} else {
				pack.setStatus(PMPackage.PMStatus.DEAD);
			}
		}
	}

	public void update(final PMPackage pack, final PMWorkspaceView view) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					manager.install(Arrays.asList(new PackageDescriptor[] { pack.getDescriptor() }));
					view.updatePackages();
				} catch (CancelledException e) {
					e.printStackTrace();
				} catch (UnknownPackageTypeException e) {
					e.printStackTrace();
				}
			}

		});
		t.start();
	}

	public void update(final Collection<PMPackage> packs, final PMWorkspaceView view) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					List<PackageDescriptor> pds = new ArrayList<PackageDescriptor>();
					for (PMPackage p : packs) {
						pds.add(p.getDescriptor());
					}
					manager.install(pds);
					view.updatePackages();
				} catch (CancelledException e) {
					e.printStackTrace();
				} catch (UnknownPackageTypeException e) {
					e.printStackTrace();
				}
			}

		});
		t.start();
	}

	public void remove(final PMPackage pack, final PMWorkspaceView view) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					manager.uninstall(Arrays.asList(new PackageDescriptor[] { pack.getDescriptor() }));
					view.updatePackages();
				} catch (CancelledException e) {
					e.printStackTrace();
				}
			}

		});
		t.start();
	}

	public void remove(final Collection<PMPackage> packs, final PMWorkspaceView view) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					List<PackageDescriptor> pds = new ArrayList<PackageDescriptor>();
					for (PMPackage p : packs) {
						pds.add(p.getDescriptor());
					}
					manager.uninstall(pds);
					view.updatePackages();
				} catch (CancelledException e) {
					e.printStackTrace();
				}
			}

		});
		t.start();
	}

	public String getQuery() {
		return query == null ? "" : query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
