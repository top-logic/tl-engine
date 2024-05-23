/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.structured.model.ANode;
import test.com.top_logic.element.structured.model.CContent;
import test.com.top_logic.element.structured.model.CNode;
import test.com.top_logic.element.structured.model.Part;
import test.com.top_logic.element.structured.model.TestTypesFactory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.model.ModelService;

/**
 * The class {@link TestCompositeMetaAttribute} tests composition reference meta attributes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCompositeMetaAttribute extends BasicTestCase {

	public void testMultipleCompositionReferenceModifiable() throws KnowledgeBaseException {
		ANode root;
		CNode cReferrer;
		try (Transaction tx = kb().beginTransaction()) {
			TestTypesFactory factory = TestTypesFactory.getInstance();
			root = factory.getRootSingleton();
			cReferrer = newC(root, "referrer");
			tx.commit();
		}

		List<CContent> referenceList = cReferrer.getCompositeReferenceMultiModifiable();
		List<CContent> expectedReferences = new ArrayList<>();

		try (Transaction tx = kb().beginTransaction()) {
			for (int i = 0; i < 100; i++) {
				CContent ref = newCContent();
				referenceList.add(ref);
				expectedReferences.add(ref);
				assertEquals(expectedReferences, referenceList);
			}
			tx.commit();
		}

		assertEquals(expectedReferences, referenceList);

		try (Transaction tx = kb().beginTransaction()) {
			cReferrer.tDelete();
			tx.commit();
		}
		for (CContent node : expectedReferences) {
			assertFalse("Deleting referrer also deletes composites.", node.tHandle().isAlive());
		}
	}

	public void testMultipleCompositionReferenceModifiableRandomOrder() throws KnowledgeBaseException {
		ANode root;
		CNode cReferrer;
		try (Transaction tx = kb().beginTransaction()) {
			TestTypesFactory factory = TestTypesFactory.getInstance();
			root = factory.getRootSingleton();
			cReferrer = newC(root, "referrer");
			tx.commit();
		}

		List<CContent> referenceList = cReferrer.getCompositeReferenceMultiModifiable();
		List<CContent> expectedReferences = new ArrayList<>();


		try (Transaction tx = kb().beginTransaction()) {
			Random r = new Random(2786);
			for (int i = 0; i < 100; i++) {
				CContent ref = newCContent();
				int insertIDX = r.nextInt(i + 1);
				referenceList.add(insertIDX, ref);
				expectedReferences.add(insertIDX, ref);
				assertEquals(expectedReferences, referenceList);
			}
			tx.commit();
		}

	}

	public void testCompositeListStoredInTarget() {
		ANode aNode;
		Part p1;
		Part p2;
		Part p3;
		Part p4;
		try (Transaction tx = kb().beginTransaction()) {
			aNode = newANode();
			p1 = newPart("p1");
			p2 = newPart("p2");
			p3 = newPart("p3");
			p4 = newPart("p4");
			tx.commit();
		}
		
		assertEquals(list(), aNode.getCompositeList1());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.addCompositeList1(p1);
			assertEquals(list(p1), aNode.getCompositeList1());
			aNode.addCompositeList1(p2);
			assertEquals(list(p1, p2), aNode.getCompositeList1());
			aNode.removeCompositeList1(p3);
			assertEquals(list(p1, p2), aNode.getCompositeList1());
			aNode.removeCompositeList1(p1);
			assertEquals(list(p2), aNode.getCompositeList1());
			aNode.addCompositeList1(p1);
			assertEquals(list(p2, p1), aNode.getCompositeList1());
			aNode.setCompositeList1(list(p2, p1));
			assertEquals(list(p2, p1), aNode.getCompositeList1());
			aNode.setCompositeList1(list(p1, p2, p3));
			assertEquals(list(p1, p2, p3), aNode.getCompositeList1());
			aNode.setCompositeList1(list(p1));
			assertEquals(list(p1), aNode.getCompositeList1());
			aNode.setCompositeList1(list());
			assertEquals(list(), aNode.getCompositeList1());
			aNode.setCompositeList1(list(p2, p1));
			assertEquals(list(p2, p1), aNode.getCompositeList1());
			tx.commit();
		}
		assertEquals(list(p2, p1), aNode.getCompositeList1());
		assertEquals(set(aNode), p1.tReferers(TestTypesFactory.getCompositeList1ANodeAttr()));
		assertEquals(set(aNode), p2.tReferers(TestTypesFactory.getCompositeList1ANodeAttr()));
		assertEquals(set(), p3.tReferers(TestTypesFactory.getCompositeList1ANodeAttr()));

		List<Part> compositeList1 = aNode.getCompositeList1Modifiable();
		try (Transaction tx = kb().beginTransaction()) {
			compositeList1.clear();
			assertEquals(list(), aNode.getCompositeList1());
			compositeList1.add(p2);
			compositeList1.add(0, p1);
			assertEquals(list(p1, p2), compositeList1);
			aNode.addCompositeList1(p3);
			assertEquals(list(p1, p2, p3), compositeList1);
			compositeList1.add(1, p4);
			assertEquals(list(p1, p4, p2, p3), compositeList1);
			compositeList1.removeAll(list(p2, p4));
			assertEquals(list(p1, p3), compositeList1);
			tx.commit();
		}
		assertEquals(list(p1, p3), aNode.getCompositeList1());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.tDelete();
			assertFalse(aNode.tValid());
			assertFalse(p1.tValid());
			assertFalse(p3.tValid());
			assertTrue(p2.tValid());
			assertTrue(p4.tValid());
			tx.rollback();
		}
		assertTrue(aNode.tValid());
		assertTrue(p1.tValid());
		assertTrue(p3.tValid());
		assertTrue(p2.tValid());
		assertTrue(p4.tValid());
		assertEquals(list(p1, p3), aNode.getCompositeList1());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.tDelete();
			tx.commit();
		}
		assertFalse(aNode.tValid());
		assertFalse(p1.tValid());
		assertFalse(p3.tValid());
		assertTrue(p2.tValid());
		assertTrue(p4.tValid());
	}

	public void testTwoCompositeListStoredInTarget() {
		ANode aNode;
		Part p1;
		Part p2;
		Part p3;
		Part p4;
		try (Transaction tx = kb().beginTransaction()) {
			aNode = newANode();
			p1 = newPart("p1");
			p2 = newPart("p2");
			p3 = newPart("p3");
			p4 = newPart("p4");
			tx.commit();
		}
		assertEquals(list(), aNode.getCompositeList1());
		assertEquals(list(), aNode.getCompositeList2());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.setCompositeList1(list(p1, p2));
			aNode.setCompositeList2(list(p3, p4));
			tx.commit();
		}
		List<Part> comp1 = aNode.getCompositeList1Modifiable();
		List<Part> comp2 = aNode.getCompositeList2Modifiable();
		assertEquals(list(p1, p2), comp1);
		assertEquals(list(p3, p4), comp2);
		try (Transaction tx = kb().beginTransaction()) {
			try {
				aNode.addCompositeList2(p1);
				fail(p1 + " already contained in a different composite attribute");
			} catch (Exception ex) {
				// expected.
			}
			try {
				comp2.add(p1);
				fail(p1 + " already contained in a different composite attribute");
			} catch (Exception ex) {
				// expected.
			}
			comp1.remove(p1);
			comp2.set(0, p1);
			tx.commit();
		}
		assertEquals(list(p2), aNode.getCompositeList1());
		assertEquals(list(p2), comp1);
		assertEquals(list(p1, p4), aNode.getCompositeList2());
		assertEquals(list(p1, p4), comp2);
		assertTrue(p3.tValid());
	
		assertEquals(null, p3.tContainer());
		assertEquals(null, p3.tContainerReference());
		assertEquals(aNode, p2.tContainer());
		assertEquals(TestTypesFactory.getCompositeList1ANodeAttr(), p2.tContainerReference());
		assertEquals(aNode, p1.tContainer());
		assertEquals(TestTypesFactory.getCompositeList2ANodeAttr(), p1.tContainerReference());
	}

	public void testCompositeSetStoredInTarget() {
		ANode aNode;
		Part p1;
		Part p2;
		Part p3;
		Part p4;
		try (Transaction tx = kb().beginTransaction()) {
			aNode = newANode();
			p1 = newPart("p1");
			p2 = newPart("p2");
			p3 = newPart("p3");
			p4 = newPart("p4");
			tx.commit();
		}

		assertEquals(set(), aNode.getCompositeSet1());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.addCompositeSet1(p1);
			assertEquals(set(p1), aNode.getCompositeSet1());
			aNode.addCompositeSet1(p2);
			assertEquals(set(p1, p2), aNode.getCompositeSet1());
			aNode.removeCompositeSet1(p3);
			assertEquals(set(p1, p2), aNode.getCompositeSet1());
			aNode.removeCompositeSet1(p1);
			assertEquals(set(p2), aNode.getCompositeSet1());
			aNode.addCompositeSet1(p1);
			assertEquals(set(p2, p1), aNode.getCompositeSet1());
			aNode.setCompositeSet1(set(p2, p1));
			assertEquals(set(p2, p1), aNode.getCompositeSet1());
			aNode.setCompositeSet1(set());
			assertEquals(set(), aNode.getCompositeSet1());
			aNode.setCompositeSet1(set(p2, p1));
			assertEquals(set(p2, p1), aNode.getCompositeSet1());
			tx.commit();
		}
		assertEquals(set(p2, p1), aNode.getCompositeSet1());
		assertEquals(set(aNode), p1.tReferers(TestTypesFactory.getCompositeSet1ANodeAttr()));
		assertEquals(set(aNode), p2.tReferers(TestTypesFactory.getCompositeSet1ANodeAttr()));
		assertEquals(set(), p3.tReferers(TestTypesFactory.getCompositeSet1ANodeAttr()));

		Set<Part> compositeSet1 = aNode.getCompositeSet1Modifiable();
		try (Transaction tx = kb().beginTransaction()) {
			compositeSet1.clear();
			assertEquals(set(), aNode.getCompositeSet1());
			compositeSet1.addAll(list(p1, p2));
			assertEquals(set(p1, p2), compositeSet1);
			aNode.addCompositeSet1(p3);
			assertEquals(set(p1, p2, p3), compositeSet1);
			compositeSet1.add(p4);
			assertEquals(set(p1, p4, p2, p3), compositeSet1);
			compositeSet1.removeAll(list(p2, p4));
			assertEquals(set(p1, p3), compositeSet1);
			assertFalse(compositeSet1.remove(p2));
			assertEquals(set(p1, p3), compositeSet1);
			assertFalse(compositeSet1.add(p1));
			assertEquals(set(p1, p3), compositeSet1);
			tx.commit();
		}
		assertEquals(set(p1, p3), aNode.getCompositeSet1());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.tDelete();
			assertFalse(aNode.tValid());
			assertFalse(p1.tValid());
			assertFalse(p3.tValid());
			assertTrue(p2.tValid());
			assertTrue(p4.tValid());
			tx.rollback();
		}
		assertTrue(aNode.tValid());
		assertTrue(p1.tValid());
		assertTrue(p3.tValid());
		assertTrue(p2.tValid());
		assertTrue(p4.tValid());
		assertEquals(set(p1, p3), aNode.getCompositeSet1());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.tDelete();
			tx.commit();
		}
		assertFalse(aNode.tValid());
		assertFalse(p1.tValid());
		assertFalse(p3.tValid());
		assertTrue(p2.tValid());
		assertTrue(p4.tValid());
	}

	public void testTwoCompositeSetStoredInTarget() {
		ANode aNode;
		Part p1;
		Part p2;
		Part p3;
		Part p4;
		try (Transaction tx = kb().beginTransaction()) {
			aNode = newANode();
			p1 = newPart("p1");
			p2 = newPart("p2");
			p3 = newPart("p3");
			p4 = newPart("p4");
			tx.commit();
		}
		assertEquals(set(), aNode.getCompositeSet1());
		assertEquals(set(), aNode.getCompositeSet2());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.setCompositeSet1(set(p1, p2));
			aNode.setCompositeSet2(set(p3, p4));
			tx.commit();
		}
		Set<Part> comp1 = aNode.getCompositeSet1Modifiable();
		Set<Part> comp2 = aNode.getCompositeSet2Modifiable();
		assertEquals(set(p1, p2), comp1);
		assertEquals(set(p3, p4), comp2);
		try (Transaction tx = kb().beginTransaction()) {
			try {
				aNode.addCompositeSet2(p1);
				fail(p1 + " already contained in a different composite attribute");
			} catch (Exception ex) {
				// expected.
			}
			try {
				comp2.add(p1);
				fail(p1 + " already contained in a different composite attribute");
			} catch (Exception ex) {
				// expected.
			}
			comp1.remove(p1);
			comp2.remove(p3);
			comp2.add(p1);
			tx.commit();
		}
		assertEquals(set(p2), aNode.getCompositeSet1());
		assertEquals(set(p2), comp1);
		assertEquals(set(p1, p4), aNode.getCompositeSet2());
		assertEquals(set(p1, p4), comp2);
		assertTrue(p3.tValid());

		assertEquals(null, p3.tContainer());
		assertEquals(null, p3.tContainerReference());
		assertEquals(aNode, p2.tContainer());
		assertEquals(TestTypesFactory.getCompositeSet1ANodeAttr(), p2.tContainerReference());
		assertEquals(aNode, p1.tContainer());
		assertEquals(TestTypesFactory.getCompositeSet2ANodeAttr(), p1.tContainerReference());
	}

	public void testTwoCompositeStoredInTarget() {
		ANode aNode;
		Part p1;
		Part p3;
		try (Transaction tx = kb().beginTransaction()) {
			aNode = newANode();
			p1 = newPart("p1");
			p3 = newPart("p3");
			tx.commit();
		}
		assertEquals(null, aNode.getComposite1());
		assertEquals(null, aNode.getComposite2());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.setComposite1(p1);
			aNode.setComposite2(p3);
			tx.commit();
		}
		assertEquals(p1, aNode.getComposite1());
		assertEquals(p3, aNode.getComposite2());
		try (Transaction tx = kb().beginTransaction()) {
			try {
				aNode.setComposite2(p1);
				fail(p1 + " already contained in a different composite attribute");
			} catch (Exception ex) {
				// expected.
			}
			aNode.setComposite1(null);
			aNode.setComposite2(p1);
			assertEquals(null, aNode.getComposite1());
			assertEquals(p1, aNode.getComposite2());
			tx.commit();
		}
		assertTrue(p3.tValid());

		assertEquals(null, p3.tContainer());
		assertEquals(null, p3.tContainerReference());
		assertEquals(aNode, p1.tContainer());
		assertEquals(TestTypesFactory.getComposite2ANodeAttr(), p1.tContainerReference());
	}

	public void testCompositeStoredInSource() {
		ANode aNode;
		Part p1;
		Part p3;
		try (Transaction tx = kb().beginTransaction()) {
			aNode = newANode();
			p1 = newPart("p1");
			p3 = newPart("p3");
			tx.commit();
		}
		assertEquals(null, aNode.getComposite());
		assertEquals(null, p1.tContainer());
		assertEquals(null, p1.tContainerReference());
		try (Transaction tx = kb().beginTransaction()) {
			aNode.setComposite(p1);
			tx.commit();
		}
		assertEquals(p1, aNode.getComposite());
		assertEquals(aNode, p1.tContainer());
		assertEquals(TestTypesFactory.getCompositeANodeAttr(), p1.tContainerReference());
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private ANode newANode() {
		ANode aNode = TestTypesFactory.getInstance().createANode();
		return aNode;
	}

	private Part newPart(String name) {
		Part part = TestTypesFactory.getInstance().createPart();
		part.setName(name);
		return part;
	}

	private CContent newCContent() {
		CContent cContent = TestTypesFactory.getInstance().createCContent();
		return cContent;
	}

	private CNode newC(ANode root, String name) {
		CNode c1 = (CNode) root.createChild(name, CNode.C_NODE_TYPE);
		return c1;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestCompositeMetaAttribute}.
	 */
	public static Test suite() {
		Test t = new TestSuite(TestCompositeMetaAttribute.class);
		t = ServiceTestSetup.createSetup(t, ModelService.Module.INSTANCE);
		t = KBSetup.getKBTest(t, KBSetup.DEFAULT_KB);
		return t;
	}
}
