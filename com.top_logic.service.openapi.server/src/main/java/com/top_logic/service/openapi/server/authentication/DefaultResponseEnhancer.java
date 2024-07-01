/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.service.openapi.server.OpenApiServer;

/**
 * Default implementation of {@link ResponseEnhancer} sending an
 * {@link HttpServletResponse#SC_UNAUTHORIZED unauthorized} error.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultResponseEnhancer implements ResponseEnhancer {

	@Override
	public void enhanceResponse(HttpServletResponse response, AuthenticationFailure ex, String requestedPath)
			throws IOException {
		String message = "Authentication failure for '" + requestedPath + "': " + ex.getMessage();
		Logger.info(message, ex, OpenApiServer.class);

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
	}

}

