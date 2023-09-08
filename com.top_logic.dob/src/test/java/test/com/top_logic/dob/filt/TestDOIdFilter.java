/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.filt;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.basic.TLID;
import com.top_logic.dob.filt.DOIdFilter;
import com.top_logic.dob.simple.FileDataObject;

/**
 * Testcases for {@link com.top_logic.dob.filt.DOIdFilter}.
 *
 * @author  <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class TestDOIdFilter extends TestCase {

	/** filename of temp-file 1 */			
	public final String tmpFile1 		= "tmpFile1.txt";

	/** filename of temp-file 2 */			
	public final String tmpFile2 		= "tmpFile2.txt";

	/**
	 * Default Constructor.
	 *
	 * @param name  name of testFunction to perform. 
	 */
	public TestDOIdFilter (String name) {
		super(name);
	}

	/**
	 * The main-test-routine	 
	 */
	public void testMain() {
		
		FileDataObject fileDataObject;
		TLID id;
		boolean result;

		fileDataObject = new FileDataObject(new File(tmpFile1));		
		id = fileDataObject.getIdentifier(); 		
		DOIdFilter filter = new DOIdFilter(id); 
		result = filter.accept(fileDataObject);		
		assertTrue(result);
		
		fileDataObject = new FileDataObject(new File(tmpFile2));		
		id = fileDataObject.getIdentifier(); 		
		result = filter.accept(fileDataObject);		
		assertFalse(result);
	}

	/**
	 * Cleanup after test.
	 */
	@Override
	protected void tearDown() throws Exception {

		File f;
		String files[] = {tmpFile1, tmpFile2};
		
		for (int i = 0; i < files.length; i++) {
			
			f = new File(files[i]);
			if (f.exists() && !f.delete())
				System.err.println("Warning: File " + f + " could not be deleted.");					
		}		
	}
        
	/**
	 * the suite of tests to perform.
	 */
	public static Test suite () {
		TestSuite suite = new TestSuite (TestDOIdFilter.class);
		return suite;
	}

	/**
	 * main function for direct testing.
	 */
	public static void main (String[] args) {
		TestRunner.run(suite());
	}
}
