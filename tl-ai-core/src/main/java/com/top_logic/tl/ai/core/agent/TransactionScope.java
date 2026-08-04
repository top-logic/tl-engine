/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

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
import com.top_logic.util.Resources;

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

	/**
	 * The one place a failed commit is announced.
	 *
	 * <p>
	 * Callers pass {@link TransactionException#getMessage()} on unchanged rather than prefixing it a
	 * second time - the doubled {@code "Commit failed: Commit failed: ..."} an agent used to report
	 * was exactly that.
	 * </p>
	 */
	static final String COMMIT_FAILED_PREFIX = "Commit failed: ";

	/** How the engine marks an encoded {@link ResKey} it embedded in an exception message. */
	private static final String ENCODED_RES_KEY_PREFIX = "msg:";

	/** A {@code {0}}-style placeholder a resolved message left standing. */
	private static final Pattern UNFILLED_PLACEHOLDER = Pattern.compile("\\{\\d+\\}");

	private static final int MAX_RETRIES = AgentTuning.LOCK_MAX_RETRIES;

	private static final long RETRY_DELAY_MS = AgentTuning.LOCK_RETRY_DELAY_MS;

	private static final long LOCK_DURATION_MS = AgentTuning.LOCK_DURATION_MS;

	/** Sleep slice for lock-retry waits, so cancellation is noticed promptly. */
	private static final long RETRY_POLL_MS = AgentTuning.BACKOFF_POLL_MS;

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
	 * The cancellation token this scope's lock-retry waits observe.
	 *
	 * <p>Pair a {@link ToolContext} built from this scope with the same token (see the
	 * {@link ToolContext} constructors that accept a {@link TransactionScope} without an
	 * explicit token — they derive it from here) so cancelling one always reaches the other.</p>
	 */
	public CancellationToken cancellationToken() {
		return _cancellationToken;
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
		if (_cancellationToken.sleepCancellably(RETRY_DELAY_MS, RETRY_POLL_MS)) {
			if (Thread.currentThread().isInterrupted()) {
				throw new TransactionException("Lock acquisition interrupted");
			}
			throw new TransactionException("Lock acquisition cancelled");
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
			throw new TransactionException(COMMIT_FAILED_PREFIX + describeCommitFailure(e), e);
		}
	}

	/**
	 * The human-readable reason a commit failed.
	 *
	 * <p>
	 * A transaction that was poisoned - typically by a nested transaction of a tool rolling back -
	 * fails with an <em>encoded</em> {@link ResKey} in its message rather than with a sentence: the
	 * engine writes {@code "msg:" + ResKey.encode(reason)}. Passing that on verbatim puts
	 * {@code msg:class.com.top_logic....} in front of the model and of the user, where it says
	 * nothing at all, so it is resolved here - in the acting user's locale, falling back to the
	 * deployment's default.
	 * </p>
	 */
	static String describeCommitFailure(KnowledgeBaseException failure) {
		String message = failure.getMessage();
		if (message == null || !message.startsWith(ENCODED_RES_KEY_PREFIX)) {
			return message;
		}
		ResKey reason = ResKey.decode(message.substring(ENCODED_RES_KEY_PREFIX.length()));
		Resources resources = Resources.getInstance();
		String resolved = resources.getString(reason);
		if (UNFILLED_PLACEHOLDER.matcher(resolved).find()) {
			return describeArguments(resources, reason);
		}
		return resolved;
	}

	/**
	 * What a {@link ResKey} was filled with, for a key whose own text cannot render it.
	 *
	 * <p>
	 * The reason a nested rollback poisons its outer transaction with is
	 * {@code NESTED_TRANSACTION_ROLLED_BACK__COMMIT_REASON}, whose translations number their
	 * placeholders from one ({@code "Nested transaction {1} rolled back: {2}"}) while the arguments
	 * are counted from zero. Resolving it therefore yields a sentence with a literal <code>{2}</code>
	 * in it and the actual reason in the wrong slot. Rather than ship that, the arguments the key
	 * carries are reported directly - they are the two things worth knowing, and they are already
	 * translated. Should the text ever be corrected upstream, the resolved sentence is used again
	 * without a change here.
	 * </p>
	 */
	private static String describeArguments(Resources resources, ResKey reason) {
		List<String> parts = new ArrayList<>();
		for (Object argument : reason.arguments()) {
			String part = argument instanceof ResKey key ? resources.getString(key)
				: String.valueOf(argument);
			if (!StringServices.isEmpty(part)) {
				parts.add(part);
			}
		}
		return parts.isEmpty() ? resources.getString(reason.plain())
			: StringServices.join(parts, " - ");
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
