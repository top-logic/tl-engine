/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.Collection;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;

/**
 * Test case for {@link WrapperHistoryUtils}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestWrapperHistoryUtils extends AbstractDBKnowledgeBaseTest {

	/**
	 * Test that a historic wrapper shows historic values
	 */
	public void testStableObject() throws KnowledgeBaseException {
		Revision r1;
		BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			tx.commit();

			r1 = tx.getCommitRevision();
		}

		Wrapper b1R1 = WrapperHistoryUtils.getWrapper(r1, b1);

		assertEquals(b1.getValue(A1_NAME), "b1");
		assertEquals(b1R1.getValue(A1_NAME), "b1");

		b1.setA1("newB1");

		assertEquals(b1.getValue(A1_NAME), "newB1");
		assertEquals(b1R1.getValue(A1_NAME), "b1");
	}

	/**
	 * Test that some historic wrappers shows historic values
	 */
	public void testStableObjects() throws KnowledgeBaseException {
		Revision r1;
		BObj b1;
		BObj b2;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			tx.commit();

			r1 = tx.getCommitRevision();
		}

		Collection<BObj> wrappersR1 = WrapperHistoryUtils.getWrappers(r1, CollectionUtil.createList(b1, b2));

		Wrapper b1R1 = this.findWrapperByName(wrappersR1, "b1");
		Wrapper b2R1 = this.findWrapperByName(wrappersR1, "b2");

		assertEquals(b1.getValue(A1_NAME), "b1");
		assertEquals(b1R1.getValue(A1_NAME), "b1");
		assertEquals(b2.getValue(A1_NAME), "b2");
		assertEquals(b2R1.getValue(A1_NAME), "b2");

		b1.setA1("newB1");
		b2.setA1("newB2");

		assertEquals(b1.getValue(A1_NAME), "newB1");
		assertEquals(b1R1.getValue(A1_NAME), "b1");

		assertEquals(b2.getValue(A1_NAME), "newB2");
		assertEquals(b2R1.getValue(A1_NAME), "b2");
	}

	private BObj findWrapperByName(Collection<BObj> someWrappers, String aName) {
		for (BObj theWrapper : someWrappers) {
			if (aName.equals(theWrapper.getValue(A1_NAME))) {
				return theWrapper;
			}
		}

		return null;
	}

	/**
	 * Test history and branch navigation.
	 */
	public void testHistory() throws KnowledgeBaseException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Revision r1;
		BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			tx.commit();
			
			r1 = tx.getCommitRevision();
		}
		
		assertTrue(WrapperHistoryUtils.isCurrent(b1));
		assertTrue(WrapperHistoryUtils.isInRevision(Revision.CURRENT, b1));
		assertTrue(WrapperHistoryUtils.isInBranch(kb().getTrunk(), b1));
		
		Wrapper b1R1 = WrapperHistoryUtils.getWrapper(r1, b1);
		
		assertFalse(WrapperHistoryUtils.isCurrent(b1R1));
		assertTrue(WrapperHistoryUtils.isInRevision(r1, b1R1));
		assertTrue(WrapperHistoryUtils.isInBranch(kb().getTrunk(), b1));
		
		Branch branch1 = kb().createBranch(kb().getTrunk(), r1, null);
		Revision r2 = HistoryUtils.getLastRevision(kb());
		Wrapper b1R2 = WrapperHistoryUtils.getWrapper(r2, b1);
		
		Wrapper b1Branch1 = WrapperHistoryUtils.getWrapper(branch1, b1);
		assertTrue(WrapperHistoryUtils.isCurrent(b1Branch1));
		assertTrue(WrapperHistoryUtils.isInRevision(Revision.CURRENT, b1Branch1));
		assertTrue(WrapperHistoryUtils.isInBranch(branch1, b1Branch1));
		
		Wrapper b1Branch1R2 = WrapperHistoryUtils.getWrapper(branch1, r2, b1);
		assertFalse(WrapperHistoryUtils.isCurrent(b1Branch1R2));
		assertTrue(WrapperHistoryUtils.isInRevision(r2, b1Branch1R2));
		assertTrue(WrapperHistoryUtils.isInBranch(branch1, b1Branch1R2));
		
		assertSame(b1Branch1R2, WrapperHistoryUtils.getWrapper(branch1, b1R2));
		assertSame(b1Branch1R2, WrapperHistoryUtils.getWrapper(r2, b1Branch1));
	}
	
	/**
	 * Test Ticket #18074
	 */
	public void testObjectToCurrentRevision() throws KnowledgeBaseException {
		Revision r1;
		BObj b1;
		BObj b2;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			tx.commit();

			r1 = tx.getCommitRevision();
		}
		BObj historicB1 = (BObj) WrapperHistoryUtils.getWrapper(r1, b1);
		BObj historicB2 = (BObj) WrapperHistoryUtils.getWrapper(r1, b2);

		Collection<BObj> currentWrappers =
			WrapperHistoryUtils.getWrappers(Revision.CURRENT, CollectionUtil.createList(historicB1, historicB2));

		assertSame(b1, findWrapperByName(currentWrappers, "b1"));
		assertSame(b2, findWrapperByName(currentWrappers, "b2"));
	}
	
	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suite(TestWrapperHistoryUtils.class);
	}

}
