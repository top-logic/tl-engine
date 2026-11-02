/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import java.util.Optional;

import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.util.HttpActionHelper;
import org.pac4j.core.util.InitializableObject;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.credentials.authenticator.OidcAuthenticator;
import org.pac4j.oidc.metadata.IOidcOpMetadataResolver;
import org.pac4j.oidc.profile.creator.TokenValidator;
import org.pac4j.oidc.redirect.OidcRedirectionActionBuilder;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.basic.Logger;
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
 * <p>
 * A login with a client whose secret is not available - the login page does not offer it, see
 * {@link #isAvailable(SecretSource, String)}, but its URL may be called directly - sends the browser
 * back to the {@link ApplicationPages.Config#getLoginRetrySSOPage() page} an application shows after
 * a failed single-sign-on.
 * </p>
 *
 * @see #install(OidcClient, SecretSource)
 */
public class OidcClientSecret {

	private final OidcClient _client;

	private final SecretSource _secret;

	/**
	 * Creates an {@link OidcClientSecret}.
	 *
	 * @param client
	 *        The client to hand the secret to.
	 * @param secret
	 *        The source of the client secret.
	 */
	public OidcClientSecret(OidcClient client, SecretSource secret) {
		_client = client;
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
		OidcClientSecret clientSecret = new OidcClientSecret(client, secret);
		client.setRedirectionActionBuilder(new RedirectionActionBuilder(client, clientSecret));
		client.setAuthenticator(new Authenticator(client, clientSecret));
	}

	/**
	 * Whether the given secret of a client is available now, so that a login with the client can
	 * succeed.
	 *
	 * <p>
	 * A secret that is present but cannot be read counts as not available and is reported as
	 * warning.
	 * </p>
	 *
	 * @param secret
	 *        The source of the client secret.
	 * @param clientName
	 *        The name of the client, for the warning.
	 */
	public static boolean isAvailable(SecretSource secret, String clientName) {
		try {
			return secret.lookup() != null;
		} catch (I18NRuntimeException ex) {
			Logger.warn("The secret of the login client '" + clientName + "' cannot be read: " + ex.getMessage(),
				ex, OidcClientSecret.class);
			return false;
		}
	}

	/**
	 * Reads the current secret and hands it to pac4j.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is not available. The current login fails with this error.
	 */
	public void update() {
		String current = _secret.getSecret();
		OidcConfiguration configuration = _client.getConfiguration();
		synchronized (configuration) {
			String used = configuration.getSecret();
			if (current.equals(used)) {
				return;
			}
			configuration.setSecret(current);
			try {
				IOidcOpMetadataResolver resolver = configuration.getOpMetadataResolver();
				if (resolver instanceof InitializableObject initializable) {
					initializable.reinit();
				}
			} catch (RuntimeException ex) {
				// Try again with the next use.
				configuration.setSecret(used);
				throw ex;
			}
		}
	}

	/**
	 * Reads the current secret and hands it to pac4j for a login in the given context.
	 *
	 * @throws HttpAction
	 *         Sending the browser back to the page shown after a failed single-sign-on, if the
	 *         secret is not available.
	 */
	void updateForLogin(WebContext context) {
		try {
			update();
		} catch (I18NRuntimeException ex) {
			Logger.warn("Login with client '" + _client.getName() + "' refused: " + ex.getMessage(),
				OidcClientSecret.class);
			throw HttpActionHelper.buildRedirectUrlAction(context, loginRetryUrl(context));
		}
	}

	private static String loginRetryUrl(WebContext context) {
		String contextPath =
			context instanceof JEEContext jeeContext ? jeeContext.getNativeRequest().getContextPath() : "";
		return LoginFailureLogic.loginRetryUrl(contextPath);
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
			_clientSecret.updateForLogin(ctx.webContext());
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
		public Optional<Credentials> validate(CallContext ctx, Credentials cred) {
			_clientSecret.updateForLogin(ctx.webContext());
			return super.validate(ctx, cred);
		}

		@Override
		protected TokenRequest createTokenRequest(AuthorizationGrant grant) {
			_clientSecret.update();
			return super.createTokenRequest(grant);
		}

	}

}
