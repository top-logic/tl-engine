/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.message.Message;

/**
 * {@link Transaction} dummy that does not perform a transaction.
 */
public final class NoTransaction extends AbstractTransaction {
	/** 
	 * Creates a {@link NoTransaction}.
	 */
	public NoTransaction(KnowledgeBase kb) {
		super(kb);
	}

	@Override
	public boolean isNested() {
		return false;
	}

	@Override
	protected void internalRollback(Message message, Throwable cause) {
		// Ignore.
	}

	@Override
	protected void internalCommit() throws KnowledgeBaseException {
		// Ignore.
	}

	@Override
	protected Revision internalGetCommitRevision() {
		return null;
	}
}