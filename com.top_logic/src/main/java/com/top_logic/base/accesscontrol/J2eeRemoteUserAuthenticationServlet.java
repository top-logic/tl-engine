/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Login via J2EE remote user authentication.
 * 
 * @author <a href="mailto:tri@top-loigc.com">Thomas Richter</a>
 */
public class J2eeRemoteUserAuthenticationServlet extends ExternalAuthenticationServlet {

	/**
	 * Configuration for the authentication server that identifies the user via the request.
	 * 
	 * @see J2eeRemoteUserAuthenticationServlet
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Name of the request header which contains the remote user.
		 * 
		 * <p>
		 * If no header name is set, the remote user is directly taken from the request.
		 * </p>
		 * 
		 * @see HttpServletRequest#getRemoteUser()
		 */
		String getRequestHeaderName();

		/**
		 * Algorithm retrieving the {@link Person} for the user name in the authentication system.
		 */
		@InstanceFormat
		@NonNullable
		@InstanceDefault(DefaultExternalUserMapping.class)
		ExternalUserMapping getUserMapping();

	}

	private Config _config;

	/**
	 * This constructor creates a new {@link J2eeRemoteUserAuthenticationServlet}.
	 */
	public J2eeRemoteUserAuthenticationServlet() {
		_config = ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
     * Checks with the given request if the requesting user is authentified
     * against the domain specified in web.xml Authentification check was done
     * implicitly before by the Samba The Java CIFS Client Library setup as
     * filter in the web.xml
     */
	@Override
	protected LoginCredentials retrieveLoginCredentials(HttpServletRequest request, HttpServletResponse response) {
		String remoteUser;
		if (!StringServices.isEmpty(getRequestHeaderName())) {
			Logger.debug("Retrieve remote user from request header: " + getRequestHeaderName(), this);
			remoteUser = request.getHeader(getRequestHeaderName());
		} else {
			Logger.debug("Retrieve remote user from getRemoteUser(): " + getRequestHeaderName(), this);
			remoteUser = request.getRemoteUser();
		}
		Logger.debug("Remote User given in request as: " + remoteUser, this);

		if (StringServices.isEmpty(remoteUser)) {
			String message = "No remote user provided by request. Transparent authentication will be denied.";
			throw new LoginDeniedException(message);
        }

		Person account = getUserMapping().findAccountForExternalName(remoteUser);
		return LoginCredentials.fromUser(account);
	}

	private String getRequestHeaderName() {
		return _config.getRequestHeaderName();
	}

	private ExternalUserMapping getUserMapping() {
		return _config.getUserMapping();
	}

}
