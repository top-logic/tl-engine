/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.table;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.grid.AbstractTreeGridBuilder;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Makes the root of the tree (in-)visible.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ToggleRootVisibilityCommand extends AbstractCommandHandler {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ToggleRootVisibilityCommand}.
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ToggleRootVisibilityCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component,
			Object model, Map<String, Object> arguments) {
		if (component instanceof GridComponent) {
			GridComponent grid = (GridComponent) component;
			ModelBuilder gridBuilder = grid.getBuilder();
			if (gridBuilder instanceof AbstractTreeGridBuilder) {
				AbstractTreeGridBuilder<?> builder = (AbstractTreeGridBuilder<?>) gridBuilder;
				builder.setRootVisible(!builder.isRootVisible());
				grid.invalidate();
				return HandlerResult.DEFAULT_RESULT;
			}
		}
		if (component instanceof TreeTableComponent) {
			TreeTableComponent treeTable = (TreeTableComponent) component;
			treeTable.setRootVisible(!treeTable.isRootVisible());
			return HandlerResult.DEFAULT_RESULT;
		}

		throw new IllegalArgumentException("There is no tree in '" + component + "' whose root can be made invisible.");
	}

}
