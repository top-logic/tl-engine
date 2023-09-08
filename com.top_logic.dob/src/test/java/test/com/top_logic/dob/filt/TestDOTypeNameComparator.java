/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.filt;

import java.beans.IntrospectionException;
import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.bean.BeanDataObject;
import com.top_logic.dob.filt.DOTypeNameComparator;
import com.top_logic.dob.simple.FileDataObject;

/**
 * Test for {@link com.top_logic.dob.filt.DOTypeNameComparator }.
 *
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class TestDOTypeNameComparator extends TestCase {

	/**
	 * Default Constructor.
	 *
	 * @param name  name of testFunction to perform. 
	 */
	public TestDOTypeNameComparator(String name) {
		super(name);
	}
    
    /**
     * Test the compare() method.
     */
	public void testCompare() throws IntrospectionException {
		
		File thisFile =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/dob/filt/TestDOTypeNameComparator.java");
        
        FileDataObject fdo = new FileDataObject(thisFile);
        BeanDataObject bdo = new BeanDataObject(thisFile);
        
        DOTypeNameComparator dtnc = DOTypeNameComparator.SINGLETON;
        
        assertEquals(0 , dtnc.compare(fdo, fdo));
        assertEquals(0 , dtnc.compare(bdo, bdo));
        assertTrue  (dtnc.compare(fdo, bdo) < 0);
        assertTrue  (dtnc.compare(bdo, fdo) > 0);
    }
    
	/**
	 * the suite of tests to perform.
	 */
	public static Test suite () {
		return new TestSuite (TestDOTypeNameComparator.class);
	}

	/**
	 * main function for direct testing.
	 */
	public static void main (String[] args) {
		junit.textui.TestRunner.run (suite ());
	}
}
