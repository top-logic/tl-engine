/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui.handler;

import com.top_logic.graphic.flow.callback.DiagramContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;

/**
 * {@link ContextMenuProvider} that can be attached to diagram elements.
 * 
 * @see ContextMenuHandler
 */
public interface DiagramContextMenuProviderSPI extends DiagramContextMenuProvider, ContextMenuProvider {
	// Pure sum interface.
}
