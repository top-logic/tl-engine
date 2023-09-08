/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.IOException;
import java.sql.SQLException;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.ExceptionGenerator;


/**
 * Testcase for the {@link com.top_logic.basic.ExceptionGenerator}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestExceptionGenerator extends TestCase {

    public void testActivate() throws Exception {
        String theKey = "TestActivate";

        ExceptionGenerator.activate();

        ExceptionGenerator.activate(theKey);
        try {
        	this.doTestActivate(theKey);
        } finally {
        	ExceptionGenerator.deactivate(theKey);
        }
    }

    public void testActivateNo() throws Exception {
        String theKey = "testActivateNo";

        ExceptionGenerator.activate();

        this.doTestDeactivate(theKey);
    }

    public void testDeactivate() throws Exception {
        String theKey = "TestDeactivate";

        ExceptionGenerator.deactivate();

        ExceptionGenerator.activate(theKey);
        try {
        	this.doTestDeactivate(theKey);
        } finally {
        	ExceptionGenerator.deactivate(theKey);
        }
    }

    protected void doTestActivate(String theKey) throws Exception {
        try {
            ExceptionGenerator.throwException(theKey);
            Assert.fail("Should throw Exception");
        }
        catch (Exception ex) {
            // Expected behavior
        }

        try {
            ExceptionGenerator.throwIOException(theKey);
            Assert.fail("Should throw IOException");
        }
        catch (IOException ex) {
            // Expected behavior
        }

        try {
            ExceptionGenerator.throwIllegalArgumentException(theKey);
            Assert.fail("Should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException ex) {
            // Expected behavior
        }

        try {
            ExceptionGenerator.throwSQLException(theKey);
            Assert.fail("Should throw SQLException");
        }
        catch (SQLException ex) {
            // Expected behavior
        }

        try {
            ExceptionGenerator.throwNullPointerException(theKey);
            Assert.fail("Should throw NullPointerException");
        }
        catch (NullPointerException ex) {
            // Expected behavior
        }
    }

    protected void doTestDeactivate(String theKey) throws Exception {
        Assert.assertTrue(ExceptionGenerator.throwException(theKey));
        Assert.assertTrue(ExceptionGenerator.throwIOException(theKey));
        Assert.assertTrue(ExceptionGenerator.throwIllegalArgumentException(theKey));
        Assert.assertTrue(ExceptionGenerator.throwSQLException(theKey));
        Assert.assertTrue(ExceptionGenerator.throwNullPointerException(theKey));
    }

    /** Consturct a new ExceptionGenerator (make coverage happy) */
    public void testCTor() {
        ClassExceptionGenerator exp = new ClassExceptionGenerator();
        assertNotNull(exp);
        
        ClassExceptionGenerator.activate();
        ClassExceptionGenerator.activate("String");
        try {
            ClassExceptionGenerator.throwClassCastException("String");
            fail("Should throw ClassCastException");
        }
        catch (ClassCastException ex) {
            // Expected behavior
        }

    }
    
    /** Example of a Subclass for ExceptionGenerator */
    public static class ClassExceptionGenerator extends ExceptionGenerator {
        
        /**
         * @param    aKey    The activation flag.
         * @return   <code>true</code>.
         * @throws   IllegalArgumentException    If generator is active and given flag is activated.
         */
        public static boolean throwClassCastException(String aKey) throws ClassCastException {
            if (ExceptionGenerator.checkFlag(aKey)) {
                throw new ClassCastException("Generated exception '" + aKey + '\'');
            }

            return (true);
        }
    }

    /**
     * the suite of test to perform
     */
    public static Test suite () {
        return (new TestSuite (TestExceptionGenerator.class));
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
