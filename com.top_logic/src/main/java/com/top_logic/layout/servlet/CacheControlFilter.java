/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;

/**
 * Servlet {@link Filter} to set the <code>max-age</code> value of the <code>Cache-Control</code>
 * header on static resources.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CacheControlFilter implements Filter {

	private String[] _excludes;

	private String _cacheControlValue;

	private boolean _debug;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String maxAgeSpec = filterConfig.getInitParameter("max-age");
		int maxAge;
		if (maxAgeSpec != null && !maxAgeSpec.trim().isEmpty()) {
			maxAge = Integer.parseInt(maxAgeSpec.trim());
		} else {
			maxAge = 30 * 60;
		}
		_cacheControlValue = "max-age=" + Integer.toString(maxAge);

		String excludesSpec = filterConfig.getInitParameter("excludes");
		if (excludesSpec == null || excludesSpec.trim().isEmpty()) {
			_excludes = new String[0];
		} else {
			_excludes = excludesSpec.trim().split("\\s*,\\s*");
		}

		String debugSpec = filterConfig.getInitParameter("debug");
		_debug = debugSpec != null && debugSpec.equals("true");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String requestURI = httpRequest.getRequestURI();

			// Note: httpRequest.getPathInfo() is null in a filter.
			String pathInfo = requestURI.substring(httpRequest.getContextPath().length());

			boolean matches = true;
			for (String exclude : _excludes) {
				if (pathInfo.startsWith(exclude)) {
					matches = false;
					break;
				}
			}

			if (matches) {
				((HttpServletResponse) response).setHeader("Cache-Control", _cacheControlValue);
			}

			if (_debug) {
				Logger.info(httpRequest.getMethod() + " " + requestURI + (matches ? " (static)" : " (dynamic)"),
					CacheControlFilter.class);
			}
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// Nothing to do.
	}

}
