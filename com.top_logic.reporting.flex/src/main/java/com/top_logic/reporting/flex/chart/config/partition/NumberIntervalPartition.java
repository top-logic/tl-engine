/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.ComparisonDependency;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ClassificationCriterion;
import com.top_logic.reporting.flex.chart.config.util.Configs;
import com.top_logic.reporting.flex.chart.config.util.Configs.ComparableConfig;
import com.top_logic.reporting.flex.chart.config.util.Configs.NumberInterval;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;
import com.top_logic.reporting.flex.chart.config.util.ToStringText.NotSetText;

/**
 * Special kind of {@link SingleValuePartition} for number-values using configured intervals to
 * split the objects.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class NumberIntervalPartition extends SingleValuePartition {

	/**
	 * Config-interface for {@link NumberIntervalPartition}.
	 */
	@DisplayOrder({ Config.INTERVAL_START, Config.INTERVAL_END, Config.INTERVAL_SIZE })
	public interface Config extends SingleValuePartition.Config {

		@Override
		@ClassDefault(NumberIntervalPartition.class)
		public Class<? extends NumberIntervalPartition> getImplementationClass();

		/**
		 * <code>INTERVAL_SIZE</code> Attribute name for interval-size-property
		 */
		public String INTERVAL_SIZE = "interval-size";

		/**
		 * <code>INTERVAL_START</code> Attribute name for interval-start-property
		 */
		public String INTERVAL_START = "interval-start";

		/**
		 * <code>INTERVAL_END</code> Attribute name for interval-end-property
		 */
		public String INTERVAL_END = "interval-end";

		/**
		 * the size of the interval
		 */
		@Name(INTERVAL_SIZE)
		@DoubleDefault(1.0)
		@Constraint(NonNegative.class)
		public double getIntervalSize();

		/**
		 * see {@link #getIntervalSize()}
		 */
		public void setIntervalSize(double size);

		/**
		 * the start-value for the first interval to begin. If no start-value is given, the
		 *         lowest-existing value is used.
		 */
		@Name(INTERVAL_START)
		@ComparisonDependency(comparison = Comparision.SMALLER, other = @Ref(INTERVAL_END))
		public Double getIntervalStart();

		/**
		 * see {@link #getIntervalStart()}
		 */
		public void setIntervalStart(Double start);

		/**
		 * the end-value for the last interval to end. If no end-value is given, the
		 *         largest-existing value is used.
		 */
		@Name(INTERVAL_END)
		public Double getIntervalEnd();

		/**
		 * see {@link #getIntervalEnd()}
		 */
		public void setIntervalEnd(Double end);

		@Override
		@BooleanDefault(false)
		public boolean getAddEmpty();
	}

	private static final double MAX_INTERVALS = 100.0;

	/**
	 * Config-constructor for {@link NumberIntervalPartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public NumberIntervalPartition(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * Interval size 0 means no interval, thus behavior becomes that of a SingleValuePartition
	 * using discrete values as partitions
	 * 
	 * @return false if the configured interval size is 0.
	 * 
	 */
	protected boolean hasInterval() {
		return getConfig().getIntervalSize() > 0;
	}

	@Override
	protected List<Object> getOptions(Partition aParent) {
		if (!hasInterval()) {
			return super.getOptions(aParent);
		}
		Double start = getConfig().getIntervalStart();
		Double end = getConfig().getIntervalEnd();

		boolean hasStart = start != null;
		boolean hasEnd = end != null;

		if (!hasStart || !hasEnd) {
			Partition root = aParent;
			while (root.getParent() != null) {
				root = root.getParent();
			}

			List<TLObject> objects = CollectionUtil.dynamicCastView(TLObject.class, root.getObjects());
			for (TLObject attr : objects) {
				Number value = (Number) getValue(attr);
				if (!hasStart) {
					start = getStart(start, value);
				}
				if (!hasEnd) {
					end = getEnd(end, value);
				}
			}
		}
		if (start == null || end == null) {
			return Collections.emptyList();
		}

		List<NumberInterval> intervals = getIntervals(getConfig().getIntervalSize(), start, end);

		return CollectionUtil.dynamicCastView(Object.class, intervals);
	}

	@Override
	protected Comparable<?> getKey(Object val) {
		if (!hasInterval()) {
			return super.getKey(val);
		}
		return Configs.comparable((ConfigurationItem) val);
	}

	@Override
	protected void handleObjects(Partition aParent, Map<Object, Partition> map, List<TLObject> objects) {
		if (!hasInterval()) {
			super.handleObjects(aParent, map, objects);
			return;
		}

		Set<Object> keys = map.keySet();
		for (Object key : keys) {
			Partition partition = map.get(key);
			Filter<TLObject> filter;
			if (key instanceof NotSetText) {
				filter = new Filter<>() {
					@Override
					public boolean accept(TLObject anObject) {
						Object value = getValue(anObject);
						return value == null;
					}
				};
			} else {
				ComparableConfig<?> conf = (ComparableConfig<?>) key;
				NumberInterval interval = (NumberInterval) conf.getConfig();
				final double iStart = interval.getFrom().doubleValue();
				final double iEnd = interval.getTo().doubleValue();
				final boolean iClosed = interval.isClosed();

				filter = new Filter<>() {
					@Override
					public boolean accept(TLObject anObject) {
						Number value = (Number) getValue(anObject);
						if (value == null) {
							return false;
						} else {
							double doubleValue = value.doubleValue();
							return iStart <= doubleValue && (iClosed ? (doubleValue <= iEnd) : (doubleValue < iEnd));
						}
					}
				};
			}
			partition.getObjects().addAll(FilterUtil.filterList(filter, objects));
		}
	}

	private List<NumberInterval> getIntervals(Double size, Double startNumber, Double endNumber) {
		double start = startNumber.doubleValue();
		double end = endNumber.doubleValue();
		double intervalLength = size.doubleValue();

		double width = end - start;
		if (width < 0) {
			throw new IllegalArgumentException("Interval has length < 0!");
		}
		if (width < intervalLength) {
			double offset = intervalLength - width;
			end += offset;
			width += offset;
		}
		double estimatedIntervals = width / intervalLength;
		if (estimatedIntervals > MAX_INTERVALS) {
			intervalLength = width / MAX_INTERVALS;
		}

		int intervalCount = (int) Math.ceil(width / intervalLength);
		List<NumberInterval> result = new ArrayList<>(intervalCount);

		double intervalStart = start;
		double intervalEnd = start + intervalLength;
		for (int n = 0; n < intervalCount; n++) {
			boolean lastInterval = (n == (intervalCount - 1));
			NumberInterval entry;
			if (lastInterval) {
				// avoid rounding problems and set the end of the last interval to the given end
				entry = Configs.numberInterval(intervalStart, end, true);
			} else {
				entry = Configs.numberInterval(intervalStart, intervalEnd, false);
			}
			
			result.add(entry);

			intervalStart = intervalEnd;
			intervalEnd += intervalLength;
		}

		return result;
	}

	private Double getStart(Double start, Number value) {
		if (value == null) {
			return start;
		}
		if (start == null) {
			return value.doubleValue();
		}
		return Math.min(start, value.doubleValue());
	}

	private Double getEnd(Double end, Number value) {
		if (value == null) {
			return end;
		}
		if (end == null) {
			return value.doubleValue();
		}
		return Math.max(end, value.doubleValue());
	}

	@Override
	public Criterion getCriterion() {
		if (!hasInterval()) {
			return super.getCriterion();
		}
		return new ClassificationCriterion(getConfig().getMetaAttribute().get());
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(TLStructuredTypePart ma, double size) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setMetaAttribute(new MetaAttributeProvider(ma));
		item.setIntervalSize(size);
		return item;
	}

}