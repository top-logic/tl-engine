/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import java.util.HashMap;
import java.util.Map;

import org.apache.hc.core5.http.ClassicHttpResponse;

import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.Call;

/**
 * {@link ResponseHandler} that dispatches to different {@link ResponseHandler} based on the status
 * code of the response.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DispatchingResponseHandler implements ResponseHandler {

	private ResponseHandler _defaultHandler;

	private ResponseHandler[] _rangeHandlers = new ResponseHandler[5];

	private Map<Integer, ResponseHandler> _concreteHandlers = new HashMap<>();

	/**
	 * Creates a new {@link DispatchingResponseHandler}.
	 */
	public DispatchingResponseHandler(ResponseHandler defaultHandler) {
		_defaultHandler = defaultHandler;
	}

	/**
	 * Registers the given {@link ResponseHandler} for the given response code, resp. response code
	 * range.
	 * 
	 * @param code
	 *        The string representation of a response code: It must either be a string representing
	 *        an {@link Integer} in the range [0 - 599]. Allowed are also range representations
	 *        {@value HTTPStatusCodes#STATUS_CODE_RANGE_INFORMATIONAL},
	 *        {@value HTTPStatusCodes#STATUS_CODE_RANGE_SUCCESS},
	 *        {@value HTTPStatusCodes#STATUS_CODE_RANGE_REDIRECTION},
	 *        {@value HTTPStatusCodes#STATUS_CODE_RANGE_CLIENT_ERROR}, and
	 *        {@value HTTPStatusCodes#STATUS_CODE_RANGE_SERVER_ERROR}.
	 * @param specialHandler
	 *        The {@link ResponseHandler} to apply when the response has the given response code.
	 * 
	 * @throws IllegalArgumentException
	 *         When the given code does not fulfils the requirements.
	 * 
	 * @see HTTPStatusCodes#STATUS_CODE_RANGE_INFORMATIONAL
	 * @see HTTPStatusCodes#STATUS_CODE_RANGE_SUCCESS
	 * @see HTTPStatusCodes#STATUS_CODE_RANGE_REDIRECTION
	 * @see HTTPStatusCodes#STATUS_CODE_RANGE_CLIENT_ERROR
	 * @see HTTPStatusCodes#STATUS_CODE_RANGE_SERVER_ERROR
	 */
	public void registerHandler(String code, ResponseHandler specialHandler) {
		switch (code) {
			case HTTPStatusCodes.STATUS_CODE_RANGE_INFORMATIONAL:
				_rangeHandlers[0] = specialHandler;
				break;
			case HTTPStatusCodes.STATUS_CODE_RANGE_SUCCESS:
				_rangeHandlers[1] = specialHandler;
				break;
			case HTTPStatusCodes.STATUS_CODE_RANGE_REDIRECTION:
				_rangeHandlers[2] = specialHandler;
				break;
			case HTTPStatusCodes.STATUS_CODE_RANGE_CLIENT_ERROR:
				_rangeHandlers[3] = specialHandler;
				break;
			case HTTPStatusCodes.STATUS_CODE_RANGE_SERVER_ERROR:
				_rangeHandlers[4] = specialHandler;
				break;
			default:
				int respCode;
				try {
					respCode = Integer.parseInt(code);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException(
						"Given code '" + code + "' is neither an int nor a valid range.");
				}
				if (!HTTPStatusCodes.isStatusCode(respCode)) {
					throw new IllegalArgumentException("Response code '" + code + "' must be in range [100-599].");
				}
				_concreteHandlers.put(respCode, specialHandler);
		}

	}

	@Override
	public Object handle(MethodDefinition method, Call call, ClassicHttpResponse response) throws Exception {
		int code = response.getCode();
		assert HTTPStatusCodes.isStatusCode(code);

		ResponseHandler concreteHandler = _concreteHandlers.get(code);
		if (concreteHandler != null) {
			return concreteHandler.handle(method, call, response);
		}
		ResponseHandler rangeHandler = _rangeHandlers[code / 100 - 1];
		if (rangeHandler != null) {
			return rangeHandler.handle(method, call, response);
		}
		return _defaultHandler.handle(method, call, response);
	}

}

