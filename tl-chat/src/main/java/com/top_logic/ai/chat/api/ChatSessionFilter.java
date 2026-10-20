/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.IOException;

import com.top_logic.base.accesscontrol.SessionService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Session validation filter for chat endpoints that returns proper HTTP error responses instead of
 * throwing {@link ServletException}. This prevents noisy server-side WARN logs when SSE connections
 * arrive before the session is established.
 */
public class ChatSessionFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to initialize.
	}

	@Override
	public void destroy() {
		// Nothing to clean up.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		HttpSession session = httpRequest.getSession(false);
		if (session == null) {
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session");
			return;
		}
		if (SessionService.getInstance().getSession(session) == null) {
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid session");
			return;
		}
		chain.doFilter(request, response);
	}

}
