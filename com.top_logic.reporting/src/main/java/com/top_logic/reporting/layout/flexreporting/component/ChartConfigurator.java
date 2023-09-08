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
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public interface ChartConfigurator {
	public static final String AGGREGATION_FUNC_PREFIX = "reporting.aggregation.";
	public static final String FORMGROUP     = "chartDetails";
	
	public ReportConfiguration getReportConfiguration();
	
	public ChartComponent getChartComponent();
	
	public FormContext    getFormContext();
	
	public AttributedSearchComponent getSearchComponent();
	
//	public List getValidCharts();
	
	public ReportQuerySelectorComponent getReportQuerySelector();

	public boolean acceptStoredReport(StoredReport aStoredReport);
	
}
