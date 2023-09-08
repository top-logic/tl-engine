/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ProtocolException;

import com.top_logic.service.openapi.client.registry.impl.call.Call;

/**
 * Callback interface to extract a value from the response of a call to use in a later TL-Script
 * call.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ResponseExtractor {

	/**
	 * Extracts a value to use in TL-Script call.
	 * 
	 * @param call
	 *        The {@link Call} to the foreign system.
	 * @param response
	 *        The response for the given call.
	 * 
	 * @throws ProtocolException
	 *         when accessing response failed.
	 */
	Object extract(Call call, ClassicHttpResponse response) throws ProtocolException;

}

