/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.service.event.CommitChecker;
import com.top_logic.knowledge.service.event.ModificationListener;

/**
 * Action executed in the committing thread after a transaction has been committed successfully.
 * 
 * <p>
 * An {@link AfterCommitAction} is registered for a transaction through
 * {@link CommitActions#afterCommit(AfterCommitAction)}. It is dropped without execution, if the
 * transaction is rolled back or its commit fails, e.g. because a {@link CommitChecker} vetoes it.
 * </p>
 * 
 * <p>
 * When the action is executed, the transaction is completely finished: The changes are visible to
 * other threads, and the action may start a transaction of its own. A failure of the action does
 * not affect the commit that already succeeded; it is logged.
 * </p>
 * 
 * <p>
 * The action receives the {@link UpdateEvent} of the commit, the same event the
 * {@link UpdateListener}s get: its revision number, the created, updated and deleted objects and
 * the {@link ChangeSet}, including the changes made by {@link ModificationListener}s during the
 * commit. An action registered in a nested transaction receives the event of the outermost commit.
 * </p>
 * 
 * @see CommitActions
 */
@FunctionalInterface
public interface AfterCommitAction {

	/**
	 * Performs the action after the commit has succeeded.
	 * 
	 * @param changes
	 *        The changes of the commit, <code>null</code> if the commit changed nothing and
	 *        therefore created no revision.
	 */
	void execute(UpdateEvent changes);

	/**
	 * Executes all given actions in order.
	 * 
	 * <p>
	 * A failing action is logged and does not prevent the execution of the remaining actions.
	 * </p>
	 * 
	 * @param actions
	 *        The actions to execute.
	 * @param changes
	 *        See {@link #execute(UpdateEvent)}.
	 */
	static void executeAll(List<? extends AfterCommitAction> actions, UpdateEvent changes) {
		for (AfterCommitAction action : actions) {
			try {
				action.execute(changes);
			} catch (RuntimeException | Error ex) {
				Logger.error("Failure in after-commit action '" + action + "'.", ex, AfterCommitAction.class);
			}
		}
	}

}
