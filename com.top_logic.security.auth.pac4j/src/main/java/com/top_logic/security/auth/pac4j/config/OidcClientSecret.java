/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import java.util.Optional;

import org.pac4j.core.context.CallContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.util.InitializableObject;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.credentials.authenticator.OidcAuthenticator;
import org.pac4j.oidc.metadata.IOidcOpMetadataResolver;
import org.pac4j.oidc.profile.creator.TokenValidator;
import org.pac4j.oidc.redirect.OidcRedirectionActionBuilder;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;

import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Hands the client secret of an {@link OidcClient} to pac4j each time pac4j needs it.
 *
 * <p>
 * The secret is not read when the client is created, so that an application starts without it.
 * Instead, it is read from its {@link SecretSource} when a login starts (before the browser is sent
 * to the identity provider) and before each token request (the exchange of the authorization code
 * and the refresh of a token).
 * </p>
 *
 * <p>
 * pac4j derives the {@link ClientAuthentication} of the token requests and the {@link TokenValidator}
 * checking ID tokens signed with the secret (<code>HS256</code>, <code>HS384</code>,
 * <code>HS512</code>) once, when its {@link IOidcOpMetadataResolver} loads the provider metadata,
 * and keeps both. When the secret read differs from the one pac4j last used - the first login, or a
 * rotated secret - the resolver is therefore loaded again, so that both use the current secret.
 * </p>
 *
 * @see #install(OidcClient, SecretSource)
 */
public class OidcClientSecret {

	private final OidcConfiguration _configuration;

	private final SecretSource _secret;

	/**
	 * Creates an {@link OidcClientSecret}.
	 *
	 * @param configuration
	 *        The pac4j configuration of the client to hand the secret to.
	 * @param secret
	 *        The source of the client secret.
	 */
	public OidcClientSecret(OidcConfiguration configuration, SecretSource secret) {
		_configuration = configuration;
		_secret = secret;
	}

	/**
	 * Lets the given client read its secret from the given source each time it needs it.
	 *
	 * <p>
	 * Installs the {@link OidcClient#getRedirectionActionBuilder() redirection action builder} and the
	 * {@link OidcClient#getAuthenticator() authenticator} of the client, which pac4j otherwise
	 * creates when the client is initialized.
	 * </p>
	 */
	public static void install(OidcClient client, SecretSource secret) {
		OidcClientSecret clientSecret = new OidcClientSecret(client.getConfiguration(), secret);
		client.setRedirectionActionBuilder(new RedirectionActionBuilder(client, clientSecret));
		client.setAuthenticator(new Authenticator(client, clientSecret));
	}

	/**
	 * Reads the current secret and hands it to pac4j.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is not available. The current login fails with this error.
	 */
	public void update() {
		String current = _secret.getSecret();
		synchronized (_configuration) {
			String used = _configuration.getSecret();
			if (current.equals(used)) {
				return;
			}
			_configuration.setSecret(current);
			try {
				IOidcOpMetadataResolver resolver = _configuration.getOpMetadataResolver();
				if (resolver instanceof InitializableObject initializable) {
					initializable.reinit();
				}
			} catch (RuntimeException ex) {
				// Try again with the next use.
				_configuration.setSecret(used);
				throw ex;
			}
		}
	}

	/**
	 * {@link OidcRedirectionActionBuilder} reading the secret before the browser is sent to the
	 * identity provider, so that a login without secret fails before the user authenticates there.
	 */
	private static class RedirectionActionBuilder extends OidcRedirectionActionBuilder {

		private final OidcClientSecret _clientSecret;

		RedirectionActionBuilder(OidcClient client, OidcClientSecret clientSecret) {
			super(client);
			_clientSecret = clientSecret;
		}

		@Override
		public Optional<RedirectionAction> getRedirectionAction(CallContext ctx) {
			_clientSecret.update();
			return super.getRedirectionAction(ctx);
		}

	}

	/**
	 * {@link OidcAuthenticator} reading the secret before each token request.
	 */
	private static class Authenticator extends OidcAuthenticator {

		private final OidcClientSecret _clientSecret;

		Authenticator(OidcClient client, OidcClientSecret clientSecret) {
			super(client.getConfiguration(), client);
			_clientSecret = clientSecret;
		}

		@Override
		protected TokenRequest createTokenRequest(AuthorizationGrant grant) {
			_clientSecret.update();
			return super.createTokenRequest(grant);
		}

	}

}
