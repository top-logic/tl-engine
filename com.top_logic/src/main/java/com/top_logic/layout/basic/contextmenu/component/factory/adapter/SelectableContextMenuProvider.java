/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory.adapter;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.SelectableContextMenuFactory;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.AcceptableClassifiers;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link ComponentContextMenuProvider} using {@link SelectableContextMenuFactory} to create the
 * context menu.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Context menu of selectable component")
@InApp(classifiers = { "table" })
public class SelectableContextMenuProvider extends ComponentContextMenuProvider {

	/**
	 * Configuration options for {@link SelectableContextMenuProvider}.
	 */
	public interface Config<I extends SelectableContextMenuProvider> extends ComponentContextMenuProvider.Config<I> {
		@Override
		@ItemDefault(SelectableContextMenuFactory.class)
		@ImplementationClassDefault(SelectableContextMenuFactory.class)
		@Options(fun = InAppImplementations.class)
		@AcceptableClassifiers("table")
		PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();
	}

	/**
	 * Creates a {@link SelectableContextMenuProvider}.
	 */
	public SelectableContextMenuProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

}
