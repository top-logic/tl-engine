/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.scripting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;

/**
 * Utility class for converting map-based schema definitions to <i>LangChain4j</i> JSON schemas.
 *
 * <p>
 * Converts TL-Script map structures into strongly-typed <i>LangChain4j</i> JSON schema objects.
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
	 * Converts a map-based schema definition to a {@link JsonSchema}.
	 *
	 * @param schemaMap
	 *        Map containing "name" and "schema" fields defining the JSON schema.
	 * @return The constructed {@link JsonSchema}.
	 * @throws IllegalArgumentException
	 *         If the schema definition is invalid.
	 */
	public static JsonSchema fromMap(Map<?, ?> schemaMap) {
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

		// Parse the root object schema with properties and required fields
		JsonObjectSchema.Builder rootBuilder = JsonObjectSchema.builder();

		// Get required properties list
		Object requiredObj = schemaPropsMap.get(FIELD_REQUIRED);
		List<String> requiredProps = new ArrayList<>();
		if (requiredObj instanceof List<?> reqList) {
			for (Object item : reqList) {
				if (item instanceof String str) {
					requiredProps.add(str);
				}
			}
		}

		// Parse and add properties
		Object propertiesObj = schemaPropsMap.get(FIELD_PROPERTIES);
		if (propertiesObj instanceof Map<?, ?> propertiesMap) {
			addPropertiesToBuilder(rootBuilder, propertiesMap);
		}

		// Add required properties if any
		if (!requiredProps.isEmpty()) {
			rootBuilder.required(requiredProps);
		}

		return JsonSchema.builder()
			.name(name)
			.rootElement(rootBuilder.build())
			.build();
	}

	/**
	 * Adds properties to a {@link dev.langchain4j.model.chat.request.json.JsonObjectSchema.Builder} using individual property methods.
	 *
	 * @param builder
	 *        The builder to add properties to.
	 * @param propertiesMap
	 *        The map of property definitions.
	 */
	private static void addPropertiesToBuilder(JsonObjectSchema.Builder builder, Map<?, ?> propertiesMap) {
		for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
			String propertyName = (String) entry.getKey();
			Object propertyValue = entry.getValue();

			if (propertyValue instanceof Map<?, ?> propertyDef) {
				addProperty(builder, propertyName, propertyDef);
			}
		}
	}

	/**
	 * Adds a property to a {@link dev.langchain4j.model.chat.request.json.JsonObjectSchema.Builder}
	 * from a type string and description.
	 *
	 * <p>
	 * This is a convenience method for adding properties when you have the type as a string (e.g.,
	 * "string", "integer", "number", "boolean", "array", "object") and an optional description.
	 * </p>
	 *
	 * @param builder
	 *        The builder to add the property to.
	 * @param propertyName
	 *        The name of the property.
	 * @param type
	 *        The JSON schema type (string, integer, number, boolean, array, object).
	 * @param description
	 *        Optional description for the property (may be {@code null}).
	 */
	public static void addProperty(JsonObjectSchema.Builder builder, String propertyName,
			String type, String description) {
		if (type == null) {
			throw new IllegalArgumentException("Property '" + propertyName + "' must have a type.");
		}

		switch (type.toLowerCase()) {
			case TYPE_STRING:
				if (description != null) {
					builder.addStringProperty(propertyName, description);
				} else {
					builder.addStringProperty(propertyName);
				}
				break;

			case TYPE_INTEGER:
				if (description != null) {
					builder.addIntegerProperty(propertyName, description);
				} else {
					builder.addIntegerProperty(propertyName);
				}
				break;

			case TYPE_NUMBER:
				if (description != null) {
					builder.addNumberProperty(propertyName, description);
				} else {
					builder.addNumberProperty(propertyName);
				}
				break;

			case TYPE_BOOLEAN:
				if (description != null) {
					builder.addBooleanProperty(propertyName, description);
				} else {
					builder.addBooleanProperty(propertyName);
				}
				break;

			case TYPE_ARRAY:
				builder.addProperty(propertyName, JsonArraySchema.builder()
					.description(description)
					.build());
				break;

			case TYPE_OBJECT:
				builder.addProperty(propertyName, JsonObjectSchema.builder()
					.description(description)
					.build());
				break;

			default:
				throw new IllegalArgumentException("Unsupported property type: " + type);
		}
	}

	/**
	 * Adds a single property to a {@link dev.langchain4j.model.chat.request.json.JsonObjectSchema.Builder} based on its definition.
	 *
	 * @param builder
	 *        The builder to add the property to.
	 * @param propertyName
	 *        The name of the property.
	 * @param propertyDef
	 *        The property definition map containing "type", "description", etc.
	 */
	private static void addProperty(JsonObjectSchema.Builder builder, String propertyName,
			Map<?, ?> propertyDef) {
		String type = (String) propertyDef.get(FIELD_TYPE);

		if (type == null) {
			throw new IllegalArgumentException("Property '" + propertyName + "' must have a '" + FIELD_TYPE + "' field.");
		}

		String description = (String) propertyDef.get(FIELD_DESCRIPTION);

		// Special handling for enum strings
		if (TYPE_STRING.equals(type.toLowerCase())) {
			Object enumValues = propertyDef.get(FIELD_ENUM);
			if (enumValues instanceof List<?> enumList) {
				List<String> enumStrings = extractStringList(enumList);
				if (description != null) {
					builder.addEnumProperty(propertyName, enumStrings, description);
				} else {
					builder.addEnumProperty(propertyName, enumStrings);
				}
				return;
			}
		}

		// Special handling for complex types (array and object) with nested definitions
		if (TYPE_ARRAY.equals(type.toLowerCase())) {
			builder.addProperty(propertyName, buildArraySchema(propertyDef));
			return;
		}

		if (TYPE_OBJECT.equals(type.toLowerCase())) {
			Object propertiesObj = propertyDef.get(FIELD_PROPERTIES);
			if (propertiesObj != null) {
				// Complex object with nested properties - use full builder
				builder.addProperty(propertyName, buildObjectSchema(propertyDef));
				return;
			}
		}

		// For simple types, delegate to the public method
		addProperty(builder, propertyName, type, description);
	}

	/**
	 * Parses a schema element definition into a {@link JsonSchemaElement}.
	 *
	 * @param elementDef
	 *        The element definition map.
	 * @return The parsed schema element.
	 */
	private static JsonSchemaElement parseSchemaElement(Map<?, ?> elementDef) {
		String type = (String) elementDef.get(FIELD_TYPE);

		if (type == null) {
			throw new IllegalArgumentException("Schema element must have a '" + FIELD_TYPE + "' field.");
		}

		switch (type.toLowerCase()) {
			case TYPE_STRING:
				return buildStringSchema(elementDef);

			case TYPE_INTEGER:
				return buildIntegerSchema(elementDef);

			case TYPE_NUMBER:
				return buildNumberSchema(elementDef);

			case TYPE_BOOLEAN:
				return buildBooleanSchema(elementDef);

			case TYPE_ARRAY:
				return buildArraySchema(elementDef);

			case TYPE_OBJECT:
				return buildObjectSchema(elementDef);

			default:
				throw new IllegalArgumentException("Unsupported element type: " + type);
		}
	}

	/**
	 * Builds a string schema from a definition map.
	 */
	private static JsonStringSchema buildStringSchema(Map<?, ?> def) {
		JsonStringSchema.Builder builder = JsonStringSchema.builder();
		String description = (String) def.get(FIELD_DESCRIPTION);
		if (description != null) {
			builder.description(description);
		}
		return builder.build();
	}

	/**
	 * Builds an integer schema from a definition map.
	 */
	private static JsonIntegerSchema buildIntegerSchema(Map<?, ?> def) {
		JsonIntegerSchema.Builder builder = JsonIntegerSchema.builder();
		String description = (String) def.get(FIELD_DESCRIPTION);
		if (description != null) {
			builder.description(description);
		}
		return builder.build();
	}

	/**
	 * Builds a number schema from a definition map.
	 */
	private static JsonNumberSchema buildNumberSchema(Map<?, ?> def) {
		JsonNumberSchema.Builder builder = JsonNumberSchema.builder();
		String description = (String) def.get(FIELD_DESCRIPTION);
		if (description != null) {
			builder.description(description);
		}
		return builder.build();
	}

	/**
	 * Builds a boolean schema from a definition map.
	 */
	private static JsonBooleanSchema buildBooleanSchema(Map<?, ?> def) {
		JsonBooleanSchema.Builder builder = JsonBooleanSchema.builder();
		String description = (String) def.get(FIELD_DESCRIPTION);
		if (description != null) {
			builder.description(description);
		}
		return builder.build();
	}

	/**
	 * Builds an array schema from a definition map.
	 */
	private static JsonArraySchema buildArraySchema(Map<?, ?> def) {
		JsonArraySchema.Builder builder = JsonArraySchema.builder();
		String description = (String) def.get(FIELD_DESCRIPTION);
		if (description != null) {
			builder.description(description);
		}

		Object itemsObj = def.get(FIELD_ITEMS);
		if (itemsObj instanceof Map<?, ?> itemsDef) {
			builder.items(parseSchemaElement(itemsDef));
		}

		return builder.build();
	}

	/**
	 * Builds an object schema from a definition map.
	 */
	private static JsonObjectSchema buildObjectSchema(Map<?, ?> def) {
		JsonObjectSchema.Builder builder = JsonObjectSchema.builder();
		String description = (String) def.get(FIELD_DESCRIPTION);
		if (description != null) {
			builder.description(description);
		}

		// Add properties
		Object propertiesObj = def.get(FIELD_PROPERTIES);
		if (propertiesObj instanceof Map<?, ?> propertiesMap) {
			addPropertiesToBuilder(builder, propertiesMap);
		}

		// Add required properties
		Object requiredObj = def.get(FIELD_REQUIRED);
		if (requiredObj instanceof List<?> reqList) {
			List<String> requiredProps = extractStringList(reqList);
			if (!requiredProps.isEmpty()) {
				builder.required(requiredProps);
			}
		}

		return builder.build();
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
