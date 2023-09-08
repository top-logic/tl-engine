/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.List;

/**
 * {@link TextCutterFactory} for
 * {@link ConfiguredChartExportManager.DefaultFlexChartTemplateTextCutter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultFlexChartTemplateTextCutterFactory implements TextCutterFactory {

	/** Instance of {@link DefaultFlexChartTemplateTextCutterFactory}. */
	public static final DefaultFlexChartTemplateTextCutterFactory INSTANCE =
		new DefaultFlexChartTemplateTextCutterFactory();

	@Override
	public SlideReplacerBuilder.TextCutter createTextCutter(List<String> columns) {
		return new ConfiguredChartExportManager.DefaultFlexChartTemplateTextCutter(columns.size(),
			ConfiguredChartExportManager.DefaultAdditionalChartValueProvider.indexColumnFirst(columns));
	}

}
