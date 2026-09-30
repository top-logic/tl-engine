/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Helper interface to enhance {@link HttpServletResponse} due to missing authorisation.
 * 
 * @see AuthenticationFailure#setResponseEnhancer(ResponseEnhancer)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FunctionalInterface
public interface ResponseEnhancer {

	/**
	 * Modifies the given {@link HttpServletResponse} due to given {@link AuthenticationFailure}.
	 * 
	 * @param response
	 *        Response to modify.
	 * @param ex
	 *        The thrown {@link AuthenticationFailure}.
	 * @param requestedPath
	 *        Requested path.
	 */
	void enhanceResponse(HttpServletResponse response, AuthenticationFailure ex, String requestedPath)
			throws IOException;

}
