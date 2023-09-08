/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;

/**
 * Simple {@link PartitionFunction} that creates a new partition for every object in the parents
 * list. The objects are also used as key. In case of {@link Wrapper}s, the key is translated to its
 * current revision.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class PartitionPerObject implements PartitionFunction, ConfiguredInstance<PartitionPerObject.Config> {

	/**
	 * Config-interface for {@link PartitionPerObject}.
	 */
	public interface Config extends PartitionFunction.Config {

		@Override
		@ClassDefault(PartitionPerObject.class)
		public Class<PartitionPerObject> getImplementationClass();

	}

	private final Config _config;

	private Criterion _criterion = ValueCriterion.INSTANCE;

	/**
	 * Config-constructor for {@link PartitionPerObject}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public PartitionPerObject(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<Partition> result = new ArrayList<>();

		for (Object obj : aParent.getObjects()) {
			Comparable<?> key = getKey(obj);
			result.add(new Partition(aParent, key, Collections.singletonList(obj)));
		}
		return result;
	}

	private Comparable<?> getKey(Object obj) {
		if (obj instanceof Wrapper && !WrapperHistoryUtils.getRevision((Wrapper) obj).isCurrent()) {
			return WrapperHistoryUtils.getWrapper(Revision.CURRENT, (Wrapper) obj);
		}
		return (Comparable<?>) obj;
	}

	@Override
	public Criterion getCriterion() {
		return _criterion;
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