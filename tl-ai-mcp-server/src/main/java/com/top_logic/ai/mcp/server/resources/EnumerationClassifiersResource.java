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

import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.ai.mcp.server.UriPattern;
import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.model.config.annotation.UnorderedEnum;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * MCP resource that provides the list of classifiers within a specific TopLogic enumeration.
 *
 * <p>
 * This resource uses a URI template to dynamically access classifiers for any enumeration in the
 * system. The resource URI pattern is
 * {@code toplogic://model/modules/{moduleName}/types/{enumName}/classifiers}, where
 * {@code {moduleName}} and {@code {enumName}} are replaced with actual values.
 * </p>
 *
 * <p>
 * For example:
 * <ul>
 * <li>{@code toplogic://model/modules/tl.core/types/TLScope/classifiers} - Lists TLScope
 * classifiers</li>
 * <li>{@code toplogic://model/modules/myapp.model/types/Status/classifiers} - Lists Status
 * classifiers</li>
 * </ul>
 * </p>
 *
 * <p>
 * The resource returns a JSON object containing enumeration metadata and an array of classifier
 * objects, each with name, default status, index, label, description, and key annotations
 * ({@code TLI18NKey}, {@code UnorderedEnum}).
 * </p>
 */
public class EnumerationClassifiersResource {

	/** Parameter name for module name in URI template. */
	private static final String PARAM_MODULE_NAME = "moduleName";

	/** Parameter name for enumeration name in URI template. */
	private static final String PARAM_ENUM_NAME = "enumName";

	/** URI template for enumeration classifiers resource. */
	public static final String URI_TEMPLATE =
		"toplogic://model/modules/{" + PARAM_MODULE_NAME + "}/types/{" + PARAM_ENUM_NAME + "}/classifiers";

	/**
	 * Pattern for extracting module name and enumeration name from URI.
	 *
	 * <p>
	 * Derived from {@link #URI_TEMPLATE} by replacing template variables with capture groups.
	 * </p>
	 */
	private static final UriPattern URI_PATTERN = UriPattern.compile(URI_TEMPLATE);

	/** Resource name template. */
	private static final String NAME_TEMPLATE =
		"enumeration-classifiers-{" + PARAM_MODULE_NAME + "}-{" + PARAM_ENUM_NAME + "}";

	/** Resource description. */
	private static final String DESCRIPTION = "List of classifiers in a TopLogic enumeration with annotations";

	/** MIME type for JSON content. */
	private static final String MIME_TYPE = JsonResponseBuilder.JSON_MIME_TYPE;

	/**
	 * Creates the MCP resource template specification for listing enumeration classifiers.
	 *
	 * <p>
	 * This creates a resource template that can handle requests for any enumeration's classifiers by
	 * extracting the module and enumeration names from the URI.
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
			EnumerationClassifiersResource::handleReadRequest);
	}

	/**
	 * Handles requests to read an enumeration's classifiers resource.
	 *
	 * <p>
	 * Establishes the calling account's TopLogic context and delegates to
	 * {@link #readEnumerationClassifiers(McpSchema.ReadResourceRequest)}.
	 * </p>
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The read resource request containing the URI with the module and enumeration names.
	 * @return The resource content with the list of classifiers in the enumeration.
	 */
	private static McpSchema.ReadResourceResult handleReadRequest(
			McpSyncServerExchange exchange,
			McpSchema.ReadResourceRequest request) {

		// Read as the account this request was authenticated as, so that model access rights
		// apply. The system interaction this used to open is the engine's super-user bypass.
		return McpCallerContext.inCallerContext(() -> readEnumerationClassifiers(request));
	}

	/**
	 * Reads the list of classifiers from a specific TopLogic enumeration.
	 *
	 * <p>
	 * This method must be called within a TopLogic thread context (see {@link #handleReadRequest}).
	 * </p>
	 *
	 * @param request
	 *        The read resource request containing the URI with the module and enumeration names.
	 * @return The resource content with enumeration metadata and classifiers as JSON.
	 */
	private static McpSchema.ReadResourceResult readEnumerationClassifiers(McpSchema.ReadResourceRequest request) {
		// Extract module name and enumeration name from URI
		String uri = request.uri();
		Map<String, String> parameters = URI_PATTERN.extractParameters(uri);
		String moduleName = parameters.get(PARAM_MODULE_NAME);
		String enumName = parameters.get(PARAM_ENUM_NAME);

		// Get the application model and find the requested module
		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);

		if (module == null) {
			throw new IllegalArgumentException("Module not found: " + moduleName);
		}

		// Find the type in the module
		TLType type = module.getType(enumName);

		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + enumName + " in module " + moduleName);
		}

		// Check if the type is an enumeration
		if (!(type instanceof TLEnumeration)) {
			throw new IllegalArgumentException(
				"Type " + enumName + " is not an enumeration (it's a " + type.getModelKind() + ")");
		}

		TLEnumeration enumeration = (TLEnumeration) type;

		// Build JSON object with enumeration metadata and classifiers using JsonWriter
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			// Build enumeration metadata
			json.name("enumeration").value(TLModelUtil.qualifiedName(enumeration));
			json.name("module").value(moduleName);
			json.name("name").value(enumName);
			json.name("classifierCount").value(enumeration.getClassifiers().size());

			// Check for UnorderedEnum annotation
			UnorderedEnum unorderedAnnotation = enumeration.getAnnotation(UnorderedEnum.class);
			json.name("unordered").value(unorderedAnnotation != null);

			// Add label and description
			JsonResponseBuilder.writeLabelAndDescription(json,
				TLModelNamingConvention.getTypeLabelKey(enumeration));

			// Write classifiers array
			json.name("classifiers").beginArray();
			for (TLClassifier classifier : enumeration.getClassifiers()) {
				json.beginObject();
				json.name("name").value(classifier.getName());
				json.name("default").value(classifier.isDefault());
				json.name("index").value(classifier.getIndex());

				// Check for TLI18NKey annotation
				TLI18NKey i18nKey = classifier.getAnnotation(TLI18NKey.class);
				if (i18nKey != null && i18nKey.getValue() != null) {
					json.name("customI18NKey").value(i18nKey.getValue().toString());
				}

				// Add label and description
				JsonResponseBuilder.writeLabelAndDescription(json,
					TLModelNamingConvention.resourceKey(classifier));

				json.endObject();
			}
			json.endArray();

			json.endObject();
		} catch (IOException ex) {
			throw new RuntimeException("Failed to generate JSON: " + ex.getMessage(), ex);
		}

		McpSchema.TextResourceContents contents = new McpSchema.TextResourceContents(
			uri,
			MIME_TYPE,
			buffer.toString());

		return new McpSchema.ReadResourceResult(List.of(contents));
	}
}
