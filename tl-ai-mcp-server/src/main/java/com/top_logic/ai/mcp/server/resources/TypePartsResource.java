/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.ai.mcp.server.UriPattern;
import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * MCP resource that provides the list of parts (properties and references) within a specific
 * TopLogic type.
 *
 * <p>
 * This resource uses a URI template to dynamically access parts for any structured type in the
 * system. The resource URI pattern is
 * {@code toplogic://model/modules/{moduleName}/types/{typeName}/parts}, where {@code {moduleName}}
 * and {@code {typeName}} are replaced with actual values.
 * </p>
 *
 * <p>
 * For example:
 * <ul>
 * <li>{@code toplogic://model/modules/tl.core/types/TLObject/parts} - Lists parts in TLObject</li>
 * <li>{@code toplogic://model/modules/myapp.model/types/Person/parts} - Lists parts in Person</li>
 * </ul>
 * </p>
 *
 * <p>
 * The resource returns a JSON array of part objects, each containing the part's name, type, kind
 * (property/reference), and description (if available).
 * </p>
 *
 * @author Bernhard Haumacher
 */
public class TypePartsResource {

	/** Parameter name for module name in URI template. */
	private static final String PARAM_MODULE_NAME = "moduleName";

	/** Parameter name for type name in URI template. */
	private static final String PARAM_TYPE_NAME = "typeName";

	/** URI template for type parts resource. */
	public static final String URI_TEMPLATE = "toplogic://model/modules/{" + PARAM_MODULE_NAME + "}/types/{" + PARAM_TYPE_NAME + "}/parts";

	/**
	 * Pattern for extracting module name and type name from URI.
	 *
	 * <p>
	 * Derived from {@link #URI_TEMPLATE} by replacing template variables with capture groups.
	 * </p>
	 */
	private static final UriPattern URI_PATTERN = UriPattern.compile(URI_TEMPLATE);

	/** Resource name template. */
	private static final String NAME_TEMPLATE = "type-parts-{" + PARAM_MODULE_NAME + "}-{" + PARAM_TYPE_NAME + "}";

	/** Resource description. */
	private static final String DESCRIPTION = "List of parts (properties and references) in a TopLogic type";

	/** MIME type for JSON content. */
	private static final String MIME_TYPE = JsonResponseBuilder.JSON_MIME_TYPE;

	/**
	 * Creates the MCP resource template specification for listing type parts.
	 *
	 * <p>
	 * This creates a resource template that can handle requests for any structured type's parts by
	 * extracting the module and type names from the URI.
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
			TypePartsResource::handleReadRequest
		);
	}

	/**
	 * Handles requests to read a type's parts resource.
	 *
	 * <p>
	 * Sets up TopLogic thread context and delegates to {@link #readTypeParts(McpSchema.ReadResourceRequest)}.
	 * </p>
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The read resource request containing the URI with the module and type names.
	 * @return The resource content with the list of parts in the type.
	 */
	private static McpSchema.ReadResourceResult handleReadRequest(
			McpSyncServerExchange exchange,
			McpSchema.ReadResourceRequest request) {

		// Wrap database access in system interaction context
		return ThreadContextManager.inSystemInteraction(TypePartsResource.class, () -> readTypeParts(request));
	}

	/**
	 * Reads the list of parts from a specific TopLogic type.
	 *
	 * <p>
	 * This method must be called within a TopLogic thread context (see {@link #handleReadRequest}).
	 * </p>
	 *
	 * @param request
	 *        The read resource request containing the URI with the module and type names.
	 * @return The resource content with the list of parts as JSON.
	 */
	private static McpSchema.ReadResourceResult readTypeParts(McpSchema.ReadResourceRequest request) {
		// Extract module name and type name from URI
		String uri = request.uri();
		Map<String, String> parameters = URI_PATTERN.extractParameters(uri);
		String moduleName = parameters.get(PARAM_MODULE_NAME);
		String typeName = parameters.get(PARAM_TYPE_NAME);

		// Get the application model and find the requested module
		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);

		if (module == null) {
			throw new IllegalArgumentException("Module not found: " + moduleName);
		}

		// Find the type in the module
		TLType type = module.getType(typeName);

		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + typeName + " in module " + moduleName);
		}

		// Check if the type is structured (has parts)
		if (!(type instanceof TLStructuredType)) {
			throw new IllegalArgumentException(
				"Type " + typeName + " is not a structured type and does not have parts");
		}

		TLStructuredType structuredType = (TLStructuredType) type;

		// Get all local parts in the type, sorted by name
		List<? extends TLStructuredTypePart> parts = structuredType.getLocalParts().stream()
			.sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
			.collect(Collectors.toList());

		// Build JSON array with part information using JsonWriter
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginArray();

			for (TLStructuredTypePart part : parts) {
				json.beginObject();
				json.name("name").value(part.getName());

				// Add part kind (property/reference)
				ModelKind partKind = part.getModelKind();
				json.name("kind").value(partKind.name().toLowerCase());

				// Add type information
				TLType partType = part.getType();
				if (partType != null) {
					json.name("type").value(TLModelUtil.qualifiedName(partType));
				}

				// Add multiplicity and collection characteristics
				json.name("mandatory").value(part.isMandatory());
				json.name("multiple").value(part.isMultiple());

				if (part.isMultiple()) {
					// Only relevant for multiple-valued parts
					json.name("ordered").value(part.isOrdered());
					json.name("bag").value(part.isBag());
				}

				// Add structural properties
				json.name("abstract").value(part.isAbstract());
				json.name("derived").value(part.isDerived());
				json.name("override").value(part.isOverride());

				// Add label and description from part resource key (optional)
				JsonResponseBuilder.writeLabelAndDescription(json, TLTypePartResourceProvider.labelKey(part));

				json.endObject();
			}

			json.endArray();
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

}
