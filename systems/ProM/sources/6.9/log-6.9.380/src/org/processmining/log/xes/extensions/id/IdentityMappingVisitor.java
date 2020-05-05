package org.processmining.log.xes.extensions.id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XIdentityExtension;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XVisitor;

/**
 * This visitor creates a mapping for the identities.
 * @author jvdwerf
 *
 */
class IdentityMappingVisitor extends XVisitor {

	private Map<String, XAttributable> idmap;
	private List<XAttributable> missing;
	
	//these are just standard
	public boolean precondition() { 
		return true; 
	}
	
	public void init(XLog log) {
		idmap = new HashMap<String, XAttributable>();
		missing = new ArrayList<XAttributable>();
	}
	
	public Map<String, XAttributable> getMapping() {
		return idmap;
	}
	
	public Collection<XAttributable> getElementsWithoutIdentity() {
		return missing;
	}
	
	public boolean allLogElementsHaveIdentifier() {
		return (missing.size() == 0);
	}
	
	@Override
	public void visitLogPre(XLog log) {
		visitXAttributable(log);
	}
	
	@Override
	public void visitTracePre(XTrace trace, XLog log) {
		visitXAttributable(trace);
	}
	
	@Override
	public void visitEventPre(XEvent event, XTrace trace) {
		visitXAttributable(event);
	}
	
	@Override
	public void visitAttributePre(XAttribute attr, XAttributable parent) {
		if (attr.getKey().equals(XIdentityExtension.KEY_ID)) {
			return;
		}
		
		visitXAttributable(attr);
	}
	
	private void visitXAttributable(XAttributable target) {
		XID id = XIdentityExtension.instance().extractID(target);
		if (id != null) {
			idmap.put(id.toString(), target);
		} else {
			missing.add(target);
		}
	}
}