/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionIdListener;

import com.top_logic.basic.CalledByReflection;

/**
 * Lets the {@link ReactWindowRegistry} of a session follow a change of the session's ID.
 *
 * <p>
 * The container announces every change of a session ID to this listener, whoever causes it -
 * {@link com.top_logic.base.accesscontrol.SessionService#loginUserInWindow(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, com.top_logic.knowledge.wrap.person.Person)
 * a login within a window} changes the ID to protect against session fixation, while the session
 * and its windows continue.
 * </p>
 *
 * <p>
 * Registered in the <code>web-fragment.xml</code> of this module.
 * </p>
 */
public class SessionIdChangeListener implements HttpSessionIdListener {

	/**
	 * Creates a {@link SessionIdChangeListener}, called by the container.
	 */
	@CalledByReflection
	public SessionIdChangeListener() {
		// Stateless.
	}

	@Override
	public void sessionIdChanged(HttpSessionEvent event, String oldSessionId) {
		ReactWindowRegistry.handleSessionIdChange(event.getSession(), oldSessionId);
	}

}
