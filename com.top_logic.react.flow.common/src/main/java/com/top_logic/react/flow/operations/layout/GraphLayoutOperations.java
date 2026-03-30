/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.Sugiyama;
import com.top_logic.graph.layouter.algorithm.node.port.assigner.coordinates.SimpleNodePortCoordinateAssigner;
import com.top_logic.graph.layouter.algorithm.node.size.NodeSizer;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.Waypoint;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphLayout;
import com.top_logic.react.flow.data.GraphWaypoint;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Operations for a {@link GraphLayout}.
 *
 * <p>
 * Bridges the flow widget system to the Sugiyama layout algorithm. Nodes are positioned in layers
 * and edges are routed orthogonally between layers.
 * </p>
 */
public interface GraphLayoutOperations extends FloatingLayoutOperations {

	@Override
	GraphLayout self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setX(offsetX);
		self().setY(offsetY);

		List<Box> nodes = self().getNodes();
		List<GraphEdge> edges = self().getEdges();

		// Step 1: Compute intrinsic sizes of all child nodes.
		for (Box node : nodes) {
			node.computeIntrinsicSize(context, 0, 0);
		}

		if (nodes.isEmpty()) {
			self().setWidth(0);
			self().setHeight(0);
			return;
		}

		// Step 2: Build a LayoutGraph from the widget nodes and edges.
		LayoutGraph graph = new LayoutGraph();
		Map<Box, LayoutNode> nodeMap = new HashMap<>();

		for (Box node : nodes) {
			LayoutNode layoutNode = graph.newNode(node);
			graph.add(layoutNode);
			layoutNode.setWidth(node.getWidth());
			layoutNode.setHeight(node.getHeight());
			nodeMap.put(node, layoutNode);
		}

		for (GraphEdge edge : edges) {
			Box source = edge.getSource();
			Box target = edge.getTarget();

			if (source == null || target == null) {
				continue;
			}

			LayoutNode sourceNode = nodeMap.get(source);
			LayoutNode targetNode = nodeMap.get(target);

			if (sourceNode == null || targetNode == null) {
				continue;
			}

			graph.connect(sourceNode, targetNode, edge);
		}

		// Step 3: Create a NodeSizer that preserves the already-computed sizes.
		NodeSizer sizer = new NodeSizer(n -> n.getWidth(), n -> n.getHeight());

		// Step 4: Create LayoutContext with vertical top-to-bottom direction.
		LayoutContext layoutContext = new LayoutContext(LayoutDirection.VERTICAL_FROM_SOURCE);

		// Step 5: Run the Sugiyama layout algorithm.
		Sugiyama.INSTANCE.layout(layoutContext, graph, sizer, SimpleNodePortCoordinateAssigner.INSTANCE);

		// Step 6: Map layout results back to widget nodes.
		double maxX = 0;
		double maxY = 0;

		for (Box node : nodes) {
			LayoutNode layoutNode = nodeMap.get(node);
			double nodeX = layoutNode.getX();
			double nodeY = layoutNode.getY();
			node.setX(nodeX);
			node.setY(nodeY);

			// Propagate final size and position to child widgets.
			node.distributeSize(context, nodeX, nodeY, node.getWidth(), node.getHeight());

			maxX = Math.max(maxX, nodeX + layoutNode.getWidth());
			maxY = Math.max(maxY, nodeY + layoutNode.getHeight());
		}

		// Step 7: Map edge waypoints from LayoutEdge to GraphEdge.
		for (LayoutNode layoutNode : graph.nodes()) {
			for (LayoutEdge layoutEdge : layoutNode.outgoingEdges()) {
				Object businessObject = layoutEdge.getBusinessObject();
				if (!(businessObject instanceof GraphEdge)) {
					continue;
				}

				GraphEdge graphEdge = (GraphEdge) businessObject;
				graphEdge.getWaypoints().clear();

				List<Waypoint> waypoints = layoutEdge.getWaypoints();
				edgeCount++;
				waypointTotal += waypoints.size();
				for (Waypoint wp : waypoints) {
					GraphWaypoint graphWp = GraphWaypoint.create();
					graphWp.setX(wp.getX());
					graphWp.setY(wp.getY());
					graphEdge.addWaypoint(graphWp);

					maxX = Math.max(maxX, wp.getX());
					maxY = Math.max(maxY, wp.getY());
				}
			}
		}

		// Step 8: Set bounding box on self.
		self().setWidth(maxX);
		self().setHeight(maxY);
	}

	@Override
	default void drawContents(SvgWriter out) {
		// Draw nodes (handled by parent via FloatingLayoutOperations).
		FloatingLayoutOperations.super.drawContents(out);

		// Draw edges.
		for (GraphEdge edge : self().getEdges()) {
			edge.draw(out);
		}
	}

}
