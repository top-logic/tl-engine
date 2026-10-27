/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * {@link CredentialProvider} authenticating requests with an access token obtained by the OAuth 2.0
 * client credentials grant (RFC 6749 §4.4).
 *
 * <p>
 * The application authenticates itself as client at an authorization server and receives a
 * short-lived access token, which is sent as bearer token. The token is cached and renewed before
 * it expires (see {@link TokenCache}). A typical setup lets the application call a central API
 * gateway with such a token, while the gateway holds the keys of the external services.
 * </p>
 *
 * <p>
 * The grant is only available to confidential clients, so a client authentication is required.
 * </p>
 *
 * <p>
 * Workload identity in Kubernetes: the client authenticates with the projected service account
 * token of its pod, so the application owns no secret at all. The authorization server (here a
 * Keycloak realm) trusts the cluster's token issuer for this client:
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
 *   issuer="https://login.example.com/realms/my-realm"
 *   client-id="my-app"
 * &gt;
 *   &lt;client-authentication class="com.top_logic.credential.oauth.ClientAssertionAuthentication"&gt;
 *     &lt;assertion class="com.top_logic.basic.credential.FileSecretSource" path="/var/run/secrets/tokens/keycloak"/&gt;
 *   &lt;/client-authentication&gt;
 * &lt;/credentials&gt;
 * </pre>
 *
 * <p>
 * Azure AKS workload identity is the same setup against
 * <code>token-url="https://login.microsoftonline.com/&lt;tenant&gt;/oauth2/v2.0/token"</code> with
 * <code>scope="&lt;resource&gt;/.default"</code>, the assertion read from the file named by the
 * environment variable <code>AZURE_FEDERATED_TOKEN_FILE</code> through an alias
 * (<code>path="%AZURE_FEDERATED_TOKEN_FILE%"</code>, defined as
 * <code>&lt;entry name="%AZURE_FEDERATED_TOKEN_FILE%" value="${env:AZURE_FEDERATED_TOKEN_FILE}"/&gt;</code>).
 * </p>
 *
 * <p>
 * Authentication with a private key (<code>private_key_jwt</code>), whose public key is registered
 * for the client:
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
 *   issuer="https://login.example.com/realms/my-realm"
 *   client-id="my-app"
 * &gt;
 *   &lt;client-authentication class="com.top_logic.credential.oauth.PrivateKeyJwtAuthentication"
 *     algorithm="ES256"
 *     key-id="my-app-2026"
 *   &gt;
 *     &lt;private-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/my-app-key.pem"/&gt;
 *   &lt;/client-authentication&gt;
 * &lt;/credentials&gt;
 * </pre>
 *
 * <p>
 * Authentication with a client secret, requesting a token for a specific audience:
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
 *   token-url="https://login.example.com/realms/my-realm/protocol/openid-connect/token"
 *   client-id="my-app"
 *   scope="llm"
 * &gt;
 *   &lt;client-authentication class="com.top_logic.credential.oauth.ClientSecretAuthentication" method="basic"&gt;
 *     &lt;secret name="MY_APP_CLIENT_SECRET"/&gt;
 *   &lt;/client-authentication&gt;
 *   &lt;parameters&gt;
 *     &lt;parameter name="audience" value="llm-gateway"/&gt;
 *   &lt;/parameters&gt;
 * &lt;/credentials&gt;
 * </pre>
 *
 * @implNote The token request is created with {@link ClientCredentialsGrant} of the Nimbus OAuth
 *           2.0 SDK.
 */
public class ClientCredentialsProvider extends AbstractConfiguredInstance<ClientCredentialsProvider.Config>
		implements CredentialProvider {

	/**
	 * Configuration options for {@link ClientCredentialsProvider}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ClientCredentialsProvider>, OAuthClientConfig {

		/**
		 * Tag name of this configuration in a list of {@link CredentialProvider}s.
		 */
		String TAG_NAME = "client-credentials";

	}

	private final OAuthClient _client;

	private final TokenCache _tokens;

	/**
	 * Creates a {@link ClientCredentialsProvider} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ClientCredentialsProvider(InstantiationContext context, Config config) {
		super(context, config);
		_client = new OAuthClient(context, config);
		_tokens = _client.newTokenCache();
		if (!_client.hasAuthentication()) {
			I18NRuntimeException error =
				new I18NRuntimeException(I18NConstants.ERROR_CLIENT_AUTHENTICATION_REQUIRED__CLIENT.fill(config.getClientId()));
			context.error(error.getMessage(), error);
		}
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		IssuedToken token = _tokens.getToken(last -> fetchToken());
		request.setHeader(AUTHORIZATION, token.getAuthorization());
	}

	private IssuedToken fetchToken() {
		return _client.requestToken(new ClientCredentialsGrant());
	}

	@Override
	public void invalidate() {
		_tokens.invalidate();
	}

}
