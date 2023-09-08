/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.aggregation.CountFunction;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilderUtil;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.reporting.flex.search.chart.GenericModelPreparationBuilder.AggregationOptions;
import com.top_logic.reporting.flex.search.chart.GenericModelPreparationBuilder.PartitionOptions;

/**
 * Default implementation of {@link ModelPreparation} that works with {@link PartitionFunction} and
 * {@link AggregationFunction}.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DefaultModelPreparation implements ModelPreparation,
		InteractiveBuilder<DefaultModelPreparation, ChartContextObserver>,
		ConfiguredInstance<DefaultModelPreparation.Config> {

	/**
	 * Config-interface for {@link DefaultModelPreparation}.
	 */
	@DisplayOrder({ Config.PARTITIONS, Config.AGGREGATIONS })
	public interface Config extends ModelPreparation.Config {

		/**
		 * <code>AGGREGATIONS</code> Attribute name for aggregations-property
		 */
		public static final String AGGREGATIONS = "aggregations";

		/**
		 * <code>PARTITIONS</code> Attribute name for partitions-property
		 */
		public static final String PARTITIONS = "partitions";

		@Override
		@ClassDefault(DefaultModelPreparation.class)
		@Hidden
		public Class<DefaultModelPreparation> getImplementationClass();

		/**
		 * List of {@link PartitionFunction} configurations that are used to build multiple levels
		 * in the {@link ChartTree} model.
		 */
		@Name(PARTITIONS)
		@Options(fun = PartitionOptions.class)
		public List<PartitionFunction.Config> getPartitions();

		/**
		 * see {@link #getPartitions()}
		 */
		public void setPartitions(List<PartitionFunction.Config> partitions);

		/**
		 * the configured aggregation-configs
		 */
		@Name(AGGREGATIONS)
		@Options(fun = AggregationOptions.class)
		public List<AggregationFunction.Config> getAggregations();

		/**
		 * see {@link #getAggregations()}
		 */
		public void setAggregations(List<AggregationFunction.Config> aggregations);

	}

	private final Config _config;

	private static final Property<Partition> PARTITION = TypedAnnotatable.property(Partition.class, "partition");

	private List<PartitionFunction> _partitions;

	private List<AggregationFunction> _aggregations;

	/**
	 * Config-Constructor for {@link DefaultModelPreparation}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public DefaultModelPreparation(InstantiationContext context, Config config) {
		_config = config;
		_partitions = TypedConfiguration.getInstanceList(context, config.getPartitions());
		_aggregations = TypedConfiguration.getInstanceList(context, config.getAggregations());

		if (CollectionUtil.isEmptyOrNull(_aggregations)) {
			_aggregations =
				Collections.<AggregationFunction> singletonList(new CountFunction(
					SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, CountFunction.item()));
		}

		for (int n = 0, cnt = _aggregations.size(); n < cnt; n++) {
			_aggregations.get(n).setOrder(n);
		}
	}

	@Override
	public ChartTree prepare(Collection<? extends Object> rawData) {
		ArrayList<Object> list = new ArrayList<>(rawData);
		ChartNode root = new ChartNode(null, null, list, null, null, 0);
		ChartTree result = root.getTree();
		Partition rootPartition = new Partition(null, null, list);
		root.set(PARTITION, rootPartition);
		initTree(root, 0);
		return result;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	private void initTree(ChartNode parentNode, int nextPartition) {
		if (nextPartition >= _partitions.size()) {
			handleAggregationFunctions(parentNode);
			return;
		}
		Partition parent = parentNode.get(PARTITION);

		PartitionFunction function = _partitions.get(nextPartition);
		parentNode.getTree().setCriterion(nextPartition, function.getCriterion());
		List<Partition> partitions = function.createPartitions(parent);
		nextPartition++;
		for (Partition partition : partitions) {
			List<?> objects = partition.getObjects();
			ChartNode childNode = new ChartNode(partition.getKey(), parentNode, objects, objects.size());
			childNode.set(PARTITION, partition);
			initTree(childNode, nextPartition);
		}
	}

	private void handleAggregationFunctions(ChartNode parentNode) {
		Partition parent = parentNode.get(PARTITION);
		if (_aggregations.size() == 1) {
			AggregationFunction function = CollectionUtil.getFirst(_aggregations);
			Number value = function.calculate(parent, parentNode.getObjects());
			parentNode.setValue(value);
			parentNode.setIsLeaf(true);
			parentNode.getTree().setCriterion(parentNode.getTree().getDepth(), function.getCriterion());
		} else {
			for (AggregationFunction function : _aggregations) {
				Number value = function.calculate(parent, parentNode.getObjects());
				if (value == null) {
					return;
				}
				ChartNode node = new ChartNode(function, parentNode, parentNode.getObjects(), value);
				node.getTree().setCriterion(node.getTree().getDepth() - 1, function.getCriterion());
				node.setIsLeaf(true);
			}
		}
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		InteractiveBuilderUtil.fillContainer(container, getConfig(), observer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DefaultModelPreparation build(FormContainer container) {
		Map<String, Object> map = InteractiveBuilderUtil.create(container, getConfig());
		getConfig().setPartitions((List<PartitionFunction.Config>) map.get(Config.PARTITIONS));
		getConfig().setAggregations((List<AggregationFunction.Config>) map.get(Config.AGGREGATIONS));
		return this;
	}

	/**
	 * Factory method to create an initialized {@link DefaultModelPreparation}.
	 * 
	 * @return a new DefaultModelPreparation.
	 */
	public static DefaultModelPreparation create(List<PartitionFunction.Config> partitions, List<AggregationFunction.Config> aggregations) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setPartitions(partitions);
		item.setAggregations(aggregations);
		return (DefaultModelPreparation) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(item);
	}

}
