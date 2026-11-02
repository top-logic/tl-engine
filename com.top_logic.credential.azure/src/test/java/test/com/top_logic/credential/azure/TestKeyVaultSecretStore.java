/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.credential.azure;

import junit.framework.Test;

import com.top_logic.basic.credential.ChainSecretStore;
import com.top_logic.basic.credential.EnvironmentSecretStore;
import com.top_logic.basic.credential.SecretStore;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;
import com.top_logic.credential.azure.I18NConstants;
import com.top_logic.credential.azure.KeyVaultSecretStore;

/**
 * Test case for {@link KeyVaultSecretStore} against a mock key vault.
 */
@SuppressWarnings("javadoc")
public class TestKeyVaultSecretStore extends AbstractAzureMockTest {

	private static final String SECRET_PATH = "/secrets/db-password";

	private static final String ENV_VARIABLE = "test_key_vault_store_fallback";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		respond(SECRET_PATH, (count, request) -> new Object[] { 200, secretBundle("value-" + count) });
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(ENV_VARIABLE);
		super.tearDown();
	}

	private static String secretBundle(String value) {
		return JSON.toString(java.util.Collections.singletonMap("value", value));
	}

	private static String testingCredentials() {
		return "<credentials class='" + TestingCredentials.class.getName() + "'/>";
	}

	private String vaultAttributes(String attributes) {
		return " vault-url='" + url("/") + "' " + attributes;
	}

	private SecretStore vault(String attributes) throws Exception {
		return store("<store class='" + KeyVaultSecretStore.class.getName() + "'" + vaultAttributes(attributes)
			+ ">" + testingCredentials() + "</store>");
	}

	public void testNameMapping() throws Exception {
		respond("/secrets/prod-db-password", (count, request) -> new Object[] { 200, secretBundle("prod-value") });
		SecretStore store = vault("prefix='prod_'");

		assertEquals("prod-value", store.getSecret("db_password"));
		assertEquals(1, requests("/secrets/prod-db-password").size());
		assertEquals("Bearer token-1", requests("/secrets/prod-db-password").get(0).header("Authorization"));
		assertEquals("prod-db-password", KeyVaultSecretStore.vaultName("prod_db_password"));
	}

	public void testInvalidName() throws Exception {
		SecretStore store = vault("");

		try {
			store.lookup("db.password");
			fail("Invalid name not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_INVALID_SECRET_NAME__NAME.fill("db.password"), ex.getErrorKey());
		}
		assertEquals(0, requests("/secrets/db.password").size());
	}

	public void testCacheTime() throws Exception {
		respond("/secrets/other", (count, request) -> new Object[] { 200, secretBundle("other-" + count) });
		SecretStore store = vault("cache-time='300ms'");

		assertEquals("value-1", store.getSecret("db_password"));
		assertEquals("other-1", store.getSecret("other"));
		assertEquals("Secret must be cached.", "value-1", store.getSecret("db_password"));
		assertEquals(1, requests(SECRET_PATH).size());

		Thread.sleep(400);
		assertEquals("Rotated secret must be read after the cache time.", "value-2", store.getSecret("db_password"));
		assertEquals(2, requests(SECRET_PATH).size());
	}

	public void testNotFoundIsAbsent() throws Exception {
		respond("/secrets/missing", (count, request) -> new Object[] { 404,
			"{\"error\":{\"code\":\"SecretNotFound\",\"message\":\"A secret with (name/id) missing was not found.\"}}" });
		SecretStore store = vault("");

		assertNull(store.lookup("missing"));
		assertNull("Absence must be cached.", store.lookup("missing"));
		assertEquals(1, requests("/secrets/missing").size());

		try {
			store.getSecret("missing");
			fail("Missing secret not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_SECRET_NOT_FOUND__NAME_VAULT.fill("missing", url("")), ex.getErrorKey());
		}
	}

	public void testChainFallthrough() throws Exception {
		System.setProperty(ENV_VARIABLE, "from-env");
		SecretStore store = store("<store class='" + ChainSecretStore.class.getName() + "'><stores>"
			+ "<" + KeyVaultSecretStore.Config.TAG_NAME + vaultAttributes("") + ">" + testingCredentials()
			+ "</" + KeyVaultSecretStore.Config.TAG_NAME + ">"
			+ "<" + EnvironmentSecretStore.Config.TAG_NAME + "/>"
			+ "</stores></store>");

		assertEquals("value-1", store.getSecret("db_password"));
		assertEquals("Missing vault secret falls through.", "from-env", store.getSecret(ENV_VARIABLE));
		assertEquals(1, requests("/secrets/test-key-vault-store-fallback").size());
	}

	public void testForbiddenIsError() throws Exception {
		respond(SECRET_PATH, (count, request) -> new Object[] { 403,
			"{\"error\":{\"code\":\"Forbidden\",\"message\":\"Caller is not authorized.\"},"
				+ "\"value\":\"leaked-secret\"}" });
		SecretStore store = vault("");

		try {
			store.lookup("db_password");
			fail("Denied access not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL.fill("db-password",
				url(""), 403, "Forbidden: Caller is not authorized."), ex.getErrorKey());
			assertNotRevealed(ex, "leaked-secret");
		}
		assertEquals("Only a rejected token is retried.", 1, requests(SECRET_PATH).size());
	}

	public void testFailureNotCached() throws Exception {
		respond(SECRET_PATH, (count, request) -> count == 1
			? new Object[] { 503, "{}" }
			: new Object[] { 200, secretBundle("value-" + count) });
		SecretStore store = vault("");

		try {
			store.lookup("db_password");
			fail("Unavailable vault not reported.");
		} catch (I18NRuntimeException ex) {
			// Expected.
		}
		assertEquals("value-2", store.getSecret("db_password"));
	}

	public void testUnauthorizedRetry() throws Exception {
		respond(SECRET_PATH, (count, request) -> count == 1
			? new Object[] { 401, "{\"error\":{\"code\":\"Unauthorized\",\"message\":\"Token expired.\"}}" }
			: new Object[] { 200, secretBundle("value-" + count) });
		SecretStore store = vault("");

		assertEquals("value-2", store.getSecret("db_password"));
		assertEquals(2, requests(SECRET_PATH).size());
		assertEquals("Bearer token-1", requests(SECRET_PATH).get(0).header("Authorization"));
		assertEquals("Bearer token-2", requests(SECRET_PATH).get(1).header("Authorization"));
	}

	public void testErrorNeverContainsSecret() throws Exception {
		respond(SECRET_PATH, (count, request) -> new Object[] { 200, "{\"value\":\"TopSecretValue\"}" });
		SecretStore store = vault("prefix='x_'");

		try {
			store.getSecret("db_password");
			fail("Missing secret not reported.");
		} catch (I18NRuntimeException ex) {
			assertNotRevealed(ex, "TopSecretValue");
		}
	}

	public static Test suite() {
		return suite(TestKeyVaultSecretStore.class);
	}

}
