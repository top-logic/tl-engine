/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.pie;

import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.general.PieDataset;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.NumberUtil;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.PieDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.tooltip.DefaultTooltipGenerator;
import com.top_logic.reporting.flex.chart.config.tooltip.PieToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.url.PieURLGeneratorProvider;
import com.top_logic.util.TLContext;

/**
 * {@link AbstractJFreeChartBuilder} that builds pie-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class PieChartBuilder extends AbstractJFreeChartBuilder<PieDataset> {

	/**
	 * Config-interface for {@link PieChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<PieDataset> {

		@Override
		@ClassDefault(PieChartBuilder.class)
		public Class<? extends PieChartBuilder> getImplementationClass();

		@Override
		@InstanceFormat
		@InstanceDefault(PieDatasetBuilder.class)
		public DatasetBuilder<? extends PieDataset> getDatasetBuilder();

		/**
		 * Factory for a {@link PieToolTipGenerator}.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultTooltipGenerator.Provider.class)
		public PieToolTipGeneratorProvider getTooltipGeneratorProvider();

		/**
		 * see {@link #getTooltipGeneratorProvider()}
		 */
		public void setTooltipGeneratorProvider(PieToolTipGeneratorProvider provider);

		@Override
		public PieURLGeneratorProvider getURLGeneratorProvider();

		/**
		 * Maximum number of sections to use simple labels.
		 * 
		 * <p>
		 * When maximum is reached, the labels will be positioned outside the chart with linking
		 * lines to the matching section.
		 * </p>
		 */
		@IntDefault(10)
		int getSimpleLabelThreshold();

		/**
		 * A generator for the labels in the pie chart. May be <code>null</code>.
		 */
		@InstanceFormat
		@InstanceDefault(StandardPieSectionLabelGeneratorFactory.class)
		PieSectionLabelGeneratorFactory getLabelGenerator();

		/** <code>true</code> for enabling the background box for labels. */
		@BooleanDefault(false)
		boolean getDisplayLabelBorder();
	}

	/**
	 * Config-Constructor for {@link PieChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public PieChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public Class<PieDataset> datasetType() {
		return PieDataset.class;
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<PieDataset> chartData) {
		JFreeChart result = createPieChart(chartData.getDataset());
		return result;
	}

	private JFreeChart createPieChart(PieDataset dataSet) {
		setLabelProvider(getLabelProvider(0), dataSet.getKeys());

		return ChartFactory.createPieChart(getTitle(), dataSet, false,
			false, false);
	}

	@Override
	public void modifyPlot(JFreeChart model, ChartContext context, ChartData<PieDataset> chartData) {
		super.modifyPlot(model, context, chartData);
		PiePlot plot = (PiePlot) model.getPlot();
		PieDataset dataset = chartData.getDataset();
		int numberNon0Values = 0;

		for (Object key : (List<?>) dataset.getKeys()) {
			Comparable<?> theKey = (Comparable<?>) key;

			if (NumberUtil.getDoubleValue(dataset.getValue(theKey)) != 0.0d) {
				numberNon0Values++;
			}

			plot.setSectionPaint(theKey, getColor(theKey));
		}

		// Optimize labels in pie chart
		Config config = getConfig();

		plot.setLabelGenerator(config.getLabelGenerator().newGenerator(TLContext.getLocale()));
		plot.setIgnoreNullValues(true);
		plot.setIgnoreZeroValues(true);

		plot.setSimpleLabels(numberNon0Values < config.getSimpleLabelThreshold());

		if (!config.getDisplayLabelBorder()) {
			plot.setLabelBackgroundPaint(null);
			plot.setLabelOutlinePaint(null);
			plot.setLabelShadowPaint(null);
		}
	}

	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<PieDataset> chartData) {
		PiePlot plot = (PiePlot) model.getPlot();
		PieURLGenerator generator = getConfig().getURLGeneratorProvider().getPieTooltipGenerator(model, context, chartData);
		plot.setURLGenerator(generator);
	}

	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<PieDataset> chartData) {
		PiePlot plot = (PiePlot) model.getPlot();
		PieToolTipGenerator generator = getConfig().getTooltipGeneratorProvider().getPieTooltipGenerator(model, context, chartData);
		plot.setToolTipGenerator(generator);
	}

	@Override
	public int getMaxDimensions() {
		return 1;
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
	public static Config defaultConfig() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	/**
	 * Factory method to create an initialized {@link PieChartBuilder}.
	 * 
	 * @return a new PieChartBuilder.
	 */
	public static PieChartBuilder newInstance() {
		return (PieChartBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(defaultConfig());
	}

}
