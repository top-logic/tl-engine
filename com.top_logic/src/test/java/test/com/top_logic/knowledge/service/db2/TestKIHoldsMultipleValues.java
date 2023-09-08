/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.lang.ref.WeakReference;

import junit.framework.Test;

import test.com.top_logic.KBTestUtils;

import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DeletedObjectAccess;
import com.top_logic.knowledge.service.db2.UpdateChainLink;

/**
 * Special test class testing that {@link KnowledgeItem} holds data for multiple revisions.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings({ "javadoc" })
public class TestKIHoldsMultipleValues extends AbstractDBKnowledgeBaseClusterTest {

	public void testAccessNewerData() throws DataObjectException {
		Transaction createTX = begin();
		final String createA1 = "E1";
		final KnowledgeObject e1 = newE(createA1);
		commit(createTX);
		refetchNode2();
		final UpdateChainLink createRev = getLastSessionRevision(kbNode2());

		Transaction change1TX = begin();
		final String change1A1 = "new E1";
		e1.setAttributeValue(A1_NAME, change1A1);
		commit(change1TX);
		refetchNode2();
		final UpdateChainLink change1Rev = getLastSessionRevision(kbNode2());

		Transaction change2TX = begin();
		final String change2A1 = "newer E1";
		e1.setAttributeValue(A1_NAME, change2A1);
		commit(change2TX);
		refetchNode2();
		final UpdateChainLink change2Rev = getLastSessionRevision(kbNode2());

		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				ObjectKey e1Key = node2Key(e1);
				checkTestAccessNewerData(e1Key, createRev, createA1, change1Rev, change1A1, change2Rev, change2A1);
				checkTestAccessNewerData(e1Key, createRev, createA1, change2Rev, change2A1, change1Rev, change1A1);

				checkTestAccessNewerData(e1Key, change1Rev, change1A1, change2Rev, change2A1, createRev, createA1);
				checkTestAccessNewerData(e1Key, change1Rev, change1A1, createRev, createA1, change2Rev, change2A1);

				checkTestAccessNewerData(e1Key, change2Rev, change2A1, createRev, createA1, change1Rev, change1A1);
				checkTestAccessNewerData(e1Key, change2Rev, change2A1, change1Rev, change1A1, createRev, createA1);
			}
		});
	}

	void checkTestAccessNewerData(ObjectKey key, UpdateChainLink rev1, Object val1, UpdateChainLink rev2, Object val2,
			UpdateChainLink rev3, Object val3) throws NoSuchAttributeException {
		/* Test checks that after initial loading of values, other values can be accessed. The
		 * initial values may be old values. Therefore the object must not yet be loaded. */
		KBTestUtils.clearCache(kbNode2());
		assertTrue(isNotLoaded(kbNode2(), key));

		updateSessionRevisionNode2(rev1);
		KnowledgeItem node2Item = kbNode2().resolveObjectKey(key);
		assertEquals(val1, node2Item.getAttributeValue(A1_NAME));

		updateSessionRevisionNode2(rev2);
		assertEquals(val2, node2Item.getAttributeValue(A1_NAME));

		updateSessionRevisionNode2(rev3);
		assertEquals(val3, node2Item.getAttributeValue(A1_NAME));
	}

	public void testResolveDeletedObject() throws DataObjectException {
		Transaction createTX = begin();
		final String a1Value = "E1";
		KnowledgeObject e1 = newE(a1Value);
		commit(createTX);
		final ObjectKey e1Key = e1.tId();
		refetchNode2();
		final UpdateChainLink createRev = getLastSessionRevision(kbNode2());

		Transaction deleteTX = begin();
		e1.delete();
		commit(deleteTX);
		refetchNode2();
		final UpdateChainLink delRev = getLastSessionRevision(kbNode2());

		Transaction resurrectTX = begin();
		final String newA1Value = "new E1";
		final KnowledgeItem newE1 =
			kb().createKnowledgeItem(kb().getBranch(e1Key.getBranchContext()), e1Key.getObjectName(),
			e1Key.getObjectType().getName(), new KeyValueBuffer<String, Object>().put(A1_NAME, newA1Value));
		commit(resurrectTX);
		refetchNode2();
		final UpdateChainLink resurrectRev = getLastSessionRevision(kbNode2());

		refetchNode2();
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				ObjectKey newE1Key = node2Key(newE1);
				checkTestResolveDeletedObject(newE1Key, createRev, a1Value, delRev, null, resurrectRev, newA1Value);
				checkTestResolveDeletedObject(newE1Key, createRev, a1Value, resurrectRev, newA1Value, delRev, null);

				checkTestResolveDeletedObject(newE1Key, delRev, null, resurrectRev, newA1Value, createRev, a1Value);
				checkTestResolveDeletedObject(newE1Key, delRev, null, createRev, a1Value, resurrectRev, newA1Value);

				checkTestResolveDeletedObject(newE1Key, resurrectRev, newA1Value, createRev, a1Value, delRev, null);
				checkTestResolveDeletedObject(newE1Key, resurrectRev, newA1Value, delRev, null, createRev, a1Value);
			}
		});

	}

	void checkTestResolveDeletedObject(ObjectKey key, UpdateChainLink rev1, Object val1, UpdateChainLink rev2,
			Object val2, UpdateChainLink rev3, Object val3) throws NoSuchAttributeException {
		/* Test checks that after initial loading of values, other values can be accessed. The
		 * initial values may be old values. Therefore the object must not yet be loaded. */
		KBTestUtils.clearCache(kbNode2());
		assertTrue(isNotLoaded(kbNode2(), key));

		checkValueInRevision(key, rev1, val1);
		checkValueInRevision(key, rev2, val2);
		checkValueInRevision(key, rev3, val3);
	}

	private void checkValueInRevision(ObjectKey key, UpdateChainLink rev, Object val) throws NoSuchAttributeException {
		updateSessionRevisionNode2(rev);
		KnowledgeItem item = kbNode2().resolveObjectKey(key);
		if (val == null) {
			assertNull(item);
		} else {
			assertEquals(val, item.getAttributeValue(A1_NAME));
		}
	}

	public void testAccessDeletedObject() throws DataObjectException {
		Transaction createTX = begin();
		final String a1Value = "E1";
		KnowledgeObject e1 = newE(a1Value);
		commit(createTX);
		final UpdateChainLink createRev = getLastSessionRevision(kb());
		final ObjectKey e1Key = e1.tId();

		Transaction deleteTX = begin();
		e1.delete();
		commit(deleteTX);

		try {
			e1.getAttributeValue(A1_NAME);
			fail(e1Key + " was deleted.");
		} catch (DeletedObjectAccess ex) {
			// expected
		}

		updateSessionRevision(createRev);
		KnowledgeItem oldE1 = kb().resolveObjectKey(e1Key);
		assertNotNull("Item is resolved in revision in which ist existed.", oldE1);
		assertEquals(a1Value, oldE1.getAttributeValue(A1_NAME));
		
	}

	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestCleanup() throws DataObjectException {
		Transaction createTX = begin();
		final String a1Value = "E1";
		KnowledgeObject e1 = newE(a1Value);
		// create new key to ensure old key can be removed
		ObjectKey e1Key =
			KBUtils.createObjectKey(e1.getBranchContext(), e1.getHistoryContext(), e1.tTable(),
				e1.getObjectName());
		commit(createTX);
		UpdateChainLink sessionRevision = getLastSessionRevision(kb());

		Transaction deleteTX = begin();
		e1.delete();
		WeakReference<KnowledgeObject> refToE1 = new WeakReference<>(e1);
		// remove explicit reference to removed e1
		e1 = null;
		commit(deleteTX);
		StopWatch sw = StopWatch.createStartedWatch();
		while (sw.getElapsedMillis() < 10000) {
			// pins the removed e1 implicit.
			assertNotNull(sessionRevision);
			provokeOutOfMemory();
			assertNotNull(refToE1.get());
		}
		sw.reset();

		while (sessionRevision != null) {
			sessionRevision = sessionRevision.getNextUpdate();
		}

		sw.start();
		while (sw.getElapsedMillis() < 10000) {
			provokeOutOfMemory();
			if (refToE1.get() == null) {
				break;
			}
		}
		assertNull("e1 not finalized yet.", refToE1.get());
		assertEquals("Original key of e1 was not removed from cache.", e1Key, kb().getCachedKey(e1Key));

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestKIHoldsMultipleValues}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (!true) {
			String testName = "testAccessDeletedObject";
			return runOneTest(TestKIHoldsMultipleValues.class, testName);
		}
		return suite(TestKIHoldsMultipleValues.class);
	}

}

