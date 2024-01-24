
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa.sql;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.dsa.DSATestSetup;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.sql.JDBCDataSourceAdaptor;

/** Testcase for the {@link com.top_logic.dsa.sql.JDBCDataSourceAdaptor}.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestJDBCDSA extends BasicTestCase {

    /** Connection actually used by the DSA */
    static final byte[] BLOBDATA1 =
		("1) This is the first  TestString from " + TestJDBCDSA.class.getName() + " to test the BLOB-functions - 1")
			.getBytes();

    static final byte[] BLOBDATA2 =
		("2) This is the second TestString from " + TestJDBCDSA.class.getName() + " to test the BLOB-functions - 2")
			.getBytes();

	private static final boolean NOFORCE = false;

    /**
     * The DataSourceAdaptor to be tested.
     */
	private JDBCDataSourceAdaptor jdbcDSA;

	private ConnectionPool connectionPool;

    /**
     * Default constructor.
     *
     * @param name of test to execute.
     */
    public TestJDBCDSA (String name) {
        super (name);
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		jdbcDSA = new JDBCDataSourceAdaptor(connectionPool);
	}

	@Override
	protected void tearDown() throws Exception {
		jdbcDSA.close();
		super.tearDown();
	}

    /** Test exists() function of the DSA.
     */
    public void testExists() throws Exception {

        assertTrue( jdbcDSA.exists(""));
		assertTrue(jdbcDSA.exists("JDBCTEST1"));
		assertTrue(!jdbcDSA.exists("JDBCTEST99"));
		assertTrue(jdbcDSA.exists("JDBCTEST1?NAME='Blah'"));
		assertTrue(!jdbcDSA.exists("JDBCTEST1?NAME='Blurb'"));
    }
    
    /** Test exists() function of the DSA.
     */
    public void testIsContainer() throws Exception {

        assertTrue( jdbcDSA.isContainer(""));
		assertTrue(jdbcDSA.isContainer("JDBCTEST1"));
		assertTrue(!jdbcDSA.isContainer("JDBCTEST99"));
		assertTrue(!jdbcDSA.isContainer("JDBCTEST1?NAME != 'murks'"));
		assertTrue(!jdbcDSA.isContainer("JDBCTEST1?NAME='Blurb'"));
    }

    /** Test getEntry() function of the DSA.
     */
    public void testEntry() throws Exception {

        assertNull( jdbcDSA.getEntry(""));
		assertNull(jdbcDSA.getEntry("JDBCTEST1"));
		assertNull(jdbcDSA.getEntry("JDBCTEST1?1=1"));
        
		final InputStream entry = jdbcDSA.getEntry("JDBCTEST1?NAME='Blah'");
        String helper;
        try {
			helper = FileUtilities.readAllFromStream(entry);
		} finally {
			entry.close();
		}
		assertTrue(helper.indexOf("NAME") > 0);
        assertTrue(helper.indexOf("Blah")  > 0);
		assertTrue(helper.indexOf("BITS") > 0);
        assertTrue(helper.indexOf("17")    > 0);
		assertTrue(helper.indexOf("BOLTS") > 0);
        assertTrue(helper.indexOf("20.4")  > 0);
        
		assertNull(jdbcDSA.getEntry("JDBCTEST1?NAME='val2'"));
		assertNull(jdbcDSA.getEntry("JDBCTEST1?NAME='valNone'"));

		assertNull(jdbcDSA.getEntry("JDBCTEST99"));
    }


    /** Test getEntry() / getEntryOutputStream() function of the DSA for a BLOB.
     */
    public void testBlob() throws Exception {

    	int len = BLOBDATA1.length;
		InputStream input = new ByteArrayInputStream(BLOBDATA1);
        try {
			OutputStream output = jdbcDSA.getEntryOutputStream("JDBCBLOBTEST?NAME='val1'");
			try {
				FileUtilities.copyStreamContents(input, output);
			} finally {
				output.close();
			}
		}
		finally {
		    input.close();
		}

		assertNull(jdbcDSA.getEntry("JDBCBLOBTEST"));
		assertNull(jdbcDSA.getEntry("JDBCBLOBTEST?1=1"));
        
		InputStream is1 = jdbcDSA.getEntry("JDBCBLOBTEST?NAME='val1'");
        try {
        	assertNotNull(is1);
        	for (int i=0; i < len; i++) {
        		int  b = BLOBDATA1[i];
        		int  s = is1.read();
        		if (b != s)
        			fail("Blobdata incorrect at position " 
        					+ i + '/' + len 
        					+ " expected: " + b + '(' + (char) b + ')'
        					+ " but was: "  + s + '(' + (char) s + ')');
        	}
		} finally {
			is1.close();
		}

		assertNull(jdbcDSA.getEntry("JDBCBLOBTEST?NAME='val2'"));
		assertNull(jdbcDSA.getEntry("JDBCBLOBTEST?NAME='valNone'"));
        
		jdbcDSA.putEntry("JDBCBLOBTEST?NAME='val2'", new ByteArrayInputStream(BLOBDATA2));
		InputStream is2 = jdbcDSA.getEntry("JDBCBLOBTEST?NAME='val2'");
        try {
        	assertNotNull(is2);
        	len = BLOBDATA2.length;
        	for (int i=0; i < len; i++) {
        		int  b = BLOBDATA2[i];
        		int  s = is2.read();
        		if (b != s)
        			fail("Blobdata incorrect at position " 
        					+ i + '/' + len 
        					+ " expected: " + b + '(' + (char) b + ')'
        					+ " but was: "  + s + '(' + (char) s + ')');
        	}
		} finally {
			is2.close();
		}
    }


    /** Test getObjectEntry() function of the DSA.
     */
    public void testObjectEntry() throws Exception {
        try {
        	jdbcDSA.getObjectEntry("");
        	fail("Invalid table name");
		} catch (DataObjectException expected) {
			/* expected */ }

		assertNotNull(jdbcDSA.getObjectEntry("JDBCTEST1"));
		assertNotNull(jdbcDSA.getObjectEntry("JDBCTEST1?1=1"));
        
		DataObject theDo = jdbcDSA.getObjectEntry("JDBCTEST1?NAME='Blah'");
		assertEquals("Blah", theDo.getAttributeValue("NAME"));
		assertEquals(Integer.valueOf(17), theDo.getAttributeValue("BITS"));
		assertEquals(Double.valueOf(20.4), theDo.getAttributeValue("BOLTS"));
        
        try {
			jdbcDSA.getObjectEntry("JDBCTEST99");
            fail("Invalid table name");
		} catch (DataObjectException expected) {
			/* expected */ }
    }

    /** Test putEntry(), createObjectEntry(), delte().
     */
    public void testCreateDelete() throws Exception {
            
        DataObject theDo = new ExampleDataObject( 
			new String[] { "NAME", "BITS", "BOLTS" },
			new Object[] { "hallo", Integer.valueOf(74), Double.valueOf(Math.PI) });
    
        
		jdbcDSA.putEntry("JDBCTEST1", theDo);
		theDo = jdbcDSA.getObjectEntry("JDBCTEST1?NAME='hallo'");
        assertNotNull(theDo);

		assertEquals("hallo", theDo.getAttributeValue("NAME"));
		assertEquals(Integer.valueOf(74), theDo.getAttributeValue("BITS"));
        double delta = Math.abs(Math.PI - 
			((Number) theDo.getAttributeValue("BOLTS")).doubleValue());
           // Do not expect Numeric precision from a Database ...
        assertTrue(delta < 0.00000000001);
        
        
		theDo = jdbcDSA.createObjectEntry("JDBCTEST1", "-> is ignored anyway !");
		theDo.setAttributeValue("NAME", "Bello");
		theDo.setAttributeValue("BITS", Integer.valueOf(Integer.MAX_VALUE));
		theDo.setAttributeValue("BOLTS", Double.valueOf(-13E-2));
		jdbcDSA.putEntry("JDBCTEST1", theDo);
    
		theDo = jdbcDSA.getObjectEntry("JDBCTEST1?NAME='Bello'");

		assertEquals("Bello", theDo.getAttributeValue("NAME"));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), theDo.getAttributeValue("BITS"));
		assertEquals(Double.valueOf(-13E-2), theDo.getAttributeValue("BOLTS"));
        
		jdbcDSA.delete("JDBCTEST1?NAME='hallo'", NOFORCE);
		jdbcDSA.delete("JDBCTEST1?NAME='Bello'", NOFORCE);
		jdbcDSA.delete("JDBCTEST1?NAME='Who Cares'", NOFORCE);
        try {
			jdbcDSA.delete("JDBCTEST1", NOFORCE);
            fail("This should require a WHERE clause");
		} catch (DatabaseAccessException expected) {
			/* expected */ }
        
		jdbcDSA.deleteRecursively("JDBCTEST1");

		PooledConnection writeConnection = connectionPool.borrowWriteConnection();
		try {
			// Recreate Tables deleted by Test above ...    
	        Statement stm = writeConnection.createStatement();
	                    
			stm.executeUpdate("INSERT INTO JDBCTEST1 VALUES ('Blah'     , 17, 20.4)");
			stm.executeUpdate("INSERT INTO JDBCTEST1 VALUES ('Schwall'  , 14,  3.5)");

	        writeConnection.commit();
		} finally {
			connectionPool.releaseWriteConnection(writeConnection);
		}
    }

	public void testClosing() throws SQLException {
    	// see #1675
    	ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
    	JDBCDataSourceAdaptor adaptor = new JDBCDataSourceAdaptor(pool);
    	adaptor.close();
    	try {
    		PooledConnection borrowReadConnection = pool.borrowReadConnection();
			pool.releaseReadConnection(borrowReadConnection);
    	} catch(RuntimeException ex) {
    		fail("can not borrow readConnection from pool '" + pool + "': " + ex.getLocalizedMessage());
    	}
    }

    /** Test main features of the DSA
     */
    public void testMain() throws Exception {

        String[]    entries;
        
        /* theDo = */ jdbcDSA.getProperties("");
        // System.out.println(theDo);
		/* theDo = */jdbcDSA.getProperties("JDBCTEST1");
        // System.out.println(theDo);
		/* theDo = */jdbcDSA.getProperties("JDBCTEST2?x=y");
        // System.out.println(theDo);
        entries = jdbcDSA.getEntryNames("");    // Names of all Tables
        assertTrue(entries.length >= 2);
		entries = jdbcDSA.getEntryNames("JDBCTEST1");
        assertEquals(2, entries.length);
		entries = jdbcDSA.getEntryNames("JDBCTEST1?NAME > 'A'");
        assertEquals(2, entries.length);
		entries = jdbcDSA.getEntryNames("JDBCTEST1?NAME IS NULL");
        assertEquals(0, entries.length);
		entries = jdbcDSA.getEntryNames("JDBCTEST2");
        assertEquals(0, entries.length);
    }

    /** Test the JDBCDSA via a Proxy ...
     */
    /*public void testProxy() throws Exception {
	 * 
	 * DataAccessProxy proxy; OutputStream os;
	 * 
	 * proxy = new DataAccessProxy("jdbcmysql://");
	 * 
	 * assertTrue(!proxy.isRepository()); assertTrue(!proxy.isPrivate()); //
	 * assertTrue(!proxy.isStructured()); assertTrue(proxy.exists());
	 * assertTrue(proxy.isContainer()); assertTrue(!proxy.isEntry());
	 * 
	 * assertEquals("",proxy.getName()); assertEquals("",proxy.getPath());
	 * assertEquals("",proxy.getParent()); os = proxy.putEntry("JDBCTEST1?NAME='Blah');
	 * 
	 * os.close(); os = proxy.getEntryOutputStream(); os.close();
	 * 
	 * assertNull (proxy.getEntry()); assertNull (proxy.putEntry(new
	 * ByteArrayInputStream(BLOBDATA1)));
	 * 
	 * proxy = new DataAccessProxy("jdbcmysql://","JDBCTEST1");
	 * 
	 * proxy = new DataAccessProxy("jdbcmysql://JDBCTEST1);
	 * 
	 * proxy = new DataAccessProxy("jdbcmysql://JDBCTEST1?1=1);
	 * 
	 * proxy = new DataAccessProxy("jdbcmysql://JDBCTEST1?NAME='Blah'); } */

    // Static methodes

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
		// SQL in doCreate is not DB independent, e.g. it does not work with MSSQL
		Test test = DatabaseTestSetup.getDBTest(TestJDBCDSA.class, DBType.H2_DB, new TestFactory() {

			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				return new Setup(suite);
			}
		});
		return DSATestSetup.createDSATestSetup(test);
    }

	private static class Setup extends ThreadContextSetup {

		public Setup(Test test) {
			super(test);
		}

		@Override
		protected void doSetUp() throws Exception {
			ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();

			PooledConnection statementCache = pool.borrowWriteConnection();
			try {
				Statement stm = statementCache.createStatement();

				// This is very MySQL Specific, admitted
				stm.executeUpdate("CREATE TABLE JDBCTEST1 ("
					+ " NAME    VARCHAR(63) NOT NULL, "
					+ " BITS    int         , "
					+ " BOLTS   double      , "
					+ " PRIMARY KEY (NAME))");

				stm.executeUpdate("CREATE TABLE JDBCTEST2 ("
					+ " PKEY    int  NOT NULL, "
					+ " THINGS  char (66)   , "
					+ " BITS    int         , "
					+ " BOLTS   double      , "
					+ " PRIMARY KEY (PKEY))");

				stm.executeUpdate("INSERT INTO JDBCTEST1 VALUES ('Blah'     , 17, 20.4)");
				stm.executeUpdate("INSERT INTO JDBCTEST1 VALUES ('Schwall'  , 14,  3.5)");

				stm.executeUpdate("CREATE TABLE JDBCBLOBTEST ("
					+ " NAME    char(32) NOT NULL, "
					+ " DATA    BLOB         , "
					+ " PRIMARY KEY (NAME))");

				stm.executeUpdate("INSERT INTO JDBCBLOBTEST VALUES('val1',NULL)");
				stm.executeUpdate("INSERT INTO JDBCBLOBTEST VALUES('val2',NULL)");

				statementCache.commit();
			} finally {
				pool.releaseWriteConnection(statementCache);
			}
		}

		@Override
		protected void doTearDown() throws Exception {
			ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
			PooledConnection statementCache = pool.borrowWriteConnection();
			try {
				Statement stm = statementCache.createStatement();
				stm.executeUpdate("DROP TABLE JDBCTEST1");
				stm.executeUpdate("DROP TABLE JDBCTEST2");
				stm.executeUpdate("DROP TABLE JDBCBLOBTEST");
				statementCache.commit();
			} finally {
				pool.releaseWriteConnection(statementCache);
			}

		}
	}

}
