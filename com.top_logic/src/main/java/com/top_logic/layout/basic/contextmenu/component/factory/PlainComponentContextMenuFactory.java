/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.contextmenu.config.ContextMenuCommandsProvider;
import com.top_logic.layout.basic.contextmenu.config.NoContextMenuCommands;
import com.top_logic.layout.provider.label.NoLabelProvider;

/**
 * {@link ComponentContextMenuFactory} that does not consider the current component model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PlainComponentContextMenuFactory<C extends PlainComponentContextMenuFactory.Config<?>>
		extends ComponentContextMenuFactory<C> {

	/**
	 * Configuration options for {@link PlainComponentContextMenuFactory}.
	 */
	public interface Config<I extends PlainComponentContextMenuFactory<?>>
			extends ComponentContextMenuFactory.Config<I> {

		@Override
		@InstanceDefault(NoLabelProvider.class)
		LabelProvider getTitleProvider();

		@Override
		@ItemDefault(NoContextMenuCommands.class)
		PolymorphicConfiguration<? extends ContextMenuCommandsProvider> getCustomCommands();

	}

	/**
	 * Creates a {@link PlainComponentContextMenuFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PlainComponentContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
	}

}
