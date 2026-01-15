/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.config;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.ai.service.jsonschema.model.ArraySchema;
import com.top_logic.ai.service.jsonschema.model.ConstSchema;
import com.top_logic.ai.service.jsonschema.model.EnumSchema;
import com.top_logic.ai.service.jsonschema.model.NumericSchema;
import com.top_logic.ai.service.jsonschema.model.ObjectSchema;
import com.top_logic.ai.service.jsonschema.model.Schema;
import com.top_logic.ai.service.jsonschema.model.StringSchema;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Bounds;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Negative;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.constraint.impl.NonPositive;
import com.top_logic.basic.config.constraint.impl.Positive;

/**
 * Builder for creating JSON Schemas from TopLogic configuration descriptors.
 *
 * <p>
 * Converts TopLogic configuration interfaces into JSON Schema representations
 * by analyzing property descriptors, types, constraints, and nested structures.
 * </p>
 */
public class ConfigSchemaBuilder {

	/**
	 * Cache of schemas being built to handle recursive references.
	 */
	private final Map<Class<?>, Schema> _schemaCache = new HashMap<>();

	/**
	 * Builds a JSON Schema from a configuration descriptor.
	 *
	 * @param descriptor
	 *        The configuration descriptor to convert.
	 * @return The JSON Schema representing the configuration interface.
	 */
	public Schema build(ConfigurationDescriptor descriptor) {
		return buildObjectSchema(descriptor);
	}

	private Schema buildObjectSchema(Class<?> type) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(type);
		return buildObjectSchema(descriptor);
	}

	/**
	 * Builds an object schema for a configuration interface.
	 */
	private ObjectSchema buildObjectSchema(ConfigurationDescriptor descriptor) {
		Class<?> configInterface = descriptor.getConfigurationInterface();

		// Check cache to handle recursive references
		if (_schemaCache.containsKey(configInterface)) {
			// Return a reference to the cached schema
			return (ObjectSchema) _schemaCache.get(configInterface);
		}

		// Create schema and add to cache before processing properties to handle recursion
		ObjectSchema result = ObjectSchema.create()
			.putPropertie("$type", ConstSchema.create().setConstValue(configInterface.getName()));
		_schemaCache.put(configInterface, result);

		// Process all properties
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Schema schema = buildPropertySchema(property);

			if (schema != null) {
				// Add description from property if available
				// TODO: Find the resource key for a property and use tooltip sub-key
				// String description = resolvePropertyDescription(property);
				// if (description != null) {
				//     schema.setDescription(description);
				// }

				result.putPropertie(property.getPropertyName(), schema);

				// Mark as required if mandatory
				if (property.isMandatory()) {
					result.addRequired(property.getPropertyName());
				}
			}
		}

		return result;
	}

	/**
	 * Builds a schema for a single property.
	 */
	private Schema buildPropertySchema(PropertyDescriptor property) {
		switch (property.kind()) {
			case PLAIN:
				return buildPlainPropertySchema(property);
			case ITEM:
				return buildItemPropertySchema(property);
			case ARRAY:
			case LIST:
				return buildListPropertySchema(property);
			case MAP:
				return buildMapPropertySchema(property);
			case COMPLEX:
				return buildComplexPropertySchema(property);
			case DERIVED:
				// Derived properties are computed - skip them
				return null;
			case REF:
				// References to TLObjects - not supported in JSON schema
				return null;
			default:
				return null;
		}
	}

	/**
	 * Builds schema for ITEM properties (nested configuration interfaces).
	 */
	private Schema buildItemPropertySchema(PropertyDescriptor property) {
		Class<?> itemType = property.getType();

		// Get the configuration descriptor for the nested interface
		ConfigurationDescriptor itemDescriptor = TypedConfiguration.getConfigurationDescriptor(itemType);

		// Recursively build schema for the nested configuration
		return buildObjectSchema(itemDescriptor);
	}

	/**
	 * Builds schema for LIST/ARRAY properties.
	 */
	private Schema buildListPropertySchema(PropertyDescriptor property) {
		ArraySchema schema = ArraySchema.create();

		// Get the element type
		Class<?> elementType = property.getElementType();

		// Create a property descriptor-like schema for the element
		Schema elementSchema = buildObjectSchema(elementType);
		if (elementSchema != null) {
			schema.setItems(elementSchema);
		}

		return schema;
	}

	/**
	 * Builds schema for MAP properties.
	 */
	private Schema buildMapPropertySchema(PropertyDescriptor property) {
		// Maps in JSON Schema are typically represented as objects with additionalProperties
		ObjectSchema schema = ObjectSchema.create();

		// Get the value type
		Class<?> valueType = property.getElementType();
		Schema valueSchema = buildObjectSchema(valueType);
		if (valueSchema != null) {
			// TODO: Use Key annotation to determine key property of the map, use this as property
			// name schema and remove that property from the properties of the inner object.
			schema.setAdditionalProperties(valueSchema);
		}

		return schema;
	}

	/**
	 * Complex properties are described by an annotated {@link ConfigurationValueBinding}. This is
	 * only possible for XML serialized configurations.
	 */
	private Schema buildComplexPropertySchema(PropertyDescriptor property) {
		return null;
	}

	/**
	 * Builds schema for plain (primitive/simple) properties.
	 */
	private Schema buildPlainPropertySchema(PropertyDescriptor property) {
		Class<?> type = property.getType();
	
		// Numeric types
		if (type == Integer.class || type == Long.class) {
			NumericSchema schema = NumericSchema.create().setIntegerOnly(true);
			processNumeric(property, schema);
			return schema;
		}
		if (type == Float.class || type == Double.class) {
			NumericSchema schema = NumericSchema.create();
			processNumeric(property, schema);
			return schema;
		}
		if (type == Short.class) {
			NumericSchema schema = NumericSchema.create()
				.setIntegerOnly(true)
				.setMinimum(Short.MIN_VALUE)
				.setMaximum(Short.MAX_VALUE);
			processNumeric(property, schema);
			return schema;
		}
		if (type == Byte.class) {
			NumericSchema schema = NumericSchema.create()
				.setIntegerOnly(true)
				.setMinimum(Byte.MIN_VALUE)
				.setMaximum(Byte.MAX_VALUE);
			processNumeric(property, schema);
			return schema;
		}
	
		// String type
		if (type == String.class) {
			return StringSchema.create();
		}
	
		// Boolean type
		if (type == Boolean.class) {
			// JSON Schema doesn't have a dedicated boolean schema in our model
			// We could use ConstSchema with true/false or create a BooleanSchema
			// For now, return a generic schema
			return StringSchema.create(); // TODO: Add BooleanSchema to the model
		}
	
		// Enum types
		if (type.isEnum()) {
			EnumSchema schema = EnumSchema.create();
			for (Object constant : type.getEnumConstants()) {
				// TODO: Care for enums that implement ExternallyNamed or ProtcolEnum that use
				// different serialization.
				schema.addEnumLiteral(((Enum<?>) constant).name());
			}
			return schema;
		}
	
		// TODO: Use annotated ConfigurationValueProvider to determine format.
		return null;
	}

	private void processNumeric(PropertyDescriptor property, NumericSchema numericSchema) {
		Constraint constraint = property.getAnnotation(Constraint.class);
		if (constraint != null) {
			Class<? extends ConstraintAlgorithm> algorithm = constraint.value();
			if (algorithm == NonNegative.class) {
				numericSchema.setMinimum(0);
			} else if (algorithm == Negative.class) {
				numericSchema.setExclusiveMaximum(0);
			} else if (algorithm == NonPositive.class) {
				numericSchema.setMaximum(0);
			} else if (algorithm == Positive.class) {
				numericSchema.setExclusiveMinimum(0);
			}
		}

		Bound bound = property.getAnnotation(Bound.class);
		if (bound != null) {
			processBound(numericSchema, bound);
		}
		
		Bounds bounds = property.getAnnotation(Bounds.class);
		if (bounds != null) {
			for (Bound inner : bounds.value()) {
				processBound(numericSchema, inner);
			}
		}
	}

	private void processBound(NumericSchema numericSchema, Bound bound) {
		switch (bound.comparison()) {
			case GREATER:
				numericSchema.setExclusiveMinimum(bound.value());
				break;
			case GREATER_OR_EQUAL:
				numericSchema.setMinimum(bound.value());
				break;
			case SMALLER:
				numericSchema.setExclusiveMaximum(bound.value());
				break;
			case SMALLER_OR_EQUAL:
				numericSchema.setMaximum(bound.value());
				break;
		}
	}

}
