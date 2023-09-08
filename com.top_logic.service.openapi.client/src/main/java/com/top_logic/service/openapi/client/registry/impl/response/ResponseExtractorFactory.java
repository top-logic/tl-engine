/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Factory to create a {@link ResponseExtractor} for a given {@link MethodSpec}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ResponseExtractorFactory {

	/**
	 * Creates an {@link ResponseExtractor} for the given {@link MethodSpec}.
	 * 
	 * @param method
	 *        The {@link MethodSpec} that is executed.
	 * @return {@link ResponseExtractor} to process calls and response for the given method.
	 */
	ResponseExtractor newExtractor(MethodSpec method);
}

