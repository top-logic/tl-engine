/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.oauth.server;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebResponse;
import com.nimbusds.oauth2.sdk.AuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.util.URLUtils;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.oauth.server.AuthorizationRefusal;
import com.top_logic.oauth.server.I18NConstants;
import com.top_logic.oauth.server.OAuthAuthorizationServer;
import com.top_logic.oauth.server.OAuthException;
import com.top_logic.oauth.server.OAuthFunctions;
import com.top_logic.oauth.server.OAuthServerServlet;
import com.top_logic.oauth.server.ValidatedAuthorization;

/**
 * Test of the authorization endpoint {@link OAuthAuthorizationServer#AUTHORIZATION_PATH} and of the
 * decision about a pending authorization on the consent page.
 */
public class TestOAuthAuthorizationEndpoint extends AbstractOAuthServerTest {

	/**
	 * The variable the configured {@link AliasManager#HOST} alias is read from.
	 */
	private static final String HOST_VARIABLE = "tl_host";

	/**
	 * The address of the application on a network host, reached over plain HTTP.
	 */
	private static final String NETWORK_HOST = "http://app.example.com";

	/** The consent page configured for the test. */
	private static final String CONSENT_PAGE = "/view/oauth-consent.view.xml";

	/** The refusal page configured for the test. */
	private static final String REFUSAL_PAGE = "/view/oauth-refused.view.xml";

	/** The parameter of an authorization request naming the response type. */
	private static final String RESPONSE_TYPE_PARAMETER = "response_type";

	/** The HTTP header naming the target of a redirect. */
	private static final String LOCATION_HEADER = "Location";

	private boolean _autoRedirect;

	@Override
	protected void setUp() throws Exception {
		_autoRedirect = HttpUnitOptions.getAutoRedirect();
		// The redirects of the endpoint are what is tested, not their targets. Set before the client
		// is created, which takes the option over.
		HttpUnitOptions.setAutoRedirect(false);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		HttpUnitOptions.setAutoRedirect(_autoRedirect);
		super.tearDown();
	}

	/**
	 * A valid request is kept as pending authorization, and the browser is sent to the consent page
	 * with its identifier.
	 */
	public void testValidRequestRedirectsToConsentPage() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		State state = new State();

		String pending = pendingId(get(authorizePath(request(clientId, REDIRECT_URI, new CodeVerifier(), resource,
			state, CodeChallengeMethod.S256))));

		ValidatedAuthorization authorization = server().pendingAuthorization(pending);
		assertEquals(clientId, authorization.client().clientId());
		assertEquals(REDIRECT_URI, authorization.redirectUri());
		assertEquals(resource, authorization.resource());
		assertEquals(state, authorization.state());
	}

	/**
	 * An authorization request reaching the application over plain HTTP on a host other than the
	 * loopback interface is refused on the refusal page, since no conforming client could redeem
	 * the code.
	 */
	public void testPlainHttpOnNetworkHostShowsPage() throws Exception {
		String clientId = register(REDIRECT_URI);
		String path = authorizePath(request(clientId, REDIRECT_URI, new CodeVerifier(), resource(RESOURCE_PATH),
			new State(), CodeChallengeMethod.S256));

		String previous = System.getProperty(HOST_VARIABLE);
		System.setProperty(HOST_VARIABLE, NETWORK_HOST);
		AliasManager.getInstance().reload();
		try {
			String detail = assertRefusalPage(get(path), AuthorizationRefusal.INSECURE_CONNECTION);
			assertTrue(detail, detail.startsWith(NETWORK_HOST));
		} finally {
			if (previous == null) {
				System.clearProperty(HOST_VARIABLE);
			} else {
				System.setProperty(HOST_VARIABLE, previous);
			}
			AliasManager.getInstance().reload();
		}
	}

	/**
	 * An unknown client is told the person, never the redirect URI.
	 */
	public void testUnknownClientShowsPage() throws Exception {
		String clientId = OAuthAuthorizationServer.CLIENT_ID_PREFIX + "unknown";
		WebResponse response = get(authorizePath(request(clientId, REDIRECT_URI, new CodeVerifier(),
			resource(RESOURCE_PATH), new State(), CodeChallengeMethod.S256)));

		assertEquals(clientId, assertRefusalPage(response, AuthorizationRefusal.UNKNOWN_CLIENT));
	}

	/**
	 * Without a refusal page, a refusal is told on a plain page of the endpoint.
	 */
	public void testRefusalWithoutRefusalPage() throws Exception {
		OAuthAuthorizationServer.Config config = server().getConfig();
		PropertyDescriptor property = config.descriptor().getProperty(OAuthAuthorizationServer.Config.REFUSAL_PAGE);
		Object refusalPage = config.value(property);
		config.update(property, null);
		try {
			String clientId = OAuthAuthorizationServer.CLIENT_ID_PREFIX + "unknown";
			WebResponse response = get(authorizePath(request(clientId, REDIRECT_URI, new CodeVerifier(),
				resource(RESOURCE_PATH), new State(), CodeChallengeMethod.S256)));

			assertEquals(response.getText(), 400, response.getResponseCode());
			assertNull(response.getHeaderField(LOCATION_HEADER));
			assertTrue(response.getContentType(),
				response.getContentType().startsWith(OAuthServerServlet.HTML_MEDIA_TYPE));
			assertTrue(response.getText(), response.getText().contains(clientId));
		} finally {
			config.update(property, refusalPage);
		}
	}

	/**
	 * A request that cannot be parsed and names no known client is told the person.
	 */
	public void testMalformedRequestOfUnknownClientShowsPage() throws Exception {
		Map<String, List<String>> parameters = new HashMap<>(URLUtils.parseParameters(request(
			OAuthAuthorizationServer.CLIENT_ID_PREFIX + "unknown", REDIRECT_URI, new CodeVerifier(),
			resource(RESOURCE_PATH), new State(), CodeChallengeMethod.S256).toQueryString()));
		parameters.remove(RESPONSE_TYPE_PARAMETER);

		WebResponse response =
			get(OAuthAuthorizationServer.AUTHORIZATION_PATH + '?' + URLUtils.serializeParameters(parameters));

		assertNull(assertRefusalPage(response, AuthorizationRefusal.MALFORMED_REQUEST));
	}

	/**
	 * A redirect URI the client did not register is told the person, never redirected to.
	 */
	public void testUnregisteredRedirectUriShowsPage() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI other = URI.create("https://attacker.example.com/callback");

		WebResponse response = get(authorizePath(request(clientId, other, new CodeVerifier(),
			resource(RESOURCE_PATH), new State(), CodeChallengeMethod.S256)));

		assertEquals(other.toString(), assertRefusalPage(response, AuthorizationRefusal.UNREGISTERED_REDIRECT_URI));
	}

	/**
	 * A request without PKCE challenge is sent back to the client with the error and its state.
	 */
	public void testMissingCodeChallengeRedirectsWithError() throws Exception {
		String clientId = register(REDIRECT_URI);
		State state = new State();
		AuthorizationRequest request = new AuthorizationRequest.Builder(ResponseType.CODE, new ClientID(clientId))
			.redirectionURI(REDIRECT_URI)
			.resource(resource(RESOURCE_PATH))
			.state(state)
			.build();

		AuthorizationErrorResponse error = errorResponse(get(authorizePath(request)));
		assertEquals(OAuth2Error.INVALID_REQUEST.getCode(), error.getErrorObject().getCode());
		assertEquals(state, error.getState());
	}

	/**
	 * A request with the PKCE method <code>plain</code> is sent back to the client with the error.
	 */
	public void testPlainCodeChallengeRedirectsWithError() throws Exception {
		String clientId = register(REDIRECT_URI);
		State state = new State();

		AuthorizationErrorResponse error = errorResponse(get(authorizePath(request(clientId, REDIRECT_URI,
			new CodeVerifier(), resource(RESOURCE_PATH), state, CodeChallengeMethod.PLAIN))));
		assertEquals(OAuth2Error.INVALID_REQUEST.getCode(), error.getErrorObject().getCode());
		assertEquals(state, error.getState());
	}

	/**
	 * A request for a resource of another application is sent back to the client with the error.
	 */
	public void testForeignResourceRedirectsWithError() throws Exception {
		String clientId = register(REDIRECT_URI);
		State state = new State();

		AuthorizationErrorResponse error = errorResponse(get(authorizePath(request(clientId, REDIRECT_URI,
			new CodeVerifier(), URI.create("https://other.example.com/mcp"), state, CodeChallengeMethod.S256))));
		assertEquals(OAuth2Error.INVALID_TARGET.getCode(), error.getErrorObject().getCode());
		assertEquals(state, error.getState());
	}

	/**
	 * A request without response type cannot be parsed; it names a valid client and redirect URI, so
	 * the error goes back there.
	 */
	public void testMissingResponseTypeRedirectsWithError() throws Exception {
		String clientId = register(REDIRECT_URI);
		State state = new State();
		Map<String, List<String>> parameters = new HashMap<>(URLUtils.parseParameters(request(clientId, REDIRECT_URI,
			new CodeVerifier(), resource(RESOURCE_PATH), state, CodeChallengeMethod.S256).toQueryString()));
		parameters.remove(RESPONSE_TYPE_PARAMETER);
		String query = URLUtils.serializeParameters(parameters);

		AuthorizationErrorResponse error = errorResponse(get(OAuthAuthorizationServer.AUTHORIZATION_PATH + '?' + query));
		assertEquals(state, error.getState());
	}

	/**
	 * Allowing issues a code for the redirect URI and the state of the client, which the client
	 * exchanges for tokens. The pending authorization is answered then.
	 */
	public void testApproveIssuesCode() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		CodeVerifier verifier = new CodeVerifier();
		State state = new State();
		String pending = pendingId(get(authorizePath(request(clientId, REDIRECT_URI, verifier, resource, state,
			CodeChallengeMethod.S256))));

		AuthorizationResponse response = AuthorizationResponse.parse(server().approve(pending, root()));
		assertTrue(response.indicatesSuccess());
		AuthorizationSuccessResponse success = response.toSuccessResponse();
		assertEquals(state, success.getState());
		assertEquals(REDIRECT_URI, success.getRedirectionURI());

		TokenResponse tokens =
			exchange(success.getAuthorizationCode().getValue(), clientId, REDIRECT_URI, verifier, resource);
		assertTrue(String.valueOf(tokens), tokens.indicatesSuccess());

		assertRefused(I18NConstants.ERROR_UNKNOWN_PENDING_AUTHORIZATION, () -> server().approve(pending, root()));
		assertRefused(I18NConstants.ERROR_UNKNOWN_PENDING_AUTHORIZATION, () -> server().deny(pending));
	}

	/**
	 * Denying sends <code>access_denied</code> and the state to the client; the pending
	 * authorization cannot be allowed afterwards.
	 */
	public void testDenySendsAccessDenied() throws Exception {
		String clientId = register(REDIRECT_URI);
		State state = new State();
		String pending = pendingId(get(authorizePath(request(clientId, REDIRECT_URI, new CodeVerifier(),
			resource(RESOURCE_PATH), state, CodeChallengeMethod.S256))));

		AuthorizationResponse response = AuthorizationResponse.parse(server().deny(pending));
		assertFalse(response.indicatesSuccess());
		AuthorizationErrorResponse error = response.toErrorResponse();
		assertEquals(OAuth2Error.ACCESS_DENIED.getCode(), error.getErrorObject().getCode());
		assertEquals(state, error.getState());

		assertRefused(I18NConstants.ERROR_UNKNOWN_PENDING_AUTHORIZATION, () -> server().approve(pending, root()));
	}

	/**
	 * Only a logged-in person can allow a request.
	 */
	public void testApproveNeedsPerson() throws Exception {
		String clientId = register(REDIRECT_URI);
		String pending = pendingId(get(authorizePath(request(clientId, REDIRECT_URI, new CodeVerifier(),
			resource(RESOURCE_PATH), new State(), CodeChallengeMethod.S256))));

		assertRefused(I18NConstants.ERROR_CONSENT_WITHOUT_LOGIN, () -> server().approve(pending, null));
		// Still pending.
		assertNotNull(server().pendingAuthorization(pending));
	}

	/**
	 * An expired pending authorization can be neither allowed nor denied, and the clean-up removes
	 * it.
	 */
	public void testExpiredPendingAuthorization() throws Exception {
		String clientId = register(REDIRECT_URI);
		String pending = pendingId(get(authorizePath(request(clientId, REDIRECT_URI, new CodeVerifier(),
			resource(RESOURCE_PATH), new State(), CodeChallengeMethod.S256))));
		expirePending(pending);

		assertRefused(I18NConstants.ERROR_PENDING_AUTHORIZATION_EXPIRED, () -> server().pendingAuthorization(pending));
		assertRefused(I18NConstants.ERROR_PENDING_AUTHORIZATION_EXPIRED, () -> server().approve(pending, root()));
		assertRefused(I18NConstants.ERROR_PENDING_AUTHORIZATION_EXPIRED, () -> server().deny(pending));

		OAuthAuthorizationServer.CleanUpResult result = server().cleanUp(Instant.now().plus(Duration.ofMinutes(1)));
		assertTrue(String.valueOf(result), result.pendingAuthorizations() >= 1);
		assertRefused(I18NConstants.ERROR_UNKNOWN_PENDING_AUTHORIZATION, () -> server().pendingAuthorization(pending));
	}

	/**
	 * An identifier nobody issued is refused.
	 */
	public void testUnknownPendingAuthorization() throws Exception {
		String unknown = OAuthAuthorizationServer.PENDING_AUTHORIZATION_PREFIX + "unknown";
		assertRefused(I18NConstants.ERROR_UNKNOWN_PENDING_AUTHORIZATION, () -> server().pendingAuthorization(unknown));
		assertRefused(I18NConstants.ERROR_UNKNOWN_PENDING_AUTHORIZATION, () -> server().approve(unknown, root()));
		assertRefused(I18NConstants.ERROR_UNKNOWN_PENDING_AUTHORIZATION, () -> server().pendingAuthorization(null));
	}

	/**
	 * The TL-Script functions of the consent page describe an open request and name the reason for
	 * one that cannot be decided.
	 */
	public void testConsentPageFunctions() throws Exception {
		String clientId = register(REDIRECT_URI);
		URI resource = resource(RESOURCE_PATH);
		String pending = pendingId(get(authorizePath(request(clientId, REDIRECT_URI, new CodeVerifier(), resource,
			new State(), CodeChallengeMethod.S256))));

		assertNull(OAuthFunctions.refusal(pending));
		Map<String, Object> request = OAuthFunctions.request(pending);
		assertEquals("Test client", request.get(OAuthFunctions.CLIENT_KEY));
		assertEquals(clientId, request.get(OAuthFunctions.CLIENT_ID_KEY));
		assertNull(request.get(OAuthFunctions.CLIENT_HOST_KEY));
		assertEquals(REDIRECT_URI.getHost(), request.get(OAuthFunctions.REDIRECT_HOST_KEY));
		assertEquals(resource.toString(), request.get(OAuthFunctions.RESOURCE_KEY));

		server().deny(pending);
		assertEquals(I18NConstants.ERROR_UNKNOWN_PENDING_AUTHORIZATION, OAuthFunctions.refusal(pending));
		assertNull(OAuthFunctions.request(pending));
	}

	/**
	 * The TL-Script functions of the refusal page name the reason with the given detail, and fall
	 * back to a general text for a reason that is not known.
	 */
	public void testRefusalPageFunctions() {
		assertEquals(I18NConstants.ERROR_AUTHORIZATION_WITHOUT_TLS__URL.fill(NETWORK_HOST),
			OAuthFunctions.refusalMessage(AuthorizationRefusal.INSECURE_CONNECTION.name(), NETWORK_HOST));
		assertEquals(I18NConstants.REFUSAL_HINT_INSECURE_CONNECTION__LOCAL_URL.fill("http://localhost:8080/app"),
			OAuthFunctions.refusalHint(AuthorizationRefusal.INSECURE_CONNECTION.name(), "http://ai:8080/app"));
		assertEquals(I18NConstants.REFUSAL_HINT_INSECURE_CONNECTION,
			OAuthFunctions.refusalHint(AuthorizationRefusal.INSECURE_CONNECTION.name(), null));
		assertEquals(I18NConstants.REFUSAL_UNKNOWN_CLIENT__CLIENT.fill("client"),
			OAuthFunctions.refusalMessage(AuthorizationRefusal.UNKNOWN_CLIENT.name(), "client"));
		assertEquals(I18NConstants.REFUSAL_UNREGISTERED_REDIRECT_URI__URI.fill(REDIRECT_URI.toString()),
			OAuthFunctions.refusalMessage(AuthorizationRefusal.UNREGISTERED_REDIRECT_URI.name(),
				REDIRECT_URI.toString()));
		assertEquals(I18NConstants.REFUSAL_MALFORMED_REQUEST,
			OAuthFunctions.refusalMessage(AuthorizationRefusal.MALFORMED_REQUEST.name(), null));
		assertEquals(I18NConstants.REFUSAL_HINT_RETRY,
			OAuthFunctions.refusalHint(AuthorizationRefusal.UNKNOWN_CLIENT.name(), "client"));

		// Missing detail, unknown or missing reason.
		assertEquals(I18NConstants.REFUSAL_UNKNOWN_REASON,
			OAuthFunctions.refusalMessage(AuthorizationRefusal.UNKNOWN_CLIENT.name(), null));
		assertEquals(I18NConstants.REFUSAL_UNKNOWN_REASON, OAuthFunctions.refusalMessage("garbled", "x"));
		assertEquals(I18NConstants.REFUSAL_UNKNOWN_REASON, OAuthFunctions.refusalMessage(null, null));
		assertEquals(I18NConstants.REFUSAL_HINT_RETRY, OAuthFunctions.refusalHint("garbled", null));
	}

	private AuthorizationRequest request(String clientId, URI redirectUri, CodeVerifier verifier, URI resource,
			State state, CodeChallengeMethod method) {
		return new AuthorizationRequest.Builder(ResponseType.CODE, new ClientID(clientId))
			.redirectionURI(redirectUri)
			.codeChallenge(verifier, method)
			.resource(resource)
			.state(state)
			.build();
	}

	private static String authorizePath(AuthorizationRequest request) {
		return OAuthAuthorizationServer.AUTHORIZATION_PATH + '?' + request.toQueryString();
	}

	/**
	 * The identifier of the pending authorization the given answer redirects to the consent page
	 * with.
	 */
	private static String pendingId(WebResponse response) throws Exception {
		assertEquals(response.getText(), 302, response.getResponseCode());
		String location = response.getHeaderField(LOCATION_HEADER);
		URI target = URI.create(location);
		assertTrue(location, target.getPath().endsWith(CONSENT_PAGE));
		List<String> values = URLUtils.parseParameters(target.getRawQuery()).get(OAuthAuthorizationServer.PENDING_PARAMETER);
		assertEquals(location, 1, values.size());
		String result = values.get(0);
		assertTrue(result, result.startsWith(OAuthAuthorizationServer.PENDING_AUTHORIZATION_PREFIX));
		assertTrue(location, location.endsWith(OAuthServerServlet.consentUrl("", CONSENT_PAGE, result)));
		return result;
	}

	private static AuthorizationErrorResponse errorResponse(WebResponse response) throws Exception {
		assertEquals(response.getText(), 302, response.getResponseCode());
		URI location = URI.create(response.getHeaderField(LOCATION_HEADER));
		assertEquals(REDIRECT_URI.getHost(), location.getHost());
		assertEquals(REDIRECT_URI.getPath(), location.getPath());
		AuthorizationResponse parsed = AuthorizationResponse.parse(location);
		assertFalse(parsed.indicatesSuccess());
		return parsed.toErrorResponse();
	}

	/**
	 * Checks that the given answer redirects to the refusal page with the given reason.
	 *
	 * @return The detail of the refusal, <code>null</code> if there is none.
	 */
	private static String assertRefusalPage(WebResponse response, AuthorizationRefusal expectedReason)
			throws Exception {
		assertEquals(response.getText(), 302, response.getResponseCode());
		String location = response.getHeaderField(LOCATION_HEADER);
		URI target = URI.create(location);
		assertTrue(location, target.getPath().endsWith(REFUSAL_PAGE));
		Map<String, List<String>> parameters = URLUtils.parseParameters(target.getRawQuery());
		assertEquals(location, List.of(expectedReason.name()), parameters.get(OAuthAuthorizationServer.REASON_PARAMETER));
		List<String> detail = parameters.get(OAuthAuthorizationServer.DETAIL_PARAMETER);
		if (detail == null) {
			return null;
		}
		assertEquals(location, 1, detail.size());
		return detail.get(0);
	}

	private interface Refusable {
		void run() throws OAuthException;
	}

	private static void assertRefused(Object expectedReason, Refusable action) {
		try {
			action.run();
		} catch (OAuthException ex) {
			assertEquals(ex.description(), expectedReason, ex.getErrorKey());
			return;
		}
		fail("Expected refusal: " + expectedReason);
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return suite(TestOAuthAuthorizationEndpoint.class);
	}

}
