/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Test case for {@link JsonConfigSchemaBuilder} @Key annotation handling.
 */
public class TestJsonConfigSchemaBuilderKeyAnnotation extends BasicTestCase {

	/**
	 * A simple value configuration with a name property.
	 */
	public interface NamedItem extends ConfigurationItem {
		String NAME = "name";
		String VALUE = "value";

		@Name(NAME)
		@Mandatory
		String getName();

		@Name(VALUE)
		String getValue();
	}

	/**
	 * A configuration with a map keyed by the "name" property of values.
	 */
	public interface ConfigWithMap extends ConfigurationItem {
		String ITEMS = "items";

		@Name(ITEMS)
		@Key(NamedItem.NAME)
		java.util.Map<String, NamedItem> getItems();
	}

	/**
	 * Tests that @Key annotation removes the key property from value schema and uses it for
	 * propertyNames constraint.
	 */
	public void testKeyAnnotationHandling() {
		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder();
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ConfigWithMap.class);

		Schema schema = builder.build(descriptor);

		assertTrue("Should be ObjectSchema, was " + schema.getClass().getName(), schema instanceof ObjectSchema);
		ObjectSchema objectSchema = (ObjectSchema) schema;

		Schema itemsProperty = objectSchema.getProperties().get("items");
		assertNotNull("Should have items property", itemsProperty);
		assertTrue("Should be ObjectSchema", itemsProperty instanceof ObjectSchema);

		ObjectSchema itemsMapSchema = (ObjectSchema) itemsProperty;

		// Check that propertyNames constraint is set
		Schema propertyNamesSchema = itemsMapSchema.getPropertyNames();
		assertNotNull("Should have propertyNames constraint", propertyNamesSchema);

		// Check the value schema
		Schema valueSchema = itemsMapSchema.getAdditionalProperties();
		assertNotNull("Should have additionalProperties (value schema)", valueSchema);
		assertTrue("Value schema should be ObjectSchema", valueSchema instanceof ObjectSchema);

		ObjectSchema valueObjectSchema = (ObjectSchema) valueSchema;

		// The "name" property should be removed from the value schema
		assertFalse("Value schema should NOT contain 'name' property",
			valueObjectSchema.getProperties().containsKey("name"));

		// But should still contain the "value" property
		assertTrue("Value schema should contain 'value' property",
			valueObjectSchema.getProperties().containsKey("value"));

		// The "name" should not be in required list
		assertFalse("Value schema required should NOT contain 'name'",
			valueObjectSchema.getRequired().contains("name"));

		System.out.println("Map with @Key annotation schema:");
		System.out.println(JsonSchemaWriter.toJson(itemsMapSchema, true));
	}

	/**
	 * Tests complete round-trip scenario.
	 */
	public void testKeyAnnotationFullExample() {
		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder();
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ConfigWithMap.class);

		Schema schema = builder.build(descriptor);
		String json = JsonSchemaWriter.toJson(schema, true);

		System.out.println("Complete schema with @Key annotation:");
		System.out.println(json);

		// Verify JSON contains expected structure
		assertTrue("Should contain 'items' property", json.contains("\"items\""));
		assertTrue("Should contain 'additionalProperties'", json.contains("\"additionalProperties\""));
		assertTrue("Should contain 'propertyNames'", json.contains("\"propertyNames\""));
		assertTrue("Should contain 'value' in value schema", json.contains("\"value\""));
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(
			ServiceTestSetup.createSetup(
				TestJsonConfigSchemaBuilderKeyAnnotation.class, ThreadContextManager.Module.INSTANCE));
	}
	
}
