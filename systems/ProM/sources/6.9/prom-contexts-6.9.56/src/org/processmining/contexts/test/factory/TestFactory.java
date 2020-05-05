/****************************************************************************
 * Copyright (c) 2011. All rights reserved. Ted Young.
 * Original source code published at
 * http://tedyoung.me/2011/01/22/junit-runtime-tests-overview/
 * 
 * Code adapted for ProM by Dirk Fahland.
 ****************************************************************************/
package org.processmining.contexts.test.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * This annotation can be added to a method in a JUnit test class. When
 * executing the JUnit test suite, a {@link FactoryRunner} that is hooked
 * to the JUnit Test will use the annotated method to generate and add
 * new JUnit to the test suite. The annotated Method has to return a 
 * {@link List} of Objects that have member methods that are annotated
 * with {@link FactoryTest}.
 *  
 * @author Ted Young
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestFactory {
}
