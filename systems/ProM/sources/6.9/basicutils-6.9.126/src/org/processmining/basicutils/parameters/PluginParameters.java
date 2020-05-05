package org.processmining.basicutils.parameters;

import org.processmining.framework.plugin.PluginContext;

public interface PluginParameters {

	public final static int MESSAGE = 1;
	public final static int WARNING = 2;
	public final static int ERROR = 3;
	public final static int DEBUG = 4;

	public int getMessageLevel();

	public void setMessageLevel(int messageLevel);
	
	public void displayMessage(String text);
	
	public void displayWarning(String text);
	
	public void displayError(String text);

	public void displayMessage(PluginContext context, String text);
	
	public void displayWarning(PluginContext context, String text);
	
	public void displayError(PluginContext context, String text);

	public boolean isTryConnections();

	public void setTryConnections(boolean tryConnections);
}
