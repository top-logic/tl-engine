/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.KBTestUtils;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SA;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SC;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBCache;
import com.top_logic.knowledge.service.merge.MergeConflictException;

/**
 * Test case for cluster support in {@link DBKnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDBKnowledgeBaseCluster extends AbstractDBKnowledgeBaseClusterTest {

	public static final String TICKET_14043 = "Ticket #14043: Refetch broken for unversioned KnowledgeBase tables.";

	public void testUpdateEventRevision() throws DataObjectException {
		UpdateListener l = new UpdateListener() {
			
			@Override
			public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
				long commitNumber = event.getCommitNumber();
				ChangeSet changes = event.getChanges();
				assertEquals(commitNumber, changes.getRevision());
				assertEquals(commitNumber, changes.getCommit().getRevision());
				assertRevisionNumber(commitNumber, changes.getCreations());
				assertRevisionNumber(commitNumber, changes.getDeletions());
				assertRevisionNumber(commitNumber, changes.getUpdates());
			}
			
			private void assertRevisionNumber(long expected, Collection<? extends KnowledgeEvent> events) {
				for (KnowledgeEvent ke : events) {
					assertEquals("Event with different commit number: " + ke, expected, ke.getRevision());
				}
			}
		};

		kb().addUpdateListener(l);
		kbNode2().addUpdateListener(l);

		Transaction createTx = begin();
		KnowledgeObject b = newB("a1");
		commit(createTx);
		Transaction updateTx = begin();
		setA1(b, "newA1");
		commit(updateTx);
		Transaction delTx = begin();
		b.delete();
		commit(delTx);

		// refetch calls update listener of node2.
		refetchNode2();
	}

	/**
	 * Refetch in {@link #kbNode2()} loads the {@link KnowledgeItem} of <code>sa</code>. As
	 * {@link KnowledgeBaseTestScenarioConstants#S_NAME} has wrapper with dynamic subtype,
	 * attributes of currently refetched item must be accessed. This must occur in the correct (the
	 * refetch) revision.
	 * 
	 * @see #testRefetchWithReferenceAccessInWrapperConstructor()
	 */
	public void testRefetchWithDynamicSubtype() {
		Transaction tx = begin();
		SA sa = SA.newSAObj("s1");
		commit(tx);

		refetchNode2();
		
		assertNotNull(node2Item(sa.tHandle()));
	}

	/**
	 * Refetch in {@link #kbNode2()} loads the {@link KnowledgeItem} of <code>sa</code>. As
	 * {@link KnowledgeBaseTestScenarioConstants#S_NAME} has wrapper with dynamic subtype,
	 * attributes of currently refetched item must be accessed. This must occur in the correct (the
	 * refetch) revision.
	 * 
	 * <p>
	 * In contrast to {@link #testRefetchWithDynamicSubtype()}, the concrete wrapper class
	 * {@link SC} also accesses a reference attribute.
	 * </p>
	 * 
	 * @see #testRefetchWithDynamicSubtype()
	 * @see #testRefetchWithReferenceAccessInWrapperConstructor2()
	 */
	public void testRefetchWithReferenceAccessInWrapperConstructor() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeItem newB =
			kb().createKnowledgeItem(KnowledgeBaseTestScenarioConstants.REFERENCE_TYPE_NAME, KnowledgeItem.class);
		setA1(newB, "a1");
		SC sc = SC.newSCObj("s1", newB);
		commit(tx);

		refetchNode2();

		assertNotNull(node2Item(sc.tHandle()));
	}

	/**
	 * As {@link #testRefetchWithReferenceAccessInWrapperConstructor()}, but the referenced object
	 * was created and re-fetched in a previous revision. This is the reason that the reference is
	 * loaded during update of caches, which currently occurs before the revision was updated.
	 * 
	 * @see #testRefetchWithReferenceAccessInWrapperConstructor()
	 */
	public void testRefetchWithReferenceAccessInWrapperConstructor2() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeItem newB =
			kb().createKnowledgeItem(KnowledgeBaseTestScenarioConstants.REFERENCE_TYPE_NAME, KnowledgeItem.class);
		setA1(newB, "a1");
		commit(tx);

		Transaction refTx = begin();
		SC sc = SC.newSCObj("s1", newB);
		commit(refTx);

		refetchNode2();

		assertNotNull(node2Item(sc.tHandle()));
	}

	public void testClearCache() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		commit(tx);
		ObjectKey b1Key = node1Key(b1);
		b1 = null;

		KBTestUtils.clearCache(kb());
		KnowledgeItem resolvedObject = kb().resolveCachedObjectKey(b1Key);
		assertNull("Cache must be empty", resolvedObject);

		// loads the item to the cache.
		resolvedObject = kb().resolveObjectKey(b1Key);
		assertNotNull(resolvedObject);

		resolvedObject = kb().resolveCachedObjectKey(b1Key);
		assertNotNull("Object is loaded to cache before", resolvedObject);
	}

	public void testResolveCachedObjectKey() throws DataObjectException, RefetchTimeout {
		final KnowledgeObject b1;
		ObjectKey key;
		DefaultObjectKey searchKey;
		{
			// Create objects on the primary node.
			Transaction tx = begin();
			b1 = newB("b1");

			key = b1.tId();
			searchKey =
				new DefaultObjectKey(key.getBranchContext(), key.getHistoryContext(), key.getObjectType(),
					key.getObjectName());

			assertEquals("Ticket #11113: Cannot resolve cached key in the open transation.",
				b1, kb().resolveCachedObjectKey(key));
			assertEquals(b1, kb().resolveCachedObjectKey(searchKey));

			commit(tx);
		}

		assertEquals(b1, kb().resolveCachedObjectKey(key));
		assertEquals(b1, kb().resolveCachedObjectKey(searchKey));

		// Update the cache to the new revision. With a consistent knowledge base cache, the object
		// cannot be resolved in node 2, otherwise.
		kbNode2().refetch();

		ObjectKey node2Key = KBUtils.transformKey(kbNode2().getMORepository(), key);
		KnowledgeItem b1Node2 = kbNode2().resolveObjectKey(node2Key);
		assertNotNull(b1Node2);

		assertEquals(b1Node2, kbNode2().resolveCachedObjectKey(node2Key));
	}

	public void testFetchingDeletedObject() throws DataObjectException, RefetchTimeout {
		Transaction createTx = begin();
		KnowledgeObject referer = newE("referer");
		KnowledgeObject reference = newD("reference");
		ObjectKey expected_referencedKey_node2 = node2Key(reference);
		String referenceAttr = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		referer.setAttributeValue(referenceAttr, reference);
		createTx.commit();

		// fetch item in node2
		refetchNode2();
		KnowledgeItem referer_node2 = node2Item(referer);
		assertNotNull(referer_node2);

		Transaction tx1 = begin();
		reference.delete();
		tx1.commit();

		// node2 doesn't know anything about the deletion.
		ObjectKey referencedKey_node2 =
			referer_node2.getReferencedKey(MetaObjectUtils.getReference(referer_node2.tTable(), referenceAttr));
		assertNotNull(referencedKey_node2);
		assertEquals(expected_referencedKey_node2, referencedKey_node2);
		Object referenceNode2 = referer_node2.getAttributeValue(referenceAttr);
		assertNotNull("Not null key causes not null reference.", referenceNode2);
	}

	public void testSearchingFutureUnversionedObject() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject u1 = newU("a1");
		KnowledgeObject u2 = newU("a2");
		commit(tx1);

		RevisionQuery<KnowledgeObject> query =
			queryUnresolved(filter(allOf(U_NAME), eqBinary(attribute(U_NAME, A1_NAME), literal("a1"))));
		assertNull("Object was created in future revision.", node2Item(u1));
		assertEquals("Object was created in future revision.", list(), kbNode2().search(query));

		refetchNode2();

		assertNotNull("Object was created in older revision.", node2Item(u1));
		assertEquals("Object was created in older revision.", list(node2Item(u1)), kbNode2().search(query));

		Transaction changeTx = begin();
		setA1(u2, "a1");
		commit(changeTx);

		assertEquals("Object was modified in future revision.", "a2", node2Item(u2).getAttributeValue(A1_NAME));
		assertEquals("Object was modified in future revision.", list(node2Item(u1)), kbNode2().search(query));

		refetchNode2();

		assertEquals("Object was modified in older revision.", "a1", node2Item(u2).getAttributeValue(A1_NAME));
		assertEquals("Object was modified in older revision.", set(node2Item(u1), node2Item(u2)), toSet(kbNode2()
			.search(query)));
	}

	public void testSearchingFutureObject() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject b1 = newB("a1");
		KnowledgeObject b2 = newB("a2");
		commit(tx1);

		RevisionQuery<KnowledgeObject> query =
			queryUnresolved(filter(allOf(B_NAME), eqBinary(attribute(B_NAME, A1_NAME), literal("a1"))));
		assertNull("Object was created in future revision.", node2Item(b1));
		assertEquals("Object was created in future revision.", list(), kbNode2().search(query));

		refetchNode2();

		assertNotNull("Object was created in older revision.", node2Item(b1));
		assertEquals("Object was created in older revision.", list(node2Item(b1)), kbNode2().search(query));

		Transaction changeTx = begin();
		setA1(b2, "a1");
		commit(changeTx);

		assertEquals("Object was modified in future revision.", "a2", node2Item(b2).getAttributeValue(A1_NAME));
		assertEquals("Object was modified in future revision.", list(node2Item(b1)), kbNode2().search(query));

		refetchNode2();

		assertEquals("Object was modified in older revision.", "a1", node2Item(b2).getAttributeValue(A1_NAME));
		assertEquals("Object was modified in older revision.", set(node2Item(b1), node2Item(b2)), toSet(kbNode2()
			.search(query)));
	}

	public void testFetchingFutureUnversionedObject() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject u = newU("a1");
		commit(tx1);

		KnowledgeItem u_Node2 = node2Item(u);
		assertNull("Create revision to large. Must not be visible in KnowledgeBase.", u_Node2);
	}

	public void testFetchingFutureObject() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject b = newB("a1");
		commit(tx1);
		
		assertTrue(HistoryUtils.getRevision(b).getCommitNumber() > kbNode2().getLastRevision());
		KnowledgeItem b_Node2 = node2Item(b);
		assertNull("Create revision to large. Must not be visible in KnowledgeBase.", b_Node2);
	}

	public void testRevisionLoad() throws Throwable {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			commit(tx);
		}

		final long r3;
		{
			Transaction tx = begin();
			setA2(b1, "update 1");
			commit(tx);
			r3 = tx.getCommitRevision().getCommitNumber();
		}

		{
			Transaction tx = begin();
			setA2(b1, "update 2");
			commit(tx);
		}

		// Drop cache and try to load old revisions.
		Revision R3 = HistoryUtils.getHistoryManager(kbNode2()).getRevision(r3);
		assertNotNull(R3);
	}

	/**
	 * Tests {@link KnowledgeItem#getReferencedKey(com.top_logic.dob.meta.MOReference)} does not
	 * load the referenced object itself.
	 */
	public void testGetReferencedObjectKey() throws DataObjectException {
		String referenceAttr = UNTYPED_POLY_CUR_GLOBAL_NAME;
		Transaction createTX = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject reference = newE("reference");

		// Choose object of type B as "not loaded reference", because it does not have any reference
		// attributes. This is necessary as all new (and changed) objects which have reference
		// attributes are loaded during refetch to get referenced object to update there association
		// caches.
		KnowledgeObject notLoadedReference = newB("referenceNotLodedInNode2");
		assertTrue("This test needs type without references",
			MetaObjectUtils.getReferences(notLoadedReference.tTable()).isEmpty());

		e1.setAttributeValue(referenceAttr, reference);
		reference.setAttributeValue(referenceAttr, notLoadedReference);
		createTX.commit();

		MOReference referenceAttribute =
			MetaObjectUtils.getReference(e1.tTable(), referenceAttr);
		assertEquals(reference.tId(), e1.getReferencedKey(referenceAttribute));

		// refetch to be able to resolve object
		refetchNode2();

		/* Re-fetching node2 will load the created objects. Therefore the cache must be cleared for
		 * test. */
		KBTestUtils.clearCache(kbNode2());

		KnowledgeItem e1Node2 = node2Item(e1);
		MOReference referenceAttributeNode2 =
			MetaObjectUtils.getReference(e1Node2.tTable(), referenceAttr);
		assertEquals(node2Key(reference), e1Node2.getReferencedKey(referenceAttributeNode2));

		// Check that the reference was not loaded yet. It checks that the object key of a reference
		// of the reference is not known yet
		ObjectKey nonLoadedKeyInNode2 = node2Key(notLoadedReference);
		assertSame("Expected key was not formerly loaded by KB2. This is a test that the reference is not resolved.",
			nonLoadedKeyInNode2, getKnownKey(kbNode2(), nonLoadedKeyInNode2));
		// load the reference such that the KnowledgeBase knows about the
		DataObject resolvedReferenceInNode2 = (DataObject) e1Node2.getValue(referenceAttributeNode2);
		assertSame("Reference was not yet loaded. This should return a different object key.",
			nonLoadedKeyInNode2, getKnownKey(kbNode2(), nonLoadedKeyInNode2));
		Object referenceNowLoadedInNode2 = resolvedReferenceInNode2.getValue(referenceAttributeNode2);
		assertNotNull(referenceNowLoadedInNode2);
		assertNotSame("Reference was loaded. This should return a different object key.",
			nonLoadedKeyInNode2, getKnownKey(kbNode2(), nonLoadedKeyInNode2));
		assertSame("Reference was loaded. This should return the object key of the referenced value object.",
			((IdentifiedObject) referenceNowLoadedInNode2).tId(), getKnownKey(kbNode2(), nonLoadedKeyInNode2));

	}

	/**
	 * Tests Ticket: 7220:
	 * <ol>
	 * <li><code>a</code> and <code>b</code> exists
	 * <li>Node1 begins transaction and deletes <code>a</code>
	 * <li>Node2 creates reference <code>ab</code> between <code>a</code> and <code>b</code> and
	 * commits.
	 * <li>Node1 commits.
	 * <li><code>ab</code> still alive
	 * </ol>
	 */
	public void testDanglingPointer() throws DataObjectException, RefetchTimeout {
		final KnowledgeObject b1;
		{
			// Create objects on the primary node.
			Transaction tx = begin();
			b1 = newB("b1");
			newB("b2");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();

		{
			Transaction deleteTXNode1 = begin();
			b1.delete();
			KnowledgeAssociation associationNode2;
			String associationType;
			{
				Transaction createAssNode2 = beginNode2();
				KnowledgeObject b1Node2 = getBNode2("b1");
				KnowledgeObject b2Node2 = getBNode2("b2");
				associationNode2 = newAB(b1Node2, b2Node2);
				associationType = associationNode2.tTable().getName();
				createAssNode2.commit();
			}
			try {
				deleteTXNode1.commit();
			} catch (KnowledgeBaseException ex) {
				// expected
				return;
			}
			// ensure Node2 receives changes of node 1
			kbNode2().refetch();
			Collection<KnowledgeAssociation> aliveAssociations = kb().getAllKnowledgeAssociations(associationType);
			assertEmpty("Ticket #7220:", true, aliveAssociations);
			assertFalse("Ticket #7220:", associationNode2.isAlive());
		}
	}

	public void testAutoRefetchUponCommit() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject b3;
		{
			// Create objects on the primary node.
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			newAB(b1, b2);
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();
		// Fetch references on the secondary node.
		KnowledgeObject b1Node2 = getBNode2("b1");
		DestinationIterator b1DestinationsNode2 = new DestinationIterator(b1Node2, AB_NAME);
		assertTrue(b1DestinationsNode2.hasNext());
		KnowledgeObject b2Node2 = b1DestinationsNode2.nextKO();
		assertNotNull(b2Node2);
		KnowledgeObject b3Node2 = getBNode2("b3");
		
		{
			// Change values on the primary node.
			Transaction tx = begin();
			setA2(b1, "update1");
			setA2(b2, "update2");
			b3.delete();
			commit(tx);
		}
		
		// Create a revision node 2.
		{
			Transaction tx = beginNode2();
			newBNode2("b4");
			commitNode2(tx);
		}
		
		// Assert that node 2 is up to date.
		assertEquals("update1", b1Node2.getAttributeValue(A2_NAME));
		assertEquals("update2", b2Node2.getAttributeValue(A2_NAME));
		assertFalse("b3 invalidated on node 2", b3Node2.isAlive());
	}

	public void testFailingCommitAfterConcurrentUpdate() throws DataObjectException {
		KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			setA2(b1, "original value");
			commit(tx);
		}
		
		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject b1Node2 = getBNode2("b1");
		assertNotNull(b1Node2);
		
		{
			// Modify on node1
			Transaction tx = begin();
			setA2(b1, "update on node 1");
			commit(tx);
		}
		
		// On node 2, there is still the original value visible.
		assertEquals("original value", b1Node2.getAttributeValue(A2_NAME));
		
		{
			Transaction tx = beginNode2();
			setA2(b1Node2, "update on node 2");
			commitNode2(tx, "Ticket #3975: Commit must fail due to concurrent change.", true);
		}
	}
	
	public void testFailingCommitAfterConcurrentUpdateLockstep() throws DataObjectException {
		KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			setA2(b1, "original value");
			commit(tx);
		}
		
		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject b1Node2 = getBNode2("b1");
		assertNotNull(b1Node2);
		
		{
			Transaction tx = begin();
			Transaction tx2 = beginNode2();
			setA2(b1, "update on node 1");
			setA2(b1Node2, "update on node 2");
			commit(tx);
			commitNode2(tx2, "Ticket #3975: Commit must fail due to concurrent change.", true);
		}
	}

	public void testUnversionedFailingCommitAfterConcurrentUpdate() throws DataObjectException {
		KnowledgeObject u1;
		{
			Transaction tx = begin();
			u1 = newU("u1");
			setA2(u1, "original value");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject u1Node2 = getUNode2("u1");
		assertNotNull(u1Node2);

		{
			// Modify on node1
			Transaction tx = begin();
			setA2(u1, "update on node 1");
			commit(tx);
		}

		// On node 2, there is still the original value visible.
		assertEquals("original value", u1Node2.getAttributeValue(A2_NAME));
		{
			Transaction tx = beginNode2();
			setA2(u1Node2, "update on node 2");
			commitNode2(tx, "Ticket #14010: Commit must fail due to concurrent change.", true);
		}
	}

	public void testUnversionedFailingCommitAfterConcurrentUpdateLockstep() throws DataObjectException {
		KnowledgeObject u1;
		{
			Transaction tx = begin();
			u1 = newU("u1");
			setA2(u1, "original value");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject u1Node2 = getUNode2("u1");
		assertNotNull(u1Node2);

		{
			Transaction tx = begin();
			Transaction tx2 = beginNode2();
			setA2(u1, "update on node 1");
			setA2(u1Node2, "update on node 2");
			commit(tx);
			commitNode2(tx2, "Ticket #14010: Commit must fail due to concurrent change.", true);
		}
	}

	public void testRefetch() throws DataObjectException, RefetchTimeout {
		KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			setA2(b1, "original value");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject b1Node2 = getBNode2("b1");
		assertNotNull(b1Node2);

		{
			// Modify on node1
			Transaction tx = begin();
			setA2(b1, "update on node 1");
			commit(tx);
		}

		// On node 2, there is still the original value visible.
		assertEquals("original value", b1Node2.getAttributeValue(A2_NAME));

		// After the refetch, the new value has to be visible.
		kbNode2().refetch();
		assertEquals("update on node 1", b1Node2.getAttributeValue(A2_NAME));
	}

	public void testUnversionedRefetch() throws DataObjectException, RefetchTimeout {
		KnowledgeObject u1;
		{
			Transaction tx = begin();
			u1 = newU("u1");
			setA2(u1, "original value");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject u1Node2 = getUNode2("u1");
		assertNotNull(u1Node2);

		{
			// Modify on node1
			Transaction tx = begin();
			setA2(u1, "update on node 1");
			commit(tx);
		}

		// On node 2, there is still the original value visible.
		assertEquals("original value", u1Node2.getAttributeValue(A2_NAME));

		// After the refetch, the new value has to be visible.
		kbNode2().refetch();
		assertEquals(TICKET_14043, "update on node 1", u1Node2.getAttributeValue(A2_NAME));
	}

	public void testRefetchMultipleRevisions() throws DataObjectException, RefetchTimeout {
		KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			setA2(b1, "original value");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject b1Node2 = getBNode2("b1");
		assertNotNull(b1Node2);
		assertEquals("original value", b1Node2.getAttributeValue(A2_NAME));

		{
			// Modify on node1
			Transaction tx1 = begin();
			setA2(b1, "update on node 1");
			commit(tx1);
			// Modify on node1
			Transaction tx2 = begin();
			setA2(b1, "update2 on node 1");
			commit(tx2);
		}

		// On node 2, there is still the original value visible.
		assertEquals("original value", b1Node2.getAttributeValue(A2_NAME));

		// After the refetch, the new value has to be visible.
		kbNode2().refetch();
		assertEquals("update2 on node 1", b1Node2.getAttributeValue(A2_NAME));
	}

	public void test_getObjectByAttribute_RespectsLocalRevision() throws DataObjectException {
		KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			setA2(b1, "original value");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject b1Node2 = getBNode2("b1");
		assertNotNull(b1Node2);

		{
			// Modify on node1
			Transaction tx = begin();
			setA2(b1, "update on node 1");
			commit(tx);
		}

		// On node 2, there is still the original value visible.
		assertEquals("original value", b1Node2.getAttributeValue(A2_NAME));

		/* If the same wrapper is acquired by KnowledgeBase.getObjectByAttribute, it has to be in
		 * the local revision. */
		assertEquals("Ticket #893: ", "original value", getBNode2("b1").getAttributeValue(A2_NAME));
	}

	public void testUnversioned_getObjectByAttribute_RespectsLocalRevision() throws DataObjectException {
		KnowledgeObject u1;
		{
			Transaction tx = begin();
			u1 = newU("u1");
			setA2(u1, "original value");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();
		KnowledgeObject u1Node2 = getUNode2("u1");
		assertNotNull(u1Node2);

		{
			// Modify on node1
			Transaction tx = begin();
			setA2(u1, "update on node 1");
			commit(tx);
		}

		// On node 2, there is still the original value visible.
		assertEquals("original value", u1Node2.getAttributeValue(A2_NAME));

		/* If the same wrapper is acquired by KnowledgeBase.getObjectByAttribute, it has to be in
		 * the local revision. */
		assertEquals("Ticket #893: ", "original value", getUNode2("u1").getAttributeValue(A2_NAME));
	}

	public void testUpdateChain() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		{
			// Create objects on node 1.
			Transaction tx1 = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			newAB(b1, b2);
			commit(tx1);
		}
		
		UpdateChain updates = kb().getUpdateChain();
		assertFalse(updates.next());
		
		// refetch to be able to resolve object
		refetchNode2();

		long r2;
		final KnowledgeObject b1Node2;
		final KnowledgeObject b3Node2;
		final KnowledgeAssociation b1b3Node2;
		{
			// Create more objects on node 2.
			Transaction tx2 = beginNode2();
			b1Node2 = getBNode2("b1");
			b3Node2 = newBNode2("b3");
			b1b3Node2 = newAB(b1Node2, b3Node2);
			commitNode2(tx2);

			r2 = tx2.getCommitRevision().getCommitNumber();
		}
		
		// A fresh update chain has no updates.
		UpdateChain updatesNode2 = kbNode2().getUpdateChain();
		assertFalse(updatesNode2.next());
		
		// Produce a sequence of updates on node 1.
		long r3;
		final KnowledgeObject b4;
		{
			Transaction tx3 = begin();
			b4 = newB("b4");
			commit(tx3);

			r3 = tx3.getCommitRevision().getCommitNumber();
		}
		
		// Up to date after commit.
		assertTrue(updates.next());
		assertCreate(r2, node1Key(b3Node2), updates.getUpdateEvent());
		assertCreate(r2, node1Key(b1b3Node2), updates.getUpdateEvent());
		
		long r4;
		final KnowledgeObject b5;
		{
			Transaction tx4 = begin();
			b5 = newB("b5");
			commit(tx4);

			r4 = tx4.getCommitRevision().getCommitNumber();
		}
		
		final KnowledgeAssociation b4b5;
		{
			Transaction tx5 = begin();
			b4b5 = newAB(b4, b5);
			commit(tx5);
		}
		
		// look for updates on node 2.
		refetchNode2();
		
		// Updates on local node (node that committed the revisions).
		checkUpdates(r3, updates, node1Key(b4), node1Key(b5), node1Key(b4b5));
		
		// Updates on the remove node (node that fetched the remotely committed revisions).
		checkUpdates(r3, updatesNode2, node2Key(b4), node2Key(b5), node2Key(b4b5));
	}

	private void checkUpdates(long commitNumber, UpdateChain updates, ObjectKey b4, ObjectKey b5, ObjectKey b4b5) {
		assertTrue(updates.next());
		assertCreate(commitNumber++, b4, updates.getUpdateEvent());
		
		assertTrue(updates.next());
		assertCreate(commitNumber++, b5, updates.getUpdateEvent());
		
		assertTrue(updates.next());
		assertCreate(commitNumber++, b4b5, updates.getUpdateEvent());
		
		assertFalse(updates.next());
	}
	
	public void testSessionUpdateChain() {
		final AtomicReference<KnowledgeObject> item = new AtomicReference<>();

		long currentRevision = kb().getSessionRevision();
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction modTX = begin();
				item.set(newB("b1"));
				modTX.commit();
			}
		});

		assertEquals(currentRevision, kb().getSessionRevision());

		UpdateChain sessionUpdateChain = kb().getSessionUpdateChain();
		assertTrue(sessionUpdateChain.next());
		assertTrue(sessionUpdateChain.getUpdateEvent().getCreatedObjectKeys().contains(item.get().tId()));
		assertEquals("Exactly on commit was made.", sessionUpdateChain.getRevision(), currentRevision + 1);
		assertEquals("Processing update chain does not update session revision.", currentRevision, kb()
			.getSessionRevision());
	}

	public void testLargeRefetch() throws DataObjectException {
		int objCnt = 4000;
		
		ArrayList<KnowledgeItem> objs = new ArrayList<>();
		{
			Transaction tx = begin();
			for (int n = 0; n < objCnt; n++) {
				objs.add(newB("b" + n));
			}
			commit(tx);
		}
		
		UpdateChain updatesNode2 = kbNode2().getUpdateChain();
		refetchNode2();
		
		assertTrue(updatesNode2.next());
		assertEquals(objCnt, updatesNode2.getUpdateEvent().getCreatedObjectKeys().size());
		
		// Read all new objects in cache on node 2.
		ArrayList<Object> objsNode2 = new ArrayList<>();
		for (Iterator<ObjectKey> it = updatesNode2.getUpdateEvent().getCreatedObjectKeys().iterator(); it.hasNext();) {
			objsNode2.add(kbNode2().resolveObjectKey(it.next()));
		}
		
		{
			// Update all objects on node 1.
			Transaction tx = begin();
			for (int n = 0; n < objCnt; n++) {
				setA2(objs.get(n), "a" + n);
			}
			commit(tx);
		}
		
		refetchNode2();
		
		assertTrue(updatesNode2.next());
		assertEquals(objCnt, updatesNode2.getUpdateEvent().getUpdatedObjectKeys().size());
		
		// Check current values on node 2.
		for (int n = 0; n < objCnt; n++) {
			KnowledgeObject obj = (KnowledgeObject) objs.get(n);
			assertEquals(obj.getAttributeValue(A2_NAME), node2Item(obj).getAttributeValue(A2_NAME));
		}
	}
	
	public void testRefetchEmptyRevision() throws DataObjectException, RefetchTimeout {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		{
			Transaction tx = begin();
			KnowledgeObject b1 = newB("b1");
			KnowledgeObject b2 = newB("b2");
			newAB(b1, b2);
			
			// Regular commit creating revision with three new objects.
			commit(tx);
		}
		
		// Refetch regular revision.
		refetchNode2();
		
		// Create branch revision. This does not insert anything into the refetch table.
		Branch contextBranch = HistoryUtils.getContextBranch();
		Revision lastRevision = HistoryUtils.getLastRevision();
		HistoryUtils.createBranch(contextBranch, lastRevision, null);
		
		// Refetch (empty) branch creation.
		refetchNode2();
		
		{
			Transaction tx = beginNode2();
			newBNode2("b3");
			commitNode2(tx);
		}
		
		kb().refetch();
		
		{
			Transaction tx = begin();
			newB("b4");
			commit(tx);
		}
	}

	/**
	 * Tests that the last revision of the {@link KnowledgeBase} can be used to fetch objects.
	 */
	public void testLastRevision() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject e1 = newE("e1");
		commit(createTX);

		// Increase global commit number
		Transaction trafficTX = beginNode2();
		newBNode2("bNode2");
		commitNode2(trafficTX);

		Revision lastNode1Revision = lastRevision();
		KnowledgeItem stableE1;
		try {
			stableE1 = HistoryUtils.getKnowledgeItem(lastNode1Revision, e1);
		} catch (Exception ex) {
			throw fail("Ticket #8883: It must be possible to stabilze an object by using last revision.", ex);
		}
		assertNotNull(stableE1);
	}
	
	/**
	 * Ticket #26968: Tests that the {@link UpdateEvent} of a remote node can be merged with a
	 * locally changed {@link KBCache} during commit.
	 */
	public void testRefetchWithLocalKBCacheValue() {
		PredicateBasedKBCache testingKBCache = new PredicateBasedKBCache(kb(), B_NAME, item -> true);
		AtomicReference<KnowledgeItem> remoteCreated = new AtomicReference<>();
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		// Fetch value to initialize localCache.
		assertSame(b1, testingKBCache.getValue().get(b1.tId()));

		inThread(() -> {
			Transaction concurrentChange = beginNode2();
			remoteCreated.set(newBNode2("remote b2"));
			concurrentChange.commit();
		});
		tx.commit();
		assertSame(b1, testingKBCache.getValue().get(b1.tId()));
		KnowledgeItem b2 = node1Item(remoteCreated.get());
		assertSame(b2, testingKBCache.getValue().get(b2.tId()));
	}

	/**
	 * When an object is deleted, the {@link KnowledgeBase} is searched for objects referencing the
	 * object to delete. Searching happens in the revision of the deletion session. When
	 * concurrently another session creates a reference, the delete commit must fail.
	 */
	public void testDeleteObjectWithConcurrentCreationOfReferencingItem() {
		Transaction tx = begin();
		KnowledgeObject d1 = newD("d1");
		tx.commit();

		AtomicReference<KnowledgeItem> referrer = new AtomicReference<>();
		inThread(() -> {
			kbNode2().refetch();
			Transaction concurrentChange = beginNode2();
			KnowledgeObject d2 = kbNode2().createKnowledgeObject(D_NAME);

			setA1(d2, "d2");
			d2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, node2Item(d1));
			concurrentChange.commit();
			referrer.set(d2);
		});
		
		try (Transaction deleteTX = begin()){
			d1.delete();
			try {
				deleteTX.commit();
				assertEquals("Precondition for checking consequence of deleting referenced object.",
					DeletionPolicy.CLEAR_REFERENCE,
					MetaObjectUtils.getReference(node1Item(referrer.get()).tTable(), REFERENCE_MONO_CUR_LOCAL_NAME)
						.getDeletionPolicy());
				assertNull(
					"Referenced object was deleted. Either commit has to fail or (in automatic merge mode) the reference has to be cleared.",
					node1Item(referrer.get()).getAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME));
			} catch (MergeConflictException ex) {
				// expected. Commit must not be successful, because the deleting thread has no
				// knowledge about the referrer.
			}
		}
	}

	protected void assertCreate(long commitNumber, ObjectKey objectKey, UpdateEvent updateEvent) {
		assertEquals("Wrong commit number in update event.", commitNumber, updateEvent.getCommitNumber());
		assertTrue(updateEvent.getCreatedObjectKeys().contains(objectKey));
		assertFalse("Created object must not occur in updated objects.",
			updateEvent.getUpdatedObjectKeys().contains(objectKey));
		assertFalse("Created object must not occur in deleted objects.",
			updateEvent.getDeletedObjectKeys().contains(objectKey));
		boolean creationFound = false;
		ObjectBranchId key = ObjectBranchId.toObjectBranchId(objectKey);
		for (ObjectCreation creation : updateEvent.getChanges().getCreations()) {
			if (creation.getObjectId().equals(key)) {
				creationFound = true;
				break;
			}
		}
		assertTrue(creationFound);
	}

	protected KnowledgeObject getBNode2(String a1) {
		return (KnowledgeObject) kbNode2().getObjectByAttribute(B_NAME, A1_NAME, a1);
	}

	protected KnowledgeObject getUNode2(String a1) {
		return (KnowledgeObject) kbNode2().getObjectByAttribute(U_NAME, A1_NAME, a1);
	}

	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestDBKnowledgeBaseCluster.class, "testRefetchWithLocalKBCacheValue");
		}
        return suite(TestDBKnowledgeBaseCluster.class);
    }
	
}
