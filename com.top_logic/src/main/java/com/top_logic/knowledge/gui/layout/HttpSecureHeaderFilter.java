/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link Filter} setting common HTTP headers enhancing app security.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HttpSecureHeaderFilter implements Filter {

	/**
	 * HTTP header for controlling the embedding to the application in frames.
	 */
	public static final String X_FRAME_OPTIONS = "X-Frame-Options";

	/**
	 * HTTP header for requesting HTTPS, if the application can be accessed through HTTPS.
	 */
	public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

	/**
	 * HTTP enabling browser heuristics for XSS protection.
	 */
	public static final String X_XSS_PROTECTION = "X-XSS-Protection";

	/**
	 * HTTP header disabling content sniffing.
	 */
	public static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

	@Override
	public void init(FilterConfig config) throws ServletException {
		// No initialization.
	}

	@Override
	public void destroy() {
		// Ignore.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (response instanceof HttpServletResponse) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			httpResponse.setHeader(X_CONTENT_TYPE_OPTIONS, "nosniff");
			httpResponse.setHeader(X_XSS_PROTECTION, "1; mode=block");
			httpResponse.setHeader(STRICT_TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains");
			httpResponse.setHeader(X_FRAME_OPTIONS, "SAMEORIGIN");
		}
		chain.doFilter(request, response);
	}

}
