/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.script;

import jakarta.servlet.http.HttpServletResponse;

/**
 * TL-Script representation of an response to deliver to an {@link HTTPResponse}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Response {

	private int _status;

	private String _contentType;

	private Object _result;

	/**
	 * Creates a new {@link Response}.
	 * 
	 * @param status
	 *        See {@link #getStatus()}.
	 * @param result
	 *        See {@link #getResult()}.
	 * @param contentType
	 *        See {@link #getContentType()}.
	 */
	public Response(int status, Object result, String contentType) {
		_status = status;
		_contentType = contentType;
		_result = result;
	}

	/**
	 * Status code for the response.
	 * 
	 * @see HttpServletResponse
	 */
	public int getStatus() {
		return _status;
	}

	/**
	 * The actual result to deliver in the HTTP response body.
	 *
	 * <p>
	 * If the value is a {@link com.top_logic.basic.io.binary.BinaryData}, a {@code byte[]},
	 * or a {@link java.io.InputStream}, the raw bytes are streamed to
	 * {@link HttpServletResponse#getOutputStream()}. Otherwise the value is written to
	 * {@link HttpServletResponse#getWriter()} - as JSON when the {@link #getContentType()
	 * content type} is {@code application/json}, or via {@link String#valueOf(Object)}.
	 * </p>
	 *
	 * <p>
	 * When the result is <code>null</code> the response is treated as error and the
	 * container's default page for the error with code {@link #getStatus()} is delivered.
	 * </p>
	 */
	public Object getResult() {
		return _result;
	}

	/**
	 * Content type of {@link #getResult()}, or <code>null</code> to derive a sensible default
	 * from the result value.
	 *
	 * <p>
	 * When <code>null</code> (or empty) the type is chosen based on the runtime type of
	 * {@link #getResult()}: a {@link com.top_logic.basic.io.binary.BinaryData}'s own
	 * content type takes precedence; raw {@code byte[]} and {@link java.io.InputStream}
	 * default to {@code application/octet-stream}; collections and maps default to
	 * {@code application/json}; everything else defaults to {@code text/plain}.
	 * </p>
	 */
	public String getContentType() {
		return _contentType;
	}

}

