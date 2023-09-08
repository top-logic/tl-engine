/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

/**
 * Helper class for status codes of an HTTP response.
 * 
 * @see "https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HTTPStatusCodes {

	/**
	 * Representation for the range [100-199] of status codes: Informational - Request received,
	 * continuing process.
	 */
	public static final String STATUS_CODE_RANGE_INFORMATIONAL = "1XX";

	/**
	 * Representation for the range [200-299] of status codes: Success - The action was successfully
	 * received, understood, and accepted.
	 */
	public static final String STATUS_CODE_RANGE_SUCCESS = "2XX";

	/**
	 * Representation for the range [300-399] of status codes: Redirection - Further action must be
	 * taken in order to complete the request.
	 */
	public static final String STATUS_CODE_RANGE_REDIRECTION = "3XX";

	/**
	 * Representation for the range [400-499] of status codes: Client Error - The request contains
	 * bad syntax or cannot be fulfilled.
	 */
	public static final String STATUS_CODE_RANGE_CLIENT_ERROR = "4XX";

	/**
	 * Representation for the range [500-599] of status codes: Server Error - The server failed to
	 * fulfil an apparently valid request.
	 */
	public static final String STATUS_CODE_RANGE_SERVER_ERROR = "5XX";

	/**
	 * Whether the given code is a valid status code.
	 * 
	 * @param code
	 *        The code to check.
	 * @return Whether the given code is within the range [100-599].
	 */
	public static boolean isStatusCode(int code) {
		return code >= 100 && code < 600;
	}

}

