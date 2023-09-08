/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.knowledge.search.CountFunction;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.AbstractDataKey;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.DateCriterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.FunctionCriterion;
import com.top_logic.reporting.flex.chart.config.util.ToStringText;
import com.top_logic.reporting.flex.chart.config.util.ToStringText.NotSetText;

/**
 * Dataset-builder for {@link TimeSeriesCollection}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class TimeseriesDatasetBuilder extends AbstractDatasetBuilder<TimeSeriesCollection> {

	/**
	 * Enum mapping the supported {@link Period}s to the {@link TimePeriod}-classes used in
	 * {@link JFreeChart}.
	 */
	public enum Period {

		/**
		 * <code>YEAR</code> corresponding to {@link Year}
		 */
		YEAR(Year.class),

		/**
		 * <code>MONTH</code> corresponding to {@link Month}
		 */
		MONTH(Month.class),

		/**
		 * <code>WEEK</code> corresponding to {@link Week}
		 */
		WEEK(Week.class),

		/**
		 * <code>DAY</code> corresponding to {@link Day}
		 */
		DAY(Day.class),

		/**
		 * <code>HOUR</code> corresponding to {@link Hour}
		 */
		HOUR(Hour.class),

		/**
		 * <code>MINUTE</code> corresponding to {@link Minute}
		 */
		MINUTE(Minute.class),

		/**
		 * <code>SECOND</code> corresponding to {@link Second}
		 */
		SECOND(Second.class);

		private final Class<? extends TimePeriod> _timePeriod;

		private Period(Class<? extends org.jfree.data.time.TimePeriod> tp) {
			_timePeriod = tp;
		}
		
		/**
		 * the corresponding JFreeChart Timeperiod for this
		 */
		public Class<? extends TimePeriod> getTimePeriod() {
			return _timePeriod;
		}
	}

	/**
	 * Config-interface for {@link TimeseriesDatasetBuilder}.
	 */
	public interface Config extends AbstractDatasetBuilder.Config {

		/**
		 * the enumeration describing the time-period for the date-axis
		 */
		@FormattedDefault("DAY")
		public Period getPeriod();
	}

	/**
	 * Config-Constructor for {@link TimeseriesDatasetBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public TimeseriesDatasetBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected TimeSeriesCollection internalCreateDataset(ChartTree tree) {
		return generateTimeSeriesCollection(tree);
	}

	private TimeSeriesCollection generateTimeSeriesCollection(ChartTree tree) {

		TimeSeriesCollection dataset = new TimeSeriesCollection(TimeZones.systemTimeZone());
		ChartNode root = tree.getRoot();
		
		if (isEmpty(tree)) {
			// return empty set to prevent follow-up NPE. No relevant data existing anyway.
			return dataset;
		}
		int depth = tree.getDepth();
		int dateIndex = tree.getCriterionIndex(DateCriterion.class);
		DateCriterion dateCriterion = (DateCriterion) tree.getCriterion(dateIndex + 1);
		int classIndex = depth >= 2 ? (dateIndex == 0 ? 1 : 0) : -1;
		Comparable<?>[] keys;
		if (classIndex == -1) {
			classIndex = depth;
			keys = new Comparable[depth + 1];
			Criterion criterion = tree.getCriterion(depth + 1);
			Comparable<?> classificationKey;
			if (criterion instanceof FunctionCriterion) {
				classificationKey = criterion.getLabel();
			}
			else {
				classificationKey = new ToStringText(CountFunction.class.getSimpleName());
			}
			keys[classIndex] = classificationKey;

			TimeSeries series = new TimeSeries(getUniqueName(classificationKey));
			dataset.addSeries(series);
		} else {
			keys = new Comparable[depth];

			// Create series in sorted order to get a reasonable order in the legend.
			List<List<Comparable<?>>> sortedKeysByLevel = CategoryDatasetBuilder.collectSortedKeysByLevel(tree, this);
			for (Comparable<?> key : sortedKeysByLevel.get(classIndex)) {
				TimeSeries series = new TimeSeries(getUniqueName(key));
				dataset.addSeries(series);
			}
		}

		handle(dataset, keys, tree, root, 0, depth, dateIndex, classIndex, dateCriterion);

		dataset.setXPosition(TimePeriodAnchor.START);

		return dataset;
	}

	/**
	 * Checks whether the given tree should be treated as empty.
	 */
	protected boolean isEmpty(ChartTree tree) {
		return tree.getDepth() < 1;
	}

	/**
	 * {@link DataKey}-implementation used for {@link TimeSeriesCollection}
	 */
	public static class TimeseriesDataKey extends AbstractDataKey<UniqueName, RegularTimePeriod> {

		/**
		 * Creates a new {@link TimeseriesDataKey}
		 * 
		 * @param seriesKey
		 *        the key indicating the series
		 * @param period
		 *        the period indicating the timestamp / timeperiod
		 */
		public TimeseriesDataKey(UniqueName seriesKey, RegularTimePeriod period) {
			super(seriesKey, period);
		}

	}

	/**
	 * a new {@link TimeseriesDataKey} for the given series-key and timeperiod.
	 */
	public static TimeseriesDataKey createDataKey(UniqueName seriesKey, RegularTimePeriod period) {
		return new TimeseriesDataKey(seriesKey, period);
	}

	/**
	 * a new {@link TimeseriesDataKey} identifying the data represented in the given dataset
	 *         by the given series and item.
	 */
	public static TimeseriesDataKey toDataKey(TimeSeriesCollection dataset, int series, int item) {
		TimeSeries ts = dataset.getSeries(series);
		UniqueName seriesKey = (UniqueName) ts.getKey();
		TimeSeriesDataItem dataItem = ts.getDataItem(item);
		RegularTimePeriod period = dataItem.getPeriod();
		return new TimeseriesDataKey(seriesKey, period);
	}

	private void handle(TimeSeriesCollection dataset, Comparable<?>[] keys, ChartTree tree, ChartNode parent, int i,
			int depth, int dateIndex, int classIndex, DateCriterion dateCriterion) {
		if (i >= depth) {
			double val = parent.getValue().doubleValue();
			UniqueName key = getUniqueName(keys[classIndex]);

			TimeSeries series = dataset.getSeries(key);
			if (dateIndex >= 0 && dateIndex < keys.length) {
				Comparable<?> dateKey = keys[dateIndex];
				if (!(dateKey instanceof NotSetText)) {
					RegularTimePeriod period = getPeriod(dateKey, dateCriterion);
					series.addOrUpdate(period, val);
					String id = parent.getID();
					DataKey dataKey = createDataKey(key, period);
					tree.registerDataKey(dataKey, id);
				} else {
					// Ignore "empty values". Too complicated to enforce consistent UI settings.
				}
			}
		}
		else {
			for (ChartNode node : parent.getChildren()) {
				keys[i] = node.getKey();
				handle(dataset, keys, tree, node, i + 1, depth, dateIndex, classIndex, dateCriterion);
			}
		}
	}

	private RegularTimePeriod getPeriod(Comparable<?> comparable, DateCriterion dateCriterion) {
		Class<? extends TimePeriod> timePeriod = getConfig().getPeriod().getTimePeriod();
		return dateCriterion.getPeriod(comparable, timePeriod);
	}

	@Override
	public Class<TimeSeriesCollection> getDatasetType() {
		return TimeSeriesCollection.class;
	}

}
