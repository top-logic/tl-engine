/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.filt;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.filt.DOIdComparator;
import com.top_logic.dob.simple.FileDataObject;

/**
 * Test for {@link com.top_logic.dob.filt.DOIdComparator}.
 *
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class TestDOIdComparator extends TestCase {

	/** filename of temp-file 1 */			
	public final String tmpFile1 		= "tmpFile1.txt";

	/** filename of temp-file 2 */			
	public final String tmpFile2 		= "tmpFile2.txt";

	/**
	 * Default Constructor.
	 *
	 * @param name  name of testFunction to perform. 
	 */
	public TestDOIdComparator(String name) {
		super(name);
	}
    
	public void testMain() {
		
		DOIdComparator 	comp = null;
		
		int result;
		
		File f1 = new File(this.tmpFile1);
		File f2 = new File(this.tmpFile2);
		
		FileDataObject fileDataObject1 = new FileDataObject(f1);  
		FileDataObject fileDataObject2 = new FileDataObject(f2);
		
		comp = new DOIdComparator(DOIdComparator.ASCENDING);
		
		result = comp.compare(fileDataObject1, fileDataObject2);
		assertTrue(result < 0);
		
		result = comp.compare(fileDataObject1, fileDataObject1);
		assertEquals(0, result);

		result = comp.compare(fileDataObject2, fileDataObject2);
		assertEquals(0, result);
				
		result = comp.compare(fileDataObject2, fileDataObject1);
		assertTrue(result > 0);		
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
		return new TestSuite (TestDOIdComparator.class);
	}

	/**
	 * main function for direct testing.
	 */
	public static void main (String[] args) {
		junit.textui.TestRunner.run (suite ());
	}
}
