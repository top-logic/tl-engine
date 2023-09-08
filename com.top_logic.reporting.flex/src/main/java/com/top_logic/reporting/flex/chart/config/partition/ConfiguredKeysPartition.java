/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.ResPrefix;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;
import com.top_logic.reporting.flex.chart.config.util.ToStringText;

/**
 * Creates partitions for a configured list of keys.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ConfiguredKeysPartition implements PartitionFunction, ConfiguredInstance<ConfiguredKeysPartition.Config> {

	/**
	 * Config-interface for {@link ConfiguredKeysPartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		@Override
		@ClassDefault(ConfiguredKeysPartition.class)
		public Class<? extends ConfiguredKeysPartition> getImplementationClass();

		/**
		 * the keys to create {@link Partition}s for
		 */
		@Format(CommaSeparatedStrings.class)
		public List<String> getKeys();

		/**
		 * See {@link #getKeys()}
		 */
		public void setKeys(List<String> keys);

		/**
		 * the prefix for the translation of the keyse
		 */
		@InstanceFormat
		public ResPrefix getResPrefix();

		/**
		 * See {@link #getResPrefix()}
		 */
		public void setResPrefix(ResPrefix string);

	}
	
	private final Config _config;

	/**
	 * Config-constructor for {@link ConfiguredKeysPartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ConfiguredKeysPartition(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<Partition> result = new ArrayList<>();
		List<String> keys = getConfig().getKeys();
		ResPrefix prefix = getConfig().getResPrefix();
		for (String key : keys) {
			Partition partition = new Partition(aParent, new ToStringText(prefix, key), aParent.getObjects());
			partition.put(aParent);
			result.add(partition);
		}
		return result;
	}

	@Override
	public Criterion getCriterion() {
		return ValueCriterion.INSTANCE;
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(ResPrefix resPrefix, List<String> keys) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setKeys(keys);
		item.setResPrefix(resPrefix);
		return item;
	}

}
