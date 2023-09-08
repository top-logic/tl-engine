/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder;

import java.awt.Font;
import java.awt.Paint;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.RegularTimePeriod;

import com.top_logic.base.time.TimePeriod;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;
import com.top_logic.layout.provider.BooleanLabelProvider;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.resources.TLPartScopedResourceProvider;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.color.ColorContext;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.util.Configs.ComparableConfig;
import com.top_logic.reporting.flex.chart.config.util.GradientPaintAdapter;
import com.top_logic.reporting.flex.chart.config.util.ToStringText;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * Base implementation for {@link JFreeChartBuilder}.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public abstract class AbstractJFreeChartBuilder<D extends Dataset> implements JFreeChartBuilder<D> {

	/** Number of points to decrement chart title. */
	private static final float CHART_TITLE_DECREMENT = 5.0F;

	private final Config<D> _config;

	private ColorContext _colorContext;

	/**
	 * Config-constructor for {@link AbstractJFreeChartBuilder}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public AbstractJFreeChartBuilder(InstantiationContext context, Config<D> config) {
		_config = config;
	}

	/**
	 * Creates a new {@link ColorContext}
	 */
	public void resetColorContext() {
		_colorContext = _config.getColorProvider().createColorContext();
	}

	@Override
	public Config<D> getConfig() {
		return _config;
	}

	@Override
	public DatasetBuilder<? extends D> getDatasetBuilder() {
		return _config.getDatasetBuilder();
	}

	/**
	 * Returns the title for the chart - end-user-readable.
	 * 
	 * @return translated title of the chart
	 */
	protected String getTitle() {
		return getTranslationNotEmpty(_config.getTitleKey());
	}

	/**
	 * Returns the x-axis-label for the chart - end-user-readable.
	 * 
	 * @return translated x-axis-label of the chart
	 */
	protected String getXAxisLabel() {
		return getTranslationNotEmpty(_config.getXAxisLabelKey());
	}

	/**
	 * Returns the y-axis-label for the chart - end-user-readable.
	 * 
	 * @return translated y-axis-label of the chart
	 */
	protected String getYAxisLabel() {
		return getTranslationNotEmpty(_config.getYAxisLabelKey());
	}

	/**
	 * Convenience-method to get a translation for a key.
	 * 
	 * @param key
	 *        the key to translate - if key starts with <code>"/s"</code> the key is not translated.
	 * @return the translated key or null if key is empty.
	 */
	protected String getTranslationNotEmpty(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	/**
	 * the {@link PlotOrientation} of the Chart, either horizontal or vertical.
	 */
	protected PlotOrientation getOrientation() {
		Orientation orientation = getConfig().getOrientation();
		return orientation.getOrientation();
	}

	/**
	 * This method is called after creating the chart to modify the plot before rendering.
	 * 
	 * @param model
	 *        the created chart
	 * @param context
	 *        the context this chart is created in
	 * @param chartData
	 *        the chart-data containing {@link Dataset} and {@link ChartTree}
	 */
	protected void modifyPlot(JFreeChart model, ChartContext context, ChartData<D> chartData) {
		adaptChart(model, context, chartData);
		if (getConfig().getShowUrls()) {
			setUrlGenerator(model, context, chartData);
		}
		if (getConfig().getShowTooltips()) {
			setTooltipGenerator(model, context, chartData);
		}
		adaptTitle(model);
	}

	/**
	 * This method is called first after creating the chart to initialize things like renderer
	 * before modifying the plot.
	 * 
	 * @param model
	 *        the created chart
	 * @param context
	 *        the context this chart is created in
	 * @param chartData
	 *        the chart-data containing {@link Dataset} and {@link ChartTree}
	 */
	protected void adaptChart(JFreeChart model, ChartContext context, ChartData<D> chartData) {
		// hook for subclasses
	}

	/**
	 * Adapt the title font when a title is set (will be called on end of
	 * {@link #modifyPlot(JFreeChart, ChartContext, ChartData)}).
	 * 
	 * @param model
	 *        the created chart
	 */
	protected void adaptTitle(JFreeChart model) {
		TextTitle title = model.getTitle();

		if (title != null && !StringServices.isEmpty(title.getText())) {
			Font titleFont = title.getFont();

			title.setFont(titleFont.deriveFont(titleFont.getSize() - CHART_TITLE_DECREMENT));
		}
	}

	/**
	 * Util-method that compares {@link Comparable}s used as {@link Dataset} keys. This method
	 * unwraps the keys if they are {@link UniqueName}s before comparing.
	 * 
	 * @return true if the given objects are functionally equal
	 */
	public static boolean equalKey(Comparable<?> c1, Comparable<?> c2) {
		Comparable<?> key1 = c1.toString();
		if (c1 instanceof UniqueName) {
			key1 = ((UniqueName) c1).getKey();
		}
		Comparable<?> key2 = c2.toString();
		if (c2 instanceof UniqueName) {
			key2 = ((UniqueName) c2).getKey();
		}
		return Utils.equals(key1, key2);
	}

	/**
	 * Hook for subclasses to initialize the URL-Generator of this chart if it is configured to use
	 * links.
	 * 
	 * @param model
	 *        the created chart
	 * @param context
	 *        the context this chart is created in
	 * @param chartData
	 *        the chart-data containing {@link Dataset} and {@link ChartTree}
	 */
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<D> chartData) {
		throw new RuntimeException("setUrlGenerator not yet implemented! " + this);
	}

	/**
	 * Hook for subclasses to initialize the Tooltip-Generator of this chart if it is configured to
	 * show tooltips.
	 * 
	 * @param model
	 *        the created chart
	 * @param context
	 *        the context this chart is created in
	 * @param chartData
	 *        the chart-data containing {@link Dataset} and {@link ChartTree}
	 */
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<D> chartData) {
		throw new RuntimeException("setTooltipGenerator not yet implemented! " + this);
	}

	@Override
	public JFreeChart createChart(ChartContext context, ChartData<D> chartData) {
		resetColorContext();
		JFreeChart chart = internalCreateChart(context, chartData);
		if (chart != null) {
			modifyPlot(chart, context, chartData);
			chart.setAntiAlias(true);
		}
		return chart;
	}

	/**
	 * Creates a {@link JFreeChart} for the given {@link ChartContext} and {@link ChartData}. To
	 * modify plot or renderer overwrite {@link #modifyPlot(JFreeChart, ChartContext, ChartData)} or
	 * {@link #adaptChart(JFreeChart, ChartContext, ChartData)}.
	 */
	protected abstract JFreeChart internalCreateChart(ChartContext context, ChartData<D> chartData);

	/**
	 * {@link LabelProvider} for keys in a {@link Dataset} if no other {@link LabelProvider} is
	 * configured.
	 * 
	 * <p>
	 * This {@link LabelProvider} provides lables for all common types of keys like
	 * {@link AggregationFunction}s, {@link TimePeriod}s and some more.
	 * </p>
	 */
	public static class StandardLabelProvider implements LabelProvider {

		private final LabelProvider _tlPartProvider = TLPartScopedResourceProvider.INSTANCE;
		private final LabelProvider _configLabels = new ConfigLabelProvider();

		@SuppressWarnings("rawtypes")
		@Override
		public String getLabel(Object object) {
			if (object == null) {
				return "-";
			}
			if (object instanceof AggregationFunction) {
				return ((AggregationFunction) object).getLabel();
			}
			else if (object instanceof ToStringText) {
				return String.valueOf(object);
			}
			else if (object instanceof Boolean) {
				return BooleanLabelProvider.INSTANCE.getLabel(object);
			}
			else if (object instanceof RegularTimePeriod) {
				return String.valueOf(object);
			}
			else if (object instanceof Pair) {
				Pair pair = (Pair) object;
				return getLabel(pair.getFirst()) + " - " + getLabel(pair.getSecond());
			}
			else if (object instanceof ComparableConfig) {
				return getLabel(((ComparableConfig) object).getConfig());
			}
			else if (object instanceof ConfigurationItem) {
				return _configLabels.getLabel(object);
			}
			else if (object instanceof TLClass) {
				return _tlPartProvider.getLabel(object);
			}
			ResourceProvider provider = LabelProviderService.getInstance().getResourceProvider(object);
			return provider.getLabel(object);
		}

	}

	/**
	 * @param level
	 *        the level in the {@link ChartTree} to get the {@link LabelProvider} for.
	 * @return the LabelProvider to use to translate the keys of the {@link ChartNode}s in the given
	 *         level.
	 */
	protected LabelProvider getLabelProvider(int level) {
		List<LabelProvider> providers = _config.getLabelProviders();
		if (CollectionUtil.isEmptyOrNull(providers)) {
			return new StandardLabelProvider();
		}
		else if (providers.size() <= level) {
			return CollectionUtil.getLast(providers);
		}
		return providers.get(level);
	}

	/**
	 * Initializes the label-providers to a list of keys if they are instanceof {@link UniqueName}
	 * 
	 * @param labelProvider
	 *        the label-provider to set
	 * @param keys
	 *        the keys where the label-provider should be initialized
	 */
	protected void setLabelProvider(LabelProvider labelProvider, List<?> keys) {
		for (Object key : keys) {
			if (key instanceof UniqueName) {
				((UniqueName) key).setProvider(labelProvider);
			}
		}
	}

	/**
	 * @param obj
	 *        the input to get a color for
	 * @return a color for the given input. Must be determined within the current
	 *         {@link ColorContext} for the same input.
	 */
	public Paint getColor(Object obj) {
		if (_colorContext == null) {
			resetColorContext();
		}
		Paint res = _colorContext.getColor(obj);
		if (getConfig().getUseGradientPaint()) {
			res = GradientPaintAdapter.toGradientColor(70, res);
		}
		return res;
	}

	/**
	 * a label for this builder describing what kind of charts are created, e.g. Bar, Line,
	 *         Pie, ...
	 */
	public String getLabel() {
		return Resources.getInstance().getString(I18NConstants.JFREE_CHART.key(getClass().getSimpleName()));
	}

	/**
	 * the max. number of dimensions supported by the generated type of chart
	 */
	public abstract int getMaxDimensions();

	/**
	 * the min. number of dimensions supported by the generated type of chart
	 */
	public abstract int getMinDimensions();

}
