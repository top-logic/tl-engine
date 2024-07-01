/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.servlet;

import java.util.Date;
import java.util.Optional;

import jakarta.servlet.http.Cookie;

import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.oidc.profile.OidcProfile;

import com.nimbusds.oauth2.sdk.token.AccessToken;

import com.top_logic.base.accesscontrol.UserTokens;
import com.top_logic.layout.DisplayContext;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;

/**
 * {@link UserTokens} implementation based on an {@link OidcProfile}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Pac4jUserTokens implements UserTokens {

	private OidcProfile _profile;

	private String _csfrToken;

	/**
	 * Creates a {@link Pac4jUserTokens}.
	 */
	public Pac4jUserTokens(DisplayContext displayContext, OidcProfile profile) {
		_profile = profile;
		installCSRFToken(displayContext);
	}

	@Override
	public String getAccessToken() {
		AccessToken accessToken = _profile.getAccessToken();
		if (accessToken != null) {
			return accessToken.getValue();
		}
		return null;
	}

	@Override
	public Date getExpiration() {
		return _profile.getExpiration();
	}

	@Override
	public boolean isExpired() {
		return _profile.isExpired();
	}

	@Override
	public String getIdToken() {
		return _profile.getIdTokenString();
	}

	@Override
	public boolean refreshTokens(DisplayContext displayContext) {
		Config config = Pac4jConfigFactory.getInstance().getPac4jConfig();
		Client client = config.getClients().findClient(_profile.getClientName()).get();
		WebContext context = new JEEContext(displayContext.asRequest(), displayContext.asResponse());
		CallContext callContext = new CallContext(context, new JEESessionStore());
		Optional<UserProfile> newProfile = client.renewUserProfile(callContext, _profile);
		if (newProfile.isEmpty()) {
			return false;
		}
		_profile = (OidcProfile) newProfile.get();
		installCSRFToken(displayContext);
		return true;
	}

	private void installCSRFToken(DisplayContext dc) {
		Cookie[] cookies = dc.asRequest().getCookies();
		if (cookies == null) {
			return;
		}
		String csfrToken = null;
		for (Cookie cookie : cookies) {
			if (Pac4jConstants.CSRF_TOKEN.equals(cookie.getName())) {
				csfrToken = cookie.getValue();
				break;
			}
		}
		_csfrToken = csfrToken;
	}

	@Override
	public String getCSFRToken() {
		return _csfrToken;
	}

}
