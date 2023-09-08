/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

// First import java...
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Test incomming request for a correct session.
 * <p>
 * This is intended to block users from accessing static areas
 * (e.g. the Workarea(s) ), if they are not logged in.
 * </p>
 *
 * @author    <a href="mailto:mer@top-logic.com">Michael Eriksson</a>
 */
public class SessionCheckingFilter implements Filter {

    @Override
	public void init(FilterConfig aFilterConfig) throws ServletException {
        // Do nothing at the moment.
    }

    @Override
	public void destroy() {
        // Do nothing at the moment.
    }

    /** Theow ServletException when there is no TLContext int the Session is invalid */
    @Override
	public void doFilter(ServletRequest aRequest,
                         ServletResponse aResponse,
                         FilterChain aFilterChain)
                         throws IOException,
                         ServletException {
        HttpServletRequest theHttpRequest = (HttpServletRequest) aRequest;
            
		final HttpSession session = theHttpRequest.getSession(false);
		if (session == null) {
			throw new ServletException("No Session");
		}
		if (null == SessionService.getInstance().getSession(session))
            throw new ServletException("Invalid Session");
        // else
        aFilterChain.doFilter(aRequest, aResponse);
    }

}

