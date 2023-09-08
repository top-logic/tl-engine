/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.edge;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;

/**
 * Partition a set of {@link LayoutEdge}'s. For each edge a new subset is created.
 * 
 * For instance: {a,b,c} is partitioned into {{a}, {b}, {c}}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class SimpleLayoutEdgePartitioner implements LayoutEdgePartitioner {

	/**
	 * Singleton instance for {@link SimpleLayoutEdgePartitioner}.
	 */
	public static final SimpleLayoutEdgePartitioner INSTANCE = new SimpleLayoutEdgePartitioner();

	@Override
	public Set<Set<LayoutEdge>> getPartition(Collection<LayoutEdge> edges) {
		return edges.stream().map(edge -> Collections.singleton(edge)).collect(getLinkedHashSetCollector());
	}

	private Collector<Set<LayoutEdge>, ?, LinkedHashSet<Set<LayoutEdge>>> getLinkedHashSetCollector() {
		return Collectors.toCollection(LinkedHashSet::new);
	}

}
