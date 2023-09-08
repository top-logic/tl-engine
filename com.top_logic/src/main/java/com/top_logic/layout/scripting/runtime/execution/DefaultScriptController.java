/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.execution;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link ScriptController} that executes actions in a single-session script.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultScriptController extends AbstractScriptController {

	private final String _currentUserName;

	private TLTreeNode<?> _current;

	private boolean _stopped;

	/**
	 * Creates a {@link DefaultScriptController}.
	 */
	public DefaultScriptController(String currentUserName, TLTreeNode<?> root) {
		super(root);

		_currentUserName = currentUserName;
		_current = root;
	}

	@Override
	public void loginUser(String userName) {
		// Ignore.
	}

	@Override
	public void logoutUser(String userName) {
		// Ignore.
	}

	@Override
	public void stop() {
		_stopped = true;
	}

	@Override
	public boolean isStopped() {
		return _stopped;
	}

	@Override
	public final boolean hasNext(Object processId, boolean login) throws WaitTimeoutException {
		if (isLoggedOut(processId)) {
			return false;
		}

		return internalHasNext(processId, login);
	}

	/**
	 * Implementation of {@link #hasNext(Object, boolean)}.
	 * 
	 * @param processId
	 *        See {@link #hasNext(Object, boolean)}.
	 * @param login
	 *        See {@link #hasNext(Object, boolean)}.
	 * @throws WaitTimeoutException
	 *         See {@link #hasNext(Object, boolean)}.
	 */
	protected boolean internalHasNext(Object processId, boolean login) throws WaitTimeoutException {
		return hasNext();
	}

	/**
	 * Has any next action to execute independent of the executing process ID.
	 */
	public final boolean hasNext() {
		return !isStopped() && _current != null && !isBreakPoint(_current);
	}

	@Override
	public ApplicationAction current() throws EnvironmentMismatch {
		ApplicationAction result = currentOfAnyUser();
		checkUser(result);
		return result;
	}

	/**
	 * The next action that is being executed next.
	 */
	public ApplicationAction currentOfAnyUser() {
		return action(_current);
	}

	private ApplicationAction action(TLTreeNode<?> current) {
		return (ApplicationAction) current.getBusinessObject();
	}

	@Override
	public final String currentUser() {
		return user(_current);
	}

	@Override
	public boolean hasSession(String userName) {
		return _currentUserName.equals(userName);
	}

	private String user(TLTreeNode<?> node) {
		if (node == null) {
			// The default user, if no parent action has an explicit user name set.
			return _currentUserName;
		}
		String result = action(node).getUserID();
		if (result.isEmpty()) {
			return user(node.getParent());
		}
		return result;
	}

	/**
	 * Makes sure that the given action can be executed in the current context.
	 * 
	 * @throws EnvironmentMismatch
	 *         If the given action cannot be executed.
	 */
	protected void checkUser(ApplicationAction action) throws EnvironmentMismatch {
		String actionUser = action.getUserID();
		if (!actionUser.isEmpty() && !actionUser.equals(_currentUserName)) {
			ResKey key = I18NConstants.WRONG_EXECUTING_USER__ACTION__EXPECTED_USER__ACTUAL_USER.fill(
				action, _currentUserName, actionUser);
			throw new EnvironmentMismatch(key, action, _currentUserName, actionUser);
		}
	}

	@Override
	public void next() {
		_current = followingNode(_current);
	}

	@Override
	public void nextSibling() {
		_current = followingSibling(_current);
	}

	@Override
	public TLTreeNode<?> scriptPosition() {
		return _current;
	}

}
