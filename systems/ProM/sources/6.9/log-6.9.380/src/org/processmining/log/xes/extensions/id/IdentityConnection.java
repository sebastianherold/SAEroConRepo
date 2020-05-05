package org.processmining.log.xes.extensions.id;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;


/**
 * This class provides a mapping for the identity of XAttributeable elements. 
 * Given an ID, it returns the corresponding XAttributeable element.
 * 
 * TODO 
 * 
 * @author jvdwerf
 *
 */
public class IdentityConnection extends AbstractConnection {

	private final Map<String,XAttributable> idmapping;
	
	private final String LOG = "LOG";
	
	public IdentityConnection(XLog log) throws IdentitiesMissingException {
		super("Identity connection for " + 
				(XConceptExtension.instance().extractName(log) == null ? log.toString() : XConceptExtension.instance().extractName(log))				
		);
		
		IdentityMappingVisitor visitor = new IdentityMappingVisitor();
		log.accept(visitor);
		if (!visitor.allLogElementsHaveIdentifier()) {
			throw new IdentitiesMissingException(visitor.getElementsWithoutIdentity());
		}
		this.idmapping = visitor.getMapping();
		
		put(LOG, log);
	}
	
	
	
	public IdentityConnection(XLog log, Map<String,XAttributable> idmapping) {
		super("Identity connection for " + 
				(XConceptExtension.instance().extractName(log) == null ? log.toString() : XConceptExtension.instance().extractName(log))				
		);
		
		this.idmapping = new HashMap<String, XAttributable>(idmapping);
		
		put(LOG, log);
	}
	
	public XAttributable getElement(String id) {
		return idmapping.get(id);
	}
	
	public XAttributable getElement(XID id) {
		return getElement(id.toString());
	}
	
	public XLog getLog() {
		return (XLog) this.get(LOG);
	}
	
}
