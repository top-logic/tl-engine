/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.ai.service.tools.util.JsonToolResponseBuilder;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for analyzing TopLogic type dependencies and relationships.
 *
 * <p>
 * This tool provides methods for understanding how types are used throughout the model:
 * <ul>
 * <li>{@link #findTypeUsages(String)} - Finds all usages of a type</li>
 * </ul>
 * </p>
 *
 * <p>
 * All methods return JSON-formatted strings for consumption by AI agents.
 * </p>
 */
public class TypeAnalysisTool {

	/**
	 * Finds all usages of a specific type in the model.
	 *
	 * <p>
	 * Returns a JSON object with two arrays:
	 * <ul>
	 * <li>{@code properties} - Properties that use this type as their value type</li>
	 * <li>{@code subclasses} - Classes that extend this type (only for classes)</li>
	 * </ul>
	 * </p>
	 *
	 * @param qualifiedTypeName
	 *        The fully qualified type name in format "module:TypeName" (e.g., "tl.core:TLObject").
	 * @return JSON object with usage information, or error JSON if type not found.
	 */
	@Tool("Find all usages of a type in the model (properties using it, classes extending it)")
	public String findTypeUsages(
			@P("The qualified type name in format 'module:TypeName'") String qualifiedTypeName) {

		return ThreadContextManager.inSystemInteraction(
			TypeAnalysisTool.class,
			() -> {
				try {
					// Validate parameter
					if (qualifiedTypeName == null || qualifiedTypeName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Qualified type name is required");
					}

					// Get the application model and find the requested type
					TLModel model = ModelService.getApplicationModel();
					TLType type = TLModelUtil.findType(model, qualifiedTypeName);

					if (type == null) {
						return JsonToolResponseBuilder.buildErrorJson("Type not found: " + qualifiedTypeName);
					}

					// Find all usages of this type
					List<PropertyUsage> propertyUsages = findPropertyUsages(model, type);
					List<String> subclasses = findSubclasses(type);

					// Build JSON object with usage information
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
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
					}

					String result = buffer.toString();
					Logger.info("findTypeUsages() result: " + result, TypeAnalysisTool.class);
					System.out.println("TypeAnalysisTool.findTypeUsages() result: " + result);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to find type usages: " + ex.getMessage());
					Logger.error("findTypeUsages() error: " + ex.getMessage(), ex, TypeAnalysisTool.class);
					System.err.println("TypeAnalysisTool.findTypeUsages() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error analyzing type: " + ex.getMessage());
					Logger.error("findTypeUsages() error: " + ex.getMessage(), ex, TypeAnalysisTool.class);
					System.err.println("TypeAnalysisTool.findTypeUsages() error: " + error);
					return error;
				}
			});
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
