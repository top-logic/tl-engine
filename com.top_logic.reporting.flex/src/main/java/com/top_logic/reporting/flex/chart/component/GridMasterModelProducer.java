/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.config.datasource.ComponentDataContext;
import com.top_logic.reporting.flex.chart.config.datasource.MasterModelProducer;

/**
 * Provide the business objects in the master component (expecting that component to be a
 * {@link GridComponent}).
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class GridMasterModelProducer extends MasterModelProducer {

	/**
	 * Creates a new {@link GridMasterModelProducer}.
	 */
	public GridMasterModelProducer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Collection<? extends Object> getRawData(ComponentDataContext context) {
		List<Object> theObjects = new ArrayList<>();

		for (FormGroup group : getFormGroups(context)) {
			Object rowObject = group.get(GridComponent.PROP_ATTRIBUTED);

			// beware: rowObject may be a new, transient object
			if (rowObject instanceof TLObject) {
				theObjects.add(rowObject);
			}
		}

		return theObjects;
	}

	/**
	 * Return the relevant form groups from the grid component (which has to be the master of the
	 * chart component).
	 * 
	 * @param context
	 *        The chart context for finding the {@link GridComponent} in.
	 * 
	 * @return The requested list of {@link FormGroup}s, never <code>null</code>.
	 */
	protected List<FormGroup> getFormGroups(ComponentDataContext context) {
		GridComponent grid = (GridComponent) context.getComponent().getMaster();

		return grid.getAllVisibleFormGroups();
	}
}
