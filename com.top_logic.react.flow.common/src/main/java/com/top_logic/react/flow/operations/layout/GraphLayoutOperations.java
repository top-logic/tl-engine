/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.Sugiyama;
// SimpleNodePortCoordinateAssigner replaced by DecorationAwarePortCoordinateAssigner
import com.top_logic.graph.layouter.algorithm.node.size.NodeSizer;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.Waypoint;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.ConnectorSymbol;
import com.top_logic.react.flow.data.EdgeDecoration;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphLayout;
import com.top_logic.react.flow.data.GraphWaypoint;
import com.top_logic.react.flow.operations.ConnectorSymbolRenderer;
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

		// Step 1: Compute intrinsic sizes of all child nodes and edge decorations.
		for (Box node : nodes) {
			node.computeIntrinsicSize(context, 0, 0);
		}
		for (GraphEdge edge : edges) {
			for (EdgeDecoration decoration : edge.getDecorations()) {
				if (decoration.getContent() != null) {
					decoration.getContent().computeIntrinsicSize(context, 0, 0);
				}
			}
		}

		if (nodes.isEmpty()) {
			self().setWidth(0);
			self().setHeight(0);
			return;
		}

		// Step 2: Compute extra width needed per node for ports and their decorations.
		Map<Box, Integer> outgoingPortCount = new HashMap<>();
		Map<Box, Integer> incomingPortCount = new HashMap<>();
		Map<Box, Double> maxOutgoingPortWidth = new HashMap<>();
		Map<Box, Double> maxIncomingPortWidth = new HashMap<>();

		for (GraphEdge edge : edges) {
			Box source = edge.getSource();
			Box target = edge.getTarget();

			double sourcePortWidth = symbolInset(edge.getSourceSymbol(), edge.getThickness());
			double targetPortWidth = symbolInset(edge.getTargetSymbol(), edge.getThickness());

			for (EdgeDecoration decoration : edge.getDecorations()) {
				if (decoration.getContent() == null) {
					continue;
				}
				double decorWidth = decoration.getContent().getWidth();
				if (decoration.getLinePosition() <= 0.1) {
					sourcePortWidth = Math.max(sourcePortWidth, decorWidth);
				} else if (decoration.getLinePosition() >= 0.9) {
					targetPortWidth = Math.max(targetPortWidth, decorWidth);
				}
			}

			if (source != null) {
				outgoingPortCount.merge(source, 1, Integer::sum);
				maxOutgoingPortWidth.merge(source, sourcePortWidth, Math::max);
			}
			if (target != null) {
				incomingPortCount.merge(target, 1, Integer::sum);
				maxIncomingPortWidth.merge(target, targetPortWidth, Math::max);
			}
		}

		// Step 3: Build a LayoutGraph from the widget nodes and edges.
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

			LayoutEdge layoutEdge = graph.connect(sourceNode, targetNode, edge);
			layoutEdge.setPriority(edge.getPriority());
		}

		// Step 4: Create a NodeSizer that accounts for port count and decoration widths.
		// Each port needs SCALE pixels of horizontal space, plus room for the widest decoration.
		int portScale = com.top_logic.graph.layouter.GraphConstants.SCALE;
		NodeSizer sizer = new NodeSizer(
			n -> {
				Box box = (Box) n.getUserObject();
				double intrinsic = n.getWidth();

				int outPorts = outgoingPortCount.getOrDefault(box, 0);
				int inPorts = incomingPortCount.getOrDefault(box, 0);
				double outDecor = maxOutgoingPortWidth.getOrDefault(box, 0.0);
				double inDecor = maxIncomingPortWidth.getOrDefault(box, 0.0);

				// Width needed for outgoing ports: count * scale + max decoration width
				double outWidth = outPorts * portScale + outDecor;
				// Width needed for incoming ports: count * scale + max decoration width
				double inWidth = inPorts * portScale + inDecor;

				return Math.max(intrinsic, Math.max(outWidth, inWidth));
			},
			n -> n.getHeight()
		);

		// Step 5: Create LayoutContext with vertical top-to-bottom direction.
		LayoutContext layoutContext = new LayoutContext(LayoutDirection.VERTICAL_FROM_SOURCE);

		// Step 6: Run the Sugiyama layout algorithm.
		Sugiyama.INSTANCE.layout(layoutContext, graph, sizer, DecorationAwarePortCoordinateAssigner.INSTANCE);

		// Step 7: Map layout results back to widget nodes.
		double maxX = 0;
		double maxY = 0;

		for (Box node : nodes) {
			LayoutNode layoutNode = nodeMap.get(node);
			double nodeX = layoutNode.getX();
			double nodeY = layoutNode.getY();
			node.setX(nodeX);
			node.setY(nodeY);

			// Sugiyama may have expanded the node; use the larger size.
			double finalWidth = Math.max(node.getWidth(), layoutNode.getWidth());
			double finalHeight = Math.max(node.getHeight(), layoutNode.getHeight());
			node.setWidth(finalWidth);
			node.setHeight(finalHeight);

			// Propagate final size and position to child widgets.
			node.distributeSize(context, nodeX, nodeY, finalWidth, finalHeight);

			maxX = Math.max(maxX, nodeX + finalWidth);
			maxY = Math.max(maxY, nodeY + finalHeight);
		}

		// Step 8: Map edge waypoints from LayoutEdge to GraphEdge.
		for (LayoutNode layoutNode : graph.nodes()) {
			for (LayoutEdge layoutEdge : layoutNode.outgoingEdges()) {
				Object businessObject = layoutEdge.getBusinessObject();
				if (!(businessObject instanceof GraphEdge)) {
					continue;
				}

				GraphEdge graphEdge = (GraphEdge) businessObject;
				graphEdge.getWaypoints().clear();

				List<Waypoint> waypoints = layoutEdge.getWaypoints();
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

		// Step 9: Set bounding box on self.
		self().setWidth(maxX);
		self().setHeight(maxY);
	}

	/**
	 * Computes the inset space needed for a connector symbol.
	 */
	private static double symbolInset(ConnectorSymbol symbol, double thickness) {
		if (symbol == null || symbol == ConnectorSymbol.NONE) {
			return 0;
		}
		return ConnectorSymbolRenderer.inset(symbol, thickness / 2);
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
