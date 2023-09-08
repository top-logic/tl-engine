/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.List;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that are informed about the change of the children of a
 * {@link LayoutContainer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ChildrenChangedListener extends PropertyListener {

	/**
	 * Handles the change of {@link LayoutContainer#getChildList()} of the sender component.
	 * 
	 * @param sender
	 *        The component on which {@link LayoutContainer#getChildList()} has changed.
	 * @param oldChildren
	 *        The former value of {@link LayoutContainer#getChildList()}.
	 * @param newValue
	 *        The new value of {@link LayoutContainer#getChildList()}.
	 * 
	 * @return Whether the event should be send to other listener.
	 */
	Bubble notifyChildrenChanged(LayoutContainer sender, List<LayoutComponent> oldChildren,
			List<LayoutComponent> newValue);

}

