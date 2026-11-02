/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.services.jms.ibmmq;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.services.jms.ibmmq.IBMMQClient;

/**
 * Tests the password of an {@link IBMMQClient} given by a secret source: the attribute forms,
 * the reference to an environment variable read for each connection factory, and the written form
 * of the configuration.
 *
 * <p>
 * The tests inspect the {@link MQConnectionFactory} set up for a connection, no server is
 * contacted.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestIBMMQPassword extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "client";

	/** The system property holding the password in the tests referencing a variable. */
	private static final String VARIABLE = "test_ibmmq_password";

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
		MQConnectionFactory factory = factory(client(IBMMQClient.Config.PASSWORD + "='unencrypted:" + PASSWORD + "'", ""));
		assertEquals(USER, factory.getStringProperty(WMQConstants.USERID));
		assertEquals(PASSWORD, factory.getStringProperty(WMQConstants.PASSWORD));
	}

	/** An encrypted password attribute keeps working. */
	public void testEncryptedAttribute() throws Exception {
		assertEquals(PASSWORD, factory(client(
			IBMMQClient.Config.PASSWORD + "='" + ConfigurationEncryption.encrypt(PASSWORD) + "'", "")).getStringProperty(WMQConstants.PASSWORD));
	}

	/** Without password, the factory has no password, as before. */
	public void testNoPassword() throws Exception {
		assertNull(factory(client("", "")).getStringProperty(WMQConstants.PASSWORD));
	}

	/** The password from a variable is read for each connection. */
	public void testEnvironmentRotation() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		IBMMQClient.Config<?> config = read(client("", environmentPassword()));
		IBMMQClient client = getInstance(config);
		assertEquals(PASSWORD, ((MQConnectionFactory) client.setupConnectionFactory()).getStringProperty(WMQConstants.PASSWORD));

		System.setProperty(VARIABLE, ROTATED_PASSWORD);
		assertEquals(ROTATED_PASSWORD, ((MQConnectionFactory) client.setupConnectionFactory()).getStringProperty(WMQConstants.PASSWORD));
	}

	/** A reference to a named secret is written as element without class holding only the name. */
	public void testWriteEnvironmentPassword() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		IBMMQClient.Config<?> config = read(client("", environmentPassword()));

		String written = toXML(config);
		assertFalse("Password written: " + written, written.contains(PASSWORD));
		assertTrue("Not written in short form: " + written,
			written.contains("<" + IBMMQClient.Config.PASSWORD + " name=\"" + VARIABLE + "\""));

		IBMMQClient.Config<?> copy = (IBMMQClient.Config<?>) fromXML(written);
		assertTrue(copy.getPassword() instanceof NamedSecretSource.Config);
		assertEquals(written, toXML(copy));
	}

	/** A configured password is written encrypted. */
	public void testWriteLiteralPassword() throws Exception {
		IBMMQClient.Config<?> config = read(client(IBMMQClient.Config.PASSWORD + "='unencrypted:" + PASSWORD + "'", ""));

		String written = toXML(config);
		assertFalse("Password written in plain text: " + written, written.contains(PASSWORD));
		assertTrue("Not written as encrypted attribute: " + written,
			written.contains(IBMMQClient.Config.PASSWORD + "=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));

		IBMMQClient.Config<?> copy = (IBMMQClient.Config<?>) fromXML(written);
		assertEquals(PASSWORD, factory(copy).getStringProperty(WMQConstants.PASSWORD));
	}

	private MQConnectionFactory factory(String xml) throws Exception {
		return factory((IBMMQClient.Config<?>) read(xml));
	}

	private MQConnectionFactory factory(IBMMQClient.Config<?> config) throws Exception {
		IBMMQClient client = getInstance(config);
		return (MQConnectionFactory) client.setupConnectionFactory();
	}

	private static String client(String attributes, String content) {
		return "<" + ROOT_TAG + " class='" + IBMMQClient.class.getName() + "'"
			+ " " + IBMMQClient.Config.HOST + "='localhost'"
			+ " " + IBMMQClient.Config.CHANNEL + "='DEV.APP.SVRCONN'"
			+ " " + IBMMQClient.Config.QUEUE_MANAGER + "='QM1'"
			+ " " + IBMMQClient.Config.USER + "='" + USER + "' "
			+ attributes + ">" + content + "</" + ROOT_TAG + ">";
	}

	private static String environmentPassword() {
		return "<" + IBMMQClient.Config.PASSWORD + " name='" + VARIABLE + "'/>";
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(IBMMQClient.Config.class));
	}

	public static Test suite() {
		return suite(TestIBMMQPassword.class, SecretStoreService.Module.INSTANCE);
	}

}
