package org.processmining.contexts.test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class to collect all test scripts annotated in a given directory.
 * 
 * @author dfahland
 */
public class AllStandardScriptTests {
	
	public static final String TESTSCRIPT_FILE_EXTENSION = "txt";

	/**
	 * @param lookUpDir
	 * @return all test script files in the given directory
	 */
	public static List<String> getAllTestScripts(String lookUpDir) {
		
		System.out.println("Collecting scripted tests from "+lookUpDir);
		
		List<String> testScriptFiles = new LinkedList<String>();
		
		File dir = new File(lookUpDir);
		if (dir.isDirectory()) {
			for (String file : dir.list()) {
				if (file.endsWith(TESTSCRIPT_FILE_EXTENSION)) {
					testScriptFiles.add(file);
				}
			}
		} else if (lookUpDir.endsWith(TESTSCRIPT_FILE_EXTENSION)) {
			testScriptFiles.add(lookUpDir);
		}
		
		return testScriptFiles;
	}

}
