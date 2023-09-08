/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.acycle;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.filter.FilterMarkedEdge;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * A Finder to get the maximal acyclic subgraph for a given graph which uses the Strategy from
 * Eades, Lin and Smyth 1993.
 * 
 * Selfloops are removed and other conflicting edges are reversed.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class EadesLinSmythAcycleFinder extends AcycleFinder {

	/**
	 * Singleton instance for {@link EadesLinSmythAcycleFinder}.
	 */
	public static final EadesLinSmythAcycleFinder INSTANCE = new EadesLinSmythAcycleFinder();

	/**
	 * Creates the acyclic graph finder.
	 */
	private EadesLinSmythAcycleFinder() {
		super();
	}

	private Set<LayoutEdge> calculateConflictingEdges(Set<LayoutNode> toVisitedNodes, Set<LayoutEdge> markedEdges) {
		if (!toVisitedNodes.isEmpty()) {
			Filter<? super LayoutEdge> filterNotMarkedEdge = FilterFactory.not(new FilterMarkedEdge(markedEdges));

			LayoutNode maxUnbalancedNode = getMaximalUnbalancedNode(toVisitedNodes, filterNotMarkedEdge);

			Set<LayoutEdge> incomingEdges =
				LayoutGraphUtil.getFilteredEdges(filterNotMarkedEdge, maxUnbalancedNode.incomingEdges());
			Set<LayoutEdge> outgoingEdges =
				LayoutGraphUtil.getFilteredEdges(filterNotMarkedEdge, maxUnbalancedNode.outgoingEdges());

			toVisitedNodes.remove(maxUnbalancedNode);

			markedEdges.addAll(outgoingEdges);
			markedEdges.addAll(incomingEdges);

			return getConflictingEdges(incomingEdges, outgoingEdges);
		}

		return new LinkedHashSet<>();
	}

	private Set<LayoutEdge> getConflictingEdges(Set<LayoutEdge> incomingEdges, Set<LayoutEdge> outgoingEdges) {
		List<Integer> incomingEdgePriorities = LayoutGraphUtil.getDescendingOrderedPriorities(incomingEdges);
		List<Integer> outgoingEdgePriorities = LayoutGraphUtil.getDescendingOrderedPriorities(outgoingEdges);

		if (compareLexicographic(incomingEdgePriorities, outgoingEdgePriorities) < 0) {
			return new LinkedHashSet<>(incomingEdges);
		} else {
			return new LinkedHashSet<>(outgoingEdges);
		}
	}

	private int compareLexicographic(List<Integer> list1, List<Integer> list2) {
		final Iterator<Integer> iterator1 = list1.iterator(), iterator2 = list2.iterator();

		while (iterator1.hasNext() && iterator2.hasNext()) {
			Integer next1 = iterator1.next();
			Integer next2 = iterator2.next();

			int compare = Integer.compare(next1, next2);

			if (compare != 0) {
				return compare;
			}
		}

		if (iterator1.hasNext() && !iterator2.hasNext())
			return 1;
		if (!iterator1.hasNext() && iterator2.hasNext())
			return -1;

		return 0;
	}

	private LayoutNode getMaximalUnbalancedNode(Set<LayoutNode> toVisitedNodes, Filter<? super LayoutEdge> filter) {
		Iterator<LayoutNode> toVisitedNodesIterator = toVisitedNodes.iterator();
		LayoutNode maxUnbalancedNode = toVisitedNodesIterator.next();

		int maxDiffOutgoingIncomingEdges = getDifferenceOutgoingIncomingEdges(maxUnbalancedNode, filter);

		while (toVisitedNodesIterator.hasNext()) {
			LayoutNode node = toVisitedNodesIterator.next();

			int differenceOutgoingIncomingEdges = getDifferenceOutgoingIncomingEdges(node, filter);

			if (differenceOutgoingIncomingEdges > maxDiffOutgoingIncomingEdges) {
				maxUnbalancedNode = node;

				maxDiffOutgoingIncomingEdges = differenceOutgoingIncomingEdges;
			}
		}

		return maxUnbalancedNode;
	}

	private int getDifferenceOutgoingIncomingEdges(LayoutNode node, Filter<? super LayoutEdge> filter) {
		Set<LayoutEdge> outgoingEdges = new LinkedHashSet<>(node.outgoingEdges());
		Set<LayoutEdge> incomingEdges = new LinkedHashSet<>(node.incomingEdges());

		return LayoutGraphUtil.getSizeDiff(outgoingEdges, incomingEdges, filter);
	}

	private void markSources(Set<LayoutNode> toVisitedNodes, Set<LayoutEdge> markedEdges) {
		LinkedHashSet<LayoutNode> sources = LayoutGraphUtil.getSources(toVisitedNodes, markedEdges);

		while (!sources.isEmpty()) {
			for (LayoutNode source : sources) {
				toVisitedNodes.remove(source);
				markedEdges.addAll(source.outgoingEdges());
			}

			sources = LayoutGraphUtil.getSources(toVisitedNodes, markedEdges);
		}
	}

	private void markSinks(Set<LayoutNode> toVisitedNodes, Set<LayoutEdge> markedEdges) {
		LinkedHashSet<LayoutNode> sinks = LayoutGraphUtil.getSinks(toVisitedNodes, markedEdges);

		while (!sinks.isEmpty()) {
			for (LayoutNode sink : sinks) {
				toVisitedNodes.remove(sink);
				markedEdges.addAll(sink.incomingEdges());
			}

			sinks = LayoutGraphUtil.getSinks(toVisitedNodes, markedEdges);
		}
	}

	private Set<LayoutEdge> getSelfLoops(LayoutGraph graph) {
		Set<LayoutEdge> selfLoops = new LinkedHashSet<>();

		for (LayoutNode node : graph.nodes()) {
			selfLoops.addAll(graph.edges(node, node));
		}

		return selfLoops;
	}

	void resolveConflictingEdge(LayoutEdge edge) {
		if (isSelfLoop(edge)) {
			removeEdge(edge);
		} else {
			reverseEdge(edge);
		}
	}

	private boolean isSelfLoop(LayoutEdge edge) {
		return edge.target() == edge.source();
	}

	private void removeEdge(LayoutEdge edge) {
		boolean isRemoved = edge.remove();

		if (!isRemoved) {
			throw new TopLogicException(I18NConstants.EDGE_COULD_NOT_BE_REMOVED);
		}

	}

	private LayoutEdge reverseEdge(LayoutEdge edge) {
		LayoutEdge reversedEdge = edge.reverse();

		if (reversedEdge != null) {
			return reversedEdge;
		} else {
			throw new TopLogicException(I18NConstants.EDGE_COULD_NOT_BE_REVERSED);
		}
	}

	@Override
	public LayoutGraph findMaximalAcyclicSubgraph(LayoutGraph graph) {
		LinkedHashSet<LayoutNode> toVisitedNodes = new LinkedHashSet<>(graph.nodes());
		LinkedHashSet<LayoutEdge> markedEdges = new LinkedHashSet<>();
		LinkedHashSet<LayoutEdge> conflictingEdges = new LinkedHashSet<>();

		conflictingEdges.addAll(getSelfLoops(graph));

		while (!toVisitedNodes.isEmpty()) {
			markSinks(toVisitedNodes, markedEdges);
			markSources(toVisitedNodes, markedEdges);

			conflictingEdges.addAll(calculateConflictingEdges(toVisitedNodes, markedEdges));
		}

		resolveConflictingEdges(conflictingEdges);

		return graph;
	}

	private void resolveConflictingEdges(Collection<LayoutEdge> conflictingEdges) {
		for (LayoutEdge conflictingEdge : conflictingEdges) {
			resolveConflictingEdge(conflictingEdge);
		}
	}
}
