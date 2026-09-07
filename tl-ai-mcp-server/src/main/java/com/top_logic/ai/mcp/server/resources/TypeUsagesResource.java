/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.ai.mcp.server.UriPattern;
import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * MCP resource that finds usages of a specific TopLogic type.
 *
 * <p>
 * This resource uses a URI template to find where a type is used in the model. The resource URI
 * pattern is {@code toplogic://model/types/{qualifiedTypeName}/usages}, where
 * {@code {qualifiedTypeName}} is the fully qualified name of the type (e.g., "tl.core:TLObject").
 * </p>
 *
 * <p>
 * For example:
 * <ul>
 * <li>{@code toplogic://model/types/tl.core:TLObject/usages} - Find usages of TLObject</li>
 * <li>{@code toplogic://model/types/myapp.model:Person/usages} - Find usages of Person type</li>
 * </ul>
 * </p>
 *
 * <p>
 * The resource returns a JSON object with two arrays:
 * <ul>
 * <li>{@code properties} - Properties that use this type as their value type</li>
 * <li>{@code subclasses} - Classes that use this type as a generalization (supertype)</li>
 * </ul>
 * </p>
 *
 * @author Bernhard Haumacher
 */
public class TypeUsagesResource {

	/** Parameter name for qualified type name in URI template. */
	private static final String PARAM_QUALIFIED_TYPE_NAME = "qualifiedTypeName";

	/** URI template for type usages resource. */
	public static final String URI_TEMPLATE = "toplogic://model/types/{" + PARAM_QUALIFIED_TYPE_NAME + "}/usages";

	/**
	 * Pattern for extracting qualified type name from URI.
	 *
	 * <p>
	 * Derived from {@link #URI_TEMPLATE} by replacing template variables with capture groups.
	 * Note: The qualified name contains a colon (module:typeName), so we capture [^/]+ to allow
	 * any characters except slashes.
	 * </p>
	 */
	private static final UriPattern URI_PATTERN = UriPattern.compile(URI_TEMPLATE);

	/** Resource name template. */
	private static final String NAME_TEMPLATE = "type-usages-{" + PARAM_QUALIFIED_TYPE_NAME + "}";

	/** Resource description. */
	private static final String DESCRIPTION = "Find usages of a TopLogic type (properties and subclasses)";

	/** MIME type for JSON content. */
	private static final String MIME_TYPE = JsonResponseBuilder.JSON_MIME_TYPE;

	/**
	 * Creates the MCP resource template specification for finding type usages.
	 *
	 * <p>
	 * This creates a resource template that can handle requests for any type's usages by
	 * extracting the qualified type name from the URI.
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
			TypeUsagesResource::handleReadRequest
		);
	}

	/**
	 * Handles requests to read a type's usages resource.
	 *
	 * <p>
	 * Establishes the calling account's TopLogic context and delegates to {@link #readTypeUsages(McpSchema.ReadResourceRequest)}.
	 * </p>
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The read resource request containing the URI with the qualified type name.
	 * @return The resource content with the list of usages.
	 */
	private static McpSchema.ReadResourceResult handleReadRequest(
			McpSyncServerExchange exchange,
			McpSchema.ReadResourceRequest request) {

		// Read as the account this request was authenticated as, so that model access rights
		// apply. The system interaction this used to open is the engine's super-user bypass.
		return McpCallerContext.inCallerContext(() -> readTypeUsages(request));
	}

	/**
	 * Reads the list of usages for a specific TopLogic type.
	 *
	 * <p>
	 * This method must be called within a TopLogic thread context (see {@link #handleReadRequest}).
	 * </p>
	 *
	 * @param request
	 *        The read resource request containing the URI with the qualified type name.
	 * @return The resource content with the list of usages as JSON.
	 */
	private static McpSchema.ReadResourceResult readTypeUsages(McpSchema.ReadResourceRequest request) {
		// Extract qualified type name from URI
		String uri = request.uri();
		Map<String, String> parameters = URI_PATTERN.extractParameters(uri);
		String qualifiedTypeName = parameters.get(PARAM_QUALIFIED_TYPE_NAME);

		// Get the application model and find the requested type
		TLModel model = ModelService.getApplicationModel();
		TLType type = TLModelUtil.findType(model, qualifiedTypeName);

		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + qualifiedTypeName);
		}

		// Find all usages of this type
		List<PropertyUsage> propertyUsages = findPropertyUsages(model, type);
		List<String> subclasses = findSubclasses(type);

		// Build JSON object with usage information using JsonWriter
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			// Properties that use this type as value type
			json.name("properties").beginArray();
			for (PropertyUsage usage : propertyUsages) {
				json.beginObject();
				json.name("owner").value(usage.ownerType);
				json.name("property").value(usage.propertyName);
				json.name("usage").value("value type");
				json.endObject();
			}
			json.endArray();

			// Classes that use this type as generalization (supertype)
			json.name("subclasses").beginArray();
			for (String subclass : subclasses) {
				json.beginObject();
				json.name("class").value(subclass);
				json.name("usage").value("generalization");
				json.endObject();
			}
			json.endArray();

			json.endObject();
		} catch (IOException ex) {
			throw new RuntimeException("Failed to generate JSON: " + ex.getMessage(), ex);
		}

		McpSchema.TextResourceContents contents = new McpSchema.TextResourceContents(
			request.uri(),
			MIME_TYPE,
			buffer.toString()
		);

		return new McpSchema.ReadResourceResult(List.of(contents));
	}

	/**
	 * Finds all properties that use the given type as their value type.
	 *
	 * <p>
	 * Uses the meta-model to find all instances of TLStructuredTypePart where the 'type' reference
	 * points to the given type.
	 * </p>
	 *
	 * @param model
	 *        The TopLogic model.
	 * @param type
	 *        The type to find usages for.
	 * @return List of property usages.
	 */
	private static List<PropertyUsage> findPropertyUsages(TLModel model, TLType type) {
		List<PropertyUsage> usages = new ArrayList<>();

		// Find the meta-model type for TLStructuredTypePart
		TLClass partMetaType = (TLClass) TLModelUtil.findType(model, "tl.model:TLStructuredTypePart");
		if (partMetaType == null) {
			return usages; // Meta-model not available
		}

		// Find the 'type' reference in TLStructuredTypePart
		TLStructuredTypePart typeReference = partMetaType.getPart("type");
		if (!(typeReference instanceof TLReference typeRef)) {
			return usages; // 'type' is not a reference
		}

		// Use getReferers() to find all TLStructuredTypePart instances that reference this type
		for (Object referer : typeRef.getReferers(type)) {
			if (referer instanceof TLStructuredTypePart part) {
				String ownerTypeName = TLModelUtil.qualifiedName(part.getOwner());
				usages.add(new PropertyUsage(ownerTypeName, part.getName()));
			}
		}

		// Sort by owner type, then property name
		usages.sort((u1, u2) -> {
			int cmp = u1.ownerType.compareTo(u2.ownerType);
			if (cmp != 0) {
				return cmp;
			}
			return u1.propertyName.compareTo(u2.propertyName);
		});

		return usages;
	}

	/**
	 * Finds all classes that use the given type as a generalization (supertype).
	 *
	 * @param type
	 *        The type to find subclasses for.
	 * @return List of qualified subclass names.
	 */
	private static List<String> findSubclasses(TLType type) {
		if (!(type instanceof TLClass tlClass)) {
			return List.of(); // Only classes can have subclasses
		}

		// Get all specializations (direct subclasses)
		return tlClass.getSpecializations().stream()
			.map(TLModelUtil::qualifiedName)
			.sorted()
			.collect(Collectors.toList());
	}

	/**
	 * Helper class to hold property usage information.
	 */
	private static class PropertyUsage {
		final String ownerType;

		final String propertyName;

		PropertyUsage(String ownerType, String propertyName) {
			this.ownerType = ownerType;
			this.propertyName = propertyName;
		}
	}

}
