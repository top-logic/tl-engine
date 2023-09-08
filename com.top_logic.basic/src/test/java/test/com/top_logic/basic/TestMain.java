/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.Main;
import com.top_logic.basic.util.Computation;

/**
 * Testcase for {@link com.top_logic.basic.Main}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestMain extends TestCase {

    /**
     * Test actual main() method.
     */
    public void testMain() throws Exception {
        final ByteArrayOutputStream bos    = new ByteArrayOutputStream(1024);
        final ByteArrayOutputStream berr   = new ByteArrayOutputStream(1024);
        PrintStream           out    = new PrintStream(bos);
        PrintStream           err    = new PrintStream(berr);
		Exception problem = BasicTestCase.runWithSystemOutAndErr(out, err, new Computation<Exception>() {

			@Override
			public Exception run() {
				try {
            Main.main(new String[0]);
            String sout = bos.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.startsWith("This tool does nothing"));
            bos.reset();
            
            Main.main(new String[] { "-h"} );
            sout = bos.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.indexOf("--help") > 0);
            bos.reset();

            Main.main(new String[] { "--help"} );
            sout = bos.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.indexOf("-h") > 0);
            bos.reset();
            berr.reset();

            try {
            	Main.main(new String[] { "--strange"} );
            	fail("Failure expected");
            } catch (AbortExecutionException ex) {
            	// expected.
            }
            sout = berr.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.indexOf("Unknown Option") >= 0);
            bos.reset();
            berr.reset();
            
            try {
            	Main.main(new String[] { "-"} );
            	fail("Failure expected");
            } catch (AbortExecutionException ex) {
            	// expected.
            }
            sout = berr.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.indexOf("not an option") >= 0);
            bos.reset();
            berr.reset();

            try {
            	Main.main(new String[] { "parameter"} );
            	fail("Failure expected");
            } catch (AbortExecutionException ex) {
            	// expected.
            }
            sout = berr.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.indexOf("parameter") >= 0);
            bos.reset();
            berr.reset();

            try {
            	Main.main(new String[] { "" } );
            	fail("Failure expected");
            } catch (AbortExecutionException ex) {
            	// expected.
            }
            sout = berr.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.indexOf("Unexpected") >= 0);
            bos.reset();
            berr.reset();

            try {
            	Main.main(new String[] { null } );
            	fail("Failure expected");
            } catch (AbortExecutionException ex) {
            	// expected.
            }
            sout = berr.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.indexOf("Unexpected") >= 0);
            bos.reset();
            berr.reset();

            try {
            	Main.main(new String[] { "-x"} );
            	fail("Failure expected");
            } catch (AbortExecutionException ex) {
            	// expected.
            }
            sout = berr.toString(); 
            // oldOut.println(sout);
            assertTrue(sout.indexOf("Unknown Option") >= 0);

            bos.reset();
            berr.reset();
					return null;
				} catch (Exception ex) {
					return ex;
				}
			}
		});
		if (problem != null) {
			throw problem;
		}
    }

    /**
     * Test non interactive flag.
     */
    public void testDefault() throws Exception {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ByteArrayOutputStream berr = new ByteArrayOutputStream();
        PrintStream           out    = new PrintStream(bos);
        PrintStream           err    = new PrintStream(berr);
		Exception problem = BasicTestCase.runWithSystemOutAndErr(out, err, new Computation<Exception>() {

			@Override
			public Exception run() {
				Main main = new Main(!Main.INTERACTIVE); // Framfurt am Main ?
				try {
					main.runMain(new String[0]);
				} catch (Exception ex) {
					return ex;
				}

				assertEquals(0, bos.size());
				assertEquals(0, berr.size());
				return null;
			}
		});
		if (problem != null) {
			throw problem;
		}
    }

    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite () {
        return new TestSuite (TestMain.class);
        
        // return new TestXMain("testXMLProps");
    }

    /**
     * The main program for executing this test also from console.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        
        TestRunner.run(suite());
    }

}
