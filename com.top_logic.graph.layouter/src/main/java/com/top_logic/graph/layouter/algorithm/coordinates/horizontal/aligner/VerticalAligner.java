/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.graph.layouter.model.util.Pair;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.algorithm.GraphLayoutAlgorithm;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.layer.SegmentContainer;

/**
 * General vertical aligner for a ordered layered graph.
 * 
 * @see VerticalAlignAlgorithm
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class VerticalAligner extends GraphLayoutAlgorithm implements VerticalAlignAlgorithm {

	private static final int START_POSITION_X = 1;

	private Map<LayoutNode, LayoutNode> _roots = new LinkedHashMap<>();

	private Map<LayoutNode, LayoutNode> _aligns = new LinkedHashMap<>();

	private Map<LayoutNode, Integer> _positions = new LinkedHashMap<>();

	private LayoutDirection _direction;

	/**
	 * Creates a {@link VerticalAligner}.
	 */
	public VerticalAligner(LayoutGraph graph, LayoutDirection direction) {
		super(graph);

		_direction = direction;

		initAligns(getGraph());
		initRoots(getGraph());
	}

	private void initAligns(LayoutGraph graph) {
		for (LayoutNode node : graph.nodes()) {
			getAligns().put(node, node);
		}
	}

	private void initRoots(LayoutGraph graph) {
		for (LayoutNode node : graph.nodes()) {
			getRoots().put(node, node);
		}
	}

	/**
	 * Initializes the {@link #getPositions() positions} of all nodes from their order in the given
	 * layering, reserving a position for each segment between the nodes.
	 *
	 * @param ordering
	 *         Ordered layering of the graph, indexed by layer number starting with 1.
	 */
	protected void initPositions(Map<Integer, DefaultAlternatingLayer> ordering) {
		for (int i = 1; i <= ordering.size(); i++) {
			DefaultAlternatingLayer defaultAlternatingLayer = ordering.get(i);

			int position = START_POSITION_X + defaultAlternatingLayer.getFirstSegmentContainer().size();

			Iterator<Pair<LayoutNode, SegmentContainer>> iterator = getLayerIterator(defaultAlternatingLayer);

			while (iterator.hasNext()) {
				Pair<LayoutNode, SegmentContainer> pair = iterator.next();

				setNodePosition(position, pair.getFirst());

				position += 1 + pair.getSecond().size();
			}
		}
	}

	private Iterator<Pair<LayoutNode, SegmentContainer>> getLayerIterator(DefaultAlternatingLayer layer) {
		List<Pair<LayoutNode, SegmentContainer>> items = layer.getAll();

		Iterator<Pair<LayoutNode, SegmentContainer>> iterator = items.iterator();

		return iterator;
	}

	private void setNodePosition(int position, LayoutNode node) {
		getPositions().put(node, position);
	}

	/**
	 * Appends the given node to the vertical block of the given parent node.
	 *
	 * @param node
	 *         The node to align.
	 * @param parent
	 *         The node in the neighbor layer the given node is aligned with.
	 */
	protected void alignNode(LayoutNode node, LayoutNode parent) {
		getAligns().put(parent, node);

		getRoots().put(node, getRoots().get(parent));

		getAligns().put(node, getRoots().get(node));
	}

	/**
	 * The indices of the (lower and upper) median of a list with the size of the given list.
	 *
	 * @param nodes
	 *         The list to compute the median indices for.
	 * @param reverseOrder
	 *         Whether to return the upper median index first.
	 * @return The two median indices, which are equal for a list of odd size.
	 */
	protected List<Integer> getMedianIndices(List<LayoutNode> nodes, boolean reverseOrder) {
		int size = nodes.size();

		int leftMedianIndex = (int) Math.floor((size + 1) / 2.) - 1;
		int rightMedianIndex = (int) Math.ceil((size + 1) / 2.) - 1;

		if (reverseOrder) {
			return Arrays.asList(rightMedianIndex, leftMedianIndex);
		} else {
			return Arrays.asList(leftMedianIndex, rightMedianIndex);
		}
	}

	/**
	 * The root node of the vertical block of each node.
	 */
	public Map<LayoutNode, LayoutNode> getRoots() {
		return _roots;
	}

	/**
	 * Setter for {@link #getRoots()}.
	 */
	public void setRoots(Map<LayoutNode, LayoutNode> roots) {
		_roots = roots;
	}

	/**
	 * The next node in the vertical block of each node; the last node of a block is mapped to
	 * the root of the block.
	 */
	public Map<LayoutNode, LayoutNode> getAligns() {
		return _aligns;
	}

	/**
	 * Setter for {@link #getAligns()}.
	 */
	public void setAligns(Map<LayoutNode, LayoutNode> aligns) {
		_aligns = aligns;
	}

	/**
	 * The position of each node within its layer.
	 */
	public Map<LayoutNode, Integer> getPositions() {
		return _positions;
	}

	/**
	 * Setter for {@link #getPositions()}.
	 */
	public void setPositions(Map<LayoutNode, Integer> positions) {
		_positions = positions;
	}

	/**
	 * The computed vertical alignment built from {@link #getRoots()} and {@link #getAligns()}.
	 */
	public VerticalAlignment getVerticalAlignment() {
		return new VerticalAlignment(_roots, _aligns);
	}

	/**
	 * The direction of the graph layout.
	 */
	public LayoutDirection getDirection() {
		return _direction;
	}

}
