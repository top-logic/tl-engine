/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.reporting.flex.chart.config.model.Partition;

/**
 * A {@link PartitionFunction} creates list of {@link Partition}s for a given parent-partition.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface PartitionFunction {

	/**
	 * Base config-interface for {@link PartitionFunction}.
	 */
	@DisplayInherited(DisplayStrategy.APPEND)
	public interface Config extends PolymorphicConfiguration<PartitionFunction> {

		@Override
		@Hidden
		public Class<? extends PartitionFunction> getImplementationClass();

	}

	/**
	 * Creates a list of partitions for the objects provided by the given parent-partition.
	 * 
	 * @param aParent
	 *        contains the relevant objects to create partitions for, never null
	 * @return a list of partitions, may be empty but never null
	 */
	public List<Partition> createPartitions(Partition aParent);

	/**
	 * the {@link Criterion} providing context-information about this function.
	 */
	public Criterion getCriterion();

}