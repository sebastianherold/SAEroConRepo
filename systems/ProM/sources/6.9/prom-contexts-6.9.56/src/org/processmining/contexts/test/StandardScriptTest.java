package org.processmining.contexts.test;

import org.processmining.contexts.cli.CLI;
import org.processmining.contexts.test.factory.FactoryTest;

public class StandardScriptTest {
	
	private String testScript;
	
	public StandardScriptTest(String testScript) {
		this.testScript = testScript;
	}

	@FactoryTest
	public void test() throws Throwable {

		System.out.println("--- TESTCASE: "+testScript+" -----------------------");
		System.err.println("--- TESTCASE: "+testScript+" -----------------------");

		String args[] = new String[] {"-f", testScript};
		CLI.main(args);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return testScript;
	}
}
