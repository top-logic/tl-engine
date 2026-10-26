/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.config;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;

import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.credentials.OidcCredentials;
import org.pac4j.oidc.credentials.authenticator.OidcAuthenticator;
import org.pac4j.oidc.exceptions.OidcException;

import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethods;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.FileSecretSource;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.DefaultOidcClientConfigurator;
import com.top_logic.security.auth.pac4j.login.Pac4jLoginMethod;

/**
 * Tests the client secret of a {@link DefaultOidcClientConfigurator} given by a secret source: the
 * attribute forms, the reference to an environment variable, the written form of the configuration,
 * and the secret being read when the client uses it.
 */
@SuppressWarnings("javadoc")
public class TestOidcClientSecret extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	/** The system property holding the client secret in the tests referencing a variable. */
	private static final String VARIABLE = "test_oidc_client_secret";

	private static final String SECRET = "the-client-secret";

	/** The context path of the application in the requests of this test. */
	private static final String CONTEXT_PATH = "/app";

	private static final String CLIENT_CLASS = " class='" + DefaultOidcClientConfigurator.class.getName() + "'";

	/**
	 * Metadata of an identity provider whose token endpoint refuses the connection.
	 */
	private static final String PROVIDER_METADATA = "{"
		+ "\"issuer\":\"https://provider.test\","
		+ "\"authorization_endpoint\":\"https://provider.test/auth\","
		+ "\"token_endpoint\":\"http://127.0.0.1:1/token\","
		+ "\"jwks_uri\":\"https://provider.test/jwks\","
		+ "\"response_types_supported\":[\"code\"],"
		+ "\"subject_types_supported\":[\"public\"],"
		+ "\"id_token_signing_alg_values_supported\":[\"RS256\"],"
		+ "\"token_endpoint_auth_methods_supported\":[\"client_secret_basic\"]"
		+ "}";

	public interface Config extends ConfigurationItem {

		String CLIENT = "client";

		@Name(CLIENT)
		DefaultOidcClientConfigurator.Config<?> getClient();

	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** The attribute with prefix <code>unencrypted:</code> keeps working. */
	public void testUnencryptedAttribute() throws Exception {
		assertEquals(SECRET, clientSecret(read(xml("secret='unencrypted:" + SECRET + "'", ""))));
	}

	/**
	 * The secret given as element text, e.g. through an alias of the application
	 * (<code>&lt;secret&gt;%MY_PAC_SECRET%&lt;/secret&gt;</code>), keeps working.
	 */
	public void testUnencryptedElementText() throws Exception {
		assertEquals(SECRET, clientSecret(read(xml("", "<secret>unencrypted:" + SECRET + "</secret>"))));
	}

	/** An encrypted value given as element text keeps working. */
	public void testEncryptedElementText() throws Exception {
		assertEquals(SECRET,
			clientSecret(read(xml("", "<secret>" + ConfigurationEncryption.encrypt(SECRET) + "</secret>"))));
	}

	/** An encrypted attribute keeps working. */
	public void testEncryptedAttribute() throws Exception {
		assertEquals(SECRET, clientSecret(read(xml("secret='" + ConfigurationEncryption.encrypt(SECRET) + "'", ""))));
	}

	/** A secret from a variable is read from the variable. */
	public void testEnvironmentSecret() throws Exception {
		System.setProperty(VARIABLE, SECRET);
		Config config = read(xml("", environmentSecret()));

		assertEquals(SECRET, clientSecret(config));
	}

	/** A reference to a named secret is written as element without class holding only the name. */
	public void testWriteEnvironmentSecret() throws Exception {
		System.setProperty(VARIABLE, SECRET);
		Config config = read(xml("", environmentSecret()));

		String written = toXML(config);
		assertFalse("Secret written: " + written, written.contains(SECRET));
		assertTrue("Not written in short form: " + written,
			written.contains("<" + DefaultOidcClientConfigurator.Config.SECRET + " name=\"" + VARIABLE + "\""));

		Config copy = (Config) fromXML(written);
		assertTrue(copy.getClient().getSecret() instanceof NamedSecretSource.Config);
		assertEquals(written, toXML(copy));
		assertEquals(SECRET, clientSecret(copy));
	}

	/** A configured secret is written encrypted. */
	public void testWriteLiteralSecret() throws Exception {
		Config config = read(xml("secret='unencrypted:" + SECRET + "'", ""));

		String written = toXML(config);
		assertFalse("Secret written in plain text: " + written, written.contains(SECRET));
		assertTrue("Not written as encrypted attribute: " + written,
			written.contains(DefaultOidcClientConfigurator.Config.SECRET + "=\""
				+ ConfigurationEncryption.AUTHENTICATED_PREFIX));

		assertEquals(SECRET, clientSecret((Config) fromXML(written)));
	}

	/** The client is created and initialized without the secret being available. */
	public void testCreateWithoutSecret() throws Exception {
		OidcClient client = createClient();
		client.init();

		assertNull(client.getConfiguration().getSecret());
	}

	/** A token request without the secret fails with the error of the missing secret. */
	public void testMissingSecretAtUse() throws Exception {
		OidcClient client = createClient();
		client.init();

		try {
			refresh(client);
			fail("Token request without secret.");
		} catch (I18NRuntimeException ex) {
			// Expected: the secret is missing.
		}
	}

	/**
	 * The secret is read with the token request, a rotated secret is used by the next token request.
	 */
	public void testSecretReadAtUse() throws Exception {
		OidcClient client = createClient();
		client.init();

		System.setProperty(VARIABLE, SECRET);
		refresh(client);
		assertEquals(SECRET, tokenRequestSecret(client));

		String rotated = SECRET + "-rotated";
		System.setProperty(VARIABLE, rotated);
		refresh(client);
		assertEquals(rotated, tokenRequestSecret(client));
	}

	/**
	 * Starting a login without the secret sends the browser back to the page shown after a failed
	 * single-sign-on instead of to the provider.
	 */
	public void testMissingSecretAtLoginStart() throws Exception {
		OidcClient client = createClient();
		client.init();

		try {
			client.getRedirectionActionBuilder().getRedirectionAction(new CallContext(webContext(), null));
			fail("Login started without secret.");
		} catch (FoundAction ex) {
			assertEquals(CONTEXT_PATH + ApplicationPages.getInstance().getLoginRetrySSOPage(), ex.getLocation());
		}
	}

	/** The login option of a client is offered only while its secret is available. */
	public void testLoginOptionAvailability() throws Exception {
		Config config = read(xml("", environmentSecret()));
		ClientConfigurator configurator = getInstance(config.getClient());
		List<LoginMethod> options = List.of(new Pac4jLoginMethod("test-oidc", ResKey.text("test-oidc"), null, configurator));

		assertEquals(List.of(), LoginMethods.available(options));

		System.setProperty(VARIABLE, SECRET);
		assertEquals(options, LoginMethods.available(options));

		System.clearProperty(VARIABLE);
		assertEquals(List.of(), LoginMethods.available(options));
	}

	/**
	 * A client whose secret file is missing is not offered, and is offered as soon as the file is
	 * created, as when a secret is put into the secret directory of a running application.
	 */
	public void testLoginOptionAvailableAfterFileCreated() throws Exception {
		Path directory = Files.createTempDirectory("oidc-secrets");
		Path file = directory.resolve("client_secret");
		try {
			Config config = read(xml("",
				"<" + DefaultOidcClientConfigurator.Config.SECRET + " class='" + FileSecretSource.class.getName()
					+ "' path='" + file + "'/>"));
			ClientConfigurator configurator = getInstance(config.getClient());
			List<LoginMethod> options =
				List.of(new Pac4jLoginMethod("test-oidc", ResKey.text("test-oidc"), null, configurator));

			assertEquals(List.of(), LoginMethods.available(options));

			Files.writeString(file, SECRET);
			assertEquals(options, LoginMethods.available(options));
		} finally {
			Files.deleteIfExists(file);
			Files.delete(directory);
		}
	}

	/** A client whose secret is present but cannot be read is not offered. */
	public void testBrokenSecretNotAvailable() throws Exception {
		File empty = File.createTempFile("oidc-secret", "");
		try {
			Config config = read(xml("",
				"<" + DefaultOidcClientConfigurator.Config.SECRET + " class='" + FileSecretSource.class.getName()
					+ "' path='" + empty.getAbsolutePath() + "'/>"));
			ClientConfigurator configurator = getInstance(config.getClient());

			assertFalse(configurator.isAvailable());
		} finally {
			empty.delete();
		}
	}

	/**
	 * A {@link WebContext} of a GET request to an application deployed under
	 * {@link #CONTEXT_PATH}.
	 */
	private static WebContext webContext() {
		HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
			TestOidcClientSecret.class.getClassLoader(), new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "getContextPath" -> CONTEXT_PATH;
				case "getMethod" -> "GET";
				default -> null;
			});
		HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
			TestOidcClientSecret.class.getClassLoader(), new Class<?>[] { HttpServletResponse.class },
			(proxy, method, args) -> null);
		return new JEEContext(request, response);
	}

	private OidcClient createClient() throws Exception {
		Config config = read("<" + ROOT_TAG + "><" + Config.CLIENT + CLIENT_CLASS
			+ " name='test-oidc' client-id='the-client-id'"
			+ " callback-url='https://app.test/callback' callback-url-absolute='true'"
			+ " provider-metadata='" + PROVIDER_METADATA + "'>"
			+ environmentSecret()
			+ "</" + Config.CLIENT + "></" + ROOT_TAG + ">");
		ClientConfigurator configurator = getInstance(config.getClient());
		return (OidcClient) configurator.createClient(null);
	}

	/**
	 * Sends a token request refreshing a token, which fails at the unreachable token endpoint
	 * after the client has authenticated itself.
	 */
	private static void refresh(OidcClient client) {
		OidcCredentials credentials = new OidcCredentials();
		credentials.setRefreshTokenObject(new RefreshToken("the-refresh-token"));
		try {
			((OidcAuthenticator) client.getAuthenticator()).refresh(credentials);
			fail("Token endpoint is not reachable.");
		} catch (OidcException ex) {
			// Expected: no identity provider in the test.
		}
	}

	/** The secret pac4j authenticates the token requests of the given client with. */
	private static String tokenRequestSecret(OidcClient client) {
		ClientAuthentication authentication =
			client.getConfiguration().getOpMetadataResolver().getClientAuthenticationTokenEndpoint();
		return ((ClientSecretBasic) authentication).getClientSecret().getValue();
	}

	private String clientSecret(Config config) {
		SecretSource source = getInstance(config.getClient().getSecret());
		return source.getSecret();
	}

	private static String xml(String attributes, String content) {
		return "<" + ROOT_TAG + "><" + Config.CLIENT + CLIENT_CLASS
			+ " name='test-oidc' client-id='the-client-id'"
			+ " discovery-uri='https://provider.test/.well-known/openid-configuration' " + attributes + ">"
			+ content
			+ "</" + Config.CLIENT + "></" + ROOT_TAG + ">";
	}

	private static String environmentSecret() {
		return "<" + DefaultOidcClientConfigurator.Config.SECRET + " name='" + VARIABLE + "'/>";
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestOidcClientSecret.class, SecretStoreService.Module.INSTANCE);
	}

}
