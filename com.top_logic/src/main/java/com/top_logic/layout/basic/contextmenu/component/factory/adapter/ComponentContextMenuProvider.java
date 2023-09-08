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
import com.top_logic.layout.basic.contextmenu.component.factory.ComponentContextMenuFactory;

/**
 * {@link TypeBasedContextMenuProvider} using {@link ComponentContextMenuFactory} for creating the
 * context menu.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentContextMenuProvider extends TypeBasedContextMenuProvider {

	/**
	 * Configuration options for {@link ComponentContextMenuProvider}.
	 */
	public interface Config<I extends ComponentContextMenuProvider> extends TypeBasedContextMenuProvider.Config<I> {
		@Override
		@ItemDefault(ComponentContextMenuFactory.class)
		@ImplementationClassDefault(ComponentContextMenuFactory.class)
		PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();
	}

	/**
	 * Creates a {@link ComponentContextMenuProvider}.
	 */
	public ComponentContextMenuProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

}
