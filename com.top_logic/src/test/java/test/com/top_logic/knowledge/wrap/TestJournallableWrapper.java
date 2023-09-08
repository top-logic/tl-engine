/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.TLID;
import com.top_logic.knowledge.journal.ChangeJournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalEntry;
import com.top_logic.knowledge.journal.JournalManager;
import com.top_logic.knowledge.journal.JournalResult;
import com.top_logic.knowledge.journal.JournalResultEntry;
import com.top_logic.knowledge.journal.MessageJournalAttributeEntry;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.JournallableWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Testcase for the {@link JournallableWrapper} and its inner <code>WrapperJournalEntry</code>.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestJournallableWrapper extends BasicTestCase {

    /**
     * Create a TestJournallableWrapper for given (fucntion-) name-
     * 
     * @param aTest name of test to execute.
     */
    public TestJournallableWrapper(String aTest) {
        super(aTest);
    }

    /**
     * Simple Test method for getJournalEntry(Map, Map, Map)
     */
    public void testGetNullJournalEntry() throws Exception {
        KnowledgeBase       theKB = KBSetup.getKnowledgeBase();
		Transaction tx = theKB.beginTransaction();
		KnowledgeObject adrKO = theKB.createKnowledgeObject(TestingJournallableWrapper.OBJECT_NAME);
		tx.commit();
		Object theO = WrapperFactory.getWrapper(adrKO);
        TestingJournallableWrapper jAdr  = (TestingJournallableWrapper) theO;
        
        assertNull(jAdr.getJournalEntry(null, null, null));
        
		Transaction deleteTx = theKB.beginTransaction();
        theKB.delete(adrKO);
		deleteTx.commit();
    }

    /**
     * Complex Test method for getJournalEntry(Map, Map, Map)
     */
    public void testGetJournalEntry() throws Exception {
        KnowledgeBase       theKB = KBSetup.getKnowledgeBase();

		KnowledgeObject adrKO = theKB.createKnowledgeObject(TestingJournallableWrapper.OBJECT_NAME);
		TLID id = adrKO.getObjectName();

		Object theO = WrapperFactory.getWrapper(adrKO);
        TestingJournallableWrapper jAdr  = (TestingJournallableWrapper) theO;
        
		Integer inty = Integer.valueOf(-99);
        jAdr.setValue("toBeRemoved", Boolean.TRUE);
        jAdr.setValue("toBeChanged", inty);
        assertTrue(theKB.commit());

        Date   now       = new Date();
		Double theDouble = Double.valueOf(77);
        jAdr.setValue("toBeRemoved", null);
        jAdr.setValue("toBeChanged", theDouble);
        jAdr.setValue("thisIsNew"  , now);
        
        JournalEntry jEntry = jAdr.getJournalEntry(null, null, null);
        assertTrue(theKB.commit());

        assertEquals(id, jEntry.getIdentifier());
        assertEquals(TestingJournallableWrapper.OBJECT_NAME            , jEntry.getType());
        
        Iterator iter = jEntry.getAttributes().iterator();
        while (iter.hasNext()) {
            ChangeJournalAttributeEntry jAttr    = (ChangeJournalAttributeEntry) iter.next();
            String                      attrName = jAttr.getName();
            if ("toBeRemoved".equals(attrName)) {
                assertEquals(Boolean.class.getName(), jAttr.getType());
                assertEquals(Boolean.TRUE           , jAttr.getPreValue());
                assertNull  (                         jAttr.getPostValue());
            } else if ("toBeChanged".equals(attrName)) {
                // TODO TSA/KHA this is only half correct ;-) think about it 
                assertEquals(Double.class.getName() , jAttr.getType());
                assertEquals(inty                   , jAttr.getPreValue());
                assertEquals( theDouble             , jAttr.getPostValue());
            } else if ("thisIsNew".equals(attrName)) {
                assertEquals(Date.class.getName()   , jAttr.getType());
                assertNull  (                         jAttr.getPreValue());
                assertEquals(now                    , jAttr.getPostValue());
            } else {
                fail("Unexpected Attribute '" + attrName + "'");
            }
        }
        
        jAdr.tDelete();
        assertTrue(theKB.commit());
    }

    /**
     * Complex Test method for getJournalEntry(Map, Map, Map)
     */
    public void testJournalAndDelete() throws Exception {
        KnowledgeBase       theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject adrKO = theKB.createKnowledgeObject(TestingJournallableWrapper.OBJECT_NAME);

		Object theO = WrapperFactory.getWrapper(adrKO);
        TestingJournallableWrapper jAdr  = (TestingJournallableWrapper) theO;
        
		Integer inty = Integer.valueOf(-99);
        jAdr.setValue("toBeRemoved", Boolean.TRUE);
        jAdr.setValue("toBeChanged", inty);
        
        jAdr.getJournalEntry(Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
        assertTrue(theKB.commit());
        
        jAdr.tDelete();
        assertTrue(theKB.commit());
        
    }

    /**
     * Test method for JournallableWrapper.getJournalType()
     */
    public void testGetJournalType() throws Exception {

        KnowledgeBase       theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject adrKO = theKB.createKnowledgeObject(TestingJournallableWrapper.OBJECT_NAME);

		TestingJournallableWrapper jAdr = (TestingJournallableWrapper) WrapperFactory.getWrapper(adrKO);
        
        assertEquals(TestingJournallableWrapper.OBJECT_NAME, jAdr.getJournalType());
        theKB.delete(adrKO);
        assertEquals(null     , jAdr.getJournalType());
        
        assertTrue(theKB.commit());
    }
    
    /**
     * Test Usage of the JournallableWrapper with the JournalManager.
     * 
     * TODO TSA make this work 
     */
    public void doNotTestJournalUsage() throws Exception {
        KnowledgeBase       theKB = KBSetup.getKnowledgeBase();
		KnowledgeObject adrKO = theKB.createKnowledgeObject(TestingJournallableWrapper.OBJECT_NAME);
        
        JournalManager jMgr = JournalManager.getInstance();
        jMgr.addType(TestingJournallableWrapper.OBJECT_NAME);
        
		Object theO = WrapperFactory.getWrapper(adrKO);
        TestingJournallableWrapper jAdr  = (TestingJournallableWrapper) theO;
		TLID ident = KBUtils.getWrappedObjectName(jAdr);
        
		Integer inty = Integer.valueOf(-99);
        jAdr.setValue("toBeRemoved", Boolean.TRUE);
        jAdr.setValue("toBeChanged", inty);

        assertTrue(theKB.commit());
        jMgr.flush();

        List jResults = jMgr.getJournal(ident, theKB.getName());
        assertEquals(1, jResults.size());
        JournalResult jRes = (JournalResult) jResults.get(0);
        Iterator      outer = jRes.entries.iterator();
        while (outer.hasNext()) {
            JournalResultEntry jRE  = (JournalResultEntry) outer.next();
            Iterator           iter = jRE.getAttributEntries().iterator();
            while (iter.hasNext()) {
                ChangeJournalAttributeEntry jAttr = (ChangeJournalAttributeEntry) iter.next();
                String  attrName = jAttr.getName();
                if ("toBeRemoved".equals(attrName)) {
                    assertEquals(Boolean.class.getName(), jAttr.getType());
                    assertNull  (                         jAttr.getPreValue());
                    assertEquals(Boolean.TRUE           , jAttr.getPostValue());
                } else if ("toBeChanged".equals(attrName)) {
                    // TODO TSA/KHA this is only half correct ;-) think about it 
                    assertEquals(Double.class.getName() , jAttr.getType());
                    assertNull  (                         jAttr.getPreValue());
                    assertEquals(inty                   , jAttr.getPostValue());
                } 
                    fail("Unexpected Attribute '" + attrName + "'");
                }
        }

        Date   now       = new Date();
		Double theDouble = Double.valueOf(77);
        jAdr.setValue("toBeRemoved", null);
        jAdr.setValue("toBeChanged", theDouble);
        jAdr.setValue("thisIsNew"  , now);

        assertTrue(theKB.commit());
        jMgr.flush();
        jResults = jMgr.getJournal(ident, theKB.getName());
        assertEquals(2, jResults.size());
        jRes = (JournalResult) jResults.get(1);
        outer = jRes.entries.iterator();
        while (outer.hasNext()) {
            ChangeJournalAttributeEntry jAttr    = (ChangeJournalAttributeEntry) outer.next();
            String                      attrName = jAttr.getName();
            if ("toBeRemoved".equals(attrName)) {
                assertEquals(Boolean.class.getName(), jAttr.getType());
                assertEquals(Boolean.TRUE           , jAttr.getPreValue());
                assertNull  (                         jAttr.getPostValue());
            } else if ("toBeChanged".equals(attrName)) {
                // TODO TSA/KHA this is only half correct ;-) think about it 
                assertEquals(Double.class.getName() , jAttr.getType());
                assertEquals(inty                   , jAttr.getPreValue());
                assertEquals(theDouble              , jAttr.getPostValue());
            } else if ("thisIsNew".equals(attrName)) {
                assertEquals(Date.class.getName()   , jAttr.getType());
                assertNull  (                         jAttr.getPreValue());
                assertEquals(now                    , jAttr.getPostValue());
            } else {
                fail("Unexpected Attribute '" + attrName + "'");
            }
        }

        jAdr.tDelete();
        assertTrue(theKB.commit());
        
        jResults = jMgr.getJournal(ident, theKB.getName());
        assertEquals(3, jResults.size());
        jRes = (JournalResult) jResults.get(2);
        outer = jRes.entries.iterator();
        while (outer.hasNext()) {
            MessageJournalAttributeEntry mAttr = (MessageJournalAttributeEntry) outer.next();
            assertEquals("Deleted", mAttr.getJournalMessage());
        }
        // TODO TSA/KHA can we do that
        // jMgr.removeType ("Address");
    }


    /** "MisUse" Address as JournallableWrapper */
    public static class TestingJournallableWrapper extends JournallableWrapper {
        
        /** Test if rollback was called */
        public boolean rolledBack;

		private static final String OBJECT_NAME = "TestJournallableWrapper";

        public TestingJournallableWrapper(KnowledgeObject ko) {
            super(ko);
        }
        
    }

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(TestJournallableWrapper.class);
    }
    
}
