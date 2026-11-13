/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import java.util.Locale;
import java.util.function.Function;

import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;

/**
 * How a client presents the credential of one authentication an agent endpoint accepts, told to
 * the client: in a self-description of the endpoint (an MCP server card, <code>llms.txt</code>)
 * and in the answer to a request without a valid credential.
 *
 * <p>
 * An authentication describes its credential itself by implementing {@link Provider}. Every other
 * authentication is described by the security scheme it exports to an <i>OpenAPI</i> document
 * ({@link ServerAuthentication#createSchemaObject(String)}), see
 * {@link #fromSecurityScheme(SecuritySchemeObject)}.
 * </p>
 *
 * @param credential
 *        What the client sends, in plain words, e.g. "a personal access token".
 * @param header
 *        The name of the request header that carries the credential, or <code>null</code> if the
 *        credential is not sent in a header.
 * @param headerValue
 *        The value of {@link #header()}, with a placeholder for the secret part, e.g.
 *        "Bearer &lt;personal access token&gt;"; <code>null</code> if {@link #header()} is
 *        <code>null</code>.
 * @param obtain
 *        Where a person gets such a credential, for the application at the URLs of the given
 *        context; <code>null</code> if there is nothing to say about it.
 */
public record EndpointCredential(ResKey credential, String header, ResKey headerValue,
		Function<DiscoveryContext, ResKey> obtain) {

	/**
	 * The HTTP request header carrying the credential of an HTTP authentication scheme.
	 */
	public static final String AUTHORIZATION_HEADER = "Authorization";

	/**
	 * The HTTP authentication scheme of {@link I18NConstants#CREDENTIAL_HTTP_BASIC}.
	 */
	public static final String BASIC_SCHEME = "basic";

	/**
	 * An {@link ServerAuthentication} that describes the credential it accepts.
	 */
	public interface Provider {

		/**
		 * The credential this authentication accepts.
		 */
		EndpointCredential endpointCredential();

	}

	/**
	 * The description of the credential the given authentication accepts.
	 *
	 * @param authentication
	 *        The authentication of an endpoint.
	 * @param name
	 *        The name the authentication is configured under.
	 * @return The description, or <code>null</code> if the authentication does not tell how its
	 *         credential is sent.
	 */
	public static EndpointCredential of(ServerAuthentication<?> authentication, String name) {
		if (authentication instanceof Provider provider) {
			return provider.endpointCredential();
		}
		return fromSecurityScheme(authentication.createSchemaObject(name));
	}

	/**
	 * The description of the credential of the given <i>OpenAPI</i> security scheme.
	 *
	 * @return The description, or <code>null</code> for a scheme of unknown type.
	 */
	public static EndpointCredential fromSecurityScheme(SecuritySchemeObject scheme) {
		if (scheme.getType() == null) {
			return null;
		}
		switch (scheme.getType()) {
			case HTTP: {
				String httpScheme = scheme.getScheme();
				if (httpScheme == null || BASIC_SCHEME.equals(httpScheme.toLowerCase(Locale.ROOT))) {
					return new EndpointCredential(I18NConstants.CREDENTIAL_HTTP_BASIC, AUTHORIZATION_HEADER,
						I18NConstants.CREDENTIAL_HTTP_BASIC_VALUE, null);
				}
				return new EndpointCredential(I18NConstants.CREDENTIAL_HTTP__SCHEME.fill(httpScheme),
					AUTHORIZATION_HEADER, I18NConstants.CREDENTIAL_HTTP_VALUE__SCHEME.fill(httpScheme), null);
			}
			case OAUTH2:
			case OPEN_ID_CONNECT:
				return new EndpointCredential(I18NConstants.CREDENTIAL_OAUTH, AUTHORIZATION_HEADER,
					I18NConstants.CREDENTIAL_OAUTH_VALUE, null);
			case API_KEY: {
				String parameter = scheme.getName();
				APIKeyPosition position = scheme.getIn();
				if (position == APIKeyPosition.QUERY) {
					return new EndpointCredential(I18NConstants.CREDENTIAL_API_KEY_QUERY__NAME.fill(parameter),
						null, null, null);
				}
				if (position == APIKeyPosition.COOKIE) {
					return new EndpointCredential(I18NConstants.CREDENTIAL_API_KEY_COOKIE__NAME.fill(parameter),
						null, null, null);
				}
				return new EndpointCredential(I18NConstants.CREDENTIAL_API_KEY_HEADER__NAME.fill(parameter),
					parameter, I18NConstants.CREDENTIAL_API_KEY_VALUE, null);
			}
		}
		return null;
	}

	/**
	 * Whether the credential is sent in a request header, see {@link #header()}.
	 */
	public boolean isHeader() {
		return header != null;
	}

}
