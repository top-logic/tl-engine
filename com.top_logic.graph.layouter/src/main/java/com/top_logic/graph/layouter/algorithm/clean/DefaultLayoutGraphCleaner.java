/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.clean;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.top_logic.graph.layouter.algorithm.edge.waypoint.OrthogonalRedundantWaypointsRemover;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.Waypoint;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Default layout graph cleaner. Removes dummy nodes and insert waypoints instead.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultLayoutGraphCleaner implements GraphCleanAlgorithm<LayoutGraph> {

	/**
	 * Singleton instance.
	 */
	public static final DefaultLayoutGraphCleaner INSTANCE = new DefaultLayoutGraphCleaner();

	private DefaultLayoutGraphCleaner() {
		// Singleton.
	}

	@Override
	public void clean(LayoutGraph graph) {
		removeDummyNodes(graph);
		removeReduntantEdgeWaypoints(graph);
	}

	private void removeReduntantEdgeWaypoints(LayoutGraph graph) {
		OrthogonalRedundantWaypointsRemover redundantWaypointsRemover = OrthogonalRedundantWaypointsRemover.INSTANCE;

		LayoutGraphUtil.getNodesStream(graph).flatMap(node -> node.outgoingEdges().stream()).forEach(edge -> {
			List<Waypoint> waypoints = edge.getWaypoints();

			redundantWaypointsRemover.removeRedundantWaypoints(waypoints);

			edge.setWaypoints(waypoints);
		});
	}

	private void removeDummyNodes(LayoutGraph graph) {
		getNotDummyNodes(graph).stream().forEach(node -> {
			getDummyTargetEdges(node).stream().forEach(edge -> {
				node.getOutgoingPort(edge).ifPresent(port -> fix(graph, edge, port));
			});
		});
	}

	private void fix(LayoutGraph graph, LayoutEdge edge, NodePort outgoingPort) {
		fix(graph, edge, new LinkedList<>(), outgoingPort);
	}

	private void fix(LayoutGraph graph, LayoutEdge edge, List<Waypoint> waynodes, NodePort outgoingPort) {
		LayoutNode target = edge.target();

		waynodes.addAll(edge.getWaypoints());

		if (!target.isDummy()) {
			fixEndPart(graph, edge, waynodes, outgoingPort);
		} else {
			fixDummyPart(graph, waynodes, outgoingPort, target);
		}

		edge.remove();
	}

	private void fixDummyPart(LayoutGraph graph, List<Waypoint> waynodes, NodePort outgoingPort, LayoutNode target) {
		Optional<LayoutEdge> firstOutgoingEdge = LayoutGraphUtil.getOutgoingEdgesStream(target).findFirst();
		firstOutgoingEdge.ifPresent(edge -> fix(graph, edge, waynodes, outgoingPort));

		graph.remove(target);
	}

	private void fixEndPart(LayoutGraph graph, LayoutEdge edge, List<Waypoint> waynodes, NodePort outgoingPort) {
		LayoutNode target = edge.target();

		target.getIncomingPort(edge).ifPresent(incomingPort -> {
			LayoutEdge connect = LayoutGraphUtil.createEdge(graph, outgoingPort, incomingPort, edge);

			connect.setWaypoints(waynodes);
		});
	}

	private Set<LayoutEdge> getDummyTargetEdges(LayoutNode node) {
		return LayoutGraphUtil.getOutgoingEdgesStream(node).filter(getDummyTargetFilter()).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private Predicate<? super LayoutEdge> getDummyTargetFilter() {
		return edge -> edge.target().isDummy();
	}

	private Set<LayoutNode> getNotDummyNodes(LayoutGraph graph) {
		return graph.nodes().stream().filter(node -> !node.isDummy()).collect(Collectors.toCollection(LinkedHashSet::new));
	}

}

