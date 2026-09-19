/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Generates a compact text digest of a {@link TLModel} for an agent run.
 *
 * <p>
 * The digest gives agents structural knowledge of the application model without requiring
 * exploration tool calls. It uses a notation chosen for token efficiency.
 * </p>
 *
 * <h2>What the principal gets to see</h2>
 *
 * <p>
 * The digest is a <b>read surface</b>: it reaches the model in one piece and describes types the
 * principal may have no business knowing about. So it is scoped, and it is scoped the way the rest of
 * L2 is - against the rights of the run, not against a mode or a flag. {@link #generate(TLModel,
 * boolean, ModelSecurity)} is the only form; there is no argument-less one to forget, which is the
 * same shape {@code TidResolver.resolve} and {@code ExecutionResult.toJson} have carried since #289.
 * </p>
 *
 * <p>
 * The unit of the decision is the <b>type</b>, because that is what the digest describes and what
 * {@link ModelSecurity#accessibleTypes(com.top_logic.tool.boundsec.BoundCommandGroup)} answers about.
 * A class the principal may not read has no entry, and a module whose classes are all inaccessible is
 * absent with its enumerations. What the digest never does is <em>describe</em> an inaccessible type.
 * </p>
 *
 * <p>
 * Three boundaries, stated so that an observation is not mistaken for a finding:
 * </p>
 *
 * <ul>
 * <li>An emitted type is written out in full - its generalizations and the target types of its parts
 * are <em>named</em> even where those types have no entry of their own. Naming is not describing, and
 * a definition with its references cut out would be a false statement about the type rather than a
 * narrower one. The same line the design draws for object labels, one level up.</li>
 * <li><b>Enumerations are not scoped</b>, because the engine's model access rights do not scope them:
 * a {@code <security-config>} grants on classes and on parts, and there is no grant an administrator
 * could write about an enumeration. They follow their module, which is scoped.</li>
 * <li>{@code accessibleTypes} resolves the principal's roles at the <b>global security root</b>. An
 * account that holds a role only on single objects - a scope rather than a tier - therefore does not
 * get that type advertised, although it may well read instances of it. The error is towards
 * disclosing less, and the instance surfaces are filtered per object anyway.</li>
 * </ul>
 *
 * @see ModelBrowserTool#outlineModel(Boolean, com.top_logic.tl.ai.tool.api.ToolExecutionContext)
 * @see ModelSecurity#accessibleTypes(com.top_logic.tool.boundsec.BoundCommandGroup)
 */
public class ModelDigestGenerator {

	/**
	 * Generates a compact model digest of everything the given principal may read.
	 *
	 * @param model
	 *        The application model to serialize.
	 * @param includeFramework
	 *        Whether to include framework/platform modules (tl.*, tl5.*).
	 * @param security
	 *        The rights of the run's principal. Mandatory: what a read surface discloses is decided
	 *        against a principal, and a digest generated without one would have to name every type or
	 *        none.
	 * @return A text digest, or the empty string if the principal may read no type of any module the
	 *         request asked for.
	 */
	public static String generate(TLModel model, boolean includeFramework, ModelSecurity security) {
		if (security == null) {
			throw new IllegalStateException("A model digest is a read surface, and this call names no"
				+ " principal to scope it for. Pass the ModelSecurity of the agent run.");
		}
		Set<TLClass> accessible = security.accessibleTypes(SimpleBoundCommandGroup.READ);

		List<TLModule> modules = model.getModules().stream()
			.filter(m -> includeFramework || !isFrameworkModule(m))
			.sorted((a, b) -> a.getName().compareTo(b.getName()))
			.collect(Collectors.toList());

		if (modules.isEmpty()) {
			return "";
		}

		StringBuilder body = new StringBuilder();
		for (TLModule module : modules) {
			appendModule(body, module, accessible);
		}
		if (body.length() == 0) {
			// Nothing survived the scoping. An empty preamble would tell the model there is a model
			// and no types in it, which is a statement about the principal's rights rather than
			// about the application.
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("# Application Model\n");
		sb.append(
			"// Notation: `name: Type` property · `name: -> Type` reference · `Type[]` collection · `(...)` modifiers: mandatory, derived, composite, override\n");
		sb.append(
			"// Type names: unqualified within the same module, else fully qualified. interface = abstract (no direct instances), class = instantiable.\n");
		sb.append(body);

		return sb.toString();
	}

	/**
	 * Appends the given module, or nothing at all when the principal may read none of its classes.
	 *
	 * <p>
	 * A module with no readable class is omitted with its enumerations: an enumeration is not scoped
	 * by the engine's rights, so the module it belongs to is the only statement about it that can be
	 * made, and a module the principal reads nothing of is not one it should be told about.
	 * </p>
	 */
	private static void appendModule(StringBuilder sb, TLModule module, Set<TLClass> accessible) {
		List<TLType> types = module.getTypes().stream()
			.filter(t -> t.getModelKind() != ModelKind.ASSOCIATION
				&& t.getModelKind() != ModelKind.DATATYPE)
			.filter(t -> !(t instanceof TLClass tlClass) || accessible.contains(tlClass))
			.sorted((a, b) -> a.getName().compareTo(b.getName()))
			.collect(Collectors.toList());

		if (types.stream().noneMatch(t -> t instanceof TLClass)) {
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
