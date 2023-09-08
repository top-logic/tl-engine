/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config;

import org.jfree.chart.JFreeChart;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.DefaultModelPreparation;
import com.top_logic.reporting.flex.chart.config.model.ModelPreparation;

/**
 * Base config-interface for {@link JFreeChart}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface ChartConfig extends ConfigurationItem {

	/**
	 * <code>DATA_SOURCE</code> Attribute name for data-source-property
	 */
	public static final String DATA_SOURCE = "data-source";

	/**
	 * <code>CHART_BUILDER</code> Attribute name for chart-builder-property
	 */
	public static final String CHART_BUILDER = "chart-builder";

	/**
	 * <code>MODEL_PREPARATION</code> Attribute name for model-preparation-property
	 */
	public static final String MODEL_PREPARATION = "model-preparation";

	/**
	 * Getter for the {@link ChartDataSource} that provides the initial data for the chart.
	 */
	@SuppressWarnings("rawtypes")
	@Name(DATA_SOURCE)
	@InstanceFormat
	public ChartDataSource getDataSource();

	/**
	 * see {@link #getDataSource()}
	 */
	@SuppressWarnings("rawtypes")
	public void setDataSource(ChartDataSource dataSource);

	/**
	 * Getter for the {@link ModelPreparation} that builds a {@link ChartTree} from the initial
	 * data.
	 */
	@Name(MODEL_PREPARATION)
	@InstanceFormat
	@ImplementationClassDefault(DefaultModelPreparation.class)
	public ModelPreparation getModelPreparation();

	/**
	 * see {@link #getModelPreparation()}
	 */
	public void setModelPreparation(ModelPreparation model);

	/**
	 * Getter for the {@link JFreeChartBuilder} that translates the internal modle (
	 * {@link ChartTree}) to a {@link JFreeChart}.
	 */
	@SuppressWarnings("rawtypes")
	@Name(CHART_BUILDER)
	@InstanceFormat
	public JFreeChartBuilder getChartBuilder();

	/**
	 * see {@link #getChartBuilder()}
	 */
	@SuppressWarnings("rawtypes")
	public void setChartBuilder(JFreeChartBuilder builder);

}
