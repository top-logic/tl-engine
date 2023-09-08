/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.edge.routing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1D;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;
import com.top_logic.graph.layouter.algorithm.rendering.lines.group.LineEndGrouper;
import com.top_logic.graph.layouter.algorithm.rendering.lines.partition.ContainerLayerPartition;
import com.top_logic.graph.layouter.algorithm.rendering.lines.partition.NoLineIntersectionLayerPartitioner;
import com.top_logic.graph.layouter.algorithm.rendering.lines.util.DefaultLineContainerPrioritizer;
import com.top_logic.graph.layouter.algorithm.rendering.lines.util.LineContainerPriorityComparator;
import com.top_logic.graph.layouter.algorithm.rendering.lines.util.LineEndTopBottomComparator;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.Waypoint;
import com.top_logic.graph.layouter.model.layer.NodeLayer;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayer;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayering;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Routes the {@link LayoutEdge}s using orthogonal edge parts.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class OrthogonalEdgeRouter implements EdgeRoutingAlgorithm {

	private double _currentY;

	private LayoutDirection _direction;

	/**
	 * Creates a {@link OrthogonalEdgeRouter}.
	 */
	public OrthogonalEdgeRouter(LayoutDirection direction) {
		_direction = direction;
		_currentY = 0;
	}

	@Override
	public void route(LayoutGraph graph) {
		UnorderedNodeLayering layering = LayoutGraphUtil.getLayoutNodeLayering(graph);

		for (int i = 1; i < layering.size(); i++) {
			NodeLayer lowerNodes = layering.getLayer(i);

			setStartLayerYCoordinate(layering.getLayer(i - 1));

			Collection<Line1D> horizontalLineParts = getHorizontalLineParts(lowerNodes);

			List<Line1D> sortedHorizontalLineParts = getSortedHorizontalLines(horizontalLineParts);

			List<Line1DContainer> groupedLines = getGroupedLinesByEnd(sortedHorizontalLineParts);

			List<Line1DContainer> sortedLineContainers = getSortedLineContainers(groupedLines);

			ContainerLayerPartition partition = NoLineIntersectionLayerPartitioner.INSTANCE.partition(sortedLineContainers);

			rearrangeVerticalNodeCoordinate(lowerNodes, partition.size());

			setWaypoints(partition);
		}
	}

	private List<Line1DContainer> getGroupedLinesByEnd(List<Line1D> sortedHorizontalLineParts) {
		return (List<Line1DContainer>) LineEndGrouper.INSTANCE.group(sortedHorizontalLineParts);
	}

	private List<Line1D> getSortedHorizontalLines(Collection<Line1D> horizontalLineParts) {
		List<Line1D> sortedLineParts = new LinkedList<>(horizontalLineParts);

		if (getDirection() == LayoutDirection.VERTICAL_FROM_SOURCE) {
			Collections.sort(sortedLineParts, new LineEndTopBottomComparator());
		} else {
			Collections.sort(sortedLineParts, getLineEndBottomTopComparator());
		}

		return sortedLineParts;
	}

	private Comparator<Line1D> getLineEndBottomTopComparator() {
		return Collections.reverseOrder(new LineEndTopBottomComparator());
	}

	private void setStartLayerYCoordinate(UnorderedNodeLayer upperLayer) {
		Collection<LayoutNode> upperNodes = upperLayer.getAll();

		_currentY += LayoutGraphUtil.findMaxNodeHeight(upperNodes);
	}

	private LinkedList<Line1DContainer> getSortedLineContainers(Collection<Line1DContainer> groupedLines) {
		new DefaultLineContainerPrioritizer(getDirection()).prioritize(groupedLines);

		LinkedList<Line1DContainer> orderedGroupedLines = new LinkedList<>(groupedLines);

		if (getDirection() == LayoutDirection.VERTICAL_FROM_SOURCE) {
			Collections.sort(orderedGroupedLines, Collections.reverseOrder(new LineContainerPriorityComparator()));
		} else {
			Collections.sort(orderedGroupedLines, new LineContainerPriorityComparator());
		}

		return orderedGroupedLines;
	}

	private void rearrangeVerticalNodeCoordinate(NodeLayer nodes, int partitionSize) {
		double nextLayerY = getNextLayerYCoordinate(partitionSize);

		nodes.getAll().stream().forEach(node -> node.setY(nextLayerY));
	}

	private double getNextLayerYCoordinate(int partitionSize) {
		double y = _currentY;

		y += EdgeRoutingConstants.LAYER_OFFSET_TOP;
		y += partitionSize * EdgeRoutingConstants.MIN_HORIZONTAL_LINE_DISTANCE;
		y += EdgeRoutingConstants.LAYER_OFFSET_BOTTOM;

		return y;
	}

	private void setWaypoints(ContainerLayerPartition partition) {
		_currentY += EdgeRoutingConstants.LAYER_OFFSET_TOP;

		for (Collection<Line1DContainer> containers : partition.getPartition()) {
			_currentY += EdgeRoutingConstants.MIN_HORIZONTAL_LINE_DISTANCE / 2;

			setWaypoints(containers);

			_currentY += EdgeRoutingConstants.MIN_HORIZONTAL_LINE_DISTANCE / 2;
		}

		_currentY += EdgeRoutingConstants.LAYER_OFFSET_BOTTOM;
	}

	private void setWaypoints(Collection<Line1DContainer> containers) {
		containers.stream().flatMap(container -> container.getLines().stream()).forEach(line -> {
			LayoutEdge edge = (LayoutEdge) line.getBusinessObject();

			double sourceX = edge.getSourceNodePort().get().getX();
			double targetX = edge.getTargetNodePort().get().getX();

			Waypoint source = new Waypoint(sourceX, getSourceY(edge.source()));
			Waypoint sourceMid = new Waypoint(sourceX, _currentY);
			Waypoint targetMid = new Waypoint(targetX, _currentY);
			Waypoint target = new Waypoint(targetX, getTargetY(edge.target()));

			edge.setWaypoints(Arrays.asList(source, sourceMid, targetMid, target));
		});
	}

	private double getSourceY(LayoutNode source) {
		double y = source.getY();

		if (getDirection() == LayoutDirection.VERTICAL_FROM_SOURCE) {
			y += source.getHeight();
		}

		return y;
	}

	private double getTargetY(LayoutNode target) {
		double y = target.getY();

		if (getDirection() == LayoutDirection.VERTICAL_FROM_SINK) {
			y += target.getHeight();
		}

		return y;
	}

	private Collection<Line1D> getHorizontalLineParts(NodeLayer lowerNodes) {
		return lowerNodes.getAll().stream().flatMap(node -> LayoutGraphUtil.getTopEdges(getDirection(), node).stream())
			.filter(edge -> !edge.isSegment())
			.map(edge -> {
				return getHorizontalPart(edge);
			})
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private Line1D getHorizontalPart(LayoutEdge edge) {
		double sourcePortX = edge.getSourceNodePort().get().getX();
		double targetPortX = edge.getTargetNodePort().get().getX();

		return new Line1D(sourcePortX, targetPortX, edge);
	}

	/**
	 * Layout direction for the given {@link LayoutGraph}.
	 */
	public LayoutDirection getDirection() {
		return _direction;
	}

	/**
	 * @see #getDirection()
	 */
	public void setDirection(LayoutDirection direction) {
		_direction = direction;
	}

}
