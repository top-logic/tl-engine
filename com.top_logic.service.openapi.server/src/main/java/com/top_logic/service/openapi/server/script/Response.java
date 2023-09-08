/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.script;

import javax.servlet.http.HttpServletResponse;

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
	 * The actual result to write to {@link HttpServletResponse#getWriter()}.
	 */
	public Object getResult() {
		return _result;
	}

	/**
	 * Content type of {@link #getResult()}.
	 */
	public String getContentType() {
		return _contentType;
	}

}

