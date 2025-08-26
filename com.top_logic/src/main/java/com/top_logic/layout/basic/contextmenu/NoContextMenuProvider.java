/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu;

import com.top_logic.layout.basic.contextmenu.menu.Menu;

/**
 * {@link ContextMenuProvider} that provides no context menu at all.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NoContextMenuProvider implements ContextMenuProvider {

	/**
	 * Singleton {@link NoContextMenuProvider} instance.
	 */
	public static final NoContextMenuProvider INSTANCE = new NoContextMenuProvider();

	private NoContextMenuProvider() {
		// Singleton constructor.
	}

	@Override
	public boolean hasContextMenu(Object obj) {
		return false;
	}

	@Override
	public Menu getContextMenu(Object directTarget, Object model) {
		return null;
	}

}
