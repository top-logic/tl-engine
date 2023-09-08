/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.crossing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.comparator.LayoutNodeMeasureComparator;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.layer.SegmentContainer;
import com.top_logic.graph.layouter.model.layer.VirtualSegmentEdge;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Crossing minimizer for a layered {@link LayoutGraph}.
 * 
 * @see LayerCrossingReductionAlgorithm
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class LayerCrossingMinimizer implements LayerCrossingReductionAlgorithm {

	private Set<LayoutEdge> _crossingTyp1Edges = new LinkedHashSet<>();

	private int _crossingNumber;

	private LayoutDirection _direction;

	/**
	 * Creates a {@link LayerCrossingMinimizer} for the given {@link LayoutDirection}.
	 */
	public LayerCrossingMinimizer(LayoutDirection direction) {
		_direction = direction;
	}

	@Override
	public DefaultAlternatingLayer getMinCrossingLayer(DefaultAlternatingLayer fixedLayer, List<LayoutNode> freeLayer) {
		removeSegmentSources(fixedLayer);
		Map<Object, Integer> positions = createLayerItemPositions(fixedLayer);

		List<LayoutNode> nextLayerNodes = getNonTargetDummyNodes(freeLayer);
		Map<LayoutNode, Double> nodeMeasures = createNodesMeasure(nextLayerNodes, positions);

		List<SegmentContainer> newContainers = removeEmptyContainers(fixedLayer.getContainers());
		Map<SegmentContainer, Double> containerMeasures = createContainerMeasure(newContainers, positions);

		orderNodes(nextLayerNodes, nodeMeasures);
		List<Object> layerOrdering =
			createLayerOrdering(nextLayerNodes, newContainers, nodeMeasures, containerMeasures);
		Map<LayoutNode, SegmentContainer> sourceContainers = addSegmentTargets(layerOrdering, freeLayer);

		DefaultAlternatingLayer alternatingLayer = getAlternatingLayer(layerOrdering);

		initCrossings(positions, layerOrdering, sourceContainers);

		return alternatingLayer;
	}

	private void initCrossings(Map<Object, Integer> positions, List<Object> ordering,
			Map<LayoutNode, SegmentContainer> sources) {
		List<Object> edges = getEdges(ordering, sources);
		sortEdges(edges, positions, ordering);

		Set<CrossingEdge> crossings = getCrossings(edges, ordering, positions);

		setCrossingNumber(getCrossingNumber(crossings));
		setCrossingTyp1Edges(getCrossingType1Edges(crossings));
	}

	private Map<Object, Integer> createLayerItemPositions(DefaultAlternatingLayer firstLayer) {
		Iterator<Pair<LayoutNode, SegmentContainer>> iterator = firstLayer.getAll().iterator();
		SegmentContainer firstContainer = firstLayer.getFirstSegmentContainer();
		Map<Object, Integer> positions = new LinkedHashMap<>();

		int currentPosition = 0;
		int size = firstContainer.size();

		if (size > 0) {
			positions.put(firstContainer, currentPosition);
			currentPosition += size;
		}

		while (iterator.hasNext()) {
			Pair<LayoutNode, SegmentContainer> item = iterator.next();
			LayoutNode node = item.getFirst();
			SegmentContainer container = item.getSecond();

			positions.put(node, currentPosition);
			currentPosition++;

			if (container.size() > 0) {
				positions.put(container, currentPosition);
				currentPosition += container.size();
			}
		}

		return positions;
	}

	private void orderNodes(List<LayoutNode> nodes, Map<LayoutNode, Double> measure) {
		Collections.sort(nodes, new LayoutNodeMeasureComparator(measure));
	}

	private List<Object> createLayerOrdering(List<LayoutNode> nodes, List<SegmentContainer> containers,
			Map<LayoutNode, Double> nodeMeasures, Map<SegmentContainer, Double> containerMeasures) {
		List<Object> ordering = new ArrayList<>();

		Stack<LayoutNode> nodeStack = getStack(nodes);
		Stack<SegmentContainer> containerStack = getStack(containers);

		join(nodeMeasures, containerMeasures, ordering, nodeStack, containerStack);

		addRemainingItems(ordering, nodeStack, containerStack);

		return ordering;
	}

	private void join(Map<LayoutNode, Double> nodeMeasures, Map<SegmentContainer, Double> containerMeasures,
			List<Object> ordering, Stack<LayoutNode> nodeStack, Stack<SegmentContainer> containerStack) {
		while (!nodeStack.isEmpty() && !containerStack.isEmpty()) {
			LayoutNode node = nodeStack.peek();
			SegmentContainer container = containerStack.peek();

			double nodeMeasure = nodeMeasures.get(node);
			double containerMeasure = containerMeasures.get(container);

			if (nodeMeasure <= containerMeasure) {
				addNode(ordering, nodeStack, node);
			} else if (nodeMeasure >= containerMeasure + container.size() - 1) {
				addContainer(ordering, containerStack, container);
			} else {
				int k = (int) Math.ceil(nodeMeasure - containerMeasure);
				Pair<SegmentContainer, SegmentContainer> containerPair = container.splitAt(k);

				addContainer(ordering, containerStack, containerPair.getFirst());
				addNode(ordering, nodeStack, node);

				containerStack.push(containerPair.getSecond());
				containerMeasures.put(containerPair.getSecond(), nodeMeasure + 1);
			}
		}
	}

	private void addNode(List<Object> ordering, Stack<LayoutNode> nodes, LayoutNode node) {
		nodes.pop();

		ordering.add(node);
	}

	private void addContainer(List<Object> ordering, Stack<SegmentContainer> containers, SegmentContainer container) {
		containers.pop();

		addContainer(ordering, container);
	}

	private void addContainer(List<Object> ordering, SegmentContainer container) {
		ordering.add(container);
	}

	private void addRemainingItems(List<Object> ordering, Stack<LayoutNode> nodes, Stack<SegmentContainer> containers) {
		if (nodes.isEmpty()) {
			addAllFromStack(ordering, containers);
		}

		if (containers.isEmpty()) {
			addAllFromStack(ordering, nodes);
		}
	}

	private <T> Stack<T> getStack(List<T> items) {
		Stack<T> stack = new Stack<>();

		addAllToStack(items, stack);

		return stack;
	}

	private <T> void addAllToStack(List<T> items, Stack<T> stack) {
		Collections.reverse(items);
		Iterator<T> iterator = items.iterator();

		while (iterator.hasNext()) {
			stack.push(iterator.next());
		}
	}

	private void addAllFromStack(List<Object> layerOrdering, Stack<?> stack) {
		while (!stack.isEmpty()) {
			layerOrdering.add(stack.pop());
		}
	}

	private DefaultAlternatingLayer getAlternatingLayer(List<Object> nextLayerOrdering) {
		DefaultAlternatingLayer alternatingLayer = new DefaultAlternatingLayer();

		LayoutNode node = null;
		SegmentContainer container = new SegmentContainer();

		for (int i = 0; i < nextLayerOrdering.size(); i++) {
			Object item = nextLayerOrdering.get(i);

			if (item instanceof LayoutNode) {
				addNodeToLayer(alternatingLayer, node, container);

				node = (LayoutNode) item;
				container = new SegmentContainer();
			} else if (item instanceof SegmentContainer) {
				container.join((SegmentContainer) item);
			}
		}

		alternatingLayer.add(new Pair<>(node, container));

		return alternatingLayer;
	}

	private void addNodeToLayer(DefaultAlternatingLayer alternatingLayer, LayoutNode node, SegmentContainer container) {
		if (node == null) {
			alternatingLayer.getFirstSegmentContainer().join(container);
		} else {
			alternatingLayer.add(new Pair<>(node, container));
		}
	}

	private List<SegmentContainer> removeEmptyContainers(List<SegmentContainer> containers) {
		List<SegmentContainer> nonEmptyContainers = new LinkedList<>();

		Iterator<SegmentContainer> iterator = containers.iterator();
		while (iterator.hasNext()) {
			SegmentContainer container = iterator.next();

			if (!container.isEmpty()) {
				nonEmptyContainers.add(container);
			}
		}

		return nonEmptyContainers;
	}


	protected SegmentContainer splitContainer(List<Object> layerOrdering, LayoutNode node, LayoutEdge segment) {
		ListIterator<Object> iterator = layerOrdering.listIterator();

		while (iterator.hasNext()) {
			Object item = iterator.next();

			if (item instanceof SegmentContainer) {
				SegmentContainer container = (SegmentContainer) item;

				if (container.contains(segment)) {
					return splitContainer(node, segment, iterator, container);
				}
			}
		}

		return null;
	}

	private SegmentContainer splitContainer(LayoutNode node, LayoutEdge segment, ListIterator<Object> iterator,
			SegmentContainer container) {
		Pair<SegmentContainer, SegmentContainer> pair = container.splitAt(segment);

		SegmentContainer firstContainer = pair.getFirst();
		SegmentContainer secondContainer = pair.getSecond();

		splitContainer(node, iterator, firstContainer, secondContainer);

		return getSplittedRootContainer(container);
	}

	private void splitContainer(LayoutNode node, ListIterator<Object> iterator, SegmentContainer first,
			SegmentContainer second) {
		iterator.remove();

		if (!first.isEmpty()) {
			iterator.add(first);
		}

		iterator.add(node);

		if (!second.isEmpty()) {
			iterator.add(second);
		}
	}

	private SegmentContainer getSplittedRootContainer(SegmentContainer container) {
		SegmentContainer rootContainer = container.getRoot();

		if (rootContainer != null) {
			return rootContainer;
		} else {
			return container;
		}
	}

	private Map<SegmentContainer, Double> createContainerMeasure(List<SegmentContainer> container,
			Map<Object, Integer> positions) {
		Map<SegmentContainer, Double> containerMeasure = new LinkedHashMap<>();

		for (int i = 0; i < container.size(); i++) {
			containerMeasure.put(container.get(i), (double) positions.get(container.get(i)));
		}

		return containerMeasure;
	}

	protected Object getEdgeBottomNode(Object secondEdge) {
		if (secondEdge instanceof LayoutEdge) {
			return LayoutGraphUtil.getBottomNode(getDirection(), ((LayoutEdge) secondEdge));
		} else if (secondEdge instanceof VirtualSegmentEdge) {
			return LayoutGraphUtil.getBottomNode(getDirection(), ((VirtualSegmentEdge) secondEdge));
		} else {
			return null;
		}
	}

	protected List<Object> getEdges(List<Object> ordering, Map<LayoutNode, SegmentContainer> sources) {
		List<Object> edges = new LinkedList<>();
		Iterator<Object> iterator = ordering.iterator();

		while (iterator.hasNext()) {
			Object item = iterator.next();

			if (item instanceof LayoutNode) {
				getEdges(sources, edges, (LayoutNode) item);
			} else if (item instanceof SegmentContainer) {
				getEdges(edges, (SegmentContainer) item);
			}
		}

		return edges;
	}

	private Set<LayoutEdge> getCrossingType1Edges(Set<CrossingEdge> crossings) {
		Set<LayoutEdge> crossingType1Edges = new LinkedHashSet<>();
		Iterator<CrossingEdge> iterator = crossings.iterator();

		while (iterator.hasNext()) {
			LayoutEdge type1ConflictEdge = getType1ConflictEdge(iterator.next());

			if (type1ConflictEdge != null) {
				crossingType1Edges.add(type1ConflictEdge);
			}
		}

		return crossingType1Edges;
	}

	private LayoutEdge getType1ConflictEdge(CrossingEdge crossingEdge) {
		return getType1ConflictEdge(crossingEdge.getFirstEdge(), crossingEdge.getSecondEdge());
	}

	private LayoutEdge getType1ConflictEdge(Object firstEdge, Object secondEdge) {
		if (firstEdge instanceof LayoutEdge && secondEdge instanceof VirtualSegmentEdge) {
			return (LayoutEdge) firstEdge;
		} else if (firstEdge instanceof VirtualSegmentEdge && secondEdge instanceof LayoutEdge) {
			return (LayoutEdge) secondEdge;
		} else {
			return null;
		}
	}

	private int getCrossingNumber(Set<CrossingEdge> crossings) {
		int crossingNumber = 0;
		Iterator<CrossingEdge> iterator = crossings.iterator();

		while (iterator.hasNext()) {
			crossingNumber += iterator.next().getWeight();
		}

		return crossingNumber;
	}

	protected Map<LayoutNode, Double> createNodesMeasure(List<LayoutNode> nodes, Map<Object, Integer> positions) {
		Map<LayoutNode, Double> nodeMeasure = new LinkedHashMap<>();

		for (LayoutNode node : nodes) {
			double measure = barycenterHeuristicMeasure(node, positions);

			nodeMeasure.put(node, measure);
		}

		return nodeMeasure;
	}

	protected void joinContainer(SegmentContainer first, List<LayoutEdge> segments, SegmentContainer second) {
		first.addAll(segments);

		first.join(second);
	}

	public Set<LayoutEdge> getCrossingTyp1Edges() {
		return _crossingTyp1Edges;
	}

	public void setCrossingTyp1Edges(Set<LayoutEdge> crossingTyp1Edges) {
		_crossingTyp1Edges = crossingTyp1Edges;
	}

	public int getCrossingNumber() {
		return _crossingNumber;
	}

	public void setCrossingNumber(int crossingNumber) {
		_crossingNumber = crossingNumber;
	}

	abstract void removeSegmentSources(DefaultAlternatingLayer fixedLayer);

	abstract List<LayoutNode> getNonTargetDummyNodes(List<LayoutNode> freeLayer);

	abstract double barycenterHeuristicMeasure(LayoutNode node, Map<Object, Integer> positions);

	abstract Map<LayoutNode, SegmentContainer> addSegmentTargets(List<Object> ordering, List<LayoutNode> freeLayer);

	abstract Set<CrossingEdge> getCrossings(List<Object> edges, List<Object> ordering, Map<Object, Integer> positions);

	abstract void sortEdges(List<Object> edges, Map<Object, Integer> positions, List<Object> ordering);

	abstract void getEdges(Map<LayoutNode, SegmentContainer> sources, List<Object> edges, LayoutNode node);

	abstract void getEdges(List<Object> edges, SegmentContainer container);

	public LayoutDirection getDirection() {
		return _direction;
	}
}
