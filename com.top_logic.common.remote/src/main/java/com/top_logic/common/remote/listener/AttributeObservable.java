/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.listener;

/**
 * Object that can be observed for property changes.
 * 
 * @see AttributeListener
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AttributeObservable {

	/**
	 * Adds an {@link AttributeListener} that is informed if the given property changes.
	 * 
	 * @param property
	 *        The name of the property to observe, or <code>null</code> to observe changes for all
	 *        properties of this object.
	 * @param listener
	 *        The listener to call upon property changes.
	 * @return Whether the given listener was newly added, <code>false</code>, if the given listener
	 *         was already added.
	 * 
	 * @see #removeAttributeListener(String, AttributeListener)
	 */
	boolean addAttributeListener(String property, AttributeListener listener);

	/**
	 * Removes a listener added with {@link #addAttributeListener(String, AttributeListener)}.
	 * 
	 * @param property
	 *        The property name the given listener was listening for changes, <code>null</code> if
	 *        the listener was registered for all properties.
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the given listener was added before.
	 */
	boolean removeAttributeListener(String property, AttributeListener listener);

}
