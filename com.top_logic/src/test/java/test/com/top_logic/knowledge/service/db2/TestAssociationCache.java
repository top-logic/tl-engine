/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DeletedObjectAccess;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.model.TLObject;

/**
 * {@link TestAssociationCache} tests association caches.
 * 
 * @see KnowledgeBase#resolveLinks(KnowledgeObject,
 *      com.top_logic.knowledge.service.db2.AbstractAssociationQuery)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestAssociationCache extends AbstractDBKnowledgeBaseClusterTest {

	static final String MATCHING_A2_VALUE = "matching";

	static final AssociationSetQuery<TLObject> QUERY = AssociationQuery.createQuery("myQuery", TLObject.class, E_NAME,
		REFERENCE_POLY_CUR_LOCAL_NAME);

	static final AssociationSetQuery<TLObject> MATCHING_REFERERS =
		AssociationQuery.createQuery("matchingReferers", TLObject.class, E_NAME,
			REFERENCE_POLY_CUR_LOCAL_NAME, Collections.singletonMap(A2_NAME, MATCHING_A2_VALUE));

	/**
	 * Tests that checking the "filter" of the association query works if the link is deleted. The
	 * check is performed in reaction to the deletion of an item that formerly matches the query.
	 */
	public void testCheckAttributesOnDeletedNewLink() {
		Transaction tx1 = begin();
		final KnowledgeObject d1 = newD("d1");
		
		final EObj e1 = EObj.newEObj("e1");
		e1.setA2(MATCHING_A2_VALUE);
		e1.setPolyCurLocal(d1);
		// install local association cache for current transaction
		assertEquals(set(e1), kb().resolveLinks(d1, MATCHING_REFERERS));
		// delete new link
		e1.tDelete();
		assertEquals(set(), kb().resolveLinks(d1, MATCHING_REFERERS));
		tx1.commit();
	}

	/**
	 * Tests that checking the "filter" of the association query works if the link is deleted. The
	 * check is performed in reaction to the deletion of an item that formerly matches the query.
	 */
	public void testCheckAttributesOnDeletedPersistentLink() {
		Transaction tx1 = begin();
		final KnowledgeObject d1 = newD("d1");
		final EObj e1 = EObj.newEObj("e1");
		e1.setA2(MATCHING_A2_VALUE);
		tx1.commit();

		Transaction tx2 = begin();
		e1.setPolyCurLocal(d1);
		// install local association cache for current transaction
		assertEquals(set(e1), kb().resolveLinks(d1, MATCHING_REFERERS));
		// delete persistent link
		e1.tDelete();
		assertEquals(set(), kb().resolveLinks(d1, MATCHING_REFERERS));
		tx2.commit();
	}

	public void testFoundNoNonMatchingObjects() throws DataObjectException {
		Transaction tx1 = begin();
		final KnowledgeObject d1 = newD("d1");
		tx1.commit();

		Transaction tx2 = begin();
		EObj match = EObj.newEObj("e1");
		match.setA2(MATCHING_A2_VALUE);
		match.setPolyCurLocal(d1);

		// install local association cache for current transaction
		assertEquals(set(match), kb().resolveLinks(d1, MATCHING_REFERERS));
		Iterable<Entry<String, Object>> initialValues =
			new KeyValueBuffer<String, Object>().put(A1_NAME, "e2").put(A2_NAME, MATCHING_A2_VALUE + MATCHING_A2_VALUE)
				.put(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		TLObject noMatch = kb().createKnowledgeItem(E_NAME, initialValues, KnowledgeObject.class).getWrapper();

		// new object does not match filter
		assertEquals(set(match), kb().resolveLinks(d1, MATCHING_REFERERS));
		// delete non matching referrer
		noMatch.tHandle().delete();
		// deleted object did not match filter
		assertEquals(set(match), kb().resolveLinks(d1, MATCHING_REFERERS));
		tx2.commit();
	}

	public void testCacheSetupFromLocalAdds() throws DataObjectException {
		Transaction tx1 = begin();
		final KnowledgeObject d1 = newD("d1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		e1.setPolyCurLocal(d1);
		e2.setPolyCurLocal(d1);
		tx1.commit();

		assertEquals(set(e1, e2), kb().resolveLinks(d1, QUERY));

		Transaction tx2 = begin();
		e3.setPolyCurLocal(d1);

		assertEquals(set(e1, e2, e3), kb().resolveLinks(d1, QUERY));
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(set(e1, e2), kb().resolveLinks(d1, QUERY));
			}
		});
		tx2.commit();

		assertEquals(set(e1, e2, e3), kb().resolveLinks(d1, QUERY));
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(set(e1, e2, e3), kb().resolveLinks(d1, QUERY));
			}
		});
	}

	public void testCacheSetupFromLocalDeletes() {
		Transaction tx1 = begin();
		final KnowledgeObject d1 = newD("d1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		e1.setPolyCurLocal(d1);
		e2.setPolyCurLocal(d1);
		e3.setPolyCurLocal(d1);
		tx1.commit();

		assertEquals(set(e1, e2, e3), kb().resolveLinks(d1, QUERY));

		Transaction tx2 = begin();
		e3.tDelete();

		assertEquals("Ticket #12388: Cache setup from local modifications failed.",
			set(e1, e2), kb().resolveLinks(d1, QUERY));
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(set(e1, e2, e3), kb().resolveLinks(d1, QUERY));
			}
		});
		tx2.commit();

		assertEquals(set(e1, e2), kb().resolveLinks(d1, QUERY));
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(set(e1, e2), kb().resolveLinks(d1, QUERY));
			}
		});
	}

	public void testCacheSetupFromLocalModifications() throws DataObjectException {
		Transaction tx1 = begin();
		final KnowledgeObject d1 = newD("d1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		e1.setA2("no match");
		e1.setPolyCurLocal(d1);
		e2.setA2(MATCHING_A2_VALUE);
		e2.setPolyCurLocal(d1);
		e3.setA2(MATCHING_A2_VALUE);
		e3.setPolyCurLocal(d1);
		tx1.commit();

		assertEquals(set(e2, e3), kb().resolveLinks(d1, MATCHING_REFERERS));

		Transaction tx2 = begin();
		e1.setA2(MATCHING_A2_VALUE);
		e3.setA2("no match");

		assertEquals("Ticket #12388: Cache setup from local modifications failed.",
			set(e1, e2), kb().resolveLinks(d1, MATCHING_REFERERS));
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(set(e2, e3), kb().resolveLinks(d1, MATCHING_REFERERS));
			}
		});
		tx2.commit();

		assertEquals("Ticket #12468: Global cache is not updated after attribute modification",
			set(e1, e2), kb().resolveLinks(d1, MATCHING_REFERERS));
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(set(e1, e2), kb().resolveLinks(d1, MATCHING_REFERERS));
			}
		});
	}

	public void testAssociationChangeCluster() throws DataObjectException, RefetchTimeout {
		Transaction createTx = begin();
		KnowledgeObject d1 = newD("d1");
		KnowledgeObject d2 = newD("d1");
		KnowledgeObject referer = newE("referer");
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		createTx.commit();
		
		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeItem d1Node2 = kbNode2().resolveObjectKey(node2Key(d1));
		KnowledgeItem d2Node2 = kbNode2().resolveObjectKey(node2Key(d2));
		TLObject refererNode2 = kbNode2().resolveObjectKey(node2Key(referer)).getWrapper();

		boolean cacheD1Node2UpToDate = kbNode2().resolveLinks((KnowledgeObject) d1Node2, QUERY).contains(refererNode2);
		assertTrue(cacheD1Node2UpToDate);
		boolean cacheD2Node2UpToDate = !kbNode2().resolveLinks((KnowledgeObject) d2Node2, QUERY).contains(refererNode2);
		assertTrue(cacheD2Node2UpToDate);

		Transaction changeTx = begin();
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d2);
		changeTx.commit();
		// get current content
		kbNode2().refetch();

		boolean cacheD1Node2Updated = !kbNode2().resolveLinks((KnowledgeObject) d1Node2, QUERY).contains(refererNode2);
		assertTrue("Node 2 not updated", cacheD1Node2Updated);
		boolean cacheD2Node2Updated = kbNode2().resolveLinks((KnowledgeObject) d2Node2, QUERY).contains(refererNode2);
		assertTrue("Node 2 not updated", cacheD2Node2Updated);
	}

	public void testDuringReferenceCreation() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject d1 = newD("d1");
		assertTrue(kb().resolveLinks(d1, QUERY).isEmpty());
		KnowledgeObject referer = newE("referer");
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		createTx.commit();

		boolean cacheUpdated = kb().resolveLinks(d1, QUERY).contains(referer.getWrapper());
		assertTrue("Ticket #9546: Cache was not updated when access during creation.", cacheUpdated);
	}

	public void testDuringRefererCreation() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject d1 = newD("d1");
		commit(createTx);

		Transaction createRefererTx = begin();
		assertTrue(kb().resolveLinks(d1, QUERY).isEmpty());
		KnowledgeObject referer = newE("referer");
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		createRefererTx.commit();

		boolean cacheUpdated = kb().resolveLinks(d1, QUERY).contains(referer.getWrapper());
		assertTrue("Ticket #9546: Cache was not updated after referer creation.", cacheUpdated);
	}

	public void testUpdateAfterRefererCreation() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject d1 = newD("d1");
		commit(createTx);

		assertTrue(kb().resolveLinks(d1, QUERY).isEmpty());

		Transaction createRefererTx = begin();
		KnowledgeObject referer = newE("referer");
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		createRefererTx.commit();

		boolean cacheUpdated = kb().resolveLinks(d1, QUERY).contains(referer.getWrapper());
		assertTrue("Ticket #9546: Cache was not updated after referer creation.", cacheUpdated);
	}

	public void testUpdateAfterRefererDeletion() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject refererItem = newE("referer");
		TLObject referer = refererItem.getWrapper();
		KnowledgeObject d1 = newD("d1");
		commit(createTx);

		Transaction deleteRefTx = begin();
		refererItem.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		assertTrue(kb().resolveLinks(d1, QUERY).contains(referer));
		refererItem.delete();
		assertFalse(kb().resolveLinks(d1, QUERY).contains(referer));
		deleteRefTx.commit();

		boolean cacheUpdated = !kb().resolveLinks(d1, QUERY).contains(referer);
		assertTrue("Ticket #9546: Cache was not updated after referer deletion.", cacheUpdated);
	}

	public void testUpdateAfterReferenceRemoval() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referer = newE("referer");
		KnowledgeObject d1 = newD("d1");
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		commit(createTx);

		assertTrue(kb().resolveLinks(d1, QUERY).contains(referer.getWrapper()));

		Transaction removeRefTx = begin();
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, null);
		removeRefTx.commit();

		boolean cacheUpdated = !kb().resolveLinks(d1, QUERY).contains(referer.getWrapper());
		assertTrue("Ticket #9546: Cache was not updated after referenceRemoval.", cacheUpdated);
	}

	public void testUpdateAfterReferenceSet() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referer = newE("referer");
		KnowledgeObject d1 = newD("d1");
		commit(createTx);

		assertFalse(kb().resolveLinks(d1, QUERY).contains(referer.getWrapper()));

		Transaction setRefTx = begin();
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		setRefTx.commit();

		boolean cacheUpdated = kb().resolveLinks(d1, QUERY).contains(referer.getWrapper());
		assertTrue("Ticket #9546: Cache was not updated after reference setting.", cacheUpdated);
	}

	public void testUpdateAfterReferenceChange() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject referer = newE("referer");
		KnowledgeObject d1 = newD("d1");
		KnowledgeObject d2 = newD("d1");
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d1);
		commit(createTx);

		assertTrue(kb().resolveLinks(d1, QUERY).contains(referer.getWrapper()));
		assertFalse(kb().resolveLinks(d2, QUERY).contains(referer.getWrapper()));

		Transaction changeRefTx = begin();
		referer.setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, d2);
		changeRefTx.commit();

		boolean cacheD1Updated = !kb().resolveLinks(d1, QUERY).contains(referer.getWrapper());
		assertTrue("Ticket #9546: Cache was not updated after reference change.", cacheD1Updated);
		boolean cacheD2Updated = kb().resolveLinks(d2, QUERY).contains(referer.getWrapper());
		assertTrue("Ticket #9546: Cache was not updated after reference change.", cacheD2Updated);
	}

	public void testDeleteNewAssociation() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject d1 = newD("d1");
		commit(createTx);

		Transaction begin = begin();
		KnowledgeAssociation association = newAB(b1, d1);
		association.delete();

		Set<KnowledgeAssociation> links =
			kb().resolveLinks(b1, AssociationQuery.createOutgoingQuery("ab", AB_NAME));
		assertEquals("Ticket #7490: Must not find deleted new association", set(), links);
		rollback(begin);
	}

	public void testDeleteNewAssociationByDeletingEnd() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("b1");
		commit(createTx);

		Transaction begin = begin();
		KnowledgeObject d1 = newD("d1");
		newAB(b1, d1);

		// association is also deleted
		d1.delete();

		Set<KnowledgeAssociation> links =
			kb().resolveLinks(b1, AssociationQuery.createOutgoingQuery("ab", AB_NAME));
		assertEquals("Ticket #7490: Must not find deleted new association", set(), links);
		rollback(begin);
	}

	public void testCurrentBranchLocalReferenceOnHistoricItems() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Branch branch = kb().createBranch(trunk(), lastRevision(), null);
		HistoryUtils.setContextBranch(branch);

		Transaction createTx = begin();
		KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");
		commit(createTx);

		Transaction setRefTx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		commit(setRefTx);

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME);
		assertEquals(set(),
			kb().resolveLinks((KnowledgeObject) inRevision(createTx.getCommitRevision(), reference), query));
		assertEquals(set(inRevision(setRefTx.getCommitRevision(), e1).getWrapper()),
			kb().resolveLinks((KnowledgeObject) inRevision(setRefTx.getCommitRevision(), reference), query));
		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));

		Transaction setRef2Tx = begin();
		KnowledgeObject e2 = newE("e1");
		e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		assertEquals(
			"Newly created e2 must not occur in navigation result, because requested context is a historic one.",
			set(inRevision(setRefTx.getCommitRevision(), e1).getWrapper()),
			kb().resolveLinks((KnowledgeObject) inRevision(setRefTx.getCommitRevision(), reference), query));

		rollback(setRef2Tx);

	}

	public void testHistoricReferenceOnHistoricItems() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");
		commit(createTx);
		TLObject wrapper = reference.getWrapper();
		TLObject stableReference = inRevision(createTx.getCommitRevision(), wrapper);

		Transaction setRef1TX = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_HIST_LOCAL_NAME, stableReference.tHandle());
		commit(setRef1TX);

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_HIST_LOCAL_NAME);
		assertEquals(set(e1.getWrapper()), kb().resolveLinks((KnowledgeObject) stableReference.tHandle(), query));

		Transaction setRef2TX = begin();
		KnowledgeObject e2 = newE("e1");
		e2.setAttributeValue(REFERENCE_MONO_HIST_LOCAL_NAME, stableReference.tHandle());
		assertEquals("Newly created referer not found during transaction.", set(e1.getWrapper(), e2.getWrapper()),
			kb().resolveLinks((KnowledgeObject) stableReference.tHandle(), query));
		commit(setRef2TX);
		assertEquals(set(e1.getWrapper(), e2.getWrapper()),
			kb().resolveLinks((KnowledgeObject) stableReference.tHandle(), query));
	}

	public void testNavigateOlderValues() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");
		commit(createTx);
		UpdateChainLink createTXSession = getLastSessionRevision(kb());

		Transaction setRef1TX = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		commit(setRef1TX);
		UpdateChainLink setRef1TXSession = getLastSessionRevision(kb());

		Transaction setRef2TX = begin();
		KnowledgeObject e2 = newE("e1");
		e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		commit(setRef2TX);
		UpdateChainLink setRef2TXSession = getLastSessionRevision(kb());

		Transaction setRef3TX = begin();
		KnowledgeObject e3 = newE("e1");
		e3.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		commit(setRef3TX);
		UpdateChainLink setRef3TXSession = getLastSessionRevision(kb());

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME);

		// Install globalCache
		updateSessionRevision();
		assertEquals(set(e1.getWrapper(), e2.getWrapper(), e3.getWrapper()), kb().resolveLinks(reference, query));

		// fake old session.
		updateSessionRevision(createTXSession);
		createTXSession = null; // Allow garbage collector to remove link
		// check older cache is filled
		assertEquals(set(), kb().resolveLinks(reference, query));
		// fake old session.
		updateSessionRevision(setRef2TXSession);
		setRef2TXSession = null; // Allow garbage collector to remove link
		// check cache between current and old is filled
		assertEquals(set(e1.getWrapper(), e2.getWrapper()), kb().resolveLinks(reference, query));
		// fake old session.
		updateSessionRevision(setRef1TXSession);
		setRef1TXSession = null; // Allow garbage collector to remove link
		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));
		// fake old session.
		updateSessionRevision(setRef3TXSession);
		setRef3TXSession = null; // Allow garbage collector to remove link

		// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
		// internally.
		// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced
		// objects"
		// provokeOutOfMemory();
		// assertEquals(set(e1.getWrapper(), e2.getWrapper(), e3.getWrapper()),
		// kb().resolveLinks(reference, query));
	}

	public void testRevertedLocalChanges() throws DataObjectException {
		Transaction createTx = begin();
		final KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");
		commit(createTx);

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME);

		Transaction changeTX = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);

		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));
		// remove referrer without navigation before
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, null);
		assertEquals("e1 does not point to reference", set(), kb().resolveLinks(reference, query));

		commit(changeTX);
	}

	public void testDropLocalCacheOnChangeRevert1() throws DataObjectException {
		testDropLocalCacheOnChangeRevert(false);
	}

	public void testDropLocalCacheOnChangeRevert2() throws DataObjectException {
		testDropLocalCacheOnChangeRevert(true);
	}

	/**
	 * Actually a test to increase coverage: In this concrete command sequence the local cache is
	 * dropped, because it is invalid and the local changes are reverted.
	 */
	private void testDropLocalCacheOnChangeRevert(boolean dropInTransaction) throws DataObjectException {
		Transaction createTx = begin();
		final KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");
		commit(createTx);

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME);

		Transaction setRef1TX = begin();
		// Make change to force commit
		setA1(reference, "a1New");

		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);

		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));
		final AtomicReference<TLObject> foreignObject = new AtomicReference<>();
		// concurrent commit
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction setRef2TX = begin();
				KnowledgeObject e2 = newE("e1");
				e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
				commit(setRef2TX);
				foreignObject.set(e2.getWrapper());
			}
		});

		assertFalse(kb().resolveLinks(reference, query).contains(foreignObject.get()));
		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));
		if (dropInTransaction) {
			updateSessionRevision();
		}

		// remove referrer without navigation before
		e1.delete();
		if (dropInTransaction) {
			assertEquals("e1 does not point to reference", set(foreignObject.get()), kb()
				.resolveLinks(reference, query));
		} else {
			updateSessionRevision();
		}

		commit(setRef1TX);
		assertEquals("e1 does not point to reference", set(foreignObject.get()), kb().resolveLinks(reference, query));
	}
	
	public void testNavigateAfterChangeReferenceInTransaction() throws DataObjectException {
		Transaction createTx = begin();
		final KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		commit(createTx);

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME);

		Transaction changeTX = begin();
		KnowledgeObject e2 = newE("e1");
		e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		
		assertEquals(set(e1.getWrapper(), e2.getWrapper()), kb().resolveLinks(reference, query));
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, null);
		assertEquals(set(e2.getWrapper()), kb().resolveLinks(reference, query));
		rollback(changeTX);
	}

	public void testConcurrentCommitEffectingCache() throws DataObjectException {
		Transaction createTx = begin();
		final KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");
		commit(createTx);

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME);

		Transaction change1Tx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		// initialize cache by navigate query
		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));

		final AtomicReference<KnowledgeObject> concurrentlyCreatedObject = new AtomicReference<>();
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction change2TX = begin();
				KnowledgeObject e2 = newE("e2");
				e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
				change2TX.commit();
				concurrentlyCreatedObject.set(e2);
			}

		});
		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));
		change1Tx.commit();
		assertEquals(set(e1.getWrapper(), concurrentlyCreatedObject.get().getWrapper()),
			kb().resolveLinks(reference, query));
	}

	public void testConcurrentCommitNotEffectingCache() throws DataObjectException {
		Transaction createTx = begin();
		final KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");
		commit(createTx);

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME);

		Transaction change1Tx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		// initialize cache by navigate query
		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));

		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction irrelevantTX = begin();
				newE("e2");
				irrelevantTX.commit();
			}

		});
		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));
		change1Tx.commit();
		assertEquals(set(e1.getWrapper()), kb().resolveLinks(reference, query));
	}

	public void testGlobalCacheIsContextIndependent() throws DataObjectException {
		String flexOrderAttribute = "flexOrder";
		Transaction createTx = begin();
		final KnowledgeObject reference = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(reference, "a1");

		final KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		e1.setAttributeValue(flexOrderAttribute, Integer.valueOf(1));

		final KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		e2.setAttributeValue(flexOrderAttribute, Integer.valueOf(2));

		final KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, reference);
		e3.setAttributeValue(flexOrderAttribute, Integer.valueOf(3));

		commit(createTx);

		final OrderedLinkQuery<TLObject> orderedQuery =
			AssociationQuery.createOrderedLinkQuery("testGlobalCacheIsContextIndependentQuery", TLObject.class, E_NAME,
				REFERENCE_MONO_CUR_LOCAL_NAME, flexOrderAttribute);

		Transaction tx = begin();
		e2.delete();
		List<TLObject> resolvedLinks;
		try {
			resolvedLinks = kb().resolveLinks(reference, orderedQuery);
		} catch (DeletedObjectAccess ex) {
			throw fail("Resolving global cache within transaction must not include local changes.", ex);
		}
		assertEquals("Deletion must be seen in transaction.", list(e1.getWrapper(), e3.getWrapper()), resolvedLinks);
		inThread(new Execution() {
			
			@Override
			public void run() throws Exception {
				assertEquals("Deletion must not be seen in transaction.",
					list(e1.getWrapper(), e2.getWrapper(), e3.getWrapper()), kb().resolveLinks(reference, orderedQuery));
			}

		});
		commit(tx);
	}

	public void testLocalAssociationChangesOnConcurrentCommit() throws InterruptedException {
		Transaction createTx = begin();
		final KnowledgeObject baseItem = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(baseItem, "a1");
		commit(createTx);

		AssociationSetQuery<?> query =
			AssociationQuery.createQuery("query", TLObject.class, E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME);

		assertEquals(set(), kb().resolveLinks(baseItem, query));

		final Barrier setLinkBarrier = createBarrier(3);
		final Barrier conCommitBarrier = createBarrier(3);
		final Barrier testBarrier = createBarrier(3);
		final Barrier setRefCommitBarrier = createBarrier(3);
		
		long baseRevision = kb().getSessionRevision();
		AtomicLong createLinkTXRev = new AtomicLong(-1);
		AtomicLong irrelevantTXRev = new AtomicLong(-1);
		AtomicReference<KnowledgeItem> reference = new AtomicReference<>();

		TestFuture result1 = inParallel(new Execution() {

			/**
			 * This method triggers a new local association cache by setting a new reference. Before
			 * the commit occurred, a concurrent commit occurs which does not affect the value of
			 * the association cache.
			 */
			@Override
			public void run() throws Exception {
				assertEquals(baseRevision, kb().getSessionRevision());
				Transaction tX = begin();
				KnowledgeObject e1 = newE("e1");
				e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, baseItem);
				reference.set(e1);
				assertEquals(set(e1.getWrapper()), kb().resolveLinks(baseItem, query));
				setLinkBarrier.enter(1000);
				conCommitBarrier.enter(1000);
				testBarrier.enter(1000);
				tX.commit();
				createLinkTXRev.set(tX.getCommitRevision().getCommitNumber());
				assertEquals(set(e1.getWrapper()), kb().resolveLinks(baseItem, query));
				setRefCommitBarrier.enter(1000);
			}

		});
		TestFuture result2 = inParallel(new Execution() {
			
			/**
			 * Waits until a new local association cache is created but not yet committed. Then a
			 * commit occurs which does not affect the association cache.
			 */
			@Override
			public void run() throws Exception {
				assertEquals(baseRevision, kb().getSessionRevision());
				setLinkBarrier.enter(1000);
				Transaction concurrentTX = begin();
				setA1(baseItem, "a2");
				concurrentTX.commit();
				irrelevantTXRev.set(concurrentTX.getCommitRevision().getCommitNumber());
				conCommitBarrier.enter(1000);
				testBarrier.enter(1000);
				setRefCommitBarrier.enter(1000);
			}
			
		});
		TestFuture result3 = inParallel(new Execution() {
			
			/**
			 * Waits until the local cache is created (not committed) and the concurrent commit was
			 * successful. At this time the values from the association cache must not be visible.
			 */
			@Override
			public void run() throws Exception {
				assertEquals(baseRevision, kb().getSessionRevision());
				setLinkBarrier.enter(1000);
				conCommitBarrier.enter(1000);
				updateSessionRevision();
				assertEquals(irrelevantTXRev.longValue(), kb().getSessionRevision());
				testBarrier.enter(1000);
				setRefCommitBarrier.enter(1000);
				assertEquals("Ticket #26903: Not yet committed changes from other session must not be visible.", set(),
					kb().resolveLinks(baseItem, query));
				updateSessionRevision();
				assertEquals(createLinkTXRev.longValue(), kb().getSessionRevision());
				assertEquals(set(reference.get().getWrapper()), kb().resolveLinks(baseItem, query));
			}
			
		});
		result1.check();
		result2.check();
		result3.check();
	}

	/**
	 * Overridden to prevent warning when calling from inner class.
	 * 
	 * @see test.com.top_logic.knowledge.service.AbstractKnowledgeBaseTest#updateSessionRevision()
	 */
	@Override
	protected void updateSessionRevision() throws MergeConflictException {
		super.updateSessionRevision();
	}

	@SuppressWarnings("unused")
	public static Test suite() {
		if (!true) {
			String testName = "testLocalAssociationChangesOnConcurrentCommit";
			return runOneTest(TestAssociationCache.class, testName);
		}
		return suite(TestAssociationCache.class);
	}

}

