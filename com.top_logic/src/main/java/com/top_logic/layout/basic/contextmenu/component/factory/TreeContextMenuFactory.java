/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link SelectableContextMenuFactory} that maps its context element tree nodes to their business
 * objects before processing.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeContextMenuFactory<C extends TreeContextMenuFactory.Config<?>>
		extends SelectableContextMenuFactory<C> {

	/**
	 * Creates a {@link TreeContextMenuFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TreeContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public ContextMenuProvider createContextMenuProvider(LayoutComponent component) {
		return new Provider(component);
	}

	/**
	 * {@link ContextMenuProvider} created by {@link TreeContextMenuFactory}.
	 */
	protected class Provider extends SelectableContextMenuFactory<C>.Provider {

		/**
		 * Creates a {@link Provider}.
		 */
		public Provider(LayoutComponent component) {
			super(component);
		}

		@Override
		protected Object mapContextObject(Object obj) {
			@SuppressWarnings("unchecked")
			Object result = ((TreeComponent) getComponent()).getTreeData().getTreeModel().getBusinessObject(obj);
			return result;
		}

	}

}