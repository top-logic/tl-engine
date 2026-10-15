/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * Test for entries of {@link XMLProperties} marked with {@link XMLProperties#ENCRYPT_ATTR}.
 */
public class TestXMLPropertiesEncryption extends TestCase {

	private static final String SECTION = "TestXMLPropertiesEncryption";

	private static final String KEY = "password";

	/**
	 * An encrypted value is decrypted when read.
	 */
	public void testDecryptValue() throws IOException {
		String plaintext = "Some secret text";
		XMLProperties properties = load(ConfigurationEncryption.encrypt(plaintext));
		assertEquals(plaintext, properties.getProperties(SECTION).getProperty(KEY));
	}

	/**
	 * A value that cannot be decrypted is rejected without reporting the value.
	 */
	public void testUndecryptableValueNotReported() throws IOException {
		String secret = "PlainSecret";
		try {
			load(secret);
			fail("Must not be able to decrypt an unencrypted value.");
		} catch (ConfigurationError ex) {
			StringBuilder report = new StringBuilder();
			for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
				report.append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append('\n');
			}
			String reported = report.toString();
			assertTrue("Key not reported: " + reported, reported.contains(KEY));
			assertFalse("Undecryptable value reported: " + reported, reported.contains(secret));
		}
	}

	private static XMLProperties load(String value) throws IOException {
		String xml = "<" + XMLProperties.ROOT_ELEMENT + ">"
			+ "<" + XMLProperties.PROP_NODE + ">"
			+ "<" + XMLProperties.SECTION_NODE + " " + XMLProperties.KEY_ATTR + "='" + SECTION + "'>"
			+ "<" + XMLProperties.ENTRY_NODE
			+ " " + XMLProperties.KEY_ATTR + "='" + KEY + "'"
			+ " " + XMLProperties.VALUE_ATTR + "='" + value + "'"
			+ " " + XMLProperties.ENCRYPT_ATTR + "='true'"
			+ "/>"
			+ "</" + XMLProperties.SECTION_NODE + ">"
			+ "</" + XMLProperties.PROP_NODE + ">"
			+ "</" + XMLProperties.ROOT_ELEMENT + ">";
		return new XMLProperties(BinaryDataFactory.createBinaryData(xml.getBytes(StandardCharsets.UTF_8)));
	}

}
