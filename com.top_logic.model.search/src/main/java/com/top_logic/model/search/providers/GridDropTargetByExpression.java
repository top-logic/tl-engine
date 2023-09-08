/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.layout.table.dnd.TableDropEvent;
import com.top_logic.layout.table.dnd.TableDropTarget;

/**
 * {@link TableDropTarget} for grids that can be completely configured using model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "grid" })
public class GridDropTargetByExpression extends TableDropTargetByExpression {

	/**
	 * Creates a {@link GridDropTargetByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GridDropTargetByExpression(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object getReferenceRow(TableDropEvent event) {
		Object internalRowObject = super.getReferenceRow(event);

		if (internalRowObject == null) {
			return null;
		}

		return ((GridComponent) _contextComponent).getBusinessObjectFromInternalRow(internalRowObject);
	}
}
