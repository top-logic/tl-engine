/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.SQLException;
import java.util.Properties;

import junit.framework.Test;

import com.top_logic.basic.sql.SQLH;

/** Testcase for {@link com.top_logic.basic.sql.SQLH}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestSQLH extends AbstractConnectionTest {

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
	 * Cannot really test this without JNDICOntetx, well
	 */
	public void testFetchJNDI() {
        assertNull(SQLH.fetchJNDIDataSource("notThere"));
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
