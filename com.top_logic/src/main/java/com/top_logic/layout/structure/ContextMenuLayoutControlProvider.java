/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.config.WithPlainContextMenuFactory;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DecoratingLayoutControlProvider} for {@link LayoutComponent} which only have a context
 * menu as configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ContextMenuLayoutControlProvider<I extends ContextMenuLayoutControlProvider<?>>
		extends DecoratingLayoutControlProvider<ContextMenuLayoutControlProvider.Config<I>> {

	/**
	 * Configuration options for {@link ContextMenuLayoutControlProvider}.
	 */
	public interface Config<I> extends PolymorphicConfiguration<I>, WithPlainContextMenuFactory {
		// Pure sum interface.
	}

	private final ContextMenuFactory _contextMenu;

	/**
	 * Create a {@link ContextMenuLayoutControlProvider}.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to create the new object in.
	 * @param config
	 *        The configuration object to be used for instantiation.
	 */
	public ContextMenuLayoutControlProvider(InstantiationContext context, Config<I> config) {
		super(context, config);
		_contextMenu = context.getInstance(config.getContextMenuFactory());
	}

	/**
	 * Creates a {@link ContentControl} for the given component with a context menu.
	 * 
	 * @param component
	 *        {@link LayoutComponent} to get control for.
	 */
	protected ContentControl createContentWithMenu(LayoutComponent component) {
		return ContentControl.create(component, contextMenu().createContextMenuProvider(component));
	}

	/**
	 * The configured {@link ContextMenuFactory}.
	 */
	protected ContextMenuFactory contextMenu() {
		return _contextMenu;
	}

}

