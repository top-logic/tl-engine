/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles change of the {@link Collapsible#isCollapsed() collapsed state} of a {@link Collapsible}.
 * 
 * @see Collapsible#COLLAPSED_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CollapsedListener extends PropertyListener {

	/**
	 * Handles change of the {@link Collapsible#isCollapsed() collapsed state} of the given
	 * {@link Collapsible}.
	 * 
	 * @param collapsible
	 *        The {@link Collapsible} whose collapsed state changed.
	 * @param oldValue
	 *        Whether the {@link Collapsible} was collapsed before.
	 * @param newValue
	 *        Whether the {@link Collapsible} is collapsed now.
	 * @return Whether this event shall bubble.
	 * 
	 * @see Collapsible#isCollapsed()
	 */
	Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue);

}

