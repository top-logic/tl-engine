/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.message.Message;

/**
 * Base class for {@link Transaction}s implementing the general live cycle.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTransaction implements Transaction {

	private final KnowledgeBase kb;
	
	private int state = STATE_OPEN;

	public AbstractTransaction(KnowledgeBase kb) {
		this.kb = kb;
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
	public final void close() {
		rollback();
	}

	@Override
	public final void rollback() {
		rollback(null, null);
	}

	@Override
	public final void rollback(Message failureMessage) {
		rollback(failureMessage, null);
	}
	
	@Override
	public final void rollback(Message failureMessage, Throwable cause) {
		if (! isOpen()) {
			// Rollback after commit or rollback is a noop.
			return;
		}

		state = STATE_FAILED;
		internalRollback(failureMessage, cause);
	}

	/**
	 * Implementation of {@link #rollback(Message, Throwable)}.
	 */
	protected abstract void internalRollback(Message message, Throwable cause);

	@Override
	public final void commit() throws KnowledgeBaseException {
		if (! isOpen()) {
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
	 * Whether this {@link Transaction} is neiter committed, nor rolled back.
	 */
	private boolean isOpen() {
		return state == STATE_OPEN;
	}
	
	private String getStateName() {
		switch (state) {
		case STATE_OPEN: return "open";
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
