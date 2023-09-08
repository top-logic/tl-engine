/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.List;

import com.top_logic.basic.col.Provider;
import com.top_logic.layout.basic.contextmenu.menu.Menu;

/**
 * {@link PopupMenuModel} determining the commands by delegating to a {@link Provider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeferredPopupMenuModel extends DefaultButtonUIModel implements PopupMenuModel {

	private Provider<List<List<CommandModel>>> _menuProvider;

	/**
	 * Creates a new {@link DeferredPopupMenuModel}.
	 * 
	 * @param menuProvider
	 *        {@link Provider} for {@link #getMenu()}.
	 */
	public DeferredPopupMenuModel(Provider<List<List<CommandModel>>> menuProvider) {
		_menuProvider = menuProvider;
	}

	@Override
	public Menu getMenu() {
		return Menu.create(_menuProvider.get());
	}

}

