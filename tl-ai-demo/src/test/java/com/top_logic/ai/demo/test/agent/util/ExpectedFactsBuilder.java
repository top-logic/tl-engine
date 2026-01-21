/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.util;


import com.top_logic.ai.service.tools.model.AnnotationDefinitionExplorerTool;
import com.top_logic.ai.service.tools.model.InstanceBrowserTool;
import com.top_logic.ai.service.tools.model.ModelAnnotationBrowserTool;
import com.top_logic.ai.service.tools.model.ModelBrowserTool;
import com.top_logic.ai.service.tools.model.ModelSearchTool;
import com.top_logic.ai.service.tools.model.TypeAnalysisTool;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Helper class for building expected facts from model exploration tools.
 *
 * <p>
 * This class provides access to all model exploration tools that the
 * ModelExplorer Agent has access to. Users can configure which tools to call
 * in their test methods to build custom "expected facts" JSON structures.
 * </p>
 *
 * <p>
 * Usage example:
 * <pre>{@code
 * ExpectedFactsBuilder builder = new ExpectedFactsBuilder();
 *
 * // Call tools to get structured data
 * String typesJson = builder.getBrowserTool().listTypes("luigis.pizzashop");
 * String partsJson = builder.getBrowserTool().listTypeParts("luigis.pizzashop", "Product", false);
 *
 * // Parse responses if needed
 * TypeListResponse typesResponse = builder.parseResponse(typesJson, TypeListResponse.class);
 *
 * // Assemble custom facts JSON
 * String expectedFacts = assembleExpectedFacts(typesJson, partsJson, ...);
 * }</pre>
 * </p>
 */
public class ExpectedFactsBuilder {

	private final ModelBrowserTool _browserTool;
	private final InstanceBrowserTool _instanceTool;
	private final TypeAnalysisTool _analysisTool;
	private final ModelSearchTool _searchTool;
	private final ModelAnnotationBrowserTool _annotationTool;
	private final AnnotationDefinitionExplorerTool _definitionTool;

	/**
	 * Creates a new {@link ExpectedFactsBuilder} with all model tools initialized.
	 */
	public ExpectedFactsBuilder() {
		_browserTool = new ModelBrowserTool();
		_instanceTool = new InstanceBrowserTool();
		_analysisTool = new TypeAnalysisTool();
		_searchTool = new ModelSearchTool();
		_annotationTool = new ModelAnnotationBrowserTool();
		_definitionTool = new AnnotationDefinitionExplorerTool();
	}

	/**
	 * Gets the {@link ModelBrowserTool} for listing modules, types, and parts.
	 *
	 * @return The browser tool.
	 */
	public ModelBrowserTool getBrowserTool() {
		return _browserTool;
	}

	/**
	 * Gets the {@link InstanceBrowserTool} for browsing instance data.
	 *
	 * @return The instance browser tool.
	 */
	public InstanceBrowserTool getInstanceTool() {
		return _instanceTool;
	}

	/**
	 * Gets the {@link TypeAnalysisTool} for finding type usages.
	 *
	 * @return The type analysis tool.
	 */
	public TypeAnalysisTool getAnalysisTool() {
		return _analysisTool;
	}

	/**
	 * Gets the {@link ModelSearchTool} for searching the model.
	 *
	 * @return The search tool.
	 */
	public ModelSearchTool getSearchTool() {
		return _searchTool;
	}

	/**
	 * Gets the {@link ModelAnnotationBrowserTool} for browsing annotations.
	 *
	 * @return The annotation browser tool.
	 */
	public ModelAnnotationBrowserTool getAnnotationTool() {
		return _annotationTool;
	}

	/**
	 * Gets the {@link AnnotationDefinitionExplorerTool} for exploring annotation definitions.
	 *
	 * @return The annotation definition explorer tool.
	 */
	public AnnotationDefinitionExplorerTool getDefinitionTool() {
		return _definitionTool;
	}

	/**
	 * Parses a JSON response from a tool call into a data class.
	 *
	 * <p>
	 * This is a convenience method for parsing JSON responses from tool calls
	 * into their corresponding message classes.
	 * </p>
	 *
	 * @param <T> The response type.
	 * @param jsonResponse The JSON string from the tool.
	 * @param responseClass The class to parse into.
	 * @return The parsed response object.
	 */
	public <T> T parseResponse(String jsonResponse, Class<T> responseClass) {
		try {
			// JsonReader from msgbuf can be created from StringR (which implements de.haumacher.msgbuf.io.Reader)
			de.haumacher.msgbuf.io.Reader reader = new StringR(jsonResponse);
			JsonReader jsonReader = new JsonReader(reader);
			// DataClassSerializer writes the object directly, so try reading it directly first
			try {
				var method = responseClass.getMethod("read" + responseClass.getSimpleName(), JsonReader.class);
				Object parsed = method.invoke(null, jsonReader);
				return responseClass.cast(parsed);
			} catch (Exception directParseException) {
				// If direct parse fails, try looking for wrapped format
				// Reset reader
				de.haumacher.msgbuf.io.Reader reader2 = new StringR(jsonResponse);
				JsonReader jsonReader2 = new JsonReader(reader2);
				jsonReader2.beginObject();
				
				while (jsonReader2.hasNext()) {
					String fieldName = jsonReader2.nextName();
					
					// Check for error response
					if ("error".equals(fieldName)) {
						String error = jsonReader2.nextString();
						throw new RuntimeException("Tool returned error: " + error);
					}
					
					// Check if this field contains our response
					if ("data".equals(fieldName) || 
						responseClass.getSimpleName().replace("Response", "").equals(fieldName)) {
						var method = responseClass.getMethod("read" + responseClass.getSimpleName(), JsonReader.class);
						Object parsed = method.invoke(null, jsonReader2);
						return responseClass.cast(parsed);
					}
					
					jsonReader2.skipValue();
				}
				throw new IllegalArgumentException("Could not find response object in JSON");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to parse JSON response: " + ex.getMessage() + "\nJSON was: " + jsonResponse, ex);
		}
	}
}
