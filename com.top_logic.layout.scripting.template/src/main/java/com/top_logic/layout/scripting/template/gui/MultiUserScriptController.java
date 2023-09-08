/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.execution.DefaultScriptController;
import com.top_logic.layout.scripting.runtime.execution.EnvironmentMismatch;
import com.top_logic.layout.scripting.runtime.execution.ScriptController;
import com.top_logic.layout.scripting.runtime.execution.WaitTimeoutException;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link ScriptController} that coordinates multiple sessions executing a multi-user script.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MultiUserScriptController extends DefaultScriptController {

	private static final long TIMEOUT = 10L * 1000L;

	private final Set<String> _activeUsers = new HashSet<>();

	/**
	 * Creates a {@link MultiUserScriptController}.
	 *
	 * @param root
	 *        See {@link #getRoot()}.
	 */
	public MultiUserScriptController(String currentUserName, TLTreeNode<?> root) {
		super(currentUserName, root);
	}

	@Override
	public synchronized void loginUser(String userName) {
		_activeUsers.add(userName);
	}

	@Override
	public synchronized void logoutUser(String userName) {
		super.logoutUser(userName);
		_activeUsers.remove(userName);
	}

	/**
	 * Stops the execution of all processors.
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		notifyAll();
	}

	@Override
	public synchronized boolean isStopped() {
		return super.isStopped();
	}

	@Override
	protected synchronized boolean internalHasNext(Object processId, boolean login) throws WaitTimeoutException {
		if (hasNext()) {
			if (acceptAction(processId, login)) {
				return true;
			} else {
				return waitForNext(processId, login);
			}
		} else {
			return false;
		}
	}

	private boolean waitForNext(Object processId, boolean login) throws WaitTimeoutException {
		long startTime = System.currentTimeMillis();
		long timeout = TIMEOUT;
		while (true) {
			try {
				// Wait for signal.
				wait(timeout);

				long sleepTime = System.currentTimeMillis() - startTime;
				timeout = TIMEOUT - sleepTime;
			} catch (InterruptedException ex) {
				return false;
			}

			if (hasNext()) {
				if (acceptAction(processId, login)) {
					return true;
				} else {
					if (timeout > 0) {
						// Sleep longer without client-server round-trip.
						continue;
					}

					// Wait timeout exceeded.
					throw new WaitTimeoutException();
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * Whether {@link #current()} should be executed by this instance.
	 */
	protected boolean acceptAction(Object processId, boolean login) {
		String currentUser = currentUser();
		if (login && noSession(currentUser)) {
			return true;
		}

		return Utils.equals(currentUser, processId);
	}

	private boolean noSession(String currentUser) {
		return !hasSession(currentUser);
	}

	@Override
	public boolean hasSession(String currentUser) {
		return _activeUsers.contains(currentUser);
	}

	@Override
	public synchronized ApplicationAction current() {
		return internalCurrent();
	}

	private ApplicationAction internalCurrent() {
		try {
			return super.current();
		} catch (EnvironmentMismatch ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	@Override
	protected void checkUser(ApplicationAction action) throws EnvironmentMismatch {
		// Allow all actions. Only compatible actions are retrieved by this controller.
	}

	@Override
	public synchronized void next() {
		super.next();
		notifyAll();
	}

	@Override
	public synchronized void nextSibling() {
		super.nextSibling();
		notifyAll();
	}

}
