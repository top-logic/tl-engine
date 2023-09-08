/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.filt;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.dob.DOBTestSetup;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.filt.DOTypeFilter;
import com.top_logic.dob.filt.DOTypeNameFilter;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.dob.simple.FileDataObject;
import com.top_logic.dob.simple.FileMetaObject;

/**
 * Testcases for the filters found in the corresponding package.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestFilters extends BasicTestCase {

    /** 
     * Test the {@link DOTypeFilter}.
     */
    public void testDOTypeFilter() {
        
        DOTypeFilter dotf = new DOTypeFilter(FileMetaObject.SINGLETON);
        
		FileDataObject fdo = new FileDataObject(
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/dob/filt/TestFilters.java"));
        ExampleDataObject edo = new ExampleDataObject(1);
        
        assertTrue (dotf.accept(fdo));
        assertFalse(dotf.accept(edo));
    }
    
    /** 
     * Test the {@link DOTypeNameFilter};
     */
    public void testDOTypeNameFilter() {
        
        DOTypeNameFilter dotnf = new DOTypeNameFilter("File");
        
		FileDataObject fdo = new FileDataObject(
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/dob/filt/TestFilters.java"));
        ExampleDataObject edo = new ExampleDataObject(1);
        
        assertTrue (dotnf.accept(fdo));
        assertFalse(dotnf.accept(edo));
    }

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestFilters.class);
        // TestSuite suite = new TestSuite ();
        // suite.addTest(new TestDOFactory("nameOfTest"));
        return DOBTestSetup.createDOBTestSetup(suite);
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
