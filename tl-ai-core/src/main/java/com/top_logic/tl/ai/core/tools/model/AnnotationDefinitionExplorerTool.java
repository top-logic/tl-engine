/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

import com.top_logic.tl.ai.core.tools.model.messages.AnnotationDefinitionResponse;
import com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.thread.ThreadContextManager;

import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool for exploring annotation definition schemas.
 *
 * <p>
 * This tool allows AI agents to understand annotation DEFINITIONS (what CAN be configured), not
 * annotation INSTANCES (what IS configured). It provides schema information including property
 * metadata, types, constraints, and defaults.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * // Explore a specific annotation definition
 * describeAnnotation("com.top_logic.model.annotate.Visibility")
 *
 * // Explore form-definition annotation
 * describeAnnotation("com.top_logic.model.form.definition.FormDefinition")
 * </pre>
 *
 * <h3>Integration:</h3>
 *
 * <p>
 * Register this tool via {@link com.top_logic.tl.ai.core.tools.AnnotatedTools} and assign it to an
 * agent through the {@link com.top_logic.tl.ai.core.provider.LLMProviderRegistry} configuration.
 * </p>
 */
public class AnnotationDefinitionExplorerTool {

	/**
	 * Configuration for {@link AnnotationDefinitionExplorerTool}.
	 */
	public interface Config extends PolymorphicConfiguration<AnnotationDefinitionExplorerTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link AnnotationDefinitionExplorerTool}.
	 */
	public AnnotationDefinitionExplorerTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link AnnotationDefinitionExplorerTool} from configuration.
	 */
	@CalledByReflection
	public AnnotationDefinitionExplorerTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Describes the schema definition of an annotation configuration interface.
	 *
	 * <p>
	 * Returns detailed schema information about what properties an annotation supports, their
	 * types, constraints, and defaults. This is useful for understanding what CAN be configured for
	 * an annotation type.
	 * </p>
	 *
	 * @param configInterfaceName
	 *        Fully-qualified configuration interface class name (e.g.,
	 *        "com.top_logic.model.annotate.Visibility")
	 * @return JSON schema description, or error JSON if class not found or invalid
	 */
	@ToolMethod(description = "Describe the schema definition of an annotation configuration interface", mode = ToolMode.READ)
	public String describeAnnotation(
			@P("Fully-qualified configuration interface class name") String configInterfaceName) {

		try {
			// System interaction returns data class
			AnnotationDefinitionResponse response = ThreadContextManager.inSystemInteraction(
				AnnotationDefinitionExplorerTool.class,
				() -> {
					// 1. Validate parameter
					if (configInterfaceName == null || configInterfaceName.isEmpty()) {
						throw new IllegalArgumentException("Configuration interface name is required");
					}

					// 2. Load class and validate it's a ConfigurationItem
					Class<?> configClass = ClassValidator.loadAndValidate(configInterfaceName);

					// 3. Get ConfigurationDescriptor via TypedConfiguration
					ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configClass);

					// 4. Build response using data class
					AnnotationDefinitionResponse result = AnnotationDefinitionResponse.create();
					result.setConfigInterface(configInterfaceName);
					result.setTagName(getTagName(configClass));
					result.setAbstract(descriptor.isAbstract());

					// Super interfaces
					for (ConfigurationDescriptor superDescriptor : descriptor.getSuperDescriptors()) {
						result.addSuperInterface(superDescriptor.getConfigurationInterface().getName());
					}

					// Properties
					Set<String> visitedDescriptors = new HashSet<>();
					for (PropertyDescriptor property : descriptor.getProperties()) {
						if (isInternalProperty(property)) {
							continue;
						}
						PropertyDefinition propDef =
							PropertyDefinitionSerializer.buildPropertyDefinition(property, visitedDescriptors, 0);
						result.addProperty(propDef);
					}

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("configInterfaceName", configInterfaceName);
			return DataClassSerializer.toJson(response, AnnotationDefinitionExplorerTool.class,
				"describeAnnotation()", args);

		} catch (Exception ex) {
			Logger.error("describeAnnotation() error: " + ex.getMessage(), ex,
				AnnotationDefinitionExplorerTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("configInterfaceName", configInterfaceName);
			return DataClassSerializer.errorToJson(
				"Error accessing annotation definition: " + ex.getMessage(),
				AnnotationDefinitionExplorerTool.class,
				"describeAnnotation()",
				args);
		}
	}

	/**
	 * Extracts the tag name from a configuration interface's {@link TagName} annotation.
	 *
	 * @param configClass
	 *        The configuration interface class.
	 * @return The tag name, or the simple class name in lowercase if no {@link TagName} annotation
	 *         is present.
	 */
	private static String getTagName(Class<?> configClass) {
		TagName tagNameAnnotation = configClass.getAnnotation(TagName.class);

		if (tagNameAnnotation != null) {
			return tagNameAnnotation.value();
		}

		// Fallback: use simple class name in lowercase
		return configClass.getSimpleName().toLowerCase();
	}

	/**
	 * Checks if a property is internal and should be filtered from the schema output.
	 *
	 * @param property
	 *        The property to check.
	 * @return True if the property is internal and should be skipped.
	 */
	private static boolean isInternalProperty(PropertyDescriptor property) {
		String name = property.getPropertyName();

		// Filter standard internal properties
		if (name.equals("annotated") || name.equals(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)) {
			return true;
		}

		// Filter properties marked as @Hidden
		if (property.getAnnotation(Hidden.class) != null) {
			return true;
		}

		return false;
	}

	/**
	 * Validates and loads configuration interface classes.
	 */
	private static class ClassValidator {

		/**
		 * Loads a class and validates it's a ConfigurationItem interface.
		 *
		 * @param className
		 *        The fully-qualified class name to load.
		 * @return The loaded and validated class.
		 * @throws ClassNotFoundException
		 *         If the class cannot be found.
		 * @throws IllegalArgumentException
		 *         If the class is not a valid ConfigurationItem interface.
		 */
		static Class<?> loadAndValidate(String className) throws ClassNotFoundException, IllegalArgumentException {

			Class<?> clazz = Class.forName(className);

			// Must be an interface
			if (!clazz.isInterface()) {
				throw new IllegalArgumentException(
					"Not an interface: " + className + ". Expected a ConfigurationItem interface.");
			}

			// Must extend ConfigurationItem
			if (!ConfigurationItem.class.isAssignableFrom(clazz)) {
				throw new IllegalArgumentException(
					"Not a ConfigurationItem: " + className
						+ ". Expected an interface extending ConfigurationItem.");
			}

			return clazz;
		}
	}

	/**
	 * Serializes property definitions from ConfigurationDescriptor to data classes.
	 */
	private static class PropertyDefinitionSerializer {

		private static final int MAX_DEPTH = 3;

		/**
		 * Builds a property definition data object.
		 *
		 * @param property
		 *        The property descriptor to serialize.
		 * @param visitedDescriptors
		 *        Set of already-visited descriptor class names for circular reference detection.
		 * @param depth
		 *        Current recursion depth.
		 * @return PropertyDefinition data object.
		 */
		static PropertyDefinition buildPropertyDefinition(PropertyDescriptor property,
				Set<String> visitedDescriptors, int depth) {

			PropertyDefinition propDef = PropertyDefinition.create();

			// Basic metadata
			propDef.setName(property.getPropertyName());
			propDef.setKind(property.kind().name().toLowerCase());

			// Type information
			Class<?> type = property.getType();
			propDef.setType(type.getName());
			propDef.setTypeSimpleName(type.getSimpleName());

			// Element type for collections/arrays
			if (property.kind() == PropertyKind.LIST ||
				property.kind() == PropertyKind.ARRAY ||
				property.kind() == PropertyKind.MAP) {
				Class<?> elementType = property.getElementType();
				if (elementType != null) {
					propDef.setElementType(elementType.getName());
					propDef.setElementTypeSimpleName(elementType.getSimpleName());
				}
			}

			// Constraints
			propDef.setMandatory(property.isMandatory());
			propDef.setNullable(property.isNullable());
			propDef.setMultiple(property.isMultiple());
			propDef.setOrdered(property.isOrdered());

			// Default value
			propDef.setHasExplicitDefault(property.hasExplicitDefault());
			if (property.hasExplicitDefault()) {
				setDefaultValue(propDef, property);
			}

			// Enum values (if applicable)
			Class<?> enumType = getEnumType(property);
			if (enumType != null && enumType.isEnum()) {
				addEnumValues(propDef, enumType);
			}

			// Nested descriptor (for ITEM/COMPLEX types, with depth limit)
			if (depth < MAX_DEPTH && shouldExpandNestedType(property)) {
				addNestedDescriptor(propDef, property, visitedDescriptors, depth);
			}

			return propDef;
		}

		/**
		 * Gets the enum type for a property, checking both the property type and element type.
		 *
		 * @param property
		 *        The property descriptor.
		 * @return The enum type, or null if not an enum.
		 */
		private static Class<?> getEnumType(PropertyDescriptor property) {
			Class<?> type = property.getType();
			if (type.isEnum()) {
				return type;
			}

			// For collections, check element type
			if (property.kind() == PropertyKind.LIST ||
				property.kind() == PropertyKind.ARRAY ||
				property.kind() == PropertyKind.MAP) {
				Class<?> elementType = property.getElementType();
				if (elementType != null && elementType.isEnum()) {
					return elementType;
				}
			}

			return null;
		}

		/**
		 * Sets the default value of a property.
		 *
		 * @param propDef
		 *        The property definition to update.
		 * @param property
		 *        The property descriptor.
		 */
		private static void setDefaultValue(PropertyDefinition propDef, PropertyDescriptor property) {
			try {
				Object defaultValue = property.getDefaultValue();
				if (defaultValue == null) {
					propDef.setDefaultValue(null);
				} else if (defaultValue instanceof String) {
					propDef.setDefaultValue((String) defaultValue);
				} else if (defaultValue instanceof Number) {
					propDef.setDefaultValue(defaultValue.toString());
				} else if (defaultValue instanceof Boolean) {
					propDef.setDefaultValue(defaultValue.toString());
				} else if (defaultValue instanceof Enum) {
					propDef.setDefaultValue(((Enum<?>) defaultValue).name());
				} else {
					propDef.setDefaultValue(defaultValue.toString());
				}
			} catch (Exception ex) {
				// If default value cannot be retrieved, leave null
				propDef.setDefaultValue(null);
			}
		}

		/**
		 * Adds enum values to a property definition.
		 *
		 * @param propDef
		 *        The property definition to update.
		 * @param enumType
		 *        The enum class.
		 */
		private static void addEnumValues(PropertyDefinition propDef, Class<?> enumType) {
			Object[] constants = enumType.getEnumConstants();
			if (constants != null) {
				for (Object constant : constants) {
					propDef.addEnumValue(((Enum<?>) constant).name());
				}
			}
		}

		/**
		 * Checks if a property's nested type should be expanded.
		 *
		 * @param property
		 *        The property descriptor.
		 * @return True if the property should be expanded.
		 */
		private static boolean shouldExpandNestedType(PropertyDescriptor property) {
			PropertyKind kind = property.kind();

			// Expand ITEM and COMPLEX properties
			if (kind == PropertyKind.ITEM || kind == PropertyKind.COMPLEX) {
				return property.getValueDescriptor() != null;
			}

			// Expand LIST/MAP/ARRAY if element type is ConfigurationItem
			if (kind == PropertyKind.LIST || kind == PropertyKind.MAP || kind == PropertyKind.ARRAY) {
				Class<?> elementType = property.getElementType();
				return elementType != null &&
					ConfigurationItem.class.isAssignableFrom(elementType) &&
					property.getValueDescriptor() != null;
			}

			return false;
		}

		/**
		 * Adds nested descriptor information to a property definition.
		 *
		 * @param propDef
		 *        The property definition to update.
		 * @param property
		 *        The property descriptor.
		 * @param visitedDescriptors
		 *        Set of already-visited descriptor class names.
		 * @param depth
		 *        Current recursion depth.
		 */
		private static void addNestedDescriptor(PropertyDefinition propDef, PropertyDescriptor property,
				Set<String> visitedDescriptors, int depth) {

			ConfigurationDescriptor valueDescriptor = property.getValueDescriptor();
			if (valueDescriptor == null) {
				return;
			}

			// Circular reference detection
			String descriptorId = valueDescriptor.getConfigurationInterface().getName();
			if (visitedDescriptors.contains(descriptorId)) {
				// Skip circular references - don't add nested properties
				return;
			}

			// Add to visited set
			visitedDescriptors.add(descriptorId);

			try {
				for (PropertyDescriptor nestedProperty : valueDescriptor.getProperties()) {
					if (isInternalProperty(nestedProperty)) {
						continue;
					}
					PropertyDefinition nestedPropDef =
						buildPropertyDefinition(nestedProperty, visitedDescriptors, depth + 1);
					propDef.addNestedProperty(nestedPropDef);
				}
			} finally {
				// Remove from visited set (allow same type in different branches)
				visitedDescriptors.remove(descriptorId);
			}
		}
	}
}
