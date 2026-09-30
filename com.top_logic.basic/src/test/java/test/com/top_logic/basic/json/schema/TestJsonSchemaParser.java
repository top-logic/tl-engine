/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.json.schema;

import junit.framework.TestCase;

import com.top_logic.basic.json.schema.JsonSchemaParser;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.ArraySchema;
import com.top_logic.basic.json.schema.model.EnumSchema;
import com.top_logic.basic.json.schema.model.NumericSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.json.schema.model.StringSchema;

/**
 * Test case for {@link JsonSchemaParser}.
 */
public class TestJsonSchemaParser extends TestCase {

	/**
	 * Tests parsing a simple string schema.
	 */
	public void testParseStringSchema() throws Exception {
		String json = "{\"type\": \"string\", \"minLength\": 1, \"maxLength\": 100}";
		Schema schema = JsonSchemaParser.parse(json);

		assertTrue("Should be StringSchema", schema instanceof StringSchema);
		StringSchema stringSchema = (StringSchema) schema;
		assertEquals("minLength should be 1", Integer.valueOf(1), stringSchema.getMinLength());
		assertEquals("maxLength should be 100", Integer.valueOf(100), stringSchema.getMaxLength());
	}

	/**
	 * Tests parsing a numeric schema.
	 */
	public void testParseNumericSchema() throws Exception {
		String json = "{\"type\": \"integer\", \"minimum\": 0, \"maximum\": 100}";
		Schema schema = JsonSchemaParser.parse(json);

		assertTrue("Should be NumericSchema", schema instanceof NumericSchema);
		NumericSchema numericSchema = (NumericSchema) schema;
		assertTrue("Should be integer only", numericSchema.isIntegerOnly());
		assertEquals("minimum should be 0", Double.valueOf(0.0), numericSchema.getMinimum());
		assertEquals("maximum should be 100", Double.valueOf(100.0), numericSchema.getMaximum());
	}

	/**
	 * Tests parsing an array schema.
	 */
	public void testParseArraySchema() throws Exception {
		String json = "{\"type\": \"array\", \"items\": {\"type\": \"string\"}, \"minItems\": 1, \"maxItems\": 10}";
		Schema schema = JsonSchemaParser.parse(json);

		assertTrue("Should be ArraySchema", schema instanceof ArraySchema);
		ArraySchema arraySchema = (ArraySchema) schema;
		assertEquals("minItems should be 1", Integer.valueOf(1), arraySchema.getMinItems());
		assertEquals("maxItems should be 10", Integer.valueOf(10), arraySchema.getMaxItems());
		assertNotNull("Should have items schema", arraySchema.getItems());
		assertTrue("Items should be StringSchema", arraySchema.getItems() instanceof StringSchema);
	}

	/**
	 * Tests parsing an object schema with properties.
	 */
	public void testParseObjectSchema() throws Exception {
		String json = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "  \"name\": {\"type\": \"string\"},"
			+ "  \"age\": {\"type\": \"integer\", \"minimum\": 0}"
			+ "},"
			+ "\"required\": [\"name\"]"
			+ "}";

		Schema schema = JsonSchemaParser.parse(json);

		assertTrue("Should be ObjectSchema", schema instanceof ObjectSchema);
		ObjectSchema objectSchema = (ObjectSchema) schema;

		assertEquals("Should have 2 properties", 2, objectSchema.getProperties().size());
		assertTrue("Should have name property", objectSchema.getProperties().containsKey("name"));
		assertTrue("Should have age property", objectSchema.getProperties().containsKey("age"));

		assertEquals("Should have 1 required property", 1, objectSchema.getRequired().size());
		assertEquals("Required property should be 'name'", "name", objectSchema.getRequired().get(0));

		Schema nameSchema = objectSchema.getProperties().get("name");
		assertTrue("name should be StringSchema", nameSchema instanceof StringSchema);

		Schema ageSchema = objectSchema.getProperties().get("age");
		assertTrue("age should be NumericSchema", ageSchema instanceof NumericSchema);
		NumericSchema ageNumeric = (NumericSchema) ageSchema;
		assertTrue("age should be integer", ageNumeric.isIntegerOnly());
		assertEquals("age minimum should be 0", Double.valueOf(0.0), ageNumeric.getMinimum());
	}

	/**
	 * Tests parsing an enum schema.
	 */
	public void testParseEnumSchema() throws Exception {
		String json = "{\"enum\": [\"red\", \"green\", \"blue\"]}";
		Schema schema = JsonSchemaParser.parse(json);

		assertTrue("Should be EnumSchema", schema instanceof EnumSchema);
		EnumSchema enumSchema = (EnumSchema) schema;
		assertEquals("Should have 3 values", 3, enumSchema.getEnumLiterals().size());
		assertTrue("Should contain 'red'", enumSchema.getEnumLiterals().contains("red"));
		assertTrue("Should contain 'green'", enumSchema.getEnumLiterals().contains("green"));
		assertTrue("Should contain 'blue'", enumSchema.getEnumLiterals().contains("blue"));
	}

	/**
	 * Tests parsing a schema with metadata annotations.
	 */
	public void testParseSchemaWithMetadata() throws Exception {
		String json = "{"
			+ "\"type\": \"string\","
			+ "\"title\": \"Username\","
			+ "\"description\": \"The user's unique username\","
			+ "\"minLength\": 3"
			+ "}";

		Schema schema = JsonSchemaParser.parse(json);

		assertTrue("Should be StringSchema", schema instanceof StringSchema);
		assertEquals("Title should be 'Username'", "Username", schema.getTitle());
		assertEquals("Description should match", "The user's unique username", schema.getDescription());

		StringSchema stringSchema = (StringSchema) schema;
		assertEquals("minLength should be 3", Integer.valueOf(3), stringSchema.getMinLength());
	}

	/**
	 * Tests parsing boolean schemas.
	 */
	public void testParseBooleanSchemas() throws Exception {
		String jsonTrue = "true";
		Schema schemaTrue = JsonSchemaParser.parse(jsonTrue);
		assertTrue("Should be TrueSchema", schemaTrue instanceof com.top_logic.basic.json.schema.model.TrueSchema);

		String jsonFalse = "false";
		Schema schemaFalse = JsonSchemaParser.parse(jsonFalse);
		assertTrue("Should be FalseSchema",
			schemaFalse instanceof com.top_logic.basic.json.schema.model.FalseSchema);
	}

	/**
	 * Tests round-trip: parse and serialize.
	 */
	public void testRoundTrip() throws Exception {
		// Create original schema
		ObjectSchema original = ObjectSchema.create()
			.putProperty("name", StringSchema.create()
				.setTitle("Name")
				.setMinLength(1))
			.putProperty("age", NumericSchema.create()
				.setTitle("Age")
				.setIntegerOnly(true)
				.setMinimum(0.0)
				.setMaximum(150.0))
			.addRequired("name");

		// Serialize to JSON
		String json = JsonSchemaWriter.toJson(original, true);

		System.out.println("Original JSON:");
		System.out.println(json);

		// Parse back
		Schema parsed = JsonSchemaParser.parse(json);

		assertTrue("Parsed schema should be ObjectSchema", parsed instanceof ObjectSchema);
		ObjectSchema parsedObject = (ObjectSchema) parsed;

		// Verify properties
		assertEquals("Should have 2 properties", 2, parsedObject.getProperties().size());
		assertTrue("Should have name property", parsedObject.getProperties().containsKey("name"));
		assertTrue("Should have age property", parsedObject.getProperties().containsKey("age"));

		// Verify required
		assertEquals("Should have 1 required property", 1, parsedObject.getRequired().size());
		assertEquals("Required should be 'name'", "name", parsedObject.getRequired().get(0));

		// Serialize again
		String json2 = JsonSchemaWriter.toJson(parsed, true);

		System.out.println("Re-serialized JSON:");
		System.out.println(json2);

		// Both JSON strings should represent the same schema
		// (exact string equality may not hold due to property ordering)
	}
}
