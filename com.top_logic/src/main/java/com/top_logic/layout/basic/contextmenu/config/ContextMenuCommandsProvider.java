/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.config;

import java.util.List;

import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Strategy for computing additional commands to display in the context menu of a certain type.
 * 
 * @see ContextMenuProvider Computing the final context menu for a given object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ContextMenuCommandsProvider {

	/**
	 * Whether {@link #getContextCommands(Object)} will return a non-empty list.
	 * 
	 * <p>
	 * Can be used for optimizing the decision, whether a context menu should be offered.
	 * </p>
	 */
	boolean hasContextMenuCommands(Object obj);

	/**
	 * Computes component commands to show in the context menu of the given object.
	 */
	List<CommandHandler> getContextCommands(Object obj);

}
