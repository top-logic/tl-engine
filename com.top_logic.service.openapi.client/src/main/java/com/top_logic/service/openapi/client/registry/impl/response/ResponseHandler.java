/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import org.apache.hc.core5.http.ClassicHttpResponse;

import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Algorithm interpreting the response to a call to an external API.
 * 
 * @see ResponseHandlerFactory#create(MethodSpec)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ResponseHandler {

	/**
	 * Processes the given response and returns the result that should be returned from the calling
	 * script function.
	 */
	Object handle(MethodDefinition method, Call call, ClassicHttpResponse response) throws Exception;

}
