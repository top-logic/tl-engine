/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.execution;

import com.top_logic.basic.NamedConstant;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * Program counter of a script execution.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ScriptController {

	/**
	 * Constant representing the process ID of a logged-out session.
	 */
	Object LOGGED_OUT = new NamedConstant("loggedOut");

	/**
	 * The action where the execution started.
	 */
	TLTreeNode<?> getRoot();

	/**
	 * Stops the execution of the script.
	 */
	void stop();

	/**
	 * Whether the script execution has externally been {@link #stop() stopped}.
	 */
	boolean isStopped();

	/**
	 * Sets a breakpoint in a script.
	 *
	 * @param node
	 *        The script node at which to stop execution.
	 */
	void setBreakPoint(TLTreeNode<?> node);

	/**
	 * Whether there is a {@link #current()} action to execute.
	 * 
	 * @param processId
	 *        The ID of the {@link LiveActionExecutor} to retrieve actions for.
	 * @param login
	 *        If all (implicit) login actions should be retrieved. A login action is an action that
	 *        should be executed with a user that has currently no active session.
	 * @throws WaitTimeoutException
	 *         If waiting for the availability of the next action triggered a timeout.
	 */
	boolean hasNext(Object processId, boolean login) throws WaitTimeoutException;

	/**
	 * The action under the program counter.
	 * 
	 * @throws EnvironmentMismatch
	 *         If the next action cannot be executed in the current context.
	 */
	ApplicationAction current() throws EnvironmentMismatch;

	/**
	 * Advances the program counter to the action logically following the {@link #current()}
	 * action.
	 */
	void next();

	/**
	 * Advances the program counter to the next action skipping potential subroutine actions.
	 */
	void nextSibling();

	/**
	 * The position of the {@link #current()} action in the script.
	 */
	TLTreeNode<?> scriptPosition();

	/**
	 * The user that should execute {@link #current()}.
	 */
	String currentUser();

	/**
	 * Marks the user with the given user name as active.
	 */
	void loginUser(String userName);

	/**
	 * Marks the user with the given user name as inactive.
	 */
	void logoutUser(String userName);

	/**
	 * Check whether a session for the given process ID exists.
	 */
	boolean hasSession(String processId);

	/**
	 * Whether the given process ID represents a logged-out session.
	 */
	default boolean isLoggedOut(Object processId) {
		return processId == LOGGED_OUT;
	}

}
