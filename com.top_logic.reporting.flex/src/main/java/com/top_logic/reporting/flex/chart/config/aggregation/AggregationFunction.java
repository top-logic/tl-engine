/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.aggregation;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;

/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface AggregationFunction extends Comparable<AggregationFunction> {

	/**
	 * Base config-interface for {@link AggregationFunction}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public interface Config extends PolymorphicConfiguration<AggregationFunction> {
		// base interface
	}

	/**
	 * Returnes a number-value based on the given input. E.g. the sum of a number-valued attribute
	 * for given wrapper-valued objects.
	 * 
	 * @param parent
	 *        the partition containing the objects for context-information
	 * @param objects
	 *        the objects to calculate the value for
	 * @return the number calculated from the given objects
	 */
	public Number calculate(Partition parent, List<?> objects);

	/**
	 * the label for this function.
	 */
	public String getLabel();

	/**
	 * the {@link Criterion} providing context-information about this function.
	 */
	public Criterion getCriterion();

	/**
	 * The sort index of this function.
	 */
	int getOrder();

	/**
	 * @see #getOrder()
	 */
	void setOrder(int index);
	
}