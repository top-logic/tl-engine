/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
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

import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * The class {@link TestWrapperMetaAttributeUtil} tests
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestWrapperMetaAttributeUtil extends BasicTestCase {

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
		object3 = Setup.newStructuredElementObject("o3");
		v1 = Setup.newStructuredElementObject("v1");
		v2 = Setup.newStructuredElementObject("v2");
		v3 = Setup.newStructuredElementObject("v3");
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

	static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	public void testGetWrappersWithValue() throws KnowledgeBaseException {
		setList1Value(object2, list(v2, v3));
		setList2Value(object, list(v1, v2));
		
		assertEquals(list(object2), WrapperMetaAttributeUtil.getWrappersWithValue(Setup.list1Attr(), v2));
		assertEquals(list(object), WrapperMetaAttributeUtil.getWrappersWithValue(Setup.list2Attr(), v2));
		assertEquals(set(object, object2), toSet(WrapperMetaAttributeUtil.getWrappersWithValue(v2)));
		
		assertEquals(list(object), WrapperMetaAttributeUtil.getWrappersWithValue(
			Setup.list2Attr(), Setup.LIST_2_ATTR_TABLE, v2));
		assertEquals(Setup.LIST_1_ATTR + " does not store in " + Setup.LIST_2_ATTR_TABLE, list(),
			WrapperMetaAttributeUtil.getWrappersWithValue(Setup.list1Attr(), Setup.LIST_2_ATTR_TABLE, v2));

		setList1Value(object3, list(v1, v3));
		setList1Value(object, list(v2, v3));
		assertEquals(set(object, object2, object3),
			toSet(WrapperMetaAttributeUtil.getWrappersWithValue(Setup.list1Attr(), v3)));
		assertEquals(list(), WrapperMetaAttributeUtil.getWrappersWithValue(Setup.list2Attr(), v3));

	}

	public void testhasWrappersWithValue() throws KnowledgeBaseException {
		setList1Value(object2, list(v2, v3));
		setList2Value(object, list(v1, v2));

		assertTrue(WrapperMetaAttributeUtil.hasWrappersWithValue(Setup.list1Attr(), v2));
		assertTrue(WrapperMetaAttributeUtil.hasWrappersWithValue(Setup.list2Attr(), v2));
		assertFalse(WrapperMetaAttributeUtil.hasWrappersWithValue(Setup.list1Attr(), v1));
		assertFalse(WrapperMetaAttributeUtil.hasWrappersWithValue(Setup.list2Attr(), v3));

		assertTrue(WrapperMetaAttributeUtil.hasWrappersWithValue(Setup.list2Attr(), Setup.LIST_2_ATTR_TABLE, v2));
		assertFalse(Setup.LIST_1_ATTR + " does not store in " + Setup.LIST_2_ATTR_TABLE,
			WrapperMetaAttributeUtil.hasWrappersWithValue(Setup.list1Attr(), Setup.LIST_2_ATTR_TABLE, v2));
	}

	private void setList1Value(Wrapper target, List<?> newValue) throws KnowledgeBaseException {
		Transaction tx2 = kb().beginTransaction();
		target.setValue(Setup.LIST_1_ATTR, newValue);
		tx2.commit();
	}

	private void setList2Value(Wrapper target, List<?> newValue) throws KnowledgeBaseException {
		Transaction tx2 = kb().beginTransaction();
		target.setValue(Setup.LIST_2_ATTR, newValue);
		tx2.commit();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestWrapperMetaAttributeUtil}.
	 */
	public static Test suite() {
		TestFactory f = new TestFactory() {

			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				return new Setup(new TestSuite(testCase));
			}
		};
		f = ServiceTestSetup.createStarterFactory(WrapperMetaAttributeUtil.Module.INSTANCE, f);
		return KBSetup.getKBTest(TestWrapperMetaAttributeUtil.class, f);
	}
}
