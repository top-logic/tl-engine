/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ExpandAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.tree.TreeData;

/**
 * Implementation for the {@link ExpandAction}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ExpandActionOp extends AbstractModelActionOp<ExpandAction> {

	/**
	 * Creates a {@link ExpandActionOp} from configuration.
	 */
	public ExpandActionOp(InstantiationContext context, ExpandAction config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext actionContext, Object argument) {
		TreeData treeData = (TreeData) findModel(actionContext);
		Object node = actionContext.resolve(getConfig().getPath(), treeData);
		setExpansionState(treeData, node, getConfig().isExpand());
		return argument;
	}

	private boolean setExpansionState(TreeData treeData, Object node, boolean expand) {
		return treeData.getTreeModel().setExpanded(node, expand);
	}

}
