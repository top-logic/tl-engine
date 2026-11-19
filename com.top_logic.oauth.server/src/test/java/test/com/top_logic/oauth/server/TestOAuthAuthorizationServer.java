/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.oauth.server;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;

import com.meterware.httpunit.WebResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationErrorResponse;
import com.nimbusds.oauth2.sdk.client.RegistrationError;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.BearerTokenError;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.oauth.server.OAuthAuthorizationServer;
import com.top_logic.oauth.server.OAuthClientInfo;
import com.top_logic.oauth.server.OAuthException;

/**
 * Test of the {@link OAuthAuthorizationServer} through its HTTP endpoints: metadata, client
 * registration, client ID metadata documents, the code exchange and refresh token rotation.
 */
public class TestOAuthAuthorizationServer extends AbstractOAuthServerTest {

	/**
	 * The client ID of a client described by a metadata document.
	 *
	 * <p>
	 * Every test uses its own, because the server keeps fetched documents.
	 * </p>
	 */
	private String documentClientId() {
		return "https://client.example.com/oauth/" + getName().replace(' ', '-') + ".json";
	}

	/**
	 * The metadata names the issuer, the endpoints and exactly what the server supports.
	 */
	public void testMetadata() throws Exception {
		AuthorizationServerMetadata metadata = metadata();
		String issuer = metadata.getIssuer().getValue();

		assertEquals(issuer + OAuthAuthorizationServer.AUTHORIZATION_PATH,
			metadata.getAuthorizationEndpointURI().toString());
		assertEquals(issuer + OAuthAuthorizationServer.TOKEN_PATH, metadata.getTokenEndpointURI().toString());
		assertEquals(issuer + OAuthAuthorizationServer.REGISTRATION_PATH,
			metadata.getRegistrationEndpointURI().toString());
		assertEquals(List.of(CodeChallengeMethod.S256), metadata.getCodeChallengeMethods());
		assertEquals(Set.of(GrantType.AUTHORIZATION_CODE, GrantType.REFRESH_TOKEN),
			Set.copyOf(metadata.getGrantTypes()));
		assertEquals(List.of(ClientAuthenticationMethod.NONE), metadata.getTokenEndpointAuthMethods());
		assertEquals(Boolean.TRUE,
			metadata.getCustomParameter(OAuthAuthorizationServer.CLIENT_ID_METADATA_DOCUMENT_SUPPORTED));
	}

	/**
	 * The OpenID Connect discovery path serves the same document.
	 */
	public void testMetadataAtOpenIdConfigurationPath() throws Exception {
		WebResponse plain = get(OAuthAuthorizationServer.METADATA_PATH);
		WebResponse openId = get(OAuthAuthorizationServer.OPENID_CONFIGURATION_PATH);

		assertEquals(200, openId.getResponseCode());
		assertEquals(plain.getText(), openId.getText());
	}

	/**
	 * A client registers itself and then completes the code flow.
	 */
	public void testRegisteredClientSignsIn() throws Exception {
		String clientId = register(REDIRECT_URI);
		assertTrue(clientId, clientId.startsWith(OAuthAuthorizationServer.CLIENT_ID_PREFIX));

		OAuthClientInfo client = server().resolveClient(clientId);
		assertEquals("Test client", client.clientName());
		assertEquals(Set.of(REDIRECT_URI), client.redirectUris());

		Tokens tokens = signIn(clientId);
		assertTrue(tokens.getAccessToken().getValue().startsWith(OAuthAuthorizationServer.ACCESS_TOKEN_PREFIX));
		assertTrue(tokens.getRefreshToken().getValue().startsWith(OAuthAuthorizationServer.REFRESH_TOKEN_PREFIX));
	}

	/**
	 * A registration with a redirect URI that is neither https nor loopback is refused.
	 */
	public void testRegistrationRefusesPlainHttpRedirect() throws Exception {
		WebResponse response = postRegistration(URI.create("http://client.example.com/callback"));

		assertEquals(400, response.getResponseCode());
		ClientRegistrationErrorResponse error =
			ClientRegistrationErrorResponse.parse(httpResponse(response));
		assertEquals(RegistrationError.INVALID_REDIRECT_URI.getCode(), error.getErrorObject().getCode());
	}

	/**
	 * A client whose metadata document names it is accepted, and its port-less loopback redirect
	 * URI matches a request on any port.
	 */
	public void testMetadataDocumentClient() throws Exception {
		String clientId = documentClientId();
		publishDocument(clientId, clientId, URI.create("http://127.0.0.1/callback"));

		OAuthClientInfo client = server().resolveClient(clientId);
		assertEquals(clientId, client.clientId());

		CodeVerifier verifier = new CodeVerifier();
		URI resource = resource(RESOURCE_PATH);
		String code = authorize(clientId, REDIRECT_URI, verifier, resource);
		TokenResponse response = exchange(code, clientId, REDIRECT_URI, verifier, resource);
		assertTrue(String.valueOf(response), response.indicatesSuccess());
	}

	/**
	 * A metadata document naming another client ID is refused.
	 */
	public void testMetadataDocumentForOtherClient() throws Exception {
		String clientId = documentClientId();
		publishDocument(clientId, "https://other.example.com/client.json", REDIRECT_URI);

		assertOAuthError(OAuth2Error.INVALID_CLIENT, () -> server().resolveClient(clientId));
	}

	/**
	 * A client ID URL whose document cannot be fetched is refused.
	 */
	public void testMetadataDocumentMissing() throws Exception {
		assertOAuthError(OAuth2Error.INVALID_CLIENT,
			() -> server().resolveClient("https://client.example.com/missing.json"));
	}

	/**
	 * A client ID that is a plain http URL is no metadata document client and no registered one.
	 */
	public void testMetadataDocumentNeedsHttps() throws Exception {
		assertOAuthError(OAuth2Error.INVALID_CLIENT,
			() -> server().resolveClient("http://client.example.com/client.json"));
	}

	/**
	 * An authorization request for a redirect URI the client did not register is refused without
	 * redirect.
	 */
	public void testAuthorizationWithUnregisteredRedirect() throws Exception {
		String clientId = register(REDIRECT_URI);

		OAuthException ex = assertOAuthError(OAuth2Error.INVALID_REQUEST, () -> authorize(clientId,
			URI.create("https://attacker.example.com/callback"), new CodeVerifier(), resource(RESOURCE_PATH)));
		assertFalse(ex.isRedirectable());
	}

	/**
	 * An authorization request for a resource outside the application is refused.
	 */
	public void testAuthorizationForForeignResource() throws Exception {
		String clientId = register(REDIRECT_URI);

		OAuthException ex = assertOAuthError(OAuth2Error.INVALID_TARGET, () -> authorize(clientId, REDIRECT_URI,
			new CodeVerifier(), URI.create("https://elsewhere.example.com/mcp")));
		assertTrue(ex.isRedirectable());
	}

	/**
	 * The code is exchanged only with the verifier of its challenge.
	 */
	public void testWrongVerifier() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		String code = authorize(clientId, REDIRECT_URI, new CodeVerifier(), resource);

		assertTokenError(OAuth2Error.INVALID_GRANT,
			exchange(code, clientId, REDIRECT_URI, new CodeVerifier(), resource));
	}

	/**
	 * A code is exchanged once; a second exchange is refused and revokes what the first one got.
	 */
	public void testReusedCode() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		CodeVerifier verifier = new CodeVerifier();
		String code = authorize(clientId, REDIRECT_URI, verifier, resource);

		TokenResponse first = exchange(code, clientId, REDIRECT_URI, verifier, resource);
		assertTrue(String.valueOf(first), first.indicatesSuccess());
		TokenResponse second = exchange(code, clientId, REDIRECT_URI, verifier, resource);
		assertTokenError(OAuth2Error.INVALID_GRANT, second);

		RefreshToken refresh = first.toSuccessResponse().getTokens().getRefreshToken();
		assertTokenError(OAuth2Error.INVALID_GRANT, token(new RefreshTokenGrant(refresh), clientId, null));
	}

	/**
	 * An expired code is refused.
	 */
	public void testExpiredCode() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		CodeVerifier verifier = new CodeVerifier();
		String code = authorize(clientId, REDIRECT_URI, verifier, resource);
		expireCode(code);

		assertTokenError(OAuth2Error.INVALID_GRANT, exchange(code, clientId, REDIRECT_URI, verifier, resource));
	}

	/**
	 * A code is exchanged only with the redirect URI of its authorization request.
	 */
	public void testWrongRedirectUri() throws Exception {
		URI other = URI.create("http://127.0.0.1:33418/other");
		String clientId = register(REDIRECT_URI, other);
		URI resource = resource(RESOURCE_PATH);
		CodeVerifier verifier = new CodeVerifier();
		String code = authorize(clientId, REDIRECT_URI, verifier, resource);

		assertTokenError(OAuth2Error.INVALID_GRANT, exchange(code, clientId, other, verifier, resource));
	}

	/**
	 * A code is exchanged only for the resource of its authorization request.
	 */
	public void testWrongResource() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		CodeVerifier verifier = new CodeVerifier();
		String code = authorize(clientId, REDIRECT_URI, verifier, resource);

		assertTokenError(OAuth2Error.INVALID_TARGET,
			exchange(code, clientId, REDIRECT_URI, verifier, resource("/a2a")));
	}

	/**
	 * A code is exchanged only by the client it was issued to.
	 */
	public void testCodeOfOtherClient() throws Exception {
		String clientId = register(REDIRECT_URI);
		String otherClientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		CodeVerifier verifier = new CodeVerifier();
		String code = authorize(clientId, REDIRECT_URI, verifier, resource);

		assertTokenError(OAuth2Error.INVALID_GRANT, exchange(code, otherClientId, REDIRECT_URI, verifier, resource));
	}

	/**
	 * A refresh token is exchanged for a new pair once; presenting it again revokes the grant, the
	 * new refresh token included.
	 */
	public void testRefreshRotation() throws Exception {
		String clientId = register(REDIRECT_URI);
		Tokens first = signIn(clientId);

		TokenResponse refreshed = token(new RefreshTokenGrant(first.getRefreshToken()), clientId, null);
		assertTrue(String.valueOf(refreshed), refreshed.indicatesSuccess());
		Tokens second = refreshed.toSuccessResponse().getTokens();
		assertFalse(second.getRefreshToken().getValue().equals(first.getRefreshToken().getValue()));
		assertFalse(second.getAccessToken().getValue().equals(first.getAccessToken().getValue()));

		TokenResponse reused = token(new RefreshTokenGrant(first.getRefreshToken()), clientId, null);
		assertTokenError(OAuth2Error.INVALID_GRANT, reused);

		TokenResponse afterRevocation = token(new RefreshTokenGrant(second.getRefreshToken()), clientId, null);
		assertTokenError(OAuth2Error.INVALID_GRANT, afterRevocation);
	}

	/**
	 * A refresh token is exchanged only by its client and for its resource.
	 */
	public void testRefreshChecksClientAndResource() throws Exception {
		String clientId = register(REDIRECT_URI);
		String otherClientId = register(REDIRECT_URI);
		Tokens tokens = signIn(clientId);
		RefreshTokenGrant grant = new RefreshTokenGrant(tokens.getRefreshToken());

		assertTokenError(OAuth2Error.INVALID_GRANT, token(grant, otherClientId, null));
		assertTokenError(OAuth2Error.INVALID_TARGET, token(grant, clientId, resource("/a2a")));

		TokenResponse refreshed = token(grant, clientId, resource(RESOURCE_PATH));
		assertTrue(String.valueOf(refreshed), refreshed.indicatesSuccess());
	}

	/**
	 * An expired refresh token is refused.
	 */
	public void testExpiredRefreshToken() throws Exception {
		String clientId = register(REDIRECT_URI);
		Tokens tokens = signIn(clientId);
		expireToken(tokens.getRefreshToken().getValue());

		assertTokenError(OAuth2Error.INVALID_GRANT, token(new RefreshTokenGrant(tokens.getRefreshToken()), clientId, null));
	}

	/**
	 * A registered client nobody authorized is removed after the unused-client lifetime.
	 */
	public void testCleanUpRemovesUnusedClient() throws Exception {
		String clientId = register(REDIRECT_URI);
		Instant now = Instant.now();

		server().cleanUp(now.plus(Duration.ofHours(1)));
		assertEquals(clientId, server().resolveClient(clientId).clientId());

		server().cleanUp(now.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(1)));
		assertOAuthError(OAuth2Error.INVALID_CLIENT, () -> server().resolveClient(clientId));
	}

	/**
	 * Expired tokens go, a grant goes with its last token, and its client stays until the
	 * abandoned-client lifetime has passed since its last use.
	 */
	public void testCleanUpOfGrantAndClient() throws Exception {
		String clientId = register(REDIRECT_URI);
		Tokens tokens = signIn(clientId);
		String resource = resource(RESOURCE_PATH).toString();
		Instant now = Instant.now();

		server().cleanUp(now.plus(Duration.ofHours(2)));
		assertOAuthError(BearerTokenError.INVALID_TOKEN,
			() -> server().authenticate(tokens.getAccessToken().getValue(), resource));
		assertNotNull("The grant lives while its refresh token does.", grantOf(tokens.getRefreshToken().getValue()));

		server().cleanUp(now.plus(Duration.ofDays(31)));
		assertTokenError(OAuth2Error.INVALID_GRANT,
			token(new RefreshTokenGrant(tokens.getRefreshToken()), clientId, null));
		assertEquals("The client was used recently.", clientId, server().resolveClient(clientId).clientId());

		server().cleanUp(now.plus(Duration.ofDays(91)));
		assertOAuthError(OAuth2Error.INVALID_CLIENT, () -> server().resolveClient(clientId));
	}

	/**
	 * An exchanged refresh token survives the clean-up until it expires, so that its reuse still
	 * revokes the grant.
	 */
	public void testCleanUpKeepsExchangedRefreshToken() throws Exception {
		String clientId = register(REDIRECT_URI);
		Tokens first = signIn(clientId);
		TokenResponse refreshed = token(new RefreshTokenGrant(first.getRefreshToken()), clientId, null);
		assertTrue(String.valueOf(refreshed), refreshed.indicatesSuccess());
		Tokens second = refreshed.toSuccessResponse().getTokens();

		server().cleanUp(Instant.now().plus(Duration.ofHours(2)));

		assertTokenError(OAuth2Error.INVALID_GRANT,
			token(new RefreshTokenGrant(first.getRefreshToken()), clientId, null));
		assertTokenError(OAuth2Error.INVALID_GRANT,
			token(new RefreshTokenGrant(second.getRefreshToken()), clientId, null));
	}

	/**
	 * An expired authorization code is removed.
	 */
	public void testCleanUpRemovesExpiredCode() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		CodeVerifier verifier = new CodeVerifier();
		String code = authorize(clientId, REDIRECT_URI, verifier, resource);

		OAuthAuthorizationServer.CleanUpResult result =
			server().cleanUp(Instant.now().plus(Duration.ofMinutes(2)));
		assertTrue(String.valueOf(result), result.codes() >= 1);

		assertTokenError(OAuth2Error.INVALID_GRANT, exchange(code, clientId, REDIRECT_URI, verifier, resource));
	}

	/**
	 * Two exchanges of one code at the same time yield at most one token pair.
	 */
	public void testConcurrentCodeExchange() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		for (int round = 0; round < 5; round++) {
			CodeVerifier verifier = new CodeVerifier();
			String code = authorize(clientId, REDIRECT_URI, verifier, resource);
			assertAtMostOnePair("Round " + round, new TokenRequest.Builder(tokenEndpoint(), new ClientID(clientId),
				new AuthorizationCodeGrant(new AuthorizationCode(code), REDIRECT_URI, verifier)).build());
		}
	}

	/**
	 * Two exchanges of one refresh token at the same time yield at most one token pair.
	 */
	public void testConcurrentRefresh() throws Exception {
		String clientId = register(REDIRECT_URI);
		for (int round = 0; round < 5; round++) {
			Tokens tokens = signIn(clientId);
			assertAtMostOnePair("Round " + round, new TokenRequest.Builder(tokenEndpoint(), new ClientID(clientId),
				new RefreshTokenGrant(tokens.getRefreshToken())).build());
		}
	}

	/**
	 * Sends the given token request twice at the same time and asserts that at most one of them
	 * succeeds, and that the other is refused with <code>invalid_grant</code>.
	 */
	private static void assertAtMostOnePair(String message, TokenRequest request) throws Exception {
		CyclicBarrier start = new CyclicBarrier(2);
		List<Future<TokenResponse>> results = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			for (int n = 0; n < 2; n++) {
				results.add(executor.submit(() -> {
					start.await();
					return ThreadContext.<TokenResponse, RuntimeException, RuntimeException> inSystemContext(
						TestOAuthAuthorizationServer.class, () -> server().token(request));
				}));
			}
			int successes = 0;
			for (Future<TokenResponse> result : results) {
				TokenResponse response = result.get(30, TimeUnit.SECONDS);
				if (response.indicatesSuccess()) {
					successes++;
				} else {
					assertEquals(message, OAuth2Error.INVALID_GRANT.getCode(),
						response.toErrorResponse().getErrorObject().getCode());
				}
			}
			assertTrue(message + ": " + successes + " token pairs for one grant.", successes <= 1);
		} finally {
			executor.shutdownNow();
		}
	}

	private static void publishDocument(String url, String clientId, URI redirectUri) {
		ClientInformation information = new ClientInformation(new ClientID(clientId), null,
			clientMetadata(redirectUri), null);
		StaticFetcher.DOCUMENTS.put(url, information.toJSONObject().toJSONString());
	}

	private static void assertTokenError(ErrorObject expected, TokenResponse response) {
		assertFalse("Refusal expected.", response.indicatesSuccess());
		assertEquals(expected.getCode(), response.toErrorResponse().getErrorObject().getCode());
	}

	/**
	 * Something that is expected to fail with an {@link OAuthException}.
	 */
	interface Refused {
		/** Runs the refused action. */
		void run() throws Exception;
	}

	private static OAuthException assertOAuthError(ErrorObject expected, Refused action) throws Exception {
		try {
			action.run();
		} catch (OAuthException ex) {
			assertEquals(ex.description(), expected.getCode(), ex.getError().getCode());
			return ex;
		}
		fail("Expected OAuth error " + expected.getCode());
		return null;
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return suite(TestOAuthAuthorizationServer.class);
	}

}
