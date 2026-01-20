/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.ai.service.jsonschema.model.ArraySchema;
import com.top_logic.ai.service.jsonschema.model.BooleanSchema;
import com.top_logic.ai.service.jsonschema.model.ConstSchema;
import com.top_logic.ai.service.jsonschema.model.EnumSchema;
import com.top_logic.ai.service.jsonschema.model.JsonSchemaDocument;
import com.top_logic.ai.service.jsonschema.model.NumericSchema;
import com.top_logic.ai.service.jsonschema.model.ObjectSchema;
import com.top_logic.ai.service.jsonschema.model.RefSchema;
import com.top_logic.ai.service.jsonschema.model.Schema;
import com.top_logic.ai.service.jsonschema.model.StringSchema;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ExternallyNamed;
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
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

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
	private Map<String, Schema> _schemaCache;

	/**
	 * Builds a JSON Schema from a configuration descriptor.
	 *
	 * @param descriptor
	 *        The configuration descriptor to convert.
	 * @return The JSON Schema representing the configuration interface.
	 */
	public Schema build(ConfigurationDescriptor descriptor) {
		_schemaCache = new HashMap<>();
		Schema toplevel = buildObjectSchema(descriptor);

		JsonSchemaDocument.create().setSchema(toplevel).setDefinitions(_schemaCache);
		_schemaCache = null;

		return toplevel;
	}

	/**
	 * Builds an object schema for a configuration interface.
	 */
	private Schema buildObjectSchema(ConfigurationDescriptor descriptor) {
		Class<?> configInterface = descriptor.getConfigurationInterface();

		String schemaName = schemaId(configInterface);

		// Check cache to handle recursive references
		if (_schemaCache.containsKey(schemaName)) {
			// Return a reference to the cached schema
			return RefSchema.create().setId(schemaName);
		}

		// Create schema and add to cache before processing properties to handle recursion
		ObjectSchema result = ObjectSchema.create()
			.putProperty("$type", ConstSchema.create().setConstValue(configInterface.getName()));
		_schemaCache.put(schemaName, result);

		// Process all properties
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Schema schema = buildPropertySchema(property);

			if (schema != null) {
				// Add label and description from property resources
				String label = resolvePropertyLabel(descriptor, property);
				if (label != null && !label.isEmpty()) {
					schema.setTitle(label);
				}

				String description = resolvePropertyDescription(descriptor, property);
				if (description != null && !description.isEmpty()) {
					schema.setDescription(description);
				}

				result.putProperty(property.getPropertyName(), schema);

				// Mark as required if mandatory
				if (property.isMandatory()) {
					result.addRequired(property.getPropertyName());
				}
			}
		}

		return result;
	}

	private String schemaId(Class<?> configInterface) {
		return "config://" + configInterface.getName();
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
		ConfigurationDescriptor valueDescriptor = property.getValueDescriptor();

		// Create a property descriptor-like schema for the element
		Schema elementSchema = buildObjectSchema(valueDescriptor);
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
		ConfigurationDescriptor valueDescriptor = property.getValueDescriptor();
		Schema valueSchema = buildObjectSchema(valueDescriptor);
		if (valueSchema != null) {
			// Check for @Key annotation
			com.top_logic.basic.config.annotation.Key keyAnnotation =
				property.getAnnotation(com.top_logic.basic.config.annotation.Key.class);

			if (keyAnnotation != null && valueSchema instanceof ObjectSchema) {
				// The map key comes from a property of the value object
				String keyPropertyName = keyAnnotation.value();
				ObjectSchema objectValueSchema = (ObjectSchema) valueSchema;

				// Get the key property schema to use as property name constraint
				Schema keyPropertySchema = objectValueSchema.getProperties().get(keyPropertyName);
				if (keyPropertySchema != null) {
					// Use the key property schema to constrain property names
					schema.setPropertyNames(keyPropertySchema);

					// Remove the key property from the value schema since it's redundant
					// (the property name already contains this information)
					ObjectSchema clonedValueSchema = ObjectSchema.create();

					// Copy all properties except the key property
					for (Map.Entry<String, Schema> entry : objectValueSchema.getProperties().entrySet()) {
						if (!entry.getKey().equals(keyPropertyName)) {
							clonedValueSchema.putProperty(entry.getKey(), entry.getValue());
						}
					}

					// Copy other object schema properties
					clonedValueSchema.setMaxProperties(objectValueSchema.getMaxProperties());
					clonedValueSchema.setMinProperties(objectValueSchema.getMinProperties());
					for (String req : objectValueSchema.getRequired()) {
						if (!req.equals(keyPropertyName)) {
							clonedValueSchema.addRequired(req);
						}
					}

					// Copy base schema properties
					copyBaseSchemaProperties(objectValueSchema, clonedValueSchema);

					valueSchema = clonedValueSchema;
				}
			}

			schema.setAdditionalProperties(valueSchema);
		}

		return schema;
	}

	/**
	 * Complex properties are described by an annotated {@link ConfigurationValueBinding}. This is
	 * only possible for XML serialized configurations.
	 */
	private Schema buildComplexPropertySchema(PropertyDescriptor property) {
		Logger.warn("Ignoring complex property '" + property + "' (only supported in XML).", ConfigSchemaBuilder.class);
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
				.setMinimum((double) Short.MIN_VALUE)
				.setMaximum((double) Short.MAX_VALUE);
			processNumeric(property, schema);
			return schema;
		}
		if (type == Byte.class) {
			NumericSchema schema = NumericSchema.create()
				.setIntegerOnly(true)
				.setMinimum((double) Byte.MIN_VALUE)
				.setMaximum((double) Byte.MAX_VALUE);
			processNumeric(property, schema);
			return schema;
		}
	
		// String type
		if (type == String.class) {
			return StringSchema.create();
		}
	
		// Boolean type
		if (type == Boolean.class) {
			return BooleanSchema.create();
		}
	
		// Enum types
		if (type.isEnum()) {
			EnumSchema schema = EnumSchema.create();
			for (Object constant : type.getEnumConstants()) {
				String literalValue;

				// Check if enum implements ExternallyNamed (TopLogic convention)
				if (constant instanceof ExternallyNamed) {
					literalValue = ((ExternallyNamed) constant).getExternalName();
				}
				// Check if enum implements ProtocolEnum (msgbuf convention)
				else if (constant instanceof de.haumacher.msgbuf.data.ProtocolEnum) {
					literalValue = ((de.haumacher.msgbuf.data.ProtocolEnum) constant).protocolName();
				}
				// Default: use enum constant name
				else {
					literalValue = ((Enum<?>) constant).name();
				}

				schema.addEnumLiteral(literalValue);
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
				numericSchema.setMinimum(0.0);
			} else if (algorithm == Negative.class) {
				numericSchema.setExclusiveMaximum(0.0);
			} else if (algorithm == NonPositive.class) {
				numericSchema.setMaximum(0.0);
			} else if (algorithm == Positive.class) {
				numericSchema.setExclusiveMinimum(0.0);
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

	/**
	 * Resolves the label (title) for a configuration property.
	 *
	 * <p>
	 * Constructs a resource key following TopLogic's naming convention for configuration
	 * properties: <code>class.&lt;ClassName&gt;.&lt;propertyName&gt;</code>
	 * </p>
	 *
	 * @param descriptor
	 *        The configuration descriptor containing the property.
	 * @param property
	 *        The property descriptor.
	 * @return The localized label, or null if not found.
	 */
	private String resolvePropertyLabel(ConfigurationDescriptor descriptor, PropertyDescriptor property) {
		ResKey labelKey = getPropertyResourceKey(descriptor, property);
		return resources().getString(labelKey, null);
	}

	private Resources resources() {
		return Resources.getInstance(Locale.ENGLISH);
	}

	/**
	 * Resolves the description (tooltip) for a configuration property.
	 *
	 * <p>
	 * Constructs a resource key following TopLogic's naming convention for configuration property
	 * tooltips: <code>class.&lt;ClassName&gt;.&lt;propertyName&gt;.tooltip</code>
	 * </p>
	 *
	 * @param descriptor
	 *        The configuration descriptor containing the property.
	 * @param property
	 *        The property descriptor.
	 * @return The localized description, or null if not found.
	 */
	private String resolvePropertyDescription(ConfigurationDescriptor descriptor, PropertyDescriptor property) {
		ResKey labelKey = getPropertyResourceKey(descriptor, property);
		ResKey tooltipKey = labelKey.tooltip();
		return resources().getString(tooltipKey, null);
	}

	private ResKey getPropertyResourceKey(ConfigurationDescriptor descriptor, PropertyDescriptor property) {
		Class<?> configInterface = descriptor.getConfigurationInterface();
		String propertyName = property.getPropertyName();

		// TopLogic convention: class.<ClassName>.<propertyName>.tooltip
		ResKey labelKey = ResKey.forClass(configInterface).suffix(propertyName);
		return labelKey;
	}

	/**
	 * Copies base schema properties (metadata) from source to target schema.
	 */
	private void copyBaseSchemaProperties(Schema source, Schema target) {
		target.setId(source.getId());
		target.setAnchor(source.getAnchor());
		target.setDynamicAnchor(source.getDynamicAnchor());
		target.setComment(source.getComment());
		target.setTitle(source.getTitle());
		target.setDescription(source.getDescription());
		target.setDefaultValue(source.getDefaultValue());
		target.setDeprecated(source.isDeprecated());
		target.setReadOnly(source.isReadOnly());
		target.setWriteOnly(source.isWriteOnly());

		// Copy definitions
		for (Map.Entry<String, Schema> entry : source.getDefinitions().entrySet()) {
			target.putDefinition(entry.getKey(), entry.getValue());
		}

		// Copy examples
		for (String example : source.getExamples()) {
			target.addExample(example);
		}
	}

}
