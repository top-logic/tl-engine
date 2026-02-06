/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.EnumSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Test case for {@link JsonConfigSchemaBuilder} enum handling.
 */
@SuppressWarnings("javadoc")
public class TestJsonConfigSchemaBuilderEnums extends TestCase {

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
	 * Tests that regular enum uses Java constant names.
	 */
	public void testRegularEnum() {
		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder();
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ConfigWithRegularEnum.class);

		Schema schema = builder.buildConfigSchema(descriptor);

		assertTrue("Should be ObjectSchema", schema instanceof ObjectSchema);
		ObjectSchema objectSchema = (ObjectSchema) schema;

		Schema enumProperty = objectSchema.getProperties().get("regular-enum");
		assertNotNull("Should have regular-enum property", enumProperty);
		assertTrue("Should be EnumSchema", enumProperty instanceof EnumSchema);

		EnumSchema enumSchema = (EnumSchema) enumProperty;
		assertEquals("Should have 3 values", 3, enumSchema.getEnumLiterals().size());
		assertTrue("Should contain VALUE_ONE", enumSchema.getEnumLiterals().contains("VALUE_ONE"));
		assertTrue("Should contain VALUE_TWO", enumSchema.getEnumLiterals().contains("VALUE_TWO"));
		assertTrue("Should contain VALUE_THREE", enumSchema.getEnumLiterals().contains("VALUE_THREE"));

		System.out.println("Regular Enum Schema:");
		System.out.println(JsonSchemaWriter.toJson(enumSchema, true));
	}

	/**
	 * Tests that ExternallyNamed enum uses external names.
	 */
	public void testExternallyNamedEnum() {
		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder();
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ConfigWithExternalEnum.class);

		Schema schema = builder.buildConfigSchema(descriptor);

		assertTrue("Should be ObjectSchema", schema instanceof ObjectSchema);
		ObjectSchema objectSchema = (ObjectSchema) schema;

		Schema enumProperty = objectSchema.getProperties().get("external-enum");
		assertNotNull("Should have external-enum property", enumProperty);
		assertTrue("Should be EnumSchema", enumProperty instanceof EnumSchema);

		EnumSchema enumSchema = (EnumSchema) enumProperty;
		assertEquals("Should have 3 values", 3, enumSchema.getEnumLiterals().size());
		assertTrue("Should contain option-a", enumSchema.getEnumLiterals().contains("option-a"));
		assertTrue("Should contain option-b", enumSchema.getEnumLiterals().contains("option-b"));
		assertTrue("Should contain option-c", enumSchema.getEnumLiterals().contains("option-c"));

		System.out.println("ExternallyNamed Enum Schema:");
		System.out.println(JsonSchemaWriter.toJson(enumSchema, true));
	}

	/**
	 * Tests that ProtocolEnum uses protocol names.
	 */
	public void testProtocolEnum() {
		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder();
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ConfigWithProtocolEnum.class);

		Schema schema = builder.buildConfigSchema(descriptor);

		assertTrue("Should be ObjectSchema", schema instanceof ObjectSchema);
		ObjectSchema objectSchema = (ObjectSchema) schema;

		Schema enumProperty = objectSchema.getProperties().get("protocol-enum");
		assertNotNull("Should have protocol-enum property", enumProperty);
		assertTrue("Should be EnumSchema", enumProperty instanceof EnumSchema);

		EnumSchema enumSchema = (EnumSchema) enumProperty;
		assertEquals("Should have 3 values", 3, enumSchema.getEnumLiterals().size());
		assertTrue("Should contain left", enumSchema.getEnumLiterals().contains("left"));
		assertTrue("Should contain center", enumSchema.getEnumLiterals().contains("center"));
		assertTrue("Should contain right", enumSchema.getEnumLiterals().contains("right"));

		System.out.println("ProtocolEnum Schema:");
		System.out.println(JsonSchemaWriter.toJson(enumSchema, true));
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(
			ServiceTestSetup.createSetup(
				TestJsonConfigSchemaBuilderKeyAnnotation.class, ThreadContextManager.Module.INSTANCE));
	}
}
