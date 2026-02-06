/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.json.schema;

import junit.framework.TestCase;

import com.top_logic.basic.json.schema.JsonSchemaSerializer;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.ArraySchema;
import com.top_logic.basic.json.schema.model.BooleanSchema;
import com.top_logic.basic.json.schema.model.EnumSchema;
import com.top_logic.basic.json.schema.model.JsonSchemaDocument;
import com.top_logic.basic.json.schema.model.NullSchema;
import com.top_logic.basic.json.schema.model.NumericSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.StringSchema;

/**
 * Test case for {@link JsonSchemaSerializer}.
 */
public class TestJsonSchemaSerializer extends TestCase {

	/**
	 * Tests serialization of a simple person schema wrapped in a JsonSchemaDocument.
	 */
	public void testPersonSchema() {
		// Build a person schema with name and age properties
		ObjectSchema personSchema = ObjectSchema.create()
			.putProperty("name", StringSchema.create()
				.setTitle("Name")
				.setDescription("The person's full name")
				.setMinLength(1))
			.putProperty("age", NumericSchema.create()
				.setTitle("Age")
				.setDescription("The person's age in years")
				.setIntegerOnly(true)
				.setMinimum(0.0)
				.setMaximum(150.0))
			.putProperty("email", StringSchema.create()
				.setTitle("Email")
				.setFormat("email"))
			.addRequired("name");

		// Wrap in a document with metadata
		JsonSchemaDocument document = JsonSchemaDocument.create()
			.setSchemaDialectUri("https://json-schema.org/draft/2020-12/schema")
			.setTitle("Person")
			.setDescription("A person with name, age, and optional email")
			.setSchema(personSchema);

		// Serialize to JSON
		String json = JsonSchemaWriter.toJson(document, true);

		// Verify output contains expected elements
		assertTrue("Should contain $schema", json.contains("\"$schema\""));
		assertTrue("Should contain title", json.contains("\"title\""));
		assertTrue("Should contain Person", json.contains("\"Person\""));
		assertTrue("Should contain type object", json.contains("\"type\": \"object\""));
		assertTrue("Should contain properties", json.contains("\"properties\""));
		assertTrue("Should contain name property", json.contains("\"name\""));
		assertTrue("Should contain age property", json.contains("\"age\""));
		assertTrue("Should contain email property", json.contains("\"email\""));
		assertTrue("Should contain required", json.contains("\"required\""));
		assertTrue("Should contain integer type", json.contains("\"type\": \"integer\""));
		assertTrue("Should contain string type", json.contains("\"type\": \"string\""));
		assertTrue("Should contain email format", json.contains("\"format\": \"email\""));

		// Print for manual inspection
		System.out.println("Person Schema:");
		System.out.println(json);
	}

	/**
	 * Tests serialization of all basic schema types.
	 */
	public void testBasicTypes() {
		// String schema
		StringSchema stringSchema = StringSchema.create()
			.setMinLength(1)
			.setMaxLength(100)
			.setPattern("^[A-Za-z]+$");
		String stringJson = JsonSchemaWriter.toJson(stringSchema);
		System.out.println("String Schema: " + stringJson);
		assertTrue("String should have type", stringJson.contains("\"type\":\"string\""));
		assertTrue("String should have minLength", stringJson.contains("\"minLength\""));
		assertTrue("String should have maxLength", stringJson.contains("\"maxLength\""));
		assertTrue("String should have pattern", stringJson.contains("\"pattern\""));

		// Integer schema
		NumericSchema integerSchema = NumericSchema.create()
			.setIntegerOnly(true)
			.setMinimum(0.0)
			.setMaximum(100.0);
		String integerJson = JsonSchemaWriter.toJson(integerSchema);
		assertTrue("Integer should have type integer", integerJson.contains("\"type\":\"integer\""));
		assertTrue("Integer should have minimum", integerJson.contains("\"minimum\""));
		assertTrue("Integer should have maximum", integerJson.contains("\"maximum\""));

		// Number schema
		NumericSchema numberSchema = NumericSchema.create()
			.setMinimum(0.0)
			.setExclusiveMaximum(1.0);
		String numberJson = JsonSchemaWriter.toJson(numberSchema);
		assertTrue("Number should have type number", numberJson.contains("\"type\":\"number\""));
		assertTrue("Number should have exclusiveMaximum", numberJson.contains("\"exclusiveMaximum\""));

		// Boolean schema
		BooleanSchema booleanSchema = BooleanSchema.create();
		String booleanJson = JsonSchemaWriter.toJson(booleanSchema);
		assertTrue("Boolean should have type", booleanJson.contains("\"type\":\"boolean\""));

		// Null schema
		NullSchema nullSchema = NullSchema.create();
		String nullJson = JsonSchemaWriter.toJson(nullSchema);
		assertTrue("Null should have type", nullJson.contains("\"type\":\"null\""));

		System.out.println("Integer Schema: " + integerJson);
		System.out.println("Number Schema: " + numberJson);
		System.out.println("Boolean Schema: " + booleanJson);
		System.out.println("Null Schema: " + nullJson);
	}

	/**
	 * Tests serialization of an array schema.
	 */
	public void testArraySchema() {
		// Array of strings with constraints
		ArraySchema tagsSchema = ArraySchema.create()
			.setTitle("Tags")
			.setDescription("List of tags")
			.setItems(StringSchema.create().setMinLength(1))
			.setMinItems(1)
			.setMaxItems(10)
			.setUniqueItems(true);

		String json = JsonSchemaWriter.toJson(tagsSchema, true);

		assertTrue("Should have type array", json.contains("\"type\": \"array\""));
		assertTrue("Should have items", json.contains("\"items\""));
		assertTrue("Should have minItems", json.contains("\"minItems\""));
		assertTrue("Should have maxItems", json.contains("\"maxItems\""));
		assertTrue("Should have uniqueItems", json.contains("\"uniqueItems\""));

		System.out.println("Array Schema:");
		System.out.println(json);
	}

	/**
	 * Tests serialization of an enum schema.
	 */
	public void testEnumSchema() {
		EnumSchema statusSchema = EnumSchema.create()
			.setTitle("Status")
			.setDescription("Order status")
			.addEnumLiteral("pending")
			.addEnumLiteral("processing")
			.addEnumLiteral("shipped")
			.addEnumLiteral("delivered")
			.addEnumLiteral("cancelled");

		String json = JsonSchemaWriter.toJson(statusSchema, true);

		assertTrue("Should have enum", json.contains("\"enum\""));
		assertTrue("Should have pending", json.contains("\"pending\""));
		assertTrue("Should have delivered", json.contains("\"delivered\""));

		System.out.println("Enum Schema:");
		System.out.println(json);
	}

	/**
	 * Tests serialization of a nested object schema.
	 */
	public void testNestedObjectSchema() {
		// Address schema
		ObjectSchema addressSchema = ObjectSchema.create()
			.putProperty("street", StringSchema.create())
			.putProperty("city", StringSchema.create())
			.putProperty("zipCode", StringSchema.create().setPattern("^\\d{5}$"))
			.addRequired("street")
			.addRequired("city");

		// Person with address
		ObjectSchema personWithAddressSchema = ObjectSchema.create()
			.putProperty("name", StringSchema.create())
			.putProperty("address", addressSchema)
			.addRequired("name")
			.addRequired("address");

		JsonSchemaDocument document = JsonSchemaDocument.create()
			.setSchemaDialectUri("https://json-schema.org/draft/2020-12/schema")
			.setTitle("Person with Address")
			.setSchema(personWithAddressSchema);

		String json = JsonSchemaWriter.toJson(document, true);

		assertTrue("Should have nested object", json.contains("\"address\""));
		assertTrue("Should have street", json.contains("\"street\""));
		assertTrue("Should have city", json.contains("\"city\""));
		assertTrue("Should have zipCode pattern", json.contains("\"pattern\""));

		System.out.println("Nested Object Schema:");
		System.out.println(json);
	}

	/**
	 * Tests serialization with $defs (reusable definitions).
	 */
	public void testSchemaWithDefinitions() {
		// Define a reusable address schema
		ObjectSchema addressDef = ObjectSchema.create()
			.putProperty("street", StringSchema.create())
			.putProperty("city", StringSchema.create())
			.putProperty("country", StringSchema.create());

		// Main schema using the definition
		ObjectSchema mainSchema = ObjectSchema.create()
			.putProperty("name", StringSchema.create());

		// Add the definition to the schema
		mainSchema.putDefinition("Address", addressDef);

		JsonSchemaDocument document = JsonSchemaDocument.create()
			.setSchemaDialectUri("https://json-schema.org/draft/2020-12/schema")
			.setTitle("Schema with Definitions")
			.setSchema(mainSchema);

		String json = JsonSchemaWriter.toJson(document, true);

		assertTrue("Should have $defs", json.contains("\"$defs\""));
		assertTrue("Should have Address definition", json.contains("\"Address\""));

		System.out.println("Schema with Definitions:");
		System.out.println(json);
	}

	/**
	 * Tests serialization of metadata annotations.
	 */
	public void testMetadataAnnotations() {
		StringSchema annotatedSchema = StringSchema.create()
			.setTitle("Username")
			.setDescription("The user's unique username")
			.setDefaultValue("\"anonymous\"")
			.addExample("\"john_doe\"")
			.addExample("\"jane_smith\"")
			.setDeprecated(false)
			.setReadOnly(false);

		String json = JsonSchemaWriter.toJson(annotatedSchema, true);

		assertTrue("Should have title", json.contains("\"title\": \"Username\""));
		assertTrue("Should have description", json.contains("\"description\""));
		assertTrue("Should have default", json.contains("\"default\""));
		assertTrue("Should have examples", json.contains("\"examples\""));

		System.out.println("Annotated Schema:");
		System.out.println(json);
	}
}
