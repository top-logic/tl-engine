/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.scripting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Utility class for converting map-based schema definitions to JSON schema strings.
 *
 * <p>
 * Converts TL-Script map structures into JSON schema strings using msgbuf {@link JsonWriter}.
 * Supports all JSON schema primitive types (string, integer, number, boolean), complex types
 * (array, object), enum values, and required property specifications.
 * </p>
 */
public class JsonSchemaConverter {

	/** Schema field name for the schema name. */
	private static final String FIELD_NAME = "name";

	/** Schema field name for the schema definition. */
	private static final String FIELD_SCHEMA = "schema";

	/** Schema field name for property definitions. */
	private static final String FIELD_PROPERTIES = "properties";

	/** Schema field name for required properties list. */
	private static final String FIELD_REQUIRED = "required";

	/** Schema field name for property type. */
	private static final String FIELD_TYPE = "type";

	/** Schema field name for property description. */
	private static final String FIELD_DESCRIPTION = "description";

	/** Schema field name for enum values. */
	private static final String FIELD_ENUM = "enum";

	/** Schema field name for array item type. */
	private static final String FIELD_ITEMS = "items";

	/** Type name for string properties. */
	private static final String TYPE_STRING = "string";

	/** Type name for integer properties. */
	private static final String TYPE_INTEGER = "integer";

	/** Type name for number properties. */
	private static final String TYPE_NUMBER = "number";

	/** Type name for boolean properties. */
	private static final String TYPE_BOOLEAN = "boolean";

	/** Type name for array properties. */
	private static final String TYPE_ARRAY = "array";

	/** Type name for object properties. */
	private static final String TYPE_OBJECT = "object";

	/**
	 * Converts a map-based schema definition to a JSON schema string.
	 *
	 * <p>
	 * The map must contain a "name" field and a "schema" field. The "schema" field must be a map
	 * with "properties" and optionally "required" fields.
	 * </p>
	 *
	 * @param schemaMap
	 *        Map containing "name" and "schema" fields defining the JSON schema.
	 * @return The JSON schema as a string.
	 * @throws IllegalArgumentException
	 *         If the schema definition is invalid.
	 */
	public static String fromMap(Map<?, ?> schemaMap) {
		String name = (String) schemaMap.get(FIELD_NAME);
		if (name == null) {
			throw new IllegalArgumentException("JSON schema must contain a '" + FIELD_NAME + "' field.");
		}

		Object schema = schemaMap.get(FIELD_SCHEMA);
		if (schema == null) {
			throw new IllegalArgumentException("JSON schema must contain a '" + FIELD_SCHEMA + "' field.");
		}

		if (!(schema instanceof Map<?, ?> schemaPropsMap)) {
			throw new IllegalArgumentException("Schema must be a Map.");
		}

		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				writeObjectSchema(json, schemaPropsMap);
			}
			return buffer.toString();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to generate JSON Schema", e);
		}
	}

	/**
	 * Builds a JSON schema string from a map of parameter definitions.
	 *
	 * <p>
	 * Each entry in the map defines a parameter with "type" and optionally "description" fields.
	 * All parameters are treated as required.
	 * </p>
	 *
	 * @param parameters
	 *        Map from parameter name to parameter definition map.
	 * @return The JSON schema as a string.
	 */
	public static String buildParameterSchema(Map<String, ? extends Map<?, ?>> parameters) {
		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				json.beginObject();
				json.name("type").value("object");

				json.name("properties");
				json.beginObject();
				for (Map.Entry<String, ? extends Map<?, ?>> entry : parameters.entrySet()) {
					json.name(entry.getKey());
					writeSchemaElement(json, entry.getValue());
				}
				json.endObject();

				json.name("required");
				json.beginArray();
				for (String name : parameters.keySet()) {
					json.value(name);
				}
				json.endArray();

				json.endObject();
			}
			return buffer.toString();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to build parameter schema JSON", e);
		}
	}

	/**
	 * Writes a JSON object schema from a map definition.
	 */
	private static void writeObjectSchema(JsonWriter json, Map<?, ?> schemaDef) throws IOException {
		json.beginObject();
		json.name("type").value("object");

		Object propertiesObj = schemaDef.get(FIELD_PROPERTIES);
		if (propertiesObj instanceof Map<?, ?> propertiesMap) {
			json.name("properties");
			json.beginObject();
			for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
				String propertyName = (String) entry.getKey();
				json.name(propertyName);
				if (entry.getValue() instanceof Map<?, ?> propertyDef) {
					writeSchemaElement(json, propertyDef);
				}
			}
			json.endObject();
		}

		Object requiredObj = schemaDef.get(FIELD_REQUIRED);
		if (requiredObj instanceof List<?> reqList) {
			List<String> requiredProps = extractStringList(reqList);
			if (!requiredProps.isEmpty()) {
				json.name("required");
				json.beginArray();
				for (String name : requiredProps) {
					json.value(name);
				}
				json.endArray();
			}
		}

		String description = (String) schemaDef.get(FIELD_DESCRIPTION);
		if (description != null) {
			json.name("description").value(description);
		}

		json.endObject();
	}

	/**
	 * Writes a single schema element (property definition) based on its type.
	 */
	private static void writeSchemaElement(JsonWriter json, Map<?, ?> elementDef) throws IOException {
		String type = (String) elementDef.get(FIELD_TYPE);

		if (type == null) {
			throw new IllegalArgumentException("Schema element must have a '" + FIELD_TYPE + "' field.");
		}

		json.beginObject();

		switch (type.toLowerCase()) {
			case TYPE_STRING:
				json.name("type").value("string");
				writeEnumValues(json, elementDef);
				break;

			case TYPE_INTEGER:
				json.name("type").value("integer");
				break;

			case TYPE_NUMBER:
				json.name("type").value("number");
				break;

			case TYPE_BOOLEAN:
				json.name("type").value("boolean");
				break;

			case TYPE_ARRAY:
				json.name("type").value("array");
				writeArrayItems(json, elementDef);
				break;

			case TYPE_OBJECT:
				json.name("type").value("object");
				writeNestedProperties(json, elementDef);
				writeNestedRequired(json, elementDef);
				break;

			default:
				throw new IllegalArgumentException("Unsupported element type: " + type);
		}

		String description = (String) elementDef.get(FIELD_DESCRIPTION);
		if (description != null) {
			json.name("description").value(description);
		}

		json.endObject();
	}

	/**
	 * Writes enum values if present on a string schema element.
	 */
	private static void writeEnumValues(JsonWriter json, Map<?, ?> elementDef) throws IOException {
		Object enumValues = elementDef.get(FIELD_ENUM);
		if (enumValues instanceof List<?> enumList) {
			List<String> enumStrings = extractStringList(enumList);
			if (!enumStrings.isEmpty()) {
				json.name("enum");
				json.beginArray();
				for (String value : enumStrings) {
					json.value(value);
				}
				json.endArray();
			}
		}
	}

	/**
	 * Writes array item schema if present.
	 */
	private static void writeArrayItems(JsonWriter json, Map<?, ?> elementDef) throws IOException {
		Object itemsObj = elementDef.get(FIELD_ITEMS);
		if (itemsObj instanceof Map<?, ?> itemsDef) {
			json.name("items");
			writeSchemaElement(json, itemsDef);
		}
	}

	/**
	 * Writes nested properties for an object schema element.
	 */
	private static void writeNestedProperties(JsonWriter json, Map<?, ?> elementDef) throws IOException {
		Object propertiesObj = elementDef.get(FIELD_PROPERTIES);
		if (propertiesObj instanceof Map<?, ?> propertiesMap) {
			json.name("properties");
			json.beginObject();
			for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
				json.name((String) entry.getKey());
				if (entry.getValue() instanceof Map<?, ?> propertyDef) {
					writeSchemaElement(json, propertyDef);
				}
			}
			json.endObject();
		}
	}

	/**
	 * Writes nested required fields for an object schema element.
	 */
	private static void writeNestedRequired(JsonWriter json, Map<?, ?> elementDef) throws IOException {
		Object requiredObj = elementDef.get(FIELD_REQUIRED);
		if (requiredObj instanceof List<?> reqList) {
			List<String> requiredProps = extractStringList(reqList);
			if (!requiredProps.isEmpty()) {
				json.name("required");
				json.beginArray();
				for (String name : requiredProps) {
					json.value(name);
				}
				json.endArray();
			}
		}
	}

	/**
	 * Extracts a list of strings from a generic list.
	 */
	private static List<String> extractStringList(List<?> list) {
		List<String> result = new ArrayList<>();
		for (Object item : list) {
			if (item instanceof String str) {
				result.add(str);
			}
		}
		return result;
	}
}
