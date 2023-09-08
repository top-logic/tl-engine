/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.edge;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;

/**
 * Partition a set of {@link LayoutEdge}'s. Edges are separated by the existance of an userObject.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class BusinessObjectLayoutEdgePartitioner implements LayoutEdgePartitioner {
	private static final String NULL = "null";
	/**
	 * Singleton instance for {@link BusinessObjectLayoutEdgePartitioner}.
	 */
	public static final BusinessObjectLayoutEdgePartitioner INSTANCE = new BusinessObjectLayoutEdgePartitioner();

	@Override
	public Set<Set<LayoutEdge>> getPartition(Collection<LayoutEdge> edges) {
		return new LinkedHashSet<>(edges.stream().collect(getGroupingByCollector()).values());
	}

	private Collector<LayoutEdge, ?, Map<Object, Set<LayoutEdge>>> getGroupingByCollector() {
		return Collectors.groupingBy(edge -> {
			Object object = edge.getBusinessObject();

			if (object == null) {
				return NULL;
			}

			return object;
		}, Collectors.toCollection(LinkedHashSet::new));
	}
}
