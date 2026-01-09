/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * MCP resource that provides all instances of a specific TopLogic type.
 *
 * <p>
 * This resource retrieves all objects of a given type from the TopLogic knowledge base and returns
 * their TIDs. This allows AI agents to quickly enumerate all instances of a particular type for
 * further processing or analysis.
 * </p>
 *
 * <p>
 * Resource URI: {@code toplogic://model/modules/{moduleName}/types/{typeName}/instances}
 * </p>
 *
 * <p>
 * The resource returns a JSON object containing:
 * </p>
 * <ul>
 * <li><code>module</code> - The module name</li>
 * <li><code>type</code> - The type name</li>
 * <li><code>instances</code> - An array of TIDs for all instances of this type</li>
 * <li><code>count</code> - The total number of instances</li>
 * </ul>
 *
 * @author jhu
 */
public class TypeInstancesResource {

	/** URI pattern for the type instances resource. */
	public static final String URI_PATTERN = "toplogic://model/modules/{moduleName}/types/{typeName}/instances";

	/** Resource name. */
	private static final String NAME = "type-instances";

	/** Resource description. */
	private static final String DESCRIPTION = "All instances of a specific TopLogic type";

	/** MIME type for JSON content. */
	private static final String MIME_TYPE = JsonResponseBuilder.JSON_MIME_TYPE;

	/**
	 * Creates the MCP resource template specification for retrieving type instances.
	 *
	 * @return The resource template specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncResourceTemplateSpecification createSpecification() {
		McpSchema.ResourceTemplate resource = McpSchema.ResourceTemplate.builder()
			.uriTemplate(URI_PATTERN)
			.name(NAME)
			.description(DESCRIPTION)
			.mimeType(MIME_TYPE)
			.build();

		return new McpServerFeatures.SyncResourceTemplateSpecification(
			resource,
			TypeInstancesResource::handleReadRequest
		);
	}

	/**
	 * Handles requests to read the type instances resource.
	 *
	 * <p>
	 * Sets up TopLogic thread context and delegates to {@link #readTypeInstances(String, String, McpSchema.ReadResourceRequest)}.
	 * </p>
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The read resource request containing the URI with module and type parameters.
	 * @return The resource content with the list of instance TIDs.
	 */
	private static McpSchema.ReadResourceResult handleReadRequest(
			McpSyncServerExchange exchange,
			McpSchema.ReadResourceRequest request) {

		// Extract module and type from URI
		String uri = request.uri();
		PathParameters params = extractPathParameters(uri);
		if (params == null) {
			return createErrorResult(request.uri(), "Invalid type instances URI: missing module or type parameters");
		}

		// Wrap database access in system interaction context
		return ThreadContextManager.inSystemInteraction(TypeInstancesResource.class,
			() -> readTypeInstances(params.moduleName, params.typeName, request));
	}

	/**
	 * Extracts module and type names from the resource URI.
	 *
	 * @param uri The resource URI in the format "toplogic://model/modules/{moduleName}/types/{typeName}/instances"
	 * @return PathParameters containing module and type names, or null if the URI format is invalid
	 */
	private static PathParameters extractPathParameters(String uri) {
		String prefix = "toplogic://model/modules/";
		if (!uri.startsWith(prefix)) {
			return null;
		}

		String remaining = uri.substring(prefix.length());
		String[] parts = remaining.split("/types/");
		if (parts.length != 2) {
			return null;
		}

		String moduleName = parts[0];
		String typeAndInstances = parts[1];
		if (!typeAndInstances.endsWith("/instances")) {
			return null;
		}

		String typeName = typeAndInstances.substring(0, typeAndInstances.length() - "/instances".length());

		return new PathParameters(moduleName, typeName);
	}

	/**
	 * Reads all instances of a specific type from the TopLogic knowledge base.
	 *
	 * <p>
	 * This method must be called within a TopLogic thread context (see {@link #handleReadRequest}).
	 * </p>
	 *
	 * @param moduleName The name of the module containing the type
	 * @param typeName The name of the type to retrieve instances for
	 * @param request The read resource request
	 * @return The resource content with the list of instance TIDs as JSON, or an error message
	 */
	private static McpSchema.ReadResourceResult readTypeInstances(String moduleName, String typeName,
			McpSchema.ReadResourceRequest request) {
		try {
			// Get the type using MetaElementUtil
			TLClass type = MetaElementUtil.getGlobalType(moduleName, typeName);
			if (type == null) {
				return createErrorResult(request.uri(),
					"Type '" + typeName + "' not found in module '" + moduleName + "'");
			}

			// Get all instances of this type
			List<TLObject> allInstances = MetaElementUtil.getAllInstancesOf(type, TLObject.class);

			// Build JSON response with all TIDs
			String json = buildInstancesJson(moduleName, typeName, allInstances);

			McpSchema.TextResourceContents contents = new McpSchema.TextResourceContents(
				request.uri(),
				MIME_TYPE,
				json
			);

			return new McpSchema.ReadResourceResult(List.of(contents));

		} catch (Exception ex) {
			return createErrorResult(request.uri(),
				"Error retrieving instances for type '" + typeName + "': " + ex.getMessage());
		}
	}

	/**
	 * Builds a JSON representation of all instances of a type.
	 *
	 * @param moduleName The module name
	 * @param typeName The type name
	 * @param instances List of all TLObject instances
	 * @return JSON string with all instance TIDs
	 */
	private static String buildInstancesJson(String moduleName, String typeName, List<TLObject> instances) {
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			// Basic information
			json.name("module").value(moduleName);
			json.name("type").value(typeName);
			json.name("count").value(instances.size());

			// Array of TIDs
			json.name("instances");
			json.beginArray();
			for (TLObject instance : instances) {
				json.value(instance.tId().asString());
			}
			json.endArray();

			json.endObject(); // root

		} catch (IOException ex) {
			throw new RuntimeException("Failed to generate JSON: " + ex.getMessage(), ex);
		}

		return buffer.toString();
	}

	/**
	 * Creates an error result.
	 *
	 * @param uri The original URI
	 * @param message The error message
	 * @return A ReadResourceResult containing the error message
	 */
	private static McpSchema.ReadResourceResult createErrorResult(String uri, String message) {
		String errorJson = JsonResponseBuilder.buildErrorJson(message);

		McpSchema.TextResourceContents contents = new McpSchema.TextResourceContents(
			uri,
			MIME_TYPE,
			errorJson
		);

		return new McpSchema.ReadResourceResult(List.of(contents));
	}

	/**
	 * Helper class to store extracted path parameters.
	 */
	private static class PathParameters {
		final String moduleName;
		final String typeName;

		PathParameters(String moduleName, String typeName) {
			this.moduleName = moduleName;
			this.typeName = typeName;
		}
	}
}