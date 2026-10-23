/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services.common;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.kafka.services.common.CommonClientConfig;

/**
 * Tests the secrets of a {@link CommonClientConfig} given by a secret source: the attribute forms,
 * the reference to an environment variable read each time the client properties are built, and
 * the written form of the configuration.
 */
@SuppressWarnings("javadoc")
public class TestKafkaClientSecrets extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "client";

	/** The system property holding the secret in the tests referencing a variable. */
	private static final String VARIABLE = "test_kafka_client_secrets_secret";

	private static final String SECRET = "first-secret";

	private static final String ROTATED_SECRET = "second-secret";

	/** All properties of {@link CommonClientConfig} holding a secret. */
	private static final List<String> SECRET_PROPERTIES = List.of(
		CommonClientConfig.SASL_JAAS_CONFIG,
		CommonClientConfig.SSL_KEY_PASSWORD_CONFIG,
		CommonClientConfig.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG,
		CommonClientConfig.SSL_KEYSTORE_KEY_CONFIG,
		CommonClientConfig.SSL_KEYSTORE_PASSWORD_CONFIG,
		CommonClientConfig.SSL_TRUSTSTORE_CERTIFICATES_CONFIG,
		CommonClientConfig.SSL_TRUSTSTORE_PASSWORD_CONFIG);

	/**
	 * Client configuration under test.
	 */
	public interface ClientConfig extends CommonClientConfig<String, Object> {
		// Pure sum interface.
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** Secret attributes with prefix <code>unencrypted:</code> reach the client properties. */
	public void testUnencryptedAttributes() throws Exception {
		StringBuilder attributes = new StringBuilder();
		for (String property : SECRET_PROPERTIES) {
			attributes.append(property).append("='unencrypted:").append(secretOf(property)).append("' ");
		}
		ClientConfig config = read(xml(attributes.toString(), ""));
		Map<String, Object> properties = config.getAllProperties();
		for (String property : SECRET_PROPERTIES) {
			assertEquals(property, secretOf(property), properties.get(property));
		}
	}

	/** An encrypted secret attribute reaches the client properties decrypted. */
	public void testEncryptedAttribute() throws Exception {
		ClientConfig config = read(xml(
			CommonClientConfig.SSL_TRUSTSTORE_PASSWORD_CONFIG + "='" + ConfigurationEncryption.encrypt(SECRET) + "'",
			""));
		assertEquals(SECRET, config.getAllProperties().get(CommonClientConfig.SSL_TRUSTSTORE_PASSWORD_CONFIG));
	}

	/** A secret not configured is not passed to the client. */
	public void testNoSecret() throws Exception {
		ClientConfig config = read(xml("", ""));
		Map<String, Object> properties = config.getAllProperties();
		for (String property : SECRET_PROPERTIES) {
			assertFalse(property, properties.containsKey(property));
		}
	}

	/** A secret from a variable is read each time the client properties are built. */
	public void testEnvironmentRotation() throws Exception {
		System.setProperty(VARIABLE, SECRET);
		ClientConfig config = read(xml("", environmentSecret(CommonClientConfig.SASL_JAAS_CONFIG)));
		assertEquals(SECRET, config.getAllProperties().get(CommonClientConfig.SASL_JAAS_CONFIG));

		System.setProperty(VARIABLE, ROTATED_SECRET);
		assertEquals(ROTATED_SECRET, config.getAllProperties().get(CommonClientConfig.SASL_JAAS_CONFIG));
	}

	/** References to variables are written as elements without class holding only the name. */
	public void testWriteEnvironmentSecret() throws Exception {
		System.setProperty(VARIABLE, SECRET);
		String property = CommonClientConfig.SSL_KEYSTORE_PASSWORD_CONFIG;
		ClientConfig config = read(xml("", environmentSecret(property)));

		String written = toXML(config);
		assertFalse("Secret written: " + written, written.contains(SECRET));
		assertTrue("Not written in short form: " + written,
			written.contains("<" + property + " name=\"" + VARIABLE + "\""));

		ClientConfig copy = (ClientConfig) fromXML(written);
		assertTrue(copy.getSslKeystorePassword() instanceof EnvironmentSecretSource.Config);
		assertEquals(written, toXML(copy));
	}

	/** Configured secrets are written encrypted. */
	public void testWriteLiteralSecret() throws Exception {
		String property = CommonClientConfig.SSL_KEY_PASSWORD_CONFIG;
		ClientConfig config = read(xml(property + "='unencrypted:" + SECRET + "'", ""));

		String written = toXML(config);
		assertFalse("Secret written in plain text: " + written, written.contains(SECRET));
		assertTrue("Not written as encrypted attribute: " + written,
			written.contains(property + "=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));

		ClientConfig copy = (ClientConfig) fromXML(written);
		assertEquals(SECRET, copy.getAllProperties().get(property));
	}

	private static String secretOf(String property) {
		return SECRET + "-" + property;
	}

	private static String xml(String attributes, String content) {
		return "<" + ROOT_TAG + " name='test' " + CommonClientConfig.BOOTSTRAP_SERVERS_CONFIG + "='localhost:9092' "
			+ attributes + ">" + content + "</" + ROOT_TAG + ">";
	}

	private static String environmentSecret(String property) {
		return "<" + property + " name='" + VARIABLE + "'/>";
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(ClientConfig.class));
	}

	public static Test suite() {
		return suite(TestKafkaClientSecrets.class);
	}

}
