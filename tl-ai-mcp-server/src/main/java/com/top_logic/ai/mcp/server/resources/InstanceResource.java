/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
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
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.tl.ai.core.security.ReadFilter;
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
	 * Establishes the calling account's TopLogic context and delegates to {@link #readInstance(String, McpSchema.ReadResourceRequest)}.
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

		// Read as the account this request was authenticated as, so that model access rights
		// apply. The system interaction this used to open is the engine's super-user bypass.
		return McpCallerContext.inCallerContext(() -> readInstance(tid, request));
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

			ReadFilter readFilter = McpCallerContext.readFilter();
			if (!readFilter.canRead(tlObject)) {
				// Deliberately the same answer as a TID that resolves to nothing. Saying "you may
				// not read this one" would confirm that an object with this TID exists, which is
				// itself a disclosure - the same rule ReadFilter follows for a collection, where an
				// unreadable element is omitted rather than replaced by a placeholder.
				return createErrorResult(request.uri(), "Object with TID '" + tid + "' not found");
			}

			// Build JSON response with the attributes the calling account may read
			String json = buildInstanceJson(readFilter, tlObject, tid);

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
	 * Builds a JSON representation of the instance including the attributes the calling account may
	 * read.
	 *
	 * @param readFilter The read filter of the calling account
	 * @param obj The TLObject to serialize
	 * @param tid The original TID as string
	 * @return JSON string with all instance information
	 */
	private static String buildInstanceJson(ReadFilter readFilter, TLObject obj, String tid) {
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

			// Attributes. ReadFilter is the one place a TLObject becomes something a client gets to
			// see: it leaves out an attribute the account may not read, and it filters the value
			// itself recursively, so a reference to an object the account may not read is dropped
			// rather than disclosed as an identity.
			json.name("attributes");
			writeJsonData(json, readFilter.attributesOf(obj));

			// Additional metadata
			json.name("metadata");
			json.beginObject();
			// The container is another object's identity, so it is disclosed only when that object is
			// readable - and the reference it is held by goes with it, because naming the attribute
			// while withholding the value says that there is a container the account may not see.
			TLObject container = (TLObject) readFilter.visible(obj.tContainer());
			json.name("container").value(container == null ? null : container.tId().toString());
			json.name("containerReference").value(container == null || obj.tContainerReference() == null
				? null : obj.tContainerReference().getName());
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
	 * Writes the JSON-compatible data {@link ReadFilter} produced.
	 *
	 * <p>
	 * The filter has already decided what may be disclosed and in what shape - primitives, lists,
	 * string-keyed maps, and a readable object reduced to its identity - so this method makes no
	 * decision of its own and only serialises.
	 * </p>
	 */
	private static void writeJsonData(JsonWriter json, Object data) throws IOException {
		if (data == null) {
			json.nullValue();
		} else if (data instanceof Map<?, ?> map) {
			json.beginObject();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				json.name(String.valueOf(entry.getKey()));
				writeJsonData(json, entry.getValue());
			}
			json.endObject();
		} else if (data instanceof Iterable<?> items) {
			json.beginArray();
			for (Object item : items) {
				writeJsonData(json, item);
			}
			json.endArray();
		} else if (data instanceof Boolean value) {
			json.value(value.booleanValue());
		} else if (data instanceof Number value) {
			json.value(value);
		} else {
			json.value(String.valueOf(data));
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