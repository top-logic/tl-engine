/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Test {@link TopLogicException}.
 * 
 * @author <a href=mailto:kha@top-logic.com>kha</a>
 */
@SuppressWarnings("javadoc")
public class TestTopLogicException extends BasicTestCase {

    /** Default CTor, create a test with this function Name */
    public TestTopLogicException (String name) {
        super (name);
    }
    
	public void testMessage() {
		TopLogicException ex = new TopLogicException(ResKey.forTest("some.key"));

		assertEquals("(some.key)", ex.getLocalizedMessage());
	}

	public void testDefaultDetails() {
		TopLogicException ex = new TopLogicException(ResKey.forTest("some.key"));
		String details = Resources.getInstance(Locale.ENGLISH).getString(ex.getDetails());
		assertEquals("(some.key" + ResKey.TOOLTIP + ")", details);
	}

	public void testDetails() {
		TopLogicException ex = new TopLogicException(ResKey.forTest("some.key"));
		ex.initDetails(ResKey.forTest("some.detail"));
		String details = Resources.getInstance(Locale.ENGLISH).getString(ex.getDetails());
		assertEquals("(some.detail)", details);
	}

    /**
     * test a non-chained TopLogicException:
     * EN-message, stacktrace present.
     */
    public void testSingleTLEX() {
        try {
			throw new TopLogicException(TestTopLogicException.class, "testSingle",
                                        new Object[] {"param1", "param2"});
        } catch (TopLogicException tlx) {
            StringWriter sw = new StringWriter(2048);
            PrintWriter  pw = new PrintWriter(sw);
            
            tlx.printStackTrace(pw);
            String trace = sw.toString();
            
			assertTrue("trace does not contain localized (EN) message: " + trace,
				trace.indexOf("single " + "param1" + " tl " + "param2" + " exception english") >= 0);
        }
    }
    
    /**
     * test for the stacking of exceptions
     */
	public void testCauseTLExWorking() {
        NullPointerException exp = new NullPointerException ("This is just the root cause");
        TopLogicException tlxp = new TopLogicException (this.getClass (),"upper",null,exp);
        assertNotNull ("We didn't get the root cause", tlxp.getCause());
        assertNotNull (tlxp.getCause());
        
    }
    /**
     * test if very first exception's stackTrace is logged.
     * there is a known bug that for a wrapped standalone 
     * TLX the wrong message at top of stacktrace is printed because an inner called getLocalizedMessage()
     * refers to the upper most tlx, not the root.
     */
    public void testCauseTLEx() throws IOException {
        
        TopLogicException tlxUpper = null;
        TopLogicException tlxMid   = null;
		TopLogicException tlxLower = new TopLogicException(TestTopLogicException.class, "lower");
        try {
            throw tlxLower;
            //throw new NullPointerException("npe");
        } catch (Exception tlxCaught) {
			tlxMid = new TopLogicException(TestTopLogicException.class,
					"mid", null, tlxCaught);
        }
        try {
            throw tlxMid;
        } catch (TopLogicException tlxCaught) {
			tlxUpper = new TopLogicException(TestTopLogicException.class, "upper", null, tlxCaught);
        }
        
                
        //getCause may not be null
        assertNotNull("current tlx has no cause", tlxUpper.getCause());
    }

	/**
	 * Test the Scenario with German Locale
	 */
	public void testSzenarioDE() throws Exception {
        
        executeInLocale(Locale.GERMANY, new Execution() {
			
			@Override
			public void run() throws Exception {
				doTestWithEnglishResults();
			}
		});
    }

	/**
	 * Test the Scenario with English {@link Locale}
	 */
	public void testSzenarioEN() throws Exception {
		executeInLocale(Locale.ENGLISH, new Execution() {

			@Override
			public void run() throws Exception {
				doTestWithEnglishResults();
			}
		});
	}

	void doTestWithEnglishResults() throws IOException {
        try {
            func3();
        }
        catch (TopLogicException tlx) {
            assertTrue("toString did not deliver localized message of exception", 
                    tlx.toString()           .indexOf("function3") >= 0);
         assertTrue("getMessage did not deliver non-localized message of exception",
				tlx.getLocalizedMessage().indexOf("function3") >= 0);
         assertTrue("getLocalizedMessage did not deliver localized message of exception", 
                    tlx.getLocalizedMessage().indexOf("function3") >= 0);
         
         StringWriter sw = new StringWriter(2048);
         PrintWriter  pw = new PrintWriter(sw);
         
         tlx.printStackTrace(pw);
         String trace = sw.toString();
         
         assertTrue("trace does not contain localized (EN) message of inner exception",
                    trace.indexOf("function1") >= 0);
         assertTrue("trace does not contain localized (EN) message of inner exception",
                 trace.indexOf("function2") >= 0);
         assertTrue("trace does not contain parameter of inner exception",
                 trace.indexOf("Testing") >= 0);
         
         trace = null;
         pw.close();
         sw.close();
         
         ByteArrayOutputStream bo = new ByteArrayOutputStream(2048);
         PrintStream           ps = new PrintStream(bo);
         tlx.printStackTrace(ps);//TODO kha what for?
				}
    }

    /** A function to check the ExceptionHandling */
    public void func3() {
        try {
            func2("Testing", new Date()); 
        }
        catch (TopLogicException tlx) {
			throw new TopLogicException(TestTopLogicException.class, "func3", null, tlx);
        }
        catch (Exception any) {
			throw new TopLogicException(TestTopLogicException.class, "func3", null, any);
        }
    }

    /** A function to check the ExceptionHandling */
    public void func2(String aString, Date aDate) {
        try {
            func1(); 
        }
        catch (Exception any) {
			throw new TopLogicException(TestTopLogicException.class, "func2",
                  new Object[] {aString, aDate}, any);
        }
    }

    /** A function to check the ExceptionHandling :
     * catches exception, wraps it and throws again*/
    public void func1() {
        try {
            func0(); 
        }
        catch (Exception any) {
			throw new TopLogicException(TestTopLogicException.class, "func1", null, any);
        }
    }

    /** A function to check the ExceptionHandling:
     * throws an exception */
    public void func0() {
        assertEquals(0, 1/0);
        fail ("we must not reach this point as a DivisionByZero comes first");
        //assertEquals(0, egal); /* never reached */
    }

    /** Return the suite of Tests to perform */
    public static Test suite () {
       TestSuite suite = new TestSuite (TestTopLogicException.class);
       // Test suite = new TestTopLogicException("testSzenarioEN");
       return TLTestSetup.createTLTestSetup(suite);
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        Logger.configureStdout();//"INFO");
        junit.textui.TestRunner.run (suite ());
    }

}
