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

/**
 * This annotation declares a Java method as 'Test Factory Method'
 * which will be turned into a JUnit test for each object that is
 * registered at the {@link TestFactory}.
 *  
 * @author Ted Young
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactoryTest {
}
