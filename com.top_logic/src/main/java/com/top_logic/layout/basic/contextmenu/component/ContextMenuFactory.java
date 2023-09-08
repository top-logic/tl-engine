/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component;

import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Factory for a {@link ContextMenuProvider} in a {@link LayoutComponent} context.
 * 
 * <p>
 * The {@link LayoutComponent} context is necessary to invoke component commands (
 * {@link CommandHandler}s) from context menus.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ContextMenuFactory {

	/**
	 * Creates the {@link ContextMenuProvider} for the given {@link LayoutComponent}.
	 */
	ContextMenuProvider createContextMenuProvider(LayoutComponent component);

}
