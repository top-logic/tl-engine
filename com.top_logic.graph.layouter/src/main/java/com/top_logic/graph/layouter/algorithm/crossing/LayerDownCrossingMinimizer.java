/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.crossing;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.comparator.LexicographicEdgeComparator;
import com.top_logic.graph.layouter.model.filter.FilterSegmentTargetNode;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.layer.SegmentContainer;
import com.top_logic.graph.layouter.model.layer.VirtualSegmentEdge;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * {@link LayerCrossingMinimizer} which sweeps down, i.e. from the topmost layer to the lowermost
 * layer.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayerDownCrossingMinimizer extends LayerCrossingMinimizer {

	public LayerDownCrossingMinimizer(LayoutDirection direction) {
		super(direction);
	}

	@Override
	void removeSegmentSources(DefaultAlternatingLayer layer) {
		Iterator<Pair<LayoutNode, SegmentContainer>> iterator = layer.getAll().iterator();
		SegmentContainer lastVisitedContainer = layer.getFirstSegmentContainer();

		while (iterator.hasNext()) {
			Pair<LayoutNode, SegmentContainer> item = iterator.next();
			LayoutNode node = item.getFirst();
			SegmentContainer container = item.getSecond();

			if (node.isSourceDummy()) {
				joinContainer(lastVisitedContainer, LayoutGraphUtil.getTopSegments(getDirection(), node), container);

				iterator.remove();
			} else {
				lastVisitedContainer = container;
			}
		}
	}

	@Override
	List<LayoutNode> getNonTargetDummyNodes(List<LayoutNode> nodes) {
		return LayoutGraphUtil.getFilteredNodes(FilterFactory.not(new FilterSegmentTargetNode()), nodes);
	}

	@Override
	double barycenterHeuristicMeasure(LayoutNode node, Map<Object, Integer> positions) {
		double measure = 0.0;
		int size = 0;

		Set<LayoutNode> nodes = LayoutGraphUtil.getTopNodes(getDirection(), node);

		for (LayoutNode source : nodes) {
			measure += positions.get(source);
			size++;
		}

		if (size == 0) {
			return 0;
		}

		return measure / size;
	}

	@Override
	Map<LayoutNode, SegmentContainer> addSegmentTargets(List<Object> ordering, List<LayoutNode> freeLayer) {
		Map<LayoutNode, SegmentContainer> targetContainers = new LinkedHashMap<>();

		for (LayoutNode node : freeLayer) {
			if (node.isTargetDummy()) {
				splitContainer(ordering, targetContainers, node);
			}
		}

		return targetContainers;
	}

	private void splitContainer(List<Object> ordering, Map<LayoutNode, SegmentContainer> containers, LayoutNode node) {
		Collection<LayoutEdge> topEdges = LayoutGraphUtil.getTopEdges(getDirection(), node);

		for (LayoutEdge edge : topEdges) {
			SegmentContainer splittedRootContainer = splitContainer(ordering, node, edge);

			containers.put(node, splittedRootContainer);
		}
	}

	@Override
	Set<CrossingEdge> getCrossings(List<Object> edges, List<Object> ordering, Map<Object, Integer> positions) {
		Set<CrossingEdge> crossings = new LinkedHashSet<>();

		for (int i = 0; i < edges.size(); i++) {
			for (int j = i + 1; j < edges.size(); j++) {
				CrossingEdge crossing = getCrossing(edges, ordering, i, j);

				if (crossing != null) {
					crossings.add(crossing);
				}
			}
		}

		return crossings;
	}

	private CrossingEdge getCrossing(List<Object> edges, List<Object> ordering, int i, int j) {
		int firstTargetIndex = getTargetOrder(edges, ordering, i);
		int secondTargetIndex = getTargetOrder(edges, ordering, j);

		if (isCrossingEdge(firstTargetIndex, secondTargetIndex)) {
			Object firstEdge = edges.get(i);
			Object secondEdge = edges.get(j);

			return new CrossingEdge(firstEdge, secondEdge);
		} else {
			return null;
		}
	}

	private boolean isCrossingEdge(int firstTargetIndex, int secondTargetIndex) {
		return firstTargetIndex > secondTargetIndex;
	}

	private int getTargetOrder(List<Object> sortedEdges, List<Object> layerOrdering, int i) {
		Object edge = sortedEdges.get(i);
		Object target = getEdgeBottomNode(edge);

		return layerOrdering.indexOf(target);
	}

	@Override
	void sortEdges(List<Object> edges, Map<Object, Integer> positions, List<Object> ordering) {
		Collections.sort(edges, new LexicographicEdgeComparator(positions, ordering, getDirection()));
	}

	@Override
	void getEdges(Map<LayoutNode, SegmentContainer> sources, List<Object> edges, LayoutNode node) {
		if (node.isTargetDummy()) {
			edges.add(LayoutGraphUtil.getVirtualEdgeSweepDown(getDirection(), sources.get(node), node));
		} else {
			Collection<LayoutEdge> topEdges = LayoutGraphUtil.getTopEdges(getDirection(), node);

			edges.addAll(topEdges);
		}
	}

	@Override
	void getEdges(List<Object> edges, SegmentContainer container) {
		SegmentContainer root = container.getRoot();

		if (root != null) {
			edges.add(LayoutGraphUtil.getVirtualEdge(getDirection(), root, container));
		} else {
			edges.add(new VirtualSegmentEdge(container, container));
		}
	}

}
