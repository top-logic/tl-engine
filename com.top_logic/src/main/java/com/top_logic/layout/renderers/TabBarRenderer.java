/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.tabbar.TabBarControl;
import com.top_logic.layout.tabbar.TabBarModel;

/**
 * The {@link TabBarRenderer} will be used in the {@link TabBarControl} to render the
 * {@link TabBarModel} of that control.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TabBarRenderer extends ControlRenderer<TabBarControl> {

	/**
	 * This method returns the size of the bar
	 */
	public int getBarSize();

	/**
	 * This method returns the size between the tab bar and the content of the shown tab.
	 */
	public int getBottomSpacerSize();
}
