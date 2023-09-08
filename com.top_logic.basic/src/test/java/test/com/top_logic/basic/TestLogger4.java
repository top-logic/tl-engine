/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;

/**
 * Testcase for {@link com.top_logic.basic.Logger}.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestLogger4 extends BasicTestCase {

    /**
     * Default Constructor for TestLogger4.
     * 
     * @param name function name of test to execute
     * 
     */
    public TestLogger4(String name) {
        super(name);
    }

    /** remove log-files since they will be appended otherwise. */
    public void cleanup() {
        File logAll = new   File("tmp/logAll.log");
        logAll.delete();
    }

    /** reset logging to default so other Tests are OK */
    public void setDefaultLogging() {
		Logger.configureStdout();
    }
    
    /** In Theory someone could subclass the Logger4 ... */
    public void testCtor() {
        Logger logx = new Logger() {
            @Override
			public String toString() { return super.toString(); }
        };
        assertNotNull(logx);
    }
        
    /** 
     * Do all variants of logging, to be combined with some configurations. 
     */
    protected void logAll(String message) {
        Throwable tr = new Exception(message);
        
        Logger.debug("testLoggAll()",       null);
        Logger.debug("testLoggAll()",       "testLoggAll");
        Logger.debug("testLoggAll()",       TestLogger4.class);
        Logger.debug("testLoggAll()",       this);
        Logger.debug("testLoggAll()", tr  , this);
        Logger.debug(null           , tr  , this);
        Logger.debug("testLoggAll()", null, this);
        Logger.debug(null           , null, this);
        
        Logger.info("testLoggAll()",       null);
        Logger.info("testLoggAll()",       "testLoggAll");
        Logger.info("testLoggAll()",       TestLogger4.class);
        Logger.info("testLoggAll()",       this);
        Logger.info("testLoggAll()", tr  , this);
        Logger.info(null           , tr  , this);
        Logger.info("testLoggAll()", null, this);
        Logger.info(null           , null, this);

        Logger.warn("testLoggAll()",       null);
        Logger.warn("testLoggAll()",       "testLoggAll");
        Logger.info("testLoggAll()",       TestLogger4.class);
        Logger.info("testLoggAll()",       this);
        Logger.info("testLoggAll()", tr  , this);
        Logger.info(null           , tr  , this);
        Logger.info("testLoggAll()", null, this);
        Logger.info(null           , null, this);

        Logger.error("testLoggAll()",       null);
        Logger.error("testLoggAll()",       "testLoggAll");
        Logger.error("testLoggAll()",       TestLogger4.class);
        Logger.error("testLoggAll()",       this);
        Logger.error("testLoggAll()", tr  , this);
        Logger.error(null           , tr  , this);
        Logger.error("testLoggAll()", null, this);
        Logger.error(null           , null, this);

        Logger.fatal("testLoggAll()",       null);
        Logger.fatal("testLoggAll()",       "testLoggAll");
        Logger.fatal("testLoggAll()",       TestLogger4.class);
        Logger.fatal("testLoggAll()",       this);
        Logger.fatal("testLoggAll()", tr  , this);
        Logger.fatal(null           , tr  , this);
        Logger.fatal("testLoggAll()", null, this);
        Logger.fatal(null           , null, this);

    }

    /** Test Debugging with everything enabled */
	public void testLoggAll() {
        configureLogAll();
        
        logAll("testLoggAll() -- Testing");
		assertTrue("Ticket #26497: Improve the Log4j support after switching to version 2.", Logger.isDebugEnabled(this));
        assertTrue(Logger.isTraceExceptions());
        assertTrue(Logger.isTraceMessages());
        
        Logger.setTraceExceptions(false);
        Logger.setTraceMessages(false);
        assertFalse(Logger.isTraceExceptions());
        assertFalse(Logger.isTraceMessages());

        setDefaultLogging();
    }

    /** 
     * Configure log All (to be reused by other tests)
     */
	protected static void configureLogAll() {
		Logger.configure(TestLogger4.class.getResource("TestLogger4.logAll.xml"));
    }

    /** Test Debugging with everything enabled */
	public void testLoggAllNoTrace() {
		Logger.configure(TestLogger4.class.getResource("TestLogger4.logAll.noTrace.xml"));
        logAll("testLoggAllNoTrace() -- Testing");
		assertTrue("Ticket #26497: Improve the Log4j support after switching to version 2.", Logger.isDebugEnabled(this));
        setDefaultLogging();
    }

    /**
     * Test for logging with some Not Existing Properties.
     */
	public void testLogBroken() throws MalformedURLException {
		Logger.configure(new URL("file:test/com/top_logic/basic/NoSuch.properties"));
        setDefaultLogging();
    }
    
    /** assert to the sizes of the logfiles at last (bad idea !).
     * 
     * The size of the log files depends on the depth of the
     * stack, wich is different when called from "upper" testcases...
     */
    public void assertSizes() {

        File logAll = new   File("tmp/logAll.log");
		assertTrue("Ticket #26497: Improve the Log4j support after switching to version 2.", logAll.exists());
		assertTrue(logAll.length() > 0);
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite ();

        suite.addTest(new TestLogger4("cleanup"));
        suite.addTest(new TestSuite (TestLogger4.class));
        suite.addTest(TestUtils.tryEnrichTestnames(new TestLogger4("setDefaultLogging"), "1"));
        suite.addTest(new TestLogger4("assertSizes"));
        suite.addTest(TestUtils.tryEnrichTestnames(new TestLogger4("setDefaultLogging"), "2"));
        
        return BasicTestSetup.createBasicTestSetup(suite);
        // return new TestLogger("xxx");
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {

        junit.textui.TestRunner.run (suite ());
    }

}
