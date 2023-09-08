/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.reference.current.global;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseUIException;
import com.top_logic.knowledge.service.Transaction;

/**
 * The class {@link TestDeletion} tests that deletion of {@link KnowledgeObject}s also reflects to
 * referee.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDeletion extends AbstractDBKnowledgeBaseTest {

	/**
	 * Tests that eventually existing associations from referee to other object are also removed
	 * when removing container.
	 */
	public void testAssociationRemoved4() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referee = newE("referee");
		KnowledgeObject reference = newE("reference");
		referee.setAttributeValue(REFERENCE_DELETE_POLICY_CONTAINER_NAME, reference);
		KnowledgeObject anyOther = newB("any");
		KnowledgeAssociation association = newAB(referee, anyOther);
		createTx.commit();

		Transaction deleteTx = begin();
		referee.delete();
		deleteTx.commit();

		assertFalse(reference.isAlive());
		assertFalse(referee.isAlive());
		assertTrue(anyOther.isAlive());
		assertFalse(association.isAlive());
	}

	/**
	 * Tests that eventually existing associations from reference to other object are also removed
	 * when removing container.
	 */
	public void testAssociationRemoved2() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referee = newE("referee");
		KnowledgeObject reference = newE("reference");
		referee.setAttributeValue(REFERENCE_DELETE_POLICY_CONTAINER_NAME, reference);
		KnowledgeObject anyOther = newB("any");
		KnowledgeAssociation association = newAB(reference, anyOther);
		createTx.commit();

		Transaction deleteTx = begin();
		referee.delete();
		deleteTx.commit();

		assertFalse(reference.isAlive());
		assertFalse(referee.isAlive());
		assertTrue(anyOther.isAlive());
		assertFalse(association.isAlive());
	}

	/**
	 * Tests that eventually existing associations from referee to reference are also removed.
	 */
	public void testAssociationRemoved3() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referee = newE("referee");
		KnowledgeObject reference = newE("reference");
		referee.setAttributeValue(REFERENCE_DELETE_POLICY_NAME, reference);
		KnowledgeAssociation association = newAB(referee, reference);
		createTx.commit();

		Transaction deleteTx = begin();
		reference.delete();
		deleteTx.commit();

		assertFalse(reference.isAlive());
		assertFalse(referee.isAlive());
		assertFalse(association.isAlive());
	}

	/**
	 * Tests that eventually existing associations from referee are also removed.
	 */
	public void testAssociationRemoved1() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referee = newE("referee");
		KnowledgeObject reference = newE("reference");
		referee.setAttributeValue(REFERENCE_DELETE_POLICY_NAME, reference);
		KnowledgeObject anyOther = newB("any");
		KnowledgeAssociation association = newAB(referee, anyOther);
		createTx.commit();

		Transaction deleteTx = begin();
		reference.delete();
		deleteTx.commit();

		assertFalse(reference.isAlive());
		assertFalse(referee.isAlive());
		assertTrue(anyOther.isAlive());
		assertFalse(association.isAlive());
	}

	public void testDeleteContainmentInTx() throws DataObjectException {
		testDeleteContainment(true, false);
	}

	public void testDeleteContainment() throws DataObjectException {
		testDeleteContainment(false, false);
	}

	public void testDeleteContainmentInTxRollback() throws DataObjectException {
		testDeleteContainment(true, true);
	}

	public void testDeleteContainmentRollback() throws DataObjectException {
		testDeleteContainment(false, true);
	}

	private void testDeleteContainment(boolean checkInTx, boolean rollback) throws DataObjectException, KnowledgeBaseException {
		Transaction createTx = begin();
		KnowledgeObject container = newE("container");
		KnowledgeObject reference = newE("reference");
		container.setAttributeValue(REFERENCE_DELETE_POLICY_CONTAINER_NAME, reference);
		createTx.commit();

		Transaction deleteTX = begin();
		container.delete();
		assertFalse(container.isAlive());
		if (checkInTx) {
			assertFalse(reference.isAlive());
		}
		if (rollback) {
			deleteTX.rollback();
			assertTrue(container.isAlive());
			assertTrue(reference.isAlive());
			assertEquals(reference, container.getAttributeValue(REFERENCE_DELETE_POLICY_CONTAINER_NAME));
		} else {
			deleteTX.commit();
			if (!checkInTx) {
				assertFalse(reference.isAlive());
			}
		}
	}

	public void testDeleteCyclicVeto() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referer1 = newE("referer1");
		KnowledgeObject referer2 = newE("referer2");
		referer2.setAttributeValue(REFERENCE_VETO_POLICY_NAME, referer1);
		referer1.setAttributeValue(REFERENCE_VETO_POLICY_NAME, referer2);
		createTx.commit();
		Transaction deleteTX = begin();
		referer1.delete();
		referer2.delete();
		// cyclic veto but both deleted
		deleteTX.commit();
	}

	public void testDeleteNewRefererDelete() throws DataObjectException {
		testDeleteNewRefererDelete(false);
	}

	public void testDeleteNewRefererDeleteRollback() throws DataObjectException {
		testDeleteNewRefererDelete(true);
	}

	private void testDeleteNewRefererDelete(boolean rollback) throws DataObjectException, KnowledgeBaseException,
			NoSuchAttributeException {
		Transaction createTX = begin();
		KnowledgeObject referer = newE("refererDelete");
		createTX.commit();

		Transaction changeTX = begin();
		KnowledgeObject reference = newD("reference");
		referer.setAttributeValue(REFERENCE_DELETE_POLICY_NAME, reference);
		assertEquals(reference, referer.getAttributeValue(REFERENCE_DELETE_POLICY_NAME));
		reference.delete();
		if (rollback) {
			assertFalse(referer.isAlive());
			changeTX.rollback();
			assertTrue(referer.isAlive());
			assertEquals(null, referer.getAttributeValue(REFERENCE_DELETE_POLICY_NAME));
		} else {
			assertFalse(referer.isAlive());
			changeTX.commit();
			assertFalse(referer.isAlive());
			assertFalse(reference.isAlive());
		}
	}

	public void testDeleteNew() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject refererClear = newE("refererClear");
		KnowledgeObject refererDelete = newE("refererDelete");
		KnowledgeObject refererStabilise = newE("refererStabilise");
		createTx.commit();

		Transaction changeTX = begin();
		KnowledgeObject reference = newD("reference");
		refererDelete.setAttributeValue(REFERENCE_DELETE_POLICY_NAME, reference);
		assertEquals(reference, refererDelete.getAttributeValue(REFERENCE_DELETE_POLICY_NAME));
		refererClear.setAttributeValue(REFERENCE_CLEAR_POLICY_NAME, reference);
		assertEquals(reference, refererClear.getAttributeValue(REFERENCE_CLEAR_POLICY_NAME));
		refererStabilise.setAttributeValue(REFERENCE_STABILISE_POLICY_NAME, reference);
		assertEquals(reference, refererStabilise.getAttributeValue(REFERENCE_STABILISE_POLICY_NAME));
		reference.delete();
		changeTX.commit();

		assertFalse(refererDelete.isAlive());
		assertNull(refererStabilise.getAttributeValue(REFERENCE_STABILISE_POLICY_NAME));
		assertNull(refererClear.getAttributeValue(REFERENCE_CLEAR_POLICY_NAME));
	}

	public void testRecursiveDelete() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referer1 = newE("referer1");
		KnowledgeObject referer2 = newE("referer2");
		referer2.setAttributeValue(REFERENCE_DELETE_POLICY_NAME, referer1);
		referer1.setAttributeValue(REFERENCE_DELETE_POLICY_NAME, referer2);
		createTx.commit();
		Transaction deleteTX = begin();
		referer1.delete();
		assertFalse(referer1.isAlive());
		assertFalse(referer2.isAlive());
		deleteTX.commit();

	}

	public void testDeleteInTransaction() throws DataObjectException {
		testDeletionPolicy(true, false);
	}

	public void testDeleteRefererPolicy() throws DataObjectException {
		testDeletionPolicy(false, false);
	}

	public void testDeleteInTransactionRollback() throws DataObjectException {
		testDeletionPolicy(true, true);
	}

	public void testDeleteRefererPolicyRollback() throws DataObjectException {
		testDeletionPolicy(false, true);
	}

	private void testDeletionPolicy(boolean inTX, boolean rollback) throws DataObjectException, KnowledgeBaseException,
			NoSuchAttributeException {
		Transaction createTx = begin();
		KnowledgeObject reference = newD("reference");
		KnowledgeObject refererDelete = newE("refererDelete");
		refererDelete.setAttributeValue(REFERENCE_DELETE_POLICY_NAME, reference);
		KnowledgeObject refererClear = newE("refererClear");
		refererClear.setAttributeValue(REFERENCE_CLEAR_POLICY_NAME, reference);
		KnowledgeObject refererStabilise = newE("refererStabilise");
		refererStabilise.setAttributeValue(REFERENCE_STABILISE_POLICY_NAME, reference);
		createTx.commit();
		KnowledgeItem stableRefernce = HistoryUtils.getKnowledgeItem(createTx.getCommitRevision(), reference);

		Transaction deleteTx = begin();
		reference.delete();
		if (inTX) {
			checkCorrectValues(refererDelete, refererClear, refererStabilise, stableRefernce);
		}
		if (rollback) {
			deleteTx.rollback();
			assertTrue(refererDelete.isAlive());
			assertEquals(reference, refererClear.getAttributeValue(REFERENCE_CLEAR_POLICY_NAME));

			assertTrue(refererStabilise.isAlive());
			assertEquals(reference, refererStabilise.getAttributeValue(REFERENCE_STABILISE_POLICY_NAME));
		} else {
			deleteTx.commit();
			if (!inTX) {
				checkCorrectValues(refererDelete, refererClear, refererStabilise, stableRefernce);
			}
		}
	}

	private void checkCorrectValues(KnowledgeObject refererDelete, KnowledgeObject refererClear,
			KnowledgeObject refererStabilise, KnowledgeItem stableRefernce) throws NoSuchAttributeException {
		assertFalse("expected referer is deleted due to policy", refererDelete.isAlive());
		assertTrue(refererClear.isAlive());
		assertNull(
			"Reference to deleted object must be switched to null in "
				+ DeletionPolicy.CLEAR_REFERENCE.getExternalName(),
			refererClear.getAttributeValue(REFERENCE_CLEAR_POLICY_NAME));

		assertTrue(refererStabilise.isAlive());
		assertEquals(stableRefernce, refererStabilise.getAttributeValue(REFERENCE_STABILISE_POLICY_NAME));
	}

	public void testVetoDelete() throws KnowledgeBaseException, NoSuchAttributeException,
			DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject reference = newD("reference");
		KnowledgeObject referer = newE("referer");
		referer.setAttributeValue(REFERENCE_VETO_POLICY_NAME, reference);
		createTx.commit();

		Transaction tx = begin();
		reference.delete();
		try {
			tx.commit();
			fail();
		} catch (KnowledgeBaseUIException ex) {
			// deleting must fail
		}
		rollback(tx);
	}

	public static Test suite() {
		return suiteDefaultDB(TestDeletion.class);
	}

}


