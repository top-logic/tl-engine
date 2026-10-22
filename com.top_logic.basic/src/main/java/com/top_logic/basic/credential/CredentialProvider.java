/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.credential.oauth.ClientCredentialsProvider;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Authentication of outgoing HTTP requests to an external service.
 *
 * <p>
 * A consumer of an external service (an LLM provider, a translation or search service, an API) does
 * not hold a credential itself but asks its configured {@link CredentialProvider} to authenticate
 * each request. The provider decides how: with a static key from a {@link SecretSource}
 * ({@link HeaderCredentials}, {@link BasicCredentials}), with a short-lived access token obtained
 * from an OAuth2 authorization server ({@link ClientCredentialsProvider}), or not at all, when a
 * gateway in between adds the credentials ({@link NoCredentials}).
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

}
