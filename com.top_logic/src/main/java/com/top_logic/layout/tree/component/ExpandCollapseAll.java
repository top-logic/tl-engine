/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.TreeDataOwner;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * Expands all nodes in the {@link LayoutComponent}, if it is a {@link TreeDataOwner}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ExpandCollapseAll extends AbstractExpandCollapseAll {

	/** The id at the {@link CommandHandlerFactory} for the "expand all" command. */
	public static final String EXPAND_ID = "expandAll";

	/** The id at the {@link CommandHandlerFactory} for the "collapse all" command. */
	public static final String COLLAPSE_ID = "collapseAll";

	/** {@link TypedConfiguration} constructor for {@link ExpandCollapseAll}. */
	public ExpandCollapseAll(InstantiationContext context, AbstractExpandCollapseAll.Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(component instanceof TreeDataOwner)) {
			return new Hide();
		}
		TreeData treeData = ((TreeDataOwner) component).getTreeData();
		if (treeData == null) {
			return new Hide();
		}
		TreeUIModel<?> treeModel = treeData.getTreeModel();
		if (treeModel == null) {
			return new Hide();
		}
		boolean expand = expand();
		if (expand && !treeModel.isFinite()) {
			return new Hide();
		}
		return Success.toSuccess(ignored -> TreeUIModelUtil.setExpandedAll(treeModel, expand));
	}

}
