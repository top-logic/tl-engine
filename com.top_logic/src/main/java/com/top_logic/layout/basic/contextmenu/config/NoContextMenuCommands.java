/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.config;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ContextMenuCommandsProvider} that provides no additional commands.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoContextMenuCommands implements ContextMenuCommandsProvider {

	/**
	 * Configuration options for {@link NoContextMenuCommands}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Config extends PolymorphicConfiguration<NoContextMenuCommands> {
		/* 
		 * Empty.
		 *
		 * This is necessary to prevent writing configuration defaults for content layouting when
		 * exporting layouts. See #26263. 
		 */
	}

	/**
	 * Creates a {@link NoContextMenuCommands} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public NoContextMenuCommands(InstantiationContext context, Config config) {
		// Empty.
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
