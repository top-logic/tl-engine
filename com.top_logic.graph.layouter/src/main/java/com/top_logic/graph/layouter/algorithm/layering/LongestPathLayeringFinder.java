/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.layering;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayer;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * {@link LayeringFinder} which builds the layering using the longest path strategy. It minimzes the
 * height but ignores the width.
 * 
 * If the graph is layered from top (sources) to bottom (sinks) and has many sources then it results
 * in a wide top layer.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class LongestPathLayeringFinder extends LayeringFinder {

	protected Map<LayoutNode, Integer> _nodeLayerMapping = new LinkedHashMap<>();;

	protected Set<LayoutNode> _allAssignedVertices = new LinkedHashSet<>();

	protected Set<LayoutNode> _possibleAssignableVertices = new LinkedHashSet<>();

	protected int _currentLayer;

	/**
	 * Creates a {@link LongestPathLayeringFinder} for the given {@link LayoutGraph}.
	 */
	public LongestPathLayeringFinder(LayoutGraph graph) {
		super(graph);
	}

	/**
	 * Get all possible {@link LayoutNode}s which can be assigned to a layer.
	 */
	protected Set<LayoutNode> getPossibleAssignableVertices(Set<LayoutNode> nodes) {
		return nodes.stream().flatMap(node -> getPossibleAssignableVertices(node).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private Set<LayoutNode> getPossibleAssignableVertices(LayoutNode node) {
		return getNotAssignedSourceNodes(node);
	}

	/**
	 * Creates a segment, a specific {@link LayoutEdge}, see {@link LayoutEdge#isSegment()}, for the
	 * given {@link LayoutNode}s.
	 */
	protected void createSegment(LayoutGraph graph, LayoutNode source, LayoutNode target, LayoutEdge edge) {
		LayoutEdge newEdge = LayoutGraphUtil.createEdge(graph, source, target, edge);

		newEdge.setSegment(true);
	}

	/**
	 * Assigns the given {@link LayoutNode}s.
	 * 
	 * @see #assignNode(LayoutNode, Integer)
	 */
	protected void assignNodes(Set<LayoutNode> nodes, Integer layer) {
		for (LayoutNode node : nodes) {
			assignNode(node, layer);
		}

		_allAssignedVertices.addAll(nodes);
	}

	/**
	 * Assigns the given {@link LayoutNode} to the given {@code layer}.
	 */
	protected void assignNode(LayoutNode node, Integer layer) {
		_nodeLayerMapping.put(node, layer);

		addToLayer(node, layer);
	}

	private void addToLayer(LayoutNode node, Integer layer) {
		UnorderedNodeLayer sameLayeredNodes = _layering.get(layer);

		if (sameLayeredNodes == null) {
			UnorderedNodeLayer newNodesLayer = new UnorderedNodeLayer(Arrays.asList(node));

			_layering.put(layer, newNodesLayer);
		} else {
			sameLayeredNodes.add(node);
		}
	}

	@Override
	public Map<Integer, UnorderedNodeLayer> getLayering() {
		LayoutGraph graph = getGraph();
		LinkedHashSet<LayoutNode> nodes = new LinkedHashSet<>(graph.nodes());

		initializeLayering(nodes);

		while (!_allAssignedVertices.equals(nodes)) {
			_currentLayer++;

			Set<LayoutNode> nextAssignedVertices = new LinkedHashSet<>();

			for (LayoutNode node : _possibleAssignableVertices) {
				if (isAssignable(node)) {
					nextAssignedVertices.add(node);
				}
			}

			assignNodes(nextAssignedVertices, _currentLayer);
			_possibleAssignableVertices.removeAll(nextAssignedVertices);
			_possibleAssignableVertices.addAll(getPossibleAssignableVertices(nextAssignedVertices));
		}

		sparseNormalizeLayering(graph);

		return _layering;
	}

	abstract Set<LayoutNode> getNotAssignedSourceNodes(LayoutNode node);

	abstract void initializeLayering(LinkedHashSet<LayoutNode> nodes);

	abstract void sparseNormalizeLayering(LayoutGraph graph);

	abstract boolean isAssignable(LayoutNode node);
}
