/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import com.top_logic.reporting.report.model.ReportConfiguration;

/**
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
@Deprecated
public interface AggregationFunctionLabelProvider {

	public String getLabel(AggregationFunctionConfiguration anAggConf, ReportConfiguration aReportConfig );

}
