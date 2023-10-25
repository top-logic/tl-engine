/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.tree.component.AbstractExpandCollapseAll;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;

/**
 * Expands all nodes in the {@link TreeTableComponent}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TreeTableExpandCollapseAll extends AbstractTreeTableExpandCollapseAll {

	/** The id at the {@link CommandHandlerFactory} for the "expand all" command. */
	public static final String EXPAND_ID = "treeTableExpandAll";

	/** The id at the {@link CommandHandlerFactory} for the "collapse all" command. */
	public static final String COLLAPSE_ID = "treeTableCollapseAll";

	/** {@link TypedConfiguration} constructor for {@link TreeTableExpandCollapseAll}. */
	public TreeTableExpandCollapseAll(InstantiationContext context, AbstractExpandCollapseAll.Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(component instanceof TreeTableComponent)) {
			return new Hide();
		}

		return prepare(((TreeTableComponent) component).getTableData());
	}

}
