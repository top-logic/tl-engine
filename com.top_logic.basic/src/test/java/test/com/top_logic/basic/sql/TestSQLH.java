/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.Test;

import com.top_logic.basic.sql.PreparedQuery;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.sql.SQLQuery;

/** Testcase for {@link com.top_logic.basic.sql.SQLH}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestSQLH extends AbstractConnectionTest {

	private static final String TABLE_NAME = ConnectionSetup.TABLE_NAME;

    public TestSQLH(String name) {
        super(name);
    }
    
    /** 
     * test some basic things to make coverage happy 
     */
    public void testBasics() {
        SQLH sqh = new SQLH() {
            @Override
			public String toString() { return super.toString(); }
        };
        assertNotNull(sqh);
    }
    
    /**
     * Tets the 
     */
	public void testCreateWhere() {
        assertEquals("IS NULL" , SQLH.createWhereString(null));
        assertEquals("='murks'", SQLH.createWhereString("murks"));
    }

    /**
     * Checks if method createLike works fine.
     * We expect select count(*) to find two rows
     */
	public void testCreateLIKE() throws SQLException {
        Connection con = getConnection();            

        String hello ="W*";
        SQLQuery query = new SQLQuery(con,
			"select count(*) from " + tableRef() + " where " + columnRef("s2") + " " + SQLH.createLIKE(hello));

        ResultSet set = query.getResultSet();
        assertTrue("Nothing to count ?",set.next());
        int counter = set.getInt(1);
        query.close();
        assertEquals(2 , counter);
    }
    
    /**
     * A very hard test - without database
     */
    public void testCreateLIKE2() {
        String challenge = "M?_?_??__%*%*%***%";
        String result = "LIKE 'M_\\__\\___\\_\\_\\%%\\%%\\%%%%\\%' ESCAPE '\\'";
        assertEquals(result, SQLH.createLIKE(challenge));
    }
    
    /**
     * Test the Quoting of Strings
     */
    public void testCreateInsertString() {
        assertEquals("NULL"  , SQLH.createInsertString(null));
        assertEquals("''"    , SQLH.createInsertString(""));
        assertEquals("'blah'", SQLH.createInsertString("blah"));
    }

    /**
     * Test the Quoting of Strings
     */
    public void testAppendInsertString() {
        StringBuilder out = new StringBuilder(128);
        out.append("INSERT INTO blah VALUES (");
        SQLH.appendInsertString(null , out);
        out.append(',');
        SQLH.appendInsertString(""    , out);
        out.append(',');
        SQLH.appendInsertString("blah", out);
        out.append(')');
        assertEquals("INSERT INTO blah VALUES (NULL,'','blah')"
                    , out.toString());
    }

	public void testMangle() {
        assertEquals("THIS_IS_THE_NUMBER1", SQLH.mangleDBName("thisIsTheNumber1"));
    }

	public void testMangleAbreviation() {
		assertEquals("TL_PERSON", SQLH.mangleDBName("TLPerson"));
	}

	public void testMangleAbreviationEnd() {
		assertEquals("ALGORITHM_SPI", SQLH.mangleDBName("AlgorithmSPI"));
	}

	public void testMangleAbreviationMiddle() {
		assertEquals("ALGORITHM_SPI_PROVIDER", SQLH.mangleDBName("AlgorithmSPIProvider"));
	}

	public void testMangleSpecialStart() {
		assertEquals("___SPECIALS", SQLH.mangleDBName("$%/Specials"));
		assertEquals("___SPECIALS", SQLH.mangleDBName("$%/SPECIALS"));
	}

	public void testMangleSpecialMiddleChars() {
		assertEquals("SOME___SPECIALS", SQLH.mangleDBName("Some$%/Specials"));
		assertEquals("SOME___SPECIALS", SQLH.mangleDBName("SOME$%/SPECIALS"));
    }

	public void testMangleSpecialEnd() {
		assertEquals("SPECIALS___", SQLH.mangleDBName("Specials$%/"));
		assertEquals("SPECIALS___", SQLH.mangleDBName("SPECIALS$%/"));
	}
    
	public void testMangleDotsSeparated() {
		assertEquals("TL_ELEMENT_TABLE", SQLH.mangleDBName("tl.element.table"));
		assertEquals("TL_ELEMENT_TABLE", SQLH.mangleDBName("Tl.Element.Table"));
	}

    /**
     * Test if a null value is converted into a empty string.
     */
	public void testGetEmptyString() throws SQLException {
        Connection con = getConnection();            
        SQLQuery query = new SQLQuery(con, "select * from "
			+ tableRef() + " where " + columnRef("s2") + " is null");
        try {
            ResultSet set = query.getResultSet();
            set.next();
            
            assertEquals("", SQLH.getEmptyString(set, "s2"));
        } finally {
            query.close();   
        }
    }

	private String tableRef() {
		return getSQLDialect().tableRef(TABLE_NAME);
	}
            
    /**
     * Test if a null value is converted into a empty string.
     */
	public void testGetEmptyCHAR() throws SQLException {
        Connection con   = getConnection();    
		SQLQuery query = new SQLQuery(con, "select * from " + tableRef()
			+ " where " + columnRef("s2") + " like '%andy%'");
        try {
            ResultSet set = query.getResultSet();
            set.next();
            
            assertEquals("Mandy", SQLH.getEmptyCHAR(set, "s2"));
            query.close();
    
			query = new SQLQuery(con, "SELECT " + columnRef("s2") + " FROM " + tableRef()
				+ " WHERE " + columnRef("s2") + " IS NULL");
            set = query.getResultSet();
            set.next();
           
            assertEquals("", SQLH.getEmptyCHAR(set, "s2"));
            query.close();
            
            // MS-Acceess cant do it with one stmt.
			query = new SQLQuery(con, "SELECT " + columnRef("s2") + " FROM " + tableRef()
				+ " WHERE " + columnRef("s2") + " IS NULL");
            set = query.getResultSet();
            set.next();
            assertEquals("", SQLH.getEmptyCHAR(set, 1));
        } finally {   
            query.close();
        }
    }
    
    /**
     * Cannot really test this without JNDICOntetx, well
     */
	public void testFetchJNDI() {
        assertNull(SQLH.fetchJNDIDataSource("notThere"));
    }

    
    /**
	 * Test if both methods of getEmptyCHAR return same results. Causes strange error in access db:
	 * "The database engine could not lock table {@link #tableRef()} because it is already in use by
	 * another person or process."
	 */
	public void testGetEmptyCHAR2() throws SQLException {
        Connection con = getConnection();            
        SQLQuery query = new SQLQuery(con, "select * from "
			+ tableRef() + " where " + columnRef("s2") + " like '%andy%'");
        try {
            ResultSet set = query.getResultSet();
            assertTrue(set.next());
            // the 6 th column in the table is 's2'
            String s1 = SQLH.getEmptyCHAR(set, "s2");
            query.close();
            
            // MS ACCES will _not_ allow acces to the same column twice:
            // java.sql.SQLException: No data found ... so 
            query = new SQLQuery(con, "select * from "
				+ tableRef() + " where " + columnRef("s2") + " like '%andy%'");
            set = query.getResultSet();
            assertTrue(set.next());
            String s2 = SQLH.getEmptyCHAR(set, 6);

            assertEquals(s1, s2);
        }
        finally {
            query.close();
        }
    }

    /**
	 * Test if both methods of getEmptyString2 return same results. Causes strange error in access
	 * db: "The database engine could not lock table {@link #tableRef()} because it is already in
	 * use by another person or process."
	 */
	public void testGetEmptyString2() throws SQLException {
        Connection con = getConnection();            
        SQLQuery query = new SQLQuery(con, "select * from "
			+ tableRef() + " where " + columnRef("s2") + " is null");
        try {
            ResultSet set = query.getResultSet();
             assertTrue(set.next());
            // the 6 th column in the table is 's2'
            String s1 = SQLH.getEmptyString(set, 6);
            query.close();
            
            // MS ACCES will _not_ allow acces to the same column twice:
            // java.sql.SQLException: No data found ... so 
            query = new SQLQuery(con, "select * from "
				+ tableRef() + " where " + columnRef("s2") + " is null");
            set = query.getResultSet();
            assertTrue(set.next());
            String s2 = SQLH.getEmptyString(set, 6);

            assertEquals(s1, s2);
        }
        finally {
            query.close();
        }
    }

    /**
     * Test if setNullKey (int value) converts the value 0 into NULL
     * writes the value into database and the selects the
     * updated row with the 'where is null' option.
     * If a row was found, method works fine.
     */
	public void testSetNullKeyInteger() throws SQLException {

        Connection con = getConnection();            
		PreparedStatement pStmt = con.prepareStatement("update " + tableRef() + " "
			+ "set " + columnRef("i2") + " =? where " + columnRef("i1") + "=-1");

        SQLH.setNullKey(pStmt, 1, 0);
        pStmt.executeUpdate();
        SQLQuery query = new SQLQuery(con, "select * from "
			+ tableRef() + " where " + columnRef("i1") + " =-1 and " + columnRef("i2") + " is null");
        ResultSet set = query.getResultSet();
        assertTrue(set.next());
        query.close();

        SQLH.setNullKey(pStmt, 1, 22);
        pStmt.executeUpdate();
        pStmt.close();
        query = new SQLQuery(con, "select * from "
			+ tableRef() + " where " + columnRef("i1") + " =-1 and " + columnRef("i2") + "=22");
        set = query.getResultSet();
        assertTrue(set.next());
        query.close();
    }

	private String columnRef(String columnName) {
		return getSQLDialect().columnRef(columnName);
	}

    /**
     * Test if setNullString converts the value "" into NULL
     * writes the value into database and the selects the
     * updated row with the 'where is null' option.
     * If a row was found, method works fine.
     */
	public void testSetNullString() throws SQLException {

        String x = "";
        Connection con = getConnection();            
        PreparedQuery pQuery = new PreparedQuery(con,
			"update " + tableRef() + " "
				+ "set " + columnRef("s2") + " =? where " + columnRef("i1") + "=2");
        PreparedStatement ps = pQuery.getPreparedStatement();             
        SQLH.setNullString(ps, 1, x);
        ps.executeUpdate();
        
        SQLQuery query = new SQLQuery(con, "select * from "
			+ tableRef() + " where " + columnRef("i1") + " =2 and " + columnRef("s2") + " is null");
        ResultSet set = query.getResultSet();
        assertTrue(set.next());
        query.close();
        
        x = "something";
        SQLH.setNullString(ps, 1, x);
        ps.executeUpdate();
        
        query = new SQLQuery(con, "select * from "
			+ tableRef() + " where " + columnRef("i1") + " =2 and " + columnRef("s2") + "='something'");
        set = query.getResultSet();
        assertTrue(set.next());
        query.close();

        x = null;
        SQLH.setNullString(ps, 1, x);
        ps.executeUpdate();
        ps.close();
        
        query = new SQLQuery(con, "select * from "
			+ tableRef() + " where " + columnRef("i1") + " =2 and " + columnRef("s2") + " is null");
        set = query.getResultSet();
        assertTrue(set.next());
        query.close();
    }
    
    /**
     * Test the testCreateLIKEParam method.
     */
	public void testCreateLIKEParam() {

        assertEquals("Nothing to do" , SQLH.createLIKEParam("Nothing to do"));
        assertEquals("No%"           , SQLH.createLIKEParam("No*"));
        assertEquals("M_ller"        , SQLH.createLIKEParam("M?ller"));
        // Will not actually work with SQL :-(
        assertEquals("Let's Go"      , SQLH.createLIKEParam("Let's Go"));
        assertEquals("100%_Questionable"      
                                     , SQLH.createLIKEParam("100%_Questionable"));
    }

    /**
     * Check creating a ODBC Datasource.
     */
	public void testBrokenDataSource() {
        Properties props = new Properties();
        
        props.setProperty("dataSource" , "com.egal.is.not.a.DataSource");
        // all other properties are called via introsprection
        props.setProperty("dataBaseName"        , "tl-basic");        // String
        props.setProperty("maintenanceInterval" , "100");  // int (special)
        props.setProperty("loginTimeout"        , "7777");  // int (special)
        try {
            SQLH.createDataSource(props);
			fail("Expected SQLException");
		} catch (SQLException ex) {
			assertEquals("Class 'com.egal.is.not.a.DataSource' not found.", ex.getMessage());
		}
    }

    /**
	 * Test suite.
	 */
    public static Test suite() {
		return suite(TestSQLH.class);
    }
}
