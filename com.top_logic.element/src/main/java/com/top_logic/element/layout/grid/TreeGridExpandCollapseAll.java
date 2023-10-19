/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.grid.AbstractTreeGridBuilder.TreeGridHandler;
import com.top_logic.element.layout.grid.GridBuilder.GridHandler;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.tree.AbstractTreeTableExpandCollapseAll;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;

/**
 * Expands or collapse all nodes in the {@link GridComponent}.
 * 
 * @see AbstractTreeGridBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeGridExpandCollapseAll extends AbstractTreeTableExpandCollapseAll {

	/** The id at the {@link CommandHandlerFactory} for the "expand all" command. */
	public static final String EXPAND_ID = "treeGridExpandAll";

	/** The id at the {@link CommandHandlerFactory} for the "collapse all" command. */
	public static final String COLLAPSE_ID = "treeGridCollapseAll";

	/** {@link TypedConfiguration} constructor for {@link TreeGridExpandCollapseAll}. */
	public TreeGridExpandCollapseAll(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(component instanceof GridComponent)) {
			return new Hide();
		}
		GridHandler<FormGroup> handler = ((GridComponent) component)._handler;
		if (!(handler instanceof TreeGridHandler)) {
			return new Hide();
		}
		TableField tableField = ((AbstractTreeGridBuilder<?>.TreeGridHandler) handler).getTableField();
		if (!(tableField instanceof TreeTableData)) {
			return new Hide();
		}
		return prepare((TreeTableData) tableField);
	}
	
}