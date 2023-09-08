/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Configuration aspect for a {@link LayoutComponent} that allows configuration of a
 * {@link ContextMenuProvider}.
 * 
 * @see #getContextMenu()
 * @see WithContextMenuFactory
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface WithContextMenu extends ConfigurationItem {

	/** @see #getContextMenu() */
	String CONTEXT_MENU = "contextMenu";

	/**
	 * {@link ContextMenuProvider} that creates a {@link ContextMenuProvider} for this component.
	 */
	@Name(CONTEXT_MENU)
	@ItemDefault(NoContextMenuProvider.class)
	PolymorphicConfiguration<? extends ContextMenuProvider> getContextMenu();

}
