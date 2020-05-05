package org.processmining.log.xsl.transformer;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

public class XSLTTransformer {

	
	
	private void executeXSLT(Source xslt) {
		
        try {
        	Processor proc = new Processor(false);
            XsltCompiler comp = proc.newXsltCompiler();
            
        	XsltExecutable exp = comp.compile(xslt);
			
			
			
		} catch (SaxonApiException e) {

			e.printStackTrace();
		}
        
		
	}
	
}
