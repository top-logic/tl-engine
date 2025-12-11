/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.config;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ContextMenuCommandsProvider} that provides no additional commands.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("No commands")
@InApp
public class NoContextMenuCommands implements ContextMenuCommandsProvider {

	/**
	 * Singleton {@link NoContextMenuCommands} instance.
	 */
	public static final NoContextMenuCommands INSTANCE = new NoContextMenuCommands();

	private NoContextMenuCommands() {
		// Singleton constructor.
	}

	@Override
	public boolean hasContextMenuCommands(Object obj) {
		return false;
	}

	@Override
	public List<CommandHandler> getContextCommands(Object obj) {
		return Collections.emptyList();
	}

}
