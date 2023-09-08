/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering.chart;

import java.awt.Color;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalSeriesCollection;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.util.Resources;

/**
 * {@link JFreeChartBuilder}-implementation for performance-chart.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class PerformanceChartBuilder extends AbstractJFreeChartBuilder<YIntervalSeriesCollection> {

	/**
	 * Config-iterface for {@link PerformanceChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<YIntervalSeriesCollection> {

		@Override
		@ClassDefault(PerformanceChartBuilder.class)
		public Class<? extends PerformanceChartBuilder> getImplementationClass();

		@Override
		@InstanceFormat
		@InstanceDefault(YIntervalSeriesBuilder.class)
		public DatasetBuilder<? extends YIntervalSeriesCollection> getDatasetBuilder();

	}

	/**
	 * Config-constructor for a {@link PerformanceChartBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public PerformanceChartBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Class<YIntervalSeriesCollection> datasetType() {
		return YIntervalSeriesCollection.class;
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<YIntervalSeriesCollection> chartData) {

		final Date date = new Date();

		Resources resources = Resources.getInstance();
		ResKey xAxisLabelKey = getConfig().getXAxisLabelKey();
		String currentDate = HTMLFormatter.getInstance().formatShortDateTime(date);
		String xAxisLabel = resources.getMessage(xAxisLabelKey, currentDate);
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setNumberFormatOverride(new NumberFormat() {

			private SimpleDateFormat _innerFormat = CalendarUtil.newSimpleDateFormat("HH:mm");

			@Override
			public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
				Date value = DateUtil.addSeconds(date, (int) (number * 60));

				StringBuffer format = _innerFormat.format(value, toAppendTo, pos);
				return format;
			}

			@Override
			public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
				Date value = DateUtil.addSeconds(date, (int) (number * 60));

				return _innerFormat.format(value, toAppendTo, pos);
			}

			@Override
			public Number parse(String source, ParsePosition parsePosition) {
				Date parsedDate = _innerFormat.parse(source, parsePosition);
				if (parsedDate == null) {
					return null;
				}
				Date normalisedDate;
				try {
					normalisedDate = _innerFormat.parse(_innerFormat.format(date));
				} catch (ParseException ex) {
					throw new UnreachableAssertion("Parse a string formatted by same format.", ex);
				}
				long differenceInSeconds = DateUtil.difference(normalisedDate, parsedDate) / (60 * 1000);
				return Long.valueOf(differenceInSeconds);
			}

		});

		NumberAxis yAxis = new NumberAxis(getYAxisLabel());
		yAxis.setNumberFormatOverride(new NumberFormat() {

			private SimpleDateFormat _innerFormat = CalendarUtil.newSimpleDateFormat("mm:ss,SSS");

			@Override
			public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
				return _innerFormat.format(new Date((long) number), toAppendTo, pos);
			}

			@Override
			public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
				return _innerFormat.format(new Date(number), toAppendTo, pos);
			}

			@Override
			public Number parse(String source, ParsePosition parsePosition) {
				Date parsedDate = _innerFormat.parse(source, parsePosition);
				if (parsedDate == null) {
					return null;
				}
				return Long.valueOf(parsedDate.getTime());
			}

		});

		XYErrorRenderer renderer = new XYErrorRenderer();
		renderer.setDefaultToolTipGenerator(
			new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					return (String) dataset.getSeriesKey(series) + " (" + dataset.getYValue(series, item) + ")";
				}
			});


		XYPlot xyplot = new XYPlot(chartData.getDataset(), xAxis, yAxis, renderer);
		JFreeChart jfreechart = new JFreeChart(getTitle(), xyplot);
		ChartFactory.getChartTheme().apply(jfreechart);
		return jfreechart;
	}

	@Override
	public int getMaxDimensions() {
		return 1;
	}

	@Override
	public int getMinDimensions() {
		return 1;
	}

	@Override
	protected void modifyPlot(JFreeChart model, ChartContext context, ChartData<YIntervalSeriesCollection> chartData) {
		super.modifyPlot(model, context, chartData);
		this.setBackgroundPaint(model);

		// get a reference to the plot for further customization...
		XYPlot plot = model.getXYPlot();
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		model.setAntiAlias(true);

		plot.setDomainCrosshairLockedOnData(true);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairLockedOnData(true);
		plot.setRangeCrosshairVisible(true);

		XYErrorRenderer xyItemRenderer = (XYErrorRenderer) plot.getRenderer();
		xyItemRenderer.setDefaultLinesVisible(true);
		xyItemRenderer.setDefaultShapesVisible(false);

		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	}

	private void setBackgroundPaint(JFreeChart aChart) {
		aChart.setBackgroundPaint(Color.white);
		aChart.getXYPlot().setBackgroundPaint(Color.white);
	}

}