/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.aggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.NumberComparator;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;

/**
 * Base-class for {@link AggregationFunction}s.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public abstract class AbstractAggregationFunction implements AggregationFunction,
		ConfiguredInstance<AggregationFunction.Config> {

	/**
	 * The {@link Operation} describes what to do with the number-values to calculate the result
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public enum Operation {
		/**
		 * <code>COUNT</code> counts the number of elements
		 */
		COUNT,
		/**
		 * <code>SUM</code> accumulates the number-values
		 */
		SUM,
		/**
		 * <code>MIN</code> finds the minimum of the number-values
		 */
		MIN,
		/**
		 * <code>MAX</code> finds the maximum of the number-values
		 */
		MAX,
		/**
		 * <code>AVERAGE</code> calculates the average of the number-values
		 */
		AVERAGE,
		/**
		 * <code>MEDIAN</code> calculates the median of the number-values
		 */
		MEDIAN;

		double calculate(double res, Object obj) {
			if (Double.isNaN(res)) {
				return getStartValue(obj);
			}
			double val = (obj instanceof Number) ? ((Number) obj).doubleValue() : 0;
			switch (this) {
				case SUM:
				case AVERAGE:
					return res + val;
				case MIN:
					return Math.min(val, res);
				case MAX:
					return Math.max(val, res);
				case COUNT:
					if (obj instanceof Collection<?>) {
						return res + ((Collection<?>) obj).size();
					} else {
						return res + (obj == null ? 0 : 1);
					}
				case MEDIAN:
					return 0.0;
				default:
					throw new UnsupportedOperationException();
			}
		}

		/**
		 * Whether the given value is accepted by {@link #calculate(double, Object)}.
		 * 
		 * @param value
		 *        The value created in {@link #getObjectValue(Object)}.
		 * @return <code>false</code>, if the value should be ignored.
		 */
		public boolean canHandle(Object value) {
			switch (this) {
				case SUM:
				case AVERAGE:
				case MEDIAN:
				case MIN:
				case MAX:
					return value != null;
				case COUNT:
					return true;
				default:
					throw new UnsupportedOperationException();
			}
		}

		private double getStartValue(Object val) {
			switch (this) {
				case MIN:
					return startVal(val, Double.MAX_VALUE);
				case MAX:
					return startVal(val, Double.MIN_VALUE);
				case COUNT:
					if (val instanceof Collection<?>) {
						return ((Collection<?>) val).size();
					} else {
						return (val == null ? 0 : 1);
					}
				case MEDIAN:
					return 0.0;
				default:
					return startVal(val, 0);
			}
		}

		private double startVal(Object val, double def) {
			if (val != null) {
				return ((Number) val).doubleValue();
			}
			return def;
		}

		double postCheck(List<Object> relevant, double res) {
			switch (this) {
				case AVERAGE:
					if (CollectionUtil.isEmptyOrNull(relevant)) {
						return res;
					}
					return res / relevant.size();
				case MEDIAN:
					if (CollectionUtil.isEmptyOrNull(relevant)) {
						return res;
					}
					List<Number> numbers =
						new ArrayList<>(CollectionUtil.dynamicCastView(Number.class, relevant));
					Collections.sort(numbers, NumberComparator.INSTANCE);
					int count = relevant.size();
					if (count % 2 != 0) {
						return numbers.get(count / 2).doubleValue();
					}
					else {
						Number n1 = numbers.get(count / 2 - 1);
						Number n2 = numbers.get(count / 2);
						return (n1.doubleValue() + n2.doubleValue()) / 2.0;
					}
				default:
					return res;
			}
		}

		/**
		 * @param values
		 *        the input-values to process according to this Operation.
		 * @return the result of the calculation according to this Operation.
		 */
		public double calculate(List<Object> values) {
			double res = Double.NaN;
			List<Object> relevant = new ArrayList<>();
			for (Object objectValue : values) {
				if (canHandle(objectValue)) {
					relevant.add(objectValue);
					res = calculate(res, objectValue);
				}
			}
			res = postCheck(relevant, res);
			if (Double.isNaN(res)) {
				res = 0;
			}
			return res;
		}

	}

	private final Config _config;

	private Criterion _criterion;

	private int _order;

	/**
	 * Config-interface for {@link AbstractAggregationFunction}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public interface Config extends AggregationFunction.Config {

		@Override
		@Hidden
		public Class<? extends AggregationFunction> getImplementationClass();

		/**
		 * The {@link Operation} describes how to aggregate the numbers extracted from the
		 * input-objects.
		 * 
		 * @return the operation, e.g. {@link Operation#SUM} if the values are accumulated.
		 */
		public Operation getOperation();

		/**
		 * see {@link #getOperation()}
		 */
		public void setOperation(Operation operation);
		
	}

	/**
	 * Config-constructor for {@link AbstractAggregationFunction}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public AbstractAggregationFunction(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public int compareTo(AggregationFunction o) {
		int indexCompare = CollectionUtil.compareInt(getOrder(), o.getOrder());
		if (indexCompare != 0) {
			return indexCompare;
		}
		return getLabel().compareTo(o.getLabel());
	}

	@Override
	public int getOrder() {
		return _order;
	}

	@Override
	public void setOrder(int index) {
		_order = index;
	}

	@Override
	public Number calculate(Partition parent, List<?> objects) {
		List<Object> numbers = toValueList(objects);
		Operation operation = getConfig().getOperation();
		return operation.calculate(numbers);
	}

	/**
	 * @param objects
	 *        the list of input-opbjects to get the relevant data from.
	 * @return a list of actual values for the elements from the given input.
	 */
	protected List<Object> toValueList(List<?> objects) {
		List<Object> result = new ArrayList<>(objects.size());
		for (Object object : objects) {
			result.add(getObjectValue(object));
		}
		return result;
	}

	@Override
	public abstract String getLabel();

	@Override
	public Criterion getCriterion() {
		if (_criterion == null) {
			_criterion = initCriterion();
		}
		return _criterion;
	}

	/**
	 * the {@link Criterion} containing context-information about this function.
	 */
	protected Criterion initCriterion() {
		return ValueCriterion.INSTANCE;
	}

	/**
	 * Calculates the number-value for the given object.
	 * 
	 * @param wrapper
	 *        the input the get the number for
	 * @return the number for calculation based on the configured {@link Operation}
	 */
	protected abstract Object getObjectValue(Object wrapper);

}
