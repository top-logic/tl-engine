/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.menu;

import java.util.List;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * {@link Group} of {@link MenuItem}s visually separated from other groups.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MenuGroup extends AbstractMenuContents implements MenuItem {

	@Override
	public boolean isVisible() {
		return hasVisibleEntries();
	}

	/**
	 * Backwards-compatible API for creating a {@link MenuGroup} from a list of buttons.
	 */
	public static MenuGroup create(List<? extends CommandModel> buttonGroup) {
		MenuGroup result = new MenuGroup();
		for (CommandModel button : buttonGroup) {
			result.add(CommandItem.create(button));
		}
		return result;
	}

}
