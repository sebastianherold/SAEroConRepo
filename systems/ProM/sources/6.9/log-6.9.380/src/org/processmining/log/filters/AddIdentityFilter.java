
package org.processmining.log.filters;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.deckfour.xes.extension.std.XIdentityExtension;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.id.XIDFactory;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeID;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.xes.extensions.id.IdentityConnection;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.LogFilterException;
import org.processmining.plugins.log.logfilters.XEventEditor;
import org.processmining.plugins.log.logfilters.XTraceEditor;

public class AddIdentityFilter implements XEventEditor, XTraceEditor {

	public static XLog addIdentities(PluginContext context, XLog log) {
		AddIdentityFilter af = new AddIdentityFilter(log);

		try {
			return af.filter(context);
		} catch (LogFilterException e) {
			e.printStackTrace();
		}

		return null;
	}

	protected AddIdentityFilter(XLog log) {
		this.log = log;
	}

	private XLog log;

	public XLog filter(PluginContext context) throws LogFilterException {
		idmapping = new HashMap<String, XAttributable>();

		attributeValueMapping = new HashMap<String, Map<Object, XID>>();

		addIdentity(log);
		XLog filtered = LogFilter.filter(context.getProgress(), log.size(), log, null, this, this);
		//create connection
		context.addConnection(getConnection(filtered));

		return filtered;
	}

	public IdentityConnection getConnection(XLog filtered) {
		return new IdentityConnection(filtered, idmapping);
	}

	public XTrace editTrace(XTrace trace) {
		addIdentity(trace);
		return trace;
	}

	public XEvent editEvent(XEvent event) {
		XEvent editedEvent = (XEvent) event.clone();
		addIdentity(editedEvent);

		return editedEvent;
	}

	private Map<String, XAttributable> idmapping;

	private void addIdentity(XAttributable target) {
		//if I'm the identity, ignore me!
		if (target instanceof XAttribute) {
			if (((XAttribute) target).getKey() == XIdentityExtension.KEY_ID) {
				return;
			}
		}

		//first add an identity to my kids...
		for (Entry<String, XAttribute> attr : target.getAttributes().entrySet()) {
			addIdentity(attr.getValue());
		}

		//then to me
		XID id = XIdentityExtension.instance().extractID(target);
		if (id == null) {

			//first check, whether "I"already have an id in my attribute mapping...
			if (target instanceof XAttribute) {
				id = getIdFromMemory((XAttribute) target);
			} else {
				id = XIDFactory.instance().createId();
			}
		}
		XIdentityExtension.instance().assignID(target, id);

		//store the mapping
		idmapping.put(id.toString(), target);
	}

	private Map<String, Map<Object, XID>> attributeValueMapping;

	private XID getIdFromMemory(XAttribute attr) {
		String key = attr.getKey();

		Map<Object, XID> values = attributeValueMapping.get(key);
		if (values == null) {
			values = new HashMap<Object, XID>();
			attributeValueMapping.put(key, values);
		}

		Object value = getValueOf(attr);
		XID id = values.get(value);
		if (id == null) {
			id = XIDFactory.instance().createId();
			values.put(value, id);
		}

		return id;
	}

	private Object getValueOf(XAttribute attr) {

		if (attr instanceof XAttributeBoolean) {
			return ((XAttributeBoolean) attr).getValue();
		} else if (attr instanceof XAttributeContinuous) {
			return ((XAttributeContinuous) attr).getValue();
		} else if (attr instanceof XAttributeDiscrete) {
			return ((XAttributeDiscrete) attr).getValue();
		} else if (attr instanceof XAttributeTimestamp) {
			return ((XAttributeTimestamp) attr).getValue();
		} else if (attr instanceof XAttributeLiteral) {
			return ((XAttributeLiteral) attr).getValue();
		} else if (attr instanceof XAttributeID) {
			return ((XAttributeID) attr).getValue();
		} else {
			return new Object();
		}
	}

}
