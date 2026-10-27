/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.MillisFormat;

/**
 * Configuration of the connection to an OAuth 2.0 authorization server.
 *
 * <p>
 * The token endpoint is given either directly by {@link #getTokenUrl()} or found through the
 * discovery document of the {@link #getIssuer()}.
 * </p>
 *
 * @see OAuthServer
 */
@Abstract
public interface OAuthServerConfig extends ConfigurationItem {

	/**
	 * @see #getIssuer()
	 */
	String ISSUER = "issuer";

	/**
	 * @see #getTokenUrl()
	 */
	String TOKEN_URL = "token-url";

	/**
	 * @see #getConnectTimeout()
	 */
	String CONNECT_TIMEOUT = "connect-timeout";

	/**
	 * @see #getRequestTimeout()
	 */
	String REQUEST_TIMEOUT = "request-timeout";

	/**
	 * Identifier (URL) of the authorization server, e.g.
	 * <code>https://login.example.com/realms/my-realm</code> for a Keycloak realm.
	 *
	 * <p>
	 * The endpoints of the server are read from its discovery document at
	 * <code>&lt;issuer&gt;/.well-known/openid-configuration</code>. Either the issuer or the
	 * token URL must be given.
	 * </p>
	 */
	@Name(ISSUER)
	String getIssuer();

	/**
	 * @see #getIssuer()
	 */
	void setIssuer(String value);

	/**
	 * URL of the token endpoint of the authorization server.
	 *
	 * <p>
	 * Either the token URL or the issuer must be given.
	 * </p>
	 */
	@Name(TOKEN_URL)
	String getTokenUrl();

	/**
	 * @see #getTokenUrl()
	 */
	void setTokenUrl(String value);

	/**
	 * Maximum time for establishing a connection to the authorization server.
	 */
	@Name(CONNECT_TIMEOUT)
	@Format(MillisFormat.class)
	@FormattedDefault("10s")
	long getConnectTimeout();

	/**
	 * @see #getConnectTimeout()
	 */
	void setConnectTimeout(long value);

	/**
	 * Maximum time to wait for the response of the authorization server, after the connection has
	 * been established.
	 */
	@Name(REQUEST_TIMEOUT)
	@Format(MillisFormat.class)
	@FormattedDefault("30s")
	long getRequestTimeout();

	/**
	 * @see #getRequestTimeout()
	 */
	void setRequestTimeout(long value);

}
