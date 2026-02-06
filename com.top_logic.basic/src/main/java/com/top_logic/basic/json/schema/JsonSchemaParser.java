/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.schema;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.json.schema.model.AllOfSchema;
import com.top_logic.basic.json.schema.model.AnyOfSchema;
import com.top_logic.basic.json.schema.model.ArraySchema;
import com.top_logic.basic.json.schema.model.BooleanSchema;
import com.top_logic.basic.json.schema.model.ConditionalSchema;
import com.top_logic.basic.json.schema.model.ConstSchema;
import com.top_logic.basic.json.schema.model.DynamicRefSchema;
import com.top_logic.basic.json.schema.model.EnumSchema;
import com.top_logic.basic.json.schema.model.FalseSchema;
import com.top_logic.basic.json.schema.model.JsonSchemaDocument;
import com.top_logic.basic.json.schema.model.NotSchema;
import com.top_logic.basic.json.schema.model.NullSchema;
import com.top_logic.basic.json.schema.model.NumericSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.OneOfSchema;
import com.top_logic.basic.json.schema.model.RefSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.json.schema.model.StringArray;
import com.top_logic.basic.json.schema.model.StringSchema;
import com.top_logic.basic.json.schema.model.TrueSchema;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonToken;

/**
 * Parser for JSON Schema documents in JSON format.
 *
 * <p>
 * Parses JSON Schema 2020-12 format and builds a {@link JsonSchemaDocument} object model.
 * </p>
 *
 * <h3>Usage:</h3>
 * <pre>
 * String json = "{ \"type\": \"string\", \"minLength\": 1 }";
 * Schema schema = JsonSchemaParser.parse(json);
 * </pre>
 */
public class JsonSchemaParser {

	/**
	 * Parses a JSON Schema document from a JSON string.
	 *
	 * @param json
	 *        The JSON string to parse.
	 * @return The parsed schema.
	 * @throws IOException
	 *         If parsing fails.
	 */
	public static Schema parse(String json) throws IOException {
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			return parseSchemaValue(reader);
		}
	}

	/**
	 * Parses a schema object from the current position in the JSON reader.
	 */
	private static Schema parseSchema(JsonReader reader) throws IOException {
		// Check for boolean schemas first
		if (reader.peek() == JsonToken.BOOLEAN) {
			boolean value = reader.nextBoolean();
			return value ? TrueSchema.create() : FalseSchema.create();
		}

		if (reader.peek() != JsonToken.BEGIN_OBJECT) {
			throw new IOException("Expected object or boolean, got: " + reader.peek());
		}

		reader.beginObject();

		// Peek at properties to determine schema type
		Map<String, Object> properties = new java.util.LinkedHashMap<>();
		String type = null;
		boolean hasRef = false;
		boolean hasDynamicRef = false;
		boolean hasEnum = false;
		boolean hasConst = false;
		boolean hasAllOf = false;
		boolean hasAnyOf = false;
		boolean hasOneOf = false;
		boolean hasNot = false;
		boolean hasIf = false;

		// First pass: read all properties to determine schema type
		while (reader.hasNext()) {
			String name = reader.nextName();

			switch (name) {
				case "type":
					type = reader.nextString();
					properties.put(name, type);
					break;
				case "$ref":
					hasRef = true;
					properties.put(name, reader.nextString());
					break;
				case "$dynamicRef":
					hasDynamicRef = true;
					properties.put(name, reader.nextString());
					break;
				case "enum":
					hasEnum = true;
					properties.put(name, readStringArray(reader));
					break;
				case "const":
					hasConst = true;
					properties.put(name, reader.nextString());
					break;
				case "allOf":
					hasAllOf = true;
					properties.put(name, readSchemaArray(reader));
					break;
				case "anyOf":
					hasAnyOf = true;
					properties.put(name, readSchemaArray(reader));
					break;
				case "oneOf":
					hasOneOf = true;
					properties.put(name, readSchemaArray(reader));
					break;
				case "not":
					hasNot = true;
					properties.put(name, parseSchemaValue(reader));
					break;
				case "if":
					hasIf = true;
					properties.put(name, parseSchemaValue(reader));
					break;
				case "then":
				case "else":
				case "contentSchema":
				case "items":
				case "contains":
				case "additionalProperties":
				case "propertyNames":
					// Schema-valued properties
					properties.put(name, parseSchemaValue(reader));
					break;
				case "properties":
				case "patternProperties":
				case "dependentSchemas":
				case "$defs":
					// Maps of schemas
					properties.put(name, readSchemaMap(reader));
					break;
				case "prefixItems":
					// Arrays of schemas
					properties.put(name, readSchemaArray(reader));
					break;
				case "dependentRequired":
					// Map of string arrays
					properties.put(name, readStringArrayMap(reader));
					break;
				default:
					// Store other properties for later processing
					properties.put(name, readValue(reader));
					break;
			}
		}

		reader.endObject();

		// Determine schema type and build appropriate schema object
		Schema schema;

		if (hasRef) {
			schema = buildRefSchema(properties);
		} else if (hasDynamicRef) {
			schema = buildDynamicRefSchema(properties);
		} else if (hasEnum) {
			schema = buildEnumSchema(properties);
		} else if (hasConst) {
			schema = buildConstSchema(properties);
		} else if (hasAllOf) {
			schema = buildAllOfSchema(properties);
		} else if (hasAnyOf) {
			schema = buildAnyOfSchema(properties);
		} else if (hasOneOf) {
			schema = buildOneOfSchema(properties);
		} else if (hasNot) {
			schema = buildNotSchema(properties);
		} else if (hasIf) {
			schema = buildConditionalSchema(properties);
		} else if (type != null) {
			schema = buildTypedSchema(type, properties);
		} else {
			// Default to object schema if no type specified
			schema = buildObjectSchema(properties);
		}

		// Apply base properties
		applyBaseProperties(schema, properties);

		return schema;
	}

	/**
	 * Parses a schema value (can be boolean or object).
	 */
	private static Schema parseSchemaValue(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.BOOLEAN) {
			boolean value = reader.nextBoolean();
			return value ? TrueSchema.create() : FalseSchema.create();
		} else {
			return parseSchema(reader);
		}
	}

	/**
	 * Reads a generic JSON value.
	 */
	private static Object readValue(JsonReader reader) throws IOException {
		switch (reader.peek()) {
			case STRING:
				return reader.nextString();
			case NUMBER:
				return reader.nextDouble();
			case BOOLEAN:
				return reader.nextBoolean();
			case NULL:
				reader.nextNull();
				return null;
			case BEGIN_ARRAY:
				return readArray(reader);
			case BEGIN_OBJECT:
				return readObject(reader);
			default:
				throw new IOException("Unexpected token: " + reader.peek());
		}
	}

	/**
	 * Reads a JSON array of arbitrary values.
	 */
	private static java.util.List<Object> readArray(JsonReader reader) throws IOException {
		java.util.List<Object> result = new java.util.ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			result.add(readValue(reader));
		}
		reader.endArray();
		return result;
	}

	/**
	 * Reads a JSON object as a map.
	 */
	private static Map<String, Object> readObject(JsonReader reader) throws IOException {
		Map<String, Object> result = new java.util.LinkedHashMap<>();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			result.put(name, readValue(reader));
		}
		reader.endObject();
		return result;
	}

	/**
	 * Reads an array of strings.
	 */
	private static java.util.List<String> readStringArray(JsonReader reader) throws IOException {
		java.util.List<String> result = new java.util.ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			result.add(reader.nextString());
		}
		reader.endArray();
		return result;
	}

	/**
	 * Reads an array of schemas.
	 */
	private static java.util.List<Schema> readSchemaArray(JsonReader reader) throws IOException {
		java.util.List<Schema> result = new java.util.ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			result.add(parseSchemaValue(reader));
		}
		reader.endArray();
		return result;
	}

	/**
	 * Reads a map of schemas.
	 */
	private static Map<String, Schema> readSchemaMap(JsonReader reader) throws IOException {
		Map<String, Schema> result = new java.util.LinkedHashMap<>();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			result.put(name, parseSchemaValue(reader));
		}
		reader.endObject();
		return result;
	}

	/**
	 * Reads a map of string arrays.
	 */
	private static Map<String, java.util.List<String>> readStringArrayMap(JsonReader reader) throws IOException {
		Map<String, java.util.List<String>> result = new java.util.LinkedHashMap<>();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			result.put(name, readStringArray(reader));
		}
		reader.endObject();
		return result;
	}

	/**
	 * Builds a typed schema based on the "type" keyword.
	 */
	private static Schema buildTypedSchema(String type, Map<String, Object> properties) {
		switch (type) {
			case "null":
				return NullSchema.create();
			case "boolean":
				return BooleanSchema.create();
			case "string":
				return buildStringSchema(properties);
			case "integer":
				return buildNumericSchema(properties, true);
			case "number":
				return buildNumericSchema(properties, false);
			case "array":
				return buildArraySchema(properties);
			case "object":
				return buildObjectSchema(properties);
			default:
				throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	/**
	 * Builds a string schema.
	 */
	private static StringSchema buildStringSchema(Map<String, Object> properties) {
		StringSchema schema = StringSchema.create();

		if (properties.containsKey("minLength")) {
			schema.setMinLength(((Number) properties.get("minLength")).intValue());
		}
		if (properties.containsKey("maxLength")) {
			schema.setMaxLength(((Number) properties.get("maxLength")).intValue());
		}
		if (properties.containsKey("pattern")) {
			schema.setPattern((String) properties.get("pattern"));
		}
		if (properties.containsKey("format")) {
			schema.setFormat((String) properties.get("format"));
		}
		if (properties.containsKey("contentEncoding")) {
			schema.setContentEncoding((String) properties.get("contentEncoding"));
		}
		if (properties.containsKey("contentMediaType")) {
			schema.setContentMediaType((String) properties.get("contentMediaType"));
		}
		if (properties.containsKey("contentSchema")) {
			schema.setContentSchema((Schema) properties.get("contentSchema"));
		}

		return schema;
	}

	/**
	 * Builds a numeric schema.
	 */
	private static NumericSchema buildNumericSchema(Map<String, Object> properties, boolean integerOnly) {
		NumericSchema schema = NumericSchema.create().setIntegerOnly(integerOnly);

		if (properties.containsKey("multipleOf")) {
			schema.setMultipleOf(((Number) properties.get("multipleOf")).doubleValue());
		}
		if (properties.containsKey("minimum")) {
			schema.setMinimum(((Number) properties.get("minimum")).doubleValue());
		}
		if (properties.containsKey("maximum")) {
			schema.setMaximum(((Number) properties.get("maximum")).doubleValue());
		}
		if (properties.containsKey("exclusiveMinimum")) {
			schema.setExclusiveMinimum(((Number) properties.get("exclusiveMinimum")).doubleValue());
		}
		if (properties.containsKey("exclusiveMaximum")) {
			schema.setExclusiveMaximum(((Number) properties.get("exclusiveMaximum")).doubleValue());
		}

		return schema;
	}

	/**
	 * Builds an array schema.
	 */
	private static ArraySchema buildArraySchema(Map<String, Object> properties) {
		ArraySchema schema = ArraySchema.create();

		if (properties.containsKey("minItems")) {
			schema.setMinItems(((Number) properties.get("minItems")).intValue());
		}
		if (properties.containsKey("maxItems")) {
			schema.setMaxItems(((Number) properties.get("maxItems")).intValue());
		}
		if (properties.containsKey("uniqueItems")) {
			schema.setUniqueItems((Boolean) properties.get("uniqueItems"));
		}
		if (properties.containsKey("items")) {
			schema.setItems((Schema) properties.get("items"));
		}
		if (properties.containsKey("prefixItems")) {
			@SuppressWarnings("unchecked")
			java.util.List<Schema> prefixItems = (java.util.List<Schema>) properties.get("prefixItems");
			for (Schema item : prefixItems) {
				schema.addPrefixItem(item);
			}
		}
		if (properties.containsKey("contains")) {
			schema.setContains((Schema) properties.get("contains"));
		}
		if (properties.containsKey("minContains")) {
			schema.setMinContains(((Number) properties.get("minContains")).intValue());
		}
		if (properties.containsKey("maxContains")) {
			schema.setMaxContains(((Number) properties.get("maxContains")).intValue());
		}

		return schema;
	}

	/**
	 * Builds an object schema.
	 */
	private static ObjectSchema buildObjectSchema(Map<String, Object> properties) {
		ObjectSchema schema = ObjectSchema.create();

		if (properties.containsKey("minProperties")) {
			schema.setMinProperties(((Number) properties.get("minProperties")).intValue());
		}
		if (properties.containsKey("maxProperties")) {
			schema.setMaxProperties(((Number) properties.get("maxProperties")).intValue());
		}
		if (properties.containsKey("required")) {
			@SuppressWarnings("unchecked")
			java.util.List<String> required = (java.util.List<String>) properties.get("required");
			for (String prop : required) {
				schema.addRequired(prop);
			}
		}
		if (properties.containsKey("properties")) {
			@SuppressWarnings("unchecked")
			Map<String, Schema> props = (Map<String, Schema>) properties.get("properties");
			for (Map.Entry<String, Schema> entry : props.entrySet()) {
				schema.putProperty(entry.getKey(), entry.getValue());
			}
		}
		if (properties.containsKey("patternProperties")) {
			@SuppressWarnings("unchecked")
			Map<String, Schema> patternProps = (Map<String, Schema>) properties.get("patternProperties");
			for (Map.Entry<String, Schema> entry : patternProps.entrySet()) {
				schema.putPatternProperty(entry.getKey(), entry.getValue());
			}
		}
		if (properties.containsKey("additionalProperties")) {
			schema.setAdditionalProperties((Schema) properties.get("additionalProperties"));
		}
		if (properties.containsKey("propertyNames")) {
			schema.setPropertyNames((Schema) properties.get("propertyNames"));
		}
		if (properties.containsKey("dependentRequired")) {
			@SuppressWarnings("unchecked")
			Map<String, java.util.List<String>> depReq = (Map<String, java.util.List<String>>) properties.get("dependentRequired");
			for (Map.Entry<String, java.util.List<String>> entry : depReq.entrySet()) {
				StringArray arr = StringArray.create();
				for (String val : entry.getValue()) {
					arr.addValue(val);
				}
				schema.putDependentRequired(entry.getKey(), arr);
			}
		}
		if (properties.containsKey("dependentSchemas")) {
			@SuppressWarnings("unchecked")
			Map<String, Schema> depSchemas = (Map<String, Schema>) properties.get("dependentSchemas");
			for (Map.Entry<String, Schema> entry : depSchemas.entrySet()) {
				schema.putDependentSchema(entry.getKey(), entry.getValue());
			}
		}

		return schema;
	}

	/**
	 * Builds a reference schema.
	 */
	private static RefSchema buildRefSchema(Map<String, Object> properties) {
		RefSchema schema = RefSchema.create();
		schema.setRef((String) properties.get("$ref"));
		return schema;
	}

	/**
	 * Builds a dynamic reference schema.
	 */
	private static DynamicRefSchema buildDynamicRefSchema(Map<String, Object> properties) {
		DynamicRefSchema schema = DynamicRefSchema.create();
		schema.setDynamicRef((String) properties.get("$dynamicRef"));
		return schema;
	}

	/**
	 * Builds an enum schema.
	 */
	private static EnumSchema buildEnumSchema(Map<String, Object> properties) {
		EnumSchema schema = EnumSchema.create();
		@SuppressWarnings("unchecked")
		java.util.List<String> enumValues = (java.util.List<String>) properties.get("enum");
		for (String value : enumValues) {
			schema.addEnumLiteral(value);
		}
		return schema;
	}

	/**
	 * Builds a const schema.
	 */
	private static ConstSchema buildConstSchema(Map<String, Object> properties) {
		ConstSchema schema = ConstSchema.create();
		schema.setConstValue((String) properties.get("const"));
		return schema;
	}

	/**
	 * Builds an allOf schema.
	 */
	private static AllOfSchema buildAllOfSchema(Map<String, Object> properties) {
		AllOfSchema schema = AllOfSchema.create();
		@SuppressWarnings("unchecked")
		java.util.List<Schema> schemas = (java.util.List<Schema>) properties.get("allOf");
		for (Schema s : schemas) {
			schema.addAllOf(s);
		}
		return schema;
	}

	/**
	 * Builds an anyOf schema.
	 */
	private static AnyOfSchema buildAnyOfSchema(Map<String, Object> properties) {
		AnyOfSchema schema = AnyOfSchema.create();
		@SuppressWarnings("unchecked")
		java.util.List<Schema> schemas = (java.util.List<Schema>) properties.get("anyOf");
		for (Schema s : schemas) {
			schema.addAnyOf(s);
		}
		return schema;
	}

	/**
	 * Builds a oneOf schema.
	 */
	private static OneOfSchema buildOneOfSchema(Map<String, Object> properties) {
		OneOfSchema schema = OneOfSchema.create();
		@SuppressWarnings("unchecked")
		java.util.List<Schema> schemas = (java.util.List<Schema>) properties.get("oneOf");
		for (Schema s : schemas) {
			schema.addOneOf(s);
		}
		return schema;
	}

	/**
	 * Builds a not schema.
	 */
	private static NotSchema buildNotSchema(Map<String, Object> properties) {
		NotSchema schema = NotSchema.create();
		schema.setNot((Schema) properties.get("not"));
		return schema;
	}

	/**
	 * Builds a conditional schema.
	 */
	private static ConditionalSchema buildConditionalSchema(Map<String, Object> properties) {
		ConditionalSchema schema = ConditionalSchema.create();
		if (properties.containsKey("if")) {
			schema.setCondition((Schema) properties.get("if"));
		}
		if (properties.containsKey("then")) {
			schema.setThenSchema((Schema) properties.get("then"));
		}
		if (properties.containsKey("else")) {
			schema.setElseSchema((Schema) properties.get("else"));
		}
		return schema;
	}

	/**
	 * Applies base schema properties (common to all schema types).
	 */
	private static void applyBaseProperties(Schema schema, Map<String, Object> properties) {
		if (properties.containsKey("$id")) {
			schema.setId((String) properties.get("$id"));
		}
		if (properties.containsKey("$anchor")) {
			schema.setAnchor((String) properties.get("$anchor"));
		}
		if (properties.containsKey("$dynamicAnchor")) {
			schema.setDynamicAnchor((String) properties.get("$dynamicAnchor"));
		}
		if (properties.containsKey("$comment")) {
			schema.setComment((String) properties.get("$comment"));
		}
		if (properties.containsKey("$defs")) {
			@SuppressWarnings("unchecked")
			Map<String, Schema> defs = (Map<String, Schema>) properties.get("$defs");
			for (Map.Entry<String, Schema> entry : defs.entrySet()) {
				schema.putDefinition(entry.getKey(), entry.getValue());
			}
		}
		if (properties.containsKey("title")) {
			schema.setTitle((String) properties.get("title"));
		}
		if (properties.containsKey("description")) {
			schema.setDescription((String) properties.get("description"));
		}
		if (properties.containsKey("default")) {
			schema.setDefaultValue((String) properties.get("default"));
		}
		if (properties.containsKey("deprecated")) {
			schema.setDeprecated((Boolean) properties.get("deprecated"));
		}
		if (properties.containsKey("readOnly")) {
			schema.setReadOnly((Boolean) properties.get("readOnly"));
		}
		if (properties.containsKey("writeOnly")) {
			schema.setWriteOnly((Boolean) properties.get("writeOnly"));
		}
		if (properties.containsKey("examples")) {
			@SuppressWarnings("unchecked")
			java.util.List<String> examples = (java.util.List<String>) properties.get("examples");
			for (String example : examples) {
				schema.addExample(example);
			}
		}
	}
}
