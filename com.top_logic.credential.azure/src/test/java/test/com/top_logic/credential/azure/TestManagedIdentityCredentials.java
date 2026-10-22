/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.credential.azure;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;
import com.top_logic.credential.azure.I18NConstants;
import com.top_logic.credential.azure.ManagedIdentityCredentials;

/**
 * Test case for {@link ManagedIdentityCredentials} against mocks of the instance metadata service
 * and the identity endpoint of an App Service.
 */
@SuppressWarnings("javadoc")
public class TestManagedIdentityCredentials extends AbstractAzureMockTest {

	private static final String IMDS_PATH = "/metadata/identity/oauth2/token";

	private static final String APP_SERVICE_PATH = "/msi/token";

	private static final String RESOURCE = "https://cognitiveservices.azure.com";

	private static final String CLIENT_ID = "11111111-2222-3333-4444-555555555555";

	private static final String IDENTITY_HEADER = "identity-header-value";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		respond(IMDS_PATH, (count, request) -> new Object[] { 200, imdsToken("imds-token-" + count, "3599") });
		respond(APP_SERVICE_PATH,
			(count, request) -> new Object[] { 200, appServiceToken("app-token-" + count, 3600) });
	}

	static String imdsToken(String token, String expiresIn) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("access_token", token);
		json.put("refresh_token", "");
		// Azure sends numbers as strings.
		json.put("expires_in", expiresIn);
		json.put("expires_on", Long.toString(System.currentTimeMillis() / 1000 + Long.parseLong(expiresIn)));
		json.put("not_before", Long.toString(System.currentTimeMillis() / 1000));
		json.put("resource", RESOURCE);
		json.put("token_type", "Bearer");
		return JSON.toString(json);
	}

	static String appServiceToken(String token, long lifetime) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("access_token", token);
		json.put("expires_on", Long.toString(System.currentTimeMillis() / 1000 + lifetime));
		json.put("resource", RESOURCE);
		json.put("token_type", "Bearer");
		json.put("client_id", CLIENT_ID);
		return JSON.toString(json);
	}

	private CredentialProvider imds(String attributes) throws Exception {
		return credentials("<credentials class='" + ManagedIdentityCredentials.class.getName()
			+ "' resource='" + RESOURCE + "' endpoint='" + url(IMDS_PATH) + "' retry-delay='10ms' " + attributes
			+ "/>");
	}

	private CredentialProvider autoDetected(String attributes) throws Exception {
		return credentials("<credentials class='" + ManagedIdentityCredentials.class.getName()
			+ "' resource='" + RESOURCE + "' retry-delay='10ms' " + attributes + "/>");
	}

	private void setAppServiceEnvironment() {
		System.setProperty(ManagedIdentityCredentials.ENV_IDENTITY_ENDPOINT, url(APP_SERVICE_PATH));
		System.setProperty(ManagedIdentityCredentials.ENV_IDENTITY_HEADER, IDENTITY_HEADER);
	}

	public void testImds() throws Exception {
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-1", authenticate(provider));
		assertEquals("Token must be cached.", "Bearer imds-token-1", authenticate(provider));

		assertEquals(1, requests(IMDS_PATH).size());
		Received request = requests(IMDS_PATH).get(0);
		assertEquals("true", request.header("Metadata"));
		assertNull(request.header("X-IDENTITY-HEADER"));
		Map<String, String> expected = new LinkedHashMap<>();
		expected.put("api-version", "2018-02-01");
		expected.put("resource", RESOURCE);
		assertEquals(expected, request._query);
	}

	public void testImdsUserAssigned() throws Exception {
		CredentialProvider provider = imds("client-id='" + CLIENT_ID + "'");

		assertEquals("Bearer imds-token-1", authenticate(provider));

		Received request = requests(IMDS_PATH).get(0);
		assertEquals(CLIENT_ID, request.parameter("client_id"));
		assertEquals(RESOURCE, request.parameter("resource"));
		assertEquals("2018-02-01", request.parameter("api-version"));
	}

	public void testStringLifetimeHonoured() throws Exception {
		respond(IMDS_PATH, (count, request) -> new Object[] { 200, imdsToken("imds-token-" + count, "1") });
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-1", authenticate(provider));
		// Renewed after half of the lifetime of 1 s.
		Thread.sleep(700);
		assertEquals("Bearer imds-token-2", authenticate(provider));
	}

	public void testInvalidate() throws Exception {
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-1", authenticate(provider));
		provider.invalidate();
		assertEquals("Bearer imds-token-2", authenticate(provider));
	}

	public void testAppService() throws Exception {
		setAppServiceEnvironment();
		CredentialProvider provider = autoDetected("client-id='" + CLIENT_ID + "'");

		assertEquals("Bearer app-token-1", authenticate(provider));
		assertEquals("Token must be cached.", "Bearer app-token-1", authenticate(provider));

		assertEquals(1, requests(APP_SERVICE_PATH).size());
		assertEquals(0, requests(IMDS_PATH).size());
		Received request = requests(APP_SERVICE_PATH).get(0);
		assertEquals(IDENTITY_HEADER, request.header("X-IDENTITY-HEADER"));
		assertNull(request.header("Metadata"));
		Map<String, String> expected = new LinkedHashMap<>();
		expected.put("api-version", "2019-08-01");
		expected.put("resource", RESOURCE);
		expected.put("client_id", CLIENT_ID);
		assertEquals(expected, request._query);
	}

	public void testAppServiceExpiresOn() throws Exception {
		setAppServiceEnvironment();
		respond(APP_SERVICE_PATH, (count, request) -> new Object[] { 200, appServiceToken("app-token-" + count, 1) });
		CredentialProvider provider = autoDetected("");

		assertEquals("Bearer app-token-1", authenticate(provider));
		// The lifetime taken from expires_on is at most 1 s, renewal after half of it.
		Thread.sleep(700);
		assertEquals("Bearer app-token-2", authenticate(provider));
	}

	public void testConfiguredEndpointWins() throws Exception {
		setAppServiceEnvironment();
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-1", authenticate(provider));
		assertEquals(0, requests(APP_SERVICE_PATH).size());
	}

	public void testRetryOnServerError() throws Exception {
		respond(IMDS_PATH, (count, request) -> count == 1
			? new Object[] { 500, "{\"error\":\"unknown_error\"}" }
			: new Object[] { 200, imdsToken("imds-token-" + count, "3600") });
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-2", authenticate(provider));
		assertEquals(2, requests(IMDS_PATH).size());
	}

	public void testRetryLimit() throws Exception {
		respond(IMDS_PATH, (count, request) -> new Object[] { 429, "{\"error\":\"too_many_requests\"}" });
		CredentialProvider provider = imds("max-attempts='3'");

		try {
			authenticate(provider);
			fail("Throttling not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_TOKEN_REQUEST_FAILED__RESOURCE_URL_STATUS_DETAIL.fill(RESOURCE,
				url(IMDS_PATH), 429, "too_many_requests"), ex.getErrorKey());
		}
		assertEquals(3, requests(IMDS_PATH).size());
	}

	public void testIdentityNotFound() throws Exception {
		respond(IMDS_PATH, (count, request) -> new Object[] { 400,
			"{\"error\":\"invalid_request\",\"error_description\":\"Identity not found\","
				+ "\"access_token\":\"leaked-token\"}" });
		CredentialProvider provider = imds("client-id='" + CLIENT_ID + "'");

		try {
			authenticate(provider);
			fail("Missing identity not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_TOKEN_REQUEST_FAILED__RESOURCE_URL_STATUS_DETAIL.fill(RESOURCE,
				url(IMDS_PATH), 400, "invalid_request: Identity not found"), ex.getErrorKey());
			assertNotRevealed(ex, "leaked-token");
		}
		assertEquals("A client error must not be repeated.", 1, requests(IMDS_PATH).size());
	}

	public void testMalformedResponse() throws Exception {
		respond(IMDS_PATH, (count, request) -> new Object[] { 200, "<html>secret-token</html>" });
		CredentialProvider provider = imds("");

		try {
			authenticate(provider);
			fail("Malformed response not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(url(IMDS_PATH)), ex.getErrorKey());
			assertNotRevealed(ex, "secret-token");
		}
	}

	public void testUnreachable() throws Exception {
		CredentialProvider provider = credentials("<credentials class='" + ManagedIdentityCredentials.class.getName()
			+ "' resource='" + RESOURCE + "' endpoint='http://localhost:1/token' connect-timeout='500ms'/>");

		try {
			authenticate(provider);
			fail("Unreachable endpoint not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_IDENTITY_ENDPOINT_UNREACHABLE__URL.fill("http://localhost:1/token"),
				ex.getErrorKey());
		}
	}

	public static Test suite() {
		return suite(TestManagedIdentityCredentials.class);
	}

}
