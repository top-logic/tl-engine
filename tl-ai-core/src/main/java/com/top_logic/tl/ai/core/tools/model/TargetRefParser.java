/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Parser and resolver for target references in format {@code "module"}, {@code "module:Type"},
 * or {@code "module:Type#part"}.
 */
class TargetRefParser {

	private final String targetRef;

	private final String moduleName;

	private final String typeName; // null for module references

	private final String partName; // null for module/type/enumeration references

	private final boolean isModule;

	private TargetRefParser(String targetRef, String moduleName, String typeName, String partName, boolean isModule) {
		this.targetRef = targetRef;
		this.moduleName = moduleName;
		this.typeName = typeName;
		this.partName = partName;
		this.isModule = isModule;
	}

	/**
	 * Parses a target reference string.
	 *
	 * @param targetRef
	 *        The target reference to parse.
	 * @return The parsed {@link TargetRefParser}.
	 * @throws IllegalArgumentException
	 *         If the format is invalid.
	 */
	static TargetRefParser parse(String targetRef) throws IllegalArgumentException {
		if (targetRef == null || targetRef.isEmpty()) {
			throw new IllegalArgumentException("Target reference cannot be null or empty");
		}

		// Split on '#' to detect part or classifier reference
		String[] refParts = targetRef.split("#", 2);
		String typeOrModuleRef = refParts[0];
		String partOrClassifierName = refParts.length > 1 ? refParts[1] : null;

		// Split on ':' to extract module and type
		String[] typeParts = typeOrModuleRef.split(":", 2);

		// If no ':' is present, it's a module-only reference
		if (typeParts.length == 1) {
			String moduleName = typeParts[0];
			if (moduleName.isEmpty()) {
				throw new IllegalArgumentException("Module name cannot be empty in: " + targetRef);
			}
			if (partOrClassifierName != null) {
				throw new IllegalArgumentException(
					"Invalid target reference format: cannot have '#' without module:type separator: " + targetRef);
			}
			return new TargetRefParser(targetRef, moduleName, null, null, true);
		}

		// Has ':' separator - it's a type or type#part reference
		if (typeParts.length != 2) {
			throw new IllegalArgumentException(
				"Invalid target reference format. Expected 'module', 'module:Type', or 'module:Type#part', got: "
					+ targetRef);
		}

		String moduleName = typeParts[0];
		String typeName = typeParts[1];

		// Validate components
		if (moduleName.isEmpty()) {
			throw new IllegalArgumentException("Module name cannot be empty in: " + targetRef);
		}
		if (typeName.isEmpty()) {
			throw new IllegalArgumentException("Type name cannot be empty in: " + targetRef);
		}
		if (partOrClassifierName != null && partOrClassifierName.isEmpty()) {
			throw new IllegalArgumentException("Part/classifier name cannot be empty in: " + targetRef);
		}

		// At this point we don't know if it's a classifier or part reference
		// We'll determine that during resolution
		return new TargetRefParser(targetRef, moduleName, typeName, partOrClassifierName, false);
	}

	/**
	 * Resolves the parsed reference to a {@link TLModelPart}.
	 *
	 * @param model
	 *        The model to resolve in.
	 * @return The resolved {@link TLModelPart}.
	 * @throws IllegalArgumentException
	 *         If the reference cannot be resolved.
	 */
	TLModelPart resolve(TLModel model) throws IllegalArgumentException {
		// Module-only reference
		if (isModule) {
			TLModule module = model.getModule(moduleName);
			if (module == null) {
				throw new IllegalArgumentException("Module not found: " + moduleName);
			}
			return module;
		}

		// Resolve module
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("Module not found: " + moduleName);
		}

		// Resolve type
		TLType type = module.getType(typeName);
		if (type == null) {
			throw new IllegalArgumentException("Type not found: " + typeName + " in module " + moduleName);
		}

		// If no part/classifier reference, return the type
		if (partName == null) {
			return type;
		}

		// Has part or classifier reference
		// Check if it's an enumeration (classifier) or structured type (part)
		if (type instanceof TLEnumeration) {
			TLEnumeration enumeration = (TLEnumeration) type;
			TLClassifier classifier = enumeration.getClassifier(partName);
			if (classifier == null) {
				throw new IllegalArgumentException(
					"Classifier not found: " + partName + " in enumeration " + TLModelUtil.qualifiedName(type));
			}
			return classifier;
		} else if (type instanceof TLStructuredType) {
			TLStructuredType structuredType = (TLStructuredType) type;
			TLStructuredTypePart part = structuredType.getPart(partName);
			if (part == null) {
				throw new IllegalArgumentException(
					"Part not found: " + partName + " in type " + TLModelUtil.qualifiedName(type));
			}
			return part;
		} else {
			throw new IllegalArgumentException(
				"Type has no parts or classifiers: " + TLModelUtil.qualifiedName(type));
		}
	}

	/**
	 * Determines the kind string based on the resolved model part.
	 *
	 * @param target
	 *        The resolved model part.
	 * @param parser
	 *        The parser that was used to resolve the target.
	 * @return The kind string ("module", "type", "enumeration", "part", or "classifier").
	 */
	static String determineKind(TLModelPart target, TargetRefParser parser) {
		if (target instanceof TLModule) {
			return "module";
		}
		if (target instanceof TLEnumeration) {
			return "enumeration";
		}
		if (target instanceof TLClassifier) {
			return "classifier";
		}
		if (target instanceof TLStructuredTypePart) {
			return "part";
		}
		if (target instanceof TLType) {
			return "type";
		}
		// Fallback
		return parser.getTypeName() == null ? "module" : "type";
	}

	String getModuleName() {
		return moduleName;
	}

	String getTypeName() {
		return typeName;
	}

	String getPartName() {
		return partName;
	}
}
