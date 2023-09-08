/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.UpdateChainLink;

/**
 * Test for recreating objects that were deleted before.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestReviveDeletedItems extends AbstractDBKnowledgeBaseClusterTest {

	public void testReviveItem() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject b1 = newB("b1");
		ObjectKey b1Key = copyKey(b1.tId());
		commit(createTX);

		// pin to ensure former deleted object is not cleared.
		UpdateChainLink lastSessionRevision = getLastSessionRevision(kb());
		assertNotNull(lastSessionRevision);

		Transaction deleteTX = begin();
		b1.delete();
		commit(deleteTX);

		Transaction reviveTX = begin();
		KnowledgeItem revivedItem = createItem(kb(), b1Key, initialValues(A1_NAME, "b2"));
		commit(reviveTX);
		assertSame(revivedItem, kb().resolveObjectKey(b1Key));
	}
	
	public void testReviveMultipleTimes() throws DataObjectException {
		Object[] result = setup();

		ObjectKey key = copyKey(((KnowledgeItem) result[0]).tId());
		updateSessionRevision((UpdateChainLink) result[1]);
		assertSame(result[0], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[2]);
		assertSame(null, kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[4]);
		assertSame(result[3], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[5]);
		assertSame(null, kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[7]);
		assertSame(result[6], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[8]);
		assertSame(null, kb().resolveObjectKey(key));
	}

	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestReviveReleaseFirstReference() throws DataObjectException {
		Object[] result = setup();
		result[0] = null;
		result[1] = null;
		result[2] = null;
		provokeOutOfMemory();

		ObjectKey key = copyKey(((KnowledgeItem) result[3]).tId());
		updateSessionRevision((UpdateChainLink) result[4]);
		assertSame(result[3], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[5]);
		assertSame(null, kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[7]);
		assertSame(result[6], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[8]);
		assertSame(null, kb().resolveObjectKey(key));
	}

	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestReviveReleaseMiddleReference() throws DataObjectException {
		Object[] result = setup();
		result[3] = null;
		result[4] = null;
		result[5] = null;
		provokeOutOfMemory();

		ObjectKey key = copyKey(((KnowledgeItem) result[0]).tId());
		updateSessionRevision((UpdateChainLink) result[1]);
		assertSame(result[0], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[2]);
		assertSame(null, kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[7]);
		assertSame(result[6], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[8]);
		assertSame(null, kb().resolveObjectKey(key));
	}

	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestReviveReleaseLastReference() throws DataObjectException {
		Object[] result = setup();
		result[6] = null;
		result[7] = null;
		result[8] = null;
		provokeOutOfMemory();

		ObjectKey key = copyKey(((KnowledgeItem) result[0]).tId());
		updateSessionRevision((UpdateChainLink) result[1]);
		assertSame(result[0], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[2]);
		assertSame(null, kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[4]);
		assertSame(result[3], kb().resolveObjectKey(key));
		updateSessionRevision((UpdateChainLink) result[5]);
		assertSame(null, kb().resolveObjectKey(key));
	}

	private Object[] setup() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeItem b1 = newB("b1");
		final ObjectKey b1Key = copyKey(b1.tId());
		commit(createTX);
		UpdateChainLink session1 = getLastSessionRevision(kb());

		Transaction deleteTX = begin();
		b1.delete();
		commit(deleteTX);
		UpdateChainLink session2 = getLastSessionRevision(kb());

		Transaction reviveTX = begin();
		KnowledgeItem b2 = createItem(kb(), b1Key, initialValues(A1_NAME, "b2"));
		commit(reviveTX);
		UpdateChainLink session3 = getLastSessionRevision(kb());

		Transaction delete2TX = begin();
		b2.delete();
		commit(delete2TX);
		UpdateChainLink session4 = getLastSessionRevision(kb());

		Transaction revive2TX = begin();
		KnowledgeItem b3 = createItem(kb(), b1Key, initialValues(A1_NAME, "b3"));
		commit(revive2TX);
		UpdateChainLink session5 = getLastSessionRevision(kb());

		Transaction delete3TX = begin();
		b3.delete();
		commit(delete3TX);
		UpdateChainLink session6 = getLastSessionRevision(kb());

		Object[] result = new Object[9];
		result[0] = b1;
		result[1] = session1;
		result[2] = session2;
		result[3] = b2;
		result[4] = session3;
		result[5] = session4;
		result[6] = b3;
		result[7] = session5;
		result[8] = session6;
		return result;
	}

	public void testReviveWithIntermediateChange() throws DataObjectException {
		UpdateChainLink session0 = getLastSessionRevision(kb());

		Transaction createTX = begin();
		KnowledgeItem b1 = newB("b1");
		KnowledgeItem otherB1 = newB("b1");
		ObjectKey b1Key = copyKey(b1.tId());
		commit(createTX);
		UpdateChainLink session1 = getLastSessionRevision(kb());

		Transaction deleteTX = begin();
		b1.delete();
		commit(deleteTX);
		UpdateChainLink session2 = getLastSessionRevision(kb());

		Transaction changeTX = begin();
		otherB1.setAttributeValue(A1_NAME, "b1_new");
		commit(changeTX);
		UpdateChainLink session3 = getLastSessionRevision(kb());

		Transaction reviveTX = begin();
		KnowledgeItem b2 = createItem(kb(), b1Key, initialValues(A1_NAME, "b2"));
		commit(reviveTX);
		UpdateChainLink session4 = getLastSessionRevision(kb());

		Transaction deleteRevivedTX = begin();
		b2.delete();
		commit(deleteRevivedTX);
		UpdateChainLink session5 = getLastSessionRevision(kb());

		Transaction change2TX = begin();
		otherB1.setAttributeValue(A1_NAME, "b1_newer");
		commit(change2TX);
		UpdateChainLink session6 = getLastSessionRevision(kb());

		updateSessionRevision(session0);
		assertSame(null, kb().resolveObjectKey(b1Key));

		updateSessionRevision(session1);
		assertSame(b1, kb().resolveObjectKey(b1Key));

		updateSessionRevision(session2);
		assertSame(null, kb().resolveObjectKey(b1Key));

		updateSessionRevision(session3);
		assertSame(null, kb().resolveObjectKey(b1Key));

		updateSessionRevision(session4);
		assertSame(b2, kb().resolveObjectKey(b1Key));

		updateSessionRevision(session5);
		assertSame(null, kb().resolveObjectKey(b1Key));

		updateSessionRevision(session6);
		assertSame(null, kb().resolveObjectKey(b1Key));

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestReviveDeletedItems}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			String testName = "";
			return runOneTest(TestReviveDeletedItems.class, testName);
		}
		return suite(TestReviveDeletedItems.class);
	}
}

