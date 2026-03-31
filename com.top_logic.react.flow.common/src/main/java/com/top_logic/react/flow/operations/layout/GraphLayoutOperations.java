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

		int portScale = com.top_logic.graph.layouter.GraphConstants.SCALE;

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

			LayoutEdge layoutEdge = graph.connect(sourceNode, targetNode, edge);
			layoutEdge.setPriority(edge.getPriority());
		}

		// Step 3: Create a NodeSizer that computes port widths from the LayoutNode's
		// actual edges. This runs INSIDE Sugiyama, after cycle breaking has reversed
		// edges, so isReversed() correctly reflects the current direction.
		NodeSizer sizer = new NodeSizer(
			n -> {
				double intrinsic = n.getWidth();

				double outWidth = computeTotalPortWidth(n.outgoingEdges(), true, portScale);
				double inWidth = computeTotalPortWidth(n.incomingEdges(), false, portScale);

				return Math.max(intrinsic, Math.max(outWidth, inWidth));
			},
			n -> n.getHeight()
		);

		// Step 4: Create LayoutContext with vertical top-to-bottom direction.
		LayoutContext layoutContext = new LayoutContext(LayoutDirection.VERTICAL_FROM_SOURCE);

		// Step 5: Run the Sugiyama layout algorithm.
		Sugiyama.INSTANCE.layout(layoutContext, graph, sizer, DecorationAwarePortCoordinateAssigner.INSTANCE);

		// Step 6: Find coordinate extent (Sugiyama may produce negative coordinates).
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = 0;
		double maxY = 0;

		for (Box node : nodes) {
			LayoutNode layoutNode = nodeMap.get(node);
			minX = Math.min(minX, layoutNode.getX());
			minY = Math.min(minY, layoutNode.getY());
			maxX = Math.max(maxX, layoutNode.getX() + Math.max(node.getWidth(), layoutNode.getWidth()));
			maxY = Math.max(maxY, layoutNode.getY() + Math.max(node.getHeight(), layoutNode.getHeight()));
		}

		for (LayoutNode layoutNode : graph.nodes()) {
			for (LayoutEdge layoutEdge : layoutNode.outgoingEdges()) {
				for (Waypoint wp : layoutEdge.getWaypoints()) {
					minX = Math.min(minX, wp.getX());
					minY = Math.min(minY, wp.getY());
					maxX = Math.max(maxX, wp.getX());
					maxY = Math.max(maxY, wp.getY());
				}
			}
		}

		// Shift so that the top-left corner is at (0, 0).
		double shiftX = minX < 0 ? -minX : 0;
		double shiftY = minY < 0 ? -minY : 0;

		// Step 7: Map layout results back to widget nodes (shifted).
		for (Box node : nodes) {
			LayoutNode layoutNode = nodeMap.get(node);
			double nodeX = layoutNode.getX() + shiftX;
			double nodeY = layoutNode.getY() + shiftY;
			node.setX(nodeX);
			node.setY(nodeY);

			double finalWidth = Math.max(node.getWidth(), layoutNode.getWidth());
			double finalHeight = Math.max(node.getHeight(), layoutNode.getHeight());
			node.setWidth(finalWidth);
			node.setHeight(finalHeight);

			node.distributeSize(context, nodeX, nodeY, finalWidth, finalHeight);
		}

		// Step 8: Map edge waypoints from LayoutEdge to GraphEdge (shifted).
		for (LayoutNode layoutNode : graph.nodes()) {
			for (LayoutEdge layoutEdge : layoutNode.outgoingEdges()) {
				Object businessObject = layoutEdge.getBusinessObject();
				if (!(businessObject instanceof GraphEdge)) {
					continue;
				}

				GraphEdge graphEdge = (GraphEdge) businessObject;
				graphEdge.getWaypoints().clear();

				for (Waypoint wp : layoutEdge.getWaypoints()) {
					GraphWaypoint graphWp = GraphWaypoint.create();
					graphWp.setX(wp.getX() + shiftX);
					graphWp.setY(wp.getY() + shiftY);
					graphEdge.addWaypoint(graphWp);
				}
			}
		}

		// Step 9: Set bounding box on self.
		self().setWidth(maxX + shiftX);
		self().setHeight(maxY + shiftY);
	}

	/**
	 * Computes the total port allocation width for a set of edges on one side of a node.
	 *
	 * <p>
	 * This method <strong>must</strong> mirror the per-port width logic in
	 * {@link DecorationAwarePortCoordinateAssigner#computePortWidth} exactly, including the
	 * {@code outgoing ^ edge.isReversed()} correction for cycle-breaking edge inversion. If
	 * the two computations diverge, nodes will be too narrow or too wide for their ports.
	 * See the class-level documentation on {@link DecorationAwarePortCoordinateAssigner} for
	 * background on the edge inversion issue.
	 * </p>
	 *
	 * <p>
	 * Called from the {@link NodeSizer} lambda during Sugiyama's coordinate assignment phase,
	 * i.e. <em>after</em> cycle breaking has already reversed edges.
	 * </p>
	 */
	private static double computeTotalPortWidth(Iterable<LayoutEdge> edges, boolean outgoing, int portScale) {
		double total = 0;
		for (LayoutEdge edge : edges) {
			Object bo = edge.getBusinessObject();
			if (!(bo instanceof GraphEdge)) {
				total += portScale;
				continue;
			}

			GraphEdge graphEdge = (GraphEdge) bo;
			boolean isSource = outgoing ^ edge.isReversed();

			ConnectorSymbol symbol = isSource ? graphEdge.getSourceSymbol() : graphEdge.getTargetSymbol();
			double width = ConnectorSymbolRenderer.inset(symbol, graphEdge.getThickness() / 2);

			for (EdgeDecoration decoration : graphEdge.getDecorations()) {
				if (decoration.getContent() == null) {
					continue;
				}
				double lp = decoration.getLinePosition();
				if ((isSource && lp <= 0.1) || (!isSource && lp >= 0.9)) {
					width = Math.max(width, decoration.getContent().getWidth());
				}
			}

			total += Math.max(portScale, width + portScale);
		}
		return total;
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
