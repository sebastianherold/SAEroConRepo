package org.processmining.contexts.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.AssertionFailedError;

/**
 * Exception that stores all collected failed tests that arised in {@link PromTestFramework}.
 * Call {@link #toString()} for a formatted test report.
 * 
 * @author dfahland
 */
public class PromTestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3476724712772295479L;
	
	/**
	 * Associates a test case to a thrown exception that occurred during
	 * its execution.
	 */
	protected static class WrappedException {
		protected Method test = null;
		protected String testScript = null;
		protected Throwable thrown = null;
		
		public WrappedException(Method test, Throwable thrown) {
			this.test = test;
			this.thrown = thrown;
		}
		
		public WrappedException(String testScript, Throwable thrown) {
			this.testScript = testScript;
			this.thrown = thrown;
		}
	}
	
	/**
	 * Associates a test case to expected and returned results in case the test was not
	 * successful.
	 */
	protected static class ResultMismatch {
		protected Method test = null;
		protected String expected = null;
		protected String result = null;
		
		protected String testScript = null;
		protected AssertionFailedError junitResult = null;
		
		public ResultMismatch(Method test, String expected, String result) {
			this.test = test;
			this.expected = expected;
			this.result = result;
		}
		
		public ResultMismatch(String testScript, AssertionFailedError junitResult) {
			this.testScript = testScript;
			this.junitResult = junitResult;
		}

	}
	
	private List<ResultMismatch> failures;
	private List<WrappedException> errors;
	
	public PromTestException(List<ResultMismatch> failures, List<WrappedException> errors) {
		this.failures = failures;
		this.errors = errors;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();

		sb.append("============================================================\n");
		sb.append("Number of failed tests: "+failures.size()+"\n");
		sb.append("============================================================\n");
		for (ResultMismatch fail : failures) {
			if (fail.test != null)
				sb.append(getTestName(fail.test)+"\n");
			else
				sb.append(fail.testScript+"\n");
			
			if (fail.junitResult != null) {
				sb.append("--- ASSERTION FAILED ---------------------------------------\n");
				sb.append(fail.junitResult.toString()+"\n");				
				sb.append("============================================================\n");
			} else {
				sb.append("--- RESULT -------------------------------------------------\n");
				sb.append(fail.result+"\n");
				sb.append("--- EXPECTED -----------------------------------------------\n");
				sb.append(fail.expected+"\n");
				sb.append("============================================================\n");
			}
		}
		
		sb.append("============================================================\n");
		sb.append("Number of errors: "+errors.size()+"\n");
		sb.append("============================================================\n");
		for (WrappedException error : errors) {
			if (error.test != null)
				sb.append(getTestName(error.test)+"\n");
			else
				sb.append(error.testScript+"\n");
			
			sb.append("--- EXCEPTION ----------------------------------------------\n");
			sb.append(error.thrown.toString()+"\n");
			sb.append("--- stack trace --------------------------------------------\n");
		    final StringWriter result = new StringWriter();
		    final PrintWriter printWriter = new PrintWriter(result);
		    error.thrown.printStackTrace(printWriter);
			sb.append(result.toString()+"\n");
			sb.append("============================================================\n");
		}

		return sb.toString();
	}

	private static String getTestName (Method m) {
		return m.getClass().toString()+"."+m.getName();
	}
}
