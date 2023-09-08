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
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public interface ChartComponent {
	
	public RevisedReport getReport();
	
	public Object getModel();
	
	public LayoutComponent getMaster();

	public ReportConfiguration getReportConfiguration();
}
