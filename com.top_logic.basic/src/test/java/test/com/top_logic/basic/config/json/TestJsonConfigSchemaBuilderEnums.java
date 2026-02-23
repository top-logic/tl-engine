/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;

/**
 * Test case for {@link JsonConfigSchemaBuilder} enum handling.
 */
@SuppressWarnings("javadoc")
public class TestJsonConfigSchemaBuilderEnums extends AbstractJsonConfigurationWriterTest {

	/**
	 * Regular enum using standard Java names.
	 */
	public enum RegularEnum {
		VALUE_ONE,
		VALUE_TWO,
		VALUE_THREE
	}

	/**
	 * Enum implementing {@link ExternallyNamed} for custom serialization.
	 */
	public enum ExternalEnum implements ExternallyNamed {
		OPTION_A("option-a"),
		OPTION_B("option-b"),
		OPTION_C("option-c");

		private final String _externalName;

		ExternalEnum(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Enum implementing {@link de.haumacher.msgbuf.data.ProtocolEnum} for msgbuf protocol.
	 */
	public enum ProtocolEnum implements de.haumacher.msgbuf.data.ProtocolEnum {
		LEFT("left"),
		CENTER("center"),
		RIGHT("right");

		private final String _protocolName;

		ProtocolEnum(String protocolName) {
			_protocolName = protocolName;
		}

		@Override
		public String protocolName() {
			return _protocolName;
		}
	}

	/**
	 * Enum using {@link Name} annotations for custom serialization names.
	 */
	public enum NameAnnotatedEnum {
		@Name("first-option")
		FIRST,

		@Name("second-option")
		SECOND,

		@Name("third-option")
		THIRD
	}

	/**
	 * Configuration interface with regular enum property.
	 */
	public interface ConfigWithRegularEnum extends ConfigurationItem {
		String REGULAR_ENUM = "regular-enum";

		@Name(REGULAR_ENUM)
		RegularEnum getRegularEnum();
	}

	/**
	 * Configuration interface with ExternallyNamed enum property.
	 */
	public interface ConfigWithExternalEnum extends ConfigurationItem {
		String EXTERNAL_ENUM = "external-enum";

		@Name(EXTERNAL_ENUM)
		ExternalEnum getExternalEnum();
	}

	/**
	 * Configuration interface with ProtocolEnum property.
	 */
	public interface ConfigWithProtocolEnum extends ConfigurationItem {
		String PROTOCOL_ENUM = "protocol-enum";

		@Name(PROTOCOL_ENUM)
		ProtocolEnum getProtocolEnum();
	}

	/**
	 * Configuration interface with Name-annotated enum property.
	 */
	public interface ConfigWithNameAnnotatedEnum extends ConfigurationItem {
		String NAME_ANNOTATED_ENUM = "name-annotated-enum";

		@Name(NAME_ANNOTATED_ENUM)
		NameAnnotatedEnum getNameAnnotatedEnum();
	}

	/**
	 * Tests that regular enum uses Java constant names.
	 */
	public void testRegularEnum() throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ConfigWithRegularEnum.class);

		String actualJson = buildSchemaJson(descriptor);
		String expectedJson = loadExpectedSchema("TestJsonConfigSchemaBuilderEnums-testRegularEnum.json", actualJson);

		assertEquals(expectedJson, actualJson);
	}

	/**
	 * Tests that ExternallyNamed enum uses external names.
	 */
	public void testExternallyNamedEnum() throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ConfigWithExternalEnum.class);

		String actualJson = buildSchemaJson(descriptor);
		String expectedJson =
			loadExpectedSchema("TestJsonConfigSchemaBuilderEnums-testExternallyNamedEnum.json", actualJson);

		assertEquals(expectedJson, actualJson);
	}

	/**
	 * Tests that ProtocolEnum uses protocol names.
	 */
	public void testProtocolEnum() throws Exception {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ConfigWithProtocolEnum.class);

		String actualJson = buildSchemaJson(descriptor);
		String expectedJson =
			loadExpectedSchema("TestJsonConfigSchemaBuilderEnums-testProtocolEnum.json", actualJson);

		assertEquals(expectedJson, actualJson);
	}

	/**
	 * Tests that Name-annotated enum uses annotation values.
	 */
	public void testNameAnnotatedEnum() throws Exception {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ConfigWithNameAnnotatedEnum.class);

		String actualJson = buildSchemaJson(descriptor);
		String expectedJson =
			loadExpectedSchema("TestJsonConfigSchemaBuilderEnums-testNameAnnotatedEnum.json", actualJson);

		assertEquals(expectedJson, actualJson);
	}

	private String loadExpectedSchema(String resourceName, String actualSchema) throws Exception {
		java.io.InputStream in = getClass().getResourceAsStream(resourceName);
		if (in == null) {
			fail("Expected schema resource not found: " + resourceName +
				". Create this file with the expected schema. Actual schema: \n" + actualSchema);
			return "";
		} else {
			return new String(in.readAllBytes(), "UTF-8");
		}
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonConfigSchemaBuilderEnums.class));
	}
}
