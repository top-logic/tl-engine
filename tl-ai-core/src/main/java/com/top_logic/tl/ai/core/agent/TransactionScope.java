/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.base.locking.Lock;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;

/**
 * Manages one KB transaction and global edit lock per agent turn.
 *
 * <p>Lifecycle: {@link #acquireLock()} → {@link #begin()} → tool executions →
 * {@link #commit()} or {@link #rollback()}. Lock is released in {@code commit}/{@code rollback}.
 * Implements {@link AutoCloseable} — {@link #close()} rolls back if still active.</p>
 *
 * <p>The {@link #NOOP} instance is for read-only agents and unit tests — all
 * methods are safe no-ops.</p>
 *
 * @see AgentLoop
 * @see ToolContext
 */
public class TransactionScope implements AutoCloseable {

	/** Lock token name used for the global agent edit lock. */
	public static final String LOCK_NAME = "agentEdit";

	/** No-op scope for read-only agents and tests. All methods are safe no-ops. */
	public static final TransactionScope NOOP = new NoOp();

	private Transaction _transaction;

	private Lock _lock;

	private Revision _commitRevision;

	/**
	 * Creates a new live transaction scope.
	 *
	 * <p>Call {@link #acquireLock()} and {@link #begin()} to start the transaction.</p>
	 */
	public TransactionScope() {
		// Live scope — uses PersistencyLayer and LockService at runtime
	}

	/**
	 * Whether this is a no-op scope.
	 */
	public boolean isNoOp() {
		return false;
	}

	/**
	 * Acquire a global exclusive lock for editing.
	 *
	 * @throws TransactionException if the lock cannot be acquired (held by another editor)
	 * @throws IllegalStateException if a lock is already held
	 */
	public void acquireLock() {
		if (_lock != null) {
			throw new IllegalStateException("Lock already held.");
		}
		Lock lock = Lock.createLock(0, LOCK_NAME);
		try {
			lock.lock();
		} catch (Exception e) {
			throw new TransactionException("Cannot acquire edit lock: " + e.getMessage(), e);
		}
		_lock = lock;
	}

	/**
	 * Release the edit lock if held. Idempotent.
	 */
	public void releaseLock() {
		if (_lock != null && _lock.isStateAcquired()) {
			_lock.unlock();
		}
		_lock = null;
	}

	/**
	 * Whether this scope currently holds the edit lock.
	 */
	public boolean holdsLock() {
		return _lock != null && _lock.isStateAcquired();
	}

	/**
	 * Begin a KB transaction.
	 *
	 * @throws IllegalStateException if a transaction is already active
	 */
	public void begin() {
		if (_transaction != null) {
			throw new IllegalStateException("Transaction already active.");
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		_transaction = kb.beginTransaction(ResKey.text("Agent edit"));
	}

	/**
	 * Begin a KB transaction with a custom commit message.
	 *
	 * @param commitMessage the message stored with the revision
	 * @throws IllegalStateException if a transaction is already active
	 */
	public void begin(ResKey commitMessage) {
		if (_transaction != null) {
			throw new IllegalStateException("Transaction already active.");
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		_transaction = kb.beginTransaction(commitMessage);
	}

	/**
	 * Commit the active transaction and release the lock.
	 *
	 * @throws TransactionException if the commit fails (changes are rolled back)
	 * @throws IllegalStateException if no transaction is active
	 */
	public void commit() {
		if (_transaction == null) {
			throw new IllegalStateException("No active transaction to commit.");
		}
		try {
			_transaction.commit();
			_commitRevision = _transaction.getCommitRevision();
			_transaction = null;
			releaseLock();
		} catch (KnowledgeBaseException e) {
			// Leave _transaction and _lock intact so callers can rollback()
			throw new TransactionException("Commit failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Roll back the active transaction and release the lock. Idempotent —
	 * safe to call when no transaction is active.
	 */
	public void rollback() {
		if (_transaction != null) {
			try {
				_transaction.rollback();
			} finally {
				_transaction = null;
				releaseLock();
			}
		}
	}

	/**
	 * Whether a KB transaction is currently active.
	 */
	public boolean isActive() {
		return _transaction != null;
	}

	/**
	 * The revision created by the last successful {@link #commit()}, or {@code null}.
	 */
	public Revision commitRevision() {
		return _commitRevision;
	}

	/**
	 * Rolls back if still active. For use in try-with-resources.
	 */
	@Override
	public void close() {
		rollback();
	}

	/**
	 * No-op implementation for read-only agents and unit tests.
	 */
	private static final class NoOp extends TransactionScope {

		/** */
		@Override
		public boolean isNoOp() {
			return true;
		}

		/** */
		@Override
		public void acquireLock() {
			// no-op
		}

		/** */
		@Override
		public void releaseLock() {
			// no-op
		}

		/** */
		@Override
		public boolean holdsLock() {
			return false;
		}

		/** */
		@Override
		public void begin() {
			// no-op
		}

		/** */
		@Override
		public void begin(ResKey commitMessage) {
			// no-op
		}

		/** */
		@Override
		public void commit() {
			// no-op
		}

		/** */
		@Override
		public void rollback() {
			// no-op
		}

		/** */
		@Override
		public boolean isActive() {
			return false;
		}

		/** */
		@Override
		public Revision commitRevision() {
			return null;
		}

		/** */
		@Override
		public void close() {
			// no-op
		}
	}
}
