/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.IdentityHashSet;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Messages;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.merge.MergeConflictException;

/**
 * {@link DefaultDBContext} for optimized replay of {@link KnowledgeEvent}s with
 * original revision meta information.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class ReplayContext extends DefaultDBContext {
	private long expectedCommitNumber;

	private String author;

	private String logMessage;

	private long lastRevisionDate;

	private TransactionImpl outermostTX;

	private long commitStartTime;

	public ReplayContext(DBKnowledgeBase kb, String anUpdater, long firstCommitNumber) {
		super(kb, anUpdater);
		
		this.expectedCommitNumber = firstCommitNumber;
	}
	
	/**
	 * the number of the revision which is produced during next commit.
	 */
	public long getNextCommitNumber() {
		return expectedCommitNumber;
	}

	public void setUpdateCommitNumber(long nextRevision) {
		if (hasCommitConnection()) {
			throw new IllegalStateException("An revision update may only be requested before replay has effectively started.");
		}
		this.expectedCommitNumber = nextRevision;
	}

	public void setCommitNumber(long commitNumber) {
		if (this.expectedCommitNumber != commitNumber) {
			throw new IllegalArgumentException("Expected next commit number '" + this.expectedCommitNumber + "', but was '" + commitNumber + "'.");
		}
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	@Override
	protected TransactionImpl initFirstTX(boolean autoBegin, boolean anonymous, Message commitMessage) {
		assert outermostTX == null;
		outermostTX = new TransactionImpl(this, false, null);
		if (!autoBegin) {
			return outermostTX.nest(anonymous, commitMessage);
		} else {
			return outermostTX;
		}
	}

	void beginReplay() {
		/* getting connection initialises the connection, which ensures that a new commit occurs. */
		internalGetConnection();
		assert hasChanges() : "hasChanges is used to decide whether a new commit must be done";

		// ensure that a transaction has began
		beginAuto();
	}


	Transaction resetReplayTransaction() {
		Transaction result = outermostTX;
		outermostTX = null;
		return result;
	}

	public void setDate(long revisionDate) {
		this.lastRevisionDate = revisionDate;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	@Override
	protected long getCommitStartTime() {
		return this.commitStartTime;
	}

	@Override
	protected RevisionImpl createRevision(PooledConnection commitConnection) throws SQLException,
			MergeConflictException, RefetchTimeout {
		this.commitStartTime = System.currentTimeMillis();
		RevisionImpl newRevision;
		if (expectedCommitNumber == Revision.FIRST_REV) {
			/* There is already a first revision in the database, because it is created during
			 * startup. */
			newRevision = (RevisionImpl) kb.getRevision(expectedCommitNumber);
		} else {
			newRevision =
				kb.internalCreateRevision(expectedCommitNumber, getUpdater(), lastRevisionDate, Messages.SYNTHESIZED_COMMIT_DURING_REPLAY);
		}
		return newRevision;
	}

	@Override
	protected void dbCommit(PooledConnection commitConnection) throws SQLException {
		RevisionImpl newRevision = getNewRevision();

		long currentCommitNumber = newRevision.getCommitNumber();
		assert currentCommitNumber == this.expectedCommitNumber : "Expected commit number did change unexpectedly.";

		// Fill with information from last commit event, which was missing at revision creation
		// time.
		newRevision.initValues(currentCommitNumber, this.author, this.lastRevisionDate, this.logMessage);
		DBAccess dbAccess = kb.getDBAccess((MOKnowledgeItem) newRevision.tTable());
		if (expectedCommitNumber == Revision.FIRST_REV) {
			/* Update known revision. */
			((RevisionDBAccess) dbAccess).updateFirstRevision(commitConnection, newRevision);
		} else {
			// Insert revision to the database.
			dbAccess.insert(commitConnection, currentCommitNumber, newRevision);
		}
		
		// Expect commit with next higher commit number next.
		this.expectedCommitNumber++;
		
		super.dbCommit(commitConnection);
	}

	@Override
	protected void insertNewRevision(RevisionImpl revision) {
		if (revision.getCommitNumber() == Revision.FIRST_REV) {
			/* For the first revision the revision object from the KnowledgeBase is taken; it must
			 * not be reinserted into the KnowledgeBase. */
		} else {
			super.insertNewRevision(revision);
		}
	}

	@Override
	Revision commitTransaction(TransactionImpl commitTransaction) throws KnowledgeBaseException {
		try {
			return super.commitTransaction(commitTransaction);
		} finally {
			// reinstall context as it is removed in dropContext.
			kb.installContext(this);
		}
	}

	@Override
	boolean rollbackComplete(boolean success) {
		try {
			return super.rollbackComplete(success);
		} finally {
			// reinstall context as it is removed in dropContext.
			kb.installContext(this);
		}
	}

	@Override
	protected void dropContext(boolean success) {
		kb.dropDBContext();
		// Do not close context, because it is reused.
		// close(success);
	}
	
	@Override
	protected UpdateEvent createUpdateEvent() {
		// Note: Association caches are updated at the end of the commit.
		// Therefore, even during replay, events must be sent. See Ticket #2765.
		return super.createUpdateEvent();
	}



	@Override
	protected <E> IdentityHashSet<E> cleanupCommittables(IdentityHashSet<E> committables) {
		return clearSet(committables);
	}

	@Override
	protected <E> IdentityHashSet<E> cleanupCommittablesDeleted(IdentityHashSet<E> committablesDeleted) {
		return clearSet(committablesDeleted);
	}

	@Override
	protected <K, V> HashMap<K, V> cleanupChangedObjectsById(HashMap<K, V> changedObjectsById) {
		return clearMap(changedObjectsById);
	}

	@Override
	protected <K, V> HashMap<K, V> cleanupNewObjectsById(HashMap<K, V> newObjectsById) {
		return clearMap(newObjectsById);
	}

	@Override
	protected <K, V> HashMap<K, V> cleanupRemovedObjectsById(HashMap<K, V> removedObjectsById) {
		return clearMap(removedObjectsById);
	}

	@Override
	protected <K, V> Map<K, V> cleanupAssociationChangesByBaseId(Map<K, V> associationChangesByBaseId) {
		associationChangesByBaseId.clear();
		return associationChangesByBaseId;
	}

	@Override
	protected <K, V> Map<K, V> cleanupLocalCaches(Map<K, V> localCaches) {
		localCaches.clear();
		return localCaches;
	}

	@Override
	protected <K, V> Map<K, V> cleanupDynamicChanges(Map<K, V> dynamicChanges) {
		dynamicChanges.clear();
		return dynamicChanges; 
	}

	@Override
	protected <K, V> Map<K, V> cleanupLocalChanges(Map<K, V> localChanges) {
		localChanges.clear();
		return localChanges; 
	}

	private <K, V> HashMap<K, V> clearMap(HashMap<K, V> changedObjectsById) {
		changedObjectsById.clear();
		return changedObjectsById;
	}

	private <E> IdentityHashSet<E> clearSet(IdentityHashSet<E> committables) {
		committables.clear();
		return committables;
	}

}