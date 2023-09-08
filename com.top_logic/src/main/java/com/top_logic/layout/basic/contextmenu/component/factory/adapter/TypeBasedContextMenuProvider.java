/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory.adapter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.config.WithContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.TypeBasedContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ContextMenuProvider} implemented by a configured {@link ContextMenuFactory} using the
 * {@link LayoutComponent} taken from the configuration context.
 * 
 * <p>
 * By default, the {@link TypeBasedContextMenuFactory} is used that selects context menu commands
 * based on the type of the context object.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeBasedContextMenuProvider extends AbstractConfiguredInstance<TypeBasedContextMenuProvider.Config<?>>
		implements ContextMenuProvider {

	/**
	 * Configuration options for {@link TypeBasedContextMenuProvider}.
	 */
	public interface Config<I extends TypeBasedContextMenuProvider>
			extends PolymorphicConfiguration<I>, WithContextMenuFactory {

		@Override
		@DefaultContainer
		@ItemDefault(TypeBasedContextMenuFactory.class)
		@ImplementationClassDefault(TypeBasedContextMenuFactory.class)
		PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();

	}

	private ContextMenuProvider _provider;

	/**
	 * Creates a {@link TypeBasedContextMenuProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TypeBasedContextMenuProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class,
			x -> _provider = context.getInstance(config.getContextMenuFactory()).createContextMenuProvider(x));
	}

	@Override
	public boolean hasContextMenu(Object obj) {
		return _provider.hasContextMenu(obj);
	}

	@Override
	public Menu getContextMenu(Object obj) {
		return _provider.getContextMenu(obj);
	}

}
