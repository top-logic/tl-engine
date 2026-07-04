/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.config;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder.SchemaResolver;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.json.JsonEscape;

/**
 * Tool for retrieving JSON Schema definitions for TopLogic configuration types.
 *
 * <p>
 * This tool allows AI agents to query the structure of configuration interfaces and implementation
 * classes used in TopLogic applications. The schemas are generated according to JSON Schema
 * 2020-12 specification and use external references for nested types, enabling incremental
 * resolution of large type hierarchies.
 * </p>
 *
 * <p>
 * The tool accepts:
 * </p>
 * <ul>
 * <li>Configuration interfaces (extending {@link ConfigurationItem})</li>
 * <li>Implementation classes (classes with a configuration constructor)</li>
 * <li>Layout template URIs ({@code toplogic://schema/layout-template/{name}})</li>
 * </ul>
 */
public class ConfigSchemaTool {

	/**
	 * The URI prefix for configuration schema references.
	 */
	public static final String SCHEMA_URI_PREFIX = "toplogic://schema/config/";

	/**
	 * The URI prefix for layout template schema references.
	 */
	public static final String LAYOUT_TEMPLATE_SCHEMA_URI_PREFIX = "toplogic://schema/layout-template/";

	/**
	 * Retrieves the JSON Schema for a TopLogic configuration type.
	 *
	 * <p>
	 * The schema describes the structure and constraints of the configuration, including property
	 * types, required fields, and nested configurations. Nested types are referenced via external
	 * URIs ({@code toplogic://schema/config/...}) that can be resolved by calling this tool again
	 * with the URI.
	 * </p>
	 *
	 * @param typeReference
	 *        Either a schema URI (e.g., {@code toplogic://schema/config/com.example.Service/Config}
	 *        or {@code toplogic://schema/layout-template/{name}}) or a fully-qualified class name
	 *        (e.g., {@code com.example.Service$Config}). In config URIs, inner classes use
	 *        {@code /} as separator instead of {@code $}.
	 * @return The JSON Schema as a string, or an error message if the class cannot be found or is
	 *         not a valid configuration type.
	 */
	@ToolMethod(description = "Get JSON Schema for a TopLogic configuration type or layout template. Accepts config schema URIs (toplogic://schema/config/...), layout template URIs (toplogic://schema/layout-template/{name}), or class names. Returns the schema with $ref URIs for nested types that can be resolved by calling this tool again.", mode = ToolMode.READ)
	public String getConfigSchema(
			@P("Schema URI (toplogic://schema/config/... or toplogic://schema/layout-template/{name}) or class name") String typeReference) {

		try {
			// Handle layout template schema URIs
			if (typeReference.startsWith(LAYOUT_TEMPLATE_SCHEMA_URI_PREFIX)) {
				return buildLayoutTemplateSchema(typeReference);
			}

			// Extract class name from URI if needed
			String className = extractClassName(typeReference);

			// Load the class using the encoded name resolver
			Class<?> targetClass = JsonConfigSchemaBuilder.loadEncodedClass(className);

			Schema schema;
			String schemaId;

			if (ConfigurationItem.class.isAssignableFrom(targetClass)) {
				// It's a configuration interface
				schema = buildConfigInterfaceSchema(targetClass);
				schemaId = buildSchemaUri(targetClass);
			} else {
				// Try to treat it as an implementation class
				schema = buildImplementationClassSchema(targetClass);
				schemaId = buildSchemaUri(targetClass);
			}

			// Add $id to identify this schema
			schema.setId(schemaId);

			// Serialize to JSON
			return JsonSchemaWriter.toJson(schema, true);

		} catch (ClassNotFoundException ex) {
			String error = "Class not found: " + typeReference;
			Logger.warn(error, ex, ConfigSchemaTool.class);
			return buildErrorJson(error);
		} catch (Exception ex) {
			String error = "Failed to generate schema: " + ex.getMessage();
			Logger.error(error, ex, ConfigSchemaTool.class);
			return buildErrorJson(error);
		}
	}

	/**
	 * Extracts the class name from a type reference.
	 *
	 * <p>
	 * Accepts both URI format ({@code toplogic://schema/config/com.example.Type}) and plain class
	 * names ({@code com.example.Type}).
	 * </p>
	 */
	private String extractClassName(String typeReference) {
		String uriPrefix = SCHEMA_URI_PREFIX;
		if (typeReference.startsWith(uriPrefix)) {
			return typeReference.substring(uriPrefix.length());
		}
		return typeReference;
	}

	/**
	 * Builds a schema for a configuration interface.
	 */
	private Schema buildConfigInterfaceSchema(Class<?> configClass) {
		@SuppressWarnings("unchecked")
		Class<? extends ConfigurationItem> configItemClass = (Class<? extends ConfigurationItem>) configClass;

		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder().setInline(true);
		builder.setSchemaResolver(new ExternalSchemaResolver());

		return builder.buildConfigSchema(TypedConfiguration.getConfigurationDescriptor(configItemClass));
	}

	/**
	 * Builds a schema for an implementation class.
	 */
	private Schema buildImplementationClassSchema(Class<?> implClass) {
		// Verify this is actually an implementation class with a configuration constructor
		try {
			DefaultConfigConstructorScheme.getFactory(implClass);
		} catch (Exception ex) {
			throw new IllegalArgumentException(
				"Class '" + implClass.getName() + "' is neither a ConfigurationItem interface "
					+ "nor an implementation class with a configuration constructor: " + ex.getMessage(),
				ex);
		}

		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder().setInline(true);
		builder.setSchemaResolver(new ExternalSchemaResolver());

		return builder.buildImplementationSchema(implClass);
	}

	/**
	 * Builds a JSON Schema for a layout template's parameter descriptor.
	 *
	 * @param typeReference
	 *        The full layout template schema URI.
	 * @return The JSON Schema as a string.
	 */
	private String buildLayoutTemplateSchema(String typeReference) {
		String templateName = typeReference.substring(LAYOUT_TEMPLATE_SCHEMA_URI_PREFIX.length());

		DynamicComponentDefinition definition =
			DynamicComponentService.getInstance().getComponentDefinition(templateName);

		if (definition == null) {
			return buildErrorJson("Layout template not found: " + templateName);
		}

		ConfigurationDescriptor templateDescriptor = definition.descriptor();

		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder() {
			@Override
			protected String resolveTypeId(
					ConfigurationDescriptor itemDescriptor,
					Class<?> configInterface) {
				if (itemDescriptor == templateDescriptor) {
					return typeReference;
				}
				return super.resolveTypeId(itemDescriptor, configInterface);
			}
		}.setInline(true);
		builder.setSchemaResolver(new ExternalSchemaResolver());

		Schema schema = builder.buildConfigSchema(templateDescriptor);
		schema.setId(typeReference);

		return JsonSchemaWriter.toJson(schema, true);
	}

	/**
	 * Builds the schema URI for a type.
	 */
	static String buildSchemaUri(Class<?> type) {
		return SCHEMA_URI_PREFIX + JsonConfigSchemaBuilder.encodeTypeName(type.getName());
	}

	/**
	 * Schema resolver that generates external URIs for nested configuration types.
	 */
	static class ExternalSchemaResolver implements SchemaResolver {

		@Override
		public String resolveConfigType(ConfigurationDescriptor descriptor) {
			Class<?> configType = descriptor.getConfigurationInterface();
			// Skip the base ConfigurationItem interface
			if (configType == ConfigurationItem.class) {
				return null;
			}
			return buildSchemaUri(configType);
		}

		@Override
		public String resolveImplementationType(Class<?> implType) {
			return buildSchemaUri(implType);
		}

	}

	/**
	 * Builds an error JSON response.
	 *
	 * @param errorMessage
	 *        The error message.
	 * @return JSON string containing the error.
	 */
	private static String buildErrorJson(String errorMessage) {
		// Simple JSON error format
		return "{\"error\": \"" + JsonEscape.escape(errorMessage) + "\"}";
	}

}
