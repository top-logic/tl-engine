/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.basic.util.ResKey;

/**
 * The failure of an external login (a {@link LoginMethod}, e.g. a single-sign-on with an identity
 * provider), kept in the session until the login page tells the visitor about it.
 *
 * <p>
 * An external login returns to the application through a redirect or forward to the
 * {@link ApplicationPages.Config#getLoginRetrySSOPage() login retry page}. The page cannot tell
 * from its address that the login failed, so the code noticing the failure
 * {@link #report(HttpServletRequest, ResKey) reports} it here, and the login page
 * {@link #take(HttpSession) takes} the message when it is rendered, showing it once.
 * </p>
 */
public final class ExternalLoginFailure {

	private static final String SESSION_ATTRIBUTE = ExternalLoginFailure.class.getName();

	private ExternalLoginFailure() {
		// Static utility.
	}

	/**
	 * Remembers the failure of an external login in the session of the given request, with the
	 * general message that the login failed.
	 *
	 * @param request
	 *        The request in which the login failed.
	 */
	public static void report(HttpServletRequest request) {
		report(request, I18NConstants.ERROR_EXTERNAL_LOGIN_FAILED);
	}

	/**
	 * Remembers the failure of an external login in the session of the given request.
	 *
	 * @param request
	 *        The request in which the login failed.
	 * @param message
	 *        The message to show the visitor. It must not contain details an attacker could use,
	 *        those belong into the log.
	 */
	public static void report(HttpServletRequest request, ResKey message) {
		request.getSession().setAttribute(SESSION_ATTRIBUTE, message);
	}

	/**
	 * Takes the reported failure out of the given session.
	 *
	 * @param session
	 *        The session of the visitor, may be <code>null</code>.
	 * @return The message of the failure reported last, <code>null</code> if no failure was
	 *         reported since the last call.
	 */
	public static ResKey take(HttpSession session) {
		if (session == null) {
			return null;
		}
		Object message = session.getAttribute(SESSION_ATTRIBUTE);
		if (message == null) {
			return null;
		}
		session.removeAttribute(SESSION_ATTRIBUTE);
		return (ResKey) message;
	}

}
