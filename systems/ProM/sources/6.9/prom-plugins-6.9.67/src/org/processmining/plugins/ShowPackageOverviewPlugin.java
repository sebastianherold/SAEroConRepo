package org.processmining.plugins;

import java.util.Collection;
import java.util.List;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.contexts.uitopia.packagemanager.PMController;
import org.processmining.contexts.uitopia.packagemanager.PMPackage;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;

@Plugin(name = "Show Package Overview", parameterLabels = {}, level= PluginLevel.BulletProof, returnLabels = { "Release info" }, returnTypes = { HTMLToString.class }, userAccessible = true)
public class ShowPackageOverviewPlugin implements HTMLToString {

	private Collection<PluginDescriptor> pluginDescriptors;

	private ShowPackageOverviewPlugin(UIPluginContext context) {
		pluginDescriptors = context.getPluginManager().getAllPlugins();
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public static HTMLToString info(final UIPluginContext context) {
		return new ShowPackageOverviewPlugin(context);
	}

	@Override
	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();

		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		buffer.append("<h1>ProM Package Overview</h1>");

		PMController packageController = new PMController(Boot.Level.NONE);
		List<? extends PMPackage> uptodatePackages = packageController.getToUninstallPackages();
		List<? extends PMPackage> outofdatePackages = packageController.getToUpdatePackages();
		buffer.append("<h2>Installed packages</h2>");
		buffer.append("<table>");
		buffer.append("<tr><th>Package</th><th>Dependency</th><th>Version</th><th>Author</th></tr>");
		for (PMPackage pack : uptodatePackages) {
			buffer.append("<tr>");
			buffer.append("<td>" + pack.getPackageName() + "</td>");
			buffer.append("<td></td>");
			buffer.append("<td>" + pack.getVersion() + "</td>");
			buffer.append("<td>" + pack.getAuthorName() + "</td>");
			buffer.append("</tr>");
			for (String s : pack.getDependencies()) {
				buffer.append("<tr><td></td><td>" + s + "</td><td></td><td></td></tr>");
			}
		}
		buffer.append("</table>");
		buffer.append("<h3>Updates available</h3>");
		buffer.append("<table>");
		buffer.append("<tr><th>Package name</th><th>Dependency names</th><th>Version number</th><th>Author name</th></tr>");
		for (PMPackage pack : outofdatePackages) {
			buffer.append("<tr>");
			buffer.append("<td>" + pack.getPackageName() + "</td>");
			buffer.append("<td></td>");
			buffer.append("<td>" + pack.getVersion() + "</td>");
			buffer.append("<td>" + pack.getAuthorName() + "</td>");
			buffer.append("</tr>");
			for (String s : pack.getDependencies()) {
				buffer.append("<tr><td></td><td>" + s + "</td><td></td><td></td></tr>");
			}
		}
		buffer.append("</table>");

		buffer.append("<h2>Available plug-ins</h2>");
		buffer.append("<table>");
		buffer.append("<tr><th>Plug-in name</th><th>UITopia</th><th>UITopia name</th><th>Package name</th><th>Author name</th></tr>");
		for (PluginDescriptor pluginDescriptor : pluginDescriptors) {
			String uiName = null;
			boolean isUITopia = false;
			UITopiaVariant variant = pluginDescriptor.getAnnotation(UITopiaVariant.class);
			if (variant != null) {
				uiName = variant.uiLabel();
				isUITopia = true;
				buffer.append("<tr>");
				buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
				buffer.append("<td>Plug-in</td>");
				buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
				String packName = null;
				PackageDescriptor packageDescriptor = pluginDescriptor.getPackage();
				if (packageDescriptor != null) {
					packName = packageDescriptor.getName();
				}
				buffer.append("<td>" + (packName == null ? "" : packName) + "</td>");
				buffer.append("<td>" + variant.author() + "</td>");
				buffer.append("</tr>");
			}
			Visualizer visualizer = pluginDescriptor.getAnnotation(Visualizer.class);
			if (visualizer != null) {
				uiName = visualizer.name();
				isUITopia = true;
				buffer.append("<tr>");
				buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
				buffer.append("<td>Visualizer</td>");
				buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
				String packName = null;
				String authorName = null;
				PackageDescriptor packageDescriptor = pluginDescriptor.getPackage();
				if (packageDescriptor != null) {
					packName = packageDescriptor.getName();
					authorName = packageDescriptor.getAuthor();
				}
				buffer.append("<td>" + (packName == null ? "" : packName) + "</td>");
				buffer.append("<td>" + (authorName == null ? "" : authorName) + "</td>");
				buffer.append("</tr>");
			}
			UIImportPlugin importPlugin = pluginDescriptor.getAnnotation(UIImportPlugin.class);
			if (importPlugin != null) {
				uiName = pluginDescriptor.getName();
				isUITopia = true;
				buffer.append("<tr>");
				buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
				buffer.append("<td>Import</td>");
				buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
				String packName = null;
				String authorName = null;
				PackageDescriptor packageDescriptor = pluginDescriptor.getPackage();
				if (packageDescriptor != null) {
					packName = packageDescriptor.getName();
					authorName = packageDescriptor.getAuthor();
				}
				buffer.append("<td>" + (packName == null ? "" : packName) + "</td>");
				buffer.append("<td>" + (authorName == null ? "" : authorName) + "</td>");
				buffer.append("</tr>");
			}
			UIExportPlugin exportPlugin = pluginDescriptor.getAnnotation(UIExportPlugin.class);
			if (exportPlugin != null) {
				uiName = pluginDescriptor.getName();
				isUITopia = true;
				buffer.append("<tr>");
				buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
				buffer.append("<td>Export</td>");
				buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
				String packName = null;
				String authorName = null;
				PackageDescriptor packageDescriptor = pluginDescriptor.getPackage();
				if (packageDescriptor != null) {
					packName = packageDescriptor.getName();
					authorName = packageDescriptor.getAuthor();
				}
				buffer.append("<td>" + (packName == null ? "" : packName) + "</td>");
				buffer.append("<td>" + (authorName == null ? "" : authorName) + "</td>");
				buffer.append("</tr>");
			}
			for (int i = 0; i < pluginDescriptor.getNumberOfMethods(); i++) {
				variant = pluginDescriptor.getAnnotation(UITopiaVariant.class, i);
				if (variant != null) {
					uiName = variant.uiLabel();
					isUITopia = true;
					buffer.append("<tr>");
					buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
					buffer.append("<td>Plug-in variant</td>");
					buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
					String packName = null;
					PackageDescriptor packageDescriptor = pluginDescriptor.getPackage();
					if (packageDescriptor != null) {
						packName = packageDescriptor.getName();
					}
					buffer.append("<td>" + (packName == null ? "" : packName) + "</td>");
					buffer.append("<td>" + variant.author() + "</td>");
					buffer.append("</tr>");
				}
			}
			if (!isUITopia) {
				buffer.append("<tr>");
				buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
				buffer.append("<td></td>");
				buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
				String packName = null;
				String authorName = null;
				PackageDescriptor packageDescriptor = pluginDescriptor.getPackage();
				if (packageDescriptor != null) {
					packName = packageDescriptor.getName();
					authorName = packageDescriptor.getAuthor();
				}
				buffer.append("<td>" + (packName == null ? "" : packName) + "</td>");
				buffer.append("<td>" + (authorName == null ? "" : authorName) + "</td>");
				buffer.append("</tr>");
			}
		}
		buffer.append("</table>");
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
	}
}
