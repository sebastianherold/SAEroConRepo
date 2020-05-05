package org.processmining.contexts.uitopia.model;

import org.deckfour.uitopia.api.model.Parameter;
import org.deckfour.uitopia.api.model.ResourceType;

public class ProMParameter implements Parameter {

	private final ResourceType type;
	private final String name;
	private final int index;
	private final boolean isArray;

	public ProMParameter(String name, int index, ResourceType type, boolean isArray) {
		this.name = name;
		this.index = index;
		this.type = type;
		this.isArray = isArray;
	}

	public boolean isRequired() {
		return true;
	}

	public String getName() {
		return name;
	}

	public ResourceType getType() {
		return type;
	}

	public int getRealParameterIndex() {
		return index;
	}

	public int getMaxCardinality() {
		return isArray ? Parameter.CARDINALITY_INFINITY : 1;
	}

	public int getMinCardinality() {
		return 1;
	}
}
