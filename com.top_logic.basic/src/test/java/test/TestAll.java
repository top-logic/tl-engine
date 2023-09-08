/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.util.AbstractBasicTestAll;

/**
 * This class collects all sub-tests to the final big test.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestAll extends AbstractBasicTestAll {

    /**
     * Return the suite of tests to perform.
     */
    public static Test suite () {
		return new TestAll().buildSuite();
    }

    /** 
     * main function for direct execution.
     */
    public static void main (String[] args) {
        TestRunner trunner = new TestRunner();
            // new DetailedResultPrinter(System.out));
            // In case you want to the the errors right away and more ....
        trunner.doRun(suite ());
    }

}
