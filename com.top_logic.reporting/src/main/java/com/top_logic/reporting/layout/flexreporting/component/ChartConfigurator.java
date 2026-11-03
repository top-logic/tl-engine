/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.reporting.layout.meta.search.ChartComponent;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.wrap.StoredReport;

/**
 * Component configuring a chart.
 *
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public interface ChartConfigurator {
	/** Resource prefix of the aggregation functions. */
	public static final String AGGREGATION_FUNC_PREFIX = "reporting.aggregation.";
	/** Name of the form group with the chart details. */
	public static final String FORMGROUP     = "chartDetails";
	
	/** The configured report. */
	public ReportConfiguration getReportConfiguration();
	
	/** The component displaying the configured chart. */
	public ChartComponent getChartComponent();
	
	/** The form of this configurator. */
	public FormContext    getFormContext();
	
	/** The search component providing the objects of the chart. */
	public AttributedSearchComponent getSearchComponent();
	
//	public List getValidCharts();
	
	/** The component selecting the stored report. */
	public ReportQuerySelectorComponent getReportQuerySelector();

	/**
	 * Loads the given stored report into this configurator.
	 *
	 * @return Whether the report was accepted.
	 */
	public boolean acceptStoredReport(StoredReport aStoredReport);
	
}
