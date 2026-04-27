/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

import com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse;
import com.top_logic.tl.ai.core.tools.model.messages.PropertyUsage;
import com.top_logic.tl.ai.core.tools.model.messages.ReferenceUsage;
import com.top_logic.tl.ai.core.tools.model.messages.SubclassUsage;
import com.top_logic.tl.ai.core.tools.model.messages.TypeUsageResponse;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool for analyzing TopLogic type dependencies and relationships.
 *
 * <p>
 * This tool provides methods for understanding how types are used throughout the model: finding all
 * usages of a type and finding where a part is defined and inherited. All methods return
 * JSON-formatted strings for consumption by AI agents.
 * </p>
 *
 * @see #findTypeUsages(String)
 * @see #findPartUsages(String, String)
 */
public class TypeAnalysisTool {

	/**
	 * Configuration for {@link TypeAnalysisTool}.
	 */
	public interface Config extends PolymorphicConfiguration<TypeAnalysisTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link TypeAnalysisTool}.
	 */
	public TypeAnalysisTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link TypeAnalysisTool} from configuration.
	 */
	@CalledByReflection
	public TypeAnalysisTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Finds all usages of a specific type in the model.
	 *
	 * <p>
	 * Returns a JSON object with three arrays:
	 * <ul>
	 * <li>{@code properties} - Non-reference properties that use this type as their value type</li>
	 * <li>{@code references} - References that point to this type</li>
	 * <li>{@code subclasses} - Classes that extend this type (only for classes)</li>
	 * </ul>
	 * </p>
	 *
	 * @param qualifiedTypeName
	 *        The fully qualified type name in format "module:TypeName" (e.g., "tl.core:TLObject").
	 * @return JSON object with usage information, or error JSON if type not found.
	 */
	@ToolMethod(description = "Find all usages of a type in the model (properties, references, and classes extending it)", mode = ToolMode.READ)
	public String findTypeUsages(
			@P("The qualified type name in format 'module:TypeName'") String qualifiedTypeName) {

		try {
			// System interaction returns data class
			TypeUsageResponse response = ThreadContextManager.inSystemInteraction(
				TypeAnalysisTool.class,
				() -> {
					// Validate parameter
					if (qualifiedTypeName == null || qualifiedTypeName.isEmpty()) {
						throw new IllegalArgumentException("Qualified type name is required");
					}

					// Get the application model and find the requested type
					TLModel model = ModelService.getApplicationModel();
					TLType type = TLModelUtil.findType(model, qualifiedTypeName);

					if (type == null) {
						throw new IllegalArgumentException("Type not found: " + qualifiedTypeName);
					}

					// Find all usages of this type
					List<PropertyUsage> propertyUsages = findPropertyUsages(model, type);
					List<ReferenceUsage> referenceUsages = findReferenceUsages(model, type);
					List<String> subclasses = findSubclasses(type);

					// Build response using data classes
					TypeUsageResponse result = TypeUsageResponse.create();

					// Add property usages (directly from {@link #findPropertyUsages(TLModel, TLType)})
					for (PropertyUsage propertyUsage : propertyUsages) {
						result.addProperty(propertyUsage);
					}

					// Add reference usages (directly from {@link #findReferenceUsages(TLModel, TLType)})
					for (ReferenceUsage referenceUsage : referenceUsages) {
						result.addReference(referenceUsage);
					}

					// Add subclasses
					for (String subclass : subclasses) {
						SubclassUsage subclassUsage = SubclassUsage.create();
						subclassUsage.setSubclass(subclass);
						subclassUsage.setUsage("generalization");
						result.addSubclass(subclassUsage);
					}

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("qualifiedTypeName", qualifiedTypeName);
			return DataClassSerializer.toJson(response, TypeAnalysisTool.class, "findTypeUsages()", args);

		} catch (Exception ex) {
			Logger.error("findTypeUsages() error: " + ex.getMessage(), ex, TypeAnalysisTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("qualifiedTypeName", qualifiedTypeName);
			return DataClassSerializer.errorToJson(
				"Error analyzing type: " + ex.getMessage(),
				TypeAnalysisTool.class,
				"findTypeUsages()",
				args);
		}
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
	@ToolMethod(description = "Find where a specific part (property or reference) is defined and inherited", mode = ToolMode.READ)
	public String findPartUsages(
			@P("The qualified type name of the owner in format 'module:TypeName'") String ownerTypeQualifiedName,
			@P("The name of the part to analyze") String partName) {

		try {
			// System interaction returns data class
			PartUsageResponse response = ThreadContextManager.inSystemInteraction(
				TypeAnalysisTool.class,
				() -> {
					// Validate parameters
					if (ownerTypeQualifiedName == null || ownerTypeQualifiedName.isEmpty() ||
							partName == null || partName.isEmpty()) {
						throw new IllegalArgumentException("Owner type and part name are required");
					}

					// Get the application model and find the requested type
					TLModel model = ModelService.getApplicationModel();
					TLType type = TLModelUtil.findType(model, ownerTypeQualifiedName);

					if (type == null) {
						throw new IllegalArgumentException("Type not found: " + ownerTypeQualifiedName);
					}

					// Check if type is structured (has parts)
					if (!(type instanceof TLStructuredType structuredType)) {
						throw new IllegalArgumentException("Type is not structured: " + ownerTypeQualifiedName);
					}

					// Get the part from the type
					TLStructuredTypePart part = structuredType.getPart(partName);
					if (part == null) {
						throw new IllegalArgumentException("Part not found: " + partName + " in type " + ownerTypeQualifiedName);
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

					// Build response using data classes
					PartUsageResponse result = PartUsageResponse.create();
					result.setOwnerType(ownerTypeQualifiedName);
					result.setPartName(partName);
					result.setPartKind(partKind);
					result.setDefinedIn(definedIn);

					// Add inherited by subclasses
					for (String subclass : inheritedBy) {
						result.addInheritedBy(subclass);
					}

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("ownerTypeQualifiedName", ownerTypeQualifiedName);
			args.put("partName", partName);
			return DataClassSerializer.toJson(response, TypeAnalysisTool.class, "findPartUsages()", args);

		} catch (Exception ex) {
			Logger.error("findPartUsages() error: " + ex.getMessage(), ex, TypeAnalysisTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("ownerTypeQualifiedName", ownerTypeQualifiedName);
			args.put("partName", partName);
			return DataClassSerializer.errorToJson(
				"Error analyzing part: " + ex.getMessage(),
				TypeAnalysisTool.class,
				"findPartUsages()",
				args);
		}
	}

	/**
	 * Finds all properties (non-references) that use the given type as their value type.
	 *
	 * <p>
	 * Uses the meta-model to find all instances of TLStructuredTypePart where the 'type' reference
	 * points to the given type. This method excludes references and only returns actual properties
	 * (attributes with primitive or enumeration types).
	 * </p>
	 *
	 * @param model
	 *        The TopLogic model.
	 * @param type
	 *        The type to find usages for.
	 * @return List of property usages (excluding references).
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

		// Use {@link TLReference#getReferers(Object)} to find all TLStructuredTypePart instances that reference this type
		// Handle both direct TLStructuredTypePart and TLAssociationEnd, but only keep properties
		for (Object referer : typeRef.getReferers(type)) {
			String ownerTypeName = null;
			String partName = null;
			boolean isReference = false;

			if (referer instanceof TLReference) {
				// This is a reference, not a property - skip it
				continue;
			} else if (referer instanceof TLAssociationEnd) {
				// Association end - check if it implements a reference
				TLAssociationEnd end = (TLAssociationEnd) referer;
				TLReference ref = end.getReference();
				if (ref != null) {
					// This association end implements a reference - skip it
					continue;
				}
				// Otherwise, treat it as a property (shouldn't normally happen)
				TLStructuredTypePart part = (TLStructuredTypePart) referer;
				ownerTypeName = TLModelUtil.qualifiedName(part.getOwner());
				partName = part.getName();
			} else if (referer instanceof TLStructuredTypePart) {
				// Direct property
				TLStructuredTypePart part = (TLStructuredTypePart) referer;
				ownerTypeName = TLModelUtil.qualifiedName(part.getOwner());
				partName = part.getName();
			}

			if (ownerTypeName != null && partName != null && !isReference) {
				PropertyUsage usage = PropertyUsage.create();
				usage.setOwner(ownerTypeName);
				usage.setProperty(partName);
				usage.setUsage("value type");
				usages.add(usage);
			}
		}

		// Sort by owner type, then property name
		usages.sort((u1, u2) -> {
			int cmp = u1.getOwner().compareTo(u2.getOwner());
			if (cmp != 0) {
				return cmp;
			}
			return u1.getProperty().compareTo(u2.getProperty());
		});

		return usages;
	}

	/**
	 * Finds all references (not properties) that use the given type as their target type.
	 *
	 * <p>
	 * Uses the meta-model to find all instances of TLStructuredTypePart where the 'type' reference
	 * points to the given type. This method handles both direct {@link TLReference} instances and
	 * {@link TLAssociationEnd} instances (which represent association-end based references).
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

		// Use {@link TLReference#getReferers(Object)} to find all TLStructuredTypePart instances that reference this type
		// Handle both TLReference (direct) and TLAssociationEnd (association-based references)
		for (Object referer : typeRef.getReferers(type)) {
			TLReference reference = null;

			if (referer instanceof TLReference) {
				// Direct reference
				reference = (TLReference) referer;
			} else if (referer instanceof TLAssociationEnd) {
				// Association end - get the reference it implements
				TLAssociationEnd end = (TLAssociationEnd) referer;
				reference = end.getReference();
			}

			if (reference != null) {
				ReferenceUsage usage = ReferenceUsage.create();
				usage.setOwner(TLModelUtil.qualifiedName(reference.getOwner()));
				usage.setReference(reference.getName());
				usage.setUsage("reference type");
				usages.add(usage);
			}
		}

		// Sort by owner type, then reference name
		usages.sort((u1, u2) -> {
			int cmp = u1.getOwner().compareTo(u2.getOwner());
			if (cmp != 0) {
				return cmp;
			}
			return u1.getReference().compareTo(u2.getReference());
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

}
