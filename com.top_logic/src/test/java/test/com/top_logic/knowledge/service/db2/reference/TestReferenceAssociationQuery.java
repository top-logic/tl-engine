/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.model.TLObject;

/**
 * The class {@link TestReferenceAssociationQuery} tests AssociationQueries for reference values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestReferenceAssociationQuery extends AbstractDBKnowledgeBaseTest {

	private static final AssociationSetQuery<TLObject> POLY_CUR_LOCAL_REF =
		AssociationQuery.createQuery("myQuery", TLObject.class, D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME);

	private static final AssociationSetQuery<TLObject> POLY_CUR_GLOBAL_REF =
		AssociationQuery.createQuery("x", TLObject.class, E_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME);

	private static final AssociationSetQuery<TLObject> POLY_MIXED_LOCAL_REF =
		AssociationQuery.createQuery("x", TLObject.class, D_NAME, REFERENCE_POLY_MIXED_LOCAL_NAME);

	/**
	 * Tests searching with an {@link AssociationQuery} which has parameters
	 */
	public void testSearchWithParameters() throws DataObjectException {
		Transaction createReferenceTx = begin();
		KnowledgeObject reference = newE("e1");
		commit(createReferenceTx);

		KnowledgeItem stableReference = HistoryUtils.getKnowledgeItem(createReferenceTx.getCommitRevision(), reference);

		Transaction createRefereeTx = begin();
		KnowledgeObject referee = newE("AAA");
		referee.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		referee.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, stableReference);
		commit(createRefereeTx);

		Transaction createNoRefereeTx = begin();
		// A1 does not match
		KnowledgeObject noReferee1 = newE("A1 does not match");
		noReferee1.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, referee);
		noReferee1.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, stableReference);

		// A_REFERENCE_TO_A_NAME does not match
		KnowledgeObject noReferee2 = newE("AAA");
		noReferee2.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		// same object but different revision
		Object stableReference2 = HistoryUtils.getKnowledgeItem(createRefereeTx.getCommitRevision(), reference);
		noReferee2.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, stableReference2);

		commit(createNoRefereeTx);

		Map<String, Object> attributeQuery = new HashMap<>();
		attributeQuery.put(REFERENCE_POLY_HIST_GLOBAL_NAME, stableReference);
		attributeQuery.put(A1_NAME, "AAA");
		AssociationSetQuery<TLObject> query =
			AssociationQuery.createQuery("xx", TLObject.class, E_NAME,
				KnowledgeBaseTestScenarioConstants.REFERENCE_POLY_CUR_GLOBAL_NAME, attributeQuery);

		Set<TLObject> resolvedLinks = kb().resolveLinks(reference, query);
		assertEquals(wrapperSet(referee), resolvedLinks);
	}

	/**
	 * Tests searching with an {@link AssociationQuery} which has parameters
	 */
	public void testSearchReferenceAttributeID() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject reference = newE("e1");
		KnowledgeObject referenced = newE("referenced");
		KnowledgeObject referencedPMG = newE("referenced");
		reference.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, referenced);
		reference.setAttributeValue(REFERENCE_POLY_MIXED_GLOBAL_NAME, referencedPMG);

		KnowledgeObject notReferenced = newE("e1");
		notReferenced.setAttributeValue(REFERENCE_POLY_MIXED_GLOBAL_NAME, referencedPMG);

		KnowledgeObject notReferencedPMG = newE("e1");
		notReferencedPMG.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, referenced);

		commit(createTX);

		Map<String, KnowledgeObject> arguments =
			Collections.singletonMap(REFERENCE_POLY_MIXED_GLOBAL_NAME, referencedPMG);
		// install associationCache
		AssociationSetQuery<TLObject> referenceQuery =
			AssociationQuery.createQuery("x", TLObject.class, E_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME, arguments);
		Set<TLObject> resolvedLinks = kb().resolveLinks(referenced, referenceQuery);
		assertEquals(wrapperSet(reference), resolvedLinks);

	}

	public void testAssociationCacheUpToDate() throws DataObjectException {
		Transaction createReferenceTX = begin();
		KnowledgeObject reference = newD("d1");
		commit(createReferenceTX);
		// install associationCache
		Set<TLObject> emptyReferees = kb().resolveLinks(reference, POLY_CUR_GLOBAL_REF);
		assertEquals(set(), emptyReferees);

		Transaction setRefTx = begin();
		KnowledgeObject referee = newE("e1");
		referee.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		commit(setRefTx);

		Set<TLObject> references = kb().resolveLinks(reference, POLY_CUR_GLOBAL_REF);
		assertEquals(wrapperSet(referee), references);
	}

	public void testResolveAssociations() throws DataObjectException {
		Transaction createReferenceTX = begin();
		KnowledgeObject referee = newE("e1");
		KnowledgeObject reference = newD("d1");
		referee.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		commit(createReferenceTX);

		Set<TLObject> references = kb().resolveLinks(reference, POLY_CUR_GLOBAL_REF);
		assertEquals(wrapperSet(referee), references);
	}

	public void testResolveSubtype() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject d1 = newD("d1");
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		d1.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);

		// Check in TX
		assertEquals("Ticket #21432", wrapperSet(d1, e1), kb().resolveLinks(d1, POLY_CUR_LOCAL_REF));
		commit(tx1);
		// Check after TX
		assertEquals(wrapperSet(d1, e1), kb().resolveLinks(d1, POLY_CUR_LOCAL_REF));

		Transaction tx2 = begin();
		e1.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, e1);
		d1.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, e1);

		// Check in TX
		assertEquals(wrapperSet(), kb().resolveLinks(d1, POLY_CUR_LOCAL_REF));
		assertEquals(wrapperSet(d1, e1), kb().resolveLinks(e1, POLY_CUR_LOCAL_REF));
		commit(tx2);
		// Check after TX
		assertEquals(wrapperSet(), kb().resolveLinks(d1, POLY_CUR_LOCAL_REF));
		assertEquals(wrapperSet(d1, e1), kb().resolveLinks(e1, POLY_CUR_LOCAL_REF));
	}

	public void testMixedAssociationQuery() throws InterruptedException {
		testMixedAssociationQuery(true, false);
		/* Checking within commit, actually triggers filling of association caches, which leads to
		 * different control flow. Therefore also test without checking within commit must be
		 * executed. */
		testMixedAssociationQuery(false, true);
		testMixedAssociationQuery(true, true);
	}

	private void testMixedAssociationQuery(boolean checkInTX, boolean checkAfterTX) throws InterruptedException {
		Transaction tx1 = begin();
		KnowledgeObject d1 = newD("d1");
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_POLY_MIXED_LOCAL_NAME, d1);
		d1.setAttributeValue(REFERENCE_POLY_MIXED_LOCAL_NAME, d1);

		if (checkInTX) {
			// Check in TX
			assertEquals(wrapperSet(d1, e1), kb().resolveLinks(d1, POLY_MIXED_LOCAL_REF));
		}
		commit(tx1);
		if (checkAfterTX) {
			// Check after TX
			assertEquals(wrapperSet(d1, e1), kb().resolveLinks(d1, POLY_MIXED_LOCAL_REF));
		}
		KnowledgeObject d1Tx1 = (KnowledgeObject) inRevision(tx1.getCommitRevision(), d1);

		try (SequentialBarrier barrier = new SequentialBarrier()) {
			inParallel(new Execution() {

				@Override
				public void run() throws Exception {
					/* Check whether old session revision retrieves the correct search result. */
					barrier.step(1);
					assertEquals(tx1.getCommitRevision(), HistoryUtils.getSessionRevision());
					barrier.step(3);
					assertEquals(wrapperSet(d1, e1), kb().resolveLinks(d1, POLY_MIXED_LOCAL_REF));
					assertEquals(wrapperSet(), kb().resolveLinks(d1Tx1, POLY_MIXED_LOCAL_REF));
					updateSessionRevision();
					assertEquals(wrapperSet(d1), kb().resolveLinks(d1, POLY_MIXED_LOCAL_REF));
					assertEquals(wrapperSet(e1), kb().resolveLinks(d1Tx1, POLY_MIXED_LOCAL_REF));
				}
			});
			barrier.step(0);
			barrier.step(2);
			Transaction tx2 = begin();
			e1.setAttributeValue(REFERENCE_POLY_MIXED_LOCAL_NAME, d1Tx1);

			if (checkInTX) {
				// Check in TX
				assertEquals(wrapperSet(d1), kb().resolveLinks(d1, POLY_MIXED_LOCAL_REF));
				assertEquals(wrapperSet(e1), kb().resolveLinks(d1Tx1, POLY_MIXED_LOCAL_REF));
			}
			commit(tx2);
			if (checkAfterTX) {
				// Check after TX
				assertEquals(wrapperSet(d1), kb().resolveLinks(d1, POLY_MIXED_LOCAL_REF));
				assertEquals(wrapperSet(e1), kb().resolveLinks(d1Tx1, POLY_MIXED_LOCAL_REF));
			}
		}
	}

	Set<Object> wrapperSet(KnowledgeItem... kis) {
		return addWrappers(new HashSet<>(), kis);
	}

	private <T extends Collection<? super TLObject>> T addWrappers(T out, KnowledgeItem... kis) {
		for (KnowledgeItem item : kis) {
			out.add(item.getWrapper());
		}
		return out;
	}

	List<Object> wrapperList(KnowledgeItem... kis) {
		return addWrappers(new ArrayList<>(), kis);
	}

	/**
	 * Allow access in inner classes.
	 */
	@Override
	protected void updateSessionRevision() throws MergeConflictException {
		super.updateSessionRevision();
	}

	public static Test suite() {
		return suite(TestReferenceAssociationQuery.class);
	}

}


