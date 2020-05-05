package org.processmining.filtering.xfilter.implementations;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeLiteral;
import org.processmining.filtering.xfilter.interfaces.XAttributableFilter;

/**
 * The XAttributableContainsKeyWithValueXFilterImpl class checks whether an
 * XAttributable object contains a given <Key, Value> pair. If the object
 * contains the pair, the apply method will return the object (that is
 * mirrored). If the object does not contain the pair, the apply method will
 * return null.
 * 
 * @author S.J. van Zelst
 * 
 * @param <T> XAttributable object
 */
public class XAttributableContainsKeyWithValueXFilterImpl<T extends XAttributable> implements XAttributableFilter<T> {

	protected String key;

	protected String strVal = null;
	protected Boolean boolVal = null;

	public XAttributableContainsKeyWithValueXFilterImpl(String key, String value) {
		this(key);
		this.strVal = value;
	}

	public XAttributableContainsKeyWithValueXFilterImpl(String key, boolean value) {
		this(key);
		this.boolVal = value;
	}

	private XAttributableContainsKeyWithValueXFilterImpl(String key) {
		this.key = key;
	}

	@SuppressWarnings("unchecked")
	public XAttributableFilter<T> clone() {
		XAttributableFilter<T> clone = null;
		try {
			clone = (XAttributableFilter<T>) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	/**
	 * Apply method, will return (t) iff <K,V> is contained by the given
	 * XAttributable t. Will return null if t does not contain <K,V>.
	 */
	public T apply(T t) {
		T result = null;
		if (t.getAttributes().containsKey(key)) {
			if (routedCheck(t)) {
				result = t;
			}
		}
		return result;
	}

	private boolean routedCheck(T t) {
		boolean result = false;
		if (strVal != null) {
			result = strEquals(t);
		} else if (boolVal != null) {
			result = boolEquals(t);
		}
		return result;
	}

	private boolean strEquals(T t) {
		boolean result = false;
		XAttributeLiteral literal;
		if (t.getAttributes().get(key) instanceof XAttributeLiteral) {
			literal = (XAttributeLiteral) t.getAttributes().get(key);
			if (literal.getValue().equals(strVal)) {
				result = true;
			}
		}
		return result;
	}

	private boolean boolEquals(T t) {
		boolean result = false;
		XAttributeBoolean bool;
		if (t.getAttributes().get(key) instanceof XAttributeBoolean) {
			bool = (XAttributeBoolean) t.getAttributes().get(key);
			if (bool.getValue() == boolVal) {
				result = true;
			}
		}
		return result;
	}

}
