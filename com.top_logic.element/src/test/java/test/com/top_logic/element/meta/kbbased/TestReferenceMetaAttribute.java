/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.meta.OrderedListHelper;
import test.com.top_logic.element.structured.model.A;
import test.com.top_logic.element.structured.model.ANode;
import test.com.top_logic.element.structured.model.BNode;
import test.com.top_logic.element.structured.model.CNode;
import test.com.top_logic.element.structured.model.TestTypesFactory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.core.TlCoreFactory;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * The class {@link TestReferenceMetaAttribute} tests reference meta attributes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestReferenceMetaAttribute extends BasicTestCase {

	public void testValueForMultipleOrderedAttribute() {
		testValueForMultipleOrderedAttribute(TransientObjectFactory.INSTANCE);
		testValueForMultipleOrderedAttribute(TestTypesFactory.getInstance());
	}

	private void testValueForMultipleOrderedAttribute(TLFactory factory) {
		TLObject b0 = factory.createObject(TestTypesFactory.getBNodeType());
		TLObject b1 = factory.createObject(TestTypesFactory.getBNodeType());
		TLObject b2 = factory.createObject(TestTypesFactory.getBNodeType());

		TLReference attr = TestTypesFactory.getTypedSetOrderedBAttr();
		b0.tUpdate(attr, list(b1, b2));
		assertEquals(list(b1, b2), b0.tValue(attr));

		// test fuzzy compatible type

		try {
			b0.tUpdate(attr, b2);
			fail("Non collection value for multiple attribute");
		} catch (RuntimeException ex) {
			// expected.
		}

		b0.tUpdate(attr, set(b1, b2));
		assertInstanceof(b0.tValue(attr), List.class);
		assertEquals(set(b1, b2), toSet((List<?>) b0.tValue(attr)));

		b0.tUpdate(attr, null);
		assertEquals(list(), b0.tValue(attr));
	}

	public void testValueForMultipleUnorderedAttribute() {
		testValueForMultipleUnorderedAttribute(TransientObjectFactory.INSTANCE);
		testValueForMultipleUnorderedAttribute(TestTypesFactory.getInstance());
	}

	private void testValueForMultipleUnorderedAttribute(TLFactory factory) {
		TLObject b0 = factory.createObject(TestTypesFactory.getBNodeType());
		TLObject b1 = factory.createObject(TestTypesFactory.getBNodeType());
		TLObject b2 = factory.createObject(TestTypesFactory.getBNodeType());

		TLReference attr = TestTypesFactory.getTypedSetUnorderedBAttr();
		b0.tUpdate(attr, set(b1, b2));
		assertEquals(set(b1, b2), b0.tValue(attr));

		// test fuzzy compatible type

		try {
			b0.tUpdate(attr, b2);
			fail("Non collection value for multiple attribute");
		} catch (RuntimeException ex) {
			// expected.
		}

		b0.tUpdate(attr, list(b1, b2));
		assertInstanceof(b0.tValue(attr), Set.class);
		assertEquals(set(b1, b2), b0.tValue(attr));

		b0.tUpdate(attr, null);
		assertEquals(set(), b0.tValue(attr));
	}

	public void testRevisionReferences() throws KnowledgeBaseException, ConfigurationException {
		if (!kb().getHistoryManager().hasHistory()) {
			return;
		}

		Transaction tx1 = kb().beginTransaction();
		TestTypesFactory factory = TestTypesFactory.getInstance();
		ANode root = factory.getRootSingleton();
		BNode b1 = newB(root, "b1");
		tx1.commit();
		Revision rev1 = tx1.getCommitRevision();

		Transaction tx2 = kb().beginTransaction();
		b1.setRevision(rev1);
		assertEquals(rev1, b1.getRevision());
		tx2.commit();
		Revision rev2 = tx2.getCommitRevision();
		assertEquals(rev1, b1.getRevision());

		assertEquals(rev1.getCommitNumber(),
			rev1.tValue(TlCoreFactory.getRevisionRevisionAttr()));
		assertEquals(rev1.getDate(),
			((Date) rev1.tValue(TlCoreFactory.getDateRevisionAttr())).getTime());
		assertEquals(TLContext.getContext().getCurrentPersonWrapper(),
			rev1.tValue(TlCoreFactory.getAuthorRevisionAttr()));
		assertEquals(rev1.getLog(),
			rev1.tValue(TlCoreFactory.getLogRevisionAttr()));


		Transaction tx3 = kb().beginTransaction();
		String revisionType = TLModelUtil.qualifiedName(TlCoreFactory.getRevisionType());
		ReferenceConfig setConfig = TestKBBasedMetaAttributes.referenceConfig("revSet", revisionType, 0.0, true);
		TLReference revisionSet = TestKBBasedMetaAttributes.createReference(TestTypesFactory.getBType(), setConfig);
		ReferenceConfig listConfig = TestKBBasedMetaAttributes.referenceConfig("revList", revisionType, 0.0, true);
		listConfig.setOrdered(true);
		TLReference revisionList = TestKBBasedMetaAttributes.createReference(TestTypesFactory.getBType(), listConfig);
		tx3.commit();
		Revision rev3 = tx3.getCommitRevision();

		assertEquals(list(), b1.tValue(revisionList));
		assertEquals(set(), b1.tValue(revisionSet));

		Transaction tx4 = kb().beginTransaction();
		b1.tUpdate(revisionList, list(rev2, rev2));
		b1.tUpdate(revisionSet, set(rev2, rev1));
		assertEquals(list(rev2, rev2), b1.tValue(revisionList));
		assertEquals(set(rev1, rev2), b1.tValue(revisionSet));
		tx4.commit();
		assertEquals(list(rev2, rev2), b1.tValue(revisionList));
		assertEquals(set(rev1, rev2), b1.tValue(revisionSet));

		Transaction tx5 = kb().beginTransaction();
		b1.tAdd(revisionList, rev1);
		b1.tAdd(revisionList, rev3);
		b1.tAdd(revisionSet, rev1);
		b1.tAdd(revisionSet, rev3);
		assertEquals(list(rev2, rev2, rev1, rev3), b1.tValue(revisionList));
		assertEquals(set(rev1, rev3, rev2), b1.tValue(revisionSet));
		tx5.commit();
		assertEquals(list(rev2, rev2, rev1, rev3), b1.tValue(revisionList));
		assertEquals(set(rev1, rev3, rev2), b1.tValue(revisionSet));

		Transaction tx6 = kb().beginTransaction();
		b1.tAdd(revisionList, rev2);
		b1.tRemove(revisionList, rev2);
		b1.tRemove(revisionList, rev3);
		b1.tRemove(revisionList, rev3);
		b1.tRemove(revisionSet, rev1);
		b1.tRemove(revisionSet, rev1);
		assertEquals(list(rev2, rev1, rev2), b1.tValue(revisionList));
		assertEquals(set(rev2, rev3), b1.tValue(revisionSet));
		tx6.commit();
		assertEquals(list(rev2, rev1, rev2), b1.tValue(revisionList));
		assertEquals(set(rev2, rev3), b1.tValue(revisionSet));
	}

	public void testModifiableSet() throws ConfigurationException {
		Transaction tx = kb().beginTransaction();
		TestTypesFactory factory = TestTypesFactory.getInstance();
		ANode root = factory.getRootSingleton();
		ANode a1 = newA(root, "a1", "val1");
		ANode a2 = newA(root, "a2", "val1");
		BNode b1 = newB(root, "b1");
		tx.commit();
		
		Set<TLObject> modifiableSet = b1.getCollectionModifiable();
		assertEquals(set(), modifiableSet);

		tx = kb().beginTransaction();
		modifiableSet.add(a1);
		assertEquals(set(a1), modifiableSet);
		assertEquals(set(a1), b1.getCollection());
		tx.commit();
		assertEquals(set(a1), modifiableSet);
		assertEquals(set(a1), b1.getCollection());
		
		tx = kb().beginTransaction();
		// No-Op
		modifiableSet.add(a1);
		assertEquals(set(a1), modifiableSet);
		assertEquals(set(a1), b1.getCollection());
		modifiableSet.add(a2);
		assertEquals(set(a1, a2), modifiableSet);
		assertEquals(set(a1, a2), b1.getCollection());
		modifiableSet.remove(a1);
		assertEquals(set(a2), modifiableSet);
		assertEquals(set(a2), b1.getCollection());

		Iterator<? extends TLObject> it = modifiableSet.iterator();
		assertEquals(a2, it.next());
		it.remove();
		assertFalse(it.hasNext());
		assertEquals(set(), modifiableSet);
		assertEquals(set(), b1.getCollection());
		tx.commit();
		assertEquals(set(), modifiableSet);
		assertEquals(set(), b1.getCollection());
	}

	public void testModifiableList() throws ConfigurationException {
		Transaction tx = kb().beginTransaction();
		TestTypesFactory factory = TestTypesFactory.getInstance();
		ANode root = factory.getRootSingleton();
		ANode a1 = newA(root, "a1", "val1");
		ANode a2 = newA(root, "a2", "val1");
		BNode b1 = newB(root, "b1");
		tx.commit();

		List<StructuredElement> modifiableList = b1.getListModifiable();
		assertEquals(list(), modifiableList);

		tx = kb().beginTransaction();
		modifiableList.add(a1);
		assertEquals(list(a1), modifiableList);
		assertEquals(list(a1), b1.getList());
		tx.commit();
		assertEquals(list(a1), modifiableList);
		assertEquals(list(a1), b1.getList());

		tx = kb().beginTransaction();
		modifiableList.add(a2);
		assertEquals(list(a1, a2), modifiableList);
		assertEquals(list(a1, a2), b1.getList());
		modifiableList.add(a2);
		modifiableList.add(2, a1);
		assertEquals(list(a1, a2, a1, a2), modifiableList);
		assertEquals(list(a1, a2, a1, a2), b1.getList());
		modifiableList.remove(a1);
		assertEquals(list(a2, a1, a2), modifiableList);
		assertEquals(list(a2, a1, a2), b1.getList());
		modifiableList.remove(2);
		assertEquals(list(a2, a1), modifiableList);
		assertEquals(list(a2, a1), b1.getList());
		modifiableList.set(1, b1);
		assertEquals(list(a2, b1), modifiableList);
		assertEquals(list(a2, b1), b1.getList());

		ListIterator<? extends TLObject> it = modifiableList.listIterator();
		assertEquals(a2, it.next());
		assertTrue(it.hasNext());
		assertTrue(it.hasPrevious());
		assertEquals(a2, it.previous());
		assertEquals(a2, it.next());
		assertEquals(b1, it.next());
		it.remove();
		assertEquals(list(a2), modifiableList);
		assertEquals(list(a2), b1.getList());
		assertFalse(it.hasNext());
		assertTrue(it.hasPrevious());
		assertEquals(a2, it.previous());
		it.remove();
		assertEquals(list(), modifiableList);
		assertEquals(list(), b1.getList());
		tx.commit();
		assertEquals(list(), modifiableList);
		assertEquals(list(), b1.getList());
	}

	public void testHistoricReferences() throws KnowledgeBaseException {
		if (!kb().getHistoryManager().hasHistory()) {
			return;
		}

		Transaction tx = kb().beginTransaction();
		TestTypesFactory factory = TestTypesFactory.getInstance();
		ANode root = factory.getRootSingleton();
		ANode a1 = newA(root, "a1", "val1");
		ANode a2 = newA(root, "a2", "val1");
		CNode c1 = newC(root, "c1", "val1");
		tx.commit();

		Transaction tx1 = kb().beginTransaction();
		c1.setHistoricReferenceMulti(list(a1, a2));
		c1.setHistoricReferenceSingle(a1);
		assertEquals(list(a1, a2), c1.getHistoricReferenceMulti());
		assertEquals(a1, c1.getHistoricReferenceSingle());
		tx1.commit();

		assertEquals(list(inRevision(tx1, a1), inRevision(tx1, a2)), c1.getHistoricReferenceMulti());
		assertEquals(inRevision(tx1, a1), c1.getHistoricReferenceSingle());

		Transaction tx2 = kb().beginTransaction();
		a1.setAs1("val2");
		a2.setAs1("val2");
		tx2.commit();

		assertEquals(list(inRevision(tx1, a1), inRevision(tx1, a2)), c1.getHistoricReferenceMulti());
		assertEquals(inRevision(tx1, a1), c1.getHistoricReferenceSingle());

		Transaction tx3 = kb().beginTransaction();
		c1.getHistoricReferenceMultiModifiable().add(0, inRevision(tx, a1));
		c1.getHistoricReferenceMultiModifiable().add(0, a1);
		assertEquals("Current and historic object added.",
			list(a1, inRevision(tx, a1), inRevision(tx1, a1), inRevision(tx1, a2)),
			c1.getHistoricReferenceMulti());
		tx3.commit();
		assertEquals("Added a1 is stabalized",
			list(inRevision(tx3, a1), inRevision(tx, a1), inRevision(tx1, a1), inRevision(tx1, a2)),
			c1.getHistoricReferenceMulti());

	}

	public void testMixedReferences() throws KnowledgeBaseException {
		if (!kb().getHistoryManager().hasHistory()) {
			return;
		}

		Transaction tx = kb().beginTransaction();
		TestTypesFactory factory = TestTypesFactory.getInstance();
		ANode root = factory.getRootSingleton();
		ANode a1 = newA(root, "a1", "val1");
		ANode a2 = newA(root, "a2", "val1");
		CNode c1 = newC(root, "c1", "val1");
		tx.commit();

		Transaction tx1 = kb().beginTransaction();
		c1.setMixedReferenceMulti(set(a1, a2));
		c1.setMixedReferenceSingle(a1);
		assertEquals(set(a1, a2), c1.getMixedReferenceMulti());
		assertEquals(a1, c1.getMixedReferenceSingle());
		tx1.commit();

		Transaction tx2 = kb().beginTransaction();
		a1.setAs1("val2");
		a2.setAs1("val2");
		tx2.commit();

		assertEquals(set(a1, a2), c1.getMixedReferenceMulti());
		assertEquals(a1, c1.getMixedReferenceSingle());

		Transaction tx3 = kb().beginTransaction();
		c1.getMixedReferenceMultiModifiable().add(inRevision(tx1, a1));
		assertEquals("Historic object added", set(a1, a2, inRevision(tx1, a1)), c1.getMixedReferenceMulti());
		c1.setMixedReferenceSingle(inRevision(tx2, a1));
		tx3.commit();
		assertEquals(inRevision(tx2, a1), c1.getMixedReferenceSingle());
		assertEquals(set(a1, a2, inRevision(tx1, a1)), c1.getMixedReferenceMulti());
	}

	public void testMixedReferenceReverse() {
		if (!kb().getHistoryManager().hasHistory()) {
			return;
		}

		Transaction tx = kb().beginTransaction();
		ANode root = TestTypesFactory.getInstance().getRootSingleton();
		ANode a1 = newA(root, "a1", "val1");
		ANode a2 = newA(root, "a2", "val1");
		CNode c1 = newC(root, "c1", "val1");
		c1.setMixedReferenceSingle(a1);
		CNode c2 = newC(root, "c2", "val1");
		c2.setMixedReferenceSingle(a1);
		c2.setMixedReferenceMulti(set(a2));
		CNode c3 = newC(root, "c3", "val1");
		c3.setMixedReferenceMulti(set(a2, a1));
		tx.commit();

		assertEquals(set(c1, c2), a1.tReferers(TestTypesFactory.getMixedReferenceSingleCNodeAttr()));
		assertEquals(set(), a2.tReferers(TestTypesFactory.getMixedReferenceSingleCNodeAttr()));
		assertEquals(set(c3), a1.tReferers(TestTypesFactory.getMixedReferenceMultiCNodeAttr()));
		assertEquals(set(c3, c2), a2.tReferers(TestTypesFactory.getMixedReferenceMultiCNodeAttr()));

		Transaction tx2 = kb().beginTransaction();
		c1.setMixedReferenceSingle(inRevision(tx, a1));
		c2.setMixedReferenceMulti(set(a2, inRevision(tx, a2)));
		tx2.commit();

		assertEquals(set(c2), a1.tReferers(TestTypesFactory.getMixedReferenceSingleCNodeAttr()));
		assertEquals(set(c1), inRevision(tx, a1).tReferers(TestTypesFactory.getMixedReferenceSingleCNodeAttr()));
		assertEquals(set(c3), a1.tReferers(TestTypesFactory.getMixedReferenceMultiCNodeAttr()));
		assertEquals(set(c3, c2), a2.tReferers(TestTypesFactory.getMixedReferenceMultiCNodeAttr()));
		assertEquals(set(c2), inRevision(tx, a2).tReferers(TestTypesFactory.getMixedReferenceMultiCNodeAttr()));

	}

	public void testHistoricInlineRef() throws KnowledgeBaseException, ConfigurationException {
		if (!kb().getHistoryManager().hasHistory()) {
			return;
		}

		Transaction tx;
		tx = kb().beginTransaction();
		TestTypesFactory factory = TestTypesFactory.getInstance();
		ANode root = factory.getRootSingleton();
		ANode a1 = newA(root, "a1", "val1");
		BNode b1 = newB(root, "b1");
		OrderedListHelper.initMandatoryFields(a1, b1);
		tx.commit();
		A historicA1 = inRevision(tx, a1);

		tx = kb().beginTransaction();
		a1.setAs1("val2");
		tx.commit();

		tx = kb().beginTransaction();
		assertTrue(WrapperHistoryUtils.isCurrent(a1));
		b1.setHistoricInlineReference(a1);
		tx.commit();
		A historicValue = b1.getHistoricInlineReference();
		assertFalse(WrapperHistoryUtils.isCurrent(historicValue));
		assertEquals(tx.getCommitRevision(), WrapperHistoryUtils.getRevision(historicValue));
		assertEquals("val2", historicValue.getAs1());

		tx = kb().beginTransaction();
		b1.setHistoricInlineReference(historicA1);
		tx.commit();
		A newHistoricValue = b1.getHistoricInlineReference();
		assertNotEquals(historicValue, newHistoricValue);
		assertEquals("val1", newHistoricValue.getAs1());
	}

	private <T extends TLObject> T inRevision(Transaction tx, T item) {
		return WrapperHistoryUtils.getWrapper(tx.getCommitRevision(), item);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private ANode newA(ANode root, String name, String as1) {
		ANode a1 = (ANode) root.createChild(name, ANode.A_NODE_TYPE);
		a1.setAs1(as1);
		return a1;
	}

	private BNode newB(ANode root, String name) throws ConfigurationException {
		BNode b1 = (BNode) root.createChild(name, BNode.B_NODE_TYPE);
		OrderedListHelper.initMandatoryFields(root, b1);
		return b1;
	}

	private CNode newC(ANode root, String name, String as1) {
		CNode c1 = (CNode) root.createChild(name, CNode.C_NODE_TYPE);
		c1.setAs1(as1);
		return c1;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestReferenceMetaAttribute}.
	 */
	public static Test suite() {
		Test t = new TestSuite(TestReferenceMetaAttribute.class);
		t = ServiceTestSetup.createSetup(t, ModelService.Module.INSTANCE);
		t = KBSetup.getKBTest(t, KBSetup.DEFAULT_KB);
		return t;
	}
}
