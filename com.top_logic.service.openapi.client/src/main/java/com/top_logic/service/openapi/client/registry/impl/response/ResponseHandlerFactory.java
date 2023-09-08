/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Factory building {@link ResponseHandler}s.
 * 
 * @see MethodDefinition#getResponseHandler()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ResponseHandlerFactory {

	/**
	 * Creates a {@link ResponseHandler} for a given method.
	 */
	ResponseHandler create(MethodSpec method);

}
