/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConstantForwardFilter implements Filter {

	/**
	 * Configuration parameter to configure the target to forward request to.
	 */
	private static final String TARGET = "target";

	/**
	 * time in milliseconds at which the filter should be deactivated.
	 * 
	 * a value of -1 means the the filter is deactived.
	 */
	private static volatile long _timedOut = -1;

	/**
	 * the forward target
	 */
	private String _target;

	@Override
	public void destroy() {
		_target = null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if (_timedOut != -1) {
			if (System.currentTimeMillis() > _timedOut) {
				_timedOut = -1;
				chain.doFilter(request, response);
			} else {
				final RequestDispatcher requestDispatcher = request.getRequestDispatcher(_target);
				requestDispatcher.forward(request, response);
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		final String configuredTarget = config.getInitParameter(TARGET);
		if (configuredTarget == null) {
			throw new ServletException("no '" + TARGET + "' configured");
		}
		_target = configuredTarget;
	}

	/**
	 * Activates this filter for the whole application for the given milliseconds
	 * 
	 * @param ms
	 *        the time in milliseconds how long the filter must be active
	 */
	public static void setActive(long ms) {
		_timedOut = System.currentTimeMillis() + ms;
	}

}
