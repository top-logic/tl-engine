/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.listener;

/**
 * Callback that is informed for property changes of an {@link AttributeObservable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AttributeListener {

	/**
	 * Callback invoked by an {@link AttributeObservable}, if the given property has changed.
	 * 
	 * @param sender
	 *        The {@link AttributeObservable} whose property has changed.
	 * @param property
	 *        The concrete property that has changed.
	 * 
	 * @see AttributeObservable#addAttributeListener(String, AttributeListener)
	 */
	void handleAttributeUpdate(Object sender, String property);

}
