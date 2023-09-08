/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Test case for ordered reference {@link TLStructuredTypePart}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestWrapperListMetaAttribute extends BasicTestCase {
	
	private Wrapper object;

	private Wrapper object2;

	private Wrapper object3;
	private Wrapper v1;
	private Wrapper v2;
	private Wrapper v3;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		Transaction tx = kb().beginTransaction();
		object = Setup.newStructuredElementObject("o");
		object2 = Setup.newStructuredElementObject("o2");
		object3 = Setup.newGenericObject("o3");
		v1 = Setup.newStructuredElementObject("v1");
		v2 = Setup.newStructuredElementObject("v2");
		v3 = Setup.newGenericObject("v3");
		tx.commit();
	}
	
	@Override
	protected void tearDown() throws Exception {
		Transaction tx = kb().beginTransaction();
		object.tDelete();
		object2.tDelete();
		object3.tDelete();
		v1.tDelete();
		v2.tDelete();
		v3.tDelete();
		tx.commit();

		super.tearDown();
	}
	
	public void testHasValue() {
		setList1Value(object, list(v1));
		setList2Value(object, list(v2));

		TLStructuredTypePart list1 = MetaElementUtil.getMetaAttribute(Setup.metaElement, Setup.LIST_1_ATTR);
		TLStructuredTypePart list2 = MetaElementUtil.getMetaAttribute(Setup.metaElement, Setup.LIST_2_ATTR);

		assertTrue(WrapperMetaAttributeUtil.hasWrappersWithValue(list1, v1));
		assertFalse(WrapperMetaAttributeUtil.hasWrappersWithValue(list1, v2));
		assertTrue(WrapperMetaAttributeUtil.hasWrappersWithValue(v2));
		assertFalse(WrapperMetaAttributeUtil.hasWrappersWithValue(list1, v3));

		assertTrue(WrapperMetaAttributeUtil.hasWrappersWithValues(v2, set(list1, list2)));
		assertTrue(WrapperMetaAttributeUtil.hasWrappersWithValues(v2, set(list1, list2)));
		assertTrue(WrapperMetaAttributeUtil.hasWrappersWithValues(v2, null));
		assertFalse(WrapperMetaAttributeUtil.hasWrappersWithValues(v3, set(list1, list2)));
	}

	public void testGetByValue() throws KnowledgeBaseException, NoSuchAttributeException {
		setList1Value(object, list(v1));
		setList2Value(object, list(v2));

		setList1Value(object2, list(v1, v2));
		setList2Value(object2, list(v3));

		TLStructuredTypePart list1MA = MetaElementUtil.getMetaAttribute(Setup.metaElement, Setup.LIST_1_ATTR);
		TLStructuredTypePart list2MA = MetaElementUtil.getMetaAttribute(Setup.metaElement, Setup.LIST_2_ATTR);

		assertEquals(set(object, object2), toSet(WrapperMetaAttributeUtil.getWrappersWithValue(list1MA, v1)));
		assertEquals(set(object2), toSet(WrapperMetaAttributeUtil.getWrappersWithValue(list1MA, v2)));
		assertEquals(set(object, object2), toSet(WrapperMetaAttributeUtil.getWrappersWithValue(v2)));
		assertEquals(set(), toSet(WrapperMetaAttributeUtil.getWrappersWithValue(list1MA, v3)));
		assertEquals(set(object2), toSet(WrapperMetaAttributeUtil.getWrappersWithValue(list2MA, v3)));
	}

	public void testSetValue() {
		setList1Value(object, list(v1, v1, v2));
		assertEquals(list(v1, v1, v2), getListValue(object));
	}
	
	public void testLiveList() {
		List<Wrapper> list = list(v1, v3, v2);
		assertFalse("Ticket #24425: Live list must work for non structured elements.", v3 instanceof StructuredElement);
		setList1Value(object, list);
		assertEquals("Ticket #24425: Live list must work for non structured elements.", list, getListValueLive(object));
	}

	public void testReorderContents() {
		setList1Value(object, list(v1, v1, v2));
		assertEquals(list(v1, v1, v2), getListValue(object));
		
		setList1Value(object, list(v1, v2, v1));
		assertEquals(list(v1, v2, v1), getListValue(object));
	}
	
	public void testReorderContentsLive() {
		setList1Value(object, list(v1, v1, v2));
		assertEquals(list(v1, v1, v2), getListValue(object));

		try (Transaction tx = kb().beginTransaction()) {
			List<Object> listValueLive = getListValueLive(object);
			listValueLive.add(listValueLive.remove(1));
			// Check in transaction
			assertEquals(list(v1, v2, v1), getListValue(object));
			tx.commit();
		}
		// Check after commit
		assertEquals(list(v1, v2, v1), getListValue(object));
	}

	public void testRemoveContents() {
		setList1Value(object, list(v1, v2, v1, v3));
		assertEquals(list(v1, v2, v1, v3), getListValue(object));
		
		setList1Value(object, list(v1, v1, v3));
		assertEquals(list(v1, v1, v3), getListValue(object));
	}
	
	public void testRemoveContentsLive() {
		setList1Value(object, list(v1, v2, v1, v3));
		assertEquals(list(v1, v2, v1, v3), getListValue(object));

		try (Transaction tx = kb().beginTransaction()) {
			getListValueLive(object).remove(1);
			// Check in transaction
			assertEquals(list(v1, v1, v3), getListValue(object));
			tx.commit();
		}
		// Check after commit
		assertEquals(list(v1, v1, v3), getListValue(object));
	}

	public void testRemoveDuplicate() {
		setList1Value(object, list(v1, v1, v2, v2));
		assertEquals(list(v1, v1, v2, v2), getListValue(object));

		setList1Value(object, list(v1, v2, v2));
		assertEquals(list(v1, v2, v2), getListValue(object));
	}
	
	public void testAddValue() {
		setList1Value(object, list(v1, v2, v1));
		assertEquals(list(v1, v2, v1), getListValue(object));
		
		addListValue(object, v3);
		assertEquals(list(v1, v2, v1, v3), getListValue(object));
	}

	public void testAddValueLive() {
		setList1Value(object, list(v1, v2, v1));
		assertEquals(list(v1, v2, v1), getListValue(object));

		try (Transaction tx = kb().beginTransaction()) {
			getListValueLive(object).add(v3);
			// Check in transaction
			assertEquals(list(v1, v2, v1, v3), getListValue(object));
			tx.commit();
		}
		// Check after commit
		assertEquals(list(v1, v2, v1, v3), getListValue(object));
	}

	public void testAddExistingValue() {
		setList1Value(object, list(v1, v2, v1));
		assertEquals(list(v1, v2, v1), getListValue(object));
		
		addListValue(object, v1);
		assertEquals(list(v1, v2, v1, v1), getListValue(object));
	}
	
	public void testRemoveValue() {
		setList1Value(object, list(v1, v2, v1));
		assertEquals(list(v1, v2, v1), getListValue(object));
		
		removeListValue(object, v1);
		assertEquals(list(v2, v1), getListValue(object));
	}
	
	public void testChangeContents() {
		setList1Value(object, list(v1, v1, v2));
		assertEquals(list(v1, v1, v2), getListValue(object));

		setList1Value(object, list(v1, v2, v3, v3, v2, v1));
		assertEquals(list(v1, v2, v3, v3, v2, v1), getListValue(object));
	}
	
	public void testClearContents() {
		setList1Value(object, list(v1, v1, v2));
		assertEquals(list(v1, v1, v2), getListValue(object));

		setList1Value(object, list());
		assertEquals(list(), getListValue(object));
	}

	public void testClearContentsLive() {
		setList1Value(object, list(v1, v1, v2));
		assertEquals(list(v1, v1, v2), getListValue(object));

		try (Transaction tx = kb().beginTransaction()) {
			getListValueLive(object).clear();
			// Check in transaction
			assertEquals(list(), getListValue(object));
			tx.commit();
		}
		// Check after commit
		assertEquals(list(), getListValue(object));
	}

	public void testUnchangedEmpty() {
		setList1Value(object, list());
		assertEquals(list(), getListValue(object));
	}
	
	public void testUnchangedNonEmpty() {
		setList1Value(object, list(v1, v1, v2));
		assertEquals(list(v1, v1, v2), getListValue(object));

		setList1Value(object, list(v1, v1, v2));
		assertEquals(list(v1, v1, v2), getListValue(object));
	}
	
	private Object getListValue(Wrapper target) {
		return target.getValue(Setup.LIST_1_ATTR);
	}

	private List<Object> getListValueLive(Wrapper target) {
		return (List<Object>) WrapperMetaAttributeUtil.getLiveCollection(target,
			target.tType().getPart(Setup.LIST_1_ATTR));
	}

	private void addListValue(Wrapper target, Wrapper value) {
		try (Transaction tx2 = kb().beginTransaction()) {
			AttributeOperations.addValue(target, Setup.LIST_1_ATTR, value);
			tx2.commit();
		}
	}
	
	private void removeListValue(Wrapper target, Wrapper value) throws KnowledgeBaseException {
		try (Transaction tx2 = kb().beginTransaction()) {
			AttributeUtil.removeValue(target, Setup.LIST_1_ATTR, value);
			tx2.commit();
		}
	}
	
	private void setList1Value(Wrapper target, List<?> newValue) throws KnowledgeBaseException {
		try (Transaction tx2 = kb().beginTransaction()) {
			target.setValue(Setup.LIST_1_ATTR, newValue);
			tx2.commit();
		}
	}

	private void setList2Value(Wrapper target, List<?> newValue) throws KnowledgeBaseException {
		try (Transaction tx2 = kb().beginTransaction()) {
			target.setValue(Setup.LIST_2_ATTR, newValue);
			tx2.commit();
		}
	}

	static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}
	
	public static Test suite() {
		TestFactory f = new TestFactory() {

			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				return new Setup(new TestSuite(testCase));
			}
		};
		f = ServiceTestSetup.createStarterFactory(WrapperMetaAttributeUtil.Module.INSTANCE, f);
		return KBSetup.getKBTest(TestWrapperListMetaAttribute.class, f);
	}
	
}
