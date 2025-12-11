/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.component.factory.SelectableContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.config.ContextMenuCommandsProvider;
import com.top_logic.layout.basic.contextmenu.config.CustomContextMenuCommands;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link SelectableContextMenuFactory} mapping node objects to their respective business objects.
 * 
 * @implNote This factory is used by default to create the context menu of a
 *           {@link TreeTableComponent}.
 * @see TreeTableContextMenuProvider.Config#getContextMenuFactory()
 * @see TLTreeNode#getBusinessObject()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Tree-table context menu commands")
@InApp(classifiers = "treetable")
public class TreeTableContextMenuFactory<C extends TreeTableContextMenuFactory.Config<?>>
		extends SelectableContextMenuFactory<C> {

	/**
	 * Configuration options for {@link TreeTableContextMenuFactory}.
	 */
	public interface Config<I extends TreeTableContextMenuFactory<?>> extends SelectableContextMenuFactory.Config<I> {

		@Override
		@ImplementationClassDefault(CustomContextMenuCommands.class)
		@ItemDefault(CustomTreeCommandsConfig.class)
		PolymorphicConfiguration<? extends ContextMenuCommandsProvider> getCustomCommands();

	}

	/**
	 * Creates a {@link TreeTableContextMenuFactory}.
	 */
	public TreeTableContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public ContextMenuProvider createContextMenuProvider(LayoutComponent component) {
		return new Provider(component);
	}

	/**
	 * {@link SelectableContextMenuFactory} wrapping node objects to their respective business
	 * objects.
	 */
	protected class Provider extends SelectableContextMenuFactory<C>.Provider {

		/**
		 * Creates a {@link Provider}.
		 */
		public Provider(LayoutComponent component) {
			super(component);
		}

		@Override
		protected Object mapContextObject(Object object) {
			if (object instanceof TLTreeNode) {
				return ((TLTreeNode<?>) object).getBusinessObject();
			}

			return null;
		}
	}

}
