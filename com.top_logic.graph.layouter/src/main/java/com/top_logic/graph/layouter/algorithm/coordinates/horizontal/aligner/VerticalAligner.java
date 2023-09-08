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

import com.top_logic.basic.col.TupleFactory.Pair;
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

	protected void alignNode(LayoutNode node, LayoutNode parent) {
		getAligns().put(parent, node);

		getRoots().put(node, getRoots().get(parent));

		getAligns().put(node, getRoots().get(node));
	}

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

	public Map<LayoutNode, LayoutNode> getRoots() {
		return _roots;
	}

	public void setRoots(Map<LayoutNode, LayoutNode> roots) {
		_roots = roots;
	}

	public Map<LayoutNode, LayoutNode> getAligns() {
		return _aligns;
	}

	public void setAligns(Map<LayoutNode, LayoutNode> aligns) {
		_aligns = aligns;
	}

	public Map<LayoutNode, Integer> getPositions() {
		return _positions;
	}

	public void setPositions(Map<LayoutNode, Integer> positions) {
		_positions = positions;
	}

	public VerticalAlignment getVerticalAlignment() {
		return new VerticalAlignment(_roots, _aligns);
	}

	public LayoutDirection getDirection() {
		return _direction;
	}

}
