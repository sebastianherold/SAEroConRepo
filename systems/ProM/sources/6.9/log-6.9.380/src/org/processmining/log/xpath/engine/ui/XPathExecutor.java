package org.processmining.log.xpath.engine.ui;

import java.io.IOException;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.xes.extensions.id.IdentitiesMissingException;

/**
 * 
 * "Vizualises" a log so that XPath queries can be executed.
 * 
 * @author jvdwerf
 *
 */

public class XPathExecutor {

	@Visualizer(name = "XPath executor")
	@Plugin(name = "XPath executor", parameterLabels = "XLog Event Log", returnTypes = JComponent.class, returnLabels = "XPath executor", userAccessible = false)
	public JComponent showXPathExecutor(UIPluginContext context, XLog source) {

		try {
			return new XPathExecutorPanel(context, source);
		} catch (IdentitiesMissingException e) {
			try {
				XLog log = context.tryToFindOrConstructFirstNamedObject(XLog.class, "Add identity attribute", null,
						null, source);
				return new XPathExecutorPanel(context, log);
			} catch (ConnectionCannotBeObtained e1) {
			} catch (IdentitiesMissingException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		context.getFutureResult(0).cancel(true);
		return null;
	}
}
