/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.Logger;
import com.top_logic.basic.message.Message;
import com.top_logic.knowledge.service.AbstractTransaction;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Messages;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;

/**
 * {@link Transaction} implementation of {@link DBKnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
class TransactionImpl extends AbstractTransaction {

	private static final char ERROR_SEPARATOR = '\n';

	/**
	 * The context of this transaction and all {@link #outer} transactions.
	 */
	private final DefaultDBContext context;

	/**
	 * @see #getOuter()
	 */
	private final TransactionImpl outer;

	/**
	 * @see #isAnonymous()
	 */
	private boolean anonymous;
	
	/**
	 * Whether the transaction has started implicitly by the first modification
	 * operation.
	 * 
	 * @see KnowledgeBase#begin()
	 */
	private boolean autoBegin;

	/**
	 * @see #getCommitMessage()
	 */
	private Message commitMessageNonNull;

	/**
	 * The failure message of an invalidated transaction.
	 * 
	 * <p>
	 * Since nested transactions are not fully supported, the
	 * {@link #getOuter()} transaction is marked invalid upon a rollback of a
	 * nested transaction.
	 * </p>
	 */
	private Message failureMessage;

	/**
	 * @see #internalGetCommitRevision()
	 */
	private Revision revision;

	private Exception failureStacktrace;

	/**
	 * Creates an auto-begin {@link TransactionImpl}.
	 */
	public TransactionImpl(DefaultDBContext context) {
		super(context.kb);

		this.context = context;
		this.outer = null;
		this.anonymous = true;
		this.initCommitMessage(null);
		this.autoBegin = true;
	}

	/**
	 * Creates a named top-level {@link TransactionImpl}.
	 * 
	 * @param anonymous
	 *        Whether this is an anonymous transaction that is created by a call
	 *        to the legacy begin method.
	 * @param commitMessage
	 *        The commit message for the corresponding {@link #commit()}.
	 */
	public TransactionImpl(DefaultDBContext context, boolean anonymous, Message commitMessage) {
		super(context.kb);

		this.context = context;
		this.anonymous = anonymous;
		this.outer = null;
		this.initCommitMessage(commitMessage);
		this.autoBegin = false;
	}

	/**
	 * Creates a named nested {@link TransactionImpl}.
	 * 
	 * @param outer
	 *        The nesting outer {@link TransactionImpl}.
	 * @param commitMessage
	 *        The commit message for the corresponding {@link #commit()}.
	 */
	public TransactionImpl(DefaultDBContext context, TransactionImpl outer, boolean anonymous, Message commitMessage) {
		super(context.kb);

		this.context = context;
		this.outer = outer;
		this.anonymous = anonymous;
		this.initCommitMessage(commitMessage);
		this.autoBegin = false;
	}

	/**
	 * The outer {@link TransactionImpl} of a nested transaction.
	 * 
	 * <p>
	 * This field is <code>null</code>, if this represents the outermost
	 * transaction.
	 * </p>
	 */
	public TransactionImpl getOuter() {
		return outer;
	}
	
	/**
	 * Whether this transaction was started by the legacy begin API that
	 * makes nesting checks impossible.
	 * 
	 * @see KnowledgeBase#begin()
	 */
	public boolean isAnonymous() {
		return anonymous;
	}

	/**
	 * The commit message of this transaction.
	 * 
	 * @return The {@link Message} to identify the current transaction, never
	 *         <code>null</code>.
	 *         
	 * @see #commit()
	 */
	public Message getCommitMessage() {
		return commitMessageNonNull;
	}

	@Override
	public boolean isNested() {
		return outer != null;
	}
	
	@Override
	protected void internalCommit() throws KnowledgeBaseException {
		this.revision = context.commitTransaction(this);
	}

	@Override
	protected void internalRollback(Message message, Throwable cause) {
		context.rollbackTransaction(this, message, cause);
	}

	@Override
	protected Revision internalGetCommitRevision() {
		return revision;
	}
	
	public TransactionImpl nest(boolean nestedAnonymous, Message nestedCommitMessage) {
    	if (autoBegin) {
    		// Fix auto-begin.
    		
    		// This is a programming error. If a nested transaction would be
			// started, the commit() matching the currently called begin would
			// not commit but only close the pseudo-nested transaction.
			Logger.warn("Attempt to start a pseudo-nested transaction after auto begin.", new Exception("StackTrace"),
				TransactionImpl.class);

    		this.autoBegin = false;
    		this.anonymous = nestedAnonymous;
    		this.initCommitMessage(nestedCommitMessage);
    		
    		return this;
    	} else {
    		return new TransactionImpl(context, this, nestedAnonymous, nestedCommitMessage);
    	}
	}
	
	/**
	 * Whether this transaction is invalidated.
	 * 
	 * <p>
	 * The commit of an invalidated transaction fails with {@link #getError()}.
	 * </p>
	 */
	public boolean hasError() {
		return failureMessage != null;
	}

	/**
	 * The error message of an invalidated transaction, or <code>null</code>, if
	 * this transaction is still valid.
	 */
	public Message getError() {
		return failureMessage;
	}

	/**
	 * Marks this transaction as invalid.
	 * 
	 * @param failureMessage
	 *        The reason of the invalidation of this transaction.
	 */
	public void setError(Message failureMessage) {
		assert failureMessage != null;
		this.failureMessage = failureMessage;
		this.failureStacktrace = new Exception("Failure stack trace");
	}
	
	/**
	 * Exception with stack trace of the caller to {@link #setError(Message)}.
	 */
	public Exception getFailureStacktrace() {
		return failureStacktrace;
	}
	
	@Override
	protected void appendProperties(StringBuilder builder) {
		super.appendProperties(builder);
		
		builder.append(", context=");
		builder.append(context);
	}

	/**
	 * Sets the {@link #getCommitMessage()} property ensuring that it is never
	 * <code>null</code>.
	 * 
	 * @param message
	 *        The {@link Message} to use. A default message is used, if
	 *        <code>null</code>.
	 */
	private void initCommitMessage(Message message) {
		if (message == null) {
			this.commitMessageNonNull = Messages.NO_COMMIT_MESSAGE;
		} else {
			this.commitMessageNonNull = message;
		}
	}
	
	private static class ErrorMessage {
		private final Message message;
		private final Throwable cause;
		
		public ErrorMessage(Message message, Throwable cause) {
			this.message = message;
			this.cause = cause;
		}

		public Message getMessage() {
			return message;
		}
		
		public Throwable getCause() {
			return cause;
		}
	}

}
