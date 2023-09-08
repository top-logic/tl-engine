/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link TableDragSourceByExpression} for tree tables.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "treetable" })
public class TreeTableDragSourceByExpression extends TableDragSourceByExpression {

	/**
	 * Creates a {@link TreeTableDragSourceByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TreeTableDragSourceByExpression(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean dragEnabled(TableData data, Object row) {
		return super.dragEnabled(data, nodeObject(row));
	}

	private Object nodeObject(Object node) {
		return ((TLTreeNode<?>) node).getBusinessObject();
	}

	@Override
	public Object getDragObject(TableData tableData, int row) {
		return ((TLTreeNode<?>) super.getDragObject(tableData, row)).getBusinessObject();
	}

}
