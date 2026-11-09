/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.util.ResKey;

/**
 * {@link Transaction} dummy that does not perform a transaction.
 */
public final class NoTransaction extends AbstractTransaction {

	private final List<AfterCommitAction> _afterCommitActions = new ArrayList<>();

	/** 
	 * Creates a {@link NoTransaction}.
	 */
	public NoTransaction(KnowledgeBase kb) {
		super(kb, I18NConstants.NO_COMMIT_MESSAGE);
	}

	@Override
	public boolean isNested() {
		return false;
	}

	@Override
	public void afterCommit(AfterCommitAction action) {
		_afterCommitActions.add(action);
	}

	@Override
	protected void internalRollback(ResKey message, Throwable cause) {
		_afterCommitActions.clear();
	}

	@Override
	protected void internalCommit() throws KnowledgeBaseException {
		List<AfterCommitAction> actions = new ArrayList<>(_afterCommitActions);
		_afterCommitActions.clear();
		AfterCommitAction.executeAll(actions);
	}

	@Override
	protected Revision internalGetCommitRevision() {
		return null;
	}
}