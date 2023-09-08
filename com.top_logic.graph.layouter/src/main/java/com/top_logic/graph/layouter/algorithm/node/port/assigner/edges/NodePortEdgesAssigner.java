/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.port.assigner.edges;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.algorithm.node.port.assigner.NodePortAssignAlgorithm;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.edge.LayoutEdgePartitioner;

/**
 * Assign {@link NodePort} using the given {@link LayoutEdgePartitioner}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NodePortEdgesAssigner implements NodePortAssignAlgorithm {

	private LayoutEdgePartitioner _incomingPortsPartitioner;

	private LayoutEdgePartitioner _outgoingPortsPartitioner;

	/**
	 * Creates an instance of {@link NodePortEdgesAssigner}.
	 * 
	 * @param incomingPartitioner
	 *        {@link LayoutEdgePartitioner} for incoming edges.
	 * @param outgoingPartitioner
	 *        {@link LayoutEdgePartitioner} for outgoing edges.
	 */
	public NodePortEdgesAssigner(LayoutEdgePartitioner incomingPartitioner, LayoutEdgePartitioner outgoingPartitioner) {
		_incomingPortsPartitioner = incomingPartitioner;
		_outgoingPortsPartitioner = outgoingPartitioner;
	}

	@Override
	public void assignNodePorts(LayoutContext context, LayoutNode node) {
		assignIncomingNodePorts(node);
		assignOutgoingNodePorts(node);
	}

	/**
	 * Assign all {@link NodePort}s of the given graph.
	 */
	public void assignNodePorts(LayoutContext context, LayoutGraph graph) {
		for (LayoutNode node : graph.nodes()) {
			assignNodePorts(context, node);
		}
	}

	private void assignOutgoingNodePorts(LayoutNode node) {
		node.setOutgoingPorts(getNodePorts(node, getPartition(_outgoingPortsPartitioner, node.outgoingEdges())));
	}

	private void assignIncomingNodePorts(LayoutNode node) {
		node.setIncomingPorts(getNodePorts(node, getPartition(_incomingPortsPartitioner, node.incomingEdges())));
	}

	private Set<Set<LayoutEdge>> getPartition(LayoutEdgePartitioner partitioner, Collection<LayoutEdge> edges) {
		return partitioner.getPartition(edges);
	}

	private List<NodePort> getNodePorts(LayoutNode node, Set<Set<LayoutEdge>> edgePartition) {
		return edgePartition.stream().map(portEdges -> new NodePort(node, portEdges)).collect(Collectors.toList());
	}

}
