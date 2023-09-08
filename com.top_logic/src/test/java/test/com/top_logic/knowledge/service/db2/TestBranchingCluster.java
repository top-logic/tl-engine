/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import test.com.top_logic.KBTestUtils;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test creating and accessing branches on other cluster node.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestBranchingCluster extends AbstractDBKnowledgeBaseClusterTest {

	public void testBranchLoad() throws Throwable {
		final Revision r2;
		{
			Transaction tx = begin();
			final KnowledgeObject b1 = newB("b1");
			assertNotNull(b1);
			commit(tx);
			r2 = tx.getCommitRevision();
		}

		Branch branchNode1 = HistoryUtils.createBranch(HistoryUtils.getContextBranch(), r2, null);
		long branchID = branchNode1.getBranchId();

		Branch branchNode2 = HistoryUtils.getHistoryManager(kbNode2()).getBranch(branchID);
		assertEquals(branchID, branchNode2.getBranchId());
	}

	public void testCreateBranch() throws Throwable {
		HistoryUtils.setContextBranch(trunk());

		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject b3;
		final KnowledgeAssociation b1b2;
		final KnowledgeAssociation b2b3;
		final KnowledgeAssociation b1b3;
		final Revision r1;
		{
			Transaction tx = begin();
			b1 = newB("b1.a1");
			b2 = newB("b2.a1");
			b3 = newB("b3.a1");

			b1b2 = newAB(b1, b2);
			b2b3 = newAB(b2, b3);
			b1b3 = newAB(b1, b3);

			assertNotNull(b1b2);

			commit(tx);
			r1 = tx.getCommitRevision();
		}

		{
			Transaction tx = begin();
			setA1(b1, "b1.a1.update1");
			setA1(b2, "b2.a1.update1");
			setA1(b3, "b3.a1.update1");

			b1b3.delete();

			commit(tx);
		}

		Branch branch1 = HistoryUtils.createBranch(trunk(), r1, null);

		HistoryUtils.setContextBranch(branch1);

		final KnowledgeObject branch1_b4;
		final KnowledgeObject branch1_b1;
		final KnowledgeObject branch1_b2;
		final KnowledgeObject branch1_b3;
		final KnowledgeAssociation branch1_b2b3;
		final Execution testValues;
		{
			Transaction tx = begin();
			branch1_b4 = newB("b4.a1.branch1");

			branch1_b1 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch1, b1);
			branch1_b2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch1, b2);
			branch1_b3 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch1, b3);
			branch1_b2b3 = (KnowledgeAssociation) HistoryUtils.getKnowledgeItem(branch1, b2b3);

			assertNotNull(branch1_b1);

			setA1(branch1_b1, "b1.a1.branch1.update1");
			setA1(branch1_b2, "b2.a1.branch1.update1");
			setA1(branch1_b3, "b3.a1.branch1.update1");

			branch1_b2b3.delete();

			newAB(branch1_b1, branch1_b4);
			newAB(branch1_b4, branch1_b3);

			testValues = new Execution() {
				@Override
				public void run() throws Exception {
					assertEquals("Branch updates seen on branch.", "b1.a1.branch1.update1",
						branch1_b1.getAttributeValue(A1_NAME));
					assertEquals("Trunk updates seen on trunk.", "b1.a1.update1", b1.getAttributeValue(A1_NAME));

					KBTestUtils.assertTargets("Branch associations are visible on branch.",
						new KnowledgeObject[] { branch1_b2, branch1_b3, branch1_b4 },
						branch1_b1.getOutgoingAssociations(AB_NAME));

					KBTestUtils.assertTargets("Trunk associations are visible on trunk.",
						new KnowledgeObject[] { b2 },
						b1.getOutgoingAssociations(AB_NAME));
				}
			};

			testValues.run();

			commit(tx);
		}

		testValues.run();
		inThread(testValues);
		
		// refetch to be able to resolve object
		refetchNode2();
		// Fetch values on other cluster node from DB (Navigate cross-branch to enforce reloading the items):
		final KnowledgeObject b1Node2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(trunk2(), node2Item(branch1_b1));
		KnowledgeItem branch1_b4_node2 = node2Item(branch1_b4);
		assertNull("b4 does not exits on trunk", HistoryUtils.getKnowledgeItem(trunk2(), branch1_b4_node2));

		Branch branch1_node2 = HistoryUtils.getHistoryManager(kbNode2()).getBranch(branch1.getBranchId());
		final KnowledgeObject branch1_b1_Node2 =
			(KnowledgeObject) HistoryUtils.getKnowledgeItem(branch1_node2, node2Item(b1));

		assertEquals("Branch updates seen on branch.", "b1.a1.branch1.update1",
			branch1_b1_Node2.getAttributeValue(A1_NAME));
		assertEquals("Trunk updates seen on trunk.", "b1.a1.update1", b1Node2.getAttributeValue(A1_NAME));

		KBTestUtils.assertTargets("Branch associations are visible on branch.",
			new KnowledgeObject[] { (KnowledgeObject) node2Item(branch1_b2), (KnowledgeObject) node2Item(branch1_b3),
				(KnowledgeObject) branch1_b4_node2 },
			branch1_b1_Node2.getOutgoingAssociations(AB_NAME));

		KBTestUtils.assertTargets("Trunk associations are visible on trunk.",
			new KnowledgeObject[] { (KnowledgeObject) node2Item(b2) },
			b1Node2.getOutgoingAssociations(AB_NAME));

	}

	public void testLookupInPartialBranch() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			newAB(b1, b2);
			commit(tx);
		}

		final KnowledgeObject b3;
		{
			Transaction tx = begin();
			// Produce additional revisions.
			b3 = newB("b3");
			commit(tx);
		}

		final KnowledgeObject b4;
		final Revision r2;
		{
			Transaction tx = begin();
			b4 = newB("b4");
			newAB(b3, b4);
			commit(tx);
			r2 = tx.getCommitRevision();
		}

		// Create branch that does not branch the created objects.
		Branch branch2 = HistoryUtils.createBranch(HistoryUtils.getTrunk(), r2, types(C_NAME));

		// Lookup in node2 to have clean copy from database
		refetchNode2();

		// Lookup objects in a revision that is not their data branch.

		Branch branch2_node2 = kbNode2().getHistoryManager().getBranch(branch2.getBranchId());
		kbNode2().getHistoryManager().setContextBranch(branch2_node2);
		DataObject b1_branch2_node2 = kbNode2().getObjectByAttribute(B_NAME, A1_NAME, "b1");
		assertNotNull(b1_branch2_node2);

		// Object is from visible part of base branch of partial branch.
		assertNotEquals("Objects in different KnowledgeBases.", b1, b1_branch2_node2);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestBranchingCluster}.
	 */
	public static Test suite() {
		return suiteNeedsBranches(TestBranchingCluster.class);
	}

}

