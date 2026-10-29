/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Announces every request of a session to the {@link ReactWindowRegistry} of that session.
 *
 * <p>
 * Every request that carries a session restarts the session's inactivity timeout, whatever it
 * asks for - a React command, a page, a stylesheet, a request of an application's own servlet such
 * as a chat. A control counting down to the end of the session learns of a request only through
 * {@link ReactWindowRegistry#noteActivity(HttpSession)}, so this filter, mapped to every request of
 * the application, is the one place that tells it.
 * </p>
 *
 * <p>
 * A request is announced exactly when the container restarts the timeout: when it carries a valid
 * session. A request without one - a poll that identifies its window by a token instead of the
 * session cookie, so that polling does not keep the session alive - restarts nothing and is not
 * announced. A session no window of the React layer has used yet has no registry, and nothing
 * counts down for it.
 * </p>
 */
public class SessionActivityFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest httpRequest) {
			HttpSession session = httpRequest.getSession(false);
			if (session != null) {
				ReactWindowRegistry registry = ReactWindowRegistry.lookup(session);
				if (registry != null) {
					registry.noteActivity(session);
				}
			}
		}
		chain.doFilter(request, response);
	}

}
