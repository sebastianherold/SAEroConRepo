package org.processmining.log.plugins;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.XEventClassifierList;
import org.processmining.log.parsers.SaxHandlerGlobalEventAttributesParser;
import org.processmining.log.parsers.SaxHandlerXEventClassifierParser;
import org.processmining.plugins.log.OpenLogFilePlugin;

@Plugin(name = "Import XEvent Classifiers list form event log", parameterLabels = { "Filename" }, returnLabels = {
		"XEventClassifier List" }, returnTypes = {
				XEventClassifierList.class }, help = "This plugin performs a lightweight read of the XLog and retrieves the available XEventClassifiers as a list. This plugins is mainly used by RapidProM")
@UIImportPlugin(description = "Import XEvent Classifiers list form event log", extensions = { "xes", "zip", "gz" })
public class ImportXEventClassifierListPlugin extends OpenLogFilePlugin {
	protected XEventClassifierList importFromStream(PluginContext context, InputStream input, String filename,
			long fileSizeInBytes) throws Exception {
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		try {
			saxFactory.setValidating(false);
			saxFactory.setNamespaceAware(false);
			saxFactory.setSchema(null);
		} catch (UnsupportedOperationException e) {

		}
		InputStream is = getInputStream(getFile());
		SAXParser globalsParser = saxFactory.newSAXParser();
		SaxHandlerGlobalEventAttributesParser globalsHandler = new SaxHandlerGlobalEventAttributesParser();
		globalsParser.parse(is, globalsHandler);
		is.close();

		is = getInputStream(getFile());
		SAXParser classifiersParser = saxFactory.newSAXParser();
		SaxHandlerXEventClassifierParser classifiersHandler = new SaxHandlerXEventClassifierParser(
				globalsHandler.getGlobalEventAttributes());
		classifiersParser.parse(is, classifiersHandler);
		is.close();

		return classifiersHandler.getClassifierList();
	}
}
