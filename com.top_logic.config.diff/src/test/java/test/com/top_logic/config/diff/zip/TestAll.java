/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.diff.zip;



import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;

/**
 * All tests in this package.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestAll {
    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite(TestAll.class.getPackage().getName());
        suite.addTest(test.com.top_logic.config.diff.zip.TestZipReader.suite());
        suite.addTest(com.top_logic.config.diff.google.diff_match_patch_test.suite());
        return suite;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        Logger.configureStdout();

        junit.textui.TestRunner.run (TestAll.suite ());
    }
}
