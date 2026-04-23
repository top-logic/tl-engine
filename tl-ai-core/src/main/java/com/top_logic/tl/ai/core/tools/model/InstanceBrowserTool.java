/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

import com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceTypeInfo;
import com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;
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
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.util.Resources;

import com.top_logic.tl.ai.core.tools.P;
import com.top_logic.tl.ai.core.tools.ToolMethod;
import com.top_logic.tl.ai.core.tools.ToolMode;

/**
 * Tool for accessing TopLogic instance data from the knowledge base.
 *
 * <p>
 * This tool provides methods for listing instances of a type with pagination and retrieving
 * detailed information about individual instances. All methods return JSON-formatted strings for
 * consumption by AI agents.
 * </p>
 *
 * @see #listInstances(String, String, Integer, String)
 * @see #getInstance(String)
 */
public class InstanceBrowserTool {

	/**
	 * Configuration for {@link InstanceBrowserTool}.
	 */
	public interface Config extends PolymorphicConfiguration<InstanceBrowserTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link InstanceBrowserTool}.
	 */
	public InstanceBrowserTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link InstanceBrowserTool} from configuration.
	 */
	@CalledByReflection
	public InstanceBrowserTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

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
	@ToolMethod(description = "Get a paginated list of instances for a specific type. Use limit parameter to control page size (default: 100, max: 1000). Use cursor from previous response to get next page.", mode = ToolMode.READ)
	public String listInstances(
			@P("The module containing the type") String moduleName,
			@P("The name of the type") String typeName,
			@P("Maximum number of instances to return (default: 100, max: 1000)") Integer limit,
			@P("Cursor for pagination (null/empty for first page, use nextCursor from previous response)") String cursor) {

		try {
			// System interaction returns data class
			InstanceListResponse response = ThreadContextManager.inSystemInteraction(
				InstanceBrowserTool.class,
				() -> {
					// Validate parameters
					if (moduleName == null || moduleName.isEmpty()) {
						throw new IllegalArgumentException("Module name is required");
					}
					if (typeName == null || typeName.isEmpty()) {
						throw new IllegalArgumentException("Type name is required");
					}

					// Validate and normalize limit
					int effectiveLimit = limit != null ? limit : DEFAULT_LIMIT;
					if (effectiveLimit <= 0) {
						throw new IllegalArgumentException("Limit must be positive, got: " + effectiveLimit);
					}
					if (effectiveLimit > MAX_LIMIT) {
						effectiveLimit = MAX_LIMIT; // Clamp to maximum
					}

					// Decode cursor to offset
					int offset = PaginationCursor.decode(cursor);

					// Get the type using MetaElementUtil
					TLClass type = MetaElementUtil.getGlobalType(moduleName, typeName);
					if (type == null) {
						throw new IllegalArgumentException(
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

					// Build response using data class
					InstanceListResponse result = InstanceListResponse.create();
					result.setModule(moduleName);
					result.setType(typeName);
					result.setTotalCount(totalCount);
					result.setLimit(effectiveLimit);
					result.setOffset(offset);

					// Add TIDs for this page
					for (TLObject instance : pageInstances) {
						result.addInstance(instance.tId().asString());
					}

					// Pagination metadata
					result.setNextCursor(nextCursor);
					result.setHasMore(hasMore);

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("moduleName", moduleName);
			args.put("typeName", typeName);
			args.put("limit", limit);
			args.put("cursor", cursor);
			return DataClassSerializer.toJson(response, InstanceBrowserTool.class, "listInstances()", args);

		} catch (Exception ex) {
			Logger.error("listInstances() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("moduleName", moduleName);
			args.put("typeName", typeName);
			args.put("limit", limit);
			args.put("cursor", cursor);
			return DataClassSerializer.errorToJson(
				"Error accessing instances: " + ex.getMessage(),
				InstanceBrowserTool.class,
				"listInstances()",
				args);
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
	@ToolMethod(description = "Get detailed information about a single instance by its TID including all attributes and values", mode = ToolMode.READ)
	public String getInstance(@P("The TopLogic identifier (TID) in format 'TypeName:id'") String tid) {

		try {
			// System interaction returns data class
			InstanceResponse response = ThreadContextManager.inSystemInteraction(
				InstanceBrowserTool.class,
				() -> {
					// Validate parameter
					if (tid == null || tid.isEmpty()) {
						throw new IllegalArgumentException("TID is required");
					}

					// Parse TID (format: "type-name:hex-id")
					String[] split = tid.split(":", 2);
					if (split.length != 2) {
						throw new IllegalArgumentException(
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
						throw new IllegalArgumentException("Object with TID '" + tid + "' not found");
					}

					// Wrap it as a TLObject
					TLObject tlObject = WrapperFactory.getWrapper(ko);

					// Build response using data class
					InstanceResponse result = InstanceResponse.create();
					result.setTid(tid);

					// Type information
					TLStructuredType type = tlObject.tType();
					if (type != null) {
						result.setType(buildTypeInfo(type));

						// Get all parts (attributes) of this type
						for (TLStructuredTypePart part : type.getAllParts()) {
							String partName = part.getName();
							Object value = tlObject.tValue(part);

							// Add attribute value as JSON string
							result.putAttribute(partName, serializeAttributeValue(value));
						}
					}

					// Additional metadata
					InstanceMetadata metadata = InstanceMetadata.create();
					metadata.setContainer(
						tlObject.tContainer() != null ? tlObject.tContainer().tId().toString() : null);
					metadata.setContainerReference(
						tlObject.tContainerReference() != null ? tlObject.tContainerReference().getName() : null);
					metadata.setTransient(tlObject.tTransient());
					result.setMetadata(metadata);

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("tid", tid);
			return DataClassSerializer.toJson(response, InstanceBrowserTool.class, "getInstance()", args);

		} catch (Exception ex) {
			Logger.error("getInstance() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("tid", tid);
			return DataClassSerializer.errorToJson(
				"Error retrieving instance: " + ex.getMessage(),
				InstanceBrowserTool.class,
				"getInstance()",
				args);
		}
	}

	/**
	 * Builds type information data object.
	 */
	private static InstanceTypeInfo buildTypeInfo(TLStructuredType type) {
		InstanceTypeInfo typeInfo = InstanceTypeInfo.create();
		typeInfo.setName(type.getName());

		// Module information
		TLModule module = type.getModule();
		if (module != null) {
			typeInfo.setModule(module.getName());

			// Module label if available
			ResKey moduleKey = LabelVisitor.getModuleResourceKey(module);
			Resources resources = Resources.getInstance();
			String moduleLabel = resources.getString(moduleKey, null);
			if (moduleLabel != null && !moduleLabel.isEmpty()) {
				typeInfo.setModuleLabel(moduleLabel);
			}
		}

		// Type label if available
		ResKey typeKey = TLModelNamingConvention.getTypeLabelKey(type);
		Resources resources = Resources.getInstance();
		String typeLabel = resources.getString(typeKey, null);
		if (typeLabel != null && !typeLabel.isEmpty()) {
			typeInfo.setLabel(typeLabel);
		}

		// Model kind
		ModelKind kind = type.getModelKind();
		if (kind != null) {
			typeInfo.setKind(kind.name().toLowerCase());
		}

		return typeInfo;
	}

	/**
	 * Serializes an attribute value to a JSON string.
	 *
	 * @param value
	 *        The value to serialize.
	 * @return JSON string representation of the value.
	 */
	private static String serializeAttributeValue(Object value) throws IOException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			writeValue(json, value);
		}
		return buffer.toString();
	}

	/**
	 * Writes a value to JSON with proper type handling.
	 */
	private static void writeValue(JsonWriter json, Object value) throws IOException {
		if (value == null) {
			json.nullValue();
		} else if (value instanceof TLObject) {
			// For reference to another TLObject, write as TLObjectReference
			TLObjectReference ref = buildTLObjectReference((TLObject) value);
			ref.writeTo(json);
		} else if (value instanceof Iterable) {
			// For collections, write as JSON array
			json.beginArray();
			for (Object item : (Iterable<?>) value) {
				writeValue(json, item);
			}
			json.endArray();
		} else if (value instanceof String) {
			json.value((String) value);
		} else if (value instanceof Number) {
			json.value((Number) value);
		} else if (value instanceof Boolean) {
			json.value((Boolean) value);
		} else {
			// For other types, write as string
			json.value(value.toString());
		}
	}

	/**
	 * Builds a TLObjectReference data object.
	 */
	private static TLObjectReference buildTLObjectReference(TLObject tlObject) {
		TLObjectReference ref = TLObjectReference.create();
		ref.setTid(tlObject.tId().asString());

		TLStructuredType type = tlObject.tType();
		if (type != null) {
			ref.setType(TLModelUtil.qualifiedName(type));
		}

		// Try to get a label for the object using TLModelNamingConvention
		String label = null;
		try {
			ResKey labelKey = TLModelNamingConvention.getTypeLabelKey(type);
			if (labelKey != null) {
				Resources resources = Resources.getInstance();
				label = resources.getString(labelKey, null);
			}
		} catch (Exception ex) {
			// Ignore - label is optional
		}
		if (label != null && !label.isEmpty()) {
			ref.setLabel(label);
		}

		return ref;
	}
}
