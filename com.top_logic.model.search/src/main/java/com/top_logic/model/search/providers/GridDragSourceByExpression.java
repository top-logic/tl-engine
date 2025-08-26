/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.layout.grid.GridDragSource;
import com.top_logic.layout.table.TableData;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Drag operation for grids that can be completely configured using model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "treegrid", "grid" })
@Label("Custom grid drag")
public class GridDragSourceByExpression extends TableDragSourceByExpression implements GridDragSource {

	private GridComponent _grid;

	/**
	 * Creates a {@link GridDragSourceByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public GridDragSourceByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, component -> {
			_grid = (GridComponent) component;
		});
	}

	@Override
	public boolean dragEnabled(TableData data, Object rowObject) {
		if (_grid.isEditing()) {
			if (_grid.getSelected() == unwrap(rowObject)) {
				return false;
			}
		}
		
		return super.dragEnabled(data, rowObject);
	}

	/**
	 * Converts the internal row object into the dragged object.
	 */
	@Override
	protected Object unwrap(Object rowObject) {
		return _grid.getBusinessObjectFromInternalRow(rowObject);
	}
}
