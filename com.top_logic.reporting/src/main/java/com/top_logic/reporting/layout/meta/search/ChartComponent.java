/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.RevisedReport;

/**
 * Component displaying a chart of a {@link RevisedReport}.
 *
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public interface ChartComponent {
	
	/** The report displayed in the chart. */
	public RevisedReport getReport();
	
	/** The model of this component. */
	public Object getModel();
	
	/** The master of this component. */
	public LayoutComponent getMaster();

	/** The configuration of the displayed report. */
	public ReportConfiguration getReportConfiguration();
}
