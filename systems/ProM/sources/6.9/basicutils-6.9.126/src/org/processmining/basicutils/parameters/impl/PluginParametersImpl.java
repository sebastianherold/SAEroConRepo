package org.processmining.basicutils.parameters.impl;

import org.processmining.basicutils.parameters.PluginParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;

public class PluginParametersImpl implements PluginParameters {

	private int messageLevel;
	private boolean tryConnections;
	
	public PluginParametersImpl() {
		setMessageLevel(PluginParameters.MESSAGE);
		setTryConnections(true);
	}
	
	public PluginParametersImpl(PluginParameters parameters) {
		setMessageLevel(parameters.getMessageLevel());
		setTryConnections(parameters.isTryConnections());
	}
	
	public boolean equals(Object object) {
		if (object instanceof PluginParametersImpl) {
			PluginParametersImpl parameters = (PluginParametersImpl) object;
			return getMessageLevel() == parameters.getMessageLevel()
					&& isTryConnections() == parameters.isTryConnections();
		}
		return false;
	}
	
	public int getMessageLevel() {
		return messageLevel;
	}

	public void setMessageLevel(int messageLevel) {
		this.messageLevel = messageLevel;
	}
	
	public void displayMessage(String text) {
		if (messageLevel >= PluginParameters.MESSAGE) {
			System.out.println(text);
		}
	}
	
	public void displayWarning(String text) {
		if (messageLevel >= PluginParameters.WARNING) {
			System.out.println(text);
		}
	}
	
	public void displayError(String text) {
		if (messageLevel >= PluginParameters.ERROR) {
			System.err.println(text);
		}
	}

	public void displayMessage(PluginContext context, String text) {
		if (messageLevel >= PluginParameters.MESSAGE) {
			context.log(text, MessageLevel.NORMAL);
		}
	}
	
	public void displayWarning(PluginContext context, String text) {
		if (messageLevel >= PluginParameters.WARNING) {
			context.log(text, MessageLevel.WARNING);
		}
	}
	
	public void displayError(PluginContext context, String text) {
		if (messageLevel >= PluginParameters.ERROR) {
			context.log(text, MessageLevel.ERROR);
		}
	}

	public boolean isTryConnections() {
		return tryConnections;
	}

	public void setTryConnections(boolean tryConnections) {
		this.tryConnections = tryConnections;
	}
}
