package org.processmining.contexts.uitopia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used in the interface
 * org.processmining.contexts.uitopia.specialplugins.ImportPlugin to signal the
 * framework that this is an import plugin.
 * 
 * @author bfvdonge
 * 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface UIImportPlugin {
	public String description();

	public String[] extensions();
	
	public String pack() default "";
}
