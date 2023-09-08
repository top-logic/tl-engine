/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.model;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.TLClass;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.reporting.flex.chart.config.util.MetaElementProvider;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public abstract class AbstractModelPreparationBuilder implements
		InteractiveBuilder<DefaultModelPreparation, ChartContextObserver>,
		ConfiguredInstance<AbstractModelPreparationBuilder.Config> {

	/**
	 * Base-config-interface for {@link AbstractModelPreparationBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<AbstractModelPreparationBuilder> {

		/**
		 * the provider for the {@link TLClass} this {@link ModelPreparation} works
		 *         with.
		 */
		public MetaElementProvider getType();

		/**
		 * see {@link #getType()}
		 */
		public void setType(MetaElementProvider me);
		
	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link AbstractModelPreparationBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public AbstractModelPreparationBuilder(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * Shortcut to {@link Config#setType(MetaElementProvider)}
	 */
	public void setTypes(Set<? extends TLClass> types) {
		getConfig().setType(new MetaElementProvider(types));
	}

	/**
	 * Shortcut to {@link Config#getType()}
	 */
	public Set<? extends TLClass> getTypes() {
		return getConfig().getType().get();
	}
	
	@Override
	public DefaultModelPreparation build(FormContainer container) {
		List<PartitionFunction.Config> partitions = getPartitions(container);
		List<AggregationFunction.Config> aggregations = getAggregations(container);
		return DefaultModelPreparation.create(partitions, aggregations);
	}

	/**
	 * Hook for subclasses to adapt aggregations.
	 * 
	 * @param container
	 *        the container for the aggregation-property
	 * @return the aggregations retrieved from the container
	 */
	protected abstract List<AggregationFunction.Config> getAggregations(FormContainer container);

	/**
	 * Hook for subclasses to adapt partitions.
	 * 
	 * @param container
	 *        the container for the partitions-property
	 * @return the partitions retrieved from the container
	 */
	protected abstract List<PartitionFunction.Config> getPartitions(FormContainer container);


}
