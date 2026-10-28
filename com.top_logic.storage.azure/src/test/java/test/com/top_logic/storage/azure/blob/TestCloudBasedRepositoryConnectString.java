/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.azure.blob;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.storage.azure.blob.CloudBasedRepository;

/**
 * Tests the connection string of a {@link CloudBasedRepository} given by a secret source: the
 * attribute forms, the reference to an environment variable, and the written form of the
 * configuration.
 *
 * <p>
 * The tests read the connection string from the configured {@link SecretSource}, as the
 * repository does when it is created; no storage account is contacted.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestCloudBasedRepositoryConnectString extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "repository";

	/** The system property holding the connection string in the tests referencing a variable. */
	private static final String VARIABLE = "test_azure_connect_string";

	private static final String CONNECT_STRING =
		"DefaultEndpointsProtocol=https;AccountName=account;AccountKey=a2V5";

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** A connection string with prefix <code>unencrypted:</code> keeps working. */
	public void testUnencryptedAttribute() throws Exception {
		assertEquals(CONNECT_STRING, connectString(read(xml(attribute("unencrypted:" + CONNECT_STRING), ""))));
	}

	/** An encrypted connection string keeps working. */
	public void testEncryptedAttribute() throws Exception {
		assertEquals(CONNECT_STRING,
			connectString(read(xml(attribute(ConfigurationEncryption.encrypt(CONNECT_STRING)), ""))));
	}

	/** The connection string is read from a variable. */
	public void testEnvironment() throws Exception {
		System.setProperty(VARIABLE, CONNECT_STRING);
		assertEquals(CONNECT_STRING, connectString(read(xml("", environmentConnectString()))));
	}

	/** A reference to a named secret is written as element without class holding only the name. */
	public void testWriteEnvironmentConnectString() throws Exception {
		System.setProperty(VARIABLE, CONNECT_STRING);
		CloudBasedRepository.Config<?> config = read(xml("", environmentConnectString()));

		String written = toXML(config);
		assertFalse("Connection string written: " + written, written.contains(CONNECT_STRING));
		assertTrue("Not written in short form: " + written,
			written.contains("<" + CloudBasedRepository.Config.CONNECT_STRING + " name=\"" + VARIABLE + "\""));

		CloudBasedRepository.Config<?> copy = (CloudBasedRepository.Config<?>) fromXML(written);
		assertTrue(copy.getConnectString() instanceof NamedSecretSource.Config);
		assertEquals(written, toXML(copy));
	}

	/** A configured connection string is written encrypted. */
	public void testWriteLiteralConnectString() throws Exception {
		CloudBasedRepository.Config<?> config = read(xml(attribute("unencrypted:" + CONNECT_STRING), ""));

		String written = toXML(config);
		assertFalse("Connection string written in plain text: " + written, written.contains(CONNECT_STRING));
		assertTrue("Not written as encrypted attribute: " + written,
			written.contains(CloudBasedRepository.Config.CONNECT_STRING + "=\""
				+ ConfigurationEncryption.AUTHENTICATED_PREFIX));

		CloudBasedRepository.Config<?> copy = (CloudBasedRepository.Config<?>) fromXML(written);
		assertEquals(CONNECT_STRING, connectString(copy));
	}

	private String connectString(CloudBasedRepository.Config<?> config) throws Exception {
		SecretSource source = getInstance(config.getConnectString());
		return source.getSecret();
	}

	private static String attribute(String value) {
		return CloudBasedRepository.Config.CONNECT_STRING + "='" + value + "'";
	}

	private static String xml(String attributes, String content) {
		return "<" + ROOT_TAG + " class='" + CloudBasedRepository.class.getName() + "'"
			+ " " + CloudBasedRepository.Config.CONTAINER_NAME + "='container'"
			+ " " + CloudBasedRepository.Config.DIRECTORY_NAME + "='directory'"
			+ " path='repository' "
			+ attributes + ">" + content + "</" + ROOT_TAG + ">";
	}

	private static String environmentConnectString() {
		return "<" + CloudBasedRepository.Config.CONNECT_STRING + " name='" + VARIABLE + "'/>";
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(CloudBasedRepository.Config.class));
	}

	public static Test suite() {
		return suite(TestCloudBasedRepositoryConnectString.class, SecretStoreService.Module.INSTANCE);
	}

}
