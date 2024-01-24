/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link FastList} and {@link FastListElement}
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestFastList extends BasicTestCase {


	/** Name of a system {@link FastList} available for tests. See "top-logic.test.xml" */
	public static final String SYSTEM_LIST_NAME = "tl.systemList";

	/** Name of the first {@link FastListElement} in {@link #SYSTEM_LIST_NAME} */
	public static final String SYSTEM_LIST_ELEMENT_1 = "tl.systemList.1";

	/** Name of the second {@link FastListElement} in {@link #SYSTEM_LIST_NAME} */
	public static final String SYSTEM_LIST_ELEMENT_2 = "tl.systemList.2";

	/** Name of the third {@link FastListElement} in {@link #SYSTEM_LIST_NAME} */
	public static final String SYSTEM_LIST_ELEMENT_3 = "tl.systemList.3";

	private static final Comparator<FastListElement> INDEX_ORDER = new Comparator<>() {
		@Override
		public int compare(FastListElement e1, FastListElement e2) {
			return CollectionUtil.compareInt(e1.getIndex(), e2.getIndex());
		}
	};

	private Collection<FastList> _scheduledForDeletion;

    /**
     * Creat a new TestFastList tescase.
     * 
     * @param name fucntion to execute for testing.
     */
    public TestFastList(String name) {
        super(name);
    }

	/**
	 * Tests that the configured "system list" is indeed a system list.
	 */
	public void testSystemList() {
		FastList systemList = FastList.getFastList(SYSTEM_LIST_NAME);
		assertNotNull("System list '" + SYSTEM_LIST_NAME + "' not configured.", systemList);
		assertTrue("System list '" + systemList + "' is not system", systemList.isSystem());

		List<String> elementNames = new ArrayList<>();
		for (FastListElement element : systemList.elements()) {
			elementNames.add(element.getName());
		}
		assertEquals(list(SYSTEM_LIST_ELEMENT_1, SYSTEM_LIST_ELEMENT_2, SYSTEM_LIST_ELEMENT_3), elementNames);
	}

	/**
	 * Tests whether adding an element to a list is reverted during rollback.
	 */
    public void testRollbackNewElement() {
        KnowledgeBase kBase = kb();
		FastList list = newList("TestRollback", null, "testRollback", !FastList.MULTI_SELECT);
    	
        FastListElement e1 = list.addElement(null, "A", "A element", 0);

        assertEquals(1, list.size());
        assertTrue(list.elements().contains(e1));
        assertNotNull(list.getElementByName("A"));
        
        commit(kBase);
        
        assertEquals(1, list.size());
        assertTrue(list.elements().contains(e1));
        assertNotNull(list.getElementByName("A"));
        
        FastListElement e2 = list.addElement(null, "B", "B element", 0);
        assertEquals(2, list.size());
        assertTrue(list.elements().contains(e2));
        assertNotNull(list.getElementByName("B"));
        
        rollback(kBase);
        
		assertEquals("Ticket #9029: Element creation not reverted.", 1, list.size());
		assertFalse("Ticket #9029: Element creation not reverted.", list.elements().contains(e2));
		assertNull("Ticket #9029: Element creation not reverted.", list.getElementByName("C"));
    }

	protected void rollback(KnowledgeBase kBase) {
		assertTrue("Rollback failed.", kBase.rollback());
	}
    
    /** Test some trivial things that should never break :-(  
     */
    public void testSingelSelect() {
        KnowledgeBase kBase = kb();
        FastList list = newList("TestFastList", "Long descripotion of TESTING nobody cares for", "TESTING", !FastList.MULTI_SELECT);
        assertEquals("TestFastList", list.getName());
        assertEquals("Long descripotion of TESTING nobody cares for", list.getDescription());
        assertEquals("TESTING"     , list.getClassificationType());
        assertFalse (list.isMultiSelect());
        assertNotNull(list.toString());
        assertEquals(0             , list.size());
        
        List<FastList> allLists = FastList.getListsForType(kBase, "TESTING");
        assertEquals(1, allLists.size());
        assertSame(list, allLists.get(0));
    }

	private FastList newList(String name, String description, String listType, boolean isMulti) {
		KnowledgeBase kBase = kb();
		FastList newList = FastList.createEnum(kBase, name, description, listType, isMulti);
		scheduleForDeletion(newList);
		return newList;
	}
    
    public void testMultiSelect() throws Exception {
        KnowledgeBase kBase = kb();
        Transaction t = kBase.beginTransaction();
        
		FastList list = newList("Some Name", null, "TESTING", FastList.MULTI_SELECT);
        assertEquals("Some Name"   , list.getName());
        assertNull  (                list.getDescription());
        assertEquals("TESTING"     , list.getClassificationType());
        assertTrue  (list.isMultiSelect());
        assertNotNull(list.toString());
		list.tDelete();
        t.commit();
        assertNotNull(list.toString()); // Check toString on invalid object
    }

    protected void commit(KnowledgeBase kBase) {
		assertTrue("Commit failed.", kBase.commit());
	}
    
    /** Tests focussed on the FastListElement */
    public void testElement() {
        KnowledgeBase kBase = kb();
		FastList list = newList("ElendElem", null, "TESTING", FastList.MULTI_SELECT);
        
        assertEquals(0, list.size());
		FastListElement elem = list.addElement(null, "A", "Description for A", 0);
		final TLID id = KBUtils.getWrappedObjectName(elem);
        
        assertEquals("Description for A"     , elem.getDescription());
        elem.setDescription("Other descrition for A");
        assertEquals("Other descrition for A", elem.getDescription());
        commit(kBase);
        assertEquals("Other descrition for A", elem.getDescription());
        
        KnowledgeObject ko = elem.tHandle();
        assertTrue(elem.tValid());
        assertNotNull(elem.toString());
        
        // Reload after element invalidation.
		list = FastList.getFastList("ElendElem");
        
        elem = list.elements().get(0);
        assertSame(ko, elem.tHandle());

        assertEquals(0,  elem.getFlags());
        elem.setFlags(99);
        assertEquals(99, elem.getFlags());
        commit(kBase);
        assertEquals(99, elem.getFlags());
        
        assertTrue (elem.isChecked());
        elem.setChecked(false);
        assertFalse(elem.isChecked());
        commit(kBase);
        assertFalse(elem.isChecked());

        elem.setChecked(true);
        assertTrue(elem.isChecked());
        commit(kBase);
        assertTrue(elem.isChecked());
        
        FastListElement elemX = FastListElement.getInstance(id, kBase);
        assertSame(elem, elemX);

        elem = FastListElement.getInstance(id, kBase);
        assertTrue(elem.tValid());
        list = elem.getList();
        assertTrue(list.tValid());
		assertEquals("ElendElem", list.getName());
    }

    public void testPrevNext() throws Exception {
    	KnowledgeBase kBase = kb();
		FastList fastList = newList("ElendElem", null, "TESTING", FastList.MULTI_SELECT);
        
        FastListElement elementA = fastList.addElement(null, "A", "Description for A", 0);
        FastListElement elementB = fastList.addElement(null, "B", "Description for B", 0);
        FastListElement elementC = fastList.addElement(null, "C", "Description for C", 0);
        
        commit(kBase);
        
        assertEquals(elementA, fastList.getElementByName("A"));
        assertEquals(elementB, fastList.getElementByName("B"));
        assertEquals(elementC, fastList.getElementByName("C"));
        
        assertEquals(elementA.getNext(), elementB);
        assertEquals(elementB.getNext(), elementC);
        assertEquals(elementC.getNext(), null);
        
        assertEquals(elementA.getPrev(), null);
        assertEquals(elementB.getPrev(), elementA);
        assertEquals(elementC.getPrev(), elementB);
    }
    
    /** Tests Aggreations of th Element Flags */
    public void testFlags() {
        KnowledgeBase kBase = kb();
		FastList list = newList("ElendElem", null, "TESTING", FastList.MULTI_SELECT);
        
        assertEquals( 0, list.size());
        assertEquals( 0, list.getFlagsORed());
        assertEquals(-1, list.getFlagsANDed());
        assertTrue  (list.isAllChecked());
        
		list.addElement(null, "2", "Description for 2", 0x02);
		list.addElement(null, "4", "Description for 4", 0x04);
		list.addElement(null, "8", "Description for 8", 0x08);

        assertEquals(0x0E, list.getFlagsORed());
        assertEquals(0   , list.getFlagsANDed());
        assertFalse (list.isAllChecked());  // since FastListElement.CHECKED_BIT is 0x01
    }

    /** Test sorting of the Lists */
    public void testSorting() {
        KnowledgeBase kBase = kb();
		FastList list = newList("Sorting", null, "TESTING", !FastList.MULTI_SELECT);
        
        assertEquals(0, list.size());
        FastListElement elem1 = list.addElement(null, "A", null, 0);
        FastListElement elem2 = list.addElement(null, "B", null, 0);
        FastListElement elem3 = list.addElement(null, "C", null, 0);
        FastListElement elem4 = list.addElement(null, "D", null, 40);
        
        try {
            list.addElement(null, "D", null, 77);
            fail("Expected IllegalArgumentException (duplicate name)");
        } catch (IllegalArgumentException expected) { /* expected */ }
        
		assertSorted(list.elements(), INDEX_ORDER);
        assertEquals("A", elem1.getName());
        assertEquals("B", elem2.getName());
        assertEquals("C", elem3.getName());
        assertEquals("D", elem4.getName());
        
        elem1.setName("B");
        elem2.setName("A");
        commit(kBase);
		assertSorted(list.elements(), INDEX_ORDER);
        
        elem1.setName("F");
        elem2.setName("E");
        commit(kBase);
		assertSorted(list.elements(), INDEX_ORDER);
    }
    
    /** Test default element functionality */
    public void testDefault() throws Exception {
        KnowledgeBase kBase = kb();
		FastList list = newList("default", null, "TESTING", !FastList.MULTI_SELECT);
		FastList list2 = newList("other", null, "TESTING", !FastList.MULTI_SELECT);
        
        assertEquals(0, list.size());
        FastListElement elem1 =
            list.addElement(null, "Alice"       , "Description for Alice"       , 0);
        FastListElement elem2 = 
            list.addElement(null, "Bob"         , "Description for Bob"         , 0);
        FastListElement elem3 = 
            list.addElement(null, "Christopher" , "Description for Christopher" , 0);
        FastListElement elem4 = 
            list.addElement(null, "Daniel"      , "Description for Daniel"      , FastListElement.CHECKED_BIT);
        
        FastListElement elemOther = 
            list2.addElement(null, "X'abuh"     , "Description for X'abuh"      , 0);

        assertEquals(4, list.size());

		elem2.setDefault(true);
		assertSame(elem2, list.getDefaultElement());
		assertTrue(elem2.isDefault());
		elem4.setDefault(true);
		assertSame(elem4, list.getDefaultElement());
		assertTrue(elem4.isDefault());
		assertFalse(elem2.isDefault());
		elem2.setDefault(false);
		assertTrue(elem4.isDefault());
		assertFalse(elem2.isDefault());
		elem4.setDefault(false);
		assertNull(list.getDefaultElement());

        assertNull(       list.getDefaultElement());
                          list.setDefaultElement(elem3);
        commit(kBase);
        assertSame(elem3, list.getDefaultElement());
        
        KnowledgeObject baseKO = list.tHandle();

        // Reload after element invalidation.
        list = FastList.getFastList(baseKO);
        elem1 = list.getElementByName("Alice");
        elem2 = list.getElementByName("Bob");
        elem4 = list.getElementByName("Daniel");
        assertNotNull(elem1);
        assertNotNull(elem2);
        assertNotNull(elem4);

        // Break internal State to check for Correct handling
		baseKO.setAttributeValue(FastList.DEFAULT_ATTRIBUTE, elem3.tHandle());
		assertEquals("Ticket #9029: Cached default element is wrong.", elem3, list.getDefaultElement());
        
                          list.setDefaultElement(null);
        assertNull(       list.getDefaultElement());
                          list.setDefaultElement(elem2);
        assertEquals(elem2, list.getDefaultElement());
        list.removeElement(elem2); // will reset Default to null.
        assertNull(       list.getDefaultElement());
        
        assertTrue (elem4.isChecked());
        assertFalse(elem1.isChecked());
        assertEquals(3, list.size());
        
        try {
            list.setDefaultElement(elemOther);
            fail("Expected IllegalArgumentException here");
        }
        catch (IllegalArgumentException expected) { /* expected */ }
    }

    /** Test deletion / removal of Elements */
    public void testDelete() {
		FastList list =
			newList("delete", "This List will not have a long live me thinks", "TESTING", !FastList.MULTI_SELECT);
        
        FastListElement elemA = list.addElement(null, "A", null, 0);
        FastListElement elemB = list.addElement(null, "B", null, 0);
        FastListElement elemC = list.addElement(null, "C", null, 0);
        FastListElement elemD = list.addElement(null, "D", null, 40);

        assertTrue(list.elements().contains(elemA));
        assertTrue(list.elements().contains(elemB));
        assertTrue(list.elements().contains(elemC));
        assertTrue(list.elements().contains(elemD));
    
		elemB.tDelete();
        assertFalse(list.elements().contains(elemB));
		elemD.tDelete();
        assertFalse(list.elements().contains(elemD));
        // By removing the element from the list, the removed literal is
		// deleted in the knowledge base.
        list.removeElement(elemC);
        assertEquals(1, list.size());   // elemB is still there
        assertFalse(elemC.tValid());
        assertFalse(list.elements().contains(elemC));
        // See above. 
        // elemC.deleteKnowledgeObject();
        list.removeElement(elemC); // Should be Noop (with a warning perhaps)
        assertFalse(list.elements().contains(elemC));
        // See above.
        // assertFalse(elemC.deleteKnowledgeObject());
    }
    
    @Override
    protected void setUp() throws Exception {
    	super.setUp();
		_scheduledForDeletion = new ArrayList<>();
    }

	@Override
	protected void tearDown() throws Exception {
		cleanupKB();
		super.tearDown();
	}

	private void cleanupKB() {
		Transaction t = kb().beginTransaction();
		for (FastList list : _scheduledForDeletion) {
			if (!list.tValid()) {
				continue;
			}
			list.tDelete();
		}
        t.commit();
	}

	private void scheduleForDeletion(FastList l) {
		_scheduledForDeletion.add(l);
	}
 
    /**
	 * Check that two FastLists with the same name cannot be created.
	 */
    public void testDuplicateNames() throws Exception {
		KnowledgeBase kb = kb();
		Transaction tx = kb.beginTransaction();
		String name = "testDuplicateNames";
		newList(name, null, "TESTING", !FastList.MULTI_SELECT);
		try {
			newList(name, null, "TESTING", !FastList.MULTI_SELECT);
			fail("A fast list with name " + name + " already exists.");
		} catch (IllegalArgumentException ex) {
			// expected
		} finally {
			tx.rollback();
		}
    }

	/**
	 * Tests the classification type of a fast list.
	 */
	public void testClassificationType() {
		FastList fastList = FastList.getFastList("TestFastList_1");
		assertNotNull(fastList);
		assertEquals("MyFunnySpecialType", fastList.getClassificationType());
		assertTrue(FastList.getListsForType("MyFunnySpecialType").contains(fastList));
	}

	private static KnowledgeBase kb() {
		return KBSetup.getKnowledgeBase();
	}

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		TestFactory f = new TestFactory() {
    		@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				Test innerTest = DefaultTestFactory.INSTANCE.createSuite(testCase, suiteName);
				return PersonManagerSetup.createPurePersonManagerSetup(
					ServiceTestSetup.createSetup(innerTest, ModelService.Module.INSTANCE));
    		}
    	};
		return KBSetup.getSingleKBTest(TestFastList.class, f);
    }

}
