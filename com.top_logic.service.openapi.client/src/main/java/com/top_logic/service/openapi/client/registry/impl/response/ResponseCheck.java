/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

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

	static void checkStatusCode(MethodDefinition method, Call call, ClassicHttpResponse response) throws IOException {
		int statusCode = response.getCode();
		if (statusCode != HttpServletResponse.SC_OK) {
			String reason = response.getReasonPhrase();
			HttpEntity entity = response.getEntity();
			String contentType = StringServices.nonNull(entity.getContentType());
			Object content;
			if (contentType.startsWith("text/") || contentType.startsWith("application/json")) {
				String contentEncoding = StringServices.nonEmpty(entity.getContentEncoding());
				if (contentEncoding == null) {
					try (InputStream in = entity.getContent()) {
						content = StreamUtilities.readAllFromStream(in);
					}
				} else {
					try (InputStream in = entity.getContent()) {
						content = StreamUtilities.readAllFromStream(in, contentEncoding);
					}
				}
			} else {
				content = I18NConstants.ERROR_CALL_FAILED_NO_DETAILS;
			}
			throw new TopLogicException(
				I18NConstants.ERROR_CALL_FAILED__FUN_ARGS_REASON_CODE_CONTENT.fill(method.getName(), call.getArguments(),
					reason, statusCode, content));
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
