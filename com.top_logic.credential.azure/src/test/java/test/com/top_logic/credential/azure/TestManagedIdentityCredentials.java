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
 * Test case for {@link ManagedIdentityCredentials} against a mock of the instance metadata
 * service.
 *
 * <p>
 * The requests of the Azure Identity library to the instance metadata service are redirected to
 * the mock by the {@link ImdsRedirectHttpClientProvider}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestManagedIdentityCredentials extends AbstractAzureMockTest {

	private static final String COGNITIVE_SERVICES = "https://cognitiveservices.azure.com";

	private static final String CLIENT_ID = "11111111-2222-3333-4444-555555555555";

	private String _resource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_resource = uniqueResource(COGNITIVE_SERVICES);
		respond(IMDS_PATH, (count, request) -> new Object[] { 200, imdsToken("imds-token-" + count, 3599) });
	}

	static String imdsToken(String token, long lifetime) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("access_token", token);
		json.put("refresh_token", "");
		// Azure sends numbers as strings.
		json.put("expires_in", Long.toString(lifetime));
		json.put("expires_on", Long.toString(System.currentTimeMillis() / 1000 + lifetime));
		json.put("not_before", Long.toString(System.currentTimeMillis() / 1000));
		json.put("resource", "https://cognitiveservices.azure.com");
		json.put("token_type", "Bearer");
		return JSON.toString(json);
	}

	private CredentialProvider imds(String attributes) throws Exception {
		return credentials("<credentials class='" + ManagedIdentityCredentials.class.getName()
			+ "' resource='" + _resource + "' retry-delay='10ms' " + attributes + "/>");
	}

	public void testImds() throws Exception {
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-1", authenticate(provider));
		assertEquals("Token must be cached.", "Bearer imds-token-1", authenticate(provider));

		assertEquals(1, requests(IMDS_PATH).size());
		Received request = requests(IMDS_PATH).get(0);
		assertEquals("true", request.header("Metadata"));
		assertEquals("2018-02-01", request.parameter("api-version"));
		assertEquals(_resource, request.parameter("resource"));
		assertNull(request.parameter("client_id"));
	}

	public void testImdsUserAssigned() throws Exception {
		CredentialProvider provider = imds("client-id='" + CLIENT_ID + "'");

		assertEquals("Bearer imds-token-1", authenticate(provider));

		Received request = requests(IMDS_PATH).get(0);
		assertEquals(CLIENT_ID, request.parameter("client_id"));
		assertEquals(_resource, request.parameter("resource"));
	}

	public void testScopeSuffixAccepted() throws Exception {
		CredentialProvider provider = credentials("<credentials class='" + ManagedIdentityCredentials.class.getName()
			+ "' resource='" + _resource + "/.default'/>");

		assertEquals("Bearer imds-token-1", authenticate(provider));
		assertEquals(_resource, requests(IMDS_PATH).get(0).parameter("resource"));
	}

	public void testExpiringTokenRenewed() throws Exception {
		// Within the renewal margin of the Azure Identity library (5 minutes).
		respond(IMDS_PATH, (count, request) -> new Object[] { 200, imdsToken("imds-token-" + count, 60) });
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-1", authenticate(provider));
		assertEquals("Bearer imds-token-2", authenticate(provider));
	}

	public void testTokenSharedBetweenInstances() throws Exception {
		assertEquals("Bearer imds-token-1", authenticate(imds("")));
		assertEquals("Bearer imds-token-1", authenticate(imds("")));
		assertEquals(1, requests(IMDS_PATH).size());
	}

	public void testInvalidate() throws Exception {
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-1", authenticate(provider));
		provider.invalidate();
		// The token of the instance metadata service stays valid until it is about to expire.
		assertEquals("Bearer imds-token-1", authenticate(provider));
		assertEquals(1, requests(IMDS_PATH).size());
	}

	public void testRetryOnServerError() throws Exception {
		respond(IMDS_PATH, (count, request) -> count == 1
			? new Object[] { 500, "{\"error\":\"unknown_error\"}" }
			: new Object[] { 200, imdsToken("imds-token-" + count, 3600) });
		CredentialProvider provider = imds("");

		assertEquals("Bearer imds-token-2", authenticate(provider));
		assertEquals(2, requests(IMDS_PATH).size());
	}

	public void testRetryLimit() throws Exception {
		respond(IMDS_PATH, (count, request) -> new Object[] { 429,
			"{\"error\":\"too_many_requests\",\"error_description\":\"Throttled.\"}" });
		CredentialProvider provider = imds("max-attempts='3'");

		try {
			authenticate(provider);
			fail("Throttling not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_TOKEN_REQUEST_FAILED__RESOURCE_URL_STATUS_DETAIL.fill(_resource,
				imdsUrl(), 429, "too_many_requests: Throttled."), ex.getErrorKey());
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
			assertEquals(I18NConstants.ERROR_TOKEN_REQUEST_FAILED__RESOURCE_URL_STATUS_DETAIL.fill(_resource,
				imdsUrl(), 400, "invalid_request: Identity not found"), ex.getErrorKey());
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
			assertEquals(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(imdsUrl()), ex.getErrorKey());
			assertNotRevealed(ex, "secret-token");
		}
	}

	public void testUnreachable() throws Exception {
		redirectImds("http://localhost:1");
		CredentialProvider provider = imds("connect-timeout='500ms'");

		try {
			authenticate(provider);
			fail("Unreachable endpoint not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_IDENTITY_ENDPOINT_UNREACHABLE__URL.fill(imdsUrl()), ex.getErrorKey());
		}
	}

	/**
	 * URL of the instance metadata service reported in errors.
	 */
	private static String imdsUrl() {
		return "http://" + ImdsRedirectHttpClientProvider.IMDS_HOST + IMDS_PATH;
	}

	public static Test suite() {
		return suite(TestManagedIdentityCredentials.class);
	}

}
