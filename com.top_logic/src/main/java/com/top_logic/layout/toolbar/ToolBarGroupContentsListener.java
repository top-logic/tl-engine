/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} for observing changes to {@link ToolBarGroup#getViews()} property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ToolBarGroupContentsListener extends PropertyListener {

	/**
	 * Notifies about a change to the {@link ToolBarGroup#getViews()}.
	 * 
	 * @param sender
	 *        The modified {@link ToolBarGroup}.
	 * @param oldValue
	 *        The old contents.
	 * @param newValue
	 *        The new contents.
	 * @return Whether to cancel event propagation.
	 */
	Bubble notifyToolBarGroupContentsChanged(ToolBarGroup sender, List<HTMLFragment> oldValue,
			List<HTMLFragment> newValue);

}
