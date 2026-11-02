/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.client.registry.impl.value;

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.service.openapi.client.registry.impl.value.SecretValue;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducer;

/**
 * Test case for {@link SecretValue}.
 */
@SuppressWarnings("javadoc")
public class TestSecretValue extends TestCase {

	private static final String VARIABLE = "TL_TEST_OPENAPI_CLIENT_SECRET_VALUE";

	private static final String SECRET = "s3cr3t-value";

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	public void testNamedSecret() throws Exception {
		System.setProperty(VARIABLE, SECRET);
		ValueProducer producer = producer("<secret name='" + VARIABLE + "'/>", "");
		assertEquals(SECRET, producer.getValue(null));
	}

	public void testSecretReadAtUse() throws Exception {
		System.setProperty(VARIABLE, SECRET);
		ValueProducer producer = producer(
			"<secret class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>", "");
		assertEquals(SECRET, producer.getValue(null));

		System.setProperty(VARIABLE, "rotated");
		assertEquals("A rotated secret must be used without re-creating the producer.", "rotated",
			producer.getValue(null));
	}

	public void testLiteralAttributeForm() throws Exception {
		ValueProducer producer = producer("", "secret='" + ConfigurationEncryption.encrypt(SECRET) + "'");
		assertEquals(SECRET, producer.getValue(null));
	}

	public void testWriteNamedSecretKeepsName() throws Exception {
		String xml = toXML(parse(config("<secret name='" + VARIABLE + "'/>", "")));
		assertTrue(xml, xml.contains("name=\"" + VARIABLE + "\""));
	}

	public void testWriteLiteralEncrypted() throws Exception {
		String xml = toXML(parse(config("", "secret='unencrypted:" + SECRET + "'")));
		assertFalse("The secret must not be written in plain text: " + xml, xml.contains(SECRET));
		assertTrue(xml, xml.contains("secret=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));
	}

	private static ValueProducer producer(String secretElement, String secretAttribute) throws ConfigurationException {
		SecretValue factory = TypedConfigUtil.createInstance(parse(config(secretElement, secretAttribute)));
		return factory.build(null);
	}

	private static String config(String secretElement, String secretAttribute) {
		return "<value class='" + SecretValue.class.getName() + "' " + secretAttribute + ">" + secretElement
			+ "</value>";
	}

	private static SecretValue.Config<?> parse(String xml) throws ConfigurationException {
		return (SecretValue.Config<?>) TypedConfiguration.parse("value", SecretValue.Config.class,
			CharacterContents.newContent(xml));
	}

	private static String toXML(SecretValue.Config<?> config) throws Exception {
		StringWriter buffer = new StringWriter();
		try (ConfigurationWriter writer = new ConfigurationWriter(buffer)) {
			writer.write("value", SecretValue.Config.class, config);
		}
		return buffer.toString();
	}

	/**
	 * Reading a configuration resolves its types through the <code>TypeIndex</code>.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestSecretValue.class), SecretStoreService.Module.INSTANCE));
	}

}
