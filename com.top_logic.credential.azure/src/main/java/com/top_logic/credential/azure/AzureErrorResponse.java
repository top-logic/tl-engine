/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.util.Map;

import com.top_logic.basic.json.JSON;

/**
 * Extracts the description of an error from the error response of an Azure endpoint.
 *
 * <p>
 * The error messages of the Azure SDK may quote the complete response, which may carry a secret or
 * token. The messages of this module therefore report only the error code and description taken
 * from the response by {@link #describe(String)}.
 * </p>
 */
public final class AzureErrorResponse {

	/**
	 * Field of an error response with the error: the error code in the OAuth 2.0 format (RFC 6749
	 * §5.2) of the identity endpoints, the error object
	 * (<code>{"error": {"code": ..., "message": ...}}</code>) in the format of the Azure resource
	 * APIs.
	 */
	public static final String FIELD_ERROR = "error";

	/**
	 * Field of an OAuth 2.0 error response with the error description.
	 */
	public static final String FIELD_ERROR_DESCRIPTION = "error_description";

	/**
	 * Field of an Azure error object with the error code.
	 */
	public static final String FIELD_CODE = "code";

	/**
	 * Field of an Azure error object with the error message.
	 */
	public static final String FIELD_MESSAGE = "message";

	private AzureErrorResponse() {
		// Static utility.
	}

	/**
	 * Description of an error response for an error message.
	 *
	 * @param body
	 *        The body of the error response, may be <code>null</code>.
	 * @return Error code and description, or the empty string if the response has none. Other
	 *         content of the response is never part of the result.
	 */
	public static String describe(String body) {
		if (body == null || body.isEmpty()) {
			return "";
		}
		Object value;
		try {
			value = JSON.fromString(body);
		} catch (JSON.ParseException | RuntimeException ex) {
			// A parse error is not passed on, since its message quotes the input.
			return "";
		}
		return describeJson(value);
	}

	/**
	 * Description of a parsed error response for an error message.
	 *
	 * @param value
	 *        The body of the error response parsed into maps, lists and primitive values.
	 * @return Error code and description, or the empty string if the response has none.
	 *
	 * @see #describe(String)
	 */
	public static String describeJson(Object value) {
		if (!(value instanceof Map)) {
			return "";
		}
		Map<?, ?> response = (Map<?, ?>) value;
		Object error = response.get(FIELD_ERROR);
		if (error instanceof String) {
			return join(error, response.get(FIELD_ERROR_DESCRIPTION));
		}
		if (error instanceof Map) {
			Map<?, ?> errorObject = (Map<?, ?>) error;
			return join(errorObject.get(FIELD_CODE), errorObject.get(FIELD_MESSAGE));
		}
		return "";
	}

	private static String join(Object code, Object description) {
		String codeText = code instanceof String ? (String) code : "";
		String descriptionText = description instanceof String ? (String) description : "";
		if (codeText.isEmpty()) {
			return descriptionText;
		}
		if (descriptionText.isEmpty()) {
			return codeText;
		}
		return codeText + ": " + descriptionText;
	}

}
