/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.common.authentication;

import java.net.URI;

import junit.framework.TestCase;

import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

/**
 * Test case for {@link TestOIDCProvider}.
 */
@SuppressWarnings("javadoc")
public class TestTestOIDCProvider extends TestCase {

	private static final String CLIENT_ID = "test-client";

	private static final String CLIENT_SECRET = "client-s3cr3t";

	private static final String CLIENT_ACCOUNT = "client-account";

	private static final String SERVER_ID = "test-server";

	private static final String SERVER_SECRET = "server-s3cr3t";

	private TestOIDCProvider _provider;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_provider = new TestOIDCProvider()
			.addClient(CLIENT_ID, CLIENT_SECRET, CLIENT_ACCOUNT)
			.addClient(SERVER_ID, SERVER_SECRET, null);
		_provider.start();
	}

	@Override
	protected void tearDown() throws Exception {
		_provider.stop();
		super.tearDown();
	}

	public void testDiscovery() throws Exception {
		OIDCProviderMetadata metadata = discover();

		assertEquals(_provider.getIssuerUrl(), metadata.getIssuer().getValue());
		assertEquals(_provider.getTokenEndpointUrl(), metadata.getTokenEndpointURI().toString());
		assertEquals(_provider.getIntrospectionEndpointUrl(), metadata.getIntrospectionEndpointURI().toString());
	}

	public void testTokenBasicAuthentication() throws Exception {
		TokenResponse response =
			requestToken(new ClientSecretBasic(new ClientID(CLIENT_ID), new Secret(CLIENT_SECRET)));

		assertTrue(response.indicatesSuccess());
		AccessToken token = response.toSuccessResponse().getTokens().getAccessToken();
		assertTrue(token instanceof BearerAccessToken);
		assertEquals(TestOIDCProvider.DEFAULT_TOKEN_LIFETIME, token.getLifetime());
	}

	public void testTokenPostAuthentication() throws Exception {
		TokenResponse response = requestToken(clientPost(CLIENT_ID, CLIENT_SECRET));

		assertTrue(response.indicatesSuccess());
	}

	public void testTokenInvalidSecret() throws Exception {
		TokenResponse response = requestToken(clientPost(CLIENT_ID, "wrong"));

		assertFalse(response.indicatesSuccess());
		assertEquals(OAuth2Error.INVALID_CLIENT, ((TokenErrorResponse) response).getErrorObject());
	}

	public void testIntrospectionActive() throws Exception {
		AccessToken token = requestToken(clientPost(CLIENT_ID, CLIENT_SECRET))
			.toSuccessResponse().getTokens().getAccessToken();

		TokenIntrospectionResponse response = introspect(clientPost(SERVER_ID, SERVER_SECRET), token);

		assertTrue(response.indicatesSuccess());
		TokenIntrospectionSuccessResponse success = response.toSuccessResponse();
		assertTrue(success.isActive());
		assertEquals(CLIENT_ACCOUNT, success.getUsername());
		assertEquals(CLIENT_ID, success.getClientID().getValue());
		assertNotNull(success.getExpirationTime());
	}

	public void testIntrospectionUnknownToken() throws Exception {
		TokenIntrospectionResponse response =
			introspect(clientPost(SERVER_ID, SERVER_SECRET), new BearerAccessToken("unknown"));

		assertTrue(response.indicatesSuccess());
		assertFalse(response.toSuccessResponse().isActive());
	}

	public void testIntrospectionRequiresClientAuthentication() throws Exception {
		AccessToken token = requestToken(clientPost(CLIENT_ID, CLIENT_SECRET))
			.toSuccessResponse().getTokens().getAccessToken();

		TokenIntrospectionResponse response = introspect(clientPost(SERVER_ID, "wrong"), token);

		assertFalse(response.indicatesSuccess());
		assertEquals(OAuth2Error.INVALID_CLIENT, ((TokenIntrospectionErrorResponse) response).getErrorObject());
	}

	public void testDoesNotKeepJVMAlive() throws Exception {
		requestToken(clientPost(CLIENT_ID, CLIENT_SECRET));

		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			String name = thread.getName();
			if (name.startsWith("HTTP-Dispatcher") || name.equals(TestOIDCProvider.class.getSimpleName())) {
				assertTrue("Not a daemon thread: " + name, thread.isDaemon());
			}
		}
	}

	private OIDCProviderMetadata discover() throws Exception {
		return OIDCProviderMetadata.resolve(new Issuer(_provider.getIssuerUrl()));
	}

	private TokenResponse requestToken(ClientAuthentication authentication) throws Exception {
		URI endpoint = discover().getTokenEndpointURI();
		TokenRequest request = new TokenRequest(endpoint, authentication, new ClientCredentialsGrant());
		return TokenResponse.parse(request.toHTTPRequest().send());
	}

	private TokenIntrospectionResponse introspect(ClientAuthentication authentication, AccessToken token)
			throws Exception {
		URI endpoint = discover().getIntrospectionEndpointURI();
		TokenIntrospectionRequest request = new TokenIntrospectionRequest(endpoint, authentication, token);
		return TokenIntrospectionResponse.parse(request.toHTTPRequest().send());
	}

	private static ClientSecretPost clientPost(String clientId, String secret) {
		return new ClientSecretPost(new ClientID(clientId), new Secret(secret));
	}

}
