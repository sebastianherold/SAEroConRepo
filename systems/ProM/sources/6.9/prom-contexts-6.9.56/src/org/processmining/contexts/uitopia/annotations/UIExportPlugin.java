package org.processmining.contexts.uitopia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface UIExportPlugin {
	public String description();

	public String extension();

	public String pack() default "";
}
