/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.VetoListener;

/**
 * {@link TabSwitchVetoListener} can be added to a {@link TabBarControl} to
 * avoid it from switching to another tab.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TabSwitchVetoListener extends VetoListener {

	/**
	 * Whether this {@link TabSwitchVetoListener} does not allows the given {@link TabBarModel} to
	 * switch to the given new tab.
	 * 
	 * @param tabBar
	 *        the {@link TabBarModel} which wants to switch.
	 * @param newTab
	 *        the tab to switch to.
	 * @throws VetoException
	 *         if this {@link TabSwitchVetoListener} does not allow the {@link TabBarModel} to
	 *         switch to the new tab
	 */
	public void checkVeto(TabBarModel tabBar, int newTab) throws VetoException;

}
