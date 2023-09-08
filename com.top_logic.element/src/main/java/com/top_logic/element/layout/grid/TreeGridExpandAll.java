/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractSystemCommand;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} that expands all nodes in a tree grid.
 * 
 * @see AbstractTreeGridBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeGridExpandAll extends AbstractSystemCommand {

	public static final String COMMAND_ID = "treeGridExpandAll";

	public TreeGridExpandAll(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, 
			LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {

		TreeUIModel<?> treeModel = AbstractTreeGridBuilder.getTreeModel(aComponent);
		TreeUIModelUtil.setExpandedAll(treeModel, treeModel.getRoot(), true);
		return HandlerResult.DEFAULT_RESULT;
	}

	public static CommandHandler getInstance() {
		return CommandHandlerFactory.getInstance().getHandler(COMMAND_ID);
	}
	
}