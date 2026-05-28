/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.URLPathBuilder;

/**
 * Abstract {@link HttpServlet} as marker for all servlet implementation used by
 * <code>TopLogic</code>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AbstractTopLogicServlet extends HttpServlet {

	/**
	 * @see #isAuthenticationParameter(String)
	 */
	private static final Set<String> AUTHENTICATION_PARAMS = new HashSet<>(Arrays.asList(
		Login.USER_NAME,
		Login.PASSWORD,
		TopLogicServlet.SESSION_CHECK));

	/**
	 * The parameter that declares which start page to use.
	 */
	public static final String PARAM_START_PAGE = "startPage";

	/**
	 * The incoming request has a parameter called {@link #PARAM_START_PAGE} to indicate to where
	 * the request should be forwarded after successful login.
	 * 
	 * <p>
	 * This method forwards the request to the given target page or to the login page if no target
	 * was given.
	 * </p>
	 * 
	 * @throws ServletException
	 *         If forwarding fails.
	 */
	protected void redirectToStartPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String startPage  = request.getParameter(PARAM_START_PAGE);
		if (startPage == null) {
			startPage = ApplicationPages.getInstance().getStartPage();
		}
		response.sendRedirect(createRedirectURL(startPage, request).toString());
	}

	/**
	 * Forward the request to the given page.
	 *
	 * @param        aRequest            The send request.
	 * @param        aResponse           The send response.
	 * @param        aPage               The page to be forwarded to.
	 * @exception    IOException         If I/O operation fails.
	 * @exception    ServletException    If an error in servlet occures.
	 */
	protected void forwardToPage(String aPage, HttpServletRequest aRequest, HttpServletResponse aResponse) throws IOException, ServletException {
		getServletContext().getRequestDispatcher(aPage).forward(aRequest, aResponse);
	}

	/**
	 * Decide, whether the given {@link ServletRequest} parameter is internally used during
	 * authentication and should therefore not forwarded to the start page.
	 * 
	 * @param param
	 *        The parameter name to test.
	 * @return Whether the given parameter is a parameter that is used only during authentication.
	 */
	private static boolean isAuthenticationParameter(String param) {
		return AUTHENTICATION_PARAMS.contains(param);
	}

	/**
	 * Appends custom parameters from the given request to the given URL buffer.
	 * 
	 * <p>
	 * Custom parameters are those that are not used in authenticating the request.
	 * </p>
	 * 
	 * @param url
	 *        The URL buffer.
	 * @param request
	 *        The request to take the parameters from.
	 */
	public static void appendCustomParameters(URLPathBuilder url, HttpServletRequest request) {
		for (Enumeration<String> it = request.getParameterNames(); it.hasMoreElements();) {
			String param = it.nextElement();
			if (isAuthenticationParameter(param)) {
				continue;
			}

			url.appendParameter(param, StringServices.nonNull(request.getParameter(param)));
		}
	}

	/**
	 * Creates the redirect URL to the given page.
	 */
	public static URLPathBuilder createRedirectURL(String aPage, HttpServletRequest aRequest) {
		URLPathBuilder url = URLPathBuilder.newEmptyBuilder();
		url.appendRaw(aRequest.getContextPath());
		url.appendRaw(aPage);

		appendCustomParameters(url, aRequest);
		return url;
	}

}

