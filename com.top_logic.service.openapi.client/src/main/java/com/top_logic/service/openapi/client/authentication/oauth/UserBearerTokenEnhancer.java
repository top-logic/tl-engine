/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.oauth;

import java.util.Date;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.base.accesscontrol.UserTokens;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;

/**
 * {@link SecurityEnhancer}, which uses the login access token of the user currently logged in via
 * OIDC as authorization header.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UserBearerTokenEnhancer implements SecurityEnhancer {

	/** Singleton {@link UserBearerTokenEnhancer} instance. */
	public static final UserBearerTokenEnhancer INSTANCE = new UserBearerTokenEnhancer();

	/**
	 * Creates a new {@link UserBearerTokenEnhancer}.
	 */
	protected UserBearerTokenEnhancer() {
		// singleton instance
	}

	@Override
	public void enhanceUrl(UriBuilder urlBuilder) {
		// URL remains untouched.
	}

	@Override
	public void enhanceRequest(HttpClient client, ClassicHttpRequest request) {
		UserTokens userTokens = ExternalAuthenticationServlet.userTokens();
		if (userTokens == null) {
			return;
		}
		String accessToken = userTokens.getAccessToken();
		if (accessToken == null) {
			return;
		}
		Date expiration = userTokens.getExpiration();
		// Ensure that token is still valid for a certain period of time
		if (expiration.after(new Date(System.currentTimeMillis() + 10000))) {
			setAuthorizationHeader(request, accessToken);
			return;
		}

		if (userTokens.refreshTokens()) {
			setAuthorizationHeader(request, userTokens.getAccessToken());
		} else {
			return;
		}

	}

	private static void setAuthorizationHeader(ClassicHttpRequest request, String token) {
		request.setHeader(AuthorizationUtil.AUTHORIZATION_HEADER_NAME,
			AuthorizationUtil.toBearerAuthenticationHeader(token));
	}

}
