/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter;

import java.util.Map;

import com.top_logic.graph.layouter.algorithm.acycle.EadesLinSmythAcycleFinder;
import com.top_logic.graph.layouter.algorithm.clean.DefaultLayoutGraphCleaner;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.BrandesKoepfCoordinateAssigner;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.HorizontalCoordinatesAssigner;
import com.top_logic.graph.layouter.algorithm.coordinates.vertical.DefaultVerticalCoordinateAssigner;
import com.top_logic.graph.layouter.algorithm.edge.routing.OrthogonalEdgeRouter;
import com.top_logic.graph.layouter.algorithm.layering.LayeringFinder;
import com.top_logic.graph.layouter.algorithm.layering.LongestPathFromSinkLayeringFinder;
import com.top_logic.graph.layouter.algorithm.layering.LongestPathFromSourceLayeringFinder;
import com.top_logic.graph.layouter.algorithm.node.port.assigner.coordinates.DefaultNodePortCoordinateAssigner;
import com.top_logic.graph.layouter.algorithm.node.port.assigner.edges.DefaultNodePortEdgesAssigner;
import com.top_logic.graph.layouter.algorithm.node.port.orderer.DefaultNodePortOrderer;
import com.top_logic.graph.layouter.algorithm.node.size.DefaultNodeSizer;
import com.top_logic.graph.layouter.algorithm.ordering.EiglspergerLayerOrderingFinder;
import com.top_logic.graph.layouter.algorithm.ordering.LayerOrderingFinder;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayer;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Implementation of the Sugiyama graph layouting algorithm. A hierarchical graph drawing in which
 * vertices are drawn in horizontal layers and edges downwards to the next layer.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Sugiyama {
	
	/**
	 * Singleton {@link Sugiyama} instance.
	 */
	public static final Sugiyama INSTANCE = new Sugiyama();

	private Sugiyama() {
		// Singleton.
	}

	/**
	 * Layout the given graph in the given direction after the known Sugiyama algorithm.
	 */
	public void layout(LayoutContext context, LayoutGraph graph) {
		LayoutDirection direction = context.getDirection();

		LayoutGraph acyclicGraph = EadesLinSmythAcycleFinder.INSTANCE.findMaximalAcyclicSubgraph(graph);

		LayeringFinder layeringFinder;
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			layeringFinder = new LongestPathFromSourceLayeringFinder(acyclicGraph);
		} else {
			layeringFinder = new LongestPathFromSinkLayeringFinder(acyclicGraph);
		}

		Map<Integer, UnorderedNodeLayer> layering = layeringFinder.getLayering();

		DefaultNodePortEdgesAssigner.INSTANCE.assignNodePorts(context, acyclicGraph);

		new DefaultNodeSizer(context).size(acyclicGraph);

		LayerOrderingFinder orderingFinder = new EiglspergerLayerOrderingFinder(acyclicGraph, direction);
		Map<Integer, DefaultAlternatingLayer> ordering = orderingFinder.getLayerOrdering(layering);

		HorizontalCoordinatesAssigner coordinateAssigner = new BrandesKoepfCoordinateAssigner(acyclicGraph, direction);
		coordinateAssigner.setHorizontalNodeCoordinates(ordering, orderingFinder.getCrossingType1Edges());

		new DefaultNodePortOrderer(direction).orderNodePorts(acyclicGraph);

		DefaultNodePortCoordinateAssigner.INSTANCE.assignNodePorts(context, acyclicGraph);

		DefaultVerticalCoordinateAssigner.INSTANCE.setVerticalNodeCoordinates(ordering);

		OrthogonalEdgeRouter router = new OrthogonalEdgeRouter(direction);
		router.route(graph);

		DefaultLayoutGraphCleaner.INSTANCE.clean(acyclicGraph);

		restoreOriginalGraph(acyclicGraph);
	}

	private void restoreOriginalGraph(LayoutGraph graph) {
		LayoutGraphUtil.getNodesStream(graph).flatMap(node -> LayoutGraphUtil.getOutgoingEdgesStream(node))
			.filter(edge -> edge.isReversed()).forEach(edge -> {
				LayoutEdge reverse = edge.reverse();
				reverse.setReversed(false);
			});
	}

}
