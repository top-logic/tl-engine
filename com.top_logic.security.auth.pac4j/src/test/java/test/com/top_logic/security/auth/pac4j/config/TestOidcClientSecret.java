/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.config;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import org.pac4j.oidc.config.OidcConfiguration;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.security.auth.pac4j.config.DefaultOidcClientConfigurator;

/**
 * Tests the client secret of a {@link DefaultOidcClientConfigurator} given by a secret source: the
 * attribute forms, the reference to an environment variable, and the written form of the
 * configuration.
 */
@SuppressWarnings("javadoc")
public class TestOidcClientSecret extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	/** The system property holding the client secret in the tests referencing a variable. */
	private static final String VARIABLE = "test_oidc_client_secret";

	private static final String SECRET = "the-client-secret";

	public interface Config extends ConfigurationItem {

		String CLIENT = "client";

		@Name(CLIENT)
		DefaultOidcClientConfigurator.Config<?> getClient();

	}

	/**
	 * {@link DefaultOidcClientConfigurator} giving access to the client configuration it builds.
	 */
	public static class ExposedConfigurator extends DefaultOidcClientConfigurator<DefaultOidcClientConfigurator.Config<?>> {

		public ExposedConfigurator(InstantiationContext context, DefaultOidcClientConfigurator.Config<?> config) {
			super(context, config);
		}

		OidcConfiguration clientConfig() {
			return buildConfig();
		}

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
	 * The secret given as element text, as in the configurations shipped with the module
	 * (<code>&lt;secret&gt;%PAC_SECRET%&lt;/secret&gt;</code>), keeps working.
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

	/** A secret from a variable is read when the client is created. */
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

	private String clientSecret(Config config) {
		return ((ExposedConfigurator) getInstance(config.getClient())).clientConfig().getSecret();
	}

	private static String xml(String attributes, String content) {
		return "<" + ROOT_TAG + "><" + Config.CLIENT + " class='" + ExposedConfigurator.class.getName() + "'"
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
