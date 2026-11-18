/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.impl;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.Logger;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.ResponseEnhancer;

/**
 * {@link ResponseEnhancer} of a request that several authenticators refused, answering it with
 * the challenges of all of them.
 *
 * <p>
 * Each refusal adds its <code>WWW-Authenticate</code> challenge as a header field of its own, in
 * the order of the refusals (RFC 9110, section 11.6.1), so that a client learns every scheme it
 * may authenticate with - e.g. a <code>Bearer</code> challenge naming the protected resource
 * metadata beside a <code>Basic</code> one. The status is
 * {@link HttpServletResponse#SC_UNAUTHORIZED} if any refusal answers with it, because then a
 * credential changes the answer; otherwise it is the status of the first refusal.
 * </p>
 */
public class CombinedResponseEnhancer implements ResponseEnhancer {

	private final List<AuthenticationFailure> _failures;

	/**
	 * Creates a {@link CombinedResponseEnhancer}.
	 *
	 * @param failures
	 *        The refusals of the single authenticators, in the order they were asked.
	 */
	public CombinedResponseEnhancer(List<AuthenticationFailure> failures) {
		_failures = failures;
	}

	@Override
	public void enhanceResponse(HttpServletResponse response, AuthenticationFailure ex, String requestedPath)
			throws IOException {
		ChallengeCollector collector = new ChallengeCollector(response);
		for (AuthenticationFailure failure : _failures) {
			failure.enhanceResponse(collector, requestedPath);
		}

		String message = "Authentication failure for '" + requestedPath + "': " + ex.getMessage();
		Logger.info(message, CombinedResponseEnhancer.class);
		response.sendError(collector.status(), message);
	}

	/**
	 * Response passed to the {@link ResponseEnhancer}s of the single refusals: adds their
	 * challenges to the response instead of letting one replace the other, and records their
	 * status instead of sending it.
	 */
	private static final class ChallengeCollector extends HttpServletResponseWrapper {

		private int _status;

		ChallengeCollector(HttpServletResponse response) {
			super(response);
		}

		/**
		 * The status of the combined answer.
		 */
		int status() {
			return _status == 0 ? HttpServletResponse.SC_UNAUTHORIZED : _status;
		}

		@Override
		public void setHeader(String name, String value) {
			if (AuthorizationUtil.AUTHENTICATE_RESPONSE_HEADER_NAME.equalsIgnoreCase(name)) {
				super.addHeader(name, value);
			} else {
				super.setHeader(name, value);
			}
		}

		@Override
		public void sendError(int sc, String msg) {
			record(sc);
		}

		@Override
		public void sendError(int sc) {
			record(sc);
		}

		@Override
		public void setStatus(int sc) {
			record(sc);
		}

		private void record(int sc) {
			if (_status == 0 || (sc == HttpServletResponse.SC_UNAUTHORIZED
				&& _status != HttpServletResponse.SC_UNAUTHORIZED)) {
				_status = sc;
			}
		}
	}

}
