/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;

import com.top_logic.knowledge.CurrentOnlyReferenceStorage;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test for {@link CurrentOnlyReferenceStorage}.
 */
@SuppressWarnings("javadoc")
public class TestCurrentOnlyReferenceStorage extends AbstractDBKnowledgeBaseTest {

	@Override
	protected LocalTestSetup createSetup(Test self) {
		return new DBKnowledgeBaseTestSetup(self,
			new ConfiguredScenario("webinf://kbase/TestCurrentOnlyReferenceStorage.xml"));
	}

	public void testCurrent() {
		Transaction tx = begin();
		KnowledgeItem a1 = newA();
		KnowledgeItem a2 = newA();
		setRef(a1, a2);
		assertEquals(a2, getRef(a1));
		tx.commit();
		Revision createRev = tx.getCommitRevision();

		assertEquals(a2, getRef(a1));

		KnowledgeItem historicA1 = inRevision(createRev, a1);
		assertEquals("Reference always current.", a2, getRef(historicA1));

		Transaction delTx = begin();
		assertEquals("a2 still exists.", a2, getRef(historicA1));
		a2.delete();
		assertTrue("No invalid element must be returned by the KB.", getRef(historicA1).isAlive());
		assertEquals("a2 deleted.", inRevision(createRev, a2), getRef(historicA1));
		delTx.rollback();

		assertEquals("a2 not deleted.", a2, getRef(historicA1));

		delTx = begin();
		assertEquals("a2 still exists.", a2, getRef(historicA1));
		a2.delete();
		assertEquals("a2 deleted.", inRevision(createRev, a2), getRef(historicA1));
		delTx.commit();
		assertEquals("a2 deleted.", inRevision(createRev, a2), getRef(historicA1));

	}

	private KnowledgeItem newA() {
		return kb().createKnowledgeObject("A");
	}

	private void setRef(KnowledgeItem a, KnowledgeItem ref) {
		a.setAttributeValue("alwaysCurrent", ref);
	}

	private KnowledgeItem getRef(KnowledgeItem a) {
		return (KnowledgeItem) a.getAttributeValue("alwaysCurrent");
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestCurrentOnlyReferenceStorage}.
	 */
	public static Test suite() {
		return suite(TestCurrentOnlyReferenceStorage.class);
	}

}
