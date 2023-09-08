/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering.chart;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceDataEntryAggregated;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.monitoring.rendering.LayoutComponentNode;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.ComponentDataContext;

/**
 * {@link ChartDataSource}-implementation for performance-chart.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class PerformanceChartDatasource implements ChartDataSource<ComponentDataContext> {

	/**
	 * Creates a new {@link PerformanceChartDatasource}.
	 * 
	 */
	public PerformanceChartDatasource() {
	}

	@Override
	public Collection<? extends Object> getRawData(ComponentDataContext context) {

		LayoutComponentNode node = (LayoutComponentNode) context.getComponent().getSelectableMaster().getSelected();
		if (node == null) {
			return Collections.emptyList();
		}
		LayoutComponent comp = node.getComponent();
		LayoutComponent buttons = node.findButtonOrDirectComponent(comp, false);
		if (buttons == null) {
			buttons = node.findButtonOrDirectComponent(comp, true);
		}

		if (buttons != null) {
			comp = buttons;
		}

		ComponentName compName = comp.getName();

		Map<Long, Set<PerformanceDataEntryAggregated>> performanceData =
			PerformanceMonitor.getInstance().getPerformanceData(null, null, null, null, null, null, null,
				Collections.singletonList(compName.qualifiedName()));

		return Collections.singletonList(performanceData);
	}

}