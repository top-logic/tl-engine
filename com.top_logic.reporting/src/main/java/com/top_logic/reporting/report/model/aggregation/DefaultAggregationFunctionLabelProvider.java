/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.reporting.layout.flexreporting.component.ConfigurationFormFieldHelper;
import com.top_logic.reporting.layout.provider.ResourcedMetaLabelProvider;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.util.Resources;

/**
 * Default label provider for aggregation functions.
 *
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
@Deprecated
public class DefaultAggregationFunctionLabelProvider implements AggregationFunctionLabelProvider {
	
	@Override
	public String getLabel(AggregationFunctionConfiguration anAggConf, ReportConfiguration aReportConfig) {
		ResourcedMetaLabelProvider provider = new ResourcedMetaLabelProvider(DefaultResourceProvider.INSTANCE, aReportConfig.getSearchMetaElement(), null);
		String funcName =
			Resources.getInstance().getString(
				ConfigurationFormFieldHelper.extractKey(anAggConf.getImplementationClass()));
		String attrName =
			anAggConf.getAttributePath().isEmpty() ? "" : "(" + provider.getLabel(anAggConf.getAttributePath()) + ")";
		return funcName + attrName;
	}
}
