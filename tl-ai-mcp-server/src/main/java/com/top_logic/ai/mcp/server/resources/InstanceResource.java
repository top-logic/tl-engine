/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.util.Resources;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * MCP resource that provides detailed information about a specific TopLogic instance by its TID.
 *
 * <p>
 * This resource retrieves an object using the efficient TopLogic API and returns all its attributes
 * and their values as JSON. This allows AI agents to inspect specific object instances and
 * understand their current state.
 * </p>
 *
 * <p>
 * Resource URI: {@code toplogic://instance/{tid}}
 * </p>
 *
 * <p>
 * The resource returns a JSON object containing:
 * </p>
 * <ul>
 * <li><code>tid</code> - The object's TID</li>
 * <li><code>type</code> - The object's type information</li>
 * <li><code>module</code> - The module containing the object's type</li>
 * <li><code>attributes</code> - A map of all attribute names and their values</li>
 * </ul>
 *
 * @author jhu
 */
public class InstanceResource {

	/** URI pattern for the instance resource. */
	public static final String URI_PATTERN = "toplogic://instance/{tid}";

	/** Resource name. */
	private static final String NAME = "instance";

	/** Resource description. */
	private static final String DESCRIPTION = "Detailed information about a TopLogic instance by its TID";

	/** MIME type for JSON content. */
	private static final String MIME_TYPE = JsonResponseBuilder.JSON_MIME_TYPE;

	/**
	 * Creates the MCP resource template specification for retrieving instance details.
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
			InstanceResource::handleReadRequest
		);
	}

	/**
	 * Handles requests to read the instance resource.
	 *
	 * <p>
	 * Sets up TopLogic thread context and delegates to {@link #readInstance(String, McpSchema.ReadResourceRequest)}.
	 * </p>
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The read resource request containing the URI with the TID.
	 * @return The resource content with the instance information.
	 */
	private static McpSchema.ReadResourceResult handleReadRequest(
			McpSyncServerExchange exchange,
			McpSchema.ReadResourceRequest request) {

		// Extract TID from URI
		String uri = request.uri();
		String tid = extractTidFromUri(uri);
		if (tid == null) {
			return createErrorResult(request.uri(), "Invalid instance URI: missing TID parameter");
		}

		// Wrap database access in system interaction context
		return ThreadContextManager.inSystemInteraction(InstanceResource.class, () -> readInstance(tid, request));
	}

	/**
	 * Extracts the TID from the instance resource URI.
	 *
	 * @param uri The resource URI in the format "toplogic://instance/{tid}"
	 * @return The extracted TID, or null if the URI format is invalid
	 */
	private static String extractTidFromUri(String uri) {
		String prefix = "toplogic://instance/";
		if (!uri.startsWith(prefix)) {
			return null;
		}
		return uri.substring(prefix.length());
	}

	/**
	 * Reads instance information from the TopLogic knowledge base.
	 *
	 * <p>
	 * This method must be called within a TopLogic thread context (see {@link #handleReadRequest}).
	 * </p>
	 *
	 * @param tid The TopLogic identifier of the object to retrieve
	 * @param request The read resource request
	 * @return The resource content with the instance information as JSON, or an error message
	 */
	private static McpSchema.ReadResourceResult readInstance(String tid, McpSchema.ReadResourceRequest request) {
		try {

			String[] split = tid.split(":");
			String typeName = split[0];
			String id = split[1];

			// Convert string TID to TLID
			TLID tlid = IdentifierUtil.fromExternalForm(id);

			// Get the knowledge base
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

			// Since we don't know the type, we need to search through all types
			KnowledgeObject ko = kb.getKnowledgeObject(typeName, tlid);

			if (ko == null) {
				return createErrorResult(request.uri(), "Object with TID '" + tid + "' not found");
			}

			// Wrap it as a TLObject
			TLObject tlObject = WrapperFactory.getWrapper(ko);


			// Build JSON response with all object information
			String json = buildInstanceJson(tlObject, tid);

			McpSchema.TextResourceContents contents = new McpSchema.TextResourceContents(
				request.uri(),
				MIME_TYPE,
				json
			);

			return new McpSchema.ReadResourceResult(List.of(contents));

		} catch (Exception ex) {
			return createErrorResult(request.uri(), "Error retrieving instance: " + ex.getMessage());
		}
	}

	/**
	 * Builds a JSON representation of the instance including all its attributes.
	 *
	 * @param obj The TLObject to serialize
	 * @param tid The original TID as string
	 * @return JSON string with all instance information
	 */
	private static String buildInstanceJson(TLObject obj, String tid) {
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			// Basic object information
			json.name("tid").value(tid);

			// Type information
			TLStructuredType type = obj.tType();
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
					Object value = obj.tValue(part);

					// Write attribute value
					writeAttributeValue(json, partName, value);
				}
			}

			json.endObject(); // attributes

			// Additional metadata
			json.name("metadata");
			json.beginObject();
			json.name("container").value(obj.tContainer() != null ? obj.tContainer().tId().toString() : null);
			json.name("containerReference").value(obj.tContainerReference() != null ? obj.tContainerReference().getName() : null);
			json.name("transient").value(obj.tTransient());
			json.endObject(); // metadata

			json.endObject(); // root

		} catch (IOException ex) {
			throw new RuntimeException("Failed to generate JSON: " + ex.getMessage(), ex);
		}

		return buffer.toString();
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
			JsonResponseBuilder.writeTLObjectReference(json, (TLObject) value);
		} else if (value instanceof Iterable) {
			// For collections, write as JSON array
			json.beginArray();
			for (Object item : (Iterable<?>) value) {
				if (item instanceof TLObject) {
					JsonResponseBuilder.writeTLObjectReference(json, (TLObject) item);
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
}