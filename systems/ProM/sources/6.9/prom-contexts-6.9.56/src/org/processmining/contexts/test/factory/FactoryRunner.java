/****************************************************************************
 * Copyright (c) 2011. All rights reserved. Ted Young.
 * Original source code published at
 * http://tedyoung.me/2011/01/22/junit-runtime-tests-overview/
 * 
 * Code adapted for ProM by Dirk Fahland.
 ****************************************************************************/
package org.processmining.contexts.test.factory;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

/**
 * A helper method that can be used to modify how JUnit executes a test suite.
 * Add annotation <code>@RunWith(FactoryRunner.class)</code> to the JUnit test
 * to hook this helper to the JUnit test suite. When executing the test suite
 * with JUnit, all methods of the test suite that are annotated with {@link TestFactory}
 * will be executed before all other tests. Each of these methods dynamically
 * adds a number of JUnit tests to the test suite.
 * 
 * @author Ted Young
 */
public class FactoryRunner extends BlockJUnit4ClassRunner {
	protected LinkedList<FrameworkMethod> tests = new LinkedList<FrameworkMethod>();

	public FactoryRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
		try {
			computeTests();
		}
		catch (Exception e) {
			throw new InitializationError(e);
		}
	}

	protected void computeTests() throws Exception {
		tests.addAll(super.computeTestMethods());
		tests.addAll(computeFactoryTests());
		
		// This is called here to ensure the test class constructor is called at least
		// once during testing.  If a test class has only TestFactories, than the 
		// test class will never be instantiated by JUnit.
		createTest();
	}
	
	protected Collection<? extends FrameworkMethod> computeFactoryTests() throws Exception {
		List<FrameworkFactoryTest> tests = new LinkedList<FrameworkFactoryTest>();

		// Final all methods in our test class marked with @TestFactory.
		for (FrameworkMethod method: getTestClass().getAnnotatedMethods(TestFactory.class)) {
			// Make sure the TestFactory method is static
			if (!Modifier.isStatic(method.getMethod().getModifiers()))
				throw new InitializationError("TestFactory " + method + " must be static.");

			// Execute the method (statically)
			Object instances = method.getMethod().invoke(getTestClass().getJavaClass());

			// Did the factory return an array?  If so, make it a list.
			if (instances.getClass().isArray())
				instances = Arrays.asList((Object[]) instances);

			// Did the factory return a scalar object?  If so, put it in a list.
			if (!(instances instanceof Iterable<?>))
				instances = Collections.singletonList(instances);

			// For each object returned by the factory.
			for (Object instance: (Iterable<?>) instances) {
				// Find any methods marked with @FactoryTest.
				for (FrameworkMethod m: new TestClass(instance.getClass()).getAnnotatedMethods(FactoryTest.class))
					tests.add(new FrameworkFactoryTest(m.getMethod(), instance, method.getName()));
			}
		}

		return tests;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.junit.runners.BlockJUnit4ClassRunner#computeTestMethods()
	 */
	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		return tests;
	}
	
	/**
	 * This method overrides the original method <code>validateInstanceMethods(...)</code>
	 * of {@link BlockJUnit4ClassRunner} to avoid a crash of the JUnit test suite caused
	 * by adding {@link FactoryTest}s to the test suite before pre-defined tests are loaded.
	 * 
	 * The original method is marked <code>@Deprecated</code>. 
	 * TODO: Remove this overriding method once the original is removed from the JUnit framework. 
	 */
	@Override
	@SuppressWarnings("deprecation")
	protected void validateInstanceMethods(List<Throwable> errors) {
		validatePublicVoidNoArgMethods(After.class, false, errors);
		validatePublicVoidNoArgMethods(Before.class, false, errors);
		validateTestMethods(errors);
	}
}



