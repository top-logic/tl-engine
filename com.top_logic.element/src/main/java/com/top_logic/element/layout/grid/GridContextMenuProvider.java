/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.adapter.SelectableContextMenuProvider;

/**
 * {@link SelectableContextMenuProvider} using {@link GridContextMenuFactory} to create the context
 * menu.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GridContextMenuProvider extends SelectableContextMenuProvider {

	/**
	 * Configuration options for {@link GridContextMenuProvider}.
	 */
	public interface Config<I extends GridContextMenuProvider> extends SelectableContextMenuProvider.Config<I> {
		@Override
		@ItemDefault(GridContextMenuFactory.class)
		@ImplementationClassDefault(GridContextMenuFactory.class)
		PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();
	}

	/**
	 * Creates a {@link GridContextMenuProvider}.
	 */
	public GridContextMenuProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

}
