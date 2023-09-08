/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.AbstractComputedAttributeStorage;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;

/**
 * Many general Tests around {@link KnowledgeBase}s.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author    tsa
 */
@SuppressWarnings("javadoc")
public class TestKnowledgeBase extends BasicTestCase {

	/** Number of objects to use when updating / inserting many objects */
    static private final int MASS_UPDDATE_COUNT = 800; // 800 is will result in > 1 sec for most DBs

	/** 
     * Constructor for a special test.
     *
     * @param name function name of the test to execute.
     */
    public TestKnowledgeBase (String name) {
        super (name);
    }
    
    /** Test creation of KnowledgeObjects */
	public void testCreateKO() throws Exception {
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		KnowledgeObject ko = kb.createKnowledgeObject(KBTestMeta.TEST_B);
        ko.setAttributeValue(KBTestMeta.TEST_B_NAME, "New Person");
		tx.commit();
		tx = kb.beginTransaction();
        kb.delete(ko);
		tx.commit();
    }
	
	public void testDeleteObjectWithComputedAttribute() throws Exception {
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		KnowledgeObject ko;
		try (Transaction tx = kb.beginTransaction()) {
			ko = kb.createKnowledgeObject("TestDeleteObjectWithComputedAttribute");
			ko.setAttributeValue("name", "testDelete");
			assertEquals("testDelete-appended", ko.getAttributeValue("computed"));
			tx.commit();
		}

		try (Transaction tx = kb.beginTransaction()) {
			assertEquals("testDelete-appended", ko.getAttributeValue("computed"));

			BooleanFlag found = new BooleanFlag(false);
			UpdateListener tester = new UpdateListener() {
				@Override
				public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
					ChangeSet cs = event.getChanges();
					List<ItemDeletion> deletions = cs.getDeletions();
					for (ItemDeletion deletion : deletions) {
						if (deletion.getObjectType().getName().equals("TestDeleteObjectWithComputedAttribute")) {
							assertEquals("testDelete-appended", deletion.getOldValue("computed"));
							found.set(true);
						}
					}
				}
			};

			kb.addUpdateListener(tester);
			try {
				ko.delete();
				tx.commit();
			} finally {
				kb.removeUpdateListener(tester);
			}

			assertTrue(found.get());

			assertFalse(ko.isAlive());
		}
	}

	public static class TestingComputedAttributeStorage extends AbstractComputedAttributeStorage {

		@Override
		public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
			return ((KnowledgeItem) item).getWrapper().tGetDataString("name") + "-appended";
		}

		@Override
		public int getCacheSize() {
			return 0;
		}

	}

	public void testModifyAfterCommittedDelete() throws Exception {
		KnowledgeBase   kb          = KBSetup.getKnowledgeBase();
		KnowledgeObject ko = kb.createKnowledgeObject(KBTestMeta.TEST_B);
		ko.setAttributeValue(KBTestMeta.TEST_B_NAME, "New Person");
		assertTrue(kb.commit());
		
		kb.delete(ko);
		assertTrue(kb.commit());
		
		try {
			ko.setAttributeValue(KBTestMeta.TEST_B_NAME, "Dead Person");
			fail("Must not allow modification after deletion.");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}
	
	public void testModifyAfterLocalDelete() throws Exception {
		KnowledgeBase   kb          = KBSetup.getKnowledgeBase();
		KnowledgeObject ko = kb.createKnowledgeObject(KBTestMeta.TEST_B);
		ko.setAttributeValue(KBTestMeta.TEST_B_NAME, "New Person");
		assertTrue(kb.commit());
		
		kb.delete(ko);
		
		try {
			ko.setAttributeValue(KBTestMeta.TEST_B_NAME, "Dead Person");
			fail("Must not allow modification after deletion.");
		} catch (IllegalStateException ex) { /* Expected */ }
		
		assertTrue(kb.commit());
	}


	/**
	 * Check if outgoing relations are well-behaved.
	 * 
	 * @param relations
	 *        An Iterator over some KAs
	 */
    protected void checkOutgoingChilds(KnowledgeObject source, Iterator<KnowledgeAssociation> relations) throws Exception {
        while (relations.hasNext()) {
            KnowledgeAssociation ka = relations.next();
            assertSame(source, ka.getSourceObject());
        }
    }

	/**
	 * Check if incoming relations are well-behaved.
	 * 
	 * @param relations
	 *        An Iterator over some KAs
	 */
    protected void checkIncomingChilds(KnowledgeObject dest, Iterator<KnowledgeAssociation> relations) throws Exception {
        while (relations.hasNext()) {
            KnowledgeAssociation ka = relations.next();
            assertSame(dest, ka.getDestinationObject());
        }
    }
    
    /** Test creation and Navigation of KnowledgeObjects and KnowledgeAssociation */
    public void testIncomingRelations() throws Exception {
        KnowledgeBase               kb          = KBSetup.getKnowledgeBase();
        Collection<KnowledgeObject> webFolders  = kb.getAllKnowledgeObjects("WebFolder");
        for (KnowledgeObject webFolder : webFolders) {
            webFolder.getIncomingAssociations(KBTestMeta.ASSOC_B);  // Fill Cache
            checkIncomingChilds(webFolder, webFolder.getIncomingAssociations(KBTestMeta.ASSOC_B));
            webFolder.getIncomingAssociations();            // Fill Cache
            checkIncomingChilds(webFolder, webFolder.getIncomingAssociations());
        }
    }

    /** Test (no-) security for Incoming / Outgoing Associations */
    public void testgetObjectByAttribute() throws Exception {
        KnowledgeBase kb = KBSetup.getKnowledgeBase();
        
        String objectName = "testGetObjectByAttribute";
		KnowledgeObject object = kb.createKnowledgeObject(KBTestMeta.TEST_D);
        object.setAttributeValue(KBTestMeta.TEST_A_NAME, objectName);
        assertTrue(kb.commit());
        
        KnowledgeObject node = null;
        node = (KnowledgeObject) kb.getObjectByAttribute(
                 KBTestMeta.TEST_D, KBTestMeta.TEST_A_NAME, objectName);
        assertNotNull(node);
    
        assertSame(node, kb.getObjectsByAttribute(
                KBTestMeta.TEST_D, new String[] {KBTestMeta.TEST_A_NAME}, new Object[] {objectName}).next());
    }

    /** 
     * Test creating of new Associations in special circumstances.
     */
    public void testNewAssoc() throws Exception  {

        KnowledgeBase   kb        = KBSetup.getKnowledgeBase();
        KnowledgeAssociation ref1 = null;
    
        ThreadContext.pushSuperUser();
		try
		{
			final TLID id1 = kb.createID();
			final TLID id2 = kb.createID();

			KnowledgeObject doc1=kb.createKnowledgeObject(id1, KBTestMeta.TEST_D);
			KnowledgeObject doc2=kb.createKnowledgeObject(id2, KBTestMeta.TEST_D);
			assertTrue(kb.commit());
			
			// Fill caches partially (OPFKB only ...)
			doc1.getOutgoingAssociations        (KBTestMeta.ASSOC_B);
			doc2.getIncomingAssociations(KBTestMeta.ASSOC_B);
			
			ref1 = kb.createAssociation(doc1, doc2, KBTestMeta.ASSOC_B);
            assertTrue(kb.commit());

			assertInIterator(ref1, doc1.getOutgoingAssociations(KBTestMeta.ASSOC_B));
			assertInIterator(ref1, doc2.getIncomingAssociations(KBTestMeta.ASSOC_B));

			assertInIterator(ref1, doc1.getOutgoingAssociations());
			assertInIterator(ref1, doc1.getOutgoingAssociations(KBTestMeta.ASSOC_B,doc2));
	
			assertInIterator(ref1, doc2.getIncomingAssociations());
		}
		finally
		{
			if (ref1 != null)
				kb.delete(ref1);
            kb.commit();
			ThreadContext.popSuperUser();
		}
    }

    /** 
     * Test creating of new Associations with KO not in KB.
     */
    public void testPartialAssoc() throws Exception  {
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		Transaction tx1 = kb.beginTransaction();
		KnowledgeObject doc1 = kb.createKnowledgeObject(KBTestMeta.TEST_D);
		tx1.commit();

		Transaction tx2 = kb.beginTransaction();
		KnowledgeObject doc3 = kb.createKnowledgeObject(KBTestMeta.TEST_D);
		TLID doc3Id = doc3.getObjectName();
		kb.delete(doc3);
		tx2.commit();

		Transaction tx3 = kb.beginTransaction();
		KnowledgeObject doc4 = kb.createKnowledgeObject(doc3Id, KBTestMeta.TEST_D);
		tx3.rollback();

		try {
			kb.createAssociation(doc1, doc3, KBTestMeta.ASSOC_B);
			fail("Should not allow createAssociation with deleted object as destination");
		} catch (DataObjectException expected) {
			/* expected */
		}

		try {
			kb.createAssociation(doc4, doc1, KBTestMeta.ASSOC_B);
			fail("Should not allow createAssociation with rolled back object as source");
		} catch (DataObjectException expected) {
			/* expected */
		}
    }
    
    /** 
     * (Time) testcase for getAllKnowledgeObjects
     */
    public void testGetAllKnowledgeObjects() throws Exception  {

        KnowledgeBase   kb        = KBSetup.getKnowledgeBase();
    
        ThreadContext.pushSuperUser();
        try
        {
            startTime();
            int numAll = kb.getAllKnowledgeObjects().size();
            assertTrue(numAll > 0);
            logTime("getAllKnowledgeObjects\t" + kb);

			KnowledgeObject ko = kb.createKnowledgeObject(KBTestMeta.TEST_B);
            ko.setAttributeValue(KBTestMeta.TEST_B_NAME, "testGetAllKnowledgeObjects");

            startTime();
            assertEquals(numAll + 1, kb.getAllKnowledgeObjects().size());
            logTime("getAllKnowledgeObjects\t" + kb);

            assertTrue(kb.commit());

            startTime();
            assertEquals(numAll + 1, kb.getAllKnowledgeObjects().size());
            logTime("getAllKnowledgeObjects\t" + kb);
            
            kb.delete(ko);

            startTime();
            assertEquals(numAll, kb.getAllKnowledgeObjects().size());
            logTime("getAllKnowledgeObjects\t" + kb);
            
            assertTrue(kb.commit());
            
            startTime();
            assertEquals(numAll, kb.getAllKnowledgeObjects().size());
            logTime("getAllKnowledgeObjects\t" + kb);
        }
        finally
        {
            ThreadContext.popSuperUser();
        }
    }
    
    /**
     * Test commit after create / delete, should be a noop.
     */
    public void testCommitAfterDelete() throws Exception {
        final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        theKO.setAttributeValue(KBTestMeta.TEST_B_NAME, "Mr. Patternman");
        kBase.delete(theKO);
        kBase.commit();
    }
    
    /**
     * Test rollback after create / delete, should be a noop.
     */
    public void testRollbackAfterDelete() throws Exception {
        final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        theKO.setAttributeValue(KBTestMeta.TEST_B_NAME, "Mr. Patternman");
        kBase.delete(theKO);
        kBase.rollback();
    }
    
    /**
     * Test insert, rollback, setValue
     */
    public void testSetAfterRollback() throws Exception {
        final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        theKO.setAttributeValue(KBTestMeta.TEST_B_NAME, "testSetAfterRollback#1");
        kBase.rollback();
        try {
            theKO.setAttributeValue(KBTestMeta.TEST_B_NAME, "testSetAfterRollback#2");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) { /* expected */ }
    }
    
    /**
     * Is deleting a KO twice OK?
     */
    public void testDeleteTwice() throws Exception {
        final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        theKO.setAttributeValue(KBTestMeta.TEST_B_NAME, "testSetAfterRollback#1");
        kBase.delete(theKO);
        try {
            kBase.delete(theKO);
            fail("Expected IllegalStateException for " + kBase);
        } catch (IllegalStateException expected ) { /* expected*/ }
        assertTrue(kBase.rollback());
        
		assertFalse(theKO.isAlive());
        try {
            kBase.delete(theKO);
            fail("Expected IllegalStateException for " + kBase);
        } catch (IllegalStateException expected ) { /* expected*/ }
        assertTrue(kBase.commit()); // Should be a noop
    }

    /**
     * Is deleting a (commited) KO twice OK?
     */
    public void testDeleteTwiceCommit() throws Exception {
        final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        theKO.setAttributeValue(KBTestMeta.TEST_B_NAME, "testSetAfterRollback#1");
        assertTrue(kBase.commit());
        kBase.delete(theKO);
        try {
            kBase.delete(theKO);
            fail("Expected IllegalStateException for " + kBase);
        } catch (IllegalStateException expected ) { /* expected*/ }
        assertTrue(kBase.rollback());
        
		assertTrue(theKO.isAlive());
        kBase.delete(theKO);
        try {
            kBase.delete(theKO);
            fail("Expected IllegalStateException for " + kBase);
        } catch (IllegalStateException expected ) { /* expected*/ }
        assertTrue(kBase.commit()); // Finally delets the Object
		assertFalse(theKO.isAlive());
    }

    
    /**
     * Is insert, delete, rollback
     */
    public void testInsDelRollback() throws Exception {
        final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		KnowledgeObject theKO = kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        theKO.setAttributeValue(KBTestMeta.TEST_B_NAME, "testSetAfterRollback#1");
        kBase.delete(theKO);
        kBase.rollback();
		assertFalse(theKO.isAlive());
        try {
            kBase.delete(theKO);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected ) { /* expected*/ }
        assertTrue(kBase.commit()); // Should be a noop
    }
    
    /**
     * Test for commit with no changed objects.
     */
    public void testEmptyCommit() throws Exception {
        final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
 
		// just do an empty commit
		kBase.commit();
		kBase.commit();
    }

    /**
     * Test for rollback with no changed objects.
     */
    public void testEmptyRollback() throws Exception {
        final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
 
		// just do an empty commit
		kBase.rollback();
		kBase.rollback();
    }

	/**
	 * Tests if the caching of associations works correctly.
	 */
    public void testAssociationCaching() throws Exception {
        KnowledgeBase   kBase          = KBSetup.getKnowledgeBase();
        KnowledgeObject anObject = 
			kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        KnowledgeObject aDestination1 = 
			kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        KnowledgeObject aDestination2 = 
			kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        KnowledgeObject aDestination3 = 
			kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        KnowledgeAssociation aRefersToAssoc =
			kBase.createAssociation(anObject, aDestination1, KBTestMeta.ASSOC_B);
        KnowledgeAssociation aHasChildAssoc =
			kBase.createAssociation(anObject, aDestination1, KBTestMeta.ASSOC_A);
		Iterator<KnowledgeAssociation> it = anObject.getOutgoingAssociations(KBTestMeta.ASSOC_A);
        assertInIterator(aHasChildAssoc, it);
        // now add another refersTo association
        KnowledgeAssociation aRefersToAssoc2 =
			kBase.createAssociation(anObject, aDestination1, KBTestMeta.ASSOC_B);
        // both refersTo must be returned
        it = anObject.getOutgoingAssociations(KBTestMeta.ASSOC_B);
        assertInIterator(aRefersToAssoc, it);
        it = anObject.getOutgoingAssociations(KBTestMeta.ASSOC_B);
        assertInIterator(aRefersToAssoc2, it);
        
        kBase.delete(anObject);
        kBase.delete(aDestination1);
        kBase.delete(aDestination2);
        kBase.delete(aDestination3);
        
    }
    
    /**
     * Tests if the caching of incoming associations works correctly.
     */
    public void testIncomingAssociationCaching() throws Exception {
        KnowledgeBase   kBase          = KBSetup.getKnowledgeBase();
        KnowledgeObject anObject = 
			kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        KnowledgeObject aDestination1 = 
			kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        KnowledgeObject aDestination2 = 
			kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        KnowledgeObject aDestination3 = 
			kBase.createKnowledgeObject(KBTestMeta.TEST_B);
        KnowledgeAssociation aRefersToAssoc =
			kBase.createAssociation(aDestination1, anObject, KBTestMeta.ASSOC_B);
        KnowledgeAssociation aHasChildAssoc =
			kBase.createAssociation(aDestination1, anObject, KBTestMeta.ASSOC_A);
		Iterator<KnowledgeAssociation> it = anObject.getIncomingAssociations(KBTestMeta.ASSOC_A);
        assertInIterator(aHasChildAssoc, it);
        // now add another refersTo association
        KnowledgeAssociation aRefersToAssoc2 =
			kBase.createAssociation(aDestination1, anObject, KBTestMeta.ASSOC_B);
        // both refersTo must be returned
        it = anObject.getIncomingAssociations(KBTestMeta.ASSOC_B);
        assertInIterator(aRefersToAssoc, it);
        it = anObject.getIncomingAssociations(KBTestMeta.ASSOC_B);
        assertInIterator(aRefersToAssoc2, it);
        
        kBase.delete(anObject);
        kBase.delete(aDestination1);
        kBase.delete(aDestination2);
        kBase.delete(aDestination3); 
    }

    /**
     * Test that KAs for KOs are removed even if KO was not commited.
     */
     public void testNewDelKOKA() throws Exception {
        KnowledgeBase   kBase = KBSetup.getKnowledgeBase();
		KnowledgeObject koB = kBase.createKnowledgeObject(KBTestMeta.TEST_B);
		KnowledgeObject koC = kBase.createKnowledgeObject(KBTestMeta.TEST_C);
		KnowledgeAssociation kaBC = kBase.createAssociation(koB, koC, KBTestMeta.ASSOC_A);
		KnowledgeAssociation kaCB = kBase.createAssociation(koC, koB, KBTestMeta.ASSOC_B);
        kBase.delete(koB); //implies deleting KAs
        assertTrue(kBase.commit());
        assertSame(kBase, koC.getKnowledgeBase());
		assertFalse(koB.isAlive());
		assertFalse(kaBC.isAlive());
		assertFalse(kaCB.isAlive());
        kBase.delete(koC); // cleanup
        assertTrue(kBase.commit());
     }
    
     /**
      * Test creating and updating many objects.
      * 
      * Used to check perfomance improvement in commit-handling.
      */
     public void testMassData() throws Exception {
         
         KnowledgeObject      ko1[] = new KnowledgeObject[MASS_UPDDATE_COUNT];
         KnowledgeObject      ko2[] = new KnowledgeObject[MASS_UPDDATE_COUNT];
         KnowledgeAssociation ka[]  = new KnowledgeAssociation[MASS_UPDDATE_COUNT];
         KnowledgeBase        kb    = KBSetup.getKnowledgeBase();
         try {
             ThreadContext.pushSuperUser();
             for (int i=0; i < MASS_UPDDATE_COUNT; i++) {
				KnowledgeObject k1 = ko1[i] = kb.createKnowledgeObject(KBTestMeta.TEST_C);
                 k1.setAttributeValue(KBTestMeta.TEST_A_NAME , "testMassData" + i);
                 k1.setAttributeValue(KBTestMeta.TEST_C_NAME, "testMassData" + i);
				KnowledgeObject k2 = ko2[i] = kb.createKnowledgeObject(KBTestMeta.TEST_D);
                 k2.setAttributeValue(KBTestMeta.TEST_A_NAME , "testMassDataDoc" + i);
				ka[i] = kb.createAssociation(k1, k2, KBTestMeta.ASSOC_B);
             }
             startTime();
             assertTrue(kb.commit());
             logTime("Creating " + MASS_UPDDATE_COUNT + " in " + kb.getName());
             
             for (int i=0; i < MASS_UPDDATE_COUNT; i++) {
                 KnowledgeObject k1 = ko1[i];
                 KnowledgeObject k2 = ko2[i];

                 k1.setAttributeValue(KBTestMeta.TEST_A_NAME , "test2MassData" + i);
                 k1.setAttributeValue(KBTestMeta.TEST_C_NAME, "test2MassData" + i);
                 
                 k2.setAttributeValue(KBTestMeta.TEST_A_NAME              , "test2MassDataDoc" + i);
                 k2.setAttributeValue(KBTestMeta.TEST_D_NAME          , "langMassDataDoc" + i);
                 k2.setAttributeValue(KBTestMeta.TEST_B_NAME              , "typeMassDataDoc" + i);
                 
                 ka[i].setAttributeValue(KBTestMeta.ASSOC_B_STRING  , "refType" + i); 
             }
             startTime();
             assertTrue(kb.commit());
             logTime("Updating " + MASS_UPDDATE_COUNT + " in " + kb.getName());
             
             for (int i=0; i < MASS_UPDDATE_COUNT; i++) {
                 kb.delete(ko1[i]);
                 ko1[i] = null;
                 kb.delete(ko2[i]);
                 ko2[i] = null;
             }
             startTime();
             assertTrue(kb.commit());
             logTime("Deleting " + MASS_UPDDATE_COUNT + " in " + kb.getName());

             assertTrue(kb.commit());
         }
         finally {
             KnowledgeObject k;
             for (int i=0; i < MASS_UPDDATE_COUNT; i++) {
                 k = ko1[i];
                 if (k != null) {
                     kb.delete(k);
                 }
                 k = ko2[i];
                 if (k != null) {
                     kb.delete(k);
                 }
             }
             kb.commit();
             ThreadContext.popSuperUser();
         }
     }

    /**
      * Creating and deleting an object in the same transaction is optimized.
      * 
      * Some business logic does such stupid things, used to check perfomance 
      * improvement is this special case.
      */
     public void testStupidDelete() throws Exception {
         
         KnowledgeObject      ko1[] = new KnowledgeObject[MASS_UPDDATE_COUNT];
         KnowledgeObject      ko2[] = new KnowledgeObject[MASS_UPDDATE_COUNT];
         KnowledgeAssociation ka[]  = new KnowledgeAssociation[MASS_UPDDATE_COUNT];
         KnowledgeBase        kb    = KBSetup.getKnowledgeBase();
         try {
             ThreadContext.pushSuperUser();
             for (int i=0; i < MASS_UPDDATE_COUNT; i++) {
				KnowledgeObject k1 = ko1[i] = kb.createKnowledgeObject(KBTestMeta.TEST_C);
                 k1.setAttributeValue(KBTestMeta.TEST_A_NAME , "testMassData" + i);
                 k1.setAttributeValue(KBTestMeta.TEST_C_NAME, "testMassData" + i);
				KnowledgeObject k2 = ko2[i] = kb.createKnowledgeObject(KBTestMeta.TEST_D);
                 k2.setAttributeValue(KBTestMeta.TEST_A_NAME , "testMassDataDoc" + i);
				ka[i] = kb.createAssociation(k1, k2, KBTestMeta.ASSOC_B);
             }
             startTime();
             for (int i=0; i < MASS_UPDDATE_COUNT; i++) {
                 kb.delete(ko1[i]);
                 ko1[i] = null;
                 kb.delete(ko2[i]);
                 ko2[i] = null;
             }
             assertTrue(kb.commit()); // This should be a noop now ...
             logTime("Deleting " + MASS_UPDDATE_COUNT + " new Objects in " + kb.getName());

             for (int i=0; i < MASS_UPDDATE_COUNT; i++) {
				assertFalse("KA for new KO not removed", ka[i].isAlive());
             }
         }
         finally {
             KnowledgeObject k;
             for (int i=0; i < MASS_UPDDATE_COUNT; i++) {
                 k = ko1[i];
                 if (k != null) {
                     kb.delete(k);
                 }
                 k = ko2[i];
                 if (k != null) {
                     kb.delete(k);
                 }
             }
             kb.commit();
             ThreadContext.popSuperUser();
         }
     }
     
    /**
	 * Test case for update after rollback.
	 * 
	 * <p>
	 * This test case was intended to trigger a bug in the knowledge base,
	 * where no context rollback happened on failing db commits.
	 * Unfortunately, this test is not able to trigger a failed commit,
	 * because the exception already is thrown as response to the update
	 * statement.
	 * </p>
	 */
	public void testUpdateAfterRollback() throws Throwable {
		final KnowledgeBase kb = KBSetup.getKnowledgeBase();

		KnowledgeObject o1 = newIdxTest(kb, "x", 1, "x", 3, "b", 4);
		KnowledgeObject o2 = newIdxTest(kb, "y", 2, "x", 4, "c", 5);

		assertTrue(kb.commit());

		// Clash on index on sattr2.
		updateIdxTest(o1, "x", 1, "x", 3, "c", 4);

		assertFalse("Commit does not fail for " + kb.getName(),kb.commit()); 

		assertEquals("Value before set seen.", "b", o1
				.getAttributeValue(KBTestMeta.IDX_TEST_SATTR2));

		updateIdxTest(o1, "z", 1, "x", 3, "b", 4);
		updateIdxTest(o2, "y", 2, "x", 4, "d", 5);

		assertTrue(kb.commit());
	}

	private KnowledgeObject newIdxTest(KnowledgeBase kb, String s3, int i3, String s1, int i1, String s2, int i2) throws DataObjectException {
		KnowledgeObject o1 = kb.createKnowledgeObject(KBTestMeta.IDX_TEST);
		updateIdxTest(o1, s3, i3, s1, i1, s2, i2);
		return o1;
	}

	private void updateIdxTest(KnowledgeObject o1, String s3, int i3, String s1, int i1,
			String s2, int i2) throws DataObjectException {
		o1.setAttributeValue(KBTestMeta.IDX_SUPER_SATTR3, s3);
		o1.setAttributeValue(KBTestMeta.IDX_SUPER_IATTR3, Integer.valueOf(i3));
		o1.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, s1);
		o1.setAttributeValue(KBTestMeta.IDX_TEST_IATTR1, Integer.valueOf(i1));
		o1.setAttributeValue(KBTestMeta.IDX_TEST_SATTR2, s2);
		o1.setAttributeValue(KBTestMeta.IDX_TEST_IATTR2, Integer.valueOf(i2));
	}

    /**
     * Test different handling of nulls in indexes.
     */
    public void testNullIndex() throws Throwable {
        final KnowledgeBase kb = KBSetup.getKnowledgeBase();
        
		KnowledgeObject ko1 = kb.createKnowledgeObject(KBTestMeta.IDX_NULL_TEST);
		KnowledgeObject ko2 = kb.createKnowledgeObject(KBTestMeta.IDX_NULL_TEST);

        ko1.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, "testNullIndex-1");
        ko1.setAttributeValue(KBTestMeta.IDX_TEST_IATTR1, Integer.valueOf(1));
        
        ko2.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, "testNullIndex-2");
        ko2.setAttributeValue(KBTestMeta.IDX_TEST_IATTR1, Integer.valueOf(2));

        assertTrue(kb.commit());

        // Provoke conflict on index
        ko2.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, "testNullIndex-1");
        ko2.setAttributeValue(KBTestMeta.IDX_TEST_IATTR1, Integer.valueOf(1));

        assertFalse("Commit does not fail for " + kb.getName(),kb.commit()); 

        try {
            ko1.setAttributeValue(KBTestMeta.IDX_TEST_SATTR1, null);
        } catch (IncompatibleTypeException expected) { /* expected */ } 

        try {
            ko2.setAttributeValue(KBTestMeta.IDX_TEST_IATTR1, Integer.valueOf(1));
        } catch (IncompatibleTypeException expected) { /* expected */ } 
    }

	public void testCustomIndex() throws DataObjectException {
		KnowledgeBase kb = KBSetup.getKnowledgeBase();

		Transaction tx = kb.beginTransaction();
		newCustomIndexTestObject(kb);
		tx.commit();

		Transaction tx2 = kb.beginTransaction();
		newCustomIndexTestObject(kb);
		try {
			tx2.commit();
			fail("Unique index " + KBTestMeta.IDX_CUSTOM_UNIQUE + " is not unique.");
		} catch (Exception ex) {
			// expected
		} finally {
			tx2.rollback();
		}
	}

	private KnowledgeObject newCustomIndexTestObject(KnowledgeBase kb) throws DataObjectException {
		KnowledgeObject ko = kb.createKnowledgeObject(KBTestMeta.IDX_CUSTOM);
		ko.setAttributeValue(KBTestMeta.IDX_CUSTOM_SATTR, "testCustomIndex");
		ko.setAttributeValue(KBTestMeta.IDX_CUSTOM_IATTR, 15);
		return ko;
	}

    /** Return the suite of tests to perform. 
     */
    public static Test suite () {
		return KBSetup.getKBTest(TestKnowledgeBase.class);
		// return KBSetup.getKBTest(TestKnowledgeBase.class, KBSetup.KB_ORACLE);
		// return KBSetup.getKBTest(new TestKnowledgeBase("testNullIndex"), KBSetup.KB_ORACLE);
    }

    /** Main function for direct testing.
     */
    public static void main (String[] args) {
        
        // SHOW_TIME               = true;
        // SHOW_RESULTS            = true;   // for debugging
        // KBSetup.CREATE_TABLES   = false;  // for debugging
        
        Logger.configureStdout();   // "INFO" 
        
        junit.textui.TestRunner.run (suite ());
    }

}
