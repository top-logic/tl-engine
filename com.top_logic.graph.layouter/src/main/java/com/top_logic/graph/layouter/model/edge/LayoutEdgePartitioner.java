/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.edge;

import java.util.Collection;
import java.util.Set;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;

/**
 * Algorithm to partition a {@link Set} of {@link LayoutEdge}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface LayoutEdgePartitioner {
	/**
	 * @param edges
	 *        {@link Collection} of {@link LayoutEdge} that should be partitioned.
	 * @return A Partition for the given {@link LayoutEdge}s.
	 */
	public Set<Set<LayoutEdge>> getPartition(Collection<LayoutEdge> edges);
}
