/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test the indexing capabilities for the {@link com.top_logic.knowledge.service.KnowledgeBase}.
 * 
 */
public class TestKOIndex extends BasicTestCase {

    /** The number of Objects created for Testing */
    public static final int COUNT     = 800;    // So a (non indexed) Test should be around 1Sec

    /** 
     * Constructor for a special test.
     *
     * @param name function nam of the test to execute.
     */
    public TestKOIndex (String name) {
        super (name);
    }
    
	private static class MySetup extends ThreadContextSetup {

		public MySetup(Test test) {
			super(test);
		}
		
		/** SetUp a lot of {@link KBTestMeta#IDX_TEST} Objects used for testing */
		@Override
		protected void doSetUp() throws Exception {
	        KnowledgeBase   kb   = KBSetup.getKnowledgeBase();

	        deleteAllIdxTest(kb);

	        for (int i=0; i < COUNT; i++)  {
				KnowledgeObject ko = kb.createKnowledgeObject(KBTestMeta.IDX_TEST);
	            
	            String  sval = Integer.toString(i);
	            Integer ival = Integer.valueOf(i);
	            
	            ko.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, "A" + sval);
	            ko.setAttributeValue(KBTestMeta.IDX_TEST_SATTR2, "B" + sval);
	            ko.setAttributeValue(KBTestMeta.IDX_SUPER_SATTR3, "C" + sval);
	            
	            ko.setAttributeValue(KBTestMeta.IDX_TEST_IATTR1, ival);
	            ko.setAttributeValue(KBTestMeta.IDX_TEST_IATTR2, ival);
	            ko.setAttributeValue(KBTestMeta.IDX_SUPER_IATTR3, ival);
	        }
	                
	        // Logger.configureStdout("INFO");
	        assertTrue("commit failed for " + kb ,kb.commit());
		}
		
		/** Remove the {@link KBTestMeta#IDX_TEST} Objects created by doSetUp */
		@Override
		protected void doTearDown() throws Exception {
	        deleteAllIdxTest(KBSetup.getKnowledgeBase());
		}
    	
		/** 
		 * Delete all KOs of type {@link KBTestMeta#IDX_TEST}.
		 */
		private static void deleteAllIdxTest(KnowledgeBase kb) throws DataObjectException {
			try (Transaction tx = kb.beginTransaction()) {
				kb.deleteAll(kb.getAllKnowledgeObjects(KBTestMeta.IDX_TEST));
				tx.commit();
			}
		}
		
    }

	/** Test Correctness of indexed access to Strings */
	public void testStringIndex() throws Exception {
	
		KnowledgeBase kb   = KBSetup.getKnowledgeBase();

		KnowledgeObject ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_SATTR1, null);
		KnowledgeObject ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_SATTR2, null);
		KnowledgeObject ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_SUPER_SATTR3, null);
		// SATTR with null are not supported with unique indexes 
		assertNull(ko1);
		assertNull(ko2);
		assertNull(ko3);

		for (int i=0; i < COUNT; i++)  {
            String sval = Integer.toString(i);
            ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_SATTR1, "A" + sval);
		    ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_SATTR2, "B" + sval);
            ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, "C" + sval);
            assertNotNull(sval, ko1);
    		assertEquals(sval,  ko1,ko2);
    		assertEquals(sval,  ko2,ko3);
		}
	}

	/** Test Correctness of indexed access to Strings via Iterator */
	public void testStringIter() throws Exception {
	
		KnowledgeBase kb = KBSetup.getKnowledgeBase();

		for (int i=0; i < COUNT; i++)  {
	        String sval = Integer.toString(i);
	        Iterator<? extends DataObject> i1 = kb.getObjectsByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_SATTR1 , "A" + sval);
		    Iterator<? extends DataObject> i2 = kb.getObjectsByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_SATTR2 , "B" + sval);
		    Iterator<? extends DataObject> i3 = kb.getObjectsByAttribute(KBTestMeta.IDX_SUPER, KBTestMeta.IDX_SUPER_SATTR3, "C" + sval);
	        assertTrue(i1.hasNext());
            KnowledgeObject ko1 = (KnowledgeObject) i1.next();
			assertEquals(ko1,i2.next());
    		assertTrue(!i1.hasNext());
		    assertTrue(!i2.hasNext());
		    assertTrue(!i3.hasNext());
		}
	}

	/** Test Correctness of indexed access to Integers */
	public void testIntIndex() throws Exception {
	
		KnowledgeBase kb   = KBSetup.getKnowledgeBase();
        
		KnowledgeObject ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_IATTR1, null);
		KnowledgeObject ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_IATTR2, null);
		KnowledgeObject ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, null);
		assertNull(ko1);
		assertNull(ko2);
		assertNull(ko3);

		for (int i=0; i < COUNT; i++)  {
	        Integer ival = Integer.valueOf(i);
	        ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_IATTR1, ival);
		    ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_IATTR2, ival);
            ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_IATTR2, ival);
	        assertNotNull(ko1);
			assertEquals(ko1,ko2);
			assertEquals(ko2,ko3);
		}
	}

	/** Test Changing values and committing them. */
	public void testCommit() throws Exception {
	
		KnowledgeBase kb   = KBSetup.getKnowledgeBase();
        
        String testVal = Integer.toString(COUNT >> 1);

		KnowledgeObject ref = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR1, "A" + testVal);
		KnowledgeObject ko1;
		KnowledgeObject ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR2, "B" + testVal);
		KnowledgeObject ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, "C" + testVal);
		
		// getObjectByAttribute should not search in subclasses, even inside an open transaction.
		KnowledgeObject ko4 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_SUPER,KBTestMeta.IDX_SUPER_SATTR3, "C" + testVal);
		assertNotNull(ref);
		assertSame(ref,ko2);
		assertSame(ko2,ko3);
		assertNull(ko4);
        
        String otherVal = Integer.toString(COUNT);

	    ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR1 , "A" + otherVal);
		ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR2 , "B" + otherVal);
		ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);
        ko4 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_SUPER,KBTestMeta.IDX_SUPER_SATTR3, "C" + testVal);
	    assertNull(ko1);
		assertNull(ko2);
		assertNull(ko3);
        assertNull(ko4);
        
        ref.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, "A" + otherVal);
        ref.setAttributeValue(KBTestMeta.IDX_TEST_SATTR2, "B" + otherVal);
        ref.setAttributeValue(KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);
        
        ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR1 , "A" + otherVal);
        ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR2 , "B" + otherVal);
        ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);
        ko4 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_SUPER,KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);

		assertSame(kb.getName(), ref, ko1);
		assertSame(kb.getName(), ref, ko2);
		assertSame(kb.getName(), ref, ko3);
		assertNull(ko4);
        
	    kb.commit();

	    ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR1, "A" + otherVal);
	    ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR2, "B" + otherVal);
  	    ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);
        ko4 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_SUPER,KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);
	    assertSame(ref,ko1);
	    assertSame(ref,ko2);
	    assertSame(ref,ko3);
        assertNull(ko4);

	    ref.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1 , "A" + testVal);
	    ref.setAttributeValue(KBTestMeta.IDX_TEST_SATTR2 , "B" + testVal);
	    ref.setAttributeValue(KBTestMeta.IDX_SUPER_SATTR3, "C" + testVal);
	
	    ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR1, "A" + otherVal);
	    ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR2, "B" + otherVal);
        ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);
        ko4 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_SUPER,KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);
        
	    assertNull(ko1);
	    assertNull(ko2);
	    assertNull(ko3);
        assertNull(ko4);
		    
        // Back to original value.
    	kb.commit();

	    ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR1, "A" + otherVal);
	    ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR2, "B" + otherVal);
        ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, "C" + otherVal);
	    assertNull(ko1);
	    assertNull(ko2);
	    assertNull(ko3);

	    ko1 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR1, "A" + testVal);
	    ko2 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR2, "B" + testVal);
        ko3 = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_SATTR3, "C" + testVal);
	    assertSame(ref,ko1);
	    assertSame(ref,ko2);
	    assertSame(ref,ko3);
	}

	/** Test A Failed commit due to an Index constraint */
    public void testFailedIndex() throws Exception {
        KnowledgeBase kb   = KBSetup.getKnowledgeBase();
        
        String          testVal = "testFailedIndex";
        Integer         count   = Integer.valueOf(COUNT);
        KnowledgeObject ko      = null;
        try {
            ko = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_SATTR2, testVal);
            if (ko != null) { // Just in case ...
                kb.delete(ko);
            }
			ko = kb.createKnowledgeObject(KBTestMeta.IDX_TEST);
            ko.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, testVal);
            ko.setAttributeValue(KBTestMeta.IDX_TEST_SATTR2, testVal);
            ko.setAttributeValue(KBTestMeta.IDX_SUPER_SATTR3, testVal);
            
            ko.setAttributeValue(KBTestMeta.IDX_TEST_IATTR1 , count);
            ko.setAttributeValue(KBTestMeta.IDX_TEST_IATTR2 , count);
            ko.setAttributeValue(KBTestMeta.IDX_SUPER_IATTR3, count);
            
            assertTrue(kb.commit());
            // Provoke duplicate Key
			ko = kb.createKnowledgeObject(KBTestMeta.IDX_TEST);
            ko.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, testVal);
            ko.setAttributeValue(KBTestMeta.IDX_TEST_SATTR2, testVal);
            ko.setAttributeValue(KBTestMeta.IDX_SUPER_SATTR3, testVal);
            
            count   = Integer.valueOf((COUNT + 17));
            ko.setAttributeValue(KBTestMeta.IDX_TEST_IATTR1 , count);
            ko.setAttributeValue(KBTestMeta.IDX_TEST_IATTR2 , count);
            ko.setAttributeValue(KBTestMeta.IDX_SUPER_IATTR3, count);
            
            assertFalse("Commit failed for " + kb, kb.commit()); 
            
            // Check that rollback was handled correctly
            
			assertFalse(ko.isAlive());
            ko = (KnowledgeObject) kb.getObjectByAttribute(KBTestMeta.IDX_TEST , KBTestMeta.IDX_TEST_SATTR2, testVal);

            assertEquals(testVal, ko.getAttributeValue(KBTestMeta.IDX_TEST_SATTR1));
            assertEquals(testVal, ko.getAttributeValue(KBTestMeta.IDX_TEST_SATTR2));
            assertEquals(testVal, ko.getAttributeValue(KBTestMeta.IDX_SUPER_SATTR3));
            
            count   = Integer.valueOf(COUNT);
            assertEquals(count, ko.getAttributeValue(KBTestMeta.IDX_TEST_IATTR1));
            assertEquals(count, ko.getAttributeValue(KBTestMeta.IDX_TEST_IATTR2));
            assertEquals(count, ko.getAttributeValue(KBTestMeta.IDX_SUPER_IATTR3));

        } finally { 
            if (ko != null) {
                kb.delete(ko);
            }
        }

    }

    
    /** Helper function for speedTest to check a single index */
    protected void checkStringIndex(KnowledgeBase kb, String type, String attrName, String prefix) {

	    KnowledgeObject ko = (KnowledgeObject)  kb.getObjectByAttribute(type, attrName, null);
	    assertNull(ko);
	    for (int i=0; i < COUNT; i++)  {
	        ko = (KnowledgeObject)  kb.getObjectByAttribute(type, attrName, prefix + i);
	        assertNotNull(ko);
	    }
    }
    
    /** Helper function for speedTest to check a single index */
    protected void checkIntIndex(KnowledgeBase kb, String type, String attrName) {

        KnowledgeObject ko = (KnowledgeObject)  kb.getObjectByAttribute(type, attrName, null);
        assertNull(ko);
        for (int i=0; i < COUNT; i++)  {
            ko = (KnowledgeObject)  kb.getObjectByAttribute(type, attrName, Integer.valueOf(i));
            assertNotNull(ko);
        }
    }

    
	/** Test Speed of using the several Indexes for {@link KBTestMeta#IDX_TEST} */
	public void testSpeed() throws Exception {

	    KnowledgeBase kb   = KBSetup.getKnowledgeBase();
        boolean       dbkb = true;

        String logStr = kb.getName() + ", " + COUNT + " Elements ";
	    startTime();
        checkStringIndex(kb, KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR1, "A");
	    logTime(logStr + "s1");
	    checkStringIndex(kb, KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_SATTR2, "B");
	    logTime(logStr + "s2");
        if (dbkb)
    	    checkStringIndex(kb, KBTestMeta.IDX_TEST,KBTestMeta.IDX_SUPER_SATTR3, "C");
        else
	    checkStringIndex(kb, KBTestMeta.IDX_SUPER,KBTestMeta.IDX_SUPER_SATTR3, "C");
	    logTime(logStr + "s3");
	    checkIntIndex(kb, KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_IATTR1);
	    logTime(logStr + "i1");
	    checkIntIndex(kb, KBTestMeta.IDX_TEST, KBTestMeta.IDX_TEST_IATTR2);
	    logTime(logStr + "i2");
        if (dbkb)
	        checkIntIndex(kb, KBTestMeta.IDX_TEST, KBTestMeta.IDX_SUPER_IATTR3);
        else
	    checkIntIndex(kb, KBTestMeta.IDX_SUPER, KBTestMeta.IDX_SUPER_IATTR3);
	    logTime(logStr + "i3");
	}
    
    /** Helper function for speedTest to check a single index */
    protected void checkDBIndex(Connection con, String attrName, String prefix) 
        throws SQLException {
        
        PreparedStatement stm = con.prepareStatement(
            "SELECT * FROM I_D_X_Test WHERE " + attrName + " = ?");
        ResultSet         res = null;
        try {
            res = stm.executeQuery("SELECT * FROM I_D_X_Test WHERE " + attrName + " IS NULL");
            assertTrue(res.next());
            assertTrue(!res.next());
            res.close();
            for (int i=0; i < COUNT; i++)  {
                stm.setString(1,  prefix + i);
                res = stm.executeQuery();
                assertTrue(res.next());
                assertTrue(!res.next());
                res.close();
            }
        } finally  {
            if (stm != null)
                stm.close();
            if (res != null)
                res.close();
        }    
    }

    /** Helper function for speedTest to check a single index */
    protected void checkDBIntIndex(Connection con, String attrName) 
        throws SQLException {
        
        PreparedStatement stm = con.prepareStatement(
            "SELECT * FROM I_D_X_Test WHERE " + attrName + " = ?");
        ResultSet         res = null;
        try {
            res = stm.executeQuery("SELECT * FROM I_D_X_Test WHERE " + attrName + " IS NULL");
            assertTrue(res.next());
            assertTrue(!res.next());
            res.close();
            for (int i=0; i < COUNT; i++)  {
                stm.setInt(1, i);
                res = stm.executeQuery();
                assertTrue(res.next());
                assertTrue(!res.next());
                res.close();
            }
        } finally  {
            if (stm != null)
                stm.close();
            if (res != null)
                res.close();
        }    
    }

    /** Test Speed by using Database directly */
    public void doDBSpeed() throws Exception {
        KnowledgeBase dbkb = KBSetup.getKnowledgeBase();
        Connection con = KBUtils.createCommitContext(dbkb).getConnection();
        
        String logStr = "[ Direct SQL ], " + COUNT + " Elements ";
        startTime();
        checkDBIndex(con, KBTestMeta.IDX_TEST_SATTR1, "A");
        logTime(logStr + "s1");
        checkDBIndex(con, KBTestMeta.IDX_TEST_SATTR2, "B");
        logTime(logStr + "s2");
        checkDBIndex(con, KBTestMeta.IDX_SUPER_SATTR3, "C");
        logTime(logStr + "s3");
        checkDBIntIndex(con, KBTestMeta.IDX_TEST_IATTR1);
        logTime(logStr + "i1");
        checkDBIntIndex(con, KBTestMeta.IDX_TEST_IATTR2);
        logTime(logStr + "i2");
        checkDBIntIndex(con, KBTestMeta.IDX_SUPER_IATTR3);
        logTime(logStr + "i3");
    }

    /** Test failed indexed access */
    public void testNotInIndex() throws Exception {
    
    	KnowledgeBase kb = KBSetup.getKnowledgeBase();

        assertNull(kb.getObjectByAttribute(KBTestMeta.IDX_TEST,  KBTestMeta.IDX_TEST_SATTR1, "Not There"));
    	assertNull(kb.getObjectByAttribute(KBTestMeta.IDX_TEST,  KBTestMeta.IDX_TEST_SATTR2, "Not There"));
    	assertNull(kb.getObjectByAttribute(KBTestMeta.IDX_SUPER, KBTestMeta.IDX_SUPER_SATTR3, "Not There"));

    	Integer ival = Integer.valueOf(Integer.MAX_VALUE);
    	assertNull(kb.getObjectByAttribute(KBTestMeta.IDX_TEST,  KBTestMeta.IDX_TEST_IATTR1, ival));
    	assertNull(kb.getObjectByAttribute(KBTestMeta.IDX_TEST,  KBTestMeta.IDX_TEST_IATTR2, ival));
    	assertNull(kb.getObjectByAttribute(KBTestMeta.IDX_SUPER, KBTestMeta.IDX_SUPER_IATTR3, ival));
    }

    /** Return the suite of tests to perform. 
     */
    public static Test suite () {
        return KBSetup.getKBTest(TestKOIndex.class, new TestFactory() {
        	@Override
			public Test createSuite(Class testCase, String suiteName) {
        		return new MySetup(new TestSuite(TestKOIndex.class));
        	}
        });
    }

    /** Main function for direct testing.
     */
    public static void main (String[] args) {

        // SHOW_TIME               = true;     // show results of logTime()
        // KBSetup.CREATE_TABLES   = false;    // do not drop/create/ import tables

        Logger.configureStdout();
        junit.textui.TestRunner.run (suite ());
    }
}

