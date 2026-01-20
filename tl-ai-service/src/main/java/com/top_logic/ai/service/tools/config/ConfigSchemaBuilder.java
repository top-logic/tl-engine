/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.ai.service.jsonschema.model.AllOfSchema;
import com.top_logic.ai.service.jsonschema.model.AnyOfSchema;
import com.top_logic.ai.service.jsonschema.model.ArraySchema;
import com.top_logic.ai.service.jsonschema.model.BooleanSchema;
import com.top_logic.ai.service.jsonschema.model.ConstSchema;
import com.top_logic.ai.service.jsonschema.model.EnumSchema;
import com.top_logic.ai.service.jsonschema.model.JsonSchemaDocument;
import com.top_logic.ai.service.jsonschema.model.NullSchema;
import com.top_logic.ai.service.jsonschema.model.NumericSchema;
import com.top_logic.ai.service.jsonschema.model.ObjectSchema;
import com.top_logic.ai.service.jsonschema.model.RefSchema;
import com.top_logic.ai.service.jsonschema.model.Schema;
import com.top_logic.ai.service.jsonschema.model.StringSchema;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Bounds;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Negative;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.constraint.impl.NonPositive;
import com.top_logic.basic.config.constraint.impl.Positive;
import com.top_logic.basic.reflect.TypeIndex;
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

		JsonSchemaDocument result = JsonSchemaDocument.create()
			.setId(schemaId(descriptor))
			.setDefinitions(_schemaCache)
			.setSchema(toplevel);
		_schemaCache = null;

		return result;
	}

	/**
	 * Builds an object schema for a configuration interface.
	 *
	 * <p>
	 * A configuration is serialized to JSON in the following ways:
	 * </p>
	 *
	 * <p>
	 * A configuration item is serialized as object with an optional <code>$type</code> property
	 * that contains either the configuration interface (for pure configurations), or the
	 * implementation class if the configuration is a {@link PolymorphicConfiguration}.
	 * </p>
	 *
	 * <p>
	 * The type property is optional, if the type is clear from its context. When a configuration
	 * item is stored in a property of type A (either configuration type A extends
	 * {@link ConfigurationItem} or implementation type {@link InstanceFormat} A, or
	 * {@link PolymorphicConfiguration}&lt;? extends A&gt; and the value is of exactly that type (no
	 * sub-type), then the type annotation can be omitted.
	 * </p>
	 *
	 * <p>
	 * Therefore, the schema for type A must enforce all mandatory properties of A, define the
	 * schema for all properties of A and its super-types. If A has sub-types, the schema for A must
	 * mention all of them in an any-of schema:
	 * </p>
	 *
	 * <p>
	 * A redundancy-free schema definition for A would look like (the schema does not exclude other
	 * garbage properties not defined for type A):
	 * </p>
	 *
	 * <pre>
	 * <code>
	 * value-schema(A) :=
	 *   any-of(base-schema(A) if not abstract, ref:extension-schema(Bn),...) with Bn is a direct non-abstract sub-type of A
	 *
	 * properties(A) :=
	 *   all-of(local-properties(A), ref:properties(Cn),...) if A is a config type and Cn is a direct super-type of A
	 *   properties(C) if A is an implementation type and C is its configuration interface.
	 *
	 * local-properties(A) :=
	 *   JSON object schema with property schemas for all direct properties of A (without $type)
	 *   that also allows other properties not defined here.
	 *
	 * base-schema(A) :=
	 *   all-of(optional-type-ref(A), ref:properties(A))
	 *
	 * extension-schema(A) :=
	 *   all-of(required-type-ref(A), ref:properties(A))
	 *
	 * required-type-ref(A) :=
	 *   JSON object schema that defines just the required $type property fixed to A.class.getName()
	 *   and allows any other properties.
	 *
	 * optional-type-ref(A) :=
	 *   JSON object schema that defines just the optional $type property fixed to A.class.getName()
	 *   and allows any other properties.
	 * </code>
	 * </pre>
	 *
	 * <p>
	 * The following holds:
	 * </p>
	 *
	 * <ul>
	 * <li>A config interface type A is abstract, if its descriptor is abstract.</li>
	 * <li>An implementation type A is abstract, if it is an abstract class.</li>
	 * <li>The direct super-types of a config interface type A are the config interfaces of the
	 * super-descriptory of the descriptor for A.</li>
	 * <li>The direct sub-types of a config interface type A are config interfaces that directly
	 * extend A and can by found through the {@link TypeIndex}.</li>
	 * </ul>
	 */
	private Schema buildObjectSchema(ConfigurationDescriptor descriptor) {
		return buildValueSchema(descriptor);
	}

	/**
	 * Builds a value-schema for type A.
	 *
	 * <pre>
	 * value-schema(A) :=
	 *   any-of(base-schema(A) if not abstract, ref:extension-schema(Bn),...)
	 *   with Bn is a direct non-abstract sub-type of A
	 * </pre>
	 */
	private Schema buildValueSchema(ConfigurationDescriptor descriptor) {
		String schemaName = schemaId(descriptor);

		// Check cache to handle recursive references
		if (_schemaCache.containsKey(schemaName)) {
			return RefSchema.create().setRef(schemaName);
		}

		// Reserve the slot in cache before processing to handle recursion
		_schemaCache.put(schemaName, null);

		AnyOfSchema anyOf = AnyOfSchema.create();

		// Add base-schema(A) if A is not abstract
		if (!descriptor.isAbstract()) {
			anyOf.addAnyOf(buildBaseSchema(descriptor));
		}

		// Add ref:extension-schema(Bn) for each direct non-abstract sub-type Bn
		Collection<Class<?>> directSubtypes = getDirectSubtypes(descriptor);
		for (Class<?> subtype : directSubtypes) {
			try {
				ConfigurationDescriptor subtypeDescriptor = TypedConfiguration.getConfigurationDescriptor(subtype);
				String extensionSchemaId = extensionSchemaId(subtypeDescriptor);

				// Build extension schema and cache it
				buildExtensionSchema(subtypeDescriptor);

				anyOf.addAnyOf(RefSchema.create().setRef(extensionSchemaId));
			} catch (Exception ex) {
				Logger.warn("Invalid sub-configuration encountered: " + subtype.getName(), ex,
					ConfigSchemaBuilder.class);
			}
		}

		Schema result;
		if (anyOf.getAnyOf().size() == 1) {
			// Simplify: if only one option, use it directly
			result = anyOf.getAnyOf().get(0);
		} else if (anyOf.getAnyOf().isEmpty()) {
			// Abstract type with no subtypes - return base schema anyway for documentation
			result = buildBaseSchema(descriptor);
		} else {
			result = anyOf;
		}

		_schemaCache.put(schemaName, result);
		return result;
	}

	/**
	 * Builds a base-schema for type A.
	 *
	 * <pre>
	 * base-schema(A) := all-of(optional-type-ref(A), ref:properties(A))
	 * </pre>
	 */
	private Schema buildBaseSchema(ConfigurationDescriptor descriptor) {
		// Build properties schema first
		buildPropertiesSchema(descriptor);
		String propertiesSchemaId = propertiesSchemaId(descriptor);

		AllOfSchema allOf = AllOfSchema.create();
		allOf.addAllOf(buildOptionalTypeRef(descriptor));
		allOf.addAllOf(RefSchema.create().setRef(propertiesSchemaId));

		return allOf;
	}

	/**
	 * Builds an extension-schema for type A.
	 *
	 * <pre>
	 * extension-schema(A) := all-of(required-type-ref(A), ref:properties(A))
	 * </pre>
	 */
	private Schema buildExtensionSchema(ConfigurationDescriptor descriptor) {
		String extensionSchemaId = extensionSchemaId(descriptor);

		if (_schemaCache.containsKey(extensionSchemaId)) {
			return RefSchema.create().setRef(extensionSchemaId);
		}

		// Reserve slot to handle recursion
		_schemaCache.put(extensionSchemaId, null);

		// Build properties schema first
		buildPropertiesSchema(descriptor);
		String propertiesSchemaId = propertiesSchemaId(descriptor);

		AllOfSchema allOf = AllOfSchema.create();
		allOf.addAllOf(buildRequiredTypeRef(descriptor));
		allOf.addAllOf(RefSchema.create().setRef(propertiesSchemaId));

		_schemaCache.put(extensionSchemaId, allOf);
		return allOf;
	}

	/**
	 * Builds a properties schema for type A.
	 *
	 * <pre>
	 * properties(A) :=
	 *   all-of(local-properties(A), ref:properties(Cn),...)
	 *   if A is a config type and Cn is a direct super-type of A
	 * </pre>
	 */
	private Schema buildPropertiesSchema(ConfigurationDescriptor descriptor) {
		String propertiesSchemaId = propertiesSchemaId(descriptor);

		if (_schemaCache.containsKey(propertiesSchemaId)) {
			return RefSchema.create().setRef(propertiesSchemaId);
		}

		// Reserve slot to handle recursion
		_schemaCache.put(propertiesSchemaId, null);

		ConfigurationDescriptor[] superDescriptors = descriptor.getSuperDescriptors();

		if (superDescriptors.length == 0) {
			// No super-types, just use local properties
			Schema localProps = buildLocalPropertiesSchema(descriptor);
			_schemaCache.put(propertiesSchemaId, localProps);
			return localProps;
		}

		AllOfSchema allOf = AllOfSchema.create();

		// Add local-properties(A)
		allOf.addAllOf(buildLocalPropertiesSchema(descriptor));

		// Add ref:properties(Cn) for each direct super-type Cn
		for (ConfigurationDescriptor superDescriptor : superDescriptors) {
			buildPropertiesSchema(superDescriptor);
			String superPropertiesId = propertiesSchemaId(superDescriptor);
			allOf.addAllOf(RefSchema.create().setRef(superPropertiesId));
		}

		Schema result;
		if (allOf.getAllOf().size() == 1) {
			result = allOf.getAllOf().get(0);
		} else {
			result = allOf;
		}

		_schemaCache.put(propertiesSchemaId, result);
		return result;
	}

	/**
	 * Builds a local-properties schema for type A.
	 *
	 * <pre>
	 * local-properties(A) :=
	 *   JSON object schema with property schemas for all direct properties of A (without $type)
	 *   that also allows other properties not defined here.
	 * </pre>
	 */
	private Schema buildLocalPropertiesSchema(ConfigurationDescriptor descriptor) {
		ObjectSchema result = ObjectSchema.create();

		// Process only local (non-inherited) properties
		for (PropertyDescriptor property : descriptor.getProperties()) {
			if (!property.isLocalProperty()) {
				continue;
			}
			if (property.isAbstract()) {
				// This cannot be set and may be derived in some concrete sub-type.
				continue;
			}

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

	/**
	 * Builds an optional-type-ref schema for type A.
	 *
	 * <pre>
	 * optional-type-ref(A) :=
	 *   JSON object schema that defines just the optional $type property fixed to A.class.getName()
	 *   and allows any other properties.
	 * </pre>
	 */
	private Schema buildOptionalTypeRef(ConfigurationDescriptor descriptor) {
		Class<?> configInterface = descriptor.getConfigurationInterface();

		ObjectSchema result = ObjectSchema.create();
		result.putProperty("$type", ConstSchema.create().setConstValue(configInterface.getName()));
		// $type is NOT added to required - it's optional

		return result;
	}

	/**
	 * Builds a required-type-ref schema for type A.
	 *
	 * <pre>
	 * required-type-ref(A) :=
	 *   JSON object schema that defines just the required $type property fixed to A.class.getName()
	 *   and allows any other properties.
	 * </pre>
	 */
	private Schema buildRequiredTypeRef(ConfigurationDescriptor descriptor) {
		Class<?> configInterface = descriptor.getConfigurationInterface();

		ObjectSchema result = ObjectSchema.create();
		result.putProperty("$type", ConstSchema.create().setConstValue(configInterface.getName()));
		result.addRequired("$type");

		return result;
	}

	/**
	 * Gets direct non-abstract subtypes of a configuration interface.
	 */
	private Collection<Class<?>> getDirectSubtypes(ConfigurationDescriptor descriptor) {
		Class<?> configInterface = descriptor.getConfigurationInterface();

		// Use TypeIndex to find direct subtypes
		// transitive=false for direct subtypes only
		// onlyInterfaces=true for configuration interfaces
		// includeAbstract=false to exclude abstract types
		return TypeIndex.getInstance().getSpecializations(
			configInterface,
			/* transitive */ false,
			/* onlyInterfaces */ true,
			/* includeAbstract */ false);
	}

	private String schemaId(ConfigurationDescriptor descriptor) {
		return "config://" + descriptor.getConfigurationInterface().getName();
	}

	private String propertiesSchemaId(ConfigurationDescriptor descriptor) {
		return "config://" + descriptor.getConfigurationInterface().getName() + "#properties";
	}

	private String localPropertiesSchemaId(ConfigurationDescriptor descriptor) {
		return "config://" + descriptor.getConfigurationInterface().getName() + "#local-properties";
	}

	private String baseSchemaId(ConfigurationDescriptor descriptor) {
		return "config://" + descriptor.getConfigurationInterface().getName() + "#base";
	}

	private String extensionSchemaId(ConfigurationDescriptor descriptor) {
		return "config://" + descriptor.getConfigurationInterface().getName() + "#extension";
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
		Class<?> itemType = property.getValueDescriptor().getConfigurationInterface();

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
		if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
			NumericSchema schema = NumericSchema.create().setIntegerOnly(true);
			processNumeric(property, schema);

			return handleWrapperType(type, schema);
		}
		if (type == float.class || type == Float.class || type == double.class || type == Double.class) {
			NumericSchema schema = NumericSchema.create();
			processNumeric(property, schema);
			return handleWrapperType(type, schema);
		}
		if (type == short.class || type == Short.class) {
			NumericSchema schema = NumericSchema.create()
				.setIntegerOnly(true)
				.setMinimum((double) Short.MIN_VALUE)
				.setMaximum((double) Short.MAX_VALUE);
			processNumeric(property, schema);
			return handleWrapperType(type, schema);
		}
		if (type == byte.class || type == Byte.class) {
			NumericSchema schema = NumericSchema.create()
				.setIntegerOnly(true)
				.setMinimum((double) Byte.MIN_VALUE)
				.setMaximum((double) Byte.MAX_VALUE);
			processNumeric(property, schema);
			return handleWrapperType(type, schema);
		}
	
		// String type
		if (type == String.class) {
			return StringSchema.create();
		}
	
		// Boolean type
		if (type == boolean.class || type == Boolean.class) {
			return handleWrapperType(type, BooleanSchema.create());
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

	private Schema handleWrapperType(Class<?> type, Schema schema) {
		if (type.isPrimitive()) {
			return schema;
		} else {
			return nullable(schema);
		}
	}

	private AnyOfSchema nullable(Schema schema) {
		return AnyOfSchema.create().addAnyOf(schema).addAnyOf(NullSchema.create());
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
