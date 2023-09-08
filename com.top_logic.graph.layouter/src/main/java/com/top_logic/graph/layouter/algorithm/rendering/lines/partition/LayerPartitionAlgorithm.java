/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.partition;

import java.util.Collection;

/**
 * Algorithm to partition a collection of items into layers.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface LayerPartitionAlgorithm<T> {

	/**
	 * Partition the given objects into layers.
	 */
	LayerPartition<T> partition(Collection<T> items);

}
