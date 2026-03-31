/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import java.util.List;

import com.top_logic.graph.layouter.GraphConstants;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.algorithm.node.port.assigner.NodePortAssignAlgorithm;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.react.flow.data.ConnectorSymbol;
import com.top_logic.react.flow.data.EdgeDecoration;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.operations.ConnectorSymbolRenderer;

/**
 * Port coordinate assigner that accounts for per-port decoration widths.
 *
 * <p>
 * Each port gets horizontal space proportional to its decoration width (symbol inset + label width).
 * Ports without decorations get the minimum {@link GraphConstants#SCALE} width.
 * </p>
 */
public class DecorationAwarePortCoordinateAssigner implements NodePortAssignAlgorithm {

	/** Singleton instance. */
	public static final DecorationAwarePortCoordinateAssigner INSTANCE = new DecorationAwarePortCoordinateAssigner();

	private DecorationAwarePortCoordinateAssigner() {
		// Singleton.
	}

	@Override
	public void assignNodePorts(LayoutContext context, LayoutNode node) {
		assignPorts(node, node.getOutgoingPorts(), true);
		assignPorts(node, node.getIncomingPorts(), false);
	}

	private void assignPorts(LayoutNode node, List<NodePort> ports, boolean outgoing) {
		if (ports.isEmpty()) {
			return;
		}

		// Compute per-port widths.
		double[] portWidths = new double[ports.size()];
		double totalWidth = 0;
		for (int i = 0; i < ports.size(); i++) {
			portWidths[i] = computePortWidth(ports.get(i), outgoing);
			totalWidth += portWidths[i];
		}

		// Center the ports within the node.
		double offset = (node.getWidth() - totalWidth) / 2;
		double x = node.getX() + Math.max(0, offset);

		double halfScale = GraphConstants.SCALE / 2.0;
		for (int i = 0; i < ports.size(); i++) {
			// Place port at the left edge of its allocation (+ half scale for edge centering).
			// The decoration label extends to the right from the port position.
			ports.get(i).setX(x + halfScale);
			x += portWidths[i];
		}
	}

	private double computePortWidth(NodePort port, boolean outgoing) {
		double minWidth = GraphConstants.SCALE;

		for (LayoutEdge edge : port.getAttachedEdges()) {
			Object bo = edge.getBusinessObject();
			if (!(bo instanceof GraphEdge)) {
				continue;
			}

			GraphEdge graphEdge = (GraphEdge) bo;

			// Account for edge inversion during cycle breaking: if the layout edge
			// is reversed, source and target semantics are swapped.
			boolean isSource = outgoing ^ edge.isReversed();

			// Symbol inset at this end.
			ConnectorSymbol symbol = isSource ? graphEdge.getSourceSymbol() : graphEdge.getTargetSymbol();
			double width = ConnectorSymbolRenderer.inset(symbol, graphEdge.getThickness() / 2);

			// Decoration width at this end.
			for (EdgeDecoration decoration : graphEdge.getDecorations()) {
				if (decoration.getContent() == null) {
					continue;
				}
				double lp = decoration.getLinePosition();
				if ((isSource && lp <= 0.1) || (!isSource && lp >= 0.9)) {
					width = Math.max(width, decoration.getContent().getWidth());
				}
			}

			minWidth = Math.max(minWidth, width + GraphConstants.SCALE);
		}

		return minWidth;
	}

}
