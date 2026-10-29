/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.resources;

import java.util.List;
import java.util.Map;

import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.ai.mcp.server.UriPattern;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder.SchemaResolver;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.Schema;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * MCP resource that provides JSON Schema definitions for TopLogic configuration types.
 *
 * <p>
 * This resource allows MCP clients to incrementally resolve configuration type schemas. Each
 * schema uses external {@code $ref} references to other configuration types, which can be resolved
 * by fetching the corresponding resource URI.
 * </p>
 *
 * <p>
 * The resource supports both:
 * </p>
 * <ul>
 * <li>Configuration interfaces (extending {@link ConfigurationItem})</li>
 * <li>Implementation classes (classes with a configuration constructor)</li>
 * </ul>
 *
 * <p>
 * The resource URI pattern is {@code toplogic://schema/config/{typeName}}, where
 * {@code {typeName}} is the encoded fully-qualified class name. Inner classes use {@code /} as
 * separator instead of {@code $}.
 * </p>
 *
 * <p>
 * For example:
 * <ul>
 * <li>{@code toplogic://schema/config/com.top_logic.ai.mcp.server.MCPServerService/Config}</li>
 * <li>{@code toplogic://schema/config/com.top_logic.ai.mcp.server.MCPServerService}</li>
 * </ul>
 * </p>
 */
public class ConfigSchemaResource {

	/**
	 * The URI prefix for configuration schema references.
	 */
	public static final String SCHEMA_URI_PREFIX = "toplogic://schema/config/";

	/** Parameter name for type name in URI template. */
	private static final String PARAM_TYPE_NAME = "typeName";

	/** URI template for configuration schema resource. */
	public static final String URI_TEMPLATE = SCHEMA_URI_PREFIX + "{" + PARAM_TYPE_NAME + "}";

	/**
	 * Pattern for extracting type name from URI.
	 */
	private static final UriPattern URI_PATTERN = UriPattern.compile(URI_TEMPLATE);

	/** Resource name template. */
	private static final String NAME_TEMPLATE = "config-schema-{" + PARAM_TYPE_NAME + "}";

	/** Resource description. */
	private static final String DESCRIPTION =
		"JSON Schema for a TopLogic configuration type. Accepts configuration interfaces or implementation classes. Nested types are referenced via $ref URIs that can be resolved through this same resource.";

	/** MIME type for JSON content. */
	private static final String MIME_TYPE = "application/schema+json";

	/**
	 * Creates the MCP resource template specification for configuration schemas.
	 *
	 * <p>
	 * This creates a resource template that can handle requests for any configuration type's
	 * schema by extracting the type name from the URI.
	 * </p>
	 *
	 * @return The resource template specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncResourceTemplateSpecification createSpecification() {
		McpSchema.ResourceTemplate template = McpSchema.ResourceTemplate.builder()
			.uriTemplate(URI_TEMPLATE)
			.name(NAME_TEMPLATE)
			.description(DESCRIPTION)
			.mimeType(MIME_TYPE)
			.build();

		return new McpServerFeatures.SyncResourceTemplateSpecification(
			template,
			ConfigSchemaResource::handleReadRequest
		);
	}

	/**
	 * Handles requests to read a configuration schema resource.
	 *
	 * <p>
	 * Establishes the calling account's TopLogic context and delegates to {@link #readConfigSchema(McpSchema.ReadResourceRequest)}.
	 * </p>
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The read resource request containing the URI with the type name.
	 * @return The resource content with the JSON schema.
	 */
	private static McpSchema.ReadResourceResult handleReadRequest(
			McpSyncServerExchange exchange,
			McpSchema.ReadResourceRequest request) {

		// Read as the account this request was authenticated as, not in system context.
		return McpCallerContext.inCallerContext(() -> readConfigSchema(request));
	}

	/**
	 * Reads the JSON Schema for a specific configuration type.
	 *
	 * <p>
	 * This method must be called within a TopLogic thread context (see {@link #handleReadRequest}).
	 * </p>
	 *
	 * @param request
	 *        The read resource request containing the URI with the type name.
	 * @return The resource content with the JSON schema.
	 */
	private static McpSchema.ReadResourceResult readConfigSchema(McpSchema.ReadResourceRequest request) {
		// Extract type name from URI
		String uri = request.uri();
		Map<String, String> parameters = URI_PATTERN.extractParameters(uri);
		String encodedTypeName = parameters.get(PARAM_TYPE_NAME);

		try {
			// Load the class using the encoded name resolver
			Class<?> targetClass = JsonConfigSchemaBuilder.loadEncodedClass(encodedTypeName);

			Schema schema;

			if (ConfigurationItem.class.isAssignableFrom(targetClass)) {
				// It's a configuration interface
				schema = buildConfigInterfaceSchema(targetClass);
			} else {
				// Try to treat it as an implementation class
				schema = buildImplementationClassSchema(targetClass);
			}

			// Add $id to identify this schema
			schema.setId(uri);

			// Serialize to JSON
			String jsonContent = JsonSchemaWriter.toJson(schema, true);

			McpSchema.TextResourceContents contents = new McpSchema.TextResourceContents(
				uri,
				MIME_TYPE,
				jsonContent
			);

			return new McpSchema.ReadResourceResult(List.of(contents));

		} catch (ClassNotFoundException ex) {
			Logger.warn("Class not found for schema: " + encodedTypeName, ex, ConfigSchemaResource.class);
			throw new IllegalArgumentException("Class not found: " + encodedTypeName, ex);
		} catch (Exception ex) {
			Logger.error("Failed to generate schema for: " + encodedTypeName, ex, ConfigSchemaResource.class);
			throw new RuntimeException("Failed to generate schema: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Builds a schema for a configuration interface.
	 */
	private static Schema buildConfigInterfaceSchema(Class<?> configClass) {
		@SuppressWarnings("unchecked")
		Class<? extends ConfigurationItem> configItemClass = (Class<? extends ConfigurationItem>) configClass;

		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder();
		builder.setSchemaResolver(new ExternalSchemaResolver());

		return builder.buildConfigSchema(TypedConfiguration.getConfigurationDescriptor(configItemClass));
	}

	/**
	 * Builds a schema for an implementation class.
	 */
	private static Schema buildImplementationClassSchema(Class<?> implClass) {
		// Verify this is actually an implementation class with a configuration constructor
		try {
			DefaultConfigConstructorScheme.getFactory(implClass);
		} catch (Exception ex) {
			throw new IllegalArgumentException(
				"Class '" + implClass.getName() + "' is neither a ConfigurationItem interface "
					+ "nor an implementation class with a configuration constructor: " + ex.getMessage(),
				ex);
		}

		JsonConfigSchemaBuilder builder = new JsonConfigSchemaBuilder();
		builder.setSchemaResolver(new ExternalSchemaResolver());

		return builder.buildImplementationSchema(implClass);
	}

	/**
	 * Builds the schema URI for a type.
	 */
	private static String buildSchemaUri(Class<?> type) {
		return SCHEMA_URI_PREFIX + JsonConfigSchemaBuilder.encodeTypeName(type.getName());
	}

	/**
	 * Schema resolver that generates external URIs for nested configuration types.
	 */
	private static class ExternalSchemaResolver implements SchemaResolver {

		@Override
		public String resolveConfigType(com.top_logic.basic.config.ConfigurationDescriptor descriptor) {
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

}
