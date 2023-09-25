/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.LoginFailedException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;

/**
 * Login via J2EE remote user authentication.
 * 
 * @author <a href="mailto:tri@top-loigc.com">Thomas Richter</a>
 */
public class J2eeRemoteUserAuthenticationServlet extends ExternalAuthenticationServlet {
    
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
		// separating domain and username...
		int sepIdx = remoteUser.lastIndexOf("\\");
		if (sepIdx > 0) {
			String domainName = remoteUser.substring(0, sepIdx);
			String userName = remoteUser.substring(sepIdx + 1);
			return LoginCredentials.fromUsernameAndDomain(userName, domainName);
		} else {
			if (!isAllowAuthWithoutDomain()) {
				String message = "Remote user given in unexpected format. Transparent login probably will not work."
					+ " Expected format is: DOMAIN_NAME\\Username -given was " + remoteUser;
				throw new LoginFailedException(message);
			}
			return LoginCredentials.fromUsername(remoteUser);
		}
	}

}
