/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import java.util.List;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} for observing changes to the {@link ToolBar#getGroups()} property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ToolbarGroupsListener extends PropertyListener {

	/**
	 * Notifies about a change in the {@link ToolBar#getGroups()} property of the given sender.
	 * 
	 * @param sender
	 *        The modified {@link ToolBar}.
	 * @param oldValue
	 *        The old groups.
	 * @param newValue
	 *        The new groups.
	 * @return Whether to stop event propagation.
	 */
	Bubble notifyToolbarGroupsChanged(ToolBar sender, List<? extends ToolBarGroup> oldValue,
			List<? extends ToolBarGroup> newValue);

}
