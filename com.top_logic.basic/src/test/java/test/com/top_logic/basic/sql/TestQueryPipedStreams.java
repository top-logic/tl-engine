/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.ThreadContextSetup;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.PreparedQuery;
import com.top_logic.basic.sql.QueryPipedStreams;

/** Testcase for the {@link com.top_logic.basic.sql.QueryPipedStreams}.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestQueryPipedStreams extends BasicTestCase {

    /** Connection actually used by the DSA */
    static final byte[] BLOBDATA =
		("This is a TestString from " + TestQueryPipedStreams.class.getName() + " to test the BLOB-functions")
			.getBytes();

    /** Connection used for Testing */
	private PooledConnection con;

    /**
     * Default constructor.
     *
     * @param name of test to execute.
     */
    public TestQueryPipedStreams (String name) {
        super (name);
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		con = ConnectionPoolRegistry.getDefaultConnectionPool().borrowWriteConnection();
	}

	@Override
	protected void tearDown() throws Exception {
		ConnectionPoolRegistry.getDefaultConnectionPool().releaseWriteConnection(con);
		con = null;
		super.tearDown();
	}

    /** Helper function to check for correct Data */
    protected void checkBLOB(String key, int len) throws IOException, SQLException {
		try (Statement stm = con.createStatement()) {
			try (ResultSet res = stm.executeQuery(
				"SELECT data FROM blobtest WHERE name = '" + key + "'")) {

				assertTrue(res.next());
				try (InputStream is = res.getBinaryStream(1)) {
					for (int i = 0; i < len; i++) {
						int b = BLOBDATA[i];
						int s = is.read();
						if (b != s)
							fail("Blobdata incorrect at position "
								+ i + '/' + len
								+ " expected: " + b + '(' + (char) b + ')'
								+ " but was: " + s + '(' + (char) s + ')');
					}
				}
			}
		}
    }

    /** Test using an INSERT Statement.
     */
    public void testInsert() throws Exception {

        PreparedQuery pq = 
            new PreparedQuery(con, "INSERT INTO blobtest VALUES(?,?)");
        
        pq.getPreparedStatement().setString(1,"val");
        QueryPipedStreams qps = new QueryPipedStreams(pq, 2 , BLOBDATA.length);
        
		try (OutputStream os = qps.getOutputStream()) {
			os.write(BLOBDATA);
		}
    
        checkBLOB("val", BLOBDATA.length);
    }

    /** Test using an UPDATE Statement.
     */
    public void testUpdate() throws Exception {
		// create initial data.
		testInsert();

        PreparedQuery pq = 
            new PreparedQuery(con, "UPDATE blobtest SET data= ? WHERE name='val'");
    
        QueryPipedStreams qps = new QueryPipedStreams(pq, 1 , BLOBDATA.length);
    
		try (OutputStream os = qps.getOutputStream()) {
			os.write(BLOBDATA);
		}

        checkBLOB("val", BLOBDATA.length);
    }

    /** Test an Error during update, should be propagated on close().
     */
    public void testUpdateError() throws Exception {
		// create initial data.
		testInsert();

		PreparedQuery pq =
			new PreparedQuery(con, "INSERT INTO blobtest (data, name) VALUES (?, 'val')");
    
        QueryPipedStreams qps = new QueryPipedStreams(pq, 1 , BLOBDATA.length);
    
		@SuppressWarnings("resource")
		OutputStream os = qps.getOutputStream();
        os.write(BLOBDATA);
        try {
            os.close();  // This should Trigger closing of all the Objects above ...
            fail("close() should result in a (wrapped) Exception");
        }
        catch (IOException expected)  { /* expected */ }
    }

    /** The JDBC Specification is unclear about the length Parameter.
     *
     * This test is highly JDBC-Driver dependant 
     * (and broke while going from mm-Mysql to MYSQL-JConncetor)
     */
    public void testWrongLength() throws Exception {

        PreparedQuery pq = 
            new PreparedQuery(con, "INSERT INTO blobtest VALUES(?,?)");
    
        pq.getPreparedStatement().setString(1,"false");
        int wrongLength = BLOBDATA.length - 10;
        QueryPipedStreams qps = new QueryPipedStreams(pq, 2 , wrongLength);
    
		try (OutputStream os = qps.getOutputStream()) {
			os.write(BLOBDATA);
		}

        checkBLOB("false", BLOBDATA.length - 10);
    }

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite ("TestQueryPipedStreams");

        // Order is important so ...
        suite.addTest(new TestQueryPipedStreams("testInsert"));
        suite.addTest(new TestQueryPipedStreams("testUpdate"));
        suite.addTest(new TestQueryPipedStreams("testUpdateError"));
        suite.addTest(new TestQueryPipedStreams("testWrongLength"));
        
		return ModuleTestSetup
			.setupModule(
				DatabaseTestSetup.getDBTest(new TestQueryPipedStreamsSetup(suite), DatabaseTestSetup.DEFAULT_DB));
    }
    

	private static class TestQueryPipedStreamsSetup extends ThreadContextSetup {

		public TestQueryPipedStreamsSetup(Test test) {
			super(test);
		}

		@Override
		protected void doSetUp() throws Exception {
			ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
			PooledConnection wc = pool.borrowWriteConnection();
			try {
				try (Statement stm = wc.createStatement()) {
					stm.execute("CREATE TABLE blobtest ("
						+ " name    char(32) NOT NULL, "
						+ " data    BLOB         , "
						+ " PRIMARY KEY (name))");
				}
			} finally {
				pool.releaseWriteConnection(wc);
			}
		}

		/**
		 * @see test.com.top_logic.basic.DecoratedTestSetup#doTearDown()
		 */
		@Override
		protected void doTearDown() throws Exception {
			ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
			PooledConnection wc = pool.borrowWriteConnection();
			try {
				try (Statement stm = wc.createStatement()) {
					stm.execute("DROP TABLE blobtest");
				}
			} finally {
				pool.releaseWriteConnection(wc);
			}
		}
	}

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
