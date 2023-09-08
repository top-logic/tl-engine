/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.bar;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.color.ColorProvider;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * {@link JFreeChartBuilder} that created stacked bar charts depending on the number of dimensions.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class DynamicStackedBarChartBuilder extends AbstractJFreeChartBuilder<CategoryDataset> {

	/**
	 * Config-interface for {@link DynamicStackedBarChartBuilder}.
	 */
	public interface Config extends AbstractJFreeChartBuilder.Config<CategoryDataset> {

		@Override
		@ClassDefault(DynamicStackedBarChartBuilder.class)
		public Class<DynamicStackedBarChartBuilder> getImplementationClass();

	}

	StackedBarChartBuilder _stackedBarBuilder;

	GroupedStackedBarChartBuilder _groupedStackedBarBuilder;

	/**
	 * Config-constructor for {@link DynamicStackedBarChartBuilder}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public DynamicStackedBarChartBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_stackedBarBuilder = StackedBarChartBuilder.instance();
		_groupedStackedBarBuilder = GroupedStackedBarChartBuilder.instance();
		PropertyDescriptor property =
			TypedConfiguration.getConfigurationDescriptor(Config.class).getProperty(
				JFreeChartBuilder.Config.COLOR_PROVIDER);
		config.addConfigurationListener(property, new ConfigurationListener() {

			@Override
			public void onChange(ConfigurationChange change) {
				_stackedBarBuilder.getConfig().setColorProvider((ColorProvider) change.getNewValue());
				_groupedStackedBarBuilder.getConfig().setColorProvider((ColorProvider) change.getNewValue());
			}
		});
	}

	@Override
	public Class<CategoryDataset> datasetType() {
		return CategoryDataset.class;
	}

	@Override
	public DatasetBuilder<CategoryDataset> getDatasetBuilder() {
		return new DatasetBuilder<>() {

			@Override
			public Class<CategoryDataset> getDatasetType() {
				return CategoryDataset.class;
			}

			@Override
			public CategoryDataset getDataset(ChartTree tree) {
				return DynamicStackedBarChartBuilder.this.getBuilder(tree).getDatasetBuilder().getDataset(tree);
			}
		};
	}

	BarChartBuilder getBuilder(ChartTree tree) {
		if (tree.getDepth() >= 3) {
			return _groupedStackedBarBuilder;
		}
		return _stackedBarBuilder;
	}

	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		getBuilder(chartData.getModel()).setUrlGenerator(model, context, chartData);
	}

	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		getBuilder(chartData.getModel()).setTooltipGenerator(model, context, chartData);
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<CategoryDataset> chartData) {
		BarChartBuilder builder = getBuilder(chartData.getModel());
		builder.getConfig().setTitleKey(getConfig().getTitleKey());
		return builder.createChart(context, chartData);
	}

	@Override
	public int getMaxDimensions() {
		return 3;
	}

	@Override
	public int getMinDimensions() {
		return 1;
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	/**
	 * Factory method to create an initialized {@link DynamicStackedBarChartBuilder}.
	 * 
	 * @return a new DynamicStackedBarChartBuilder.
	 */
	public static DynamicStackedBarChartBuilder instance() {
		return (DynamicStackedBarChartBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(item());
	}

}
