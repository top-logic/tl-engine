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

import com.top_logic.layout.configedit.ConfigPendingEntries.PendingEntry;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Looks up the {@link ConfigFieldModel} that displays a given {@link PropertyDescriptor} of a
 * given {@link ConfigurationItem}.
 *
 * <p>
 * A validation failure names an item and a property (that is what
 * {@link com.top_logic.basic.config.constraint.check.ConstraintFailure ConstraintFailure}
 * carries), but the field that shows it is a {@link ConfigFieldModel} buried in the control tree.
 * This index is filled while the editor builds and consulted when Apply refuses.
 * </p>
 *
 * <p>
 * Both maps are {@link IdentityHashMap}. The outer map must be, because a
 * {@link ConfigurationItem} may declare value equality
 * ({@link com.top_logic.basic.config.equal.EqualityByValue}): two separate items with the same
 * property values are still two separate fields, and a plain {@link java.util.HashMap} keyed by
 * such an item would merge their registrations into one. The inner map must be too, because
 * {@link PropertyDescriptor} overrides neither {@link Object#equals(Object)} nor
 * {@link Object#hashCode()} - and a subtype's descriptor hands out its own instance even for an
 * inherited property - so a {@link java.util.HashMap} there would already fall back to identity by
 * accident. Using {@link IdentityHashMap} for both says so explicitly and keeps a later change from
 * silently drifting to equality-based lookup.
 * </p>
 */
public final class ConfigFieldIndex {

	private final Map<ConfigurationItem, Map<PropertyDescriptor, ConfigFieldModel>> _fields =
		new IdentityHashMap<>();

	/**
	 * The entries a collection editor below the form has started and nobody has confirmed yet.
	 *
	 * <p>
	 * A pending entry is not in the configuration - that is what pending means - so validation
	 * cannot see it, and applying would rebuild the form over the original and drop it without a
	 * word. Whoever renders one registers it here, so the form can refuse instead.
	 * </p>
	 */
	private final List<PendingEntry> _pending = new ArrayList<>();

	/**
	 * Registers the {@link ConfigFieldModel} that displays the given property of the given
	 * configuration item.
	 *
	 * @param item
	 *        The configuration item the property belongs to.
	 * @param property
	 *        The property the model displays.
	 * @param model
	 *        The field model to register.
	 */
	public void register(ConfigurationItem item, PropertyDescriptor property, ConfigFieldModel model) {
		_fields.computeIfAbsent(item, any -> new IdentityHashMap<>()).put(property, model);
	}

	/**
	 * The {@link ConfigFieldModel} previously {@link #register(ConfigurationItem, PropertyDescriptor, ConfigFieldModel)
	 * registered} for the given item and property, or {@code null} if none was.
	 *
	 * @param item
	 *        The configuration item the property belongs to.
	 * @param property
	 *        The property to look up.
	 */
	public ConfigFieldModel lookup(ConfigurationItem item, PropertyDescriptor property) {
		Map<PropertyDescriptor, ConfigFieldModel> byProperty = _fields.get(item);
		return byProperty == null ? null : byProperty.get(property);
	}

	/**
	 * Forgets the registration made for the given item and property, if there is one.
	 *
	 * <p>
	 * Called when the control that built the field is disposed. Without it the index outlives what
	 * it indexes: {@link ConfigListEditorControl} rebuilds its own children whenever an entry is
	 * added, removed or moved, and nothing clears the index in between - only
	 * {@link ConfigFormControl}'s own rebuild does, which a refused Apply deliberately skips. An
	 * entry's field model would then stay in the index after the entry itself is gone, carrying
	 * whatever error it last had into every later question the index is asked.
	 * </p>
	 *
	 * @param item
	 *        The configuration item the property belongs to.
	 * @param property
	 *        The property to forget.
	 */
	public void unregister(ConfigurationItem item, PropertyDescriptor property) {
		Map<PropertyDescriptor, ConfigFieldModel> byProperty = _fields.get(item);
		if (byProperty == null) {
			return;
		}
		byProperty.remove(property);
		if (byProperty.isEmpty()) {
			_fields.remove(item);
		}
	}

	/**
	 * Whether any registered field currently rejects the raw input it was given.
	 *
	 * <p>
	 * A field that rejected its input - an unparsable duration, a fractional value for an integral
	 * property - keeps the rejected text on screen while the configuration still holds the last
	 * accepted value. {@link ConfigValidation#check(ConfigurationItem)} inspects the configuration
	 * and would therefore see nothing wrong at all, so the rejected input has to be asked about
	 * here, at the fields. This is the counterpart of {@code FormContext#checkAll()} in the classic
	 * declarative form.
	 * </p>
	 *
	 * <p>
	 * Answers only about input the field itself rejected, never about a violation
	 * {@link ConfigValidation#report(java.util.List, ConfigFieldIndex)} placed: those go to the
	 * separate model-validation channel, see {@link #clearModelErrors()}. Were the two to share one
	 * slot, a violation the user has since fixed elsewhere would keep reading as "an entry cannot
	 * be read" and refuse every further Apply with nothing on screen to correct.
	 * </p>
	 *
	 * @see com.top_logic.layout.form.model.AbstractFieldModel#getInputError()
	 */
	public boolean hasInputError() {
		for (Map<PropertyDescriptor, ConfigFieldModel> byProperty : _fields.values()) {
			for (ConfigFieldModel field : byProperty.values()) {
				if (field.getInputError() != null) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Takes back every violation {@link ConfigValidation#report(java.util.List, ConfigFieldIndex)}
	 * put on a field.
	 *
	 * <p>
	 * Run before each re-check, so a violation is shown for exactly as long as it holds: one that
	 * still holds is placed again by the very next {@code report}, one the user has fixed - possibly
	 * by editing a different field, which is how a cross-item constraint is fixed - is gone. A
	 * reported error left behind would otherwise outlive its own cause and make every further Apply
	 * refuse.
	 * </p>
	 *
	 * <p>
	 * Touches only the model-validation channel, never
	 * {@link com.top_logic.layout.form.model.AbstractFieldModel#getInputError() the input error}:
	 * that one belongs to the input control, is the last thing standing between Apply and silently
	 * discarding what the user typed, and is nobody's to take back here.
	 * </p>
	 */
	public void clearModelErrors() {
		for (Map<PropertyDescriptor, ConfigFieldModel> byProperty : _fields.values()) {
			for (ConfigFieldModel field : byProperty.values()) {
				field.setModelValidationError(null);
			}
		}
	}

	/**
	 * Forgets all registrations.
	 *
	 * <p>
	 * Every render cycle refills the index, so what an earlier cycle put there must go.
	 * </p>
	 */
	/** Remembers an entry that has been started but not yet confirmed. */
	public void registerPending(PendingEntry pending) {
		_pending.add(pending);
	}

	/** Forgets an entry that has been confirmed, discarded, or is no longer rendered. */
	public void unregisterPending(PendingEntry pending) {
		_pending.remove(pending);
	}

	/** The entries below the form that are still waiting to be confirmed. */
	public List<PendingEntry> pending() {
		return Collections.unmodifiableList(_pending);
	}

	public void clear() {
		_fields.clear();
		_pending.clear();
	}
}
