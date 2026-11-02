/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.credential;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.credential.BasicCredentials;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.FileSecretSource;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.I18NConstants;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.NoCredentials;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Test case for the static {@link CredentialProvider}s {@link HeaderCredentials},
 * {@link BasicCredentials} and {@link NoCredentials}, and for the choice between credentials and an
 * API key shorthand in
 * {@link CredentialProvider#create(com.top_logic.basic.config.InstantiationContext, String, PolymorphicConfiguration, PolymorphicConfiguration, String, String)},
 * and for their {@link CredentialProvider#isAvailable() availability}.
 */
@SuppressWarnings("javadoc")
public class TestCredentialProvider extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	private static final String OWNER = "test-service";

	private static final String TEST_VARIABLE = "test.credential.provider.secret";

	private Path _dir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.clearProperty(TEST_VARIABLE);
		_dir = Files.createTempDirectory(TestCredentialProvider.class.getSimpleName());
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(TEST_VARIABLE);
		FileUtilities.deleteR(_dir.toFile());
		super.tearDown();
	}

	public interface Config extends ConfigurationItem {

		String CREDENTIALS = "credentials";

		String PROVIDERS = "providers";

		String API_KEY = "api-key";

		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		@Name(API_KEY)
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

		@Name(PROVIDERS)
		List<PolymorphicConfiguration<? extends CredentialProvider>> getProviders();

	}

	public void testBearer() throws ConfigurationException {
		Map<String, String> headers = authenticate(provider(
			"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer' secret='unencrypted:s3cr3t'/>"));

		assertEquals(Collections.singletonMap("Authorization", "Bearer s3cr3t"), headers);
	}

	public void testCustomHeaderWithoutScheme() throws ConfigurationException {
		Map<String, String> headers = authenticate(provider(
			"<credentials class='" + HeaderCredentials.class.getName() + "' header='x-api-key' secret='unencrypted:s3cr3t'/>"));

		assertEquals(Collections.singletonMap("x-api-key", "s3cr3t"), headers);
	}

	public void testCustomScheme() throws ConfigurationException {
		Map<String, String> headers = authenticate(provider(
			"<credentials class='" + HeaderCredentials.class.getName()
				+ "' scheme='DeepL-Auth-Key' secret='unencrypted:s3cr3t'/>"));

		assertEquals(Collections.singletonMap("Authorization", "DeepL-Auth-Key s3cr3t"), headers);
	}

	public void testHeaderCredentialsInCode() {
		LiteralSecretSource.Config secret = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
		secret.setValue("s3cr3t");

		Map<String, String> headers =
			authenticate(getInstance(HeaderCredentials.config("x-api-key", null, secret)));

		assertEquals(Collections.singletonMap("x-api-key", "s3cr3t"), headers);
	}

	public void testBasic() throws ConfigurationException {
		Map<String, String> headers = authenticate(provider(
			"<credentials class='" + BasicCredentials.class.getName()
				+ "' user='service-user' password='unencrypted:pässwörd'/>"));

		String expected = "Basic " + Base64.getEncoder()
			.encodeToString("service-user:pässwörd".getBytes(StandardCharsets.UTF_8));
		assertEquals(Collections.singletonMap("Authorization", expected), headers);
	}

	public void testNoCredentials() throws ConfigurationException {
		Map<String, String> headers =
			authenticate(provider("<credentials class='" + NoCredentials.class.getName() + "'/>"));

		assertEquals(Collections.emptyMap(), headers);
	}

	public void testProviderList() throws ConfigurationException {
		Config config = read("<config><providers>"
			+ "<" + HeaderCredentials.Config.TAG_NAME + " header='api-key' secret='unencrypted:key'/>"
			+ "<" + BasicCredentials.Config.TAG_NAME + " user='u' password='unencrypted:p'/>"
			+ "<" + NoCredentials.Config.TAG_NAME + "/>"
			+ "</providers></config>");

		List<CredentialProvider> providers = TypedConfiguration.getInstanceList(context, config.getProviders());
		assertEquals(3, providers.size());
		assertEquals(Collections.singletonMap("api-key", "key"), authenticate(providers.get(0)));
	}

	public void testHttpRequestBuilderAdapter() throws ConfigurationException {
		CredentialProvider provider = provider(
			"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer' secret='unencrypted:s3cr3t'/>");
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost/"));

		provider.authenticate(RequestAuthentication.of(builder));

		assertEquals("Bearer s3cr3t", builder.build().headers().firstValue("Authorization").orElse(null));
	}

	public void testCreateFromApiKey() throws ConfigurationException {
		Map<String, String> headers = authenticate(create("<config api-key='unencrypted:s3cr3t'/>"));

		assertEquals(Collections.singletonMap("x-api-key", "s3cr3t"), headers);
	}

	public void testCreateFromApiKeyWithScheme() throws ConfigurationException {
		Config config = read("<config api-key='unencrypted:s3cr3t'/>");
		CredentialProvider provider = CredentialProvider.create(context, OWNER, config.getCredentials(),
			config.getApiKey(), CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER);

		assertEquals(Collections.singletonMap("Authorization", "Bearer s3cr3t"), authenticate(provider));
	}

	public void testCreateFromCredentials() throws ConfigurationException {
		Map<String, String> headers = authenticate(create("<config>"
			+ "<credentials class='" + HeaderCredentials.class.getName() + "' header='X-Gateway' secret='unencrypted:gw'/>"
			+ "</config>"));

		assertEquals(Collections.singletonMap("X-Gateway", "gw"), headers);
	}

	public void testCreateWithoutCredentials() throws ConfigurationException {
		CredentialProvider provider = create("<config/>");

		assertTrue(provider instanceof NoCredentials);
		assertEquals(Collections.emptyMap(), authenticate(provider));
	}

	public void testCreateWithEmptyApiKey() throws ConfigurationException {
		CredentialProvider provider = create("<config api-key='unencrypted:'/>");

		assertTrue(provider instanceof NoCredentials);
	}

	public void testCreateRefusesCredentialsAndApiKey() throws ConfigurationException {
		try {
			create("<config api-key='unencrypted:s3cr3t'>"
				+ "<credentials class='" + NoCredentials.class.getName() + "'/>"
				+ "</config>");
			fail("Credentials and an API key must not be accepted together.");
		} catch (ConfigurationError ex) {
			assertEquals(I18NConstants.ERROR_CREDENTIALS_AND_API_KEY__OWNER.fill(OWNER), ex.getErrorKey());
		}
	}

	public void testHeaderCredentialsAvailable() throws ConfigurationException {
		System.setProperty(TEST_VARIABLE, "s3cr3t");
		CredentialProvider provider = envHeaderProvider();

		assertTrue(provider.isAvailable());
		assertEquals(Collections.singletonMap("x-api-key", "s3cr3t"), authenticate(provider));
	}

	public void testHeaderCredentialsNotAvailable() throws ConfigurationException {
		CredentialProvider provider = envHeaderProvider();

		assertFalse(provider.isAvailable());
	}

	public void testHeaderCredentialsBecomeAvailable() throws ConfigurationException {
		CredentialProvider provider = envHeaderProvider();
		assertFalse(provider.isAvailable());

		System.setProperty(TEST_VARIABLE, "provided-later");
		assertTrue("A secret provided later must make the same provider available.", provider.isAvailable());
		assertEquals(Collections.singletonMap("x-api-key", "provided-later"), authenticate(provider));

		System.clearProperty(TEST_VARIABLE);
		assertFalse("A removed secret must make the provider unavailable.", provider.isAvailable());
	}

	public void testHeaderCredentialsUnreadableSecretAvailable() throws Exception {
		Path secretFile = _dir.resolve("empty-secret");
		Files.write(secretFile, new byte[0]);
		CredentialProvider provider = provider("<credentials class='" + HeaderCredentials.class.getName() + "'>"
			+ "<secret class='" + FileSecretSource.class.getName() + "' path='" + secretFile + "'/>"
			+ "</credentials>");

		assertTrue("A present but unreadable secret is reported on use, not hidden.", provider.isAvailable());
		try {
			authenticate(provider);
			fail("An empty secret file must be reported on use.");
		} catch (I18NRuntimeException ex) {
			// Expected.
		}
	}

	public void testBasicCredentialsAvailability() throws ConfigurationException {
		CredentialProvider provider = provider("<credentials class='" + BasicCredentials.class.getName()
			+ "' user='service-user'>"
			+ "<password class='" + EnvironmentSecretSource.class.getName() + "' name='" + TEST_VARIABLE + "'/>"
			+ "</credentials>");
		assertFalse(provider.isAvailable());

		System.setProperty(TEST_VARIABLE, "pw");
		assertTrue(provider.isAvailable());
	}

	public void testLiteralSecretAvailable() throws ConfigurationException {
		CredentialProvider provider = provider(
			"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer' secret='unencrypted:s3cr3t'/>");

		assertTrue(provider.isAvailable());
	}

	public void testNoCredentialsAvailable() throws ConfigurationException {
		CredentialProvider provider = provider("<credentials class='" + NoCredentials.class.getName() + "'/>");

		assertTrue(provider.isAvailable());
	}

	private CredentialProvider envHeaderProvider() throws ConfigurationException {
		return provider("<credentials class='" + HeaderCredentials.class.getName() + "' header='x-api-key'>"
			+ "<secret class='" + EnvironmentSecretSource.class.getName() + "' name='" + TEST_VARIABLE + "'/>"
			+ "</credentials>");
	}

	private CredentialProvider create(String xml) throws ConfigurationException {
		Config config = read(xml);
		return CredentialProvider.create(context, OWNER, config.getCredentials(), config.getApiKey(), "x-api-key",
			null);
	}

	private CredentialProvider provider(String xml) throws ConfigurationException {
		Config config = read("<config>" + xml + "</config>");
		return getInstance(config.getCredentials());
	}

	private static Map<String, String> authenticate(CredentialProvider provider) {
		Map<String, String> headers = new HashMap<>();
		provider.authenticate(headers::put);
		return headers;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestCredentialProvider.class);
	}

}
