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
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.Schema;

/**
 * Test case for {@link JsonConfigSchemaBuilder} @Key annotation handling.
 */
public class TestJsonConfigSchemaBuilderKeyAnnotation extends AbstractJsonConfigurationWriterTest {

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
	 * Abstract base interface for named things.
	 */
	@Abstract
	public interface Named extends ConfigurationItem {
		String NAME = "name";

		@Name(NAME)
		@Mandatory
		String getName();

		void setName(String value);
	}

	/**
	 * A person with a name and age.
	 */
	public interface Person extends Named {
		String AGE = "age";

		@Name(AGE)
		int getAge();

		void setAge(int value);
	}

	/**
	 * An organization with a name and employee count.
	 */
	public interface Organization extends Named {
		String EMPLOYEE_COUNT = "employee-count";

		@Name(EMPLOYEE_COUNT)
		int getEmployeeCount();

		void setEmployeeCount(int value);
	}

	/**
	 * A configuration with a map of polymorphic named items.
	 */
	public interface ConfigWithPolymorphicMap extends ConfigurationItem {
		String ENTITIES = "entities";

		@Name(ENTITIES)
		@Key(Named.NAME)
		java.util.Map<String, Named> getEntities();
	}

	/**
	 * Tests that @Key annotation is properly handled in the generated schema.
	 */
	public void testKeyAnnotationHandling() throws Exception {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ConfigWithMap.class);

		Schema schema = new JsonConfigSchemaBuilder().setInline(true).build(descriptor);
		String actualJson = JsonSchemaWriter.toJson(schema, true);

		String expectedJson = loadExpectedSchema("TestJsonConfigSchemaBuilderKeyAnnotation-testKeyAnnotationHandling.json");

		assertEquals(expectedJson, actualJson);
	}

	/**
	 * Tests @Key annotation with a polymorphic type hierarchy.
	 */
	public void testKeyAnnotationPolymorphic() throws Exception {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ConfigWithPolymorphicMap.class);

		Schema schema = new JsonConfigSchemaBuilder().setInline(true).build(descriptor);
		String actualJson = JsonSchemaWriter.toJson(schema, true);

		String expectedJson = loadExpectedSchema("TestJsonConfigSchemaBuilderKeyAnnotation-testKeyAnnotationPolymorphic.json");

		assertEquals(expectedJson, actualJson);
	}

	/**
	 * Tests that serialized configuration with @Key annotation validates against the schema.
	 *
	 * <p>
	 * The schema builder creates specialized map value schemas that exclude the key property, so
	 * the serialized JSON (which omits the key property since it's used as the map key) should
	 * validate successfully.
	 * </p>
	 */
	public void testKeyAnnotationSerializationValidation() throws Exception {
		// Create a configuration with polymorphic map entries
		ConfigWithPolymorphicMap config = TypedConfiguration.newConfigItem(ConfigWithPolymorphicMap.class);

		Person person = TypedConfiguration.newConfigItem(Person.class);
		person.setName("John");
		person.setAge(30);
		config.getEntities().put("John", person);

		Organization org = TypedConfiguration.newConfigItem(Organization.class);
		org.setName("Acme");
		org.setEmployeeCount(100);
		config.getEntities().put("Acme", org);

		// Serialize configuration to JSON and validate against schema
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ConfigWithPolymorphicMap.class);
		String configJson = writeJson(descriptor, config);
		validateAgainstSchema(descriptor, configJson);
	}

	private String loadExpectedSchema(String resourceName) throws Exception {
		java.io.InputStream in = getClass().getResourceAsStream(resourceName);
		if (in == null) {
			fail("Expected schema resource not found: " + resourceName +
				". Create this file in the test resources directory with the generated schema output above.");
		}
		return new String(in.readAllBytes(), "UTF-8");
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonConfigSchemaBuilderKeyAnnotation.class));
	}

}
