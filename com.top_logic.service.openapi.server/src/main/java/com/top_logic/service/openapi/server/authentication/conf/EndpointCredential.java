/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.conf;

import java.util.Locale;
import java.util.function.Function;

import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.util.Resources;

/**
 * How a client presents the credential of one authentication an endpoint accepts, told to the
 * client: in a self-description of the endpoint (an MCP server card, <code>llms.txt</code>) and in
 * the answer to a request without a valid credential.
 *
 * <p>
 * An authentication describes its credential itself by implementing {@link Provider}. Every other
 * authentication is described by the security scheme it exports to an <i>OpenAPI</i> document
 * ({@link ServerAuthentication#createSchemaObject(String)}), see
 * {@link #fromSecurityScheme(SecuritySchemeObject)}.
 * </p>
 *
 * <p>
 * A credential is either configured in the client - a secret the client sends in a
 * {@link #header() header} - or obtained by the client itself through a {@link #signIn() sign-in},
 * e.g. an OAuth access token the client requests after the person logged in through the browser.
 * </p>
 *
 * @param credential
 *        What the client sends, in plain words, e.g. "a personal access token".
 * @param header
 *        The name of the request header in which the client is configured to send the credential,
 *        or <code>null</code> if the credential is not configured in the client.
 * @param headerValue
 *        The value of {@link #header()}, with a placeholder for the secret part, e.g.
 *        "Bearer &lt;personal access token&gt;"; <code>null</code> if {@link #header()} is
 *        <code>null</code>.
 * @param obtain
 *        Where a person gets such a credential, or how the client obtains it, for the application
 *        at the URLs of the given context; <code>null</code> if there is nothing to say about it.
 * @param signIn
 *        Whether the client obtains the credential by itself, so that nothing has to be configured
 *        in the client but the address of the endpoint.
 */
public record EndpointCredential(ResKey credential, String header, ResKey headerValue,
		Function<DiscoveryContext, ResKey> obtain, boolean signIn) {

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
	 * Creates a description of a credential that is configured in the client.
	 *
	 * @see #EndpointCredential(ResKey, String, ResKey, Function, boolean)
	 */
	public EndpointCredential(ResKey credential, String header, ResKey headerValue,
			Function<DiscoveryContext, ResKey> obtain) {
		this(credential, header, headerValue, obtain, false);
	}

	/**
	 * The description of a credential the client obtains by itself, see {@link #signIn()}.
	 *
	 * @param credential
	 *        See {@link #credential()}.
	 * @param obtain
	 *        How the client obtains the credential, see {@link #obtain()}.
	 */
	public static EndpointCredential bySignIn(ResKey credential, Function<DiscoveryContext, ResKey> obtain) {
		return new EndpointCredential(credential, null, null, obtain, true);
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
	 * Whether the client is configured to send the credential in a request header, see
	 * {@link #header()}.
	 */
	public boolean isHeader() {
		return header != null;
	}

	/**
	 * What a client sends for this credential, in one line.
	 *
	 * @param resources
	 *        The language of the description.
	 */
	public String describe(Resources resources) {
		String text = resources.getString(credential);
		if (!isHeader()) {
			return text;
		}
		return resources.getString(I18NConstants.CREDENTIAL_IN_HEADER__CREDENTIAL_HEADER_VALUE.fill(text, header,
			resources.getString(headerValue)));
	}

	/**
	 * {@link #describe(Resources)} followed by where a person gets the credential, if the
	 * credential says so ({@link #obtain()}).
	 *
	 * @param context
	 *        The URLs of the application.
	 * @param resources
	 *        The language of the description.
	 */
	public String describeWithOrigin(DiscoveryContext context, Resources resources) {
		String text = describe(resources);
		if (obtain == null) {
			return text;
		}
		ResKey origin = obtain.apply(context);
		return origin == null ? text : text + ". " + resources.getString(origin);
	}

}
