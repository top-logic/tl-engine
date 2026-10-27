/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A request to the workspace proxy and its answer, independent of the server receiving it.
 *
 * @see WorkspaceProxyHandler#handle(ProxyExchange)
 */
public interface ProxyExchange {

	/**
	 * The method of the request, e.g. {@code POST}.
	 */
	String method();

	/**
	 * The path of the request below the root of the proxy - the root of the
	 * {@link WorkspaceProxyListener} or the context of the web application -, starting with the
	 * {@link ProxyRoute#getPath() path of a route}; {@code null} if there is none.
	 */
	String path();

	/**
	 * The query string of the request, {@code null} if there is none.
	 */
	String query();

	/**
	 * The address of the client of the connection, never taken from a header.
	 */
	String remoteAddress();

	/**
	 * The names of the headers of the request.
	 */
	List<String> headerNames();

	/**
	 * The values of the header of the given name, compared ignoring case; empty if it is missing.
	 */
	List<String> headers(String name);

	/**
	 * The body of the request, read as it arrives.
	 */
	InputStream body() throws IOException;

	/**
	 * Adds a header to the answer, before {@link #sendHeaders(int, long)}.
	 */
	void addHeader(String name, String value);

	/**
	 * Sends status and headers of the answer.
	 *
	 * @param status
	 *        The HTTP status.
	 * @param length
	 *        The length of the body, or {@code -1} for a body of unknown length that is streamed.
	 */
	void sendHeaders(int status, long length) throws IOException;

	/**
	 * The body of the answer, after {@link #sendHeaders(int, long)}; each
	 * {@link OutputStream#flush()} passes what was written on to the client.
	 */
	OutputStream responseBody() throws IOException;

}
