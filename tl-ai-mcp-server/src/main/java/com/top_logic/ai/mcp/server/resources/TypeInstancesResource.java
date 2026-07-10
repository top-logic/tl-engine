/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

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
 * MCP resource that provides paginated instances of a specific TopLogic type.
 *
 * <p>
 * This resource retrieves objects of a given type from the TopLogic knowledge base and returns
 * their TIDs with pagination support. This allows AI agents to efficiently enumerate instances
 * of a particular type without overwhelming memory or network bandwidth.
 * </p>
 *
 * <p>
 * Resource URI: {@code toplogic://model/modules/{moduleName}/types/{typeName}/instances}
 * </p>
 *
 * <p>
 * Query Parameters:
 * </p>
 * <ul>
 * <li><code>limit</code> - Maximum number of instances per page (default: 100, max: 1000)</li>
 * <li><code>cursor</code> - Pagination cursor from previous response (omit for first page)</li>
 * </ul>
 *
 * <p>
 * The resource returns a JSON object containing:
 * </p>
 * <ul>
 * <li><code>module</code> - The module name</li>
 * <li><code>type</code> - The type name</li>
 * <li><code>totalCount</code> - The total number of instances</li>
 * <li><code>limit</code> - The page size used</li>
 * <li><code>offset</code> - The starting offset for this page</li>
 * <li><code>instances</code> - An array of TIDs for instances on this page</li>
 * <li><code>nextCursor</code> - Cursor for next page (null if last page)</li>
 * <li><code>hasMore</code> - Boolean indicating if more pages exist</li>
 * </ul>
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

	/** Default number of instances per page. */
	private static final int DEFAULT_LIMIT = 100;

	/** Maximum number of instances per page. */
	private static final int MAX_LIMIT = 1000;

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
	 * Sets up TopLogic thread context and delegates to {@link #readTypeInstances}.
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

		// Strip query string before path matching
		String baseUri = uri;
		if (uri.contains("?")) {
			baseUri = uri.substring(0, uri.indexOf('?'));
		}

		PathParameters params = extractPathParameters(baseUri);
		if (params == null) {
			return createErrorResult(request.uri(), "Invalid type instances URI: missing module or type parameters");
		}

		// Extract query parameters
		QueryParams queryParams = extractQueryParameters(uri);

		// Wrap database access in system interaction context
		return ThreadContextManager.inSystemInteraction(TypeInstancesResource.class,
			() -> readTypeInstances(params.moduleName, params.typeName, queryParams, request));
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
	 * Extracts query parameters (limit and cursor) from the resource URI.
	 *
	 * @param uri
	 *        The resource URI which may contain query parameters.
	 * @return QueryParams with limit and cursor values (using defaults if not specified).
	 */
	private static QueryParams extractQueryParameters(String uri) {
		Integer limit = null;
		String cursor = null;

		// Extract query string
		int queryStart = uri.indexOf('?');
		if (queryStart >= 0 && queryStart < uri.length() - 1) {
			String queryString = uri.substring(queryStart + 1);

			// Parse query parameters
			String[] params = queryString.split("&");
			for (String param : params) {
				String[] keyValue = param.split("=", 2);
				if (keyValue.length == 2) {
					String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
					String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);

					if ("limit".equals(key)) {
						try {
							limit = Integer.parseInt(value);
						} catch (NumberFormatException ex) {
							// Ignore invalid limit, will use default
						}
					} else if ("cursor".equals(key)) {
						cursor = value;
					}
				}
			}
		}

		return new QueryParams(limit, cursor);
	}

	/**
	 * Reads instances of a specific type from the TopLogic knowledge base with pagination.
	 *
	 * <p>
	 * This method must be called within a TopLogic thread context (see {@link #handleReadRequest}).
	 * </p>
	 *
	 * @param moduleName
	 *        The name of the module containing the type.
	 * @param typeName
	 *        The name of the type to retrieve instances for.
	 * @param queryParams
	 *        The query parameters for pagination (limit and cursor).
	 * @param request
	 *        The read resource request.
	 * @return The resource content with paginated instance TIDs as JSON, or an error message.
	 */
	private static McpSchema.ReadResourceResult readTypeInstances(String moduleName, String typeName,
			QueryParams queryParams, McpSchema.ReadResourceRequest request) {
		try {
			// Validate and normalize limit
			int effectiveLimit = queryParams.limit != null ? queryParams.limit : DEFAULT_LIMIT;
			if (effectiveLimit <= 0) {
				return createErrorResult(request.uri(), "Limit must be positive, got: " + effectiveLimit);
			}
			if (effectiveLimit > MAX_LIMIT) {
				effectiveLimit = MAX_LIMIT; // Clamp to maximum
			}

			// Decode cursor to offset
			int offset;
			try {
				offset = PaginationCursor.decode(queryParams.cursor);
			} catch (IllegalArgumentException ex) {
				return createErrorResult(request.uri(), "Invalid cursor: " + ex.getMessage());
			}

			// Get the type using MetaElementUtil
			TLClass type = MetaElementUtil.getGlobalType(moduleName, typeName);
			if (type == null) {
				return createErrorResult(request.uri(),
					"Type '" + typeName + "' not found in module '" + moduleName + "'");
			}

			// Get all instances of this type
			List<TLObject> allInstances = MetaElementUtil.getAllInstancesOf(type, TLObject.class);
			int totalCount = allInstances.size();

			// Apply pagination
			List<TLObject> pageInstances = allInstances.stream()
				.skip(offset)
				.limit(effectiveLimit)
				.collect(Collectors.toList());

			// Calculate next cursor
			int nextOffset = offset + pageInstances.size();
			String nextCursor = (nextOffset < totalCount) ? PaginationCursor.encode(nextOffset) : null;
			boolean hasMore = nextOffset < totalCount;

			// Build JSON response with paginated TIDs
			String json = buildInstancesJson(moduleName, typeName, totalCount, effectiveLimit, offset,
				pageInstances, nextCursor, hasMore);

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
	 * Builds a JSON representation of paginated instances of a type.
	 *
	 * @param moduleName
	 *        The module name.
	 * @param typeName
	 *        The type name.
	 * @param totalCount
	 *        The total number of instances.
	 * @param limit
	 *        The page size.
	 * @param offset
	 *        The starting offset.
	 * @param pageInstances
	 *        List of TLObject instances for this page.
	 * @param nextCursor
	 *        Cursor for next page (null if last page).
	 * @param hasMore
	 *        Whether more pages exist.
	 * @return JSON string with paginated instance TIDs and metadata.
	 */
	private static String buildInstancesJson(String moduleName, String typeName, int totalCount, int limit,
			int offset, List<TLObject> pageInstances, String nextCursor, boolean hasMore) {
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			// Basic information
			json.name("module").value(moduleName);
			json.name("type").value(typeName);
			json.name("totalCount").value(totalCount);
			json.name("limit").value(limit);
			json.name("offset").value(offset);

			// Array of TIDs for this page
			json.name("instances");
			json.beginArray();
			for (TLObject instance : pageInstances) {
				json.value(instance.tId().asString());
			}
			json.endArray();

			// Pagination metadata
			if (nextCursor != null) {
				json.name("nextCursor").value(nextCursor);
			} else {
				json.name("nextCursor").nullValue();
			}
			json.name("hasMore").value(hasMore);

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

	/**
	 * Helper class to store extracted query parameters.
	 */
	private static class QueryParams {

		final Integer limit;

		final String cursor;

		QueryParams(Integer limit, String cursor) {
			this.limit = limit;
			this.cursor = cursor;
		}
	}

	/**
	 * Utility class for encoding and decoding pagination cursors.
	 */
	private static final class PaginationCursor {

		/**
		 * Encodes an offset into a Base64 cursor string.
		 *
		 * @param offset
		 *        The offset to encode (must be non-negative).
		 * @return Base64-encoded cursor string.
		 * @throws IllegalArgumentException
		 *         if offset is negative.
		 */
		static String encode(int offset) {
			if (offset < 0) {
				throw new IllegalArgumentException("Offset must be non-negative: " + offset);
			}
			return Base64.getEncoder().encodeToString(
				String.valueOf(offset).getBytes(StandardCharsets.UTF_8)
			);
		}

		/**
		 * Decodes a Base64 cursor string into an offset.
		 *
		 * @param cursor
		 *        The cursor to decode (null or empty means offset 0).
		 * @return The decoded offset.
		 * @throws IllegalArgumentException
		 *         if cursor format is invalid or offset is negative.
		 */
		static int decode(String cursor) {
			if (cursor == null || cursor.isEmpty()) {
				return 0;
			}
			try {
				byte[] decoded = Base64.getDecoder().decode(cursor);
				int offset = Integer.parseInt(new String(decoded, StandardCharsets.UTF_8));
				if (offset < 0) {
					throw new IllegalArgumentException("Decoded offset is negative: " + offset);
				}
				return offset;
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Invalid cursor format: " + cursor, ex);
			}
		}
	}
}