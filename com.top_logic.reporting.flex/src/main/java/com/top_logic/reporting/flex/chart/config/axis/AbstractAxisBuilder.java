/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.axis;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.util.ResKey;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.util.Resources;

/**
 * {@link AbstractAxisBuilder}
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public abstract class AbstractAxisBuilder implements ConfiguredInstance<AbstractAxisBuilder.Config> {

	/**
	 * Enum to name axes in jfree-chart.
	 */
	public enum ChartAxis {
		/**
		 * <code>DOMAIN</code> - the X-axis
		 */
		DOMAIN, 
		/**
		 * <code>RANGE</code> - the Y-axis
		 */
		RANGE;
		
		/**
		 * Getter for the axis according to this.
		 */
		public Axis getAxis(Plot plot) {
			if (plot instanceof CategoryPlot) {
				return (this == DOMAIN) ? ((CategoryPlot) plot).getDomainAxis() : ((CategoryPlot) plot).getRangeAxis();
			}
			else if (plot instanceof XYPlot) {
				return (this == DOMAIN) ? ((XYPlot) plot).getDomainAxis() : ((XYPlot) plot).getRangeAxis();
			}
			throw new IllegalArgumentException("Plot not supported: " + plot);
		}
		
		/**
		 * See {@link #getAxis(Plot)}
		 */
		public void setAxis(Plot plot, Axis axis) {
			if (plot instanceof CategoryPlot) {
				CategoryPlot categoryPlot = (CategoryPlot) plot;
				if (this == DOMAIN) {
					CategoryAxis categoryAxis = (CategoryAxis) axis;
					categoryPlot.setDomainAxis(categoryAxis);
				}
				else {
					ValueAxis valueAxis = (ValueAxis) axis;
					categoryPlot.setRangeAxis(valueAxis);
				}
			}
			else if (plot instanceof XYPlot) {
				XYPlot xyPlot = (XYPlot) plot;
				ValueAxis valueAxis = (ValueAxis) axis;
				if (this == DOMAIN) {
					xyPlot.setDomainAxis(valueAxis);
				}
				else {
					xyPlot.setRangeAxis(valueAxis);
				}
			}
			else {
				throw new IllegalArgumentException("Plot not supported: " + plot);
			}
		}

		/**
		 * Indirection to get configured label for this axis
		 * 
		 * @param builder
		 *        the builder-config where the axis-label is configured
		 * @return the translated value for this axis
		 */
		public String getLabel(JFreeChartBuilder.Config builder) {
			if (builder == null) {
				return "";
			}
			ResKey key;
			switch (this) {
				case DOMAIN:
					key = builder.getXAxisLabelKey();
					break;
				case RANGE:
					key = builder.getYAxisLabelKey();
					break;
				default:
					return "";
			}
			return Resources.getInstance().getString(key);
		}
	}

	/**
	 * Config-interface for {@link AbstractAxisBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<AbstractAxisBuilder>, ConfigPart {

		/**
		 * the container-config with simple axis-information like label
		 */
		@Container
		public JFreeChartBuilder.Config getBuilder();

	}

	private final Config _config;

	/**
	 * Config-constructor for {@link AbstractAxisBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor.
	 * @param config
	 *        - default config-constructor.
	 */
	public AbstractAxisBuilder(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * @param plot
	 *        the plot where to modify the axes
	 * @param chartAxis
	 *        the enum describing the axis this builder works with
	 * @param chartData
	 *        the chart-data for context-information
	 * @param parent
	 *        workaround for #17180 to get the container-config
	 */
	public void applyTo(Plot plot, ChartAxis chartAxis, ChartData<? extends Dataset> chartData,
			JFreeChartBuilder.Config parent) {
		Axis axis = createAxis(chartAxis.getAxis(plot), chartAxis, chartData, parent);
		chartAxis.setAxis(plot, axis);
	}

	/**
	 * @param axis
	 *        the original axis
	 * @param chartAxis
	 *        the enum describing the axis this builder works with
	 * @param chartData
	 *        the chart-data for context-information
	 * @param parent
	 *        workaround for #17180 to get the container-config
	 */
	protected abstract Axis createAxis(Axis axis, ChartAxis chartAxis, ChartData<? extends Dataset> chartData,
			JFreeChartBuilder.Config parent);

}
