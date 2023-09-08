/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.DateCriterion;

/**
 * Simple {@link PartitionFunction} to provide a List of {@link Date}s for futher partitioning.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DatePartitionFunction implements PartitionFunction,
		ConfiguredInstance<PartitionFunction.Config> {

	/**
	 * Config-interface for {@link DatePartitionFunction}.
	 */
	public interface Config extends PartitionFunction.Config {

		/**
		 * the list of dates used as partition-keys
		 */
		@ListBinding(format = DateValueProvider.class, attribute = "value", tag = "date")
		List<Date> getDates();

	}

	private final Config _config;

	/**
	 * Config-constructor for {@link DatePartitionFunction}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public DatePartitionFunction(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {

		List<Partition> result = new ArrayList<>();
		for (Date date : getConfig().getDates()) {
			result.add(new Partition(aParent, date, aParent.getObjects()));
		}

		return result;
	}

	@Override
	public Criterion getCriterion() {
		return DateCriterion.DATE_INSTANCE;
	}

	/**
	 * {@link ConfigurationValueProvider} for {@link Date}s. Absolute and relative values are
	 * supported. Examples:
	 * <ul>
	 * <li>Use SimpleDateFormat("dd.MM.yyyy") for fix date</li>
	 * <li>Use keyword "NOW" for current date</li>
	 * <li>Use prefix d with int-value to add days to current date</li>
	 * <li>Use prefix w with int-value to add weeks to current date</li>
	 * </ul>
	 */
	public static class DateValueProvider extends AbstractConfigurationValueProvider<Date> {

		/**
		 * Singleton INSTANCE
		 */
		public static final DateValueProvider INSTANCE = new DateValueProvider();

		private static final Pattern ADD_WEEK_PATTERN = Pattern.compile("w(\\d+)");

		private static final Pattern ADD_DAYS_PATTERN = Pattern.compile("d(-?\\d+)");

		/**
		 * Default-Constructor for {@link DateValueProvider}. Use Singleton {@link #INSTANCE}
		 * instead.
		 */
		public DateValueProvider() {
			super(Date.class);
		}

		@Override
		protected Date getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {

			if ("NOW".equals(propertyValue)) {
				return new Date();
			}
			Matcher matcher = ADD_WEEK_PATTERN.matcher(propertyValue);
			if (matcher.matches()) {
				Date date = new Date();
				int diff = Integer.valueOf(matcher.group(1));
				date = DateUtil.addDays(date, diff + 7);
				date = DateUtil.adjustToDayEnd(date);
				return date;
			}
			matcher = ADD_DAYS_PATTERN.matcher(propertyValue);
			if (matcher.matches()) {
				Date date = new Date();
				int diff = Integer.valueOf(matcher.group(1));
				date = DateUtil.addDays(date, diff);
				date = DateUtil.adjustToDayEnd(date);
				return date;
			}
			else {
				try {
					Date date = getDateFormat().parse(propertyValue.toString());
					date = DateUtil.adjustToDayEnd(date);
					return date;
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		@Override
		protected String getSpecificationNonNull(Date configValue) {
			return getDateFormat().format(configValue);
		}

		private static SimpleDateFormat getDateFormat() {
			return CalendarUtil.newSimpleDateFormat("dd.MM.yyyy");
		}

	}

}