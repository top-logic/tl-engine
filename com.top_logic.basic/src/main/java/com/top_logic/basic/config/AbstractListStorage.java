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
	 * Must only be called if key property is not <code>null</code>.
	 * 
	 * @param reference
	 *        the value of the key property in the item to remove.
	 */
	abstract void remove(K reference);

	/**
	 * Updates the item which has the same value for the key property as the
	 * given item.
	 * 
	 * Must only be called if key property is not <code>null</code>.
	 * 
	 * @param item
	 *        the new item
	 */
	abstract void update(V item);

	/**
	 * Updates the item which has the same value for the key property as the
	 * given item and moves it to the start of the list.
	 * 
	 * Must only be called if key property is not <code>null</code>.
	 * 
	 * @param item
	 *        the new item
	 */
	abstract void moveToStart(V item);

	/**
	 * Updates the item which has the same value for the key property as the
	 * given item and moves it to the end of the list.
	 * 
	 * Must only be called if key property is not <code>null</code>.
	 * 
	 * @param item
	 *        the new item
	 */
	abstract void moveToEnd(V item);

	/**
	 * Updates the item which has the same value for the key property as the
	 * given item and moves it directly before the item which has the given
	 * reference as value for the key property.
	 * 
	 * Must only be called if key property is not <code>null</code>.
	 * 
	 * @param item
	 *        the new item
	 * @param reference
	 *        the value of the key property in the item to move the given item
	 *        before.
	 */
	abstract void moveBefore(V item, K reference);

	/**
	 * Updates the item which has the same value for the key property as the
	 * given item and moves it directly after the item which has the given
	 * reference as value for the key property.
	 * 
	 * Must only be called if key property is not <code>null</code>.
	 * 
	 * @param item
	 *        the new item
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
	 * Must only be called if key property is not <code>null</code>.
	 * 
	 * @param reference
	 *        the value of the key property in the item to return.
	 * 
	 * @return the item which has the given reference as value for the key
	 *         property or <code>null</code> if nothing is found.
	 */
	abstract V resolveReferenceOrNull(K reference);
	
	/**
	 * the resulting list.
	 */
	abstract List<V> toList();

}
