/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.v1.TestingVersion1;

import com.top_logic.basic.version.Version;

/**
 * Testcase for {@link com.top_logic.basic.version.Version}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestAbstractVersion extends BasicTestCase {
    
    /** Test basic fucntions like equals() */
    public void testBasics() {
		Version av2 = TestingVersion1.getInstance();
        
        String verString = av2.toString();
		assertEquals("Basic2          2.3.99 (2009.12.11.12.59.13)", verString);
    }
    
    /**
     * Test {@link Version#getBuildQualifier()}.
     */
    public void testBuildQualifier() {
		Version av1 = TestingVersion1.getInstance();
    	
		assertEquals("2009.12.11.12.59.13", av1.getBuildQualifier());
    }

    /**
     * Class under test for void printVersions(PrintStream)
     */
    public void testPrintVersionsPrintStream() {
		Version av2 = TestingVersion1.getInstance();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        PrintStream           ps  = new PrintStream(bos);
        av2.printVersions(ps);
        String versions = bos.toString();
        assertTrue(versions.indexOf("Basic2          2.3.99")  >= 0); 
    }

    /**
     * the suite of test to perform
     */
    public static Test suite () {

        return BasicTestSetup.createBasicTestSetup(new TestSuite (TestAbstractVersion.class));
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
