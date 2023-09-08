/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * Provides the resources for a grid internal row object.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class GridRowResourceProvider extends AbstractResourceProvider {

	private GridComponent _grid;

	/**
	 * Creates a {@link GridRowResourceProvider}.
	 */
	public GridRowResourceProvider(GridComponent grid) {
		_grid = grid;
	}

	@Override
	public String getLabel(Object internalRowObject) {
		Object businessObject = getBusinessObject(internalRowObject);

		if (GridComponent.isTransient(businessObject)) {
			return null;
		}

		return MetaLabelProvider.INSTANCE.getLabel(businessObject);
	}

	@Override
	public ThemeImage getImage(Object internalRowObject, Flavor flavor) {
		Object businessObject = getBusinessObject(internalRowObject);

		if (GridComponent.isTransient(businessObject)) {
			DefaultResourceProvider.getTypeImage(GridComponent.getTypeName(businessObject), flavor);
		}

		return MetaResourceProvider.INSTANCE.getImage(businessObject, flavor);
	}

	@Override
	public String getTooltip(Object internalRowObject) {
		Object businessObject = getBusinessObject(internalRowObject);

		if (GridComponent.isTransient(businessObject)) {
			return null;
		}

		return MetaResourceProvider.INSTANCE.getTooltip(businessObject);
	}

	@Override
	public String getLink(DisplayContext context, Object internalRowObject) {
		Object businessObject = getBusinessObject(internalRowObject);

		if (GridComponent.isTransient(businessObject)) {
			return null;
		}

		return MetaResourceProvider.INSTANCE.getLink(context, businessObject);
	}

	private Object getBusinessObject(Object internalRowObject) {
		return GridComponent.getRowObject(_grid.getHandler().getGridRow(internalRowObject));
	}
}
