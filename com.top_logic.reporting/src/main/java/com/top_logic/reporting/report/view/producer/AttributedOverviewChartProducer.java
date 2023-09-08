/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.util.List;

import com.top_logic.base.chart.util.ChartType;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.report.control.producer.ChartContext;

/**
 * Produce a data set for any classification chart of an attributed object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AttributedOverviewChartProducer extends AbstractClassificationChartProducer {

	private static final List<ChartType> SUPPORTED_TYPES =
		new ListBuilder<ChartType>().add(ChartType.BAR_CHART).add(ChartType.PIE_CHART).toList();

	/**
	 * Singleton {@link AttributedOverviewChartProducer} instance.
	 */
	public static final AttributedOverviewChartProducer INSTANCE = new AttributedOverviewChartProducer();

	private AttributedOverviewChartProducer() {
		// Singleton constructor.
	}

    @Override
    protected ClassificationDatasetGenerator createGenerator(ChartContext aContext) {
		TLStructuredTypePart theMA = this.getMetaAttribute();

        return new ClassificationDatasetGenerator(this.model, this.getClassificationList().getName(), theMA.getName(), !theMA.isMandatory());
    }

    @Override
	public List<ChartType> getSupportedChartTypes() {
        return SUPPORTED_TYPES;
    }
}

