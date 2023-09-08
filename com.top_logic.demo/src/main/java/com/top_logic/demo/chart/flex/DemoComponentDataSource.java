/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart.flex;

import java.util.Collection;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.ComponentDataContext;

/**
 * {@link ChartDataSource} that selects {@link StructuredElement} children of the chart component's
 * master selection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoComponentDataSource implements ChartDataSource<ComponentDataContext> {

	/** Singleton {@link DemoComponentDataSource} instance. */
	public static final DemoComponentDataSource INSTANCE = new DemoComponentDataSource();

	/**
	 * Creates a new {@link DemoComponentDataSource}.
	 */
	protected DemoComponentDataSource() {
		// singleton instance
	}

	@Override
	public Collection<? extends Object> getRawData(ComponentDataContext context) {
		LayoutComponent master = context.getComponent().getMaster();
		StructuredElement selection = (StructuredElement) ((Selectable) master).getSelected();
		return selection.getChildren();
	}

}
