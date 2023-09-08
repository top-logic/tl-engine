/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.NoneCriterion;

/**
 * Simple {@link PartitionFunction} that creates partitions for configured enum-values.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class EnumPartition implements PartitionFunction {
	
	/**
	 * Config-interface for {@link EnumPartition}.
	 */
	public interface Config extends PartitionFunction.Config {
		
		@Override
		@ClassDefault(EnumPartition.class)
		public Class<? extends EnumPartition> getImplementationClass();
		
		/**
		 * names of the enum-values to create {@link Partition}s for
		 */
		@Format(CommaSeparatedStrings.class)
		public List<String> getValues();

		/**
		 * see {@link #getValues()}
		 */
		public void setValues(List<String> values);
		
		/**
		 * the enum-class to get the values from
		 */
		public Class<? extends Enum<?>> getEnum();

		/**
		 * see {@link #getEnum()}
		 */
		public void setEnum(Class<? extends Enum<?>> enumClass);
		
	}
	
	private final Config _config;
	
	/**
	 * Config-constructor for {@link EnumPartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public EnumPartition(InstantiationContext context, Config config) {
		_config = config;
	}
	
	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<Partition> result = new ArrayList<>();
		List<Enum<?>> types = getTypes();
		for (Enum<?> type : types) {
			Partition partition = new Partition(aParent, type, aParent.getObjects());
			result.add(partition);
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Enum<?>> getTypes() {
		List<Enum<?>> result = new ArrayList<>();
		Class<? extends Enum> enumeration = _config.getEnum();
		for (String type : _config.getValues()) {
			result.add(Enum.valueOf(enumeration, type));
		}
		return result;
	}
	
	@Override
	public Criterion getCriterion() {
		return NoneCriterion.INSTANCE;
	}
}