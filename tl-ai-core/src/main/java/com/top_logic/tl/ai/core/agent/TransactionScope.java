/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.tools.model.LockHolderLookup;
import com.top_logic.tl.ai.tool.api.CancellationToken;

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

	/** Sleep slice for lock-retry waits, so cancellation is noticed promptly. */
	private static final long RETRY_POLL_MS = 250L;

	private Transaction _transaction;

	private final Set<Lock> _locks = new LinkedHashSet<>();

	private Revision _commitRevision;

	private final CancellationToken _cancellationToken;

	/**
	 * Creates a new live transaction scope.
	 *
	 * <p>Call {@link #begin()} to start a transaction. Tools acquire locks on
	 * their targets via {@link #acquireLock(String, TLObject...)}.</p>
	 */
	public TransactionScope() {
		this(CancellationToken.UNCANCELLABLE);
	}

	/**
	 * Creates a new live transaction scope that observes the given cancellation token.
	 *
	 * <p>Lock-retry waits in {@link #acquireLock(String, TLObject...)} end promptly when
	 * the associated run is cancelled.</p>
	 *
	 * @param cancellationToken the token cancelled when the agent run is cancelled
	 */
	public TransactionScope(CancellationToken cancellationToken) {
		_cancellationToken =
			Objects.requireNonNull(cancellationToken, "Cancellation token must not be null.");
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
	 * @throws TransactionException  if lock acquisition is interrupted or cancelled
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
					long totalWaitSecs = (MAX_RETRIES - 1) * RETRY_DELAY_MS / 1000;
					throw new LockConflictException(resource, holder, MAX_RETRIES, totalWaitSecs, e);
				}
				waitBeforeRetry();
			}
		}
	}

	/**
	 * Sleep {@value #RETRY_DELAY_MS}ms in {@value #RETRY_POLL_MS}ms slices, re-checking
	 * cancellation between slices so a cancelled run does not sit out the full delay.
	 */
	private void waitBeforeRetry() {
		long deadline = System.nanoTime() + RETRY_DELAY_MS * 1_000_000L;
		while (true) {
			if (_cancellationToken.isCancelled()) {
				throw new TransactionException("Lock acquisition cancelled");
			}
			long remainingMs = (deadline - System.nanoTime()) / 1_000_000L;
			if (remainingMs <= 0) {
				return;
			}
			try {
				Thread.sleep(Math.min(RETRY_POLL_MS, remainingMs));
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				throw new TransactionException("Lock acquisition interrupted", ie);
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
		List<String> labels = new ArrayList<>(targets.length);
		for (TLObject target : targets) {
			if (target instanceof TLModelPart part) {
				labels.add(TLModelUtil.qualifiedName(part));
			} else {
				labels.add(target.toString());
			}
		}
		return StringServices.join(labels, ", ");
	}

	private static String describeLockHolder(TLObject[] targets) {
		for (TLObject target : targets) {
			String holder = LockHolderLookup.findLockHolder(target);
			if (holder != null) {
				return holder;
			}
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
