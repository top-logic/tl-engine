/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.reference.current.global;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Tests getting referer given by
 * {@link DBKnowledgeBase#getAnyReferer(KnowledgeItem)}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReferer extends AbstractDBKnowledgeBaseTest {

	public void testFindOnDifferentBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction createReferenceTX = begin();
		KnowledgeObject reference = newD("d1");
		commit(createReferenceTX);

		KnowledgeObject refereeB1;
		Branch b1 = kb().createBranch(trunk(), lastRevision(), null);
		Branch oldCtxBranch = HistoryUtils.setContextBranch(b1);
		try {
			Transaction createRefereeTx = begin();
			refereeB1 = newE("e1");
			refereeB1.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
			commit(createRefereeTx);

		} finally {
			HistoryUtils.setContextBranch(oldCtxBranch);
		}
		assertEquals(list(refereeB1), kb().getAnyReferer(reference));
		KnowledgeItem referenceOnBranch = HistoryUtils.getKnowledgeItem(b1, reference);
		assertEquals(list(), kb().getAnyReferer(referenceOnBranch));
	}

	public void testFindOnlyCurrentObject() throws DataObjectException {
		Transaction createReferenceTX = begin();
		KnowledgeObject referee = newE("e1");
		KnowledgeObject reference = newD("d1");
		referee.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		commit(createReferenceTX);

		Transaction changeTx = begin();
		referee.setAttributeValue(A2_NAME, "someValue");
		commit(changeTx);

		// must not find the historic variant of "b1"
		assertEquals(list(referee), kb().getAnyReferer(reference));
	}

	public void testNavigateHistoric() throws DataObjectException {
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject referee = newE("e1");
		KnowledgeObject reference = newD("d1");
		referee.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		createReferenceTX.commit();

		assertEquals(list(referee), kb().getAnyReferer(reference));

		KnowledgeItem historicReference =
			HistoryUtils.getKnowledgeItem(createReferenceTX.getCommitRevision(), reference);
		KnowledgeItem historicReferee = HistoryUtils.getKnowledgeItem(createReferenceTX.getCommitRevision(), referee);
		assertEquals(list(historicReferee), kb()
			.getAnyReferer(createReferenceTX.getCommitRevision(), historicReference));
	}

	public void testChangeRefererInTx() throws DataObjectException {
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject referer = newE("e1");
		createReferenceTX.commit();

		Transaction changeTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		// reference changed within the transaction
		assertEquals(list(referer), kb().getAnyReferer(reference));
		changeTX.commit();

		assertEquals(list(referer), kb().getAnyReferer(reference));
	}

	public void testRefererToSelf() throws DataObjectException {
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject reference = referer;
		referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		createReferenceTX.commit();

		assertEquals(list(referer), kb().getAnyReferer(reference));
	}

	public void testRefererToSelfInTransaction() throws DataObjectException {
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject reference = referer;
		referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		List<KnowledgeItem> allReferers = kb().getAnyReferer(reference);
		createReferenceTX.commit();

		assertEquals(list(referer), allReferers);
	}

	public void testReferenceSetInTransaction() throws DataObjectException {
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referer = newE("e1");
		createReferenceTX.commit();

		assertEquals(list(), kb().getAnyReferer(reference));

		Transaction deleteTX = kb().beginTransaction();
		referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		// reference deleted within the transaction
		assertEquals(list(referer), kb().getAnyReferer(reference));
		deleteTX.commit();

		assertEquals(list(referer), kb().getAnyReferer(reference));
	}

	public void testReferenceDeletedInTransaction() throws DataObjectException {
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referer = newE("e1");
		referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		createReferenceTX.commit();

		assertEquals(list(referer), kb().getAnyReferer(reference));

		Transaction deleteTX = kb().beginTransaction();
		referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, null);
		// reference deleted within the transaction
		assertEquals(list(), kb().getAnyReferer(reference));
		deleteTX.commit();

		assertEquals(list(), kb().getAnyReferer(reference));
	}

	public void testRefererDeletedInTransaction() throws DataObjectException {
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referer = newE("e1");
		referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		createReferenceTX.commit();

		assertEquals(list(referer), kb().getAnyReferer(reference));

		Transaction deleteTX = kb().beginTransaction();
		referer.delete();
		// referer deleted within the transaction
		assertEquals(list(), kb().getAnyReferer(reference));
		deleteTX.commit();

		assertEquals(list(), kb().getAnyReferer(reference));
	}

	public void testRefererCreatedInTransaction() throws DataObjectException {
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		createReferenceTX.commit();

		Transaction createRefererTX = kb().beginTransaction();
		KnowledgeObject referer = newE("e1");
		referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		// receive refers within the transaction
		List<KnowledgeItem> referers = kb().getAnyReferer(reference);

		createRefererTX.commit();

		assertNotNull(referers);
		assertEquals(1, referers.size());
		assertEquals(referer, referers.get(0));
	}

	public void testGetRefererFromDifferentBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction createReferenceTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		createReferenceTX.commit();

		Branch branch = HistoryUtils.createBranch(HistoryUtils.getTrunk(), createReferenceTX.getCommitRevision());

		KnowledgeObject referer;
		Branch previousBranch = HistoryUtils.setContextBranch(branch);
		try {
			Transaction createRefererOnOtherBranchTX = kb().beginTransaction();
			referer = newE("e1_branch");
			referer.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
			createRefererOnOtherBranchTX.commit();
		} finally {
			HistoryUtils.setContextBranch(previousBranch);
		}

		List<KnowledgeItem> referers = kb().getAnyReferer(reference);
		assertNotNull(referers);
		assertEquals(1, referers.size());
		assertEquals(referer, referers.get(0));
	}

	public void testGetRefererComplex() throws DataObjectException {
		Transaction createTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referer = newE("e1");
		setReference(referer, reference, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		createTX.commit();

		Transaction createRef2TX = kb().beginTransaction();
		KnowledgeObject referer2 = newE("e2");
		setReference(referer2, reference, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		createRef2TX.commit();

		Transaction createRef3TX = kb().beginTransaction();
		KnowledgeObject referer3 = newF("f1");
		referer3.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		createRef3TX.commit();

		List<KnowledgeItem> referers = kb().getAnyReferer(reference);
		assertNotNull(referers);
		assertEquals(set(referer, referer2, referer3), toSet(referers));
		assertEquals(3, referers.size());
	}

	public void testGetRefererSimple() throws DataObjectException {
		Transaction createTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referer = newE("e1");
		setReference(referer, reference, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		createTX.commit();

		List<KnowledgeItem> referers = kb().getAnyReferer(reference);
		assertNotNull(referers);
		assertEquals(1, referers.size());
		assertEquals(referer, referers.get(0));
	}

	public static Test suite() {
		if (false) {
			return runOneTest(TestReferer.class, "testRefererToSelfInTransaction", DBType.MYSQL_DB);
		}
		return suiteDefaultDB(TestReferer.class);
	}

}
