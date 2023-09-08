/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.util.Arrays;
import java.util.Map.Entry;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.message.Message;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContextManager;

/**
 * Base class for test cases testing the persistency layer.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractKnowledgeBaseTest extends BasicTestCase {

	public KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	public Transaction begin() {
		return begin(null);
	}
	
	public Transaction begin(Message modificationDescription) {
		return kb().beginTransaction(modificationDescription);
	}

	public void commit(Transaction tx) {
		commit(tx, null);
	}
	
	public void commit(Transaction tx, String testDescription) {
		commit(tx, testDescription, false);
	}

	
	public void commit(Transaction tx, boolean expectFailure) {
		commit(tx, null, expectFailure);
	}
	
	public void commit(Transaction tx, String testDescription, boolean expectFailure) {
		assertSame(kb(), tx.getKnowledgeBase()); 
		internalCommitTx(tx, testDescription, expectFailure);
	}

	protected static void internalCommitTx(Transaction tx, String testDescription, boolean expectFailure) {
		try {
			tx.commit();
			
			if (expectFailure) {
				fail("Commit succeeded, which should fail" + (testDescription != null ? " for '" + testDescription + "'" : "") + ".");
			}
		} catch (KnowledgeBaseException ex) {
			if (! expectFailure) {
				fail("Commit failed, which should complete normally" + (testDescription != null ? " for '" + testDescription + "'" : "") + ".", ex);
			}
		}
	}
	
	protected void rollback(Transaction tx) {
		assertSame(kb(), tx.getKnowledgeBase()); 
		tx.rollback(null);
	}	
	
	protected CommitHandler commitHandler() {
		return (CommitHandler) kb();
	}
	
	protected static Revision lastUpdate(KnowledgeItem item) {
		return HistoryUtils.getLastUpdate(item);
	}

	protected static Revision revison(KnowledgeItem item) {
		return HistoryUtils.getRevision(item);
	}
	
	protected ObjectReference itemIdentity(final KnowledgeItem item) {
		return HistoryUtils.getItemIdentity(item, item);
	}

	/**
	 * Returns TRUNK of the current {@link KnowledgeBase} given by {@link #kb()}
	 */
	protected Branch trunk() {
		return HistoryUtils.getHistoryManager(kb()).getTrunk();
	}

	/**
	 * Returns the last {@link Revision} of the current {@link KnowledgeBase} given by {@link #kb()}
	 */
	protected Revision lastRevision() {
		final HistoryManager historyManager = HistoryUtils.getHistoryManager(kb());
		return historyManager.getRevision(historyManager.getLastRevision());
	}

	/**
	 * Updates the revision of the current thread in {@link #kb()} to its current revision.
	 */
	protected void updateSessionRevision() throws MergeConflictException {
		HistoryManager hm = kb().getHistoryManager();
		hm.updateSessionRevision();
	}

	/**
	 * Updates the revision of the current thread in {@link #kb()} to the given revision.
	 */
	protected void updateSessionRevision(UpdateChainLink revision) {
		updateSessionRevision(kb().getHistoryManager(), revision);
	}

	/**
	 * Returns the last {@link UpdateChainLink} of the KnowledgeBase.
	 */
	protected UpdateChainLink getLastSessionRevision(KnowledgeBase kb) {
		UpdateChain updateChain = kb.getUpdateChain();
		return ReflectionUtils.getValue(updateChain, "current", UpdateChainLink.class);
	}

	/**
	 * Updates the session revision of the given {@link HistoryManager}
	 */
	protected void updateSessionRevision(HistoryManager hm, UpdateChainLink newSessionRevision) {
		HistoryUtils.updateSessionAndInteractionRevision(hm, TLContextManager.getSubSession(), newSessionRevision);
	}

	/**
	 * Returns the given object in the given revision, or <code>null</code> when it did not exists
	 * in that revision.
	 */
	protected <T extends TLObject> T inRevision(Revision revision, T object) {
		KnowledgeItem item = inRevision(revision, object.tHandle());
		if (item == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		T result = (T) item.getWrapper();
		return result;
	}

	/**
	 * Returns the given object in the given revision, or <code>null</code> when it did not exists
	 * in that revision.
	 */
	protected KnowledgeItem inRevision(Revision revision, KnowledgeItem object) {
		return object.getKnowledgeBase().resolveObjectKey(inRevision(revision, object.tId()));
	}

	/**
	 * Returns an object key with the given history context. Other values are taken from the given
	 * key.
	 */
	protected ObjectKey inRevision(Revision revision, ObjectKey key) {
		return new DefaultObjectKey(key.getBranchContext(), revision.getCommitNumber(), key.getObjectType(),
			key.getObjectName());
	}

	/**
	 * Creates a new {@link ObjectKey} with the same informations as the given one.
	 * 
	 * @param key
	 *        The key to copy.
	 * @return A new key with same informations.
	 */
	protected ObjectKey copyKey(ObjectKey key) {
		TLID objectName = key.getObjectName();
		MetaObject objectType = key.getObjectType();
		long historyContext = key.getHistoryContext();
		long branchContext = key.getBranchContext();
		return new DefaultObjectKey(branchContext, historyContext, objectType, objectName);
	}

	/**
	 * Creates an object defined by the given key and the given initial values.
	 * 
	 * @see #initialValues(Object...) Method to create initial values.
	 */
	protected KnowledgeItem createItem(KnowledgeBase kb, ObjectKey key, Iterable<Entry<String, Object>> initialValues)
			throws DataObjectException {
		Branch branch = kb.getHistoryManager().getBranch(key.getBranchContext());
		TLID objectName = key.getObjectName();
		String typeName = key.getObjectType().getName();
		return kb.createKnowledgeItem(branch, objectName, typeName, initialValues, KnowledgeItem.class);
	}

	/**
	 * Transforms a chain of "String, Object, ...., String, Object" to an corresponding sequence of
	 * {@link Entry}s.
	 */
	protected Iterable<Entry<String, Object>> initialValues(Object... keyValuePairs) {
		if (keyValuePairs.length % 2 != 0) {
			throw new IllegalArgumentException("Need pairs String, Object, String, Object, ...; got "
				+ Arrays.toString(keyValuePairs));
		}
		NameValueBuffer buffer = new NameValueBuffer(keyValuePairs.length / 2);
		int i = 0;
		while (i < keyValuePairs.length) {
			Object attributeName = keyValuePairs[i++];
			if (!(attributeName instanceof String)) {
				throw new IllegalArgumentException("Need pairs String, Object, String, Object, ...; got "
					+ Arrays.toString(keyValuePairs));
			}
			Object value = keyValuePairs[i++];
			buffer.setValue((String) attributeName, value);
		}
		return buffer;
	}

}
