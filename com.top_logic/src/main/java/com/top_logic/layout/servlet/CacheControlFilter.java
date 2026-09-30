/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.servlet;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;

/**
 * Servlet {@link Filter} setting the <code>Cache-Control</code> header on static resources.
 * 
 * <p>
 * A request whose query string carries the version parameter {@link #VERSION_PARAMETER} addresses
 * a fixed content version of a resource: the URL changes whenever the content changes. Such a
 * response is marked <code>immutable</code> and may be cached for the time given by the
 * init-parameter {@link #VERSIONED_MAX_AGE_PARAM} (one year by default). All other requests receive
 * the <code>max-age</code> given by the init-parameter {@link #MAX_AGE_PARAM}.
 * </p>
 * 
 * <p>
 * Requests whose context-relative path starts with one of the comma-separated prefixes in the
 * init-parameter {@link #EXCLUDES_PARAM} receive no <code>Cache-Control</code> header. With the
 * init-parameter {@link #DEBUG_PARAM} set to <code>true</code>, each request is logged.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CacheControlFilter implements Filter {

	/**
	 * Name of the URL query parameter that carries the content version of a static resource.
	 * 
	 * <p>
	 * A resource URL with this parameter must change whenever the resource content changes, since
	 * the response to such a URL is cached as immutable.
	 * </p>
	 */
	public static final String VERSION_PARAMETER = "v";

	/**
	 * Init-parameter giving the <code>max-age</code> in seconds for requests without the
	 * {@link #VERSION_PARAMETER}.
	 */
	public static final String MAX_AGE_PARAM = "max-age";

	/**
	 * Init-parameter giving the <code>max-age</code> in seconds for requests carrying the
	 * {@link #VERSION_PARAMETER}.
	 */
	public static final String VERSIONED_MAX_AGE_PARAM = "versioned-max-age";

	/**
	 * Init-parameter with a comma-separated list of context-relative path prefixes that receive no
	 * <code>Cache-Control</code> header.
	 */
	public static final String EXCLUDES_PARAM = "excludes";

	/**
	 * Init-parameter that enables logging of each filtered request when set to <code>true</code>.
	 */
	public static final String DEBUG_PARAM = "debug";

	/**
	 * Name of the HTTP header set by this filter.
	 */
	public static final String CACHE_CONTROL_HEADER = "Cache-Control";

	/**
	 * Default value for {@link #MAX_AGE_PARAM}: 30 minutes.
	 */
	public static final int DEFAULT_MAX_AGE = 30 * 60;

	/**
	 * Default value for {@link #VERSIONED_MAX_AGE_PARAM}: one year.
	 */
	public static final int DEFAULT_VERSIONED_MAX_AGE = 365 * 24 * 60 * 60;

	private String[] _excludes;

	private String _cacheControlValue;

	private String _versionedCacheControlValue;

	private boolean _debug;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		int maxAge = intParam(filterConfig, MAX_AGE_PARAM, DEFAULT_MAX_AGE);
		_cacheControlValue = "max-age=" + maxAge;

		int versionedMaxAge = intParam(filterConfig, VERSIONED_MAX_AGE_PARAM, DEFAULT_VERSIONED_MAX_AGE);
		_versionedCacheControlValue = "max-age=" + versionedMaxAge + ", immutable";

		String excludesSpec = filterConfig.getInitParameter(EXCLUDES_PARAM);
		if (excludesSpec == null || excludesSpec.trim().isEmpty()) {
			_excludes = new String[0];
		} else {
			_excludes = excludesSpec.trim().split("\\s*,\\s*");
		}

		String debugSpec = filterConfig.getInitParameter(DEBUG_PARAM);
		_debug = debugSpec != null && debugSpec.equals("true");
	}

	private static int intParam(FilterConfig filterConfig, String name, int defaultValue) {
		String spec = filterConfig.getInitParameter(name);
		if (spec == null || spec.trim().isEmpty()) {
			return defaultValue;
		}
		return Integer.parseInt(spec.trim());
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

			boolean versioned = false;
			if (matches) {
				versioned = hasVersionParameter(httpRequest.getQueryString());
				((HttpServletResponse) response).setHeader(CACHE_CONTROL_HEADER,
					versioned ? _versionedCacheControlValue : _cacheControlValue);
			}

			if (_debug) {
				Logger.info(httpRequest.getMethod() + " " + requestURI
					+ (matches ? (versioned ? " (versioned)" : " (static)") : " (dynamic)"),
					CacheControlFilter.class);
			}
		}

		chain.doFilter(request, response);
	}

	/**
	 * Whether the given query string contains the {@link #VERSION_PARAMETER}.
	 * 
	 * <p>
	 * The raw query string is inspected instead of calling
	 * {@link ServletRequest#getParameter(String)}, because the latter parses a form-encoded request
	 * body and thereby consumes the input stream before the request reaches its servlet.
	 * </p>
	 */
	private static boolean hasVersionParameter(String queryString) {
		if (queryString == null) {
			return false;
		}
		for (String entry : queryString.split("&")) {
			if (entry.equals(VERSION_PARAMETER) || entry.startsWith(VERSION_PARAMETER + "=")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void destroy() {
		// Nothing to do.
	}

}
