/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * A collection of {@link ConfigurationItem}s the {@link ConfigListEditorControl} edits, seen as
 * rows: read them, address them by index, add, remove, reorder and replace them.
 *
 * <p>
 * The point of the interface is that the editor never learns where the rows come from.
 * {@link ConfigCollectionValue} holds a collection-valued property of a surrounding configuration,
 * where the shape (list, array or map) decides how a change is written back. A collection that is a
 * model attribute's value has no surrounding configuration and no property at all - and yet the same
 * editor should render it. Everything that differs between the two lives behind this interface;
 * everything the editor does is the same either way.
 * </p>
 *
 * <p>
 * The mutating methods are what a caller must go through. What {@link #elements()} hands out may or
 * may not be the live collection - only these pair a change with whatever write-back the underlying
 * shape needs.
 * </p>
 */
public interface ConfigCollection {

	/**
	 * The elements currently held, in the order they are rendered.
	 *
	 * <p>
	 * May be the live collection or a detached copy, depending on the implementation - which is
	 * exactly why a caller must not change it directly, see the class comment.
	 * </p>
	 */
	List<ConfigurationItem> elements();

	/** The position of the given element, or {@code -1} if it is not in the collection. */
	int indexOf(ConfigurationItem item);

	/**
	 * Whether the collection has an order the user can rearrange.
	 */
	boolean isReorderable();

	/**
	 * Whether the collection is indexed by a property of its entries.
	 *
	 * <p>
	 * A keyed collection cannot hold an entry whose key is empty or already taken, which is what
	 * makes a freshly created entry pending until the user has given it a key - see
	 * {@link ConfigPendingEntries}. An unkeyed collection takes a new entry straight away.
	 * </p>
	 */
	boolean isKeyed();

	/**
	 * The key property of the given entry, or {@code null} if the collection is not
	 * {@link #isKeyed() keyed}.
	 *
	 * <p>
	 * Resolved against the entry's own {@link ConfigurationItem#descriptor() descriptor}: for a
	 * polymorphic collection an entry's actual type is typically a subtype of the declared element
	 * type, and its descriptor creates a fresh {@link PropertyDescriptor} instance even for a
	 * property inherited unchanged. {@link PropertyDescriptor} has no
	 * {@link Object#equals(Object) equals}, so callers comparing key properties by identity only
	 * agree if every one of them asks here.
	 * </p>
	 */
	PropertyDescriptor keyProperty(ConfigurationItem entry);

	/**
	 * Whether some element of the collection carries the given key.
	 *
	 * @param key
	 *        The candidate key. Never {@code null} or empty.
	 */
	boolean hasEntryWithKey(Object key);

	/** Appends the given element. */
	void add(ConfigurationItem entry);

	/** Removes the element at the given position. */
	void remove(int index);

	/**
	 * Moves the element at the given position by the given number of places.
	 *
	 * @param delta
	 *        How far to move, negative towards the front. A move that would leave the collection is
	 *        ignored.
	 */
	void move(int index, int delta);

	/** Puts the given element in the place of the one at the given position. */
	void replace(int index, ConfigurationItem replacement);

	/**
	 * Creates an element of the collection's own element type, not yet part of it.
	 */
	ConfigurationItem newElement();

	/**
	 * What the collection is called, for the label of the button that adds to it.
	 */
	String label();

	/**
	 * The property the collection itself declares as the title of its entries, or {@code null} if
	 * it declares none.
	 *
	 * <p>
	 * Only the collection's own opinion - a {@code @TitleProperty} on the edited property, say. What
	 * an entry's own type says about its title, and the fallback to a conventional name, is the
	 * editor's business and the same whatever the collection is.
	 * </p>
	 *
	 * @param entry
	 *        The entry the title is resolved for; the answer is a property of its own descriptor,
	 *        for the reason {@link #keyProperty(ConfigurationItem)} explains.
	 */
	PropertyDescriptor titleProperty(ConfigurationItem entry);

}
