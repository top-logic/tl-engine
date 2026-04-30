/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.token.LockInfo;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * Manages one KB transaction and object-level locks per agent turn.
 *
 * <p>Tools acquire locks on their targets via
 * {@link #acquireLock(String, TLObject...)} after resolving the objects they
 * need to modify. The framework manages the transaction lifecycle
 * ({@link #begin()} / {@link #commit()} / {@link #rollback()}) and releases
 * all accumulated locks on commit or rollback.</p>
 *
 * <p>Implements {@link AutoCloseable} — {@link #close()} rolls back if still
 * active.</p>
 *
 * <p>The {@link #NOOP} instance is for read-only agents and unit tests — all
 * methods are safe no-ops.</p>
 *
 * @see AgentLoop
 * @see ToolContext
 */
public class TransactionScope implements AutoCloseable {

	/** No-op scope for read-only agents and tests. All methods are safe no-ops. */
	public static final TransactionScope NOOP = new NoOp();

	private static final int MAX_RETRIES = 3;

	private static final long RETRY_DELAY_MS = 10_000L;

	private static final long LOCK_DURATION_MS = 10 * 60 * 1000L;

	private Transaction _transaction;

	private final Set<Lock> _locks = new LinkedHashSet<>();

	private Revision _commitRevision;

	/**
	 * Creates a new live transaction scope.
	 *
	 * <p>Call {@link #begin()} to start a transaction. Tools acquire locks on
	 * their targets via {@link #acquireLock(String, TLObject...)}.</p>
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
	 * Acquire an object-level lock for the given operation and targets.
	 *
	 * <p>Uses the {@link LockService} to create a lock bound to the target objects,
	 * bridging agent locks with UI locks. Retries up to {@value #MAX_RETRIES} times
	 * with a {@value #RETRY_DELAY_MS}ms delay between attempts.</p>
	 *
	 * @param operation the abstract operation name (e.g. "editValues")
	 * @param targets   the objects to lock
	 * @throws LockConflictException if the lock cannot be acquired after all retries
	 * @throws TransactionException  if lock acquisition is interrupted
	 */
	public void acquireLock(String operation, TLObject... targets) {
		Lock lock = LockService.getInstance().createLock(operation, (Object[]) targets);
		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
			try {
				lock.lock(LOCK_DURATION_MS);
				_locks.add(lock);
				return;
			} catch (Exception e) {
				if (attempt == MAX_RETRIES) {
					String resource = describeTargets(targets);
					String holder = describeLockHolder(targets);
					throw new LockConflictException(resource, holder, MAX_RETRIES, e);
				}
				try {
					Thread.sleep(RETRY_DELAY_MS);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new TransactionException("Lock acquisition interrupted", ie);
				}
			}
		}
	}

	/**
	 * Release all held locks. Idempotent.
	 */
	public void releaseLock() {
		for (Lock lock : _locks) {
			if (lock.isStateAcquired()) {
				lock.unlock();
			}
		}
		_locks.clear();
	}

	/**
	 * Whether this scope currently holds any locks.
	 */
	public boolean holdsLock() {
		for (Lock lock : _locks) {
			if (lock.isStateAcquired()) {
				return true;
			}
		}
		return false;
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
	 * Commit the active transaction and release all locks.
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
			// Leave _transaction and _locks intact so callers can rollback()
			throw new TransactionException("Commit failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Roll back the active transaction and release all locks. Idempotent —
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

	private static String describeTargets(TLObject[] targets) {
		if (targets.length == 0) {
			return "unknown";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < targets.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			if (targets[i] instanceof TLModelPart part) {
				sb.append(TLModelUtil.qualifiedName(part));
			} else {
				sb.append(targets[i].toString());
			}
		}
		return sb.toString();
	}

	private static String describeLockHolder(TLObject[] targets) {
		try {
			TokenService tokenService = TokenService.getInstance();
			for (LockInfo info : tokenService.getAllLocks()) {
				for (Token token : info.getTokens()) {
					for (TLObject target : targets) {
						if (target.equals(token.getObject())) {
							TLObject owner = info.getOwner();
							if (owner instanceof TLNamed named) {
								return named.getName();
							}
							return owner != null ? owner.toString() : "unknown";
						}
					}
				}
			}
		} catch (Exception e) {
			// Best effort — don't fail lock acquisition because of holder lookup
		}
		return "unknown";
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
		public void acquireLock(String operation, TLObject... targets) {
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
