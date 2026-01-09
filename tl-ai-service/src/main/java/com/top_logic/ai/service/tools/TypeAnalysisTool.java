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
import com.top_logic.model.TLStructuredType;
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
 * <li>{@link #findIncomingReferences(String)} - Finds references pointing to a type</li>
 * <li>{@link #findPartUsages(String, String)} - Finds where a part is defined and inherited</li>
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
	 * Finds all references pointing to a specific type (excludes simple properties).
	 *
	 * <p>
	 * Returns a JSON object with:
	 * <ul>
	 * <li>{@code targetType} - The type being referenced</li>
	 * <li>{@code references} - Array of references pointing to this type</li>
	 * </ul>
	 * </p>
	 *
	 * @param qualifiedTypeName
	 *        The fully qualified type name in format "module:TypeName" (e.g., "tl.core:TLObject").
	 * @return JSON object with incoming references information, or error JSON if type not found.
	 */
	@Tool("Find all references pointing to a specific type")
	public String findIncomingReferences(
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

					// Find all references to this type
					List<ReferenceUsage> referenceUsages = findReferenceUsages(model, type);

					// Build JSON object with reference information
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginObject();

						json.name("targetType").value(qualifiedTypeName);

						// References that point to this type
						json.name("references").beginArray();
						for (ReferenceUsage usage : referenceUsages) {
							json.beginObject();
							json.name("ownerType").value(usage.ownerType);
							json.name("referenceName").value(usage.referenceName);
							json.endObject();
						}
						json.endArray();

						json.endObject();
					}

					String result = buffer.toString();
					Logger.info("findIncomingReferences() result: " + result, TypeAnalysisTool.class);
					System.out.println("TypeAnalysisTool.findIncomingReferences() result: " + result);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to find incoming references: " + ex.getMessage());
					Logger.error("findIncomingReferences() error: " + ex.getMessage(), ex, TypeAnalysisTool.class);
					System.err.println("TypeAnalysisTool.findIncomingReferences() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error analyzing references: " + ex.getMessage());
					Logger.error("findIncomingReferences() error: " + ex.getMessage(), ex, TypeAnalysisTool.class);
					System.err.println("TypeAnalysisTool.findIncomingReferences() error: " + error);
					return error;
				}
			});
	}

	/**
	 * Finds where a specific part (property or reference) is defined and inherited.
	 *
	 * <p>
	 * Returns a JSON object with:
	 * <ul>
	 * <li>{@code ownerType} - The owner type specified</li>
	 * <li>{@code partName} - The part name</li>
	 * <li>{@code partKind} - "property" or "reference"</li>
	 * <li>{@code definedIn} - The type where the part is actually defined (may be a superclass)</li>
	 * <li>{@code inheritedBy} - Array of subclasses that inherit this part</li>
	 * </ul>
	 * </p>
	 *
	 * @param ownerTypeQualifiedName
	 *        The fully qualified type name of the owner in format "module:TypeName".
	 * @param partName
	 *        The name of the part to analyze.
	 * @return JSON object with part usage information, or error JSON if type or part not found.
	 */
	@Tool("Find where a specific part (property or reference) is defined and inherited")
	public String findPartUsages(
			@P("The qualified type name of the owner in format 'module:TypeName'") String ownerTypeQualifiedName,
			@P("The name of the part to analyze") String partName) {

		return ThreadContextManager.inSystemInteraction(
			TypeAnalysisTool.class,
			() -> {
				try {
					// Validate parameters
					if (ownerTypeQualifiedName == null || ownerTypeQualifiedName.isEmpty() ||
							partName == null || partName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Owner type and part name are required");
					}

					// Get the application model and find the requested type
					TLModel model = ModelService.getApplicationModel();
					TLType type = TLModelUtil.findType(model, ownerTypeQualifiedName);

					if (type == null) {
						return JsonToolResponseBuilder.buildErrorJson("Type not found: " + ownerTypeQualifiedName);
					}

					// Check if type is structured (has parts)
					if (!(type instanceof TLStructuredType structuredType)) {
						return JsonToolResponseBuilder.buildErrorJson("Type is not structured: " + ownerTypeQualifiedName);
					}

					// Get the part from the type
					TLStructuredTypePart part = structuredType.getPart(partName);
					if (part == null) {
						return JsonToolResponseBuilder.buildErrorJson("Part not found: " + partName + " in type " + ownerTypeQualifiedName);
					}

					// Find where the part is actually defined (may be in a superclass)
					String definedIn = TLModelUtil.qualifiedName(part.getOwner());

					// Determine part kind
					String partKind = (part instanceof TLReference) ? "reference" : "property";

					// Find subclasses that inherit this part
					List<String> inheritedBy = new ArrayList<>();
					if (type instanceof TLClass tlClass) {
						findInheritingSubclasses(tlClass, partName, inheritedBy);
					}

					// Build JSON object with part usage information
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginObject();

						json.name("ownerType").value(ownerTypeQualifiedName);
						json.name("partName").value(partName);
						json.name("partKind").value(partKind);
						json.name("definedIn").value(definedIn);

						// Subclasses that inherit this part
						json.name("inheritedBy").beginArray();
						for (String subclass : inheritedBy) {
							json.value(subclass);
						}
						json.endArray();

						json.endObject();
					}

					String result = buffer.toString();
					Logger.info("findPartUsages() result: " + result, TypeAnalysisTool.class);
					System.out.println("TypeAnalysisTool.findPartUsages() result: " + result);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to find part usages: " + ex.getMessage());
					Logger.error("findPartUsages() error: " + ex.getMessage(), ex, TypeAnalysisTool.class);
					System.err.println("TypeAnalysisTool.findPartUsages() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error analyzing part: " + ex.getMessage());
					Logger.error("findPartUsages() error: " + ex.getMessage(), ex, TypeAnalysisTool.class);
					System.err.println("TypeAnalysisTool.findPartUsages() error: " + error);
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
	 * Finds all references (not properties) that use the given type as their target type.
	 *
	 * <p>
	 * Uses the meta-model to find all instances of TLReference where the 'type' reference
	 * points to the given type. This differs from {@link #findPropertyUsages(TLModel, TLType)}
	 * by filtering to only TLReference instances.
	 * </p>
	 *
	 * @param model
	 *        The TopLogic model.
	 * @param type
	 *        The type to find references for.
	 * @return List of reference usages.
	 */
	private static List<ReferenceUsage> findReferenceUsages(TLModel model, TLType type) {
		List<ReferenceUsage> usages = new ArrayList<>();

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
		// Filter to only TLReference instances (exclude TLProperty)
		for (Object referer : typeRef.getReferers(type)) {
			if (referer instanceof TLReference reference) {
				String ownerTypeName = TLModelUtil.qualifiedName(reference.getOwner());
				usages.add(new ReferenceUsage(ownerTypeName, reference.getName()));
			}
		}

		// Sort by owner type, then reference name
		usages.sort((u1, u2) -> {
			int cmp = u1.ownerType.compareTo(u2.ownerType);
			if (cmp != 0) {
				return cmp;
			}
			return u1.referenceName.compareTo(u2.referenceName);
		});

		return usages;
	}

	/**
	 * Recursively finds all subclasses that inherit a specific part.
	 *
	 * @param tlClass
	 *        The class to start searching from.
	 * @param partName
	 *        The name of the part.
	 * @param inheritedBy
	 *        List to accumulate subclass names.
	 */
	private static void findInheritingSubclasses(TLClass tlClass, String partName, List<String> inheritedBy) {
		for (TLClass subclass : tlClass.getSpecializations()) {
			// Check if subclass has this part (inherited or overridden)
			if (subclass.getPart(partName) != null) {
				inheritedBy.add(TLModelUtil.qualifiedName(subclass));
				// Recursively check subclasses
				findInheritingSubclasses(subclass, partName, inheritedBy);
			}
		}
		// Sort after all subclasses are collected
		if (!inheritedBy.isEmpty()) {
			inheritedBy.sort(String::compareTo);
		}
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

	/**
	 * Helper class to hold reference usage information.
	 */
	private static class ReferenceUsage {
		final String ownerType;

		final String referenceName;

		ReferenceUsage(String ownerType, String referenceName) {
			this.ownerType = ownerType;
			this.referenceName = referenceName;
		}
	}
}
