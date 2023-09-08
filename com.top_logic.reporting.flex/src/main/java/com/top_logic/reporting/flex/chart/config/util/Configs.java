/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.datasource.DataContext;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveChartBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * Util class
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class Configs {

	/**
	 * Expected root tag of chart builder configuration files.
	 *
	 * @see #readBuilderConfig(String)
	 */
	public static final String BUILDER_CONFIG_ROOT_TAG = "chart-builder";

	/**
	 * Expected root tag of chart configuration files.
	 *
	 * @see #readChartConfig(String)
	 */
	public static final String CHART_CONFIG_ROOT_TAG = "chart";

	private static final String FIX_CHARTS_DIR = "/WEB-INF/charts";

	private static final String INTERACTIVE_CHARTS_DIR = "/WEB-INF/charts";

	/**
	 * Simple value-holder based on a {@link ConfigurationItem} to describe a number-interval
	 */
	public interface NumberInterval extends ConfigurationItem {

		/**
		 * The lower bound (inclusive) of this interval.
		 */
		public Double getFrom();

		/**
		 * @see #getFrom()
		 */
		public void setFrom(Double from);

		/**
		 * The upper bound of this interval.
		 * 
		 * <p>
		 * This bound is inclusive, if {@link #isClosed()} is <code>true</code>, exclusive otherwise.
		 * </p>
		 */
		public Double getTo();

		/**
		 * @see #getTo()
		 */
		public void setTo(Double to);

		/**
		 * Whether the right-hand-side of the interval is also closed.
		 */
		boolean isClosed();

		/**
		 * @see #isClosed()
		 */
		void setClosed(boolean value);

	}

	/**
	 * Simple value-holder based on a {@link ConfigurationItem} to describe a date-interval
	 */
	public interface DateInterval extends ConfigurationItem {

		/**
		 * the lower bound of this interval
		 */
		public Date getFrom();

		/**
		 * see {@link #getFrom()}
		 */
		public void setFrom(Date from);

		/**
		 * the upper bound of this interval
		 */
		public Date getTo();

		/**
		 * see {@link #getTo()}
		 */
		public void setTo(Date to);

	}

	/**
	 * Simple value-holder based on a {@link ConfigurationItem} to describe a number-interval
	 */
	public interface YHighLowInterval extends ConfigurationItem {

		/**
		 * the x-value
		 */
		public double getX();

		/**
		 * see {@link #getX()}
		 */
		public void setX(double x);

		/**
		 * the y-value
		 */
		public double getY();

		/**
		 * see {@link #getY()}
		 */
		public void setY(double y);

		/**
		 * the upper bound of the y-value
		 */
		public double getYHigh();

		/**
		 * see {@link #getYHigh()}
		 */
		public void setYHigh(double yHigh);

		/**
		 * the lower bound of the y-value
		 */
		public double getYLow();

		/**
		 * see {@link #getYLow()}
		 */
		public void setYLow(double yLow);

	}

	/**
	 * a new initialized {@link NumberInterval}
	 */
	public static NumberInterval numberInterval(Double from, Double to, boolean closed) {
		NumberInterval item = TypedConfiguration.newConfigItem(NumberInterval.class);
		item.setFrom(from);
		item.setTo(to);
		item.setClosed(closed);
		return item;
	}

	/**
	 * a new initialized {@link DateInterval}
	 */
	public static DateInterval dateInterval(Date from, Date to) {
		DateInterval item = TypedConfiguration.newConfigItem(DateInterval.class);
		item.setFrom(from);
		item.setTo(to);
		return item;
	}

	/**
	 * a new initialized {@link YHighLowInterval}
	 */
	public static YHighLowInterval yHighLowInterval(double x, double y, double yLow, double yHigh) {
		YHighLowInterval item = TypedConfiguration.newConfigItem(YHighLowInterval.class);
		item.setX(x);
		item.setY(y);
		item.setYLow(yLow);
		item.setYHigh(yHigh);
		return item;
	}

	/**
	 * Wrapps the given {@link ConfigurationItem} to make it {@link Comparable}
	 */
	public static <C extends ConfigurationItem> ComparableConfig<C> comparable(C item) {
		return new ComparableConfig<>(item);
	}

	/**
	 * Wrapper-class to make a {@link ConfigurationItem} {@link Comparable}
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class ComparableConfig<C extends ConfigurationItem> implements Comparable<ComparableConfig<C>> {

		private final C _config;

		/**
		 * Creates a new {@link ComparableConfig}
		 */
		public ComparableConfig(C config) {
			_config = config;
		}

		/**
		 * the wrapped {@link ConfigurationItem}
		 */
		public C getConfig() {
			return _config;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof ComparableConfig)) {
				return false;
			}
			return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(_config, ((ComparableConfig<?>) obj).getConfig());
		}

		@Override
		public int hashCode() {
			return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(_config);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public int compareTo(ComparableConfig<C> o) {
			int res = 0;
			for (PropertyDescriptor prop : _config.descriptor().getPropertiesOrdered()) {
				if (o.getConfig().descriptor().hasProperty(prop.getPropertyName())) {
					Object v1 = _config.value(prop);
					Object v2 = o.getConfig().value(prop);
					if (v1 instanceof Comparable && v2 instanceof Comparable) {
						res = ((Comparable) v1).compareTo(v2);
					}
					if (res != 0) {
						break;
					}
				}
			}
			return res;
		}

	}

	/**
	 * Reads the {@link ChartConfig} from the given file
	 */
	public static ChartConfig readChartConfig(String configFile) {
		try {
			BinaryData file = getFile(FIX_CHARTS_DIR, configFile);
			if (file == null) {
				return null;
			}
			return readConfig(file, ChartConfig.class, CHART_CONFIG_ROOT_TAG);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Reads the
	 * {@link com.top_logic.reporting.flex.chart.config.gui.InteractiveChartBuilder.Config} from
	 * the given file
	 */
	public static InteractiveChartBuilder.Config readBuilderConfig(String configFile) {
		try {
			BinaryData file = getFile(INTERACTIVE_CHARTS_DIR, configFile);
			if (file == null) {
				return null;
			}
			return readConfig(file, InteractiveChartBuilder.Config.class, BUILDER_CONFIG_ROOT_TAG);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static BinaryData getFile(String baseDir, String filename) {
		assert !StringServices.isEmpty(filename) : "File name must not be null or empty.";
		{
			String path = baseDir + '/' + filename;
			BinaryData file = FileManager.getInstance().getDataOrNull(path);
			if (file == null) {
				Logger.error("Invalid chart configuration file: " + path, Configs.class);
				return null;
			}
			return file;
		}
	}

	private static <C extends ConfigurationItem> C readConfig(BinaryData file, Class<C> clazz, String rootTag)
			throws ConfigurationException {
		ConfigurationDescriptor desc = TypedConfiguration.getConfigurationDescriptor(clazz);
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(rootTag, desc);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);

		@SuppressWarnings("unchecked")
		C result = (C) reader.setSource(file).read();
		return result;
	}

	/**
	 * Convenience-method to create a JFreeChart for a given ChartConfig without component-context.
	 * 
	 * @param config
	 *        - the {@link ChartConfig} describing the Chart.
	 * @return the {@link JFreeChart} as described in given {@link ChartConfig}
	 */
	@SuppressWarnings("unchecked")
	public static JFreeChart createChart(ChartConfig config) {
		Collection<?> rawData = config.getDataSource().getRawData(DataContext.NO_CONTEXT);
		ChartTree tree = config.getModelPreparation().prepare(rawData);
		JFreeChartBuilder<Dataset> builder = config.getChartBuilder();
		Dataset dataset = builder.getDatasetBuilder().getDataset(tree);
		return builder.createChart(ChartContext.NO_CONTEXT, new ChartData<>(tree, dataset));
	}

}
