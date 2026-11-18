/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * A response that answers an error with a plain-text body instead of the error page of the
 * container.
 *
 * <p>
 * A {@link #sendError(int, String)} is recorded instead of passed on; headers set on this response
 * reach the wrapped one directly. {@link #complete(IntFunction)} then writes the recorded status
 * and a {@link #PLAIN_TEXT_UTF8} body: the recorded message, followed by what the caller adds for
 * that status. An MCP client is no browser, and the person who reads its error output needs the
 * message, not an HTML page around it.
 * </p>
 */
public class PlainTextErrorResponse extends HttpServletResponseWrapper {

	/**
	 * The content type of the error body.
	 */
	public static final String PLAIN_TEXT_UTF8 = "text/plain; charset=UTF-8";

	private int _status;

	private String _message;

	/**
	 * Creates a {@link PlainTextErrorResponse}.
	 *
	 * @param response
	 *        The response to answer.
	 */
	public PlainTextErrorResponse(HttpServletResponse response) {
		super(response);
	}

	@Override
	public void sendError(int sc, String msg) {
		_status = sc;
		_message = msg;
	}

	@Override
	public void sendError(int sc) {
		sendError(sc, null);
	}

	/**
	 * Writes the recorded error, if any.
	 *
	 * @param addition
	 *        What to add to the message for the recorded status; may answer <code>null</code> for
	 *        nothing.
	 */
	public void complete(IntFunction<String> addition) throws IOException {
		if (_status == 0) {
			return;
		}
		HttpServletResponse response = (HttpServletResponse) getResponse();
		response.setStatus(_status);
		response.setContentType(PLAIN_TEXT_UTF8);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		StringBuilder body = new StringBuilder();
		if (_message != null && !_message.isBlank()) {
			body.append(_message.strip()).append("\n");
		}
		String added = addition.apply(_status);
		if (added != null) {
			if (body.length() > 0) {
				body.append('\n');
			}
			body.append(added);
		}
		PrintWriter out = response.getWriter();
		out.write(body.toString());
		out.flush();
	}

}
