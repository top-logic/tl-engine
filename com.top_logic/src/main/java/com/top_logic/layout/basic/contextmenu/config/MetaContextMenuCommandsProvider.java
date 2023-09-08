/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.config;

import java.util.List;

import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ContextMenuCommandsProvider} based on global application-level configuration.
 * 
 * @see LabelProviderService#getContextCommands(Object)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaContextMenuCommandsProvider implements ContextMenuCommandsProvider {

	/**
	 * Singleton {@link MetaContextMenuCommandsProvider} instance.
	 */
	public static final MetaContextMenuCommandsProvider INSTANCE = new MetaContextMenuCommandsProvider();

	private MetaContextMenuCommandsProvider() {
		// Singleton constructor.
	}

	@Override
	public boolean hasContextMenuCommands(Object obj) {
		return service().hasContextMenuCommands(obj);
	}

	@Override
	public List<CommandHandler> getContextCommands(Object obj) {
		return service().getContextCommands(obj);
	}

	private LabelProviderService service() {
		return LabelProviderService.getInstance();
	}

}
