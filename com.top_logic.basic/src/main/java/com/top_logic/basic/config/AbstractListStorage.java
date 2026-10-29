/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.List;

import com.top_logic.basic.Log;

/**
 * Abstract super class used in {@link ConfigurationReader} to modify list
 * valued properties.
 *
 * <p>
 * Items are added by value and addressed either by the value of their key property (a reference),
 * by their position, or by the item itself (for items already in the list). An item of a keyed
 * list whose key value is <code>null</code> is anonymous: it is stored in order, but no reference
 * matches it.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AbstractListStorage<K, V> {

	protected final Log _protocol;

	public AbstractListStorage(Log protocol) {
		_protocol = protocol;
	}

	/**
	 * Appends the given item to the resulting list.
	 */
	abstract void append(V item);

	/**
	 * Sets the given item at the start of the resulting list.
	 */
	abstract void prepend(V item);

	/**
	 * Inserts the given item before the item which has the given reference as
	 * value for the key property.
	 *
	 * Must only be called if key property is not <code>null</code>.
	 *
	 * @param reference
	 *        the value of the key property in the item to insert the given item
	 *        before.
	 * @param item
	 *        the item to insert
	 */
	abstract void insertBefore(K reference, V item);

	/**
	 * Inserts the given item after the item which has the given reference as
	 * value for the key property.
	 *
	 * Must only be called if key property is not <code>null</code>.
	 *
	 * @param reference
	 *        the value of the key property in the item to insert the given item
	 *        after.
	 * @param item
	 *        the item to insert
	 */
	abstract void insertAfter(K reference, V item);

	/**
	 * Removes the item which has the given reference as value for the key
	 * property.
	 *
	 * <p>
	 * If the list contains no item with the given key value, the list stays unchanged and a
	 * warning is reported: a configuration layer may remove an entry that the underlying layers do
	 * not define.
	 * </p>
	 *
	 * Must only be called if key property is not <code>null</code>.
	 *
	 * @param reference
	 *        the value of the key property in the item to remove.
	 */
	abstract void remove(K reference);

	/**
	 * Replaces an item of the list with a new item at the same position.
	 *
	 * <p>
	 * The new item may have a different key value than the replaced one. It must not have the key
	 * value of another item of the list.
	 * </p>
	 *
	 * @param oldItem
	 *        The item in the list to replace.
	 * @param newItem
	 *        The item that takes the position of the old item.
	 */
	abstract void replace(V oldItem, V newItem);

	/**
	 * Moves the given item of the list to the start of the list.
	 *
	 * @param item
	 *        An item contained in the list.
	 */
	abstract void moveToStart(V item);

	/**
	 * Moves the given item of the list to the end of the list.
	 *
	 * @param item
	 *        An item contained in the list.
	 */
	abstract void moveToEnd(V item);

	/**
	 * Moves the given item of the list directly before the item which has the given reference as
	 * value for the key property.
	 *
	 * Must only be called if key property is not <code>null</code>.
	 *
	 * @param item
	 *        An item contained in the list.
	 * @param reference
	 *        the value of the key property in the item to move the given item
	 *        before.
	 */
	abstract void moveBefore(V item, K reference);

	/**
	 * Moves the given item of the list directly after the item which has the given reference as
	 * value for the key property.
	 *
	 * Must only be called if key property is not <code>null</code>.
	 *
	 * @param item
	 *        An item contained in the list.
	 * @param reference
	 *        the value of the key property in the item to move the given item
	 *        after.
	 */
	abstract void moveAfter(V item, K reference);

	/**
	 * Returns the item which has the given reference as value for the key
	 * property.
	 *
	 * Must only be called if key property is not <code>null</code>.
	 *
	 * @param reference
	 *        the value of the key property in the item to return.
	 *
	 * @return the item which has the given reference as value for the key
	 *         property or <code>null</code> if none .
	 */
	abstract V resolveReference(K reference);

	/**
	 * Returns the item which has the given reference as value for the key
	 * property or <code>null</code> if it is not found.
	 *
	 * <p>
	 * A <code>null</code> reference matches no item, not even an anonymous one.
	 * </p>
	 *
	 * @param reference
	 *        the value of the key property in the item to return.
	 *
	 * @return the item which has the given reference as value for the key
	 *         property or <code>null</code> if nothing is found.
	 */
	abstract V resolveReferenceOrNull(K reference);

	/**
	 * Returns the item at the given zero-based position of the list.
	 *
	 * @param index
	 *        The position of the requested item.
	 * @return The item at the given position, or <code>null</code> if the index is out of range.
	 */
	abstract V resolveIndexOrNull(int index);

	/**
	 * The number of items in the list.
	 */
	abstract int size();

	/**
	 * the resulting list.
	 */
	abstract List<V> toList();

}
