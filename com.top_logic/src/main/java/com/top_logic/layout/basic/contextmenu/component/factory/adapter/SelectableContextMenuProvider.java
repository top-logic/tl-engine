/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory.adapter;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.SelectableContextMenuFactory;

/**
 * {@link ComponentContextMenuProvider} using {@link SelectableContextMenuFactory} to create the
 * context menu.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectableContextMenuProvider extends ComponentContextMenuProvider {

	/**
	 * Configuration options for {@link SelectableContextMenuProvider}.
	 */
	public interface Config<I extends SelectableContextMenuProvider> extends ComponentContextMenuProvider.Config<I> {
		@Override
		@ItemDefault(SelectableContextMenuFactory.class)
		@ImplementationClassDefault(SelectableContextMenuFactory.class)
		PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();
	}

	/**
	 * Creates a {@link SelectableContextMenuProvider}.
	 */
	public SelectableContextMenuProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

}
