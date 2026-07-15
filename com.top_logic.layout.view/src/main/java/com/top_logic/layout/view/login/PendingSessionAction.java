/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Coordinates the deferred session swap for login and logout initiated from the React view UI.
 *
 * <p>
 * A React command cannot safely swap the HTTP session itself: the command pipeline keeps using the
 * (now invalidated) session for post-processing, and the SSE stream is bound to it. Instead, a login
 * or logout command records the intended action on the current session via {@link #requestLogin} /
 * {@link #requestLogout} and triggers a client reload. The reload {@code GET} on the
 * {@link com.top_logic.layout.view.ViewServlet} then performs the actual
 * {@link SessionService#loginUser} swap through {@link #apply}, which is the natural place for it (the
 * servlet already establishes the anonymous session there for fresh requests).
 * </p>
 */
public class PendingSessionAction {

	/** Session attribute holding the account name to log in on the next view request. */
	public static final String PENDING_LOGIN_USER = "com.top_logic.layout.view.login.pendingUser";

	/** Session attribute (Boolean) requesting a switch back to the anonymous user. */
	public static final String PENDING_LOGOUT = "com.top_logic.layout.view.login.pendingLogout";

	private PendingSessionAction() {
		// Utility class.
	}

	/**
	 * Records that the given account should become the session user on the next view request.
	 */
	public static void requestLogin(HttpSession session, String accountName) {
		session.setAttribute(PENDING_LOGIN_USER, accountName);
	}

	/**
	 * Records that the session should switch back to the anonymous user on the next view request.
	 */
	public static void requestLogout(HttpSession session) {
		session.setAttribute(PENDING_LOGOUT, Boolean.TRUE);
	}

	/**
	 * Performs a pending login/logout session swap if one is recorded on the current session.
	 *
	 * <p>
	 * On a swap, the current session is invalidated, a new session is created for the target user
	 * (the requested account, or the anonymous user on logout), and a redirect back to the current
	 * URL is sent so the browser re-requests the view with the new session cookie.
	 * </p>
	 *
	 * @return {@code true} if a swap was performed and a redirect was sent (the caller must stop
	 *         processing the request), {@code false} otherwise.
	 */
	public static boolean apply(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return false;
		}

		String pendingUser = (String) session.getAttribute(PENDING_LOGIN_USER);
		boolean pendingLogout = Boolean.TRUE.equals(session.getAttribute(PENDING_LOGOUT));
		if (pendingUser == null && !pendingLogout) {
			return false;
		}

		session.removeAttribute(PENDING_LOGIN_USER);
		session.removeAttribute(PENDING_LOGOUT);

		// Runs within the current request's interaction context (the session being replaced), as
		// the legacy LoginViewDialog.loginUserAndReload does. The swap must NOT run in a system
		// interaction: invalidateSession fires a logout event that opens a knowledge-base
		// transaction, which requires a valid session context.
		Person target = pendingUser != null ? Person.byName(pendingUser) : null;
		if (target != null) {
			// Last gate before the session swap: maintenance mode may have been activated after
			// the login was recorded. Fall back to the anonymous session in that case; the
			// user-visible maintenance message is raised by the login command itself.
			try {
				Login.getInstance().checkAllowedGroups(target);
			} catch (InMaintenanceModeException ex) {
				Logger.info("Pending login for '" + pendingUser + "' dropped: maintenance mode active.",
					PendingSessionAction.class);
				target = null;
			}
		}
		if (target == null) {
			target = PersonManager.getManager().getAnonymous();
		}

		SessionService service = SessionService.getInstance();
		service.invalidateSession(request.getSession());
		service.loginUser(request, response, target);

		// loginUser performs the session-cookie check redirect itself; only redirect explicitly if
		// it did not already commit the response.
		if (!response.isCommitted()) {
			response.sendRedirect(currentUrl(request));
		}
		return true;
	}

	private static String currentUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		return query == null ? uri : uri + '?' + query;
	}

}
