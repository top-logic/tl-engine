/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.port.assigner.edges;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.edge.SimpleLayoutEdgePartitioner;

/**
 * Default {@link NodePortEdgesAssigner} which maps each outgoing {@link LayoutEdge} to a new
 * {@link NodePort}. Incoming {@link LayoutEdge}s are partitioned by the existence of an userObject.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultNodePortEdgesAssigner extends NodePortEdgesAssigner {
	/**
	 * Singleton instance for {@link DefaultNodePortEdgesAssigner}.
	 */
	public static final DefaultNodePortEdgesAssigner INSTANCE = new DefaultNodePortEdgesAssigner();

	private DefaultNodePortEdgesAssigner() {
		super(SimpleLayoutEdgePartitioner.INSTANCE, SimpleLayoutEdgePartitioner.INSTANCE);
	}

}
