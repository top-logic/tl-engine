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
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.ComponentContextMenuFactory;

/**
 * Configuration aspect defining a {@link ContextMenuFactory} property.
 * 
 * @see WithContextMenu
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface WithContextMenuFactory extends ConfigurationItem {

	/**
	 * @see #getContextMenuFactory()
	 */
	String CONTEXT_MENU_FACTORY = "contextMenuFactory";

	/**
	 * {@link ContextMenuFactory} creating the {@link ContextMenuProvider} for the configured
	 * component.
	 */
	@Name(CONTEXT_MENU_FACTORY)
	@ItemDefault(ComponentContextMenuFactory.class)
	PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();

}
