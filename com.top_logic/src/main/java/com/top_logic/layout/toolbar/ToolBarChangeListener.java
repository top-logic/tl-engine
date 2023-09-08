/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.table.TableData;

/**
 * {@link PropertyListener} for {@link TableData#getToolBar()} changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ToolBarChangeListener extends PropertyListener {

	/**
	 * Notifies about a change to the {@link TableData#getToolBar()} property.
	 * 
	 * @param sender
	 *        The modified {@link TableData}.
	 * @param oldValue
	 *        The old {@link ToolBar}.
	 * @param newValue
	 *        The new {@link ToolBar}.
	 */
	void notifyToolbarChange(ToolBarOwner sender, ToolBar oldValue, ToolBar newValue);

}
