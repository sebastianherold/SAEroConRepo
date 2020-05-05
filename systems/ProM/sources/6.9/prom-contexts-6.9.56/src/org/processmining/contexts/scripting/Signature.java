package org.processmining.contexts.scripting;

import java.util.Collections;
import java.util.List;

public class Signature {

	private final String name;
	private final List<Class<?>> parameterTypes;
	private final List<Class<?>> returnTypes;

	Signature(List<Class<?>> returnTypes, String name, List<Class<?>> parameterTypes) {
		this.returnTypes = returnTypes;
		this.name = name;
		this.parameterTypes = parameterTypes;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Signature)) {
			return false;
		}
		Signature other = (Signature) o;
		return name.equals(other.name) && parameterTypes.equals(other.parameterTypes);
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String getName() {
		return name;
	}

	public List<Class<?>> getParameterTypes() {
		return Collections.unmodifiableList(parameterTypes);
	}

	public List<Class<?>> getReturnTypes() {
		return returnTypes;
	}

	public String toString() {
		return toString(1, 1);
	}

	public String toString(int indent1, int indent2) {
		String result = getName() + "(";

		boolean isFirst = true;
		for (Class<?> param : getParameterTypes()) {
			if (isFirst) {
				isFirst = false;
			} else {
				result += ", ";
			}
			result += param.getSimpleName();
		}
		result += ") -> ";

		if (getReturnTypes().size() > 1) {
			result += "(";
		}
		isFirst = true;
		for (Class<?> type : getReturnTypes()) {
			if (isFirst) {
				isFirst = false;
			} else {
				result += ", ";
			}
			result += type.getSimpleName();
		}
		if (getReturnTypes().size() > 1) {
			result += ")";
		}
		return result;
	}
}
