/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLModuleDisplayGroup;
import com.top_logic.model.util.TLModelUtil;

/**
 * Generates a compact text digest of a {@link TLModel} for injection into agent system prompts.
 *
 * <p>
 * The digest gives agents structural knowledge of the application model without requiring
 * exploration tool calls. It uses a notation chosen for token efficiency.
 * </p>
 *
 * @see AgentFactory#MODEL_PLACEHOLDER
 */
public class ModelDigestGenerator {

	/**
	 * Generates a compact model digest.
	 *
	 * @param model
	 *        The application model to serialize.
	 * @param includeFramework
	 *        Whether to include framework/platform modules (tl.*, tl5.*).
	 * @return A text digest suitable for system prompt injection, or empty string if no application
	 *         modules exist.
	 */
	public static String generate(TLModel model, boolean includeFramework) {
		List<TLModule> modules = model.getModules().stream()
			.filter(m -> includeFramework || !isFrameworkModule(m))
			.sorted((a, b) -> a.getName().compareTo(b.getName()))
			.collect(Collectors.toList());

		if (modules.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("# Application Model\n");

		for (TLModule module : modules) {
			appendModule(sb, module);
		}

		return sb.toString();
	}

	private static void appendModule(StringBuilder sb, TLModule module) {
		List<TLType> types = module.getTypes().stream()
			.filter(t -> t.getModelKind() != ModelKind.ASSOCIATION
				&& t.getModelKind() != ModelKind.DATATYPE)
			.sorted((a, b) -> a.getName().compareTo(b.getName()))
			.collect(Collectors.toList());

		if (types.isEmpty()) {
			return;
		}

		// In TopLogic, ModelKind.CLASS covers both abstract/interface-like classes and concrete
		// classes. There is no ModelKind.INTERFACE — use isAbstract() to distinguish them.
		long classCount = types.stream()
			.filter(t -> t.getModelKind() == ModelKind.CLASS)
			.count();
		long enumCount = types.stream()
			.filter(t -> t instanceof TLEnumeration)
			.count();

		sb.append("\n## ").append(module.getName()).append(" (");
		sb.append(classCount).append(classCount == 1 ? " type" : " types");
		if (enumCount > 0) {
			sb.append(", ").append(enumCount).append(enumCount == 1 ? " enum" : " enums");
		}
		sb.append(")\n");

		// Enums first (compact one-liners), then interfaces, then classes
		for (TLType type : types) {
			if (type instanceof TLEnumeration) {
				appendEnum(sb, (TLEnumeration) type);
			}
		}
		for (TLType type : types) {
			if (type instanceof TLClass tlClass && isInterface(tlClass)) {
				appendStructuredType(sb, tlClass, "interface");
			}
		}
		for (TLType type : types) {
			if (type instanceof TLClass tlClass && !isInterface(tlClass)) {
				appendStructuredType(sb, tlClass, "class");
			}
		}
	}

	private static void appendEnum(StringBuilder sb, TLEnumeration enumeration) {
		sb.append("\nenum ").append(enumeration.getName()).append(" { ");
		List<? extends TLClassifier> classifiers = enumeration.getClassifiers();
		for (int i = 0; i < classifiers.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(classifiers.get(i).getName());
		}
		sb.append(" }\n");
	}

	private static void appendStructuredType(StringBuilder sb, TLClass tlClass, String keyword) {
		sb.append("\n").append(keyword).append(" ").append(tlClass.getName());

		// Generalizations
		List<TLClass> generalizations = tlClass.getGeneralizations();
		if (!generalizations.isEmpty()) {
			sb.append(" extends ");
			for (int i = 0; i < generalizations.size(); i++) {
				if (i > 0) sb.append(", ");
				sb.append(shortenTypeName(tlClass, generalizations.get(i)));
			}
		}

		List<? extends TLStructuredTypePart> localParts = tlClass.getLocalParts().stream()
			.sorted((a, b) -> a.getName().compareTo(b.getName()))
			.collect(Collectors.toList());

		if (localParts.isEmpty()) {
			sb.append(" { }\n");
			return;
		}

		sb.append(" {\n");
		for (TLStructuredTypePart part : localParts) {
			appendPart(sb, part, tlClass);
		}
		sb.append("}\n");
	}

	private static void appendPart(StringBuilder sb, TLStructuredTypePart part, TLClass owner) {
		sb.append("  ").append(part.getName()).append(": ");

		// Arrow prefix for references
		boolean isReference = part.getModelKind() == ModelKind.REFERENCE;
		if (isReference) {
			sb.append("-> ");
		}

		// Type name (shortened if same module)
		TLType partType = part.getType();
		if (partType != null) {
			sb.append(shortenTypeName(owner, partType));
		} else {
			sb.append("?");
		}

		// Collection marker
		if (part.isMultiple()) {
			sb.append("[]");
		}

		// Modifiers
		List<String> modifiers = new ArrayList<>();
		if (part.isMandatory()) modifiers.add("mandatory");
		if (part.isDerived()) modifiers.add("derived");
		if (part.isOverride()) modifiers.add("override");
		if (isReference && isComposite(part)) modifiers.add("composite");

		if (!modifiers.isEmpty()) {
			sb.append(" (").append(String.join(", ", modifiers)).append(")");
		}

		sb.append("\n");
	}

	/**
	 * Shortens a type's qualified name if it's in the same module as the owning type.
	 */
	private static String shortenTypeName(TLType owner, TLType referenced) {
		if (referenced.getModule() != null && owner.getModule() != null
				&& referenced.getModule().equals(owner.getModule())) {
			return referenced.getName();
		}
		return TLModelUtil.qualifiedName(referenced);
	}

	/**
	 * Checks if a {@link TLClass} represents an interface in the sense of having no concrete
	 * instances.
	 *
	 * <p>
	 * In TopLogic, there is no dedicated {@code ModelKind.INTERFACE} — both abstract mixin types
	 * (declared as {@code <interface>} in model XML) and user-defined abstract classes return
	 * {@code ModelKind.CLASS}. The {@link TLClass#isAbstract()} flag is the closest available
	 * distinction and covers all non-instantiable types.
	 * </p>
	 */
	private static boolean isInterface(TLClass tlClass) {
		return tlClass.isAbstract();
	}

	private static boolean isComposite(TLStructuredTypePart part) {
		if (part instanceof TLReference ref) {
			return ref.getEnd().isComposite();
		}
		return false;
	}

	private static boolean isFrameworkModule(TLModule module) {
		TLModuleDisplayGroup displayGroup = module.getAnnotation(TLModuleDisplayGroup.class);
		if (displayGroup != null) {
			String groupValue = displayGroup.getValue();
			if (groupValue != null && groupValue.startsWith("tl")) {
				return true;
			}
		}
		String name = module.getName();
		return name.startsWith("tl.") || name.startsWith("tl5.");
	}
}
