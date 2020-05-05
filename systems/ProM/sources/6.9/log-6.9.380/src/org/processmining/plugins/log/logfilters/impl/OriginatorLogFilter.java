package org.processmining.plugins.log.logfilters.impl;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.XEventCondition;

@Plugin(name = "Originator Log Filter", categories = { PluginCategory.Filtering }, parameterLabels = { "Log", "Resources", "Groups", "Roles" }, returnLabels = { "Log (filtered)" }, returnTypes = { XLog.class })
public class OriginatorLogFilter {

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the resource of XEvent is not contained in
	 * the given set of resources.
	 * 
	 * If no resource information is available in the log, all events are
	 * removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param namesToKeep
	 *            The names of the resources to keep
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Filter on Names")
	public XLog filterWithNames(PluginContext context, XLog log, String[] namesToKeep) {
		return filterWithAll(context, log, namesToKeep, new String[0], new String[0]);
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the group of XEvent is not contained in the
	 * given set of groups.
	 * 
	 * If no resource information is available in the log, all events are
	 * removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param groupsToKeep
	 *            The names of the groups to keep
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 2 }, variantLabel = "Filter on Groups")
	public XLog filterWithGroups(PluginContext context, XLog log, String[] groupsToKeep) {
		return filterWithAll(context, log, new String[0], groupsToKeep, new String[0]);
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the role of XEvent is not contained in the
	 * given set of roles.
	 * 
	 * If no resource information is available in the log, all events are
	 * removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param rolesToKeep
	 *            The names of the roles to keep
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 3 }, variantLabel = "Filter on Roles")
	public XLog filterWithRoles(PluginContext context, XLog log, String[] rolesToKeep) {
		return filterWithAll(context, log, new String[0], new String[0], rolesToKeep);
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the resource of XEvent is not contained in
	 * the given set of resources, or in the given set of groups.
	 * 
	 * If no resource information is available in the log, all events are
	 * removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param namesToKeep
	 *            The names of the resources to keep
	 * @param groupsToKeep
	 *            The names of the groups to keep
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 2 }, variantLabel = "Filter on Names and Groups")
	public XLog filterWithNamesAndGroups(PluginContext context, XLog log, String[] namesToKeep, String[] groupsToKeep) {
		return filterWithAll(context, log, namesToKeep, groupsToKeep, new String[0]);
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the group of XEvent is not contained in the
	 * given set of groups, or in the given set of roles.
	 * 
	 * If no resource information is available in the log, all events are
	 * removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param groupsToKeep
	 *            The names of the groups to keep
	 * @param rolesToKeep
	 *            The names of the roles to keep
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 2, 3 }, variantLabel = "Filter on Groups")
	public XLog filterWithGroupsAndRoles(PluginContext context, XLog log, final String[] groupsToKeep,
			String[] rolesToKeep) {
		return filterWithAll(context, log, new String[0], groupsToKeep, rolesToKeep);
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the group of XEvent is not contained in the
	 * given set of resources, or in the given set of roles.
	 * 
	 * If no resource information is available in the log, all events are
	 * removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param namesToKeep
	 *            The names of the resources to keep
	 * @param rolesToKeep
	 *            The names of the roles to keep
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 3 }, variantLabel = "Filter on Groups")
	public XLog filterWithNamesAndRoles(PluginContext context, XLog log, String[] namesToKeep, String[] rolesToKeep) {
		return filterWithAll(context, log, namesToKeep, new String[0], rolesToKeep);
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the group of XEvent is not contained in the
	 * given set of resources, or in the given set of roles, or in the given set
	 * of groups.
	 * 
	 * If no resource information is available in the log, all events are
	 * removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param namesToKeep
	 *            The names of the resources to keep
	 * @param groupsToKeep
	 *            The names of the resources to keep
	 * @param rolesToKeep
	 *            The names of the roles to keep
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 2, 3 }, variantLabel = "Filter on Groups")
	public XLog filterWithAll(PluginContext context, XLog log, String[] namesToKeep, String[] groupsToKeep,
			String[] rolesToKeep) {

		// Construct a sorted set of names for easy lookup
		final Set<String> names = new TreeSet<String>(Arrays.asList(namesToKeep));
		final Set<String> roles = new TreeSet<String>(Arrays.asList(rolesToKeep));
		final Set<String> groups = new TreeSet<String>(Arrays.asList(groupsToKeep));

		return LogFilter.filter(context.getProgress(), 100, log, XLogInfoFactory.createLogInfo(log),
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// Keep the event if the getName() is contained in events.
						return roles.contains(XOrganizationalExtension.instance().extractRole(event))
								|| groups.contains(XOrganizationalExtension.instance().extractGroup(event))
								|| names.contains(XOrganizationalExtension.instance().extractResource(event));
					}

				});
	}

}
