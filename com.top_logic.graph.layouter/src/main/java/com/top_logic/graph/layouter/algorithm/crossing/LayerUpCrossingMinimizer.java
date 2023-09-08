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
import com.top_logic.graph.layouter.model.comparator.LexicographicEdgeReversedComparator;
import com.top_logic.graph.layouter.model.filter.FilterSegmentSourceNode;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.layer.SegmentContainer;
import com.top_logic.graph.layouter.model.layer.VirtualSegmentEdge;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * {@link LayerCrossingMinimizer} which sweeps up, i.e. from the lowermost layer to the topmost
 * layer.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayerUpCrossingMinimizer extends LayerCrossingMinimizer {

	public LayerUpCrossingMinimizer(LayoutDirection direction) {
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

			if (node.isTargetDummy()) {
				joinContainer(lastVisitedContainer, LayoutGraphUtil.getBottomSegments(getDirection(), node), container);

				iterator.remove();
			} else {
				lastVisitedContainer = container;
			}
		}
	}

	@Override
	List<LayoutNode> getNonTargetDummyNodes(List<LayoutNode> nodes) {
		return LayoutGraphUtil.getFilteredNodes(FilterFactory.not(new FilterSegmentSourceNode()), nodes);
	}

	@Override
	double barycenterHeuristicMeasure(LayoutNode node, Map<Object, Integer> positions) {
		double measure = 0.0;
		int size = 0;

		Set<LayoutNode> nodes = LayoutGraphUtil.getBottomNodes(getDirection(), node);

		for (LayoutNode target : nodes) {
			measure += positions.get(target);
			size++;
		}

		if (size == 0) {
			return 0;
		}

		return measure / size;
	}

	@Override
	Map<LayoutNode, SegmentContainer> addSegmentTargets(List<Object> ordering, List<LayoutNode> freeLayer) {
		Map<LayoutNode, SegmentContainer> sourceContainers = new LinkedHashMap<>();

		for (LayoutNode node : freeLayer) {
			if (node.isSourceDummy()) {
				splitContainer(ordering, sourceContainers, node);
			}
		}

		return sourceContainers;
	}

	private void splitContainer(List<Object> ordering, Map<LayoutNode, SegmentContainer> containers, LayoutNode node) {
		Collection<LayoutEdge> bottomEdges = LayoutGraphUtil.getBottomEdges(getDirection(), node);

		for (LayoutEdge edge : bottomEdges) {
			SegmentContainer splittedContainer = splitContainer(ordering, node, edge);

			containers.put(node, splittedContainer);
		}
	}

	@Override
	Set<CrossingEdge> getCrossings(List<Object> edges, List<Object> ordering, Map<Object, Integer> positions) {
		Set<CrossingEdge> crossings = new LinkedHashSet<>();

		for (int i = 0; i < edges.size(); i++) {
			for (int j = i + 1; j < edges.size(); j++) {
				CrossingEdge crossing = getCrossing(edges, positions, i, j);

				if (crossing != null) {
					crossings.add(crossing);
				}
			}
		}

		return crossings;
	}

	private CrossingEdge getCrossing(List<Object> edges, Map<Object, Integer> positions, int i, int j) {
		Object firstEdge = edges.get(i);
		Object secondEdge = edges.get(j);
		
		return getCrossing(positions, firstEdge, secondEdge);
	}

	private CrossingEdge getCrossing(Map<Object, Integer> positions, Object firstEdge, Object secondEdge) {
		int firstTargetIndex = positions.get(getEdgeBottomNode(firstEdge));
		int secondTargetIndex = positions.get(getEdgeBottomNode(secondEdge));

		if (isCrossingEdge(firstTargetIndex, secondTargetIndex)) {
			return new CrossingEdge(firstEdge, secondEdge);
		} else {
			return null;
		}
	}

	private boolean isCrossingEdge(int firstTargetIndex, int secondTargetIndex) {
		return firstTargetIndex > secondTargetIndex;
	}

	@Override
	void sortEdges(List<Object> edges, Map<Object, Integer> positions, List<Object> ordering) {
		Collections.sort(edges, new LexicographicEdgeReversedComparator(positions, ordering, getDirection()));
	}


	@Override
	void getEdges(List<Object> edges, SegmentContainer container) {
		SegmentContainer root = container.getRoot();

		if (root != null) {
			edges.add(LayoutGraphUtil.getVirtualEdge(getDirection(), container, root));
		} else {
			edges.add(new VirtualSegmentEdge(container, container));
		}
	}

	@Override
	void getEdges(Map<LayoutNode, SegmentContainer> sources, List<Object> edges, LayoutNode node) {
		if (node.isSourceDummy()) {
			edges.add(LayoutGraphUtil.getVirtualEdgeSweepUp(getDirection(), node, sources.get(node)));
		} else {
			Collection<LayoutEdge> bottomEdges = LayoutGraphUtil.getBottomEdges(getDirection(), node);

			edges.addAll(bottomEdges);
		}
	}

}
