/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.services.jms.activemq;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.services.jms.activemq.ActiveMQClient;

/**
 * Tests the password of an {@link ActiveMQClient} given by a secret source: the attribute forms,
 * the reference to an environment variable read for each connection factory, and the written form
 * of the configuration.
 *
 * <p>
 * The tests inspect the {@link ActiveMQConnectionFactory} set up for a connection, no server is
 * contacted.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestActiveMQPassword extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "client";

	/** The system property holding the password in the tests referencing a variable. */
	private static final String VARIABLE = "test_activemq_password";

	private static final String USER = "app";

	private static final String PASSWORD = "first-password";

	private static final String ROTATED_PASSWORD = "second-password";

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** The password attribute with prefix <code>unencrypted:</code> keeps working. */
	public void testUnencryptedAttribute() throws Exception {
		ActiveMQConnectionFactory factory = factory(client(ActiveMQClient.Config.PASSWORD + "='unencrypted:" + PASSWORD + "'", ""));
		assertEquals(USER, factory.getUser());
		assertEquals(PASSWORD, factory.getPassword());
	}

	/** An encrypted password attribute keeps working. */
	public void testEncryptedAttribute() throws Exception {
		assertEquals(PASSWORD, factory(client(
			ActiveMQClient.Config.PASSWORD + "='" + ConfigurationEncryption.encrypt(PASSWORD) + "'", "")).getPassword());
	}

	/** Without password, the factory has no password, as before. */
	public void testNoPassword() throws Exception {
		assertNull(factory(client("", "")).getPassword());
	}

	/** The password from a variable is read for each connection. */
	public void testEnvironmentRotation() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		ActiveMQClient.Config<?> config = read(client("", environmentPassword()));
		ActiveMQClient client = getInstance(config);
		assertEquals(PASSWORD, ((ActiveMQConnectionFactory) client.setupConnectionFactory()).getPassword());

		System.setProperty(VARIABLE, ROTATED_PASSWORD);
		assertEquals(ROTATED_PASSWORD, ((ActiveMQConnectionFactory) client.setupConnectionFactory()).getPassword());
	}

	/** A reference to a named secret is written as element without class holding only the name. */
	public void testWriteEnvironmentPassword() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		ActiveMQClient.Config<?> config = read(client("", environmentPassword()));

		String written = toXML(config);
		assertFalse("Password written: " + written, written.contains(PASSWORD));
		assertTrue("Not written in short form: " + written,
			written.contains("<" + ActiveMQClient.Config.PASSWORD + " name=\"" + VARIABLE + "\""));

		ActiveMQClient.Config<?> copy = (ActiveMQClient.Config<?>) fromXML(written);
		assertTrue(copy.getPassword() instanceof NamedSecretSource.Config);
		assertEquals(written, toXML(copy));
	}

	/** A configured password is written encrypted. */
	public void testWriteLiteralPassword() throws Exception {
		ActiveMQClient.Config<?> config = read(client(ActiveMQClient.Config.PASSWORD + "='unencrypted:" + PASSWORD + "'", ""));

		String written = toXML(config);
		assertFalse("Password written in plain text: " + written, written.contains(PASSWORD));
		assertTrue("Not written as encrypted attribute: " + written,
			written.contains(ActiveMQClient.Config.PASSWORD + "=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));

		ActiveMQClient.Config<?> copy = (ActiveMQClient.Config<?>) fromXML(written);
		assertEquals(PASSWORD, factory(copy).getPassword());
	}

	private ActiveMQConnectionFactory factory(String xml) throws Exception {
		return factory((ActiveMQClient.Config<?>) read(xml));
	}

	private ActiveMQConnectionFactory factory(ActiveMQClient.Config<?> config) throws Exception {
		ActiveMQClient client = getInstance(config);
		return (ActiveMQConnectionFactory) client.setupConnectionFactory();
	}

	private static String client(String attributes, String content) {
		return "<" + ROOT_TAG + " class='" + ActiveMQClient.class.getName() + "'"
			+ " " + ActiveMQClient.Config.URL_SCHEME + "='TCP'"
			+ " " + ActiveMQClient.Config.HOST + "='localhost'"
			+ " " + ActiveMQClient.Config.PORT + "='61616'"
			+ " " + ActiveMQClient.Config.USER + "='" + USER + "' "
			+ attributes + ">" + content + "</" + ROOT_TAG + ">";
	}

	private static String environmentPassword() {
		return "<" + ActiveMQClient.Config.PASSWORD + " name='" + VARIABLE + "'/>";
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(ActiveMQClient.Config.class));
	}

	public static Test suite() {
		return suite(TestActiveMQPassword.class);
	}

}
