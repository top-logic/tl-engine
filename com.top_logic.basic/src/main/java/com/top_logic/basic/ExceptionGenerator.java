/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Generates exceptions, when flag {@link #active} is set to <code>true</code>.
 * 
 * This generator can be used to check the catch blocks in your code, even when
 * you are not able to build up a test suite, which leads to the needed exception.
 * 
 * For using it please put the followin code in your method to be tested:
 * <pre>
 * package com.top_logic.basic;
 * 
 * public class DemoClass {
 * 
 *     public boolean makeItSo(String aName) throws DemoException {
 *         try {
 *             assert ExceptionGenerator.throwIOException("com.top_logic.basic.DemoClass#makeItSo.1");
 *             
 *             File theFile = new File(aName).exists();
 *             ...
 *         }
 *         catch (IOException ex) {
 *             throw new DemoException("Unable to make it so with " + aName, ex);
 *         }
 *     }
 *     ...
 * }
 * </pre>
 * Next is the code in the test case, which will check the behavior:
 * <pre>
 * package test.com.top_logic.basic;
 * 
 * public class TestDemoClass {
 * 
 *     public void testMakeItSo() throws Exception {
 *         DemoClass theDemo = new DemoClass();
 *         String    theName = "MyFileName";
 *         
 *         // First the normal test
 *         theDemo.makeItSo(theName);
 *         
 *         // Now activate the exceptionGenerator to test the failure handling.
 *         ExceptionGenerator.activate("com.top_logic.basic.DemoClass#makeItSo.1");
 *         
 *         try {
 *             theDemo.makeItSo(theName);
 *             Assert.fail("Method should throw an DemoException");
 *         }
 *         catch (DemoException ex) {
 *             // Expected behavior
 *         }
 *         finally {
 *             ExceptionGenerator.deactivate("com.top_logic.basic.DemoClass#makeItSo.1");
 *         }
 *     }
 *     ...
 * }
 * </pre>
 * Be shure to deactivate the generator afterwards for the tested exception. Moreover
 * you have to activate the "assert" handling of your VM.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class ExceptionGenerator {

    /** Flag, if this generator should throw exceptions. */
    protected static boolean active = false;

    /** Set of currently active keys, which will generate an exception. */
    protected static Set activeFlags = new HashSet();

    /**
     * Check, if the generator is generally active and if the given key is active
     * for throwing an exception.
     *  
     * @param    aKey    The flag to be checked.
     * @return   <code>true</code>, if an exception has to be thrown.
     */
    protected static boolean checkFlag(String aKey) {
        return ExceptionGenerator.active && ExceptionGenerator.activeFlags.contains(aKey);
    }

    /**
     * @param    aKey    The activation flag.
     * @return   <code>true</code>.
     * @throws   Exception    If generator is active and given flag is activated.
     */
    public static boolean throwException(String aKey) throws Exception {
        if (ExceptionGenerator.checkFlag(aKey)) {
            throw new Exception("Generated exception '" + aKey + '\'');
        }

        return (true);
    }

    /**
     * @param    aKey    The activation flag.
     * @return   <code>true</code>.
     * @throws   IOException    If generator is active and given flag is activated.
     */
    public static boolean throwIOException(String aKey) throws IOException {
        if (ExceptionGenerator.checkFlag(aKey)) {
            throw new IOException("Generated exception '" + aKey + '\'');
        }

        return (true);
    }

    /**
     * @param    aKey    The activation flag.
     * @return   <code>true</code>.
     * @throws   IllegalArgumentException    If generator is active and given flag is activated.
     */
    public static boolean throwIllegalArgumentException(String aKey) throws IllegalArgumentException {
        if (ExceptionGenerator.checkFlag(aKey)) {
            throw new IllegalArgumentException("Generated exception '" + aKey + '\'');
        }

        return (true);
    }

    /**
     * @param    aKey    The activation flag.
     * @return   <code>true</code>.
     * @throws   NullPointerException    If generator is active and given flag is activated.
     */
    public static boolean throwNullPointerException(String aKey) throws NullPointerException {
        if (ExceptionGenerator.checkFlag(aKey)) {
            throw new NullPointerException("Generated exception '" + aKey + '\'');
        }

        return (true);
    }

    /**
     * @param    aKey    The activation flag.
     * @return   <code>true</code>.
     * @throws   SQLException    If generator is active and given flag is activated.
     */
    public static boolean throwSQLException(String aKey) throws SQLException {
        if (ExceptionGenerator.checkFlag(aKey)) {
            throw new SQLException("Generated exception '" + aKey + '\'');
        }

        return (true);
    }

    /**
     * Active the generator for the given activation key.
     * 
     * @param    aKey    The activation key to be enabled.
     * @return   If activation succeeds.
     */
    public synchronized static boolean activate(String aKey) {
        return (ExceptionGenerator.activeFlags.add(aKey) && ExceptionGenerator.active);
    }

    /**
     * Deactivate the generator for the given activation key.
     * 
     * @param    aKey    The activation key to be disabled.
     * @return   If deactivation succeeds.
     */
    public synchronized static boolean deactivate(String aKey) {
        return (ExceptionGenerator.activeFlags.remove(aKey) && ExceptionGenerator.active);
    }

    /**
     * Active the generator.
     */
    public synchronized static void activate() {
        ExceptionGenerator.active = true;
    }

    /**
     * Deactivate the generator.
     */
    public synchronized static void deactivate() {
        ExceptionGenerator.active = false;
    }
}
