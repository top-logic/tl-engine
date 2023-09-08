/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder.Period;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.TimePeriodCriterion;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;

/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DateAttributePartition extends SingleValuePartition {

	/**
	 * Config-interface for {@link DateAttributePartition}.
	 */
	public interface Config extends SingleValuePartition.Config {

		/**
		 * @see #getPeriod()
		 */
		String PERIOD = "period";

		/**
		 * @see #getSkipEmptyPeriods()
		 */
		String SKIP_EMPTY_PERIODS = "skip-empty-periods";

		@Override
		@ClassDefault(DateAttributePartition.class)
		public Class<? extends DateAttributePartition> getImplementationClass();

		/**
		 * see
		 * {@link com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder.Period}
		 * 
		 * @return the period defining how to accumulate date-values
		 */
		@Name(PERIOD)
		@FormattedDefault("DAY")
		public Period getPeriod();

		/**
		 * see {@link #getPeriod()}
		 */
		public void setPeriod(Period period);

		/**
		 * Whether periods without data should be ignored (instead of being drawn with a null
		 * value).
		 */
		@Name(SKIP_EMPTY_PERIODS)
		boolean getSkipEmptyPeriods();

		/**
		 * @see #getSkipEmptyPeriods()
		 */
		public void setSkipEmptyPeriods(boolean b);

	}

	/**
	 * Config-constructor for {@link DateAttributePartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public DateAttributePartition(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) super.getConfig();
	}

	@Override
	protected void initOptions(Partition aParent, Map<Object, Partition> map) {
		if (config().getSkipEmptyPeriods()) {
			return;
		}

		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		for (Object obj : aParent.getObjects()) {
			Date value = (Date) getValue((TLObject) obj);
			if (value != null) {
				min = Math.min(min, value.getTime());
				max = Math.max(max, value.getTime());
			}
		}

		if (min <= max) {
			RegularTimePeriod period = createRegularTimePeriod(new Date(min));

			// Safety check: Prevent out of memory error in case of large time ranges with small
			// periods.
			long periodDuration = period.getEnd().getTime() - period.getStart().getTime();
			long periodCount = (max - min) / periodDuration;
			if (periodCount > 1000) {
				return;
			}

			while (period.getEnd().getTime() < max) {
				map.put(period, new Partition(aParent, period, new ArrayList<>()));

				period = period.next();
			}
		}
	}

	@Override
	protected Comparable<?> getKey(Object val) {
		return createRegularTimePeriod((Date) val);
	}

	private RegularTimePeriod createRegularTimePeriod(Date date) {
		Class<? extends TimePeriod> period = config().getPeriod().getTimePeriod();
		TLSubSessionContext subSession = DefaultDisplayContext.getDisplayContext().getSubSessionContext();
		TimeZone timeZone = subSession.getCurrentTimeZone();
		Locale locale = subSession.getCurrentLocale();
		return RegularTimePeriod.createInstance(period, date, timeZone, locale);
	}

	@Override
	protected Criterion initCriterion() {
		return TimePeriodCriterion.newInstance(getConfig().getMetaAttribute().get(), false);
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(TLStructuredTypePart ma, Period period) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setMetaAttribute(new MetaAttributeProvider(ma));
		item.setPeriod(period);
		return item;
	}

}