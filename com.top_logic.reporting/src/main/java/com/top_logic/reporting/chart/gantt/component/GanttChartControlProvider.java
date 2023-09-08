/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} rendering the chart image of a {@link GanttComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GanttChartControlProvider implements LayoutControlProvider {

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
		LayoutControlAdapter chart = new LayoutControlAdapter(createChartDisplay((GanttComponent) component));
		chart.listenForInvalidation(component);
		chart.setConstraint(DefaultLayoutData.DEFAULT_CONSTRAINT);
		return chart;
	}

	/**
	 * Creates the actual {@link HTMLFragment} for the {@link GanttComponent}.
	 */
	protected HTMLFragment createChartDisplay(GanttComponent component) {
		return new GanttChartDisplay(component);
	}

}

