/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.knowledge.dummy.DummyKnowledgeObject;
import test.com.top_logic.knowledge.service.AbstractKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * The TestAbstractWrapper tests the compare, equals and hashcode methods.
 * 
 * @author  Thomas Dickhut
 */
public class TestAbstractWrapper extends AbstractKnowledgeBaseTest {

	public void testEquals() throws Exception {
		KnowledgeObject item = DummyKnowledgeObject.item("id");
		Wrapper wrapper = new TestWrapper("name", item);
		Wrapper wrapperClone = new TestWrapper("name", item);
		Wrapper wrapperOtherName = new TestWrapper("otherName", item);
        
        // Equal wrappers
        assertEquals(wrapper, wrapperClone);
        assertEquals(wrapper.hashCode(), wrapperClone.hashCode());
        
        assertEquals(wrapper, wrapperOtherName);
        assertEquals(wrapper.hashCode(), wrapperOtherName.hashCode());
        
        // Unequal wrappers
		Wrapper wrapperWrong = new TestWrapper("name", DummyKnowledgeObject.item("otherId"));
        assertFalse(wrapper.equals(wrapperWrong));
        assertFalse(wrapperWrong.equals(wrapper));
        assertFalse(wrapper.equals(null));
        assertFalse(wrapperWrong.equals(null));
    }
    
    /**
     * Test {@link AbstractWrapper#compareTo(Wrapper)}.
     */
    public void testComparable() throws Exception {
		Wrapper wrapper1 = new TestWrapper("111", DummyKnowledgeObject.item("id1"));
		Wrapper wrapper11 = new TestWrapper("111", DummyKnowledgeObject.item("id1"));
		Wrapper wrappera = new TestWrapper("aaa", DummyKnowledgeObject.item("ida"));
		Wrapper wrapperA = new TestWrapper("AAA", DummyKnowledgeObject.item("idA"));

		Wrapper wrapperNN1 = new TestWrapper(null, DummyKnowledgeObject.item("idNN1"));
		Wrapper wrapperNN2 = new TestWrapper(null, DummyKnowledgeObject.item("idNN2"));

		Wrapper wrapperI1 = new TestWrapper("Invalid1", deleted("Invalid1"));
		Wrapper wrapperI2 = new TestWrapper("Invalid2", deleted("Invalid2"));
        
        assertEquals(0, wrapper1  .compareTo(wrapper1));
        assertEquals(0, wrapper1  .compareTo(wrapper11));
        assertEquals(0, wrapperNN1.compareTo(wrapperNN1));
        assertEquals(0, wrapperI1.  compareTo(wrapperI1));
        assertEquals(0, wrapperI1.  compareTo(wrapperI2));
        assertEquals(0, wrapperI2.  compareTo(wrapperI1));

        assertTrue(wrapperNN1.compareTo(wrapperNN2) < 0); // as "idNN1" < "idNN2"
        assertTrue(wrapperNN2.compareTo(wrapperNN1) > 0); // as "idNN2" > "idNN1"

        assertTrue(wrapperNN1.compareTo(wrapperA)   > 0);
        assertTrue(wrapperI2 .compareTo(wrapperA)   > 0);
        
        assertTrue(wrapperA.compareTo(wrapperNN2)   < 0);
        assertTrue(wrapperA.compareTo(wrapperI1)    < 0);

        assertTrue(wrapperA.compareTo(wrappera) < 0);
        assertTrue(wrappera.compareTo(wrapperA) > 0);
    }

	private KnowledgeObject deleted(String id) {
		DummyKnowledgeObject result = DummyKnowledgeObject.item(id);
		result.delete();
		return result;
	}

	/**
	 * Test {@link AbstractWrapper#compareTo(Wrapper)} via {@link Collections#sort(List)}
	 */
    public void testComparableSort() throws Exception {
		Wrapper wrapper1 = new TestWrapper("111", DummyKnowledgeObject.item("id1"));
		Wrapper wrapper2 = new TestWrapper("AAA", DummyKnowledgeObject.item("idA"));
		Wrapper wrapper3 = new TestWrapper("aaa", DummyKnowledgeObject.item("ida"));
		Wrapper wrapper4 = new TestWrapper("bbb", DummyKnowledgeObject.item("idb"));
		Wrapper wrapper5 = new TestWrapper("ccc", DummyKnowledgeObject.item("idc"));
		Wrapper wrapper6 = new TestWrapper("ddd", DummyKnowledgeObject.item("idd"));
		Wrapper wrapper7 = new TestWrapper("‹‹‹", DummyKnowledgeObject.item("id‹"));
		Wrapper wrapper8 = new TestWrapper("ﬂﬂﬂ", DummyKnowledgeObject.item("idﬂ"));
        
        List<Wrapper> list = new ArrayList<>(8);
        list.add(wrapper8);
        list.add(wrapper3);
        list.add(wrapper2);
        list.add(wrapper4);
        list.add(wrapper6);
        list.add(wrapper1);
        list.add(wrapper5);
        list.add(wrapper7);
        
        Random rand = new Random(767676767L);
        for (int j=0; j < 10; j++) {
            Collections.sort(list);
            
            int i = 0;
            assertEquals(wrapper1, list.get(i++));
            assertEquals(wrapper2, list.get(i++));
            assertEquals(wrapper3, list.get(i++));
            assertEquals(wrapper4, list.get(i++));
            assertEquals(wrapper5, list.get(i++));
            assertEquals(wrapper6, list.get(i++));
            assertEquals(wrapper7, list.get(i++));
            assertEquals(wrapper8, list.get(i++));
            Collections.shuffle(list,rand);
        }
    }
    
	public void testModifier() throws Exception {
		if (!kb().getHistoryManager().hasHistory()) {
			// This test test lifecycle attributes which only works with history.
			return;
		}
		PersonManager pm = PersonManager.getManager();
		final Person p1;
		{
			Transaction tx = begin();
			p1 = createPerson(pm, "p1");
			tx.commit();
		}

		final ObjectFlag<TestBWrapper> wrapper = new ObjectFlag<>(null);
		final ObjectFlag<Revision> createRev = new ObjectFlag<>(null);

		// P1 creates object
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				TLContextManager.getSubSession().setPerson(p1);

				Transaction tx = begin();
				KnowledgeObject o1 = kb().createKnowledgeObject(KBTestMeta.TEST_B);
				wrapper.set(new TestBWrapper(o1));
				tx.commit();
				createRev.set(tx.getCommitRevision());
			}
		});

		// make change visible in this session
		updateSessionRevision();

		// Create person <b>after</p> creation of object
		final Person p2;
		{
			Transaction tx = begin();
			p2 = createPerson(pm, "p2");
			tx.commit();
		}
		final ObjectFlag<Revision> changeRev = new ObjectFlag<>(null);

		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				TLContextManager.getSubSession().setPerson(p2);

				Transaction tx = begin();
				wrapper.get().setValue(KBTestMeta.TEST_B_NAME, "change");
				tx.commit();
				changeRev.set(tx.getCommitRevision());
			}
		});

		// make change visible in this session
		updateSessionRevision();

		Wrapper p1InCreate = WrapperHistoryUtils.getWrapper(createRev.get(), p1);
		Wrapper p2InChange = WrapperHistoryUtils.getWrapper(changeRev.get(), p2);

		assertEquals(p1InCreate, wrapper.get().getCreator());
		assertNotNull("Ticket #18489: Modifier must not be fetched in create revision.", wrapper.get().getModifier());
		assertEquals(p2InChange, wrapper.get().getModifier());

		// Delete the test accounts.
		{
			Transaction tx = begin();
			p1.tDelete();
			p2.tDelete();
			tx.commit();
		}
		assertEquals(p1InCreate, wrapper.get().getCreator());
		assertEquals(p2InChange, wrapper.get().getModifier());
	}

	public void testLifeCycle() {
        final Person p1;
        final Person p2;
        
        // Create two test persons.
		PersonManager pm = PersonManager.getManager();
		{
			Transaction tx = begin();
			p1 = createPerson(pm, "p1");
			p2 = createPerson(pm, "p2");
			tx.commit();
		}
		
		final TestBWrapper[] w1 = { null };
		final Revision[] r1 = {null};
		
		// Create object on behalf of p1:
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				TLContext.getContext().setCurrentPerson(p1);
				
				// On behalf of p1.
				Transaction tx = begin();
				{
					KnowledgeObject o1 = kb().createKnowledgeObject(KBTestMeta.TEST_B);
					w1[0] = new TestBWrapper(o1);
				}
				tx.commit();
				
				r1[0] = tx.getCommitRevision();
			}
		});
		assertFalse("Session has an old revision. Objects created in other session must not be visible.",
			w1[0].tValid());
		updateSessionRevision();
		assertTrue(w1[0].tValid());
		
		ObjectReference p1Id = WrapperHistoryUtils.getUnversionedIdentity(p1);
		ObjectReference p2Id = WrapperHistoryUtils.getUnversionedIdentity(p2);
		
		// Test that creator and modifier are equivalent to p1.
		assertEquals(p1Id, WrapperHistoryUtils.getUnversionedIdentity(w1[0].getCreator()));
		assertEquals(p1Id, WrapperHistoryUtils.getUnversionedIdentity(w1[0].getModifier()));
		assertTrue(w1[0].getCreator().isAlive());
		assertTrue(w1[0].getModifier().isAlive());
		
		// Modify object on behalf of p1:
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				TLContext.getContext().setCurrentPerson(p2);
				
				// On behalf of p2.
				Transaction tx = begin();
				{
					w1[0].setInteger(KBTestMeta.TEST_B_INT, Integer.valueOf(42));
				}
				tx.commit();
			}
		});

		// Test that modifier has changed to p2 after update.
		assertEquals("Must not see future changes.", p1Id,
			WrapperHistoryUtils.getUnversionedIdentity(w1[0].getModifier()));
		updateSessionRevision();
		assertEquals(p1Id, WrapperHistoryUtils.getUnversionedIdentity(w1[0].getCreator()));
		assertEquals(p2Id, WrapperHistoryUtils.getUnversionedIdentity(w1[0].getModifier()));
		assertTrue(w1[0].getCreator().isAlive());
		assertTrue(w1[0].getModifier().isAlive());
		
		// Test that creator and modifier in historic version are still both p1.
		boolean versioningSupport = kb().getHistoryManager().hasHistory();
		if (versioningSupport) {
			Wrapper w1R1 = WrapperHistoryUtils.getWrapper(r1[0], w1[0]);
			assertEquals(p1Id, WrapperHistoryUtils.getUnversionedIdentity(w1R1.getCreator()));
			assertEquals(p1Id, WrapperHistoryUtils.getUnversionedIdentity(w1R1.getModifier()));
			assertTrue(w1[0].getCreator().isAlive());
			assertTrue(w1[0].getModifier().isAlive());
		}
		
		// Delete the test accounts.
		{
			Transaction tx = begin();
			p1.tDelete();
			p2.tDelete();
			tx.commit();
		}
		
		// Test that the accounts are really gone.
		assertFalse(p1.tValid());
		assertFalse(p2.tValid());
		
		if (versioningSupport) {
			// Test that creator and modifier do not change after deleting the accounts.
			assertEquals(p1Id, WrapperHistoryUtils.getUnversionedIdentity(w1[0].getCreator()));
			assertEquals(p2Id, WrapperHistoryUtils.getUnversionedIdentity(w1[0].getModifier()));
			// Test that no non-alive people are reported.
			assertTrue(w1[0].getCreator().tValid());
			assertTrue(w1[0].getModifier().tValid());
		} else {
			assertEquals(null, w1[0].getCreator());
			assertEquals(null, w1[0].getModifier());
		}
    }

	private Person createPerson(PersonManager pm, String personName) {
		TLSecurityDeviceManager sdm = TLSecurityDeviceManager.getInstance();
		String authenticationDeviceID = sdm.getDefaultAuthenticationDevice().getDeviceID();
		return Person.create(PersistencyLayer.getKnowledgeBase(), personName, authenticationDeviceID);
	}

    /**
     * The method constructing a test suite for this class.
     * 
     * @return The test to be executed.
     */
    public static Test suite () {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestAbstractWrapper.class));
    }
    
    /** 
     * This method runs the tests.
     */
    public static void main(String[] args) {
        SHOW_TIME             = false;
        
        Logger.configureStdout(); // "INFO"
        
        TestRunner.run (suite ());
    }

    private class TestWrapper extends AbstractWrapper {

        private String name;
        
        /** Creates a {@link TestWrapper}. */
		public TestWrapper(String aName, KnowledgeObject ko) {
			super(ko);
            
            this.name = aName;
        }
        
        @Override
		public String getName() throws WrapperRuntimeException {
            return this.name;
        }
        
    }
    
}

