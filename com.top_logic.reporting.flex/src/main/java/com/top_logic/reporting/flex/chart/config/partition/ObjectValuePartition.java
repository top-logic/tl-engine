/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.jfree.data.time.RegularTimePeriod;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder.Period;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.TimePeriodCriterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;

/**
 * Default implementation for {@link PartitionFunction}s based on an accessor to retrieve values.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ObjectValuePartition implements PartitionFunction {

	/**
	 * Interface for aggregation-classes that can group objects by creating a group-key for an
	 * input-object.
	 * 
	 * @see #getKey(Object)
	 */
	public interface Aggregator {
		
		/** 
		 * @param source the input-object to create a group-key for
		 * @return the key to identify the group the input object should be part of
		 */
		public Comparable<?> getKey(Object source);

		/**
		 * Dispatched from {@link ObjectValuePartition#getCriterion()}
		 * See {@link PartitionFunction#getCriterion()}
		 */
		public Criterion getCriterion(ObjectValuePartition function);

	}

	/**
	 * Aggregator that uses the input-object as key. The input object is expected to be instanceof
	 * {@link Comparable}.
	 */
	public static class InstanceAggregator implements Aggregator {

		@Override
		public Comparable<?> getKey(Object source) {
			return (Comparable<?>) source;
		}

		@Override
		public Criterion getCriterion(ObjectValuePartition function) {
			return ValueCriterion.INSTANCE;
		}

	}

	/**
	 * Aggregator that uses the configured time-periods as key. The input object is expected to be
	 * instanceof {@link Date}.
	 */
	public static class TimePeriodAggregator implements Aggregator, ConfiguredInstance<TimePeriodAggregator.Config> {

		private final Config _config;

		/**
		 * Base-config-interface for {@link TimePeriodAggregator}.
		 */
		public interface Config extends PolymorphicConfiguration<TimePeriodAggregator> {

			/**
			 * see
			 * {@link com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder.Period}
			 * 
			 * @return the period defining how to accumulate date-values
			 */
			@FormattedDefault("DAY")
			public Period getPeriod();

			/**
			 * See {@link #getPeriod()}
			 */
			public void setPeriod(Period period);

		}

		/**
		 * Config-constructor for {@link TimePeriodAggregator}
		 * 
		 * @param context
		 *        - default config-constructor
		 * @param config
		 *        - default config-constructor
		 */
		public TimePeriodAggregator(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Config getConfig() {
			return _config;
		}

		@Override
		public Comparable<?> getKey(Object source) {
			Date date = (Date) source;
			TLSubSessionContext subSession = DefaultDisplayContext.getDisplayContext().getSubSessionContext();
			TimeZone timeZone = subSession.getCurrentTimeZone();
			Locale locale = subSession.getCurrentLocale();
			return RegularTimePeriod.createInstance(getConfig().getPeriod().getTimePeriod(), date, timeZone, locale);
		}

		@Override
		public Criterion getCriterion(ObjectValuePartition function) {
			return TimePeriodCriterion.TIME_PERIOD_INSTANCE;
		}

		/**
		 * Factory method to create an initialized {@link Config}.
		 * 
		 * @return a new ConfigItem.
		 */
		public static Config item(Period period) {
			Config item = TypedConfiguration.newConfigItem(Config.class);
			item.setPeriod(period);
			return item;
		}

		/**
		 * Factory method to create an initialized {@link TimePeriodAggregator}.
		 * 
		 * @return a new TimePeriodAggregator.
		 */
		public static TimePeriodAggregator instance(Period period) {
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(item(period));
		}

	}

	/**
	 * Base-config-interface for {@link ObjectValuePartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		@Override
		@ClassDefault(ObjectValuePartition.class)
		public Class<? extends ObjectValuePartition> getImplementationClass();

		/**
		 * the accessor used to retrieve values from the input-object
		 */
		@SuppressWarnings("rawtypes")
		@InstanceFormat
		@InstanceDefault(WrapperAccessor.class)
		public Accessor getAccessor();

		/**
		 * See {@link #getAccessor()}
		 */
		@SuppressWarnings("rawtypes")
		public void setAccessor(Accessor accessor);

		/**
		 * the {@link Aggregator} used to assign the object to a group
		 */
		@InstanceFormat
		@InstanceDefault(InstanceAggregator.class)
		public Aggregator getAggregator();

		/**
		 * See {@link #getAggregator()}
		 */
		public void setAggregator(Aggregator aggr);

		/**
		 * the attribute-name to get the value for
		 */
		public String getAttribute();

		/**
		 * See {@link #getAttribute()}
		 */
		public void setAttribute(String attr);

	}

	private final Config _config;

	/**
	 * Config-constructor for {@link AbstractAttributeBasedPartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ObjectValuePartition(InstantiationContext context, Config config) {
		_config = config;
	}

	private Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<Partition> result = new ArrayList<>();
		Map<Comparable<?>, Partition> map = new HashMap<>();
		for (Object obj : aParent.getObjects()) {
			Object val = getValue(obj);
			Comparable<?> key = getKey(val);
			Partition part = map.get(key);
			if (part == null) {
				part = new Partition(aParent, key, new ArrayList<>());
				map.put(key, part);
			}
			part.getObjects().add(obj);
		}
		result.addAll(map.values());
		Collections.sort(result);

		return result;
	}

	private Comparable<?> getKey(Object val) {
		return getConfig().getAggregator().getKey(val);
	}

	@SuppressWarnings("unchecked")
	private Object getValue(Object obj) {
		Config config = getConfig();
		return config.getAccessor().getValue(obj, config.getAttribute());
	}

	@Override
	public Criterion getCriterion() {
		return getConfig().getAggregator().getCriterion(this);
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(String attr, Aggregator aggr) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setAttribute(attr);
		item.setAggregator(aggr);
		return item;
	}

}