/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.login;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.jee.filter.SecurityFilter;

import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.base.accesscontrol.UserTokens;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;
import com.top_logic.security.auth.pac4j.servlet.Pac4jUserTokens;
import com.top_logic.util.AbstractTopLogicServlet;

/**
 * {@link LoginMethod} backed by a single configured pac4j SSO client.
 *
 * <p>
 * Activating it is a full browser redirect to the pac4j authentication servlet
 * ({@code /servlet/openid}), naming this method's client in {@link #CLIENT_PARAM} and carrying the
 * post-login return page. The pac4j {@link SecurityFilter} in front of that servlet hands the
 * browser to the external identity provider and lets it through once the provider has
 * authenticated the user.
 * </p>
 */
public class Pac4jLoginMethod implements LoginMethod {

	/** Path of the pac4j authentication entry servlet (relative to the context path). */
	private static final String OPENID_SERVLET = "/servlet/openid";

	/**
	 * Name of the request parameter selecting the client that the pac4j {@link SecurityFilter} sends
	 * a request to, among the clients the application registers.
	 */
	private static final String CLIENT_PARAM = Pac4jConstants.DEFAULT_FORCE_CLIENT_PARAMETER;

	private final String _clientName;

	private final ResKey _label;

	private final ThemeImage _icon;

	/**
	 * Creates a {@link Pac4jLoginMethod}.
	 *
	 * @param clientName
	 *        The pac4j client name (the {@link Client#getName() name} of the client this method
	 *        logs in with).
	 * @param label
	 *        The user-visible label.
	 * @param icon
	 *        An optional icon, or {@code null}.
	 */
	public Pac4jLoginMethod(String clientName, ResKey label, ThemeImage icon) {
		_clientName = clientName;
		_label = label;
		_icon = icon;
	}

	@Override
	public String getId() {
		return _clientName;
	}

	@Override
	public ResKey getLabel() {
		return _label;
	}

	@Override
	public ThemeImage getIcon() {
		return _icon;
	}

	@Override
	public String getInitiationUrl(String returnToUrl) {
		StringBuilder url = servletUrl(DefaultDisplayContext.getDisplayContext(), _clientName);
		if (returnToUrl != null && !returnToUrl.isEmpty()) {
			url.append('&').append(AbstractTopLogicServlet.PARAM_START_PAGE).append('=').append(encode(returnToUrl));
		}
		return url.toString();
	}

	/**
	 * The URL sending the browser through this method's identity provider once more, so that it
	 * confirms the identity of the user the running session belongs to.
	 *
	 * <p>
	 * Answered only for the session this method itself established: a session logged in elsewhere is
	 * not confirmed by an identity provider it never authenticated against. The URL selects the
	 * {@link Pac4jConfigFactory#getReauthenticationName(String) re-authentication client}, which
	 * demands a fresh authentication, and carries the token back to the authentication servlet. It
	 * names no start page - the window it is opened in reports the outcome and closes.
	 * </p>
	 */
	@Override
	public String getReauthenticationUrl(String token) {
		if (!isSessionOwner()) {
			return null;
		}

		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		dropStoredProfile(context);

		StringBuilder url = servletUrl(context, Pac4jConfigFactory.getReauthenticationName(_clientName));
		url.append('&').append(ExternalAuthenticationServlet.VERIFICATION_PARAM).append('=').append(encode(token));
		return url.toString();
	}

	/**
	 * The URL of the pac4j authentication servlet that hands the browser to the given client, open
	 * for further parameters.
	 */
	private static StringBuilder servletUrl(DisplayContext context, String clientName) {
		StringBuilder result = new StringBuilder();
		result.append(context.getContextPath()).append(OPENID_SERVLET);
		result.append('?').append(CLIENT_PARAM).append('=').append(encode(clientName));
		return result;
	}

	/**
	 * Whether the session of the current thread was established by this method's client.
	 */
	private boolean isSessionOwner() {
		UserTokens tokens = ExternalAuthenticationServlet.userTokens();
		if (!(tokens instanceof Pac4jUserTokens pac4jTokens)) {
			return false;
		}
		return _clientName.equals(pac4jTokens.getClientName());
	}

	/**
	 * Discards the profile the login left in the HTTP session.
	 *
	 * <p>
	 * The pac4j {@link SecurityFilter} lets a request pass as soon as it finds a stored profile.
	 * Without this, the browser would never reach the identity provider and the round trip would
	 * confirm nothing but the session it started from. The profile the callback of the fresh
	 * authentication stores takes the place of the discarded one; the tokens of the session hold a
	 * reference of their own and are untouched.
	 * </p>
	 */
	private static void dropStoredProfile(DisplayContext context) {
		Config config = Pac4jConfigFactory.getInstance().getPac4jConfig();
		WebContext webContext = new JEEContext(context.asRequest(), context.asResponse());
		ProfileManager manager = config.getProfileManagerFactory().apply(webContext, new JEESessionStore());
		manager.setConfig(config);
		manager.removeProfiles();
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

}
