/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;

/**
 * Simple {@link PartitionFunction} that creates partitions based on {@link TLClass}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class TLClassPartition implements PartitionFunction {
	
	/**
	 * Config-interface for {@link TLClassPartition}.
	 */
	public interface Config extends PartitionFunction.Config {
		
		@Override
		@ClassDefault(TLClassPartition.class)
		public Class<? extends TLClassPartition> getImplementationClass();
		
	}
	
	private final Config _config;
	
	/**
	 * Config-constructor for {@link TLClassPartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public TLClassPartition(InstantiationContext context, Config config) {
		_config = config;
	}
	
	@Override
	public List<Partition> createPartitions(Partition aParent) {
		Map<TLStructuredType, Partition> result = new HashMap<>();
		for (Object obj : aParent.getObjects()) {
			Partition partition = getPartition(aParent, result, getType(obj));
			if (partition != null) {
				partition.getObjects().add(obj);
			}
		}
		return new ArrayList<>(result.values());
	}
	
	private Partition getPartition(Partition parent, Map<TLStructuredType, Partition> map, TLStructuredType type) {
		if (type == null) {
			return null;
		}
		Partition result = map.get(type);
		if (result == null) {
			result = new Partition(parent, (Comparable<?>) type, Collections.emptyList());
			map.put(type, result);
		}
		return result;
	}

	private TLStructuredType getType(Object obj) {
		if (obj instanceof TLObject) {
			return ((TLObject) obj).tType();
		}
		return null;
	}

	@Override
	public Criterion getCriterion() {
		return ValueCriterion.INSTANCE;
	}
}