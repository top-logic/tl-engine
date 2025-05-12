/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.util.ResKey;

/**
 * Base class for {@link Transaction}s implementing the general live cycle.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTransaction implements Transaction {

	private final KnowledgeBase kb;
	
	private int state = STATE_OPEN;

	private ResKey _commitMessage;

	/**
	 * Creates a {@link AbstractTransaction}.
	 */
	public AbstractTransaction(KnowledgeBase kb, ResKey commitMessage) {
		assert commitMessage != null;

		this.kb = kb;
		_commitMessage = commitMessage;
	}

	@Override
	public final KnowledgeBase getKnowledgeBase() {
		return kb;
	}
	
	@Override
	public final int getState() {
		return state;
	}
	
	@Override
	public ResKey getCommitMessage() {
		return _commitMessage;
	}

	/**
	 * Internally starts the transaction in the persistency layer.
	 */
	@FrameworkInternal
	public ResKey start() {
		if (state == STATE_OPEN) {
			// Change of commit message no longer possible.
			state = STATE_STARTED;
		}

		return _commitMessage;
	}

	/**
	 * Updates the commit message.
	 * 
	 * <p>
	 * Must only be called before the transaction in the persistency layer has been started.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *         If the transaction in the persistency layer has already been started.
	 */
	@Override
	public void setCommitMessage(ResKey newCommitMessage) throws IllegalStateException {
		if (newCommitMessage == null) {
			throw new IllegalArgumentException("Commit message must not be null.");
		}
		if (state != STATE_OPEN) {
			throw new IllegalStateException("Transaction already " + getStateName() + ".");
		}
		this._commitMessage = newCommitMessage;
	}

	@Override
	public final void close() {
		rollback();
	}

	@Override
	public final void rollback() {
		rollback(null, null);
	}

	@Override
	public final void rollback(ResKey failureMessage) {
		rollback(failureMessage, null);
	}
	
	@Override
	public final void rollback(ResKey failureMessage, Throwable cause) {
		if (state > STATE_STARTED) {
			// Rollback after commit or rollback is a noop.
			return;
		}

		state = STATE_FAILED;
		internalRollback(failureMessage, cause);
	}

	/**
	 * Implementation of {@link #rollback(ResKey, Throwable)}.
	 */
	protected abstract void internalRollback(ResKey message, Throwable cause);

	@Override
	public final void commit() throws KnowledgeBaseException {
		if (state > STATE_STARTED) {
			throw new IllegalStateException("Transaction already in state '" + getStateName() + "'.");
		}

		state = STATE_COMMITTED;
		internalCommit();
	}

	/**
	 * Implementation of {@link #commit()}.
	 */
	protected abstract void internalCommit() throws KnowledgeBaseException;

	@Override
	public final Revision getCommitRevision() {
		switch (state) {
		case STATE_OPEN:
			throw new IllegalStateException("Transaction was not yet committed, no commit information available.");
		case STATE_FAILED:
			throw new IllegalStateException("Transaction was rolled back, no commit information available.");
		}
		
		if (isNested()) {
			return null;
		}
		
		return internalGetCommitRevision();
	}

	/**
	 * Implementation of {@link #getCommitRevision()}.
	 * 
	 * <p>
	 * Called only if in a valid state for retrieving commit information.
	 * </p>
	 */
	protected abstract Revision internalGetCommitRevision();
	
	/**
	 * The name of the {@link #getState() state}.
	 */
	protected final String getStateName() {
		switch (state) {
		case STATE_OPEN: return "open";
		case STATE_STARTED: return "started";
		case STATE_COMMITTED: return "committed";
		case STATE_FAILED: return "failed";
		default:
			throw new AssertionError("No such state: " + state);
		}
	}
	
	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Transaction(");
		appendProperties(builder);
		builder.append(')');
		return builder.toString();
	}

	/**
	 * Appends all internal properties of this object to the given builder for
	 * implementing {@link #toString()}.
	 * 
	 * @param builder
	 *        The builder to append the debug representation of this object to.
	 */
	protected void appendProperties(StringBuilder builder) {
		builder.append("state=");
		builder.append(getStateName());
	}
	
}
