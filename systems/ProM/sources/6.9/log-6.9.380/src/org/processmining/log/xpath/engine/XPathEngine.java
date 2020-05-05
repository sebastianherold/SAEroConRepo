package org.processmining.log.xpath.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.extension.std.XIdentityExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.Pair;
import org.processmining.log.xes.extensions.id.IdentitiesMissingException;
import org.processmining.log.xes.extensions.id.IdentityConnection;
import org.processmining.plugins.log.exporting.ExportLogXes;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class XPathEngine {

	private XLog log;
	private PluginContext context;
	private IdentityConnection idConnection;
	private XdmNode logXMLDoc;
	private XPathCompiler xpath;
	private File logFile;

	private String logNameSpace = "http://www.xes-standard.org/";

	private static QName KEYNAME = new QName("key");
	private static QName VALUENAME = new QName("value");
	private static QName IDNode = new QName("", "http://www.xes-standard.org/", "id");

	public void setLogNameSpace(String namespace) {
		logNameSpace = namespace;

		IDNode = new QName("", namespace, "id");
	}

	public String getLogNameSpace() {
		return logNameSpace;
	}

	public XPathEngine(PluginContext context, XLog log) throws IdentitiesMissingException, IOException {
		this.context = context;
		setLog(log);

		init();
	}

	public XLog getLog() {
		return log;
	}

	private void setLog(XLog log) throws IdentitiesMissingException {
		this.log = log;

		//get the connection
		try {
			Collection<IdentityConnection> cons = context.getConnectionManager()
					.getConnections(IdentityConnection.class, context, log);
			//context.log("I found: " + cons.size() + " connections", MessageLevel.DEBUG );

			for (IdentityConnection con : cons) {
				idConnection = con;
				break;
			}

		} catch (ConnectionCannotBeObtained e) {
			//context.log("No connection found :-( We need to create one", MessageLevel.DEBUG );
			idConnection = new IdentityConnection(log);

			context.addConnection(idConnection);
		}

	}

	private void init() throws IOException {
		//transform the log into a XES XML file

		PluginContext child = context.createChildContext("export xes");
		logFile = File.createTempFile("ProM", ".xes");

		context.log("Created temporary file: " + logFile.getAbsolutePath(), MessageLevel.DEBUG);

		Set<PluginParameterBinding> plugins = context.getPluginManager().getPluginsAcceptingOrdered(child.getClass(),
				false, log.getClass(), File.class);

		//export log to temporary file
		(new ExportLogXes()).export(null, log, logFile);

		//and load the generated document in the XPath engine
		Processor proc = new Processor(false);
		xpath = proc.newXPathCompiler();
		xpath.declareNamespace("", getLogNameSpace());

		DocumentBuilder builder = proc.newDocumentBuilder();
		builder.setLineNumbering(true);
		builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);

		try {
			logXMLDoc = builder.build(logFile);

		} catch (SaxonApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<Pair<XAttributable, XAttributable>> query(String query, String relQuery) {

		try {
			return query(query, relQuery, false);
		} catch (SaxonApiException e) {
		}

		return null;
	}

	public List<Pair<XAttributable, XAttributable>> query(String query, String relQuery, boolean throwException)
			throws SaxonApiException {
		try {
			return query(logXMLDoc, query, relQuery);
		} catch (SaxonApiException e) {
			if (throwException) {
				throw e;
			}
		}
		return null;
	}

	protected List<Pair<XAttributable, XAttributable>> query(XdmNode startNode, String query, String relQuery)
			throws SaxonApiException {
		List<Pair<XAttributable, XAttributable>> results = new ArrayList<Pair<XAttributable, XAttributable>>();

		XPathSelector selector;

		selector = xpath.compile(query).load();

		selector.setContextItem(startNode);

		int counter = 0;

		for (XdmItem item : selector) {
			XAttributable first = getItem(item);

			//now create a second selector
			XPathSelector relSel = xpath.compile(relQuery).load();
			relSel.setContextItem(item);

			for (XdmItem relItem : relSel) {
				XAttributable second = getItem(relItem);

				results.add(new Pair<XAttributable, XAttributable>(first, second));
				counter++;
			}

		}

		System.out.println("Counter: " + counter);

		return results;
	}

	/**
	 * Main function that actually queries the log object.
	 * 
	 * @param query
	 * @return
	 */
	public List<XAttributable> query(String query) {
		try {
			return query(query, false);
		} catch (SaxonApiException e) {
		}
		return null;
	}

	public List<XAttributable> query(String query, boolean throwException) throws SaxonApiException {
		try {
			return query(logXMLDoc, query);
		} catch (SaxonApiException e) {
			if (throwException) {
				throw e;
			}
		}

		return null;
	}

	protected List<XAttributable> query(XdmNode startNode, String query) throws SaxonApiException {
		//execute the query. Look whether the internal things have an ID...
		XPathSelector selector;

		selector = xpath.compile(query).load();

		selector.setContextItem(startNode);

		List<XAttributable> result = new ArrayList<XAttributable>();

		int counter = 0;

		for (XdmItem item : selector) {
			//check whether each element has an id.
			//if you find an id, use it :-)

			XAttributable target = getItem(item);
			counter++;
			if (target != null) {
				result.add(target);
			}
		}
		System.out.println("Counter <single>: " + counter);
		System.out.println("   in result set: " + result.size());

		return result;
	}

	public void close() {
		//remove the log file
		logFile.delete();
	}

	public void finalize() {
		close();
	}

	private XAttributable getItem(XdmItem item) {
		if (item instanceof XdmNode) {
			return getItem((XdmNode) item);
		}
		return null;
	}

	private XAttributable getItem(XdmNode node) {
		//get the type of this element, if it is an id node,
		//get the key, and if the key equals identity:id
		if (node.getNodeName().getLocalName().equals("id")) {
			if (node.getAttributeValue(KEYNAME) != null
					&& node.getAttributeValue(KEYNAME).equals(XIdentityExtension.KEY_ID)) {
				return idConnection.getElement(node.getAttributeValue(VALUENAME));
			}
		} else {
			XdmSequenceIterator iterator = node.axisIterator(Axis.CHILD, IDNode);
			while (iterator.hasNext()) {

				XAttributable item = getItem(iterator.next());
				if (item != null) {
					return item;
				}
			}
		}

		return null;
	}

}
