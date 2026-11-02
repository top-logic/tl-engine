/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.credential.azure;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;
import com.top_logic.credential.azure.I18NConstants;
import com.top_logic.credential.azure.KeyVaultSecretSource;
import com.top_logic.credential.azure.ManagedIdentityCredentials;

/**
 * Test case for {@link KeyVaultSecretSource} against a mock key vault and a mock of the instance
 * metadata service.
 */
@SuppressWarnings("javadoc")
public class TestKeyVaultSecretSource extends AbstractAzureMockTest {

	private static final String TOKEN_PATH = IMDS_PATH;

	private static final String SECRET_NAME = "db-password";

	private static final String SECRET_PATH = "/secrets/" + SECRET_NAME;

	private String _resource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_resource = uniqueResource(KeyVaultSecretSource.KEY_VAULT_RESOURCE);
		respond(TOKEN_PATH, (count, request) -> new Object[] { 200,
			TestManagedIdentityCredentials.imdsToken("vault-token-" + count, 3600) });
		respond(SECRET_PATH, (count, request) -> new Object[] { 200, secretBundle("value-" + count) });
	}

	private String secretBundle(String value) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("value", value);
		json.put("id", url(SECRET_PATH) + "/0123456789abcdef");
		Map<String, Object> attributes = new LinkedHashMap<>();
		attributes.put("enabled", Boolean.TRUE);
		json.put("attributes", attributes);
		return JSON.toString(json);
	}

	private String credentials() {
		return "<credentials class='" + ManagedIdentityCredentials.class.getName() + "' resource='" + _resource
			+ "'/>";
	}

	private static String testingCredentials() {
		return "<credentials class='" + TestingCredentials.class.getName() + "'/>";
	}

	private SecretSource vault(String attributes, String content) throws Exception {
		return secret("<secret class='" + KeyVaultSecretSource.class.getName() + "' vault-url='" + url("/")
			+ "' secret-name='" + SECRET_NAME + "' " + attributes + ">" + content + "</secret>");
	}

	public void testReadSecret() throws Exception {
		SecretSource source = vault("", credentials());

		assertEquals("value-1", source.getSecret());
		assertEquals("Secret must be cached.", "value-1", source.getSecret());

		assertEquals(1, requests(SECRET_PATH).size());
		Received request = requests(SECRET_PATH).get(0);
		assertEquals("Bearer vault-token-1", request.header("Authorization"));
		assertNotNull(request.parameter("api-version"));
		assertEquals(_resource, requests(TOKEN_PATH).get(0).parameter("resource"));
	}

	public void testSecretVersion() throws Exception {
		String versionPath = SECRET_PATH + "/0123456789abcdef";
		respond(versionPath, (count, request) -> new Object[] { 200, secretBundle("old-value") });
		SecretSource source = vault("secret-version='0123456789abcdef'", credentials());

		assertEquals("old-value", source.getSecret());
		assertEquals(1, requests(versionPath).size());
		assertNotNull(requests(versionPath).get(0).parameter("api-version"));
		assertEquals(0, requests(SECRET_PATH).size());
	}

	public void testCacheTime() throws Exception {
		SecretSource source = vault("cache-time='300ms'", credentials());

		assertEquals("value-1", source.getSecret());
		assertEquals("value-1", source.getSecret());
		Thread.sleep(400);
		assertEquals("Rotated secret must be read after the cache time.", "value-2", source.getSecret());
		assertEquals(2, requests(SECRET_PATH).size());
		assertEquals("Access token must stay cached.", 1, requests(TOKEN_PATH).size());
	}

	public void testNoCache() throws Exception {
		SecretSource source = vault("cache-time='0ms'", credentials());

		assertEquals("value-1", source.getSecret());
		assertEquals("value-2", source.getSecret());
	}

	public void testUnauthorizedRetry() throws Exception {
		respond(SECRET_PATH, (count, request) -> count == 1
			? new Object[] { 401, "{\"error\":{\"code\":\"Unauthorized\",\"message\":\"Token expired.\"}}" }
			: new Object[] { 200, secretBundle("value-" + count) });
		SecretSource source = vault("", testingCredentials());

		assertEquals("value-2", source.getSecret());
		assertEquals(2, requests(SECRET_PATH).size());
		assertEquals("Bearer token-1", requests(SECRET_PATH).get(0).header("Authorization"));
		assertEquals("Bearer token-2", requests(SECRET_PATH).get(1).header("Authorization"));
	}

	public void testUnauthorizedOnlyOneRetry() throws Exception {
		respond(SECRET_PATH, (count, request) -> new Object[] { 401,
			"{\"error\":{\"code\":\"Unauthorized\",\"message\":\"Access denied.\"}}" });
		SecretSource source = vault("", testingCredentials());

		try {
			source.getSecret();
			fail("Rejected token not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL.fill(SECRET_NAME,
				url(""), 401, "Unauthorized: Access denied."), ex.getErrorKey());
		}
		assertEquals(2, requests(SECRET_PATH).size());
	}

	public void testNotFound() throws Exception {
		respond(SECRET_PATH, (count, request) -> new Object[] { 404,
			"{\"error\":{\"code\":\"SecretNotFound\",\"message\":\"A secret with (name/id) db-password was not found.\"},"
				+ "\"value\":\"leaked-secret\"}" });
		SecretSource source = vault("", credentials());

		try {
			source.getSecret();
			fail("Missing secret not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL.fill(SECRET_NAME,
				url(""), 404, "SecretNotFound: A secret with (name/id) db-password was not found."),
				ex.getErrorKey());
			assertNotRevealed(ex, "leaked-secret");
		}
		assertEquals("Only a rejected token is retried.", 1, requests(SECRET_PATH).size());
	}

	private static final String NOT_FOUND_BODY =
		"{\"error\":{\"code\":\"SecretNotFound\",\"message\":\"A secret with (name/id) db-password was not found.\"}}";

	private static final String NOT_FOUND_DETAIL = "SecretNotFound: A secret with (name/id) db-password was not found.";

	private static final String FORBIDDEN_BODY =
		"{\"error\":{\"code\":\"Forbidden\",\"message\":\"Access denied.\"}}";

	private static final String FORBIDDEN_DETAIL = "Forbidden: Access denied.";

	public void testLookupExisting() throws Exception {
		SecretSource source = vault("", credentials());

		assertEquals("value-1", source.lookup());
		assertEquals("Secret read by lookup must be cached.", "value-1", source.getSecret());
		assertEquals(1, requests(SECRET_PATH).size());
	}

	public void testLookupNotFoundThenGetSecret() throws Exception {
		respond(SECRET_PATH, (count, request) -> new Object[] { 404, NOT_FOUND_BODY });
		SecretSource source = vault("", credentials());

		assertNull("Missing secret must be absent.", source.lookup());
		assertFailure(404, NOT_FOUND_DETAIL, source);
		assertNull("Cached absence must stay absent.", source.lookup());
		assertEquals("Absence must be cached.", 1, requests(SECRET_PATH).size());
	}

	public void testGetSecretNotFoundThenLookup() throws Exception {
		respond(SECRET_PATH, (count, request) -> new Object[] { 404, NOT_FOUND_BODY });
		SecretSource source = vault("", credentials());

		assertFailure(404, NOT_FOUND_DETAIL, source);
		assertNull("Cached absence must be absent for lookup.", source.lookup());
		assertFailure(404, NOT_FOUND_DETAIL, source);
		assertEquals("Absence must be cached.", 1, requests(SECRET_PATH).size());
	}

	public void testForbiddenIsError() throws Exception {
		respond(SECRET_PATH, (count, request) -> new Object[] { 403, FORBIDDEN_BODY });
		SecretSource source = vault("", credentials());

		try {
			source.lookup();
			fail("Denied access not reported by lookup.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL.fill(SECRET_NAME,
				url(""), 403, FORBIDDEN_DETAIL), ex.getErrorKey());
		}
		assertFailure(403, FORBIDDEN_DETAIL, source);
		assertEquals("Failure must not be cached.", 2, requests(SECRET_PATH).size());
	}

	private void assertFailure(int status, String detail, SecretSource source) {
		try {
			source.getSecret();
			fail("Failure not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL.fill(SECRET_NAME,
				url(""), status, detail), ex.getErrorKey());
		}
	}

	public void testEmptySecret() throws Exception {
		respond(SECRET_PATH, (count, request) -> new Object[] { 200, "{\"value\":\"\"}" });
		SecretSource source = vault("", credentials());

		try {
			source.getSecret();
			fail("Empty secret not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_SECRET_EMPTY__NAME_VAULT.fill(SECRET_NAME, url("")), ex.getErrorKey());
		}
	}

	public void testFailureNotCached() throws Exception {
		respond(SECRET_PATH, (count, request) -> count == 1
			? new Object[] { 503, "{}" }
			: new Object[] { 200, secretBundle("value-" + count) });
		SecretSource source = vault("", credentials());

		try {
			source.getSecret();
			fail("Unavailable vault not reported.");
		} catch (I18NRuntimeException ex) {
			// Expected.
		}
		assertEquals("value-2", source.getSecret());
	}

	public void testDefaultCredentials() throws Exception {
		SecretSource source = vault("", "");

		assertEquals("value-1", source.getSecret());

		assertEquals(1, requests(TOKEN_PATH).size());
		Received tokenRequest = requests(TOKEN_PATH).get(0);
		assertEquals(KeyVaultSecretSource.KEY_VAULT_RESOURCE, tokenRequest.parameter("resource"));
		assertNull(tokenRequest.parameter("client_id"));
		assertEquals("Bearer vault-token-1", requests(SECRET_PATH).get(0).header("Authorization"));
	}

	public void testCredentialsFailure() throws Exception {
		respond(TOKEN_PATH, (count, request) -> new Object[] { 400,
			"{\"error\":\"invalid_request\",\"error_description\":\"Identity not found\"}" });
		SecretSource source = vault("", credentials());

		try {
			source.getSecret();
			fail("Missing identity not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_TOKEN_REQUEST_FAILED__RESOURCE_URL_STATUS_DETAIL.fill(_resource,
				"http://" + ImdsRedirectHttpClientProvider.IMDS_HOST + TOKEN_PATH, 400,
				"invalid_request: Identity not found"), ex.getErrorKey());
		}
		assertEquals(0, requests(SECRET_PATH).size());
	}

	public void testUnreachable() throws Exception {
		SecretSource source = secret("<secret class='" + KeyVaultSecretSource.class.getName()
			+ "' vault-url='http://localhost:1' secret-name='x' connect-timeout='500ms'>" + testingCredentials()
			+ "</secret>");

		try {
			source.getSecret();
			fail("Unreachable vault not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_VAULT_UNREACHABLE__URL.fill("http://localhost:1"), ex.getErrorKey());
		}
	}

	public static Test suite() {
		return suite(TestKeyVaultSecretSource.class);
	}

}
