/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;

/**
 * Simple {@link PartitionFunction} that splits {@link TLObject} values based on their
 * {@link com.top_logic.model.TLObject#tType() type}.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class MetaElementTypePartition implements PartitionFunction,
		ConfiguredInstance<PartitionFunction.Config> {

	/**
	 * Config-interface for {@link MetaElementTypePartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		/**
		 * the list of {@link TLClass}-types to split the raw-data into
		 */
		@Format(CommaSeparatedStrings.class)
		public List<String> getMetaElementTypes();

	}

	private final Config _config;

	private Criterion _criterion = ValueCriterion.INSTANCE;

	/**
	 * Config-constructor for {@link MetaElementTypePartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public MetaElementTypePartition(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Criterion getCriterion() {
		return _criterion;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<Partition> result = new ArrayList<>();
		List<String> types = getConfig().getMetaElementTypes();
		Map<String, List<Object>> map = new HashMap<>();
		for (String type : types) {
			map.put(type, new ArrayList<>());
		}
		for (Object obj : aParent.getObjects()) {
			if (obj instanceof TLObject) {
				TLStructuredType type = ((TLObject) obj).tType();
				while (type != null) {
					String name = type.getName();
					if (map.containsKey(name)) {
						map.get(name).add(obj);
						break;
					}
					if (type instanceof TLClass) {
						type = TLModelUtil.getPrimaryGeneralization((TLClass) type);
					} else {
						break;
					}
				}
			}
		}
		for (String type : types) {
			Partition partition =
				new Partition(aParent, type, map.get(type));
			result.add(partition);
		}
		return result;
	}

}