/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.CoordinateCriterion;

/**
 * A Coordinate Partition can be used for XYPlots.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class CoordinatePartition implements PartitionFunction, ConfiguredInstance<CoordinatePartition.Config> {

	/**
	 * Simple enum for type-safty. Provides the name of the supported axes. So far, only X and Y are
	 * used because only XY-Plots are supported.
	 */
	public enum Coordinate {
		/**
		 * <code>X</code>-axis
		 */
		X,
		/**
		 * <code>Y</code>-axis
		 */
		Y,
		/**
		 * <code>Z</code>-axis
		 */
		Z
	}

	/**
	 * Config-interface for {@link CoordinatePartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		@Override
		@ClassDefault(CoordinatePartition.class)
		public Class<CoordinatePartition> getImplementationClass();

		/**
		 * the axis this {@link PartitionFunction} creates values for
		 */
		@Mandatory
		public Coordinate getCoordinate();

		/**
		 * see {@link #getCoordinate()}
		 */
		public void setCoordinate(Coordinate coord);

		/**
		 * the aggregation calculating a value on the axis defined in
		 *         {@link #getCoordinate()}
		 */
		@InstanceFormat
		public AggregationFunction getAggregation();

		/**
		 * see {@link #getAggregation()}
		 */
		public void setAggregation(AggregationFunction function);

	}

	private final Config _config;

	private Criterion _criterion;

	/**
	 * Config-constructor for {@link CoordinatePartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public CoordinatePartition(InstantiationContext context, Config config) {
		super();
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		Map<Double, Partition> map = new HashMap<>();
		AggregationFunction aggregation = getConfig().getAggregation();
		for (Object obj : aParent.getObjects()) {
			List<Object> singletonList = Collections.singletonList(obj);
			Number val = aggregation.calculate(aParent, singletonList);
			double coordinate = val.doubleValue();
			Partition partition = map.get(coordinate);
			if (partition == null) {
				partition = new Partition(aParent, coordinate, singletonList);
				map.put(coordinate, partition);
			}
			else {
				partition.getObjects().add(obj);
			}
		}
		List<Partition> result = new ArrayList<>();
		result.addAll(map.values());
		return result;
	}

	@Override
	public Criterion getCriterion() {
		if (_criterion == null) {
			_criterion = new CoordinateCriterion(getConfig().getCoordinate());
		}
		return _criterion;
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(Coordinate coord, AggregationFunction.Config config) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setCoordinate(coord);
		item.setAggregation(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config));
		return item;
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(Coordinate coord, AggregationFunction function) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setCoordinate(coord);
		item.setAggregation(function);
		return item;
	}

}