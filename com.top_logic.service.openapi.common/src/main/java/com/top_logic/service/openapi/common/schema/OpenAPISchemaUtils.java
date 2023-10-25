/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

import static com.top_logic.service.openapi.common.schema.OpenAPISchemaConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.common.document.ReferencingObject;

/**
 * Utilities to work with <i>OpenAPI</i> schemas.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenAPISchemaUtils {

	/**
	 * Parses the given {@link String} to a {@link Schema} instance
	 *
	 * @param schema
	 *        The {@link Schema} to parse.
	 * @param referencedSchemas
	 *        Function that delivers a referenced the {@link Schema}. A referenced schema is the
	 *        string value of an {@link ReferencingObject#$REF} attribute.
	 * @throws ParseException
	 *         When the schema is not valid {@link JSON}.
	 */
	public static Schema parseSchema(String schema, Function<String, Schema> referencedSchemas) throws ParseException {
		return createSchema(schemaAsMap(JSON.fromString(schema)), referencedSchemas);
	}

	private static Schema createSchema(Map<?, ?> schemaAsMap, Function<String, Schema> referencedSchemas)
			throws ParseException {
		String referencedSchema = stringValue(schemaAsMap, ReferencingObject.$REF);
		if (!referencedSchema.isEmpty()) {
			return referencedSchemas.apply(referencedSchema);
		}

		String schemaType = stringValue(schemaAsMap, SCHEMA_PROPERTY_TYPE);
		Schema schema;
		switch (schemaType) {
			case SCHEMA_TYPE_ARRAY: {
				ArraySchema arraySchema = TypedConfiguration.newConfigItem(ArraySchema.class);
				Map<?, ?> itemsSchema = mapValue(schemaAsMap, SCHEMA_PROPERTY_ITEMS);
				arraySchema.setItems(createSchema(itemsSchema, referencedSchemas));
				schema = arraySchema;
				break;
			}
			case SCHEMA_TYPE_BOOLEAN: {
				PrimitiveSchema primitiveSchema = TypedConfiguration.newConfigItem(PrimitiveSchema.class);
				primitiveSchema.setType(SCHEMA_TYPE_BOOLEAN);
				schema = primitiveSchema;
				break;
			}
			case SCHEMA_TYPE_INTEGER: {
				PrimitiveSchema primitiveSchema = TypedConfiguration.newConfigItem(PrimitiveSchema.class);
				primitiveSchema.setType(SCHEMA_TYPE_INTEGER);
				String format = stringValue(schemaAsMap, SCHEMA_PROPERTY_FORMAT);
				switch (format) {
					case SCHEMA_FORMAT_INT64:
					case SCHEMA_FORMAT_INT32: {
						primitiveSchema.setFormat(format);
						break;
					}
					case StringServices.EMPTY_STRING: {
						// No format given
						break;
					}
					default: {
						primitiveSchema.setFormat(format);
						break;
					}
				}
				schema = primitiveSchema;
				break;
			}
			case SCHEMA_TYPE_NUMBER: {
				PrimitiveSchema primitiveSchema = TypedConfiguration.newConfigItem(PrimitiveSchema.class);
				primitiveSchema.setType(SCHEMA_TYPE_NUMBER);
				String format = stringValue(schemaAsMap, SCHEMA_PROPERTY_FORMAT);
				switch (format) {
					case SCHEMA_FORMAT_DOUBLE:
					case SCHEMA_FORMAT_FLOAT: {
						primitiveSchema.setFormat(format);
						break;
					}
					case StringServices.EMPTY_STRING: {
						// No format given
						break;
					}
					default: {
						primitiveSchema.setFormat(format);
						break;
					}
				}
				schema = primitiveSchema;
				break;
			}
			case SCHEMA_TYPE_OBJECT: {
				ObjectSchema objectSchema = TypedConfiguration.newConfigItem(ObjectSchema.class);
				Set<?> required = CollectionUtil.toSet(listValue(schemaAsMap, SCHEMA_PROPERTY_REQUIRED));
				for (Entry<?, ?> entry : mapValue(schemaAsMap, SCHEMA_PROPERTY_PROPERTIES).entrySet()) {
					String propertyName = (String) entry.getKey();
					ObjectSchemaProperty property = TypedConfiguration.newConfigItem(ObjectSchemaProperty.class);
					property.setName(propertyName);
					Schema propertySchema = createSchema(schemaAsMap(entry.getValue()), referencedSchemas);
					property.setSchema(propertySchema);
					property.setRequired(required.contains(propertyName));
					objectSchema.getProperties().add(property);
				}
				schema = objectSchema;
				break;
			}
			case SCHEMA_TYPE_STRING: {
				PrimitiveSchema primitiveSchema = TypedConfiguration.newConfigItem(PrimitiveSchema.class);
				primitiveSchema.setType(SCHEMA_TYPE_STRING);
				String format = stringValue(schemaAsMap, SCHEMA_PROPERTY_FORMAT);
				switch (format) {
					case SCHEMA_FORMAT_DATE:
					case SCHEMA_FORMAT_DATE_TIME: {
						primitiveSchema.setFormat(format);
						break;
					}
					case StringServices.EMPTY_STRING: {
						// No format given
						break;
					}
					default: {
						primitiveSchema.setFormat(format);
						break;
					}
				}
				schema = primitiveSchema;
				break;
			}
			default: {
				schema = null;
			}
		}
		if (schema != null) {
			String description = stringValue(schemaAsMap, SCHEMA_PROPERTY_DESCRIPTION);
			if (!description.isEmpty()) {
				schema.setDescription(description);
			}
			Object defaultValue = schemaAsMap.get(SCHEMA_PROPERTY_DEFAULT);
			if (defaultValue != null) {
				schema.setDefault(JSON.toString(defaultValue));
			}
			Object exampleValue = schemaAsMap.get(SCHEMA_PROPERTY_EXAMPLE);
			if (exampleValue != null) {
				schema.setDefault(JSON.toString(exampleValue));
			}
		}
		return schema;
	}

	private static Map<?, ?> schemaAsMap(Object schema) throws ParseException {
		if (!(schema instanceof Map)) {
			throw new ParseException(I18NConstants.NO_OBJECT_SCHEMA__SCHEMA.fill(schema), -1);
		}
		return (Map<?, ?>) schema;
	}

	private static List<?> listValue(Map<?, ?> schemaAsMap, String key) throws ParseException {
		return valueTyped(List.class, schemaAsMap, key, Collections.emptyList());
	}

	private static Map<?, ?> mapValue(Map<?, ?> schemaAsMap, String key) throws ParseException {
		return valueTyped(Map.class, schemaAsMap, key, Collections.emptyMap());
	}

	private static <T> T valueTyped(Class<T> expectedType, Map<?, ?> schemaAsMap, Object key, T defaultValue)
			throws ParseException {
		Object value = schemaAsMap.get(key);
		if (value == null && !schemaAsMap.containsKey(key)) {
			value = defaultValue;
		}
		if (value == null) {
			return null;
		}
		if (expectedType.isInstance(value)) {
			return expectedType.cast(value);
		}
		ResKey message = I18NConstants.UNEXPECTED_VALUE_TYPE__VALUE__KEY__EXPECTED_TYPE.fill(value, key,
			expectedType.getSimpleName());
		throw new ParseException(message, -1);
	}

	private static String stringValue(Map<?, ?> schemaAsMap, String key) throws ParseException {
		return valueTyped(String.class, schemaAsMap, key, StringServices.EMPTY_STRING);
	}

}
