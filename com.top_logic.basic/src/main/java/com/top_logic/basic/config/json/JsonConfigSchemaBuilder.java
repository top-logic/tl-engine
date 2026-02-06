/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
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
import com.top_logic.basic.json.schema.model.AllOfSchema;
import com.top_logic.basic.json.schema.model.AnyOfSchema;
import com.top_logic.basic.json.schema.model.ArraySchema;
import com.top_logic.basic.json.schema.model.BooleanSchema;
import com.top_logic.basic.json.schema.model.ConstSchema;
import com.top_logic.basic.json.schema.model.EnumSchema;
import com.top_logic.basic.json.schema.model.FalseSchema;
import com.top_logic.basic.json.schema.model.NullSchema;
import com.top_logic.basic.json.schema.model.NumericSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.RefSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.json.schema.model.StringSchema;
import com.top_logic.basic.json.schema.model.TrueSchema;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Builder for creating JSON Schemas from TopLogic configuration descriptors.
 *
 * <p>
 * Converts TopLogic configuration interfaces into JSON Schema representations
 * by analyzing property descriptors, types, constraints, and nested structures.
 * </p>
 */
public class JsonConfigSchemaBuilder {

	/**
	 * Resolver that controls how schema references are generated.
	 *
	 * <p>
	 * This interface allows customizing whether nested type schemas should be built inline or
	 * referenced externally. When external references are used, the schema for nested types is not
	 * generated immediately but referenced via a URL that can be resolved separately.
	 * </p>
	 */
	public interface SchemaResolver {

		/**
		 * Resolves how a configuration type should be referenced in the schema.
		 *
		 * @param configType
		 *        The configuration interface class.
		 * @return An external reference URL if the type should be referenced externally, or
		 *         <code>null</code> to build the schema inline.
		 */
		String resolveConfigType(Class<?> configType);

		/**
		 * Resolves how an implementation type should be referenced in the schema.
		 *
		 * @param implType
		 *        The implementation class.
		 * @return An external reference URL if the type should be referenced externally, or
		 *         <code>null</code> to build the schema inline.
		 */
		String resolveImplementationType(Class<?> implType);

	}

	private static final String TYPE_PROPERTY = "$type";

	private static final Schema NONE = FalseSchema.create();

	/**
	 * Cache of schemas being built to handle recursive references.
	 */
	private Map<String, Schema> _schemaCache;

	/**
	 * The schema id of the root type being built.
	 *
	 * <p>
	 * Used to generate correct references: the root type is referenced via {@code #} (document
	 * root), while other types use {@code #/$defs/...}.
	 * </p>
	 */
	private String _rootSchemaId;

	/**
	 * Whether to inline properties schemas instead of using references.
	 *
	 * <p>
	 * When set to <code>true</code>, properties schemas are inlined directly into the surrounding
	 * all-of schema instead of being referenced via <code>$ref</code>. This produces a
	 * self-contained schema without separate definitions for properties.
	 * </p>
	 */
	private boolean _inline;

	/**
	 * Optional resolver for controlling external schema references.
	 *
	 * @see SchemaResolver
	 */
	private SchemaResolver _schemaResolver;

	/**
	 * Sets whether to inline properties schemas.
	 *
	 * @param inline
	 *        <code>true</code> to inline properties schemas, <code>false</code> to use references.
	 * @return This builder for method chaining.
	 *
	 * @see #_inline
	 */
	public JsonConfigSchemaBuilder setInline(boolean inline) {
		_inline = inline;
		return this;
	}

	/**
	 * Sets the schema resolver for controlling external references.
	 *
	 * <p>
	 * When a resolver is set, it is consulted before building schemas for nested configuration or
	 * implementation types. If the resolver returns an external URL, a <code>$ref</code> to that
	 * URL is emitted instead of building the schema inline.
	 * </p>
	 *
	 * @param resolver
	 *        The resolver to use, or <code>null</code> to build all schemas inline.
	 * @return This builder for method chaining.
	 */
	public JsonConfigSchemaBuilder setSchemaResolver(SchemaResolver resolver) {
		_schemaResolver = resolver;
		return this;
	}

	/**
	 * Builds a JSON Schema for a configuration interface.
	 *
	 * @param descriptor
	 *        The configuration descriptor to convert.
	 * @return The JSON Schema representing the configuration interface.
	 */
	public Schema buildConfigSchema(ConfigurationDescriptor descriptor) {
		_schemaCache = new HashMap<>();

		// Set root schema id for correct reference generation
		Class<?> instanceType = descriptor.getInstanceType();
		if (instanceType == null) {
			_rootSchemaId = schemaId(descriptor);
		} else if (instanceType != Object.class) {
			_rootSchemaId = schemaId(instanceType);
		}

		Schema toplevel = internalBuildConfigSchema(descriptor);

		// Extract root schema from $defs and make it the top-level
		toplevel = hoistRootSchema(toplevel);

		_schemaCache = null;
		_rootSchemaId = null;

		return toplevel;
	}

	/**
	 * Builds a JSON Schema for an implementation class.
	 *
	 * <p>
	 * This method creates a schema that describes the configuration options for a specific
	 * implementation class, including its {@code $type} annotation and all properties from its
	 * configuration interface.
	 * </p>
	 *
	 * @param implementationType
	 *        The implementation class to create a schema for.
	 * @return The JSON Schema representing the implementation class configuration.
	 */
	public Schema buildImplementationSchema(Class<?> implementationType) {
		_schemaCache = new HashMap<>();

		// Set root schema id for correct reference generation
		_rootSchemaId = schemaId(implementationType);

		Schema toplevel = internalBuildImplementationSchema(implementationType);

		// Extract root schema from $defs and make it the top-level
		toplevel = hoistRootSchema(toplevel);

		_schemaCache = null;
		_rootSchemaId = null;

		return toplevel;
	}

	/**
	 * Extracts the root schema from $defs and makes it the top-level schema.
	 *
	 * <p>
	 * The internal build methods put all schemas (including the root) into {@link #_schemaCache}
	 * and return references. This method extracts the root schema and places the remaining schemas
	 * as {@code $defs}.
	 * </p>
	 *
	 * @param toplevel
	 *        The schema returned from internal build methods (typically a RefSchema).
	 * @return The hoisted root schema with definitions set.
	 */
	private Schema hoistRootSchema(Schema toplevel) {
		if (_rootSchemaId != null && _schemaCache.containsKey(_rootSchemaId)) {
			// Extract root schema from cache
			Schema rootSchema = _schemaCache.remove(_rootSchemaId);
			if (rootSchema != null) {
				toplevel = rootSchema;
			}
		}

		// Set remaining schemas as $defs
		toplevel.setDefinitions(_schemaCache);
		return toplevel;
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
	 * value-schema(A) := any-of(
	 *   all-of(required-type-ref(A), ref:properties(A)), // if A is non-abstract
	 *   ref:value-schema(Bn), ...  // with Bn is a direct sub-type of A
	 * )
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
	private Schema internalBuildConfigSchema(ConfigurationDescriptor descriptor) {
		Class<?> instanceType = descriptor.getInstanceType();
		if (instanceType == null) {
			return buildConfigValueSchema(descriptor);
		} else {
			if (instanceType == Object.class) {
				// Safety: Do not enumerate the world.
				return allConfigs();
			} else {
				return internalBuildImplementationSchema(instanceType);
			}
		}
	}

	/**
	 * Creates a schema for a configured instance.
	 *
	 * @param instanceType
	 *        The implementation type being used for a configuration property (either a type
	 *        parameter of {@link PolymorphicConfiguration} or directly with {@link InstanceFormat}.
	 * @return An object schema describing all possible implementation classes with their
	 *         configuration options.
	 */
	private Schema internalBuildImplementationSchema(Class<?> instanceType) {
		return internalBuildImplementationSchema(instanceType, null);
	}

	private Schema internalBuildImplementationSchema(Class<?> instanceType, String excludeProperty) {
		String schemaName = schemaId(instanceType, excludeProperty);

		// Check cache first - if already building this type, use internal ref
		if (_schemaCache.containsKey(schemaName)) {
			return ref(schemaName);
		}

		// Check resolver for external reference (skip root type and types with excludeProperty)
		if (_schemaResolver != null && excludeProperty == null && !schemaName.equals(_rootSchemaId)) {
			String externalRef = _schemaResolver.resolveImplementationType(instanceType);
			if (externalRef != null) {
				return RefSchema.create().setRef(externalRef);
			}
		}

		// Reserve the slot in cache before processing to handle recursion
		_schemaCache.put(schemaName, null);

		Schema result = createImplementationSchema(instanceType, excludeProperty);

		_schemaCache.put(schemaName, result);
		return ref(schemaName);
	}

	private Schema createImplementationSchema(Class<?> instanceType, String excludeProperty) {
		Schema result = NONE;

		try {
			if (!isAbstract(instanceType)) {
				result = anyOf(result, createLocalImplementationSchema(instanceType, excludeProperty));
			}

			result = addSubtypeOptions(instanceType, result, excludeProperty);
		} catch (ConfigurationException ex) {
			Logger.warn("Cannot resolve schema for instance type '': " + ex.getMessage(), ex,
				JsonConfigSchemaBuilder.class);
		}

		return result;
	}

	private Schema addSubtypeOptions(Class<?> instanceType, Schema result, String excludeProperty) {
		for (Class<?> specialization : TypeIndex.getInstance().getSpecializations(instanceType, false, false,
			true)) {
			result = anyOf(result, internalBuildImplementationSchema(specialization, excludeProperty));
		}
		return result;
	}

	private Schema anyOf(Schema a, Schema b) {
		if (a instanceof FalseSchema) {
			return b;
		}
		if (b instanceof FalseSchema) {
			return a;
		}
		if (a instanceof TrueSchema) {
			return a;
		}
		if (b instanceof TrueSchema) {
			return b;
		}

		AnyOfSchema result = AnyOfSchema.create();
		addAnyOf(result, a);
		addAnyOf(result, b);
		return result;
	}

	private void addAnyOf(AnyOfSchema result, Schema other) {
		if (other instanceof AnyOfSchema inner) {
			for (Schema option : inner.getAnyOf()) {
				result.addAnyOf(option);
			}
		} else {
			result.addAnyOf(other);
		}
	}

	private Schema createLocalImplementationSchema(Class<?> instanceType, String excludeProperty)
			throws ConfigurationException {
		AllOfSchema allOf = AllOfSchema.create();

		ObjectSchema implRef = ObjectSchema.create();
		implRef.putProperty(TYPE_PROPERTY, ConstSchema.create().setConstValue(instanceType.getName()));
		implRef.addRequired(TYPE_PROPERTY);
		allOf.addAllOf(implRef);

		Class<?> configInterface = DefaultConfigConstructorScheme.getFactory(instanceType).getConfigurationInterface();
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configInterface);
		Schema propertiesSchema = buildPropertiesSchema(descriptor, excludeProperty);
		inlineAllOf(allOf, propertiesSchema);

		return finalize(allOf);
	}

	private Schema finalize(AllOfSchema allOf) {
		Schema result = simplifyAllOf(allOf);
		if (result instanceof ObjectSchema objSchema) {
			objSchema.setAdditionalProperties(NONE);
		}
		return result;
	}

	private boolean isAbstract(Class<?> instanceType) {
		return (instanceType.getModifiers() & Modifier.ABSTRACT) != 0;
	}

	/**
	 * Builds an extension-schema for type A.
	 *
	 * <pre>
	 * value-schema(A) := any-of(
	 *   all-of(required-type-ref(A), ref:properties(A)), // if A is non-abstract
	 *   ref:value-schema(Bn), ...  // with Bn is a direct sub-type of A
	 * )
	 * </pre>
	 *
	 * <p>
	 * When {@link #_inline} is set, the properties schema is inlined directly instead of being
	 * referenced.
	 * </p>
	 */
	private Schema buildConfigValueSchema(ConfigurationDescriptor descriptor) {
		return buildConfigValueSchema(descriptor, null);
	}

	/**
	 * Builds an extension-schema for type A, optionally excluding a property.
	 *
	 * @param descriptor
	 *        The configuration descriptor.
	 * @param excludeProperty
	 *        Property name to exclude (for map value schemas where the key property is omitted), or
	 *        <code>null</code> for normal schemas.
	 */
	private Schema buildConfigValueSchema(ConfigurationDescriptor descriptor, String excludeProperty) {
		String extensionSchemaId = schemaId(descriptor, excludeProperty);

		// Check cache first - if already building this type, use internal ref
		if (_schemaCache.containsKey(extensionSchemaId)) {
			return ref(extensionSchemaId);
		}

		// Check resolver for external reference (skip root type and types with excludeProperty)
		if (_schemaResolver != null && excludeProperty == null && !extensionSchemaId.equals(_rootSchemaId)) {
			String externalRef = _schemaResolver.resolveConfigType(descriptor.getConfigurationInterface());
			if (externalRef != null) {
				return RefSchema.create().setRef(externalRef);
			}
		}

		// Reserve slot to handle recursion
		_schemaCache.put(extensionSchemaId, null);

		Schema result;
		if (descriptor.getConfigurationInterface() == ConfigurationItem.class) {
			// Safety: Do not iterate the world.
			result = allConfigs();
		} else if (descriptor.isFinal()) {
			// Final types: no $type annotation, no subtypes considered.
			result = buildPropertiesSchema(descriptor, excludeProperty);
			if (result instanceof ObjectSchema objSchema) {
				objSchema.setAdditionalProperties(NONE);
			}
		} else {
			result = NONE;

			// The concrete configuration itself.
			if (!descriptor.isAbstract()) {
				AllOfSchema allOf = AllOfSchema.create();

				allOf.addAllOf(buildRequiredTypeRef(descriptor));

				Schema propertiesSchema = buildPropertiesSchema(descriptor, excludeProperty);
				inlineAllOf(allOf, propertiesSchema);

				Schema self = finalize(allOf);

				result = anyOf(result, self);
			}

			// Add sub-type options.
			for (Class<?> subType : getDirectSubtypes(descriptor)) {
				result = anyOf(result, buildConfigValueSchema(TypedConfiguration.getConfigurationDescriptor(subType), excludeProperty));
			}
		}

		_schemaCache.put(extensionSchemaId, result);
		return ref(extensionSchemaId);
	}

	/**
	 * Generic schema that allows any configuration.
	 */
	private ObjectSchema allConfigs() {
		ObjectSchema allConfigs = ObjectSchema.create();
		allConfigs.setDescription(
			"All configuration types fit here. Such situation typically is a but in the configuration descriptor.");
		allConfigs.putProperty(TYPE_PROPERTY, StringSchema.create());
		allConfigs.addRequired(TYPE_PROPERTY);
		return allConfigs;
	}

	private RefSchema ref(String schemaName) {
		if (schemaName.equals(_rootSchemaId)) {
			// Reference to root schema uses document root URI
			return RefSchema.create().setRef("#");
		}
		return RefSchema.create().setRef("#/$defs/" + schemaName);
	}

	/**
	 * Builds a properties schema for type A.
	 *
	 * <pre>
	 * properties(A) :=
	 *   all-of(local-properties(A), ref:properties(Cn),...)
	 *   if A is a config type and Cn is a direct super-type of A
	 * </pre>
	 *
	 * <p>
	 * When {@link #_inline} is set, super-type properties schemas are inlined directly instead of
	 * being referenced.
	 * </p>
	 */
	private Schema buildPropertiesSchema(ConfigurationDescriptor descriptor) {
		return buildPropertiesSchema(descriptor, null);
	}

	/**
	 * Builds a properties schema for type A, optionally excluding a property.
	 *
	 * @param descriptor
	 *        The configuration descriptor.
	 * @param excludeProperty
	 *        Property name to exclude (for map value schemas), or <code>null</code>.
	 */
	private Schema buildPropertiesSchema(ConfigurationDescriptor descriptor, String excludeProperty) {
		if (_inline) {
			return createPropertiesSchema(descriptor, excludeProperty);
		} else {
			String propsSchemaId = propertiesSchemaId(descriptor, excludeProperty);

			if (!_schemaCache.containsKey(propsSchemaId)) {
				// Reserve slot to handle recursion
				_schemaCache.put(propsSchemaId, null);

				Schema result = createPropertiesSchema(descriptor, excludeProperty);

				_schemaCache.put(propsSchemaId, result);
			}

			return ref(propsSchemaId);
		}
	}

	private Schema createPropertiesSchema(ConfigurationDescriptor descriptor, String excludeProperty) {
		ConfigurationDescriptor[] superDescriptors = descriptor.getSuperDescriptors();
		ObjectSchema localProps = buildLocalPropertiesSchema(descriptor, excludeProperty);
		boolean hasLocalProps = !localProps.getProperties().isEmpty();

		Schema result;
		if (superDescriptors.length == 0) {
			// No super-types, just use local properties
			result = localProps;
		} else {
			AllOfSchema allOf = AllOfSchema.create();

			// Add local-properties(A) only if it has properties
			if (hasLocalProps) {
				allOf.addAllOf(localProps);
			}

			// Add properties(Cn) for each direct super-type Cn
			for (ConfigurationDescriptor superDescriptor : superDescriptors) {
				Schema superPropertiesSchema = buildPropertiesSchema(superDescriptor, excludeProperty);
				inlineAllOf(allOf, superPropertiesSchema);
			}

			result = simplifyAllOf(allOf);
			if (result == null) {
				// No local properties and no super-types with properties
				result = localProps;
			}
		}
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
	private ObjectSchema buildLocalPropertiesSchema(ConfigurationDescriptor descriptor, String excludeProperty) {
		ObjectSchema result = ObjectSchema.create();

		if (descriptor.getConfigurationInterface() == ConfigurationItem.class
			|| descriptor.getConfigurationInterface() == PolymorphicConfiguration.class) {
			// All properties are handles especially.
			return result;
		}

		// Process only local (non-inherited) properties
		for (PropertyDescriptor property : descriptor.getProperties()) {
			if (property.isInherited()) {
				continue;
			}
			if (property.isAbstract()) {
				// This cannot be set and may be derived in some concrete sub-type.
				continue;
			}

			String propertyName = property.getPropertyName();

			// Skip the excluded property (used for map value schemas where the key property is
			// omitted)
			if (propertyName.equals(excludeProperty)) {
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

				result.putProperty(propertyName, schema);

				// Mark as required if mandatory
				if (property.isMandatory()) {
					result.addRequired(propertyName);
				}
			}
		}
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
		result.putProperty(TYPE_PROPERTY, ConstSchema.create().setConstValue(configInterface.getName()));
		result.addRequired(TYPE_PROPERTY);

		return result;
	}

	/**
	 * Gets direct subtypes of a configuration interface (including abstract ones).
	 */
	private Collection<Class<?>> getDirectSubtypes(ConfigurationDescriptor descriptor) {
		Class<?> configInterface = descriptor.getConfigurationInterface();

		return TypeIndex.getInstance().getSpecializations(
			configInterface,
			/* transitive */ false,
			/* onlyInterfaces */ true,
			/* includeAbstract */ true);
	}

	private String schemaId(Class<?> implType) {
		return implType.getName();
	}

	private String schemaId(Class<?> implType, String excludeProperty) {
		if (excludeProperty == null) {
			return schemaId(implType);
		}
		return implType.getName() + "#map:" + excludeProperty;
	}

	private String schemaId(ConfigurationDescriptor descriptor) {
		return descriptor.getConfigurationInterface().getName();
	}

	private String schemaId(ConfigurationDescriptor descriptor, String excludeProperty) {
		if (excludeProperty == null) {
			return schemaId(descriptor);
		}
		return descriptor.getConfigurationInterface().getName() + "#map:" + excludeProperty;
	}

	private String propertiesSchemaId(ConfigurationDescriptor descriptor) {
		return descriptor.getConfigurationInterface().getName() + "#properties";
	}

	private String propertiesSchemaId(ConfigurationDescriptor descriptor, String excludeProperty) {
		if (excludeProperty == null) {
			return propertiesSchemaId(descriptor);
		}
		return descriptor.getConfigurationInterface().getName() + "#properties#map:" + excludeProperty;
	}

	/**
	 * Inlines the given schema into an all-of schema.
	 *
	 * <p>
	 * If the schema is itself an {@link AllOfSchema}, its contents are added directly to avoid
	 * nested all-of schemas. Otherwise, the schema is added as a single element.
	 * </p>
	 *
	 * @param allOf
	 *        The all-of schema to add to.
	 * @param schema
	 *        The schema to inline.
	 */
	private void inlineAllOf(AllOfSchema allOf, Schema schema) {
		if (schema instanceof AllOfSchema) {
			// Flatten nested all-of
			for (Schema nested : ((AllOfSchema) schema).getAllOf()) {
				allOf.addAllOf(nested);
			}
		} else if (schema != null) {
			allOf.addAllOf(schema);
		}
	}

	/**
	 * Simplifies an all-of schema.
	 *
	 * <p>
	 * Returns <code>null</code> if the all-of is empty, returns the single element if it contains
	 * only one schema. If all elements are {@link ObjectSchema}s, they are merged into a single
	 * object schema. Otherwise returns the all-of as-is.
	 * </p>
	 *
	 * @param allOf
	 *        The all-of schema to simplify.
	 * @return The simplified schema, or <code>null</code> if empty.
	 */
	private Schema simplifyAllOf(AllOfSchema allOf) {
		if (allOf.getAllOf().isEmpty()) {
			return TrueSchema.create();
		} else if (allOf.getAllOf().size() == 1) {
			return allOf.getAllOf().get(0);
		} else if (canMerge(allOf)) {
			return mergeAllOf(allOf);
		} else {
			return allOf;
		}
	}

	/**
	 * Checks if all elements in the all-of schema are object schemas.
	 */
	private boolean canMerge(AllOfSchema allOf) {
		for (Schema schema : allOf.getAllOf()) {
			if (schema instanceof ObjectSchema) {
				continue;
			}
			if (schema instanceof TrueSchema) {
				continue;
			}
			if (schema instanceof FalseSchema) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Merges all object schemas in an all-of into a single object schema.
	 *
	 * <p>
	 * Combines properties and required fields from all object schemas.
	 * </p>
	 */
	private Schema mergeAllOf(AllOfSchema allOf) {
		ObjectSchema merged = null;

		for (Schema schema : allOf.getAllOf()) {
			if (schema instanceof TrueSchema) {
				continue;
			}
			if (schema instanceof FalseSchema) {
				return schema;
			}

			ObjectSchema objectSchema = (ObjectSchema) schema;

			if (merged == null) {
				merged = ObjectSchema.create();
			}

			// Merge properties
			for (Map.Entry<String, Schema> entry : objectSchema.getProperties().entrySet()) {
				if (merged.getProperties().containsKey(entry.getKey())) {
					// Prevent clash. overwritten properties are entered first, so all other
					// properties with the same name are redundant.
					continue;
				}
				merged.putProperty(entry.getKey(), entry.getValue());
			}

			// Merge required fields
			for (String required : objectSchema.getRequired()) {
				if (!merged.getRequired().contains(required)) {
					merged.addRequired(required);
				}
			}
		}

		if (merged == null) {
			return TrueSchema.create();
		}

		return merged;
	}

	/**
	 * Builds a schema for a single property.
	 */
	private Schema buildPropertySchema(PropertyDescriptor property) {
		switch (property.kind()) {
			case PLAIN:
				return nullable(property, buildPlainPropertySchema(property));
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
		Schema result = NONE;
		if (property.getValueProvider() != null) {
			// Also allow a plain string configuration.
			result = anyOf(result, StringSchema.create());
		}

		if (property.isNullable()) {
			// Also allow a plain string configuration.
			result = anyOf(result, NullSchema.create());
		}

		Schema objectSchema = buildItemValueSchema(property);
		result = anyOf(result, objectSchema);

		return result;
	}

	private Schema buildItemValueSchema(PropertyDescriptor property) {
		return buildItemValueSchema(property, null);
	}

	private Schema buildItemValueSchema(PropertyDescriptor property, String excludeProperty) {
		Class<?> instanceType = property.getInstanceType();
		if (instanceType != null) {
			return internalBuildImplementationSchema(instanceType, excludeProperty);
		} else {
			return buildConfigValueSchema(property, excludeProperty);
		}
	}

	private Schema buildConfigValueSchema(PropertyDescriptor property, String excludeProperty) {
		// Get the configuration descriptor for the nested interface
		ConfigurationDescriptor valueDescriptor = property.getValueDescriptor();

		// Recursively build schema for the nested configuration
		return buildConfigValueSchema(valueDescriptor, excludeProperty);
	}

	/**
	 * Builds schema for LIST/ARRAY properties.
	 */
	private Schema buildListPropertySchema(PropertyDescriptor property) {
		ArraySchema listSchema = ArraySchema.create();

		Schema elementSchema = buildItemValueSchema(property);
		listSchema.setItems(elementSchema);

		return listSchema;
	}

	/**
	 * Builds schema for MAP properties.
	 */
	private Schema buildMapPropertySchema(PropertyDescriptor property) {
		// Maps in JSON Schema are typically represented as objects with additionalProperties
		ObjectSchema schema = ObjectSchema.create();

		// Check for key property to determine if we need a specialized value schema
		PropertyDescriptor keyProperty = property.getKeyProperty();
		String excludeProperty = keyProperty != null ? keyProperty.getPropertyName() : null;

		Schema valueSchema = buildItemValueSchema(property, excludeProperty);
		if (valueSchema != null) {
			schema.setAdditionalProperties(valueSchema);
		}

		// Add propertyNames constraint based on the key property's schema
		if (keyProperty != null) {
			Schema keySchema = buildPlainPropertySchema(keyProperty, true);
			if (keySchema != null) {
				schema.setPropertyNames(keySchema);
			}
		}

		return schema;
	}

	/**
	 * Complex properties are described by an annotated {@link ConfigurationValueBinding}. This is
	 * only possible for XML serialized configurations.
	 */
	private Schema buildComplexPropertySchema(PropertyDescriptor property) {
		Logger.warn("Ignoring complex property '" + property + "' (only supported in XML).", JsonConfigSchemaBuilder.class);
		return null;
	}

	/**
	 * Builds schema for plain (primitive/simple) properties.
	 */
	private Schema buildPlainPropertySchema(PropertyDescriptor property) {
		return buildPlainPropertySchema(property, false);
	}

	/**
	 * Builds schema for plain (primitive/simple) properties.
	 *
	 * @param property
	 *        The property descriptor.
	 * @param asMapKey
	 *        Whether this schema is for a map key. If <code>true</code>, numeric and boolean types
	 *        are represented as string schemas with appropriate patterns, since JSON object keys
	 *        are always strings.
	 */
	private Schema buildPlainPropertySchema(PropertyDescriptor property, boolean asMapKey) {
		Class<?> type = property.getType();

		// Numeric types
		if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
			if (asMapKey) {
				return StringSchema.create().setPattern("^-?[0-9]+$");
			}
			NumericSchema schema = NumericSchema.create().setIntegerOnly(true);
			processNumeric(property, schema);

			return schema;
		}
		if (type == float.class || type == Float.class || type == double.class || type == Double.class) {
			if (asMapKey) {
				return StringSchema.create().setPattern("^-?[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?$");
			}
			NumericSchema schema = NumericSchema.create();
			processNumeric(property, schema);
			return schema;
		}
		if (type == short.class || type == Short.class) {
			if (asMapKey) {
				return StringSchema.create().setPattern("^-?[0-9]+$");
			}
			NumericSchema schema = NumericSchema.create()
				.setIntegerOnly(true)
				.setMinimum((double) Short.MIN_VALUE)
				.setMaximum((double) Short.MAX_VALUE);
			processNumeric(property, schema);
			return schema;
		}
		if (type == byte.class || type == Byte.class) {
			if (asMapKey) {
				return StringSchema.create().setPattern("^-?[0-9]+$");
			}
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
		if (type == boolean.class || type == Boolean.class) {
			if (asMapKey) {
				return EnumSchema.create().addEnumLiteral("true").addEnumLiteral("false");
			}
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

		// Concrete format is defined by the ConfigurationValueProvider.
		return StringSchema.create();
	}

	private Schema nullable(PropertyDescriptor property, Schema schema) {
		if (property.isNullable()) {
			return anyOf(schema, NullSchema.create());
		} else {
			return schema;
		}
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

	private I18NBundle resources() {
		return ResourcesModule.getInstance().getBundle(Locale.ENGLISH);
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
	 * Encodes a Java class name to a URI-safe schema identifier.
	 *
	 * <p>
	 * Replaces the {@code $} character used for inner classes with {@code /} to produce an
	 * unambiguous URI-like naming scheme. Package separators remain as {@code .}.
	 * </p>
	 *
	 * <p>
	 * Example: {@code com.example.Outer$Inner} becomes {@code com.example.Outer/Inner}
	 * </p>
	 *
	 * @param className
	 *        The fully-qualified Java class name.
	 * @return The encoded schema identifier.
	 */
	public static String encodeTypeName(String className) {
		return className.replace('$', '/');
	}

	/**
	 * Decodes a schema identifier back to a Java class name.
	 *
	 * <p>
	 * This is the inverse of {@link #encodeTypeName(String)}.
	 * </p>
	 *
	 * @param schemaId
	 *        The encoded schema identifier.
	 * @return The original Java class name.
	 */
	public static String decodeTypeName(String schemaId) {
		return schemaId.replace('/', '$');
	}

	/**
	 * Loads a class from an encoded type name.
	 *
	 * <p>
	 * Decodes the schema identifier and loads the corresponding class.
	 * </p>
	 *
	 * @param encodedName
	 *        The encoded type name (with {@code /} instead of {@code $} for inner classes).
	 * @return The loaded class.
	 * @throws ClassNotFoundException
	 *         If no class can be found matching the encoded name.
	 */
	public static Class<?> loadEncodedClass(String encodedName) throws ClassNotFoundException {
		String className = decodeTypeName(encodedName);
		return Class.forName(className);
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
