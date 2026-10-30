/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigPendingEntries.PendingEntry;

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

	private final List<Consumer<ConfigFieldModel>> _observers = new ArrayList<>();

	private final Map<ConfigurationItem, Map<PropertyDescriptor, ConfigFieldModel>> _fields =
		new IdentityHashMap<>();

	/**
	 * The entries a collection editor below the form has started and nobody has confirmed yet.
	 *
	 * <p>
	 * A pending entry is not in the configuration - that is what pending means - so validation
	 * cannot see it, and applying would rebuild the form over the original and drop it without a
	 * word. Whoever renders one registers it here, so the form can confirm it before handing the
	 * configuration over, see {@link #confirmPending()}.
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
		for (Consumer<ConfigFieldModel> observer : _observers) {
			observer.accept(model);
		}
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
	/**
	 * Watches every field the editor builds, now and on every later rebuild.
	 *
	 * <p>
	 * The way to learn that something in the edited configuration changed. A
	 * {@link ConfigurationItem} offers listeners per property only, so there is no asking a whole
	 * tree; but every change the user can make goes through a field the editor built and reported
	 * here, and every structural change - an element added, removed or reordered - rebuilds the
	 * affected editor and reports its fields afresh. Watching registration therefore catches both,
	 * without a listener tree of its own to keep in step.
	 * </p>
	 *
	 * <p>
	 * The observer is called for each field as it is registered, not for those already in the
	 * index: it is meant to attach something to each field, and a caller that installs it before
	 * the editor is built sees every one.
	 * </p>
	 */
	public void observeFields(Consumer<ConfigFieldModel> observer) {
		_observers.add(observer);
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
	 * The fields registered for the properties of the given item.
	 *
	 * @param item
	 *        The configuration item the fields belong to.
	 * @return The fields, empty if none was registered.
	 */
	public Collection<ConfigFieldModel> fieldsOf(ConfigurationItem item) {
		Map<PropertyDescriptor, ConfigFieldModel> byProperty = _fields.get(item);
		return byProperty == null ? Collections.emptyList() : List.copyOf(byProperty.values());
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
	 * Whether some field shows an error: an input it rejected, or a violation a check placed on it.
	 *
	 * <p>
	 * What a command saving the configuration is disabled over: the problems are on screen, so the
	 * save would be refused anyway.
	 * </p>
	 */
	public boolean hasVisibleError() {
		for (Map<PropertyDescriptor, ConfigFieldModel> byProperty : _fields.values()) {
			for (ConfigFieldModel field : byProperty.values()) {
				if (field.hasError()) {
					return true;
				}
			}
		}
		return false;
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
	 * {@link ConfigValidation#report(ConfigValidation.Findings, ConfigFieldIndex)} placed: those go
	 * to the separate model-validation channel, see {@link #clearFindings()}. Were the two to share
	 * one slot, a violation the user has since fixed elsewhere would keep reading as "an entry cannot
	 * be read" and refuse every further Apply with nothing on screen to correct.
	 * </p>
	 *
	 * <p>
	 * The key field of a {@link #pending() pending entry} does not count: its complaint is one
	 * about a key already spoken for, not about input that could not be read. It is the reason
	 * confirming the entry would be refused, and it is reported as such by whoever confirms it, see
	 * {@link #confirmPending()}.
	 * </p>
	 *
	 * @see com.top_logic.layout.form.model.AbstractFieldModel#getInputError()
	 */
	public boolean hasInputError() {
		Set<Object> keyFields = Collections.newSetFromMap(new IdentityHashMap<>());
		for (PendingEntry pending : _pending) {
			Object keyField = pending.keyFieldModel();
			if (keyField != null) {
				keyFields.add(keyField);
			}
		}
		return hasInputError(keyFields);
	}

	private boolean hasInputError(Set<Object> ignored) {
		for (Map<PropertyDescriptor, ConfigFieldModel> byProperty : _fields.values()) {
			for (ConfigFieldModel field : byProperty.values()) {
				if (field.getInputError() != null && !ignored.contains(field)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Takes back everything
	 * {@link ConfigValidation#report(ConfigValidation.Findings, ConfigFieldIndex)} put on a field -
	 * both a {@link ConfigValidation.Violation} and a {@link ConfigValidation.Warning}.
	 *
	 * <p>
	 * Run before each re-check, so a finding is shown for exactly as long as it holds: one that
	 * still holds is placed again by the very next {@code report}, one the user has fixed - possibly
	 * by editing a different field, which is how a cross-item constraint is fixed - is gone. A
	 * reported error left behind would otherwise outlive its own cause and make every further Apply
	 * refuse; a warning left behind would question a value nobody has any reason to look at twice.
	 * </p>
	 *
	 * <p>
	 * Touches only the two model-validation channels, never
	 * {@link com.top_logic.layout.form.model.AbstractFieldModel#getInputError() the input error}:
	 * that one belongs to the input control, is the last thing standing between Apply and silently
	 * discarding what the user typed, and is nobody's to take back here.
	 * </p>
	 */
	public void clearFindings() {
		for (Map<PropertyDescriptor, ConfigFieldModel> byProperty : _fields.values()) {
			for (ConfigFieldModel field : byProperty.values()) {
				field.setModelValidationError(null);
				field.setModelValidationWarnings(Collections.emptyList());
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

	/**
	 * Confirms every entry still waiting to be confirmed, exactly as if the user had confirmed each
	 * of them with its own Confirm button.
	 *
	 * <p>
	 * What a form surrounding the editor runs before it hands the edited configuration over: the
	 * user who saves has finished editing, and an entry filled in but not confirmed is part of what
	 * is saved, not something to be dropped or to block the save with.
	 * </p>
	 *
	 * <p>
	 * An entry is confirmed only after every pending entry nested inside it: confirming an entry
	 * rebuilds the editor around it, which would drop an entry still pending within. An entry inside
	 * which a confirmation was refused therefore stays pending as well, so nothing the user entered
	 * is lost. Each refused entry shows its reason at its key field, see
	 * {@link ConfigPendingEntries#confirm(PendingEntry)}.
	 * </p>
	 *
	 * @return The reason the first refused entry gave, or {@code null} if every entry joined its
	 *         collection.
	 */
	public ResKey confirmPending() {
		Set<PendingEntry> tried = Collections.newSetFromMap(new IdentityHashMap<>());
		Map<PendingEntry, ResKey> refused = new IdentityHashMap<>();
		ResKey result = null;
		while (true) {
			PendingEntry next = nextToConfirm(tried, refused.keySet());
			if (next == null) {
				break;
			}
			tried.add(next);
			ResKey refusal = next.confirm();
			if (refusal != null) {
				refused.put(next, refusal);
				if (result == null) {
					result = refusal;
				}
			}
		}
		// Confirming an entry re-examines the keys of all others and rebuilds the editor, which
		// takes back what an earlier refusal reported. The reasons are reported once more, where the
		// refused entries are displayed now.
		for (Map.Entry<PendingEntry, ResKey> entry : refused.entrySet()) {
			entry.getKey().setKeyFieldInputError(entry.getValue());
		}
		return result;
	}

	/**
	 * A pending entry not yet tried that holds no other pending entry still in the way, or
	 * {@code null} if there is none.
	 *
	 * <p>
	 * An entry is in the way if it is neither confirmed yet nor refused, or if it was refused.
	 * </p>
	 */
	private PendingEntry nextToConfirm(Set<PendingEntry> tried, Set<PendingEntry> refused) {
		List<PendingEntry> pending = List.copyOf(_pending);
		for (PendingEntry candidate : pending) {
			if (tried.contains(candidate)) {
				continue;
			}
			boolean blocked = false;
			for (PendingEntry other : pending) {
				if (other == candidate || (tried.contains(other) && !refused.contains(other))) {
					continue;
				}
				ConfigurationItem target = other.collectionOwner();
				if (target != null && reaches(candidate.entry(), target,
					Collections.newSetFromMap(new IdentityHashMap<>()))) {
					blocked = true;
					break;
				}
			}
			if (!blocked) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Whether the given target is the given item or lies somewhere within it, reached through its
	 * ITEM, LIST, ARRAY and MAP properties.
	 *
	 * <p>
	 * Walks down rather than up along the containers: only a
	 * {@link com.top_logic.basic.config.container.ConfigPart} knows its container, so walking up
	 * would miss an entry pending below an item that is none, and drop it.
	 * </p>
	 *
	 * @param visited
	 *        Items already walked, guarding against an item reachable twice and against a cycle.
	 */
	private static boolean reaches(ConfigurationItem item, ConfigurationItem target, Set<ConfigurationItem> visited) {
		if (item == null || !visited.add(item)) {
			return false;
		}
		if (item == target) {
			return true;
		}
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			if (property.isDerived()) {
				// Computed from elsewhere, possibly from outside the item.
				continue;
			}
			for (Object value : nestedValues(item, property)) {
				if (value instanceof ConfigurationItem nested && reaches(nested, target, visited)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * The values of the given property that may be nested items.
	 */
	private static Collection<?> nestedValues(ConfigurationItem item, PropertyDescriptor property) {
		switch (property.kind()) {
			case ITEM:
				return Collections.singletonList(item.value(property));
			case LIST: {
				Collection<?> entries = (Collection<?>) item.value(property);
				return entries == null ? Collections.emptyList() : entries;
			}
			case ARRAY: {
				List<?> entries = PropertyDescriptorImpl.arrayAsList(item.value(property));
				return entries == null ? Collections.emptyList() : entries;
			}
			case MAP: {
				Map<?, ?> entries = (Map<?, ?>) item.value(property);
				return entries == null ? Collections.emptyList() : entries.values();
			}
			default:
				return Collections.emptyList();
		}
	}

	/**
	 * Forgets all registrations.
	 *
	 * <p>
	 * Every render cycle refills the index, so what an earlier cycle put there must go.
	 * </p>
	 */
	public void clear() {
		_fields.clear();
		_pending.clear();
	}
}
