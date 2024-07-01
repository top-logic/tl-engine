/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.LoginFailedException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * {@link BasicAuthenticationServlet} is a login servlet which authenticates the user by simple HTTP
 * basic authentication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BasicAuthenticationServlet extends ExternalAuthenticationServlet {

	private static ResKey I18N_NO_AUTHORIZATION_SENT =
		I18NConstants.ERROR_BASIC_AUTHENTICATION_FAILED;

	@Override
	protected LoginCredentials retrieveLoginCredentials(HttpServletRequest request, HttpServletResponse response)
			throws ForwardRequiredException, LoginDeniedException, LoginFailedException {
		if (!AuthorizationUtil.authorizationSent(request)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			AuthorizationUtil.setBasicAuthAuthenticationRequestHeader(response);
			response.setCharacterEncoding(AuthorizationUtil.UTF8.name());
			try {
				String target = request.getContextPath() + ApplicationPages.getInstance().getLoginRetrySSOPage();
				String message = Resources.getInstance().getMessage(I18N_NO_AUTHORIZATION_SENT, target);
				String page = "<html><head></head><body onload='window.location=\"" + target + "\"'>" + message + "</body></html>";
				response.getWriter().println(page);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			throw new BreakCheckRequestException();
		}
		String header = AuthorizationUtil.getAuthorizationHeader(request);
		if (!AuthorizationUtil.isBasicAuthentiation(header)) {
			String message = "Expected header " + AuthorizationUtil.AUTHORIZATION_HEADER_NAME + " starts with '"
					+ AuthorizationUtil.BASIC_AUTH_HEADER_PREFIX + "'";
			throw new LoginFailedException(message);
		}
		return AuthorizationUtil.retrieveBasicAuthCredentials(header, LoginCredentials::fromUsernameAndPassword);
	}

	@Override
	protected void checkLoginCredentials(LoginCredentials credentials, HttpServletRequest request,
			HttpServletResponse response)
			throws InMaintenanceModeException, LoginDeniedException, LoginFailedException {
		Login.getInstance().checkLoginByPassword(credentials);
	}

}
