/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart.flex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.ResPrefix;
import com.top_logic.reporting.common.tree.TreeInfo;
import com.top_logic.reporting.flex.chart.config.aggregation.AbstractAggregationFunction;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.util.Resources;

/**
 * {@link PartitionFunction} for status report chart.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class TreeInfoDatesPartition implements PartitionFunction,
		ConfiguredInstance<TreeInfoDatesPartition.Config> {

	/** Resource prefix for UI elements. */
	public static final ResPrefix RES_PREFIX = ResPrefix.legacyClassLocal(TreeInfoDatesPartition.class);

	private static final Property<Integer> INDEX_NC = TypedAnnotatable.property(Integer.class, "index");

	/**
	 * Config interface for {@link TreeInfoDatesPartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		@Override
		@ClassDefault(TreeInfoDatesPartition.class)
		public Class<? extends TreeInfoDatesPartition> getImplementationClass();

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link TreeInfoDatesPartition}.
	 */
	public TreeInfoDatesPartition(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<TreeInfo> infos = CollectionUtil.dynamicCastView(TreeInfo.class, aParent.getObjects());
		List<Partition> result = new ArrayList<>();
		int i = 0;
		for (TreeInfo info : infos) {
			StructuredElement element = (StructuredElement) info.getObject();

			Partition partition = new Partition(aParent, element, Collections.singletonList(element));
			partition.put(info);
			partition.put(INDEX_NC, i);
			result.add(partition);
			i++;
		}

		return result;
	}

	@Override
	public Criterion getCriterion() {
		return ValueCriterion.INSTANCE;
	}

	/**
	 * {@link AbstractAggregationFunction} for tree info charts.
	 */
	public static class TreeInfoIndexFunction extends AbstractAggregationFunction {

		/**
		 * Config interface for {@link TreeInfoIndexFunction}.
		 */
		public interface Config extends AbstractAggregationFunction.Config {

			@Override
			@ClassDefault(TreeInfoIndexFunction.class)
			public Class<? extends TreeInfoIndexFunction> getImplementationClass();

		}

		/**
		 * Creates a new {@link TreeInfoIndexFunction}.
		 */
		public TreeInfoIndexFunction(InstantiationContext context, TreeInfoIndexFunction.Config config) {
			super(context, config);
		}

		@Override
		public Number calculate(Partition parent, List<?> objects) {
			return parent.get(Partition.class).get(TreeInfoDatesPartition.INDEX_NC);
		}

		@Override
		public String getLabel() {
			return Resources.getInstance().getString(ResPrefix.legacyClass(getClass()).key("label"));
		}

		@Override
		protected Object getObjectValue(Object wrapper) {
			return wrapper;
		}

	}

}
