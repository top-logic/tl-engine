/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import org.apache.hc.core5.http.ClassicHttpResponse;

import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.Call;

/**
 * {@link ResponseHandler} checking the response for a success status code and delegating to a given
 * implementation.
 * 
 * <p>
 * Every status code in the range {@link HTTPStatusCodes#STATUS_CODE_RANGE_SUCCESS} counts as
 * success, including responses without a body such as "204 No Content". Any other status code fails
 * the call with a {@link com.top_logic.util.error.TopLogicException} before the delegate is invoked.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResponseChecker implements ResponseHandler {

	private final ResponseHandler _impl;

	/**
	 * Creates a new {@link ResponseChecker}.
	 */
	public ResponseChecker(ResponseHandler impl) {
		_impl = impl;
	}

	@Override
	public Object handle(MethodDefinition method, Call call, ClassicHttpResponse response) throws Exception {
		ResponseCheck.checkStatusCode(method, call, response);

		return _impl.handle(method, call, response);
	}

}

