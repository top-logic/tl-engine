/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * Authentication of outgoing HTTP requests to an external service.
 *
 * <p>
 * A consumer of an external service (an LLM provider, a translation or search service, an API) does
 * not hold a credential itself but asks its configured {@link CredentialProvider} to authenticate
 * each request. The provider decides how: with a static key from a {@link SecretSource}
 * ({@link HeaderCredentials}, {@link BasicCredentials}), with a short-lived access token obtained
 * from an OAuth2 authorization server (the providers of the module <code>tl-credential-oauth</code>),
 * or not at all, when a gateway in between adds the credentials ({@link NoCredentials}).
 * </p>
 *
 * <p>
 * A consumer declares the provider as configuration property:
 * </p>
 *
 * <pre>
 * &#64;Name(CREDENTIALS)
 * PolymorphicConfiguration&lt;? extends CredentialProvider&gt; getCredentials();
 * </pre>
 *
 * <p>
 * and uses it for each request:
 * </p>
 *
 * <pre>
 * HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
 * credentials.authenticate(RequestAuthentication.of(builder));
 * HttpResponse&lt;String&gt; response = client.send(builder.build(), BodyHandlers.ofString());
 * if (response.statusCode() == 401) {
 *     credentials.invalidate();
 *     // Optionally retry once with fresh credentials.
 * }
 * </pre>
 *
 * <p>
 * A consumer whose service expects a static API key in a known header may accept the key as
 * shorthand next to its credentials and instantiate both with
 * {@link #create(InstantiationContext, String, PolymorphicConfiguration, PolymorphicConfiguration, String, String)}.
 * </p>
 */
public interface CredentialProvider {

	/**
	 * Name of the standard HTTP header carrying the credentials of a request.
	 */
	String AUTHORIZATION = "Authorization";

	/**
	 * Authentication scheme of a bearer token in the {@link #AUTHORIZATION} header (RFC 6750).
	 */
	String BEARER = "Bearer";

	/**
	 * Authentication scheme of a user name and password in the {@link #AUTHORIZATION} header (RFC
	 * 7617).
	 */
	String BASIC = "Basic";

	/**
	 * Adds the credentials to the given request.
	 *
	 * <p>
	 * Called for each request. The credentials are retrieved at the moment of the call, so that a
	 * rotated secret or a renewed token is used without restarting the application.
	 * </p>
	 *
	 * @param request
	 *        The request to authenticate.
	 *
	 * @throws I18NRuntimeException
	 *         If no credentials are available. The message of the exception never contains a
	 *         secret or token.
	 */
	void authenticate(RequestAuthentication request);

	/**
	 * Informs this provider that the server rejected the credentials last added (HTTP status 401).
	 *
	 * <p>
	 * A provider that caches credentials drops them, so that the next call to
	 * {@link #authenticate(RequestAuthentication)} fetches fresh ones. Providers without cache
	 * ignore the call.
	 * </p>
	 */
	default void invalidate() {
		// No cached credentials.
	}

	/**
	 * Whether this provider can add credentials to a request now.
	 *
	 * <p>
	 * Asked each time a consumer chooses whether to offer the external service, so that the
	 * service is offered as soon as its credentials, e.g. a secret, are provided. The check is
	 * local and cheap: it never contacts a server, never reveals a secret and does not fail for an
	 * absent secret. A provider that cannot tell without contacting a server, e.g. because it
	 * obtains a token from it, answers <code>true</code>; a failure is then reported by
	 * {@link #authenticate(RequestAuthentication)}.
	 * </p>
	 *
	 * @see SecretSource#isAvailable()
	 */
	default boolean isAvailable() {
		return true;
	}

	/**
	 * Where this provider takes its credentials from, e.g. to tell a user which secret must be
	 * provided to make a service {@link #isAvailable() available}.
	 *
	 * <p>
	 * The description identifies the secret without revealing it.
	 * </p>
	 *
	 * @return The description, <code>null</code> if this provider cannot tell.
	 *
	 * @see SecretSource#reference()
	 */
	default ResKey reference() {
		return null;
	}

	/**
	 * Creates the {@link CredentialProvider} of a consumer that accepts an API key as shorthand
	 * for its credentials.
	 *
	 * @param context
	 *        The context for instantiating the credentials.
	 * @param owner
	 *        Name of the configured consumer, reported in the error message.
	 * @param credentials
	 *        The configured credentials, <code>null</code> if none are configured.
	 * @param apiKey
	 *        The configured API key, <code>null</code> if none is configured.
	 * @param header
	 *        The request header in which the service expects the API key.
	 * @param scheme
	 *        The authentication scheme written before the API key, <code>null</code> for sending
	 *        the bare key.
	 * @return The configured credentials; if there are none, {@link HeaderCredentials} sending the
	 *         API key in the given header; without API key, {@link NoCredentials}.
	 * @throws ConfigurationError
	 *         If both credentials and an API key are configured.
	 */
	static CredentialProvider create(InstantiationContext context, String owner,
			PolymorphicConfiguration<? extends CredentialProvider> credentials,
			PolymorphicConfiguration<? extends SecretSource> apiKey, String header, String scheme) {
		if (credentials != null) {
			if (apiKey != null) {
				throw new ConfigurationError(I18NConstants.ERROR_CREDENTIALS_AND_API_KEY__OWNER.fill(owner));
			}
			return context.getInstance(credentials);
		}
		if (apiKey != null) {
			return context.getInstance(HeaderCredentials.config(header, scheme, apiKey));
		}
		return context.getInstance(TypedConfiguration.newConfigItem(NoCredentials.Config.class));
	}

}
