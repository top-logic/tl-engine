/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.knowledge.service.event.ModificationListener;

/**
 * Handle of a running transaction for registering {@link AfterCommitAction}s.
 * 
 * <p>
 * The handle is provided by the {@link Transaction} itself and is passed to a
 * {@link ModificationListener} together with the changes of the transaction being committed.
 * </p>
 */
public interface CommitActions {

	/**
	 * Registers an action to execute after the transaction of this handle has been committed
	 * successfully.
	 * 
	 * <p>
	 * The action is executed exactly once, in the committing thread, after the outermost
	 * transaction has been committed. It is dropped without execution, if the transaction is
	 * rolled back, or if its commit fails. It receives the {@link UpdateEvent} of the outermost
	 * commit, see {@link AfterCommitAction#execute(UpdateEvent)}.
	 * </p>
	 * 
	 * <p>
	 * The registration is also possible while the transaction is being committed, e.g. from a
	 * {@link ModificationListener} or the modification it creates.
	 * </p>
	 * 
	 * @param action
	 *        The action to execute after a successful commit.
	 */
	void afterCommit(AfterCommitAction action);

}
