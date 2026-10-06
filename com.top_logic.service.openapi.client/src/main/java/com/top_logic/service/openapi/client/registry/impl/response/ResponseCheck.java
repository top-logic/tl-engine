/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ResponseHandlerFactory} only checking that the service call returned a success status code.
 * 
 * <p>
 * Every status code in the range {@link HTTPStatusCodes#STATUS_CODE_RANGE_SUCCESS} counts as
 * success. Any other status code fails the call with a {@link TopLogicException} that reports the
 * reason phrase, the status code, and the textual content of the response body, if any.
 * </p>
 * 
 * <p>
 * The function result is always <code>null</code>.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResponseCheck implements ResponseHandlerFactory {

	@Override
	public ResponseHandler create(MethodSpec method) {
		return new Handler();
	}

	/**
	 * Checks that the given response has a success status code.
	 * 
	 * @param method
	 *        The called method.
	 * @param call
	 *        The arguments of the call.
	 * @param response
	 *        The response to check.
	 * @throws TopLogicException
	 *         If the status code of the response is not in the range
	 *         {@link HTTPStatusCodes#STATUS_CODE_RANGE_SUCCESS}.
	 * @see HTTPStatusCodes#isSuccess(int)
	 */
	static void checkStatusCode(MethodDefinition method, Call call, ClassicHttpResponse response) throws IOException {
		int statusCode = response.getCode();
		if (!HTTPStatusCodes.isSuccess(statusCode)) {
			String reason = response.getReasonPhrase();
			Object content = errorContent(response.getEntity());
			throw new TopLogicException(
				I18NConstants.ERROR_CALL_FAILED__FUN_ARGS_REASON_CODE_CONTENT.fill(method.getName(), call.getArguments(),
					reason, statusCode, content));
		}
	}

	/**
	 * The details to report for a failed call.
	 * 
	 * @param entity
	 *        The body of the response, <code>null</code> if the response has no body.
	 * @return The textual body content, or {@link I18NConstants#ERROR_CALL_FAILED_NO_DETAILS} if
	 *         there is no body or the body is not textual.
	 */
	private static Object errorContent(HttpEntity entity) throws IOException {
		if (entity == null) {
			return I18NConstants.ERROR_CALL_FAILED_NO_DETAILS;
		}
		String contentType = StringServices.nonNull(entity.getContentType());
		if (!contentType.startsWith("text/") && !contentType.startsWith("application/json")) {
			return I18NConstants.ERROR_CALL_FAILED_NO_DETAILS;
		}
		String contentEncoding = StringServices.nonEmpty(entity.getContentEncoding());
		try (InputStream in = entity.getContent()) {
			if (contentEncoding == null) {
				return StreamUtilities.readAllFromStream(in);
			} else {
				return StreamUtilities.readAllFromStream(in, contentEncoding);
			}
		}
	}

	/**
	 * {@link ResponseHandler} for {@link ResponseCheck}s.
	 */
	protected class Handler implements ResponseHandler {
		@Override
		public Object handle(MethodDefinition method, Call call, ClassicHttpResponse response) throws Exception {
			checkStatusCode(method, call, response);

			return null;
		}
	}


}
