/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObject;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.ex.UnknownDBException;
import com.top_logic.dsa.file.FilesystemDataSourceAdaptor;

/**
 * Testing the DataAccesService implementation.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestDataAccessService extends TestCase {

    private static final boolean NOFORCE = false;

	/**
     * The DataAccessService to be tested.
     */
    private static DataAccessService das = null;

    /**
     * The DataSourceAdaptor to be tested.
     */
    private DataSourceAdaptor        db  = null;

    /**
     * Create a new tst fo0r the given function name.
     */
    public TestDataAccessService (String name) {
        super (name);
    }

    /**
     * create the Classes to be tested
     */
    @Override
	protected void setUp () throws Exception {
        
        if (das == null) {
            super.setUp();

            das = DataAccessService.getInstance ();
			db = new FilesystemDataSourceAdaptor("./tmp");
            das.registerDataSource ("testfile", db);
        }
    }

    // Test methodes

    // This is currently done in the getInstance method of DataAccessService

    /**
     * Test registering Datasources.
     */
    public void testRegisterDataSource () throws Exception {
    
        DataSourceAdaptor db2 = null;
        DataSourceAdaptor db3 = null;
        // Register a DataSource
		db2 = new FilesystemDataSourceAdaptor("./tmp");
        das.registerDataSource ("testfile2", db2);
        db3 = das.getDataSourceByName ("testfile2");
        assertEquals("Database not registered properly!", db2 ,db3);

        // Try to register a DataSource twice -> expecting a DBAlreadyRegisteredException
        try {
            das.registerDataSource ("testfile2", db2);
        }
        catch (Exception DBAlreadyRegisteredException) {
            // When we are here -> test is OK
            das.registerDataSource ("testfile3", db2);
            db3 = das.getDataSourceByName ("testfile3");
            assertEquals ("Database not registered properly", db2 , db3);
        }
    }

    /**
     * Test unregistering datasources.
     */
    public void testUnregisterDatabase () throws Exception {

        // Register a DataSource
		DataSourceAdaptor db2 = new FilesystemDataSourceAdaptor("./tmp");
        das.registerDataSource ("testfile5", db2);
        das.unregisterDataSource ("testfile5");

        Exception expected = null;
        try  { // to unregister again
            das.unregisterDataSource ("testfile5");
        }
        catch (UnknownDBException udb) {
            // When we are here -> test is OK
            expected = udb;
        }
        assertTrue("Expected an Exception here", expected != null);
    }

    /**
     * Test putting an entry somewhere.
     */
    public void testCreate () throws Exception {

        String         testdata = "Yo, it's working!";
        InputStream    data     =
            new ByteArrayInputStream (testdata.getBytes());
        DataAccessProxy dap = new DataAccessProxy ("testfile://");
        dap.createEntry ("TestDataAccessService.txt", data);
    }

    /**
     * test gettting the data whe puttet in test2Put().
     */
    public void test3Get () throws Exception {
        DataAccessProxy dap     = new DataAccessProxy (
            "testfile://TestDataAccessService.txt");
        InputStream     iStream = dap.getEntry ();
        assertTrue ("Unable to get Stream ?", iStream != null);
        StringBuffer   buf       = new StringBuffer ();
        String         line      = null;
        BufferedReader theReader =
            new BufferedReader (new InputStreamReader (iStream));
        while ((line = theReader.readLine ()) != null) {
            buf.append (line);
            buf.append ('\n');
        }
        theReader.close ();
        assertEquals("Yo, it's working!\n", buf.toString ());
        iStream.close ();
    }

    /**
     * Test some properties of a file.
     */
    public void test4GetProperties () throws Exception 
    {
        DataAccessProxy dap      = new DataAccessProxy (
            "testfile://TestDataAccessService.txt");
        /* DataObject      propsDob = */  dap.getProperties ();
        // System.out.println (propsDob);
    }

    
    public void test5SetProperties () throws Exception  {

        DataAccessProxy dap      = new DataAccessProxy (
            "testfile://TestDataAccessService.txt");
        DataObject propsDob = dap.getProperties ();
        /* Long lastModif = (Long) */  propsDob.getAttributeValue("lastModified");
        // Long newLastModif   = Long.valueOf( 10000 + lastModif.longValue());
        
        /* This is not suppoted anymore
        propsDob.setAttributeValue ("lastModified", newLastModif);
        propsDob.setAttributeValue ("isWriteable" , Boolean.FALSE);
        
        dap.setProperties (propsDob);
        propsDob = ( DataObject ) dap.getProperties ();
        
        // Mhh, this fails with WinNT ...
        // assertEquals(newLastModif , propsDob.getAttributeValue("lastModified"));
        assertEquals(Boolean.FALSE, propsDob.getAttributeValue("isWriteable" ));
        */
    }

    
    public void test6Delete () throws Exception  {
        DataAccessProxy dap = new DataAccessProxy (
            "testfile://TestDataAccessService.txt");
        dap.delete (NOFORCE);
    }

    // Static methodes

    /**
     * the suite of tests to perform
     */
    public static Test suite () {
        TestSuite suite = new TestSuite ();
        suite.addTest(new TestDataAccessService("testRegisterDataSource"));
        suite.addTest(new TestDataAccessService("testUnregisterDatabase"));
        suite.addTest(new TestDataAccessService("testCreate"));
        suite.addTest(new TestDataAccessService("test3Get"));
        suite.addTest(new TestDataAccessService("test4GetProperties"));
        suite.addTest(new TestDataAccessService("test5SetProperties"));
        suite.addTest(new TestDataAccessService("test6Delete"));
        
        Test innerTest = ServiceTestSetup.createSetup(suite, DataAccessService.Module.INSTANCE);
        return DSATestSetup.createDSATestSetup(innerTest);
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
