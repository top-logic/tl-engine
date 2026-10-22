/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * The named arguments of a drop: the dragged objects, followed by the objects its
 * {@link DropSignature} refers to at the place the drop is made.
 *
 * <p>
 * The dragged objects are the main argument, named by {@link DropConfig#getAs()}, the
 * {@link DropReference references} are named by the properties of the drop naming them. The
 * {@link DropConfig#getRefuseIf() refusal function} and the {@link DropConfig#getActions() actions}
 * of the drop see the same arguments.
 * </p>
 */
public class DropArguments {

	/** Name of the {@link DropReference#TARGET target} when no other name is given. */
	public static final String TARGET = "target";

	/** Name of the {@link DropReference#PARENT parent} when no other name is given. */
	public static final String PARENT = "parent";

	/** Name of the item inserted {@link DropReference#BEFORE before} when no other name is given. */
	public static final String BEFORE = "before";

	private DropArguments() {
		// Static utilities only.
	}

	/**
	 * The name of the argument holding the dragged objects of the given drop.
	 */
	public static String objectsName(DropConfig config) {
		return nameOr(config.getAs(), DropConfig.OBJECTS_ARGUMENT);
	}

	/**
	 * The name of the argument holding the given reference of the given drop.
	 */
	public static String referenceName(DropConfig config, DropReference reference) {
		return nameOr(reference.name().apply(config), reference.defaultName());
	}

	/**
	 * The names of the arguments of the given drop in the order of their values: the dragged
	 * objects, followed by the references of the signature.
	 */
	public static List<String> names(DropConfig config, DropSignature signature) {
		List<String> result = new ArrayList<>(signature.references().size() + 1);
		result.add(objectsName(config));
		for (DropReference reference : signature.references()) {
			result.add(referenceName(config, reference));
		}
		return result;
	}

	/**
	 * The names of the arguments of a drop with the given signature that gives no names of its own.
	 */
	public static List<String> defaultNames(DropSignature signature) {
		List<String> result = new ArrayList<>(signature.references().size() + 1);
		result.add(DropConfig.OBJECTS_ARGUMENT);
		for (DropReference reference : signature.references()) {
			result.add(reference.defaultName());
		}
		return result;
	}

	/**
	 * The scope the refusal function and the actions of the given drop are compiled in.
	 */
	public static ActionScope scope(DropConfig config, DropSignature signature) {
		return ActionScope.of(names(config, signature), objectsName(config));
	}

	/**
	 * The arguments of a drop of the given objects at the given location.
	 *
	 * @param names
	 *        The {@link #names(DropConfig, DropSignature) names} of the arguments of the drop.
	 * @param signature
	 *        The signature of the drop.
	 * @param objects
	 *        The dragged objects.
	 * @param location
	 *        The place the drop is made at.
	 */
	public static ActionArguments arguments(List<String> names, DropSignature signature, List<?> objects,
			DropLocation location) {
		return ActionArguments.of(names, values(signature, objects, location), names.get(0));
	}

	/**
	 * The values of the arguments of a drop of the given objects at the given location, in the
	 * order of the {@link #names(DropConfig, DropSignature) names}.
	 */
	public static List<Object> values(DropSignature signature, List<?> objects, DropLocation location) {
		List<Object> result = new ArrayList<>(signature.references().size() + 1);
		result.add(objects);
		result.addAll(signature.valuesAt(location));
		return result;
	}

	/**
	 * Reports the names of the given drop that cannot be used with the given signature: a name for
	 * a reference the signature does not have, and two arguments with the same name.
	 */
	static void check(InstantiationContext context, DropConfig config, DropSignature signature) {
		String mode = signature.mode().wireName();
		for (DropReference reference : DropReference.ALL) {
			String name = reference.name().apply(config);
			if (name != null && !name.isBlank() && !signature.references().contains(reference)) {
				context.error("A <" + DropConfig.TAG_NAME + "> of mode '" + mode + "' names its argument '"
					+ reference.nameProperty() + "', but refers to no such object; its arguments are "
					+ names(config, signature) + " (at " + config.location() + ").");
			}
		}
		Set<String> seen = new HashSet<>();
		for (String name : names(config, signature)) {
			if (!seen.add(name)) {
				context.error("A <" + DropConfig.TAG_NAME + "> gives two of its arguments the name '" + name
					+ "' (at " + config.location() + ").");
			}
		}
	}

	/**
	 * The scope of the given part of an element declaring the given drops, for
	 * {@link com.top_logic.layout.view.command.ActionScopeDefinition#actionScope(ConfigurationItem, ActionScope)}.
	 *
	 * @param part
	 *        A part of the element.
	 * @param drops
	 *        The drops the element declares.
	 * @param signature
	 *        The signature the element assigns a drop.
	 * @return The {@link #scope(DropConfig, DropSignature) scope} of the drop the part is,
	 *         {@code null} if the part is none of the drops.
	 */
	public static <D extends DropConfig> ActionScope scopeOf(ConfigurationItem part, List<D> drops,
			Function<? super D, DropSignature> signature) {
		for (D drop : drops) {
			if (drop == part) {
				return scope(drop, signature.apply(drop));
			}
		}
		return null;
	}

	private static String nameOr(String name, String defaultName) {
		return name == null || name.isBlank() ? defaultName : name.trim();
	}

	/**
	 * {@link ScriptContextVariablesProvider} for the {@link DropConfig#getRefuseIf() refusal
	 * function}: the arguments of the drop, as defined by the element declaring it.
	 */
	public static class Variables implements ScriptContextVariablesProvider {

		@Override
		public List<String> getVariables(ValueModel valueModel) {
			return ActionScopes.scopeAt(valueModel.getModel()).names();
		}
	}
}
