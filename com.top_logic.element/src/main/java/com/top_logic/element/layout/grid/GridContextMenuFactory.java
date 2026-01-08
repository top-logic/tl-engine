/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.element.layout.grid.GridComponent.*;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.basic.contextmenu.component.factory.SelectableContextMenuFactory;
import com.top_logic.layout.table.tree.TreeTableContextMenuFactory;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link SelectableContextMenuFactory} that maps grid rows to business objects.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Grid context menu commands")
@InApp(classifiers = "grid")
public class GridContextMenuFactory<C extends TreeTableContextMenuFactory.Config<?>>
		extends TreeTableContextMenuFactory<C> {

	/**
	 * Creates a {@link GridContextMenuFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GridContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected Object mapContextObject(LayoutComponent component, Object obj) {
		GridComponent grid = (GridComponent) component;
		return getRowObject(grid.getHandler().getGridRow(obj));
	}

}
