/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.event;

import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.event.CommitChecker;
import com.top_logic.knowledge.service.event.CommitVetoException;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.service.event.ModificationListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.Utils;

/**
 * Test event handling in knowledge base.
 *
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class TestKBEventHandling extends BasicTestCase {

	static final Locale LOCALE1 = Locale.ENGLISH;

	static final Locale LOCALE2 = Locale.CHINESE;

	static final Locale LOCALE3 = Locale.KOREAN;

    /** a dummy listener for KBChangeEvents. */
    DummyKBEventListener listener1;
    DummyKBEventListener listener2;

    /** some objects in the kb. */
    KnowledgeObject object1;
    KnowledgeObject object2;
    KnowledgeObject object3;
    KnowledgeObject dummyKO;

    /** 
     * Constructor for a special test.
     *
     * @param name fucntion nam of the test to execute.
     */
    public TestKBEventHandling(String name) {
        super(name);
    }

    /**
     * Setup method.
     */
    @Override
	protected void setUp() throws Exception {
    	super.setUp();
		KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		listener1 = new DummyKBEventListener("Listener_1");
		listener2 = new DummyKBEventListener("Listener_2");
		kBase.addModificationListener(listener1);
		kBase.addModificationListener(listener2);
		kBase.addCommitChecker(listener1);
		kBase.addCommitChecker(listener2);

		Transaction setUpTx = kBase.beginTransaction();
		// create some objects
		object1 = kBase.createKnowledgeObject(Person.OBJECT_NAME);
		object2 = kBase.createKnowledgeObject(Person.OBJECT_NAME);
		object3 = kBase.createKnowledgeObject(Person.OBJECT_NAME);
		object1.setAttributeValue(Person.NAME_ATTRIBUTE, "old1");
		object2.setAttributeValue(Person.NAME_ATTRIBUTE, "old2");
		object3.setAttributeValue(Person.NAME_ATTRIBUTE, "old3");
		object1.setAttributeValue(Person.LOCALE, Utils.formatLocale(LOCALE1));
		object2.setAttributeValue(Person.LOCALE, Utils.formatLocale(LOCALE1));
		object3.setAttributeValue(Person.LOCALE, Utils.formatLocale(LOCALE1));
		setUpTx.commit();
    } 
    
    @Override
	protected void tearDown() throws Exception {
    	KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		kBase.removeCommitChecker(listener2);
		kBase.removeCommitChecker(listener1);
		kBase.removeModificationListener(listener2);
		kBase.removeModificationListener(listener1);
		Transaction tearDownTx = kBase.beginTransaction();
		if (object1.isAlive()) {
			object1.delete();
		}
		if (object2.isAlive()) {
			object2.delete();
		}
		if (object3.isAlive()) {
			object3.delete();
		}
		if (dummyKO != null && dummyKO.isAlive())
			dummyKO.delete();
		tearDownTx.commit();
    	
    	listener1 = listener2 = null;
    	object1   = object2   = object3 = dummyKO = null;
    	
    	super.tearDown();
    }
    
	/**
	 * Tests that an {@link ModificationListener} can delete other objects.
	 */
	public void testDeleteInVetoEvent() throws DataObjectException {
		ModificationListener l = new ModificationListener() {

			@Override
			public Modification createModification(KnowledgeBase kb,
					Map<ObjectKey, ? extends KnowledgeItem> createdObjects,
					Map<ObjectKey, ? extends KnowledgeItem> updatedObjects,
					Map<ObjectKey, ? extends KnowledgeItem> removedObjects) {
				if (removedObjects.containsKey(object3.tId())) {
					return Modification.NONE;
				} else {
					return new Modification() {

						@Override
						public void execute() throws DataObjectException {
							if (object3.isAlive()) {
								object3.delete();
							}
						}

					};

				}
			}

			@Override
			public Modification notifyUpcomingDeletion(KnowledgeBase kb, KnowledgeItem item) {
				return Modification.NONE;
			}

		};
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		kb.addModificationListener(l);
		try {
			Transaction delTx = kb.beginTransaction();
			try {
				object1.delete();
			} catch (StackOverflowError err) {
				delTx.rollback();
				fail("Ticket #9341", err);
			} catch (ConcurrentModificationException ex) {
				delTx.rollback();
				fail("Ticket #9341:", ex);
			}
			delTx.commit();

			assertFalse(object1.isAlive());
			assertFalse(object3.isAlive());

			assertTrue(object2.isAlive());
		} finally {
			kb.removeModificationListener(l);
		}

	}

	/**
	 * Tests that an {@link ModificationListener} can not change objects during call.
	 */
	public void testProvideChangeDuringCall() throws DataObjectException {
		ModificationListener l = new ModificationListener() {

			@Override
			public Modification createModification(KnowledgeBase kb,
					Map<ObjectKey, ? extends KnowledgeItem> createdObjects,
					Map<ObjectKey, ? extends KnowledgeItem> updatedObjects,
					Map<ObjectKey, ? extends KnowledgeItem> removedObjects) {
				return Modification.NONE;
			}

			@Override
			public Modification notifyUpcomingDeletion(KnowledgeBase kb, KnowledgeItem item) {
				if (object3.isAlive()) {
					object3.setAttributeValue(Person.LOCALE, Utils.formatLocale(LOCALE2));
				}
				return Modification.NONE;
			}

		};
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		kb.addModificationListener(l);
		try {
			Object previousValue = object3.getAttributeValue(Person.LOCALE);
			Transaction delTx = kb.beginTransaction();
			try {
				try {
					object1.delete();
					fail("Expected failure because the listener changes object instead creating Modification which do it.");
				} catch (IllegalStateException err) {
					// expected
				}

				assertTrue(object1.isAlive());
				assertTrue(object2.isAlive());
				assertTrue(object3.isAlive());
				assertTrue(object3.isAlive());
				assertEquals(previousValue, object3.getAttributeValue(Person.LOCALE));
			} finally {
				delTx.rollback();
			}
		} finally {
			kb.removeModificationListener(l);
		}

	}

    /**
     * Test put and remove KBChangeEvents.
     */
    public void testPutRemove() throws Exception {
		// set listener to non-veto
		listener1.resetCounts();
		listener2.resetCounts();
		listener1.setVeto(false);
		listener2.setVeto(false);
		doNewDeleteKO();
		// New, Commit, Remove, Commit -> 4
		assertEquals("Listener1 has not received event", 4, listener1.count);
		assertEquals("Listener2 has not received event", 4, listener2.count);
		assertTrue("Listener1 has not received Delete event", !listener1.getLastVetoEvent().getDeletedObjectKeys()
			.isEmpty());
		assertTrue("Listener2 has not received Delete event", !listener2.getLastVetoEvent().getDeletedObjectKeys()
				.isEmpty());

		// set listener to veto
		listener1.resetCounts();
		listener2.resetCounts();
		listener1.setVeto(true);
		listener2.setVeto(true);
        try {
			doNewDeleteKO();
			fail("No KBChangeVetoException thrown.");
		} catch (CommitVetoException e) {
			// this exception is expected
        }
		assertEquals("Listener1 has not received event", 2, listener1.count);
		assertEquals("Listener2 has not received event", 1, listener2.count);
		assertTrue("Listener1 has nor received Put event", !listener1.getLastVetoEvent().getCreatedObjectKeys()
			.isEmpty());

		// set listener2 to veto
		listener1.resetCounts();
		listener2.resetCounts();
		listener1.setVeto(false);
		listener2.setVeto(true);
		try {
			doNewDeleteKO();
			fail("No KBChangeVetoException thrown.");
		} catch (CommitVetoException e) {
			// this exception is expected
			assertEquals(
				"Exception has wrong listener,",
				e.getRootListener(),
				listener2);
			assertEquals(
				"Exception has wrong event,",
				e.getRootEvent(),
				listener2.getLastVetoEvent());
		}
		assertEquals("Listener1 has nor received event", 2, listener1.count);
		assertEquals("Listener2 has nor received event", 2, listener2.count);
		assertTrue("Listener1 has nor received Put event", !listener1.getLastVetoEvent().getCreatedObjectKeys()
			.isEmpty());
		assertTrue("Listener2 has nor received Put event", !listener2.getLastVetoEvent().getCreatedObjectKeys()
			.isEmpty());
    }

	/**
	 * Test commit.
	 */
	public void testCommitEvent() throws DataObjectException {
		KnowledgeBase kBase = KBSetup.getKnowledgeBase();
		// test with veto
		listener1.setVeto(true);
		listener1.resetCounts();

		final Transaction failingTX = kBase.beginTransaction();
		object1.setAttributeValue(Person.LOCALE, Utils.formatLocale(LOCALE3));
		object2.setAttributeValue(Person.LOCALE, Utils.formatLocale(LOCALE3));
		try {
			failingTX.commit();
			fail("expected listener has veto");
		} catch (KnowledgeBaseException ex) {
			// expected
		}
		assertEquals("Number of changed objects wrong.", 3, listener1.count);
		assertEquals("Wrong value for object1.", Utils.formatLocale(LOCALE1),
			object1.getAttributeValue(Person.LOCALE));

		// test without veto
		listener1.setVeto(false);
		listener1.resetCounts();

		final Transaction successFullTX = kBase.beginTransaction();
		object1.setAttributeValue(Person.LOCALE, Utils.formatLocale(LOCALE3));
		object2.setAttributeValue(Person.LOCALE, Utils.formatLocale(LOCALE3));
		successFullTX.commit();

		assertEquals("Number of dirty objects wrong.", 3, listener1.count);
		assertEquals("Wrong value for object1.", Utils.formatLocale(LOCALE3),
			object1.getAttributeValue(Person.LOCALE));

	}

    /**
     * Do the actual put and remove of a KO.
     */
    protected void doNewDeleteKO () throws Exception {
        KnowledgeBase kBase = KBSetup.getKnowledgeBase();
        // create an object
		Transaction tx = kBase.beginTransaction();
		dummyKO = kBase.createKnowledgeObject(Person.OBJECT_NAME);
        // change KO
		dummyKO.setAttributeValue(Person.NAME_ATTRIBUTE, "Rabarbara");
		tx.commit();
        // remove the object
		Transaction delTx = kBase.beginTransaction();
        kBase.delete(dummyKO);
		delTx.commit();
    }
    
    /**
     * A test KBEventHandler.
     */
	class DummyKBEventListener implements CommitChecker, ModificationListener {

        /** Indicates that changes are vetoed. */
        boolean doVeto;
        
        /** Name for the event listener. */
        String name;
        
		/** The last received {@link UpdateEvent}. */
		UpdateEvent lastVetoEvent;
        
        /** The number of event since last reset. */
        int count;
        
        /** The number of non-veto event since last reset. */
        int countNonVeto;
        
        /**
         * Constructor.
         */
        public DummyKBEventListener (String aName) {
            name = aName;
        }
        
        /**
         * Set veto behaviour.
         * 
         * @param aDoVeto   true, if vetoing.
         */
        public void setVeto(boolean aDoVeto) {
            doVeto = aDoVeto;
        }
        
        /**
         * Get the last received veto event.
         * 
         * @return the last event.
         */
		public UpdateEvent getLastVetoEvent() {
            return lastVetoEvent;
        }         
        
        @Override
		public Modification createModification(KnowledgeBase kb,
				Map<ObjectKey, ? extends KnowledgeItem> createdObjects, Map<ObjectKey, ? extends KnowledgeItem> updatedObjects, Map<ObjectKey, ? extends KnowledgeItem> removedObjects) {
			count += createdObjects.size();
			count += updatedObjects.size();
			count += removedObjects.size();
			return Modification.NONE;
		}

		@Override
		public Modification notifyUpcomingDeletion(KnowledgeBase kb, KnowledgeItem item) {
			// No special handling for deletions.
			return Modification.NONE;
		}

		@Override
		public void checkCommit(UpdateEvent anEvent)
            throws CommitVetoException {
            count++;
            lastVetoEvent = anEvent;
            //System.out.println(name + ": VetoEvent received: " + anEvent.getClass().getName());
            if (doVeto) {
                //System.out.println(" (VETO)");
                throw new CommitVetoException(anEvent, this);
            } else {
                //System.out.println();
            }
        }
        
        /**
         * reset the counts.
         */
        public void resetCounts() {
            count = 0;
            countNonVeto = 0;
        }
    }

    /** 
     * Return the suite of tests to execute.
     */
    public static Test suite() {
    	return KBSetup.getKBTest(TestKBEventHandling.class);
    }
    
    /** 
     * main function for direct testing.
     */
    public static void main(String[] args) {
        Logger.configureStdout();   // "INFO"
        // KBSetup.CREATE_TABLES = false; // spped up debugging
        TestRunner.run(suite());
    }
    
}
