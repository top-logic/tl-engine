/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} for placeholder properties.
 */
public interface PlaceholderChangedListener extends PropertyListener {

	/**
	 * Informs this listener about a change of the placeholder value of a model.
	 *
	 * @param sender
	 *        The changed model.
	 * @param oldValue
	 *        The old placeholder.
	 * @param newValue
	 *        The new placeholder.
	 * @return Whether the event should bubble to containers.
	 */
	Bubble handlePlaceholderChanged(FormField sender, Object oldValue, Object newValue);

}
