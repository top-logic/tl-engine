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

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.NoneCriterion;

/**
 * Simple {@link PartitionFunction} that splits pre-compiled data given as Map into partitions based
 * on the keys.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class KeySetPartition implements PartitionFunction, ConfiguredInstance<KeySetPartition.Config> {

	/**
	 * Config-interface for {@link KeySetPartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		@Override
		@ClassDefault(KeySetPartition.class)
		Class<KeySetPartition> getImplementationClass();

		/**
		 * The criterion of this partition function.
		 */
		@InstanceFormat
		@InstanceDefault(NoneCriterion.class)
		Criterion getCriterion();

		/**
		 * @see #getCriterion()
		 */
		void setCriterion(Criterion criterion);

	}

	private final Config _config;

	/**
	 * Config-constructor for {@link KeySetPartition}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public KeySetPartition(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<Object> objects = aParent.getObjects();
		switch (objects.size()) {
			case 0: {
				return Collections.emptyList();
			}
			default:
				assert objects.size() == 1;
				Object obj = CollectionUtil.getFirst(objects);
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Map<Comparable<?>, Object> map = (Map) obj;
				List<Partition> result = new ArrayList<>();
				for (Comparable<?> comp : map.keySet()) {
					result.add(new Partition(aParent, comp, Collections.singletonList(map.get(comp))));
				}
				return result;
		}
	}

	@Override
	public Criterion getCriterion() {
		return getConfig().getCriterion();
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item() {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		return item;
	}

}