/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.adapter.SelectableContextMenuProvider;

/**
 * {@link SelectableContextMenuProvider} choosing {@link TreeTableContextMenuFactory} by default.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeTableContextMenuProvider extends SelectableContextMenuProvider {

	/**
	 * Configuration options for {@link TreeTableContextMenuProvider}.
	 */
	public interface Config<I extends TreeTableContextMenuProvider> extends SelectableContextMenuProvider.Config<I> {
		@Override
		@ItemDefault(TreeTableContextMenuFactory.class)
		@ImplementationClassDefault(TreeTableContextMenuFactory.class)
		PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();
	}

	/**
	 * Creates a {@link TreeTableContextMenuProvider}.
	 */
	public TreeTableContextMenuProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

}
