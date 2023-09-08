/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.structured.model.ANode;
import test.com.top_logic.element.structured.model.CContent;
import test.com.top_logic.element.structured.model.CNode;
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


	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
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
