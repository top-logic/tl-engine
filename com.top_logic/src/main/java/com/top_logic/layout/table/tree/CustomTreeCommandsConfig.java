/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.List;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.basic.contextmenu.config.CustomContextMenuCommands;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;
import com.top_logic.tool.boundsec.CommandHandlerReference;

/**
 * Configuration template for {@link CustomContextMenuCommands} in a tree context.
 * 
 * @see TreeTableContextMenuFactory.Config#getCustomCommands()
 */
public interface CustomTreeCommandsConfig extends CustomContextMenuCommands.Config<CustomContextMenuCommands> {

	/**
	 * Configuration template that references the select subtree command from the application
	 * configuration.
	 */
	interface SelectSubTreeConfig extends CommandHandlerReference.Config {
		@Override
		@StringDefault(SelectSubtree.SELECT_SUBTREE_ID)
		@Hidden
		String getCommandId();
	}

	/**
	 * Configuration template that references the deselect subtree command from the application
	 * configuration.
	 */
	interface DeselectSubTreeConfig extends CommandHandlerReference.Config {
		@Override
		@StringDefault(SelectSubtree.DESELECT_SUBTREE_ID)
		@Hidden
		String getCommandId();
	}

	@ListDefault({ SelectSubTreeConfig.class, DeselectSubTreeConfig.class })
	@Override
	List<ConfigBase<? extends CommandHandler>> getCommands();

}
