/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.model;

import com.top_logic.reporting.view.component.AbstractProgressFilterComponent;
import com.top_logic.reporting.view.component.FilteringFilterVO;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * The {@link GanttChartSettings} holds informations that can be used by the
 * {@link AbstractProgressFilterComponent}s slave (e.g. the chart that uses this filter). These
 * informations are the startElement on which the slave should work and a map of
 * {@link FilterProperty}s identified by their names.
 * 
 * @author <a href=mailto:jes@top-logic.com>Jens Schäfer</a>
 */
public class GanttChartSettings extends FilteringFilterVO<Object> implements GanttChartConstants {

	/**
	 * Creates a new {@link GanttChartSettings}.
	 */
	public GanttChartSettings(Object model, BoundComponent component) {
		super(model);
	}

	@Override
	public boolean acceptElement(Object element, Object holder) {
		return false;
	}

	@Override
	public boolean acceptHolder(Object holder) {
		return false;
	}

}