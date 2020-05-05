package org.processmining.contexts.uitopia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
public @interface Visualizer {

	public final static String USEPLUGINNAME = "Use Plugin Name";

	/**
	 * Returns the name of the visualizer.
	 * 
	 * @return A short string (< 20 characters)
	 */
	public String name() default USEPLUGINNAME;

	public String pack() default "";
}
