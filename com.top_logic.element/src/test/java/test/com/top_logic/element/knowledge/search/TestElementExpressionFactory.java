/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.knowledge.search;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.element.meta.kbbased.Setup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.knowledge.search.ElementExpressionFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Tests for {@link ElementExpressionFactory}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestElementExpressionFactory extends BasicTestCase {

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

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
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

	private void setList1Value(Wrapper target, List<?> newValue) throws KnowledgeBaseException {
		Transaction tx2 = kb().beginTransaction();
		target.setValue(Setup.LIST_1_ATTR, newValue);
		tx2.commit();
	}

	public void testHasReferenceTo() throws KnowledgeBaseException {
		setList1Value(object, list(v1, v2));
		setList1Value(object2, list(v2, v3));
		setList1Value(object3, list(v3));

		assertEquals(set(object,object2), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), list(v1, v2))),
			Wrapper.class))));
		assertEquals(set(object, object2, object3), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), list(v1, v3))),
			Wrapper.class))));
		assertEquals(set(), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME),
				ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), BasicTestCase.<Wrapper> list())),
			Wrapper.class))));
		assertEquals(set(object), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), v1)),
			Wrapper.class))));
		assertEquals(set(object), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), list(v1))),
			Wrapper.class))));
		assertEquals(set(object, object2), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), v2)),
			Wrapper.class))));
		assertEquals(set(object, object2), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), list(v2))),
			Wrapper.class))));
		assertEquals(set(object3, object2), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), v3)),
			Wrapper.class))));
		assertEquals(set(object3, object2), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceTo(Setup.list1Attr(), list(v3))),
			Wrapper.class))));
	}

	public void testIsReferencedBy() throws KnowledgeBaseException {
		setList1Value(object, list(v1, v2));
		setList1Value(object2, list(v2, v3));
		setList1Value(object3, list(v3));

		assertEquals(set(v1, v2, v3), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME),
				ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), list(object, object2))),
			Wrapper.class))));
		assertEquals(set(v2, v3), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME),
				ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), list(object2, object3))),
			Wrapper.class))));
		assertEquals(set(), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME),
				ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), BasicTestCase.<Wrapper> list())),
			Wrapper.class))));
		assertEquals(set(v1, v2), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), object)),
			Wrapper.class))));
		assertEquals(set(v1, v2), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), list(object))),
			Wrapper.class))));
		assertEquals(set(v2, v3), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), object2)),
			Wrapper.class))));
		assertEquals(set(v2, v3), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), list(object2))),
			Wrapper.class))));
		assertEquals(set(v3), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), object3)),
			Wrapper.class))));
		assertEquals(set(v3), toSet(kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.isReferencedBy(Setup.list1Attr(), list(object3))),
			Wrapper.class))));
	}

	public void testHasReferenceToType() throws KnowledgeBaseException {
		setList1Value(object, list(v1, v2));
		assertEquals(list(object), kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.hasReferenceToType(Setup.list1Attr(), Setup.STRUCTURED_ELEMENT_TABLE_NAME)),
			Wrapper.class)));
	}

	public void testIsReferencedByType() throws KnowledgeBaseException {
		setList1Value(object, list(v1));
		assertEquals(list(v1), kb().search(queryResolved(
			filter(allOf(Setup.STRUCTURED_ELEMENT_TABLE_NAME), ElementExpressionFactory.isReferencedByType(Setup.list1Attr(), Setup.STRUCTURED_ELEMENT_TABLE_NAME)),
			Wrapper.class)));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestElementExpressionFactory}.
	 */
	public static Test suite() {
		return KBSetup.getKBTest(TestElementExpressionFactory.class, new TestFactory() {

			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				return new Setup(new TestSuite(testCase));
			}
		});
	}

}

