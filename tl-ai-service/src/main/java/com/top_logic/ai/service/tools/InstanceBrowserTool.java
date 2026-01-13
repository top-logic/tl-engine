/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.ai.service.tools.util.JsonToolResponseBuilder;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.util.Resources;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for accessing TopLogic instance data from the knowledge base.
 *
 * <p>
 * This tool provides two methods for working with instances:
 * <ul>
 * <li>{@link #listInstances(String, String, Integer, String)} - Lists all TIDs for a type</li>
 * <li>{@link #getInstance(String)} - Gets detailed info for a specific instance</li>
 * </ul>
 * </p>
 *
 * <p>
 * All methods return JSON-formatted strings for consumption by AI agents.
 * </p>
 */
public class InstanceBrowserTool {

	/** Default number of instances per page. */
	private static final int DEFAULT_LIMIT = 100;

	/** Maximum number of instances per page. */
	private static final int MAX_LIMIT = 1000;

	/**
	 * Lists instances of a specific type with pagination support.
	 *
	 * <p>
	 * Returns a JSON object containing paginated instance TIDs, total count, and a cursor for
	 * retrieving the next page. The cursor is an opaque string that should be passed to
	 * subsequent calls to retrieve more results.
	 * </p>
	 *
	 * @param moduleName
	 *        The module containing the type.
	 * @param typeName
	 *        The type name.
	 * @param limit
	 *        Maximum number of instances to return (default: 100, max: 1000).
	 * @param cursor
	 *        Pagination cursor from previous response (null/empty for first page).
	 * @return JSON object with paginated instance TIDs and pagination metadata, or error JSON if
	 *         type not found.
	 */
	@Tool("Get a paginated list of instances for a specific type. Use limit parameter to control page size (default: 100, max: 1000). Use cursor from previous response to get next page.")
	public String listInstances(
			@P("The module containing the type") String moduleName,
			@P("The name of the type") String typeName,
			@P("Maximum number of instances to return (default: 100, max: 1000)") Integer limit,
			@P("Cursor for pagination (null/empty for first page, use nextCursor from previous response)") String cursor) {

		return ThreadContextManager.inSystemInteraction(
			InstanceBrowserTool.class,
			() -> {
				try {
					// Validate parameters
					if (moduleName == null || moduleName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Module name is required");
					}
					if (typeName == null || typeName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Type name is required");
					}

					// Validate and normalize limit
					int effectiveLimit = limit != null ? limit : DEFAULT_LIMIT;
					if (effectiveLimit <= 0) {
						return JsonToolResponseBuilder.buildErrorJson("Limit must be positive, got: " + effectiveLimit);
					}
					if (effectiveLimit > MAX_LIMIT) {
						effectiveLimit = MAX_LIMIT; // Clamp to maximum
					}

					// Decode cursor to offset
					int offset;
					try {
						offset = PaginationCursor.decode(cursor);
					} catch (IllegalArgumentException ex) {
						return JsonToolResponseBuilder.buildErrorJson("Invalid cursor: " + ex.getMessage());
					}

					// Get the type using MetaElementUtil
					TLClass type = MetaElementUtil.getGlobalType(moduleName, typeName);
					if (type == null) {
						return JsonToolResponseBuilder.buildErrorJson(
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
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginObject();

						// Basic information
						json.name("module").value(moduleName);
						json.name("type").value(typeName);
						json.name("totalCount").value(totalCount);
						json.name("limit").value(effectiveLimit);
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

						json.endObject();
					}

					String result = buffer.toString();
					Logger.info("listInstances() result: " + result, InstanceBrowserTool.class);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to list instances: " + ex.getMessage());
					Logger.error("listInstances() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
					System.err.println("InstanceBrowserTool.listInstances() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error accessing instances: " + ex.getMessage());
					Logger.error("listInstances() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
					System.err.println("InstanceBrowserTool.listInstances() error: " + error);
					return error;
				}
			});
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

	/**
	 * Gets detailed information about a single instance by its TID.
	 *
	 * <p>
	 * Returns a JSON object containing the instance's TID, type information, all attribute
	 * values, and metadata (container, transient status). References to other objects are
	 * represented as structured objects with TIDs.
	 * </p>
	 *
	 * @param tid
	 *        The TopLogic identifier in format "TypeName:id".
	 * @return JSON object with instance details, or error JSON if instance not found.
	 */
	@Tool("Get detailed information about a single instance by its TID including all attributes and values")
	public String getInstance(@P("The TopLogic identifier (TID) in format 'TypeName:id'") String tid) {

		return ThreadContextManager.inSystemInteraction(
			InstanceBrowserTool.class,
			() -> {
				try {
					// Validate parameter
					if (tid == null || tid.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("TID is required");
					}

					// Parse TID (format: "TypeName:hexId")
					String[] split = tid.split(":", 2);
					if (split.length != 2) {
						return JsonToolResponseBuilder.buildErrorJson(
							"Invalid TID format. Expected 'TypeName:id', got: " + tid);
					}

					String typeName = split[0];
					String id = split[1];

					// Convert string ID to TLID
					TLID tlid = IdentifierUtil.fromExternalForm(id);

					// Get the knowledge base
					KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

					// Get the knowledge object
					KnowledgeObject ko = kb.getKnowledgeObject(typeName, tlid);

					if (ko == null) {
						return JsonToolResponseBuilder.buildErrorJson("Object with TID '" + tid + "' not found");
					}

					// Wrap it as a TLObject
					TLObject tlObject = WrapperFactory.getWrapper(ko);

					// Build JSON response with all object information
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginObject();

						// Basic object information
						json.name("tid").value(tid);

						// Type information
						TLStructuredType type = tlObject.tType();
						if (type != null) {
							json.name("type");
							writeTypeInfo(json, type);
						}

						// Attributes
						json.name("attributes");
						json.beginObject();

						if (type != null) {
							// Get all parts (attributes) of this type
							for (TLStructuredTypePart part : type.getAllParts()) {
								String partName = part.getName();
								Object value = tlObject.tValue(part);

								// Write attribute value
								writeAttributeValue(json, partName, value);
							}
						}

						json.endObject(); // attributes

						// Additional metadata
						json.name("metadata");
						json.beginObject();
						json.name("container").value(
							tlObject.tContainer() != null ? tlObject.tContainer().tId().toString() : null);
						json.name("containerReference").value(
							tlObject.tContainerReference() != null ? tlObject.tContainerReference().getName() : null);
						json.name("transient").value(tlObject.tTransient());
						json.endObject(); // metadata

						json.endObject(); // root
					}

					String result = buffer.toString();
					Logger.info("getInstance() result: " + result, InstanceBrowserTool.class);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to get instance: " + ex.getMessage());
					Logger.error("getInstance() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
					System.err.println("InstanceBrowserTool.getInstance() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error retrieving instance: " + ex.getMessage());
					Logger.error("getInstance() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
					System.err.println("InstanceBrowserTool.getInstance() error: " + error);
					return error;
				}
			});
	}

	/**
	 * Writes type information to JSON output.
	 */
	private static void writeTypeInfo(JsonWriter json, TLStructuredType type) throws IOException {
		json.beginObject();
		json.name("name").value(type.getName());

		// Module information
		TLModule module = type.getModule();
		if (module != null) {
			json.name("module").value(module.getName());

			// Module label if available
			ResKey moduleKey = LabelVisitor.getModuleResourceKey(module);
			Resources resources = Resources.getInstance();
			String moduleLabel = resources.getString(moduleKey, null);
			if (moduleLabel != null && !moduleLabel.isEmpty()) {
				json.name("moduleLabel").value(moduleLabel);
			}
		}

		// Type label if available
		ResKey typeKey = TLModelNamingConvention.getTypeLabelKey(type);
		Resources resources = Resources.getInstance();
		String typeLabel = resources.getString(typeKey, null);
		if (typeLabel != null && !typeLabel.isEmpty()) {
			json.name("label").value(typeLabel);
		}

		// Model kind
		ModelKind kind = type.getModelKind();
		if (kind != null) {
			json.name("kind").value(kind.name().toLowerCase());
		}

		json.endObject();
	}

	/**
	 * Writes an attribute value to JSON output with proper type handling.
	 */
	private static void writeAttributeValue(JsonWriter json, String name, Object value) throws IOException {
		json.name(name);

		if (value == null) {
			json.nullValue();
		} else if (value instanceof TLObject) {
			// For reference to another TLObject, use the utility method
			JsonToolResponseBuilder.writeTLObjectReference(json, (TLObject) value);
		} else if (value instanceof Iterable) {
			// For collections, write as JSON array
			json.beginArray();
			for (Object item : (Iterable<?>) value) {
				if (item instanceof TLObject) {
					JsonToolResponseBuilder.writeTLObjectReference(json, (TLObject) item);
				} else {
					json.value(item != null ? item.toString() : null);
				}
			}
			json.endArray();
		} else {
			// For primitive values, write as string
			json.value(value.toString());
		}
	}
}
