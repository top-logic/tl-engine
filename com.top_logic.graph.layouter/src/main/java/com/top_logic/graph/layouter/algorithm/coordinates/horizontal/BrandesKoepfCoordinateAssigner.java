/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner.VerticalAligner;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner.VerticalAlignment;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner.VerticalLeftDownAligner;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner.VerticalLeftUpAligner;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner.VerticalRightDownAligner;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner.VerticalRightUpAligner;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.layer.SegmentContainer;

/**
 * Horizontal coordinate assigner for a layered graph.
 * 
 * First the graph nodes are partitioned into blocks. Each block spans over possible multiple layers
 * and contains a node on each layer. Nodes in the same block get the same x coordinate.
 * 
 * To build such blocks there exists 4 different strategies to traverse the layered graph. From top
 * to bottom and left to right.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class BrandesKoepfCoordinateAssigner extends HorizontalCoordinatesAssigner {

	enum Strategy {
		LEFTMOST_UPPER, RIGHTMOST_UPPER, LEFTMOST_LOWER, RIGHTMOST_LOWER;
	}

	private Map<LayoutNode, LayoutNode> _sinks = new LinkedHashMap<>();

	private Map<LayoutNode, Double> _shifts = new LinkedHashMap<>();

	private Map<LayoutNode, Set<LayoutNode>> _predecessors = new LinkedHashMap<>();

	private Map<LayoutNode, Set<LayoutNode>> _successors = new LinkedHashMap<>();

	private Map<LayoutNode, Double[]> _xCoordinates = new LinkedHashMap<>();

	private LayoutDirection _direction;

	/**
	 * Creates a Horizontal Coordinate Assigner for the given graph.
	 */
	public BrandesKoepfCoordinateAssigner(LayoutGraph graph, LayoutDirection direction) {
		super(graph);

		_direction = direction;

		initXCoordinates(graph);
	}

	private void initXCoordinates(LayoutGraph graph) {
		graph.nodes().forEach(node -> {
			_xCoordinates.put(node, new Double[Strategy.values().length]);
		});
	}

	@Override
	public void setHorizontalNodeCoordinates(Map<Integer, DefaultAlternatingLayer> ordering, Set<LayoutEdge> crossingEdges) {
		Map<Strategy, Pair<Double, Double>> pairsMinMax = new LinkedHashMap<>();

		Strategy minStrategy = null;
		double minWidth = Double.MAX_VALUE;

		for (Strategy strategy : Strategy.values()) {
			VerticalAlignment alignment = verticalAlign(ordering, crossingEdges, strategy);
			horizontalCompaction(ordering, alignment, strategy);

			Pair<Double, Double> pairMinMax = addResultingXCoordinates(strategy);

			pairsMinMax.put(strategy, pairMinMax);

			double currentWidth = pairMinMax.getSecond() - pairMinMax.getFirst();

			if (currentWidth < minWidth) {
				minWidth = currentWidth;
				minStrategy = strategy;
			}
		}

		shiftAll(pairsMinMax, minStrategy);

		setBalancedXCoordinate();

		shiftZeroOrigin(pairsMinMax.get(minStrategy).getFirst());
	}

	private void shiftZeroOrigin(double minX) {
		for (LayoutNode node : getGraph().nodes()) {
			node.setX(node.getX() - minX);
		}
	}

	private void setBalancedXCoordinate() {
		for (LayoutNode node : getGraph().nodes()) {
			double newX = getBalancedXCoordinate(node);

			node.setX(newX);
		}
	}

	private double getBalancedXCoordinate(LayoutNode node) {
		Double[] xCoordinates = sortXCoordinates(node);

		double newX = (xCoordinates[1] + xCoordinates[2]) / 2.;

		return newX;
	}

	private Double[] sortXCoordinates(LayoutNode node) {
		Double[] xCoordinates = _xCoordinates.get(node);

		Arrays.sort(xCoordinates);

		return xCoordinates;
	}

	private void shiftAll(Map<Strategy, Pair<Double, Double>> pairsMinMax, Strategy minDirection) {
		Pair<Double, Double> pairMinMax = pairsMinMax.get(minDirection);

		shift(Strategy.LEFTMOST_UPPER, pairMinMax.getFirst() - pairsMinMax.get(Strategy.LEFTMOST_UPPER).getFirst());
		shift(Strategy.LEFTMOST_LOWER, pairMinMax.getFirst() - pairsMinMax.get(Strategy.LEFTMOST_LOWER).getFirst());
		shift(Strategy.RIGHTMOST_UPPER, pairMinMax.getSecond() - pairsMinMax.get(Strategy.RIGHTMOST_UPPER).getSecond());
		shift(Strategy.RIGHTMOST_LOWER, pairMinMax.getSecond() - pairsMinMax.get(Strategy.RIGHTMOST_LOWER).getSecond());
	}

	private void shift(Strategy strategy, double value) {
		if (value != 0) {
			for (LayoutNode node : getGraph().nodes()) {
				Double[] xCoordinates = _xCoordinates.get(node);

				xCoordinates[strategy.ordinal()] += value;
			}
		}
	}

	private Pair<Double, Double> addResultingXCoordinates(Strategy strategy) {
		double min = Double.MAX_VALUE;
		double max = Integer.MIN_VALUE;

		for (LayoutNode node : getGraph().nodes()) {
			Double[] xCoordinates = _xCoordinates.get(node);
			double x = node.getX();

			if (x > max) {
				max = x;
			}

			if (x < min) {
				min = x;
			}

			xCoordinates[strategy.ordinal()] = x;
		}

		return new Pair<>(min, max);
	}

	private VerticalAlignment verticalAlign(Map<Integer, DefaultAlternatingLayer> ordering, Set<LayoutEdge> edges,
			Strategy strategy) {
		Set<LayoutEdge> markedEdges = new LinkedHashSet<>(edges);
		VerticalAligner aligner = getVerticalAligner(strategy);

		aligner.verticalAlign(ordering, markedEdges);

		return aligner.getVerticalAlignment();
	}

	private VerticalAligner getVerticalAligner(Strategy strategy) {
		switch (strategy) {
			case LEFTMOST_UPPER:
				return new VerticalLeftDownAligner(getGraph(), getDirection());

			case RIGHTMOST_UPPER:
				return new VerticalRightDownAligner(getGraph(), getDirection());

			case LEFTMOST_LOWER:
				return new VerticalLeftUpAligner(getGraph(), getDirection());

			case RIGHTMOST_LOWER:
				return new VerticalRightUpAligner(getGraph(), getDirection());

			default:
				return null;
		}
	}

	private boolean isRightmost(Strategy strategy) {
		return Strategy.RIGHTMOST_LOWER == strategy || Strategy.RIGHTMOST_UPPER == strategy;
	}

	private boolean isLeftmost(Strategy strategy) {
		return !isRightmost(strategy);
	}

	private void horizontalCompaction(Map<Integer, DefaultAlternatingLayer> ordering, VerticalAlignment alignment,
			Strategy strategy) {
		initPredecessors(ordering);
		initSuccessors();

		initSinks();
		initShifts();
		initX();

		setRelativeCoordinates(alignment, strategy);
		setAbsoluteCoordinates(alignment, strategy);
	}

	private void initSuccessors() {
		for (LayoutNode successor : _predecessors.keySet()) {
			Set<LayoutNode> predecessors = _predecessors.get(successor);
			
			for(LayoutNode predecessor : predecessors) {
				initSuccessorNode(predecessor, successor);
			}
		}
	}

	private void initPredecessors(Map<Integer, DefaultAlternatingLayer> ordering) {
		for (int layer : ordering.keySet()) {
			DefaultAlternatingLayer defaultAlternatingLayer = ordering.get(layer);

			List<LayoutNode> nodes = getLayerSourceItems(defaultAlternatingLayer);

			initPredecessors(nodes);
		}
	}

	private List<LayoutNode> getLayerSourceItems(DefaultAlternatingLayer layer) {
		List<LayoutNode> nodes = new LinkedList<>(layer.getFirstSegmentContainer().getSegmentSources());

		Iterator<Pair<LayoutNode, SegmentContainer>> iterator = getLayerIterator(layer);

		while (iterator.hasNext()) {
			Pair<LayoutNode, SegmentContainer> pair = iterator.next();

			nodes.add(pair.getFirst());
			nodes.addAll(pair.getSecond().getSegmentSources());
		}

		return nodes;
	}

	private Iterator<Pair<LayoutNode, SegmentContainer>> getLayerIterator(DefaultAlternatingLayer layer) {
		List<Pair<LayoutNode, SegmentContainer>> pairs = layer.getAll();

		Iterator<Pair<LayoutNode, SegmentContainer>> iterator = pairs.iterator();

		return iterator;
	}

	private void initPredecessors(List<LayoutNode> nodes) {
		for (int i = 1; i < nodes.size(); i++) {
			initPredecessorNode(nodes.get(i), nodes.get(i - 1));
		}
	}

	private void initPredecessorNode(LayoutNode current, LayoutNode predecessor) {
		Set<LayoutNode> predecessors = _predecessors.get(current);

		if (predecessors == null) {
			Set<LayoutNode> newPredecessors = new LinkedHashSet<>(Arrays.asList(predecessor));

			_predecessors.put(current, newPredecessors);
		} else {
			predecessors.add(predecessor);
		}
	}

	private void initSuccessorNode(LayoutNode current, LayoutNode successor) {
		Set<LayoutNode> successors = _successors.get(current);

		if (successors == null) {
			Set<LayoutNode> newSuccessors = new LinkedHashSet<>(Arrays.asList(successor));

			_successors.put(current, newSuccessors);
		} else {
			successors.add(successor);
		}
	}

	private void setAbsoluteCoordinates(VerticalAlignment alignment, Strategy strategy) {
		for (LayoutNode currentNode : getGraph().nodes()) {
			LayoutNode root = alignment.getRoots().get(currentNode);

			setX(currentNode, root.getX());
		}
		
		shitNodes(alignment, strategy);
	}

	private void shitNodes(VerticalAlignment alignment, Strategy strategy) {
		for (LayoutNode currentNode : getGraph().nodes()) {
			LayoutNode root = alignment.getRoots().get(currentNode);

			double shift = getNodeShift(root);

			shiftNode(strategy, currentNode, shift);
		}
	}

	private void shiftNode(Strategy strategy, LayoutNode node, double shift) {
		if (shift < Double.MAX_VALUE) {
			if (isLeftmost(strategy)) {
				node.setX(node.getX() + shift);
			} else {
				node.setX(node.getX() - shift);
			}
		}
	}

	private void setX(LayoutNode node, double x) {
		node.setX(x);
	}

	private double getNodeShift(LayoutNode root) {
		return _shifts.get(_sinks.get(root));
	}

	private void setRelativeCoordinates(VerticalAlignment alignment, Strategy strategy) {
		for (LayoutNode node : getGraph().nodes()) {
			if (alignment.getRoots().get(node) == node) {
				placeBlock(node, alignment, strategy);
			}
		}
	}

	private void placeBlock(LayoutNode root, VerticalAlignment alignment, Strategy strategy) {
		if (root.getX() == HorizontalCoordinatesConstants.X_UNDEFINED) {
			root.setX(HorizontalCoordinatesConstants.START_X_COORDINATE);

			LayoutNode currentNode = root;

			do {
				placeNode(root, currentNode, alignment, strategy);

				currentNode = alignment.getAligns().get(currentNode);
			} while (currentNode != root);
		}
	}

	private void placeNode(LayoutNode root, LayoutNode currentNode, VerticalAlignment alignment, Strategy strategy) {
		Set<LayoutNode> neighbors = getNeighbors(currentNode, strategy);

		if (neighbors != null) {
			for (LayoutNode neighbor : neighbors) {
				LayoutNode neighborRootNode = alignment.getRoots().get(neighbor);

				placeBlock(neighborRootNode, alignment, strategy);

				LayoutNode neighborSink = _sinks.get(neighborRootNode);

				if (_sinks.get(root) == root) {
					_sinks.put(root, neighborSink);
				}

				if (_sinks.get(root) != neighborSink) {
					shiftNeighborBlock(root, strategy, neighborRootNode, neighbor, currentNode, neighborSink);
				} else {
					setNodeCoordinate(root, strategy, neighborRootNode, neighbor, currentNode);
				}
			}
		}
	}

	private void shiftNeighborBlock(LayoutNode root, Strategy strategy, LayoutNode neighborRoot,
			LayoutNode neighbor, LayoutNode currentNode, LayoutNode neighborSink) {
		double minShift = getMinShift(root, strategy, neighborRoot, neighbor, currentNode, neighborSink);

		_shifts.put(neighborSink, minShift);
	}

	private void setNodeCoordinate(LayoutNode root, Strategy strategy, LayoutNode neighborRootNode,
			LayoutNode neighbor, LayoutNode currentNode) {
		if (isLeftmost(strategy)) {
			root.setX(Math.max(root.getX(),
				neighborRootNode.getX() + neighbor.getWidth() + HorizontalCoordinatesConstants.MIN_DISTANCE));
		} else {
			root.setX(Math.min(root.getX(),
				neighborRootNode.getX() - currentNode.getWidth() - HorizontalCoordinatesConstants.MIN_DISTANCE));
		}
	}

	private Set<LayoutNode> getNeighbors(LayoutNode currentNode, Strategy strategy) {
		Set<LayoutNode> neighbors;

		if (isLeftmost(strategy)) {
			neighbors = _predecessors.get(currentNode);
		} else {
			neighbors = _successors.get(currentNode);
		}

		return neighbors;
	}

	private double getMinShift(LayoutNode root, Strategy strategy, LayoutNode neighborRootNode,
			LayoutNode neighbor, LayoutNode currentNode, LayoutNode neighborSink) {
		double minShift = _shifts.get(neighborSink);

		if (isLeftmost(strategy)) {
			minShift = Math.min(minShift, root.getX() - neighborRootNode.getX() - neighbor.getWidth()
				- HorizontalCoordinatesConstants.MIN_DISTANCE);
		} else {
			minShift = Math.min(minShift, neighborRootNode.getX() - root.getX() - currentNode.getWidth()
				- HorizontalCoordinatesConstants.MIN_DISTANCE);
		}

		return minShift;
	}

	private void initX() {
		for (LayoutNode node : getGraph().nodes()) {
			node.setX(HorizontalCoordinatesConstants.X_UNDEFINED);
		}
	}

	private void initShifts() {
		for (LayoutNode node : getGraph().nodes()) {
			_shifts.put(node, Double.MAX_VALUE);
		}
	}

	private void initSinks() {
		for (LayoutNode node : getGraph().nodes()) {
			_sinks.put(node, node);
		}
	}

	/**
	 * {@link LayoutDirection} of the graph.
	 */
	public LayoutDirection getDirection() {
		return _direction;
	}

}
