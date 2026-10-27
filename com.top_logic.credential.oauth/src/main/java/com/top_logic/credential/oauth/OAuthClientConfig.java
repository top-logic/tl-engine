/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.util.Map;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.MillisFormat;

/**
 * Configuration of an OAuth 2.0 client: the authorization server, the client's identity there and
 * the handling of the tokens it obtains.
 *
 * @see OAuthClient
 */
@Abstract
public interface OAuthClientConfig extends OAuthServerConfig {

	/**
	 * @see #getClientId()
	 */
	String CLIENT_ID = "client-id";

	/**
	 * @see #getClientAuthentication()
	 */
	String CLIENT_AUTHENTICATION = "client-authentication";

	/**
	 * @see #getScope()
	 */
	String SCOPE = "scope";

	/**
	 * @see #getParameters()
	 */
	String PARAMETERS = "parameters";

	/**
	 * @see #getRenewalMargin()
	 */
	String RENEWAL_MARGIN = "renewal-margin";

	/**
	 * @see #getDefaultTokenLifetime()
	 */
	String DEFAULT_TOKEN_LIFETIME = "default-token-lifetime";

	/**
	 * Identifier of the client at the authorization server.
	 */
	@Name(CLIENT_ID)
	@Mandatory
	String getClientId();

	/**
	 * @see #getClientId()
	 */
	void setClientId(String value);

	/**
	 * How the client proves its identity to the authorization server.
	 *
	 * <p>
	 * If not given, the client is a public client that sends only its identifier.
	 * </p>
	 */
	@Name(CLIENT_AUTHENTICATION)
	PolymorphicConfiguration<? extends ClientAuthentication> getClientAuthentication();

	/**
	 * @see #getClientAuthentication()
	 */
	void setClientAuthentication(PolymorphicConfiguration<? extends ClientAuthentication> value);

	/**
	 * Space-separated scopes requested for the access token, e.g.
	 * <code>https://cognitiveservices.azure.com/.default</code> for Azure.
	 *
	 * <p>
	 * If empty, the authorization server grants its default scope.
	 * </p>
	 */
	@Name(SCOPE)
	String getScope();

	/**
	 * @see #getScope()
	 */
	void setScope(String value);

	/**
	 * Additional parameters of the token request, e.g. <code>audience</code> or
	 * <code>resource</code> (RFC 8707) to select the service the token is meant for.
	 */
	@Name(PARAMETERS)
	@MapBinding(tag = "parameter", key = "name", attribute = "value")
	Map<String, String> getParameters();

	/**
	 * @see #getParameters()
	 */
	void setParameters(Map<String, String> value);

	/**
	 * Time before the end of the lifetime of an access token when it is renewed.
	 *
	 * <p>
	 * At most half of the token's lifetime is used as margin, so that short-lived tokens are still
	 * used for a while.
	 * </p>
	 */
	@Name(RENEWAL_MARGIN)
	@Format(MillisFormat.class)
	@FormattedDefault("30s")
	long getRenewalMargin();

	/**
	 * @see #getRenewalMargin()
	 */
	void setRenewalMargin(long value);

	/**
	 * Assumed lifetime of an access token whose lifetime the authorization server does not
	 * specify.
	 *
	 * <p>
	 * When a service rejects such a token earlier, the consumer invalidates it and a new token is
	 * requested.
	 * </p>
	 */
	@Name(DEFAULT_TOKEN_LIFETIME)
	@Format(MillisFormat.class)
	@FormattedDefault("5min")
	long getDefaultTokenLifetime();

	/**
	 * @see #getDefaultTokenLifetime()
	 */
	void setDefaultTokenLifetime(long value);

}
