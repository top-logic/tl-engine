/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import static test.com.top_logic.knowledge.service.KBTestMeta.*;

import java.util.Iterator;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Test the commit and Rollback functions for the Knowledgebases.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestTransactions extends BasicTestCase {

    /** An Node of the Triangle created for testing. */
    KnowledgeObject person_A;
    /** An Node of the Triangle created for testing. */
    KnowledgeObject person_B;
    /** An Node of the Triangle created for testing. */
    KnowledgeObject person_C;
    
    /** An Edge of the Triangle created for testing. */
    KnowledgeAssociation ka_AB;
    /** An Edge of the Triangle created for testing. */
    KnowledgeAssociation ka_BC;
    /** An Edge of the Triangle created for testing. */
    KnowledgeAssociation ka_CA;

    /** 
     * Constructor for a special test.
     *
     * @param name function name of the test to execute.
     */
    public TestTransactions (String name) {
        super (name);
    }

    /** Helper function to check if an Object is (via ==) part of an Iterator,
     */
    protected void assertSameInIter(Iterator<?> it, Object o) {
        while (it.hasNext())
            if (it.next() == o)
                return;
        fail("Object not found in iterator");
    }
    
    /** Test creation and commiting of KnowledgeObjects */
	public void testCreateKO() throws Exception {
        KnowledgeBase   kb   = KBSetup.getKnowledgeBase();
		KnowledgeObject ko = kb.createKnowledgeObject("Person");
        ko.setAttributeValue("name","TestPerson");
        
        assertTrue(kb.commit());        

        ko.setAttributeValue("name", "TestPerson");
        assertTrue(kb.commit());    
        assertSame(kb, ko.getKnowledgeBase());           
        assertTrue(kb.commit());    // Should be a Noop
        ko.setAttributeValue("name", "TestPerson2");
        assertTrue(kb.commit());    // Mhh, this should really commit the KO
        ko.setAttributeValue("name", "TestPerson3");
        kb.delete(ko);
        assertTrue(kb.commit());    
		assertFalse(ko.isAlive());
	}
        
    /** SetUp a Triangle of KOs and KAs used for testing */
    protected void setupTriangle() throws Exception {
        KnowledgeBase   kb   = KBSetup.getKnowledgeBase();

        // TransactionDataObjectImpl.setIgnoreLockes(true);
		person_A = kb.createKnowledgeObject("Person");
		person_B = kb.createKnowledgeObject("Person");
		person_C = kb.createKnowledgeObject("Person");
        person_A.setAttributeValue("name","PersonA");
        person_B.setAttributeValue("name","PersonB");
        person_C.setAttributeValue("name","PersonC");

		ka_AB = kb.createAssociation(person_A, person_B, ASSOC_A);
		ka_BC = kb.createAssociation(person_B, person_C, ASSOC_A);
		ka_CA = kb.createAssociation(person_C, person_A, ASSOC_A);
    }

    /** Remove the Triangle of KOs and KAs by deleting them from the KB */
    protected void tearDownTriangle() throws Exception {
        // clean it all up again (removing the KOs only must do the Job, too)
        KnowledgeBase   kb   = KBSetup.getKnowledgeBase();
        person_A = safeDelete(kb, person_A);
        person_B = safeDelete(kb, person_B);
        person_C = safeDelete(kb, person_C);
        
        ka_AB = ka_BC = ka_CA = null;
    }

    /** 
     * Delete a ko only if not already deleted.
     */
    static KnowledgeObject safeDelete(KnowledgeBase kb, KnowledgeObject ko) throws DataObjectException {
		if (ko != null && ko.isAlive()) {
            kb.delete(ko);
        }
        return null;
    }

    /** Helper function to check a Triangle of KOs and KAs */
    protected void checkTriangle() 
            throws Exception {
        
        assertSame(ka_AB.getSourceObject()      , person_A);
        assertSame(ka_AB.getDestinationObject() , person_B);
        assertSame(ka_BC.getSourceObject()      , person_B);
        assertSame(ka_BC.getDestinationObject() , person_C);
        assertSame(ka_CA.getSourceObject()      , person_C);
        assertSame(ka_CA.getDestinationObject() , person_A);

        assertSameInIter(person_A.getOutgoingAssociations()                     , ka_AB);
        assertSameInIter(person_A.getIncomingAssociations()             , ka_CA);
        assertSameInIter(person_A.getOutgoingAssociations(ASSOC_A)           , ka_AB);
        assertSameInIter(person_A.getIncomingAssociations(ASSOC_A)   , ka_CA);
        assertSameInIter(person_A.getOutgoingAssociations(ASSOC_A, person_B) , ka_AB);

        assertSameInIter(person_B.getOutgoingAssociations()                     , ka_BC);
        assertSameInIter(person_B.getIncomingAssociations()             , ka_AB);
        assertSameInIter(person_B.getOutgoingAssociations(ASSOC_A)           , ka_BC);
        assertSameInIter(person_B.getIncomingAssociations(ASSOC_A)   , ka_AB);
        assertSameInIter(person_B.getOutgoingAssociations(ASSOC_A, person_C) , ka_BC);

        assertSameInIter(person_C.getOutgoingAssociations()                     , ka_CA);
        assertSameInIter(person_C.getIncomingAssociations()             , ka_BC);
        assertSameInIter(person_C.getOutgoingAssociations(ASSOC_A)           , ka_CA);
        assertSameInIter(person_C.getIncomingAssociations(ASSOC_A)   , ka_BC);
        assertSameInIter(person_C.getOutgoingAssociations(ASSOC_A, person_A) , ka_CA);
        
    }

	/** Test creation and committing of KnowledgeAssociations (Version 1) */
	public void testCreateKA() throws Exception {
        KnowledgeBase   kb   = KBSetup.getKnowledgeBase();
        ThreadContext.pushSuperUser();
        try {
            setupTriangle();
            assertTrue(kb.commit());
            // Check all the dependencies after commit
            checkTriangle();
    	}
        finally {
            tearDownTriangle();
            kb.commit();
            ThreadContext.popSuperUser();
        }
	}


    /** There where Problems with old changes being lost on commit and with commiting null-values. */
    public void testLostCommit() throws Exception {
        KnowledgeBase   kb   = KBSetup.getKnowledgeBase();
		KnowledgeObject pers = kb.createKnowledgeObject("Person");
        try {
        pers.setAttributeValue("name","Heinerle");
        assertEquals("Heinerle", pers.getAttributeValue("name"));
        assertNull  ( pers.getAttributeValue("physicalResource"));
        kb.commit();
        assertEquals("Heinerle", pers.getAttributeValue("name"));
        assertNull  (pers.getAttributeValue("physicalResource"));
        pers.setAttributeValue("physicalResource","Gaddezwersch");
        assertEquals("Heinerle"     , pers.getAttributeValue("name"));
        assertEquals("Gaddezwersch" , pers.getAttributeValue("physicalResource"));
        kb.commit();
        assertEquals("Heinerle", pers.getAttributeValue("name"));
        assertEquals("Gaddezwersch" , pers.getAttributeValue("physicalResource"));
        pers.setAttributeValue("physicalResource", null);
        assertNull  ( pers.getAttributeValue("physicalResource"));
        assertEquals("Heinerle", pers.getAttributeValue("name"));
        kb.commit();
        assertNull  ( pers.getAttributeValue("physicalResource"));
        assertEquals("Heinerle", pers.getAttributeValue("name"));
    }
        finally {
            if (pers != null) {
                kb.delete(pers);
                kb.commit();
            }
        }
    }
    
    /** Return the suite of tests to perform. 
     */
    public static Test suite () {
        return KBSetup.getKBTest(TestTransactions.class);
    }

    /** Main function for direct testing.
     */
    public static void main (String[] args) {
        
        // KBSetup.CREATE_TABLES    = false;    // for debugging
        
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

    

}
