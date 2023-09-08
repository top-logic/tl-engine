/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.DBSetup;
import com.top_logic.basic.util.Computation;

/**
 * Test the {DBSetup}.
 * 
 * DBSetup should normally be used in ant task to fire some scripts against a Database.
 *  
 * @author    <a href=mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestDBSetup extends BasicTestCase {

    /**
     * Constructor for TestDBSetup.
     * 
     * @param name the function to execute for testing.
     */
    public TestDBSetup(String name) {
        super(name);
    }
 
    /** 
     * Test some invalid argument usage
     */
    public void testInvalidArgs() throws Exception {
        
        ByteArrayOutputStream bos    = new ByteArrayOutputStream(1024);
		final ByteArrayOutputStream berr = new ByteArrayOutputStream(1024);
        PrintStream           out    = new PrintStream(bos);
        PrintStream           err    = new PrintStream(berr);
		Exception problem = BasicTestCase.runWithSystemOutAndErr(out, err, new Computation<Exception>() {

			@Override
			public Exception run() {
				String longArgs[] = {
					// "--real" , "test/script",
					"--feature", "Basic",
					"createTables",
				};
				try {
					DBSetup.main(longArgs);
				} catch (Exception ex) {
					return ex;
				}
				// String sout = bos.toString();
				String serr = berr.toString();
				// oldOut.println(sout);
				assertTrue(serr.indexOf("FileNotFoundException") > 0);
				assertTrue(serr.indexOf("missing file") > 0);
				return null;
			}
		});
		if (problem != null) {
			throw problem;
		}
    }
    
    /** 
     * Test the Long argument variant
     */
    public void testLongArgs() throws Exception {
        
        ByteArrayOutputStream bos    = new ByteArrayOutputStream(1024);
        ByteArrayOutputStream berr   = new ByteArrayOutputStream(1024);
        PrintStream           out    = new PrintStream(bos);
        PrintStream           err    = new PrintStream(berr);
		Exception problem = BasicTestCase.runWithSystemOutAndErr(out, err, new Computation<Exception>() {

			@Override
			public Exception run() {
				String longArgs[] = {
					"--real", "test/script",
					"--feature", "Basic",
					"dropTables",
					"createTables"
				};
				try {
					DBSetup.main(longArgs);
				} catch (Exception ex) {
					return ex;
				}
				return null;
			}
		});
		if (problem != null) {
			throw problem;
		}
    }
    
    /** 
     * Test the normal way of operating a DBSetup 
     */
    public void testShortArgs() throws Exception {
        
        String shortArgs[] = {
            "-r"        , "file://test/script",     
            "-f"        , "Basic",
            "dropTables",
            "createTables"
        };
        // This should not generate any output 
        DBSetup dbSetup = new DBSetup(!DBSetup.INTERACTIVE);
        dbSetup.runMainCommandLine (shortArgs);

        String otherArgs[] = {
            "-r"        , "file://test/script",     
            "dropTables",
            "createTables"
        };
        dbSetup = new DBSetup("Basic");
        dbSetup.runMainCommandLine (otherArgs);
    }
 
     /** 
     * What happens when the SQL is broken ? 
     */
    public void testBrokenArgs() throws Exception {
        
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		final ByteArrayOutputStream berr = new ByteArrayOutputStream(1024);
        PrintStream           out    = new PrintStream(bos);
        PrintStream           err    = new PrintStream(berr);
		Exception problem = BasicTestCase.runWithSystemOutAndErr(out, err, new Computation<Exception>() {

			@Override
			public Exception run() {
				String brokenArgs[] = {
					"-r", "file://test/script",
					"createTables"
				};
				DBSetup dbSetup = new DBSetup(true, "Broken");
				try {
					dbSetup.runMainCommandLine(brokenArgs);
				} catch (Exception ex) {
					return ex;
				}
				String sout = bos.toString();
				String serr = berr.toString();
				// oldOut.println(sout);
				assertTrue("Expected SQLException, found '" + serr + "'", serr.indexOf("SQLException") > 0);
				assertTrue(sout.indexOf("Check") > 0);
				return null;
			}
		});
		if (problem != null) {
			throw problem;
		}
     }
 
    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
		return ModuleTestSetup
			.setupModule(DatabaseTestSetup.getDBTest(TestDBSetup.class, DatabaseTestSetup.DBType.H2_DB));
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite ());
    }
    /** 
     * Test the help arguements
     */
    public void testHelp() throws Exception {
        
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        ByteArrayOutputStream berr   = new ByteArrayOutputStream(1024);
        PrintStream           out    = new PrintStream(bos);
        PrintStream           err    = new PrintStream(berr);
		Exception problem = BasicTestCase.runWithSystemOutAndErr(out, err, new Computation<Exception>() {

			@Override
			public Exception run() {
				String theArgs[] = {
					"--help"
				};
				try {
					DBSetup.main(theArgs);
				} catch (Exception ex) {
					return ex;
				}
				String sout = bos.toString();
				// oldOut.println(sout);
				assertTrue(sout.indexOf("-r") > 0);
				assertTrue(sout.indexOf("--real") > 0);
				assertTrue(sout.indexOf("createTables") > 0);
				assertTrue(sout.indexOf("dropTables") > 0);
				bos.reset();
				return null;
			}
		});
		if (problem != null) {
			throw problem;
		}
    }


}
