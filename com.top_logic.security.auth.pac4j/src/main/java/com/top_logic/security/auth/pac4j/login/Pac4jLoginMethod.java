/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.login;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;

/**
 * {@link LoginMethod} backed by a single configured pac4j SSO client.
 *
 * <p>
 * Activating it is a full browser redirect to the pac4j authentication servlet
 * ({@code /servlet/openid}) selecting this client and carrying the post-login return page, which
 * hands off to the external identity provider and returns to the application after authentication.
 * </p>
 */
public class Pac4jLoginMethod implements LoginMethod {

	/** Path of the pac4j authentication entry servlet (relative to the context path). */
	private static final String OPENID_SERVLET = "/servlet/openid";

	private final String _clientName;

	private final ResKey _label;

	private final ThemeImage _icon;

	/**
	 * Creates a {@link Pac4jLoginMethod}.
	 *
	 * @param clientName
	 *        The pac4j client name (the {@code client_name} request parameter).
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
		String contextPath = DefaultDisplayContext.getDisplayContext().asRequest().getContextPath();
		StringBuilder url = new StringBuilder();
		url.append(contextPath).append(OPENID_SERVLET);
		url.append("?client_name=").append(encode(_clientName));
		if (returnToUrl != null && !returnToUrl.isEmpty()) {
			url.append("&startPage=").append(encode(returnToUrl));
		}
		return url.toString();
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

}
