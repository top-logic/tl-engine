/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.FalseSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;

/**
 * Test for {@link JsonConfigSchemaBuilder} with a configuration type declared in XML (a
 * {@link ConfigurationDescriptorConfig}, as the {@code <properties>} of a layout template).
 */
@SuppressWarnings("javadoc")
public class TestJsonConfigSchemaBuilderDeclared extends BasicTestCase {

	/**
	 * A programmatic type the declared type extends.
	 */
	public interface Extended extends ConfigurationItem {

		String FLAG = "flag";

		@Name(FLAG)
		boolean getFlag();
	}

	private static final String DECLARATION =
		"<properties extends='" + Extended.class.getName() + "'>"
			+ "<property name='name' type='String'>"
			+ "<label><en>Name</en><tooltip><en>The name to use.</en></tooltip></label>"
			+ "<mandatory/>"
			+ "</property>"
			+ "<property name='count' type='int'/>"
			+ "</properties>";

	/**
	 * A declared type gets the schema of its own properties, not the schema of all configurations
	 * its interface {@link ConfigurationItem} would stand for.
	 */
	public void testDeclaredTypeHasSchemaOfItsProperties() throws ConfigurationException {
		ObjectSchema schema = schema(DECLARATION);

		assertEquals(schema.getProperties().keySet().toString(), 3, schema.getProperties().size());
		assertTrue(schema.getProperties().containsKey("name"));
		assertTrue(schema.getProperties().containsKey("count"));
		assertTrue("Properties of an extended type are part of it.",
			schema.getProperties().containsKey(Extended.FLAG));
		assertFalse("A declared type has no type name to state.", schema.getProperties().containsKey("$type"));
		assertEquals(Collections.singletonList("name"), schema.getRequired());
		assertTrue(schema.getAdditionalProperties() instanceof FalseSchema);
	}

	/**
	 * The label of a declared property is the title of its schema, the tooltip of the label its
	 * description.
	 */
	public void testDeclaredLabelAndTooltip() throws ConfigurationException {
		Schema name = schema(DECLARATION).getProperties().get("name");

		assertEquals("Name", name.getTitle());
		assertEquals("The name to use.", name.getDescription());
	}

	/**
	 * The descriptor of {@link ConfigurationItem} itself still stands for all configurations.
	 */
	public void testConfigurationItemStandsForAllConfigurations() {
		Schema schema = new JsonConfigSchemaBuilder().setInline(true)
			.buildConfigSchema(TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class));

		String json = JsonSchemaWriter.toJson(schema, false);
		assertTrue(json, json.contains("\"required\":[\"$type\"]"));
	}

	private static ObjectSchema schema(String declaration) throws ConfigurationException {
		Schema schema = new JsonConfigSchemaBuilder().setInline(true).buildConfigSchema(descriptor(declaration));
		assertTrue("Expected an object schema, but got: " + JsonSchemaWriter.toJson(schema, true),
			schema instanceof ObjectSchema);
		return (ObjectSchema) schema;
	}

	private static ConfigurationDescriptor descriptor(String declaration) throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("properties",
				TypedConfiguration.getConfigurationDescriptor(ConfigurationDescriptorConfig.class)));
		reader.setSource(CharacterContents.newContent(declaration));
		return TypedConfiguration.getConfigurationDescriptor((ConfigurationDescriptorConfig) reader.read());
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonConfigSchemaBuilderDeclared.class));
	}
}
