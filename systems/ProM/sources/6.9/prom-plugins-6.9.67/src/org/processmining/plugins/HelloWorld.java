package org.processmining.plugins;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

public class HelloWorld {
        @Plugin(
                name = "My Hello World Plugin", 
                parameterLabels = {}, 
                returnLabels = { "Hello world string" }, 
                returnTypes = { String.class }, 
                userAccessible = true, 
                help = "Produces the string: 'Hello world'"
        )
        @UITopiaVariant(
                affiliation = "My company", 
                author = "My name", 
                email = "My e-mail address"
        )
        public static String helloWorld(PluginContext context) {
                return "Hello World";
        }
}