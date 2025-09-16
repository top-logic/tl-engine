/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.util.ResKey;

/**
 * A transaction in a {@link KnowledgeBase}.
 * 
 * <p>
 * A transaction must be exclusively used with the following pattern:
 * </p>
 * 
 * <pre>
 * Transaction tx = kb.beginTx("Saving changes.");
 * try {
 * 	// Code that manipulates the knowledge base.
 * 
 * 	// Commit changes in this transaction.
 * 	tx.commit();
 * } finally {
 * 	// Roll back, if control flow did not reach commit. Noop otherwise.
 * 	tx.rollback("Invalid state.");
 * }
 * </pre>
 * 
 * @see KnowledgeBase#beginTransaction(ResKey)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Transaction extends AutoCloseable {

	/**
	 * Initial state of a {@link Transaction}.
	 * 
	 * @see #getState()
	 */
	int STATE_OPEN = 0;
	
	/**
	 * The transaction has been started in the persistency layer.
	 * 
	 * @see #getState()
	 */
	int STATE_STARTED = 1;

	/**
	 * {@link Transaction} has been successfully {@link #commit() committed}.
	 * 
	 * @see #getState()
	 */
	int STATE_COMMITTED = 2;

	/**
	 * {@link Transaction} has been {@link #rollback(ResKey, Throwable) rolled back}, or
	 * {@link #commit()} has failed.
	 * 
	 * @see #getState()
	 */
	int STATE_FAILED = 3;

	/**
	 * The {@link KnowledgeBase} in which this transaction happens.
	 */
	KnowledgeBase getKnowledgeBase();
	
	/**
	 * The state of this {@link Transaction}.
	 * 
	 * @return {@link #STATE_OPEN}, {@link #STATE_STARTED}, {@link #STATE_COMMITTED}, or
	 *         {@link #STATE_FAILED}.
	 */
	int getState();
	
	/**
	 * The commit message that is associated with the changes when committed.
	 */
	ResKey getCommitMessage();

	/**
	 * Updates the commit message.
	 * 
	 * <p>
	 * Must only be called before the transaction in the persistency layer has been started.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *         If the transaction in the persistency layer has already been started.
	 * 
	 * @see #getState()
	 * @see #STATE_STARTED
	 */
	void setCommitMessage(ResKey newCommitMessage) throws IllegalStateException;

	/**
	 * Whether this is a pseudo-nested transaction.
	 */
	boolean isNested();

	/**
	 * Commits the changes of this {@link Transaction}.
	 */
	void commit() throws KnowledgeBaseException;
	
	/**
	 * Closes this transaction, rolling back uncommitted changes.
	 */
	void rollback();

	/**
	 * Closes this transaction, rolling back uncommitted changes.
	 * 
	 * @param failureMessage
	 *        Message describing the failure that caused the rollback.
	 */
	void rollback(ResKey failureMessage);

	/**
	 * Closes this transaction, rolling back uncommitted changes.
	 * 
	 * @param failureMessage
	 *        Message describing the failure that caused the rollback.
	 * @param cause
	 *        an optional cause for this rollback. May be <code>null</code>.
	 */
	void rollback(ResKey failureMessage, Throwable cause);

	/**
	 * The revision that was created during the preceding call to
	 * {@link #commit()}.
	 * 
	 * @return The {@link Revision} that was created in this transaction,
	 *         <code>null</code> for a {@link Transaction} that did not result
	 *         in a database commit. This is the case for a pseudo nested
	 *         transaction, or a transaction in which no changes were performed.
	 * 
	 * @throws IllegalStateException
	 *         If {@link #commit()} was not yet called.
	 */
	Revision getCommitRevision();
	
	/**
	 * Calls {@link #rollback()} if not yet committed.
	 */
	@Override
	void close();
}
