/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.dnd.TableDropEvent;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link TableDropTarget} for tree tables that can be completely configured using model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "treetable" })
public class TreeTableDropTargetByExpression extends TableDropTargetByExpression {

	/**
	 * Creates a {@link TreeTableDropTargetByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TreeTableDropTargetByExpression(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object getReferenceRow(TableDropEvent event) {
		Object referencedObject = super.getReferenceRow(event);

		if (referencedObject == null) {
			return null;
		}

		return ((TLTreeNode<?>) referencedObject).getBusinessObject();
	}

}
