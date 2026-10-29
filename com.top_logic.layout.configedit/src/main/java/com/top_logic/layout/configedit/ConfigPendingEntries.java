/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.container.ConfigPartUtilInternal;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * The entries a {@link ConfigListEditorControl} has created but not yet put into the edited
 * collection.
 *
 * <p>
 * A keyed collection is indexed by a property of its entries, so it cannot hold one whose key is
 * already taken, nor one whose key is empty unless it
 * {@link ConfigCollection#acceptsAnonymous(ConfigurationItem) accepts anonymous entries} - and a
 * freshly created entry's key is empty. Such an entry lives here until the user confirms it with a
 * key the collection accepts. Any number may be pending at once, independently: a keyed
 * collection's constraint is on the entries actually in it, and a pending entry, by definition, is
 * not.
 * </p>
 *
 * <p>
 * "Not in the collection" is what this class exists to make explicit. Every operation on a pending
 * entry is named here, so a caller cannot reach for the collection's own index and quietly do
 * nothing when it comes back {@code -1} - which is exactly how a type change on a pending entry
 * once got lost (see {@link #replaceEntry(PendingEntry, ConfigurationItem)}).
 * </p>
 *
 * <p>
 * Nothing here draws anything. It holds the entries and the rules that decide when one may join the
 * collection; the control renders them and hands each entry's key field back through
 * {@link PendingEntry#setKeyFieldModel(AbstractFieldModel)}, which is where a refused confirmation
 * reports itself.
 * </p>
 */
public final class ConfigPendingEntries {

	private final ConfigCollection _value;

	private final Consumer<ConfigurationItem> _onChanged;

	private final List<PendingEntry> _entries = new ArrayList<>();

	/**
	 * One pending entry, paired with the field model built for its key field: the
	 * {@link ConfigFieldModel} of a key that is typed, the model of the type selector of a key that
	 * is the entry's type.
	 *
	 * <p>
	 * The pairing is why this class exists: the entry and its key field model used to be two
	 * separate fields that had to be set and cleared in lockstep by hand. With several entries
	 * pending simultaneously that lockstep would have had to be repeated per list index across two
	 * parallel lists - keeping the pair in one object removes the chance of the two ever drifting
	 * apart.
	 * </p>
	 *
	 * <p>
	 * Neither field is {@code final}. The entry is replaced wholesale when the user picks a
	 * different type for a still-pending entry of a polymorphic collection; the holder is what every
	 * closure of the current render captured, so swapping the entry inside it keeps those closures
	 * pointing at the right thing. The key field model is replaced on every render cycle, since the
	 * control discards and recreates all its child controls each time.
	 * </p>
	 */
	public static final class PendingEntry {

		private final ConfigPendingEntries _owner;

		private ConfigurationItem _entry;

		private AbstractFieldModel _keyFieldModel;

		PendingEntry(ConfigPendingEntries owner, ConfigurationItem entry) {
			_owner = owner;
			_entry = entry;
		}

		/**
		 * Moves this entry into the edited collection, as its own Confirm button does.
		 *
		 * @return The reason it was refused, or {@code null} if it joined the collection.
		 *
		 * @see ConfigPendingEntries#confirm(PendingEntry)
		 */
		public ResKey confirm() {
			return _owner.confirm(this);
		}

		/** The entry itself, not yet part of the edited collection. */
		public ConfigurationItem entry() {
			return _entry;
		}

		/**
		 * Remembers the field model of the key field just built for this entry - the type selector
		 * of a collection keyed by its entries' type.
		 *
		 * <p>
		 * Called on every render cycle, so that a refused
		 * {@link ConfigPendingEntries#confirm(PendingEntry)} never reports its error on a control
		 * that is no longer displayed.
		 * </p>
		 */
		public void setKeyFieldModel(AbstractFieldModel keyFieldModel) {
			_keyFieldModel = keyFieldModel;
		}

		/**
		 * The field model of the key field last built for this entry, or {@code null} if it has
		 * none.
		 */
		AbstractFieldModel keyFieldModel() {
			return _keyFieldModel;
		}

		/**
		 * The configuration whose collection this entry joins once confirmed, or {@code null} if
		 * the collection is part of none.
		 *
		 * @see ConfigCollection#container()
		 */
		ConfigurationItem collectionOwner() {
			return _owner._value.container();
		}

		/**
		 * Reports the given complaint about what has been typed into this entry's key, or clears it
		 * when {@code null} is given.
		 *
		 * <p>
		 * The input channel, not {@link ConfigFieldModel#setModelValidationError(ResKey)}: a key
		 * already spoken for is a complaint about the text in the field, it must survive the
		 * clearing that a refused Apply does to the verdicts <em>it</em> placed, and the next
		 * keystroke should drop it - all of which is what that channel does.
		 * </p>
		 */
		void setKeyFieldInputError(ResKey message) {
			if (_keyFieldModel != null) {
				_keyFieldModel.setError(message);
			}
		}
	}

	/**
	 * Creates a {@link ConfigPendingEntries}.
	 *
	 * @param value
	 *        The collection a confirmed entry joins.
	 * @param onChanged
	 *        Re-renders the editor, expanding the given entry, or none if it is {@code null}. Called
	 *        by every operation here that changes what is displayed.
	 */
	public ConfigPendingEntries(ConfigCollection value, Consumer<ConfigurationItem> onChanged) {
		_value = value;
		_onChanged = onChanged;
	}

	/**
	 * The pending entries, in the order they were created.
	 *
	 * <p>
	 * The control renders them in this order after every committed entry, so a newly created entry
	 * always appears at the end - a stable, predictable place.
	 * </p>
	 */
	public List<PendingEntry> entries() {
		return Collections.unmodifiableList(_entries);
	}

	/** Starts a new pending entry for the given, freshly created item. */
	public PendingEntry start(ConfigurationItem entry) {
		attach(entry);
		PendingEntry pending = new PendingEntry(this, entry);
		_entries.add(pending);
		_onChanged.accept(entry);
		return pending;
	}

	/**
	 * Moves the given entry into the edited collection, on the user's explicit confirmation.
	 *
	 * <p>
	 * Confirming is a deliberate action of its own rather than something inferred from the key
	 * field's value. Committing as soon as the key merely <em>looks</em> usable would take a
	 * half-typed key for the finished one - a key field reports what has been typed so far, so
	 * anyone not typing the whole key in one go would find the entry committed under a prefix of it,
	 * its key field by then already fixed. Committing when the field loses focus would be no better:
	 * leaving the field to fetch the value from somewhere else is exactly what one does while
	 * filling it in.
	 * </p>
	 *
	 * <p>
	 * An empty key is accepted only where the collection
	 * {@link ConfigCollection#acceptsAnonymous(ConfigurationItem) accepts anonymous entries}: the
	 * entry then joins it without a key. Otherwise both an empty key and one that another entry
	 * already uses are reported at the key field, and the entry stays pending. Neither is silently
	 * tolerated: the user asked for this entry to be taken, and has to be told why it was not.
	 * Inserting a duplicate key would otherwise be rejected by {@link TypedConfiguration} with a
	 * message about the collection's index, which says nothing to whoever is editing the form.
	 * </p>
	 *
	 * <p>
	 * "Another entry" means one already in the collection -
	 * {@link ConfigCollectionValue#hasEntryWithKey(Object)} does not also consult the entries here,
	 * deliberately. A pending entry has claimed nothing yet: its key is still being written, and two
	 * entries being filled in side by side may well pass through the same intermediate text.
	 * Whichever is confirmed first takes the key; the other is then told the key is taken, at the
	 * moment it is confirmed - which is the moment its key is actually being claimed.
	 * </p>
	 *
	 * @param pending
	 *        The entry to confirm.
	 * @return The reason it was refused, or {@code null} if it joined the collection. Also reported
	 *         at the entry's key field, where there is one.
	 */
	public ResKey confirm(PendingEntry pending) {
		ConfigurationItem entry = pending._entry;
		PropertyDescriptor keyProperty = _value.keyProperty(entry);
		Object key = entry.value(keyProperty);
		if (ConfigUtil.isAnonymousKey(key)) {
			if (_value.acceptsAnonymous(entry)) {
				return commit(pending);
			}
			return refuse(pending,
				I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY.fill(Labels.propertyLabel(keyProperty, false)));
		}
		if (_value.hasEntryWithKey(key)) {
			return refuse(pending, I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE
				.fill(Labels.propertyLabel(keyProperty, false), keyLabel(key)));
		}
		return commit(pending);
	}

	private ResKey commit(PendingEntry pending) {
		ConfigurationItem entry = pending._entry;
		_entries.remove(pending);
		// Joining the collection makes it the entry's container for real.
		detach(entry);
		_value.add(entry);
		checkKeys();
		_onChanged.accept(entry);
		return null;
	}

	/**
	 * Reports the given reason at the entry's key field and hands it back for the caller to show.
	 *
	 * <p>
	 * The field is where the reason belongs when there is one, but an entry need not show one: a
	 * collection keyed by the entry's type offers no type selector where there is a single type to
	 * choose from - and a refusal nobody is told about is the one thing this must never be. So the
	 * reason travels back either way.
	 * </p>
	 */
	private ResKey refuse(PendingEntry pending, ResKey reason) {
		pending.setKeyFieldInputError(reason);
		return reason;
	}

	/**
	 * Re-reports, at each pending entry's key field, whether the key typed there is already spoken
	 * for.
	 *
	 * <p>
	 * Called on every key change, so the complaint appears while the name is being typed rather than
	 * only when the entry is confirmed - otherwise the user finishes an entry before learning that
	 * its name was never available.
	 * </p>
	 *
	 * <p>
	 * Every entry is re-examined, not only the one just typed into: a clash has two ends, and
	 * renaming one of them has to clear the complaint at the other. An empty key is not complained
	 * about - it is unfinished, not wrong.
	 * </p>
	 *
	 * <p>
	 * Complaining is broader than refusing. {@link #confirm(PendingEntry)} still refuses only a key
	 * an entry <em>in the collection</em> holds: two entries being filled in side by side may pass
	 * through the same text, and whichever is confirmed first is entitled to the name. The other is
	 * then told the key is taken - by then truthfully, since it now is.
	 * </p>
	 */
	public void checkKeys() {
		for (PendingEntry pending : _entries) {
			pending.setKeyFieldInputError(clash(pending));
		}
	}

	/**
	 * What is wrong with the key of the given pending entry, or {@code null} if nothing is.
	 */
	private ResKey clash(PendingEntry pending) {
		ConfigurationItem entry = pending.entry();
		PropertyDescriptor keyProperty = _value.keyProperty(entry);
		Object key = entry.value(keyProperty);
		if (ConfigUtil.isAnonymousKey(key)) {
			return null;
		}
		String propertyLabel = Labels.propertyLabel(keyProperty, false);
		if (_value.hasEntryWithKey(key)) {
			return I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE.fill(propertyLabel, keyLabel(key));
		}
		for (PendingEntry other : _entries) {
			if (other != pending && key.equals(other.entry().value(_value.keyProperty(other.entry())))) {
				return I18NConstants.ERROR_DUPLICATE_PENDING_KEY__PROPERTY_VALUE.fill(propertyLabel, keyLabel(key));
			}
		}
		return null;
	}

	/**
	 * Whether the given item, not yet started here, would take a key already in use - by an entry
	 * of the collection or by another pending entry.
	 *
	 * <p>
	 * An empty key takes nothing: it is not yet a key at all.
	 * </p>
	 */
	public boolean isKeyTaken(ConfigurationItem item) {
		Object key = item.value(_value.keyProperty(item));
		if (ConfigUtil.isAnonymousKey(key)) {
			return false;
		}
		if (_value.hasEntryWithKey(key)) {
			return true;
		}
		for (PendingEntry other : _entries) {
			if (key.equals(other.entry().value(_value.keyProperty(other.entry())))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The given key as the user sees it.
	 *
	 * <p>
	 * A collection keyed by its entries' type has a {@link Class} as key: it is named as the type
	 * selector names it, see {@link PolymorphicOptions#labelFor(Object)}, not by the class's own
	 * text. A text key is itself, any other key is named by its label.
	 * </p>
	 */
	static String keyLabel(Object key) {
		if (key instanceof Class<?>) {
			return PolymorphicOptions.labelFor(key);
		}
		if (key instanceof String text) {
			return text;
		}
		return MetaLabelProvider.INSTANCE.getLabel(key);
	}

	/**
	 * Drops the given entry - its own Remove action, which cannot remove it from the edited
	 * collection since it was never added there. Any other pending entries are left untouched.
	 */
	public void discard(PendingEntry pending) {
		_entries.remove(pending);
		detach(pending._entry);
		checkKeys();
		_onChanged.accept(null);
	}

	/**
	 * Puts a differently typed item in the place of a pending entry, keeping its holder.
	 *
	 * <p>
	 * This is the operation a pending entry needs and the collection cannot provide: a pending entry
	 * is not in the collection, so it has no index there to replace. Left to
	 * {@link ConfigCollectionValue#indexOf(ConfigurationItem)}, picking a type for a pending entry
	 * came back {@code -1} and did nothing at all - the select showed the newly picked type while
	 * the entry kept the old one, and confirming took an entry of a type nobody had chosen.
	 * </p>
	 *
	 * @param replacement
	 *        The item of the newly picked type, with whatever was already filled in carried over.
	 */
	public void replaceEntry(PendingEntry pending, ConfigurationItem replacement) {
		detach(pending._entry);
		attach(replacement);
		pending._entry = replacement;
		_onChanged.accept(replacement);
	}

	/**
	 * Gives the given pending entry the collection's {@link ConfigCollection#container() container}
	 * as its own, before it is part of the collection.
	 *
	 * <p>
	 * A pending entry is filled in as any other entry: its options, derived values and constraints
	 * are computed while it is pending. Whatever of these navigates outwards from the entry - an
	 * annotation's option function asking the annotated element, say - would find nothing, and
	 * offer something else than once the entry is confirmed.
	 * </p>
	 */
	private void attach(ConfigurationItem entry) {
		ConfigurationItem container = _value.container();
		if (container != null && entry instanceof ConfigPart part && part.container() == null) {
			ConfigPartUtilInternal.initContainer(part, container);
		}
	}

	/**
	 * Takes back what {@link #attach(ConfigurationItem)} gave the given entry.
	 */
	private void detach(ConfigurationItem entry) {
		ConfigurationItem container = _value.container();
		if (container != null && entry instanceof ConfigPart part && part.container() == container) {
			ConfigPartUtilInternal.clearContainer(part);
		}
	}

}
