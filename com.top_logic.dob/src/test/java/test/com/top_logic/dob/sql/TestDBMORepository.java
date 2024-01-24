/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.dob.DOBTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.sql.DBMORepository;
import com.top_logic.dob.sql.DynamicDBMORepository;

/**
 * Test cases for the {@link com.top_logic.dob.sql.DBMORepository}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestDBMORepository extends BasicTestCase {

    /**
     * Create a Test for the given (function-) name.
     *
     * @param name the (function)-name of the test to perform.
     */
    public TestDBMORepository (String name) {
        super (name);
    }

	private static class Setup extends ThreadContextSetup {

    	public Setup(Test test) {
			super(test);
		}

		@Override
		protected void doSetUp() throws Exception {
	        ConnectionPool db = getDB();
	        PooledConnection connection = db.borrowWriteConnection();
			try {
				Statement stm = connection.createStatement();
				String    sql;

				dropTables(stm);
				sql = "CREATE TABLE GRP ( "
				    + " GROUPNAME	CHAR(15)	NOT NULL PRIMARY KEY, "
				    + " GROUPBITS	INTEGER )";
				    
				stm.executeUpdate(sql);

				stm.executeUpdate("INSERT INTO GRP VALUES ('aaa' , NULL)");
				stm.executeUpdate("INSERT INTO GRP VALUES ('bbb' , 99)");
				stm.executeUpdate("INSERT INTO GRP VALUES ('ccc' , 32767)");
				    
				sql = "CREATE TABLE LOGIN ( "
				    + "	USERNAME	CHAR(10) 	NOT NULL PRIMARY KEY,"
				    + "	PASSWORD	CHAR(10) 	NOT NULL,"
				    + "	LASTLOGIN	DATE	 	NULL,"
				    + "	USERBITS	INTEGER		NOT NULL)";

				stm.executeUpdate(sql);

				insertLogin(connection, "hugo" , "gock6el0", null, 17);
				insertLogin(connection, "heinz", "Pfed7fus", null, 903);
				insertLogin(connection, "erna" , "mUetZe12", DateUtil.createDate(2001, 10, 29), -3);
				insertLogin(connection, "berta", "senarK8s", DateUtil.createDate(2001, 10, 29), 44);

				sql ="CREATE TABLE LOGINGRP ("
				    + "	USERNAME	CHAR(10) NOT NULL	REFERENCES LOGIN(USERNAME),"
				    + "	GROUPNAME 	CHAR(15) NOT NULL	REFERENCES GRP(GROUPNAME),"
				    + "	PRIMARY KEY (USERNAME, GROUPNAME) )";
				
				stm.executeUpdate(sql);

				stm.executeUpdate("INSERT INTO LOGINGRP VALUES ('hugo'  ,'aaa')");
				stm.executeUpdate("INSERT INTO LOGINGRP VALUES ('heinz' ,'aaa')");
				stm.executeUpdate("INSERT INTO LOGINGRP VALUES ('erna'  ,'bbb')");
				stm.executeUpdate("INSERT INTO LOGINGRP VALUES ('erna'  ,'ccc')");
				stm.executeUpdate("INSERT INTO LOGINGRP VALUES ('berta' ,'ccc')");
				connection.commit();
			} finally {
				db.releaseWriteConnection(connection);
			}
    	}

    	private void insertLogin(PooledConnection statementCache, String s1, String s2, java.util.Date date, int i) throws SQLException {
			try (PreparedStatement stmt =
				statementCache.prepareStatement("INSERT INTO LOGIN VALUES (?  , ? , ? , ?)")) {
				int offset = 1;
				stmt.setString(offset++, s1);
				stmt.setString(offset++, s2);
				if (date != null) {
					statementCache.getSQLDialect().setDate(stmt, offset++, new Date(date.getTime()));
				} else {
					stmt.setNull(offset++, Types.DATE);
				}
				stmt.setInt(offset++, i);

				int insertedRows = stmt.executeUpdate();
				assert insertedRows == 1;
			}
		}

		@Override
		protected void doTearDown() throws Exception {
	        ConnectionPool db = getDB();
	        PooledConnection connection = db.borrowWriteConnection();
			try {
	            Statement stm = connection.createStatement();

	            dropTables(stm);
	            
			} finally {
				db.releaseWriteConnection(connection);
			}
    	}
    	
		private void dropTables(Statement stm) {
			dropTable(stm, "GRP");
	        dropTable(stm, "LOGIN");
	        dropTable(stm, "LOGINGRP");
		}

		private void dropTable(Statement stm, String tablename) {
			try {
				stm.executeUpdate("DROP TABLE " + tablename);
	        } catch (SQLException ex) {
	        	// Ignore
	        }
		}
    }

	static ConnectionPool getDB() throws SQLException {
		return ConnectionPoolRegistry.getDefaultConnectionPool();
	}

    /** Test MetaObject generation for the DBMORepository */
    public void testMetaObjects() throws Exception  {
        DBMORepository dbRep = new DynamicDBMORepository(getDB());
        
        try {
            dbRep.getMetaObject("HamWaNicht");
            fail("This table does not exist");
        } 
        catch (UnknownTypeException expected) { /* expected */ }
        
        MetaObject metaGrp      = dbRep.getMetaObject("GRP");
        MetaObject metaLogin    = dbRep.getMetaObject("LOGIN");
        MetaObject metaLoginGRP = dbRep.getMetaObject("LOGINGRP");
        
        List names = dbRep.getMetaObjectNames();
        
        // Attention, names of the actual MetaObject may be different !
        assertTrue(names.contains(metaGrp.getName()));
        assertTrue(names.contains(metaLogin.getName()));
        assertTrue(names.contains(metaLoginGRP.getName()));

		assertEquals(set(metaGrp, metaLogin, metaLoginGRP), toSet(dbRep.getMetaObjects()));
    }

   /** Test Getting all MetaObhject via theire names */
   public void testMetaObjectNames() throws Exception  {

       DBMORepository dbRep = new DBMORepository();
       
       List     names = dbRep.getMetaObjectNames();
       Iterator iter  = names.iterator();
       while (iter.hasNext()) {
           /* MetaObject metaLoginGRP = */ dbRep.getMetaObject(iter.next().toString());
       }
   }

   /** Test Factories for the DBMORepository */
   public void testFactory() throws Exception  {
       
       ConnectionPool pool = getDB();
       DBMORepository dbRep = new DynamicDBMORepository(pool);

       MetaObject loginMO =  dbRep.getMetaObject("LOGIN");
       DataObject theDo    = new DefaultDataObject(loginMO);
       
       Object theDate = (pool.getSQLDialect() instanceof MSSQLHelper) ?
       		            (Object) new java.sql.Timestamp(0) : (Object) new java.sql.Date(0);
       
       theDo.setAttributeValue("USERNAME", "someName");
       theDo.setAttributeValue("LASTLOGIN", theDate);
		theDo.setAttributeValue("USERBITS", Integer.valueOf(0xFF));
   }

   /**
     * The suite of Test to execute.
     */
    public static Test suite () {
		// SQL is not DB independent, e.g. it does not work with MSSQL
		DBType dbType = DatabaseTestSetup.DBType.H2_DB;
		return DOBTestSetup.createDOBTestSetup(DatabaseTestSetup.getDBTest(TestDBMORepository.class,
			dbType, new TestFactory() {
			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				return new Setup(new TestSuite(TestDBMORepository.class, suiteName));
			}
        }));
    }

    /**
     * Main fucntion for direct testing.
     */
    public static void main (String[] args) {
        TestRunner.run (suite ());
    }

}
