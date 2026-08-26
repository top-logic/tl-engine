/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.Labels;

/**
 * The checks {@link ConfigFormModel#apply()} runs before a working copy is allowed to replace the
 * original, and the reporting that puts each failure on the field that caused it.
 *
 * <p>
 * Two kinds of violation are collected, from two different places. A mandatory property with no
 * value is not something {@link ConstraintChecker} reports - the configuration framework only
 * catches that when it reads XML, and nothing here reads XML - so it is checked directly via
 * {@link PropertyDescriptor#isMandatory()}, {@link ConfigurationItem#valueSet(PropertyDescriptor)},
 * and the property's current value: {@link ConfigurationItem#update(PropertyDescriptor, Object)}
 * marks a property as set the moment it is called, whatever value it was called with - so a field
 * the user cleared back to {@code null} or {@code ""} reads as "set" too, and {@code valueSet}
 * alone cannot tell that apart from a value the user actually entered. A constraint violation comes
 * from {@link ConstraintChecker}, which is itself recursive and already names, for every failure,
 * the exact item and property {@link ConfigFieldIndex} is keyed by.
 * </p>
 *
 * <p>
 * {@link #check(ConfigurationItem)} is meant to run on the edited part of a configuration, not on
 * the copied root above it: only the edited part was actually changed by the user, and a violation
 * elsewhere in the tree predates the edit and has no field on screen to show it. A constraint that
 * navigates upward out of the edited part still works, because the copy keeps the root above it in
 * place.
 * </p>
 */
public final class ConfigValidation {

	/**
	 * One reason a configuration may not be applied, and where it belongs.
	 *
	 * @param item
	 *        The configuration item the violation was found on.
	 * @param property
	 *        The property of {@link #item()} the violation belongs to.
	 * @param message
	 *        The message describing the violation.
	 */
	public record Violation(ConfigurationItem item, PropertyDescriptor property, ResKey message) {
		// Nothing beyond the components.
	}

	private ConfigValidation() {
		// Static use only.
	}

	/**
	 * Checks the given configuration item, and everything reachable from it, for violations that
	 * must block {@link ConfigFormModel#apply()}.
	 *
	 * @param item
	 *        The edited part of the configuration to check.
	 * @return The violations found, in no particular order. Empty if the item may be applied as
	 *         it stands.
	 */
	public static List<Violation> check(ConfigurationItem item) {
		List<Violation> violations = new ArrayList<>();
		collectMissingMandatory(item, violations, Collections.newSetFromMap(new IdentityHashMap<>()));
		collectConstraintFailures(item, violations);
		return violations;
	}

	/**
	 * Puts every violation on the field that {@link ConfigFieldIndex#register(ConfigurationItem,
	 * PropertyDescriptor, ConfigFieldModel) registered} for its item and property.
	 *
	 * @param violations
	 *        The violations to report, typically {@link #check(ConfigurationItem)}'s result.
	 * @param index
	 *        The index the editor filled while building its fields.
	 * @return Whether every violation found a field to carry it. {@code false} if at least one
	 *         violation named a property the editor does not render, and was therefore not placed
	 *         anywhere the user can see.
	 */
	public static boolean report(List<Violation> violations, ConfigFieldIndex index) {
		boolean complete = true;
		for (Violation violation : violations) {
			ConfigFieldModel field = index.lookup(violation.item(), violation.property());
			if (field == null) {
				complete = false;
			} else {
				field.setError(violation.message());
			}
		}
		return complete;
	}

	/**
	 * Walks the given item and every nested item and collection entry reachable through its ITEM,
	 * LIST, ARRAY and MAP properties, adding a {@link Violation} for every mandatory property with
	 * no value.
	 *
	 * @param visited
	 *        Items already walked, guarding against revisiting one reachable twice and against a
	 *        cycle that would otherwise never terminate.
	 */
	private static void collectMissingMandatory(ConfigurationItem item, List<Violation> violations,
			Set<ConfigurationItem> visited) {
		if (item == null || !visited.add(item)) {
			return;
		}
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			if (property.isMandatory() && isMissing(item, property)) {
				violations.add(new Violation(item, property,
					I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY.fill(Labels.propertyLabel(property, false))));
			}
			descendMissingMandatory(item, property, violations, visited);
		}
	}

	/**
	 * Whether the given property of the given item has no value worth keeping, for the purpose of
	 * a {@link PropertyDescriptor#isMandatory() @Mandatory} check.
	 *
	 * <p>
	 * {@link ConfigurationItem#valueSet(PropertyDescriptor)} alone is not enough: it answers
	 * whether {@link ConfigurationItem#update(PropertyDescriptor, Object)} was ever called for the
	 * property, not whether the value it left behind is actually usable. A field the user cleared -
	 * typed over what was there and left it blank - calls {@code update} with {@code null} (or, for
	 * a {@link String} property, whatever the framework normalizes that {@code null} to, typically
	 * {@code ""}) just as much as a field the user filled in calls it with the entered value;
	 * {@code valueSet} reads "set" either way. So this also rejects a {@code null} value outright,
	 * and an empty {@link String} - the only type whose "empty" value is not itself {@code null}.
	 * </p>
	 *
	 * <p>
	 * Deliberately narrow: only a {@code null} value or an empty {@link String} count as missing.
	 * A {@link PropertyKind#LIST}, {@link PropertyKind#ARRAY}, or {@link PropertyKind#MAP}
	 * property is never flagged here even when empty, mirroring
	 * {@link ConfigFieldModel#isTechnicallyMandatory(PropertyDescriptor)}, which excludes exactly
	 * those kinds because they are "not nullable, but may be empty" - the same rule the classic
	 * declarative form applies. Two reasons this method keeps step with that rule rather than
	 * refusing an empty mandatory collection: it would enforce a requirement the field layer itself
	 * does not, and a collection property has no {@link ConfigFieldModel} of its own - it is
	 * rendered by {@link ConfigListEditorControl}, not as a field - so {@link #report(List,
	 * ConfigFieldIndex)} would find nothing in the {@link ConfigFieldIndex} for it, leaving the
	 * user stuck in an edit mode that refuses to close with nothing on screen to correct.
	 * </p>
	 */
	private static boolean isMissing(ConfigurationItem item, PropertyDescriptor property) {
		if (!item.valueSet(property)) {
			return true;
		}
		Object value = item.value(property);
		if (value == null) {
			return true;
		}
		return value instanceof String string && string.isEmpty();
	}

	/**
	 * Recurses {@link #collectMissingMandatory(ConfigurationItem, List, Set)} into the nested
	 * items held by an ITEM, LIST, ARRAY or MAP property, if that is what the given property is.
	 */
	private static void descendMissingMandatory(ConfigurationItem item, PropertyDescriptor property,
			List<Violation> violations, Set<ConfigurationItem> visited) {
		switch (property.kind()) {
			case ITEM:
				collectMissingMandatory((ConfigurationItem) item.value(property), violations, visited);
				break;

			case LIST: {
				List<?> entries = (List<?>) item.value(property);
				if (entries != null) {
					for (Object entry : entries) {
						collectMissingMandatory((ConfigurationItem) entry, violations, visited);
					}
				}
				break;
			}

			case ARRAY: {
				List<?> entries = PropertyDescriptorImpl.arrayAsList(item.value(property));
				if (entries != null) {
					for (Object entry : entries) {
						collectMissingMandatory((ConfigurationItem) entry, violations, visited);
					}
				}
				break;
			}

			case MAP: {
				Map<?, ?> entries = (Map<?, ?>) item.value(property);
				if (entries != null) {
					for (Object entry : entries.values()) {
						collectMissingMandatory((ConfigurationItem) entry, violations, visited);
					}
				}
				break;
			}

			default:
				break;
		}
	}

	/**
	 * Runs {@link ConstraintChecker} on the given item and adds a {@link Violation} for every
	 * non-{@link ConstraintFailure#isWarning() warning} failure it finds.
	 *
	 * <p>
	 * Uses {@link ConstraintChecker#check(ConfigurationItem)}, not one of the logging overloads:
	 * those clear the failure list in a {@code finally} block once they have reported it, so
	 * {@link ConstraintChecker#getFailures()} would read empty afterwards. The check is itself
	 * recursive, so nested items and collection entries are covered without any recursion here.
	 * </p>
	 */
	private static void collectConstraintFailures(ConfigurationItem item, List<Violation> violations) {
		ConstraintChecker checker = new ConstraintChecker();
		try {
			checker.check(item);
		} catch (ConfigurationException ex) {
			// Malformed constraint annotations - a programming error in the configuration
			// interface, not something the user editing the form did or can fix. Nothing is
			// added; the constraints that did parse have already been collected.
			Logger.error("Cannot check constraints of '" + item + "'.", ex, ConfigValidation.class);
		}
		for (ConstraintFailure failure : checker.getFailures()) {
			if (!failure.isWarning()) {
				violations.add(new Violation(failure.getItem(), failure.getContextProperty(), failure.getMessage()));
			}
		}
	}
}
