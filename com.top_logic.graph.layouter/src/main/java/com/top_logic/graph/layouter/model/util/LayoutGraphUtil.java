/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.util;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.graph.layouter.GraphConstants;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner.VerticalAlignment;
import com.top_logic.graph.layouter.algorithm.node.size.DefaultNodeSizer;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1D;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;
import com.top_logic.graph.layouter.math.util.MathUtil;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.comparator.LayoutNodeHeightComparator;
import com.top_logic.graph.layouter.model.filter.FilterMarkedEdge;
import com.top_logic.graph.layouter.model.layer.NodeLayer;
import com.top_logic.graph.layouter.model.layer.NodeLayering;
import com.top_logic.graph.layouter.model.layer.SegmentContainer;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayer;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayering;
import com.top_logic.graph.layouter.model.layer.VirtualSegmentEdge;
import com.top_logic.graph.layouter.text.util.DiagramTextRenderingUtil;
import com.top_logic.layout.LabelProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Util methods to get informations about the graph structure.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutGraphUtil implements LayoutGraphUtilConstants {

	private static final String ENUMERATION_STEREOTYPE = "<<enumeration>>";

	/**
	 * Maximal {@link LayoutNode} height.
	 */
	public static double findMaxNodeHeight(Collection<LayoutNode> nodes) {
		return Collections.max(nodes, new LayoutNodeHeightComparator()).getHeight();
	}

	/**
	 * {@link NodeLayering} of the given {@link LayoutGraph}.
	 */
	public static UnorderedNodeLayering getLayoutNodeLayering(LayoutGraph graph) {
		Map<Integer, Collection<LayoutNode>> collect = getNodesStream(graph).collect(getSortedYCoordinateGrouper());

		return new UnorderedNodeLayering(getUnorderedNodeLayers(collect));
	}

	private static List<UnorderedNodeLayer> getUnorderedNodeLayers(Map<Integer, Collection<LayoutNode>> collect) {
		return collect.values().stream().map(layer -> new UnorderedNodeLayer(layer)).collect(Collectors.toList());
	}

	/**
	 * Layering of the given {@link LayoutGraph}.
	 */
	public static Map<Integer, Collection<LayoutNode>> getLayering(LayoutGraph graph) {
		return getNodesStream(graph).collect(getYCoordinateGrouper());
	}

	private static Collector<? super LayoutNode, ?, Map<Integer, Collection<LayoutNode>>> getSortedYCoordinateGrouper() {
		return Collectors.groupingBy(yCoordinate(), TreeMap::new, Collectors.toCollection(LinkedHashSet::new));
	}

	private static Collector<LayoutNode, ?, Map<Integer, Collection<LayoutNode>>> getYCoordinateGrouper() {
		return Collectors.groupingBy(yCoordinate(), Collectors.toCollection(LinkedHashSet::new));
	}

	private static Function<? super LayoutNode, ? extends Integer> yCoordinate() {
		return node -> (int) node.getY();
	}

	/**
	 * {@link LayoutEdge} between the given source and target {@link LayoutNode}.
	 */
	public static Collection<LayoutEdge> findEdges(LayoutNode source, LayoutNode target) {
		return getOutgoingEdgesStream(source).filter(edge -> edge.target().equals(target)).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * {@link Stream} of all attached edges to the given {@link NodePort}.
	 */
	public static Stream<LayoutEdge> getAttachedEdgesStream(NodePort port) {
		return port.getAttachedEdges().stream();
	}

	/**
	 * Get all outgoing segments.
	 */
	public static List<LayoutEdge> getOutgoingSegments(LayoutNode node) {
		return getOutgoingEdgesStream(node).filter(edge -> edge.isSegment()).collect(Collectors.toList());
	}

	/**
	 * {@link Stream} of all outgoing {@link LayoutEdge}s for the given node.
	 */
	public static Stream<LayoutEdge> getOutgoingEdgesStream(LayoutNode node) {
		return node.outgoingEdges().stream();
	}

	/**
	 * Get all incoming segments.
	 */
	public static List<LayoutEdge> getIncomingSegments(LayoutNode node) {
		return getIncomingEdgesStream(node).filter(edge -> edge.isSegment()).collect(Collectors.toList());
	}

	private static Stream<LayoutEdge> getIncomingEdgesStream(LayoutNode node) {
		return node.incomingEdges().stream();
	}

	/**
	 * Print the vertical alignment, i.e. all blocks for all {@link LayoutNode}s for this graph.
	 */
	public void printVerticalAlignment(LayoutGraph graph, VerticalAlignment alignment, PrintStream printer) {
		Map<LayoutNode, LayoutNode> aligns = alignment.getAligns();

		getNodesStream(graph).filter(node -> alignment.getRoots().get(node) == node).forEach(node -> {
			printVerticalAlignment(printer, aligns, node);
		});
	}

	private void printVerticalAlignment(PrintStream printer, Map<LayoutNode, LayoutNode> aligns, LayoutNode node) {
		printer.println("Root: " + node);
		LayoutNode currentNode = aligns.get(node);

		while (currentNode != node) {
			printer.println("Node: " + currentNode);
			currentNode = aligns.get(currentNode);
		}
	}

	/**
	 * Print outgoing edges for all {@link LayoutNode}s for this graph.
	 */
	public static void printOutgoingEdges(LayoutGraph graph, PrintStream printer) {
		getOutgoingEdgesStream(graph).forEach(edge -> printer.println(edge));
	}

	private static Stream<LayoutEdge> getOutgoingEdgesStream(LayoutGraph graph) {
		return getNodesStream(graph).flatMap(node -> getOutgoingEdgesStream(node));
	}

	/**
	 * Print incoming edges for all {@link LayoutNode}s for this graph.
	 */
	public static void printIncomingEdges(LayoutGraph graph, PrintStream printer) {
		getIncomingEdgesStream(graph).forEach(edge -> printer.println(edge));
	}

	private static Stream<LayoutEdge> getIncomingEdgesStream(LayoutGraph graph) {
		return getNodesStream(graph).flatMap(node -> getIncomingEdgesStream(node));
	}

	/**
	 * Print all {@link LayoutNode}s for this graph.
	 */
	public static void printNodes(LayoutGraph graph, PrintStream printer) {
		getNodesStream(graph).forEach(node -> printer.println(node));
	}

	/**
	 * Print the given {@link LayoutNode}s.
	 */
	public static void printNodes(Collection<LayoutNode> nodes, PrintStream printer) {
		nodes.stream().forEach(node -> printer.println(node));
	}

	/**
	 * Print the given {@link LayoutNode}.
	 */
	public static void printNode(LayoutNode node, PrintStream printer) {
		printer.println(node);
	}

	/**
	 * Print the given {@link NodeLayer}.
	 */
	public static void printNodeLayer(UnorderedNodeLayer nodeLayer, PrintStream printer) {
		printNodes(nodeLayer.getAll(), printer);
	}

	/**
	 * Print the given one dimensional lines.
	 */
	public static void print1DLines(Collection<Line1D> lines, PrintStream printer) {
		lines.stream().forEach(line -> print1DLine(line, printer));
	}

	/**
	 * Print the given one dimensional line.
	 */
	public static void print1DLine(Line1D line, PrintStream printer) {
		printer.println("start: " + line.getStart() + ", end: " + line.getEnd());
	}

	/**
	 * Print the given one dimensional line containers.
	 */
	public static void print1DLineContainers(Collection<Line1DContainer> containers, PrintStream printer) {
		containers.stream().forEach(container -> print1DLineContainer(container, printer));
	}

	/**
	 * Print the given one dimensional line container.
	 */
	public static void print1DLineContainer(Line1DContainer container, PrintStream printer) {
		print1DLines(container.getLines(), printer);
	}

	/**
	 * {@link LayoutNode} {@link Stream} for the given {@link LayoutGraph}.
	 */
	public static Stream<LayoutNode> getNodesStream(LayoutGraph graph) {
		return graph.nodes().stream();
	}

	/**
	 * Filter sinks for the given nodes. A {@link LayoutNode} is a sink if all outgoing
	 * {@link LayoutEdge} are marked.
	 * 
	 * @param nodes
	 *        {@link Collection} of {@link LayoutNode} to be checked.
	 * @return {@link Collection} of {@link LayoutNode} sinks.
	 */
	public static LinkedHashSet<LayoutNode> getSinks(Collection<LayoutNode> nodes, Set<LayoutEdge> markedEdges) {
		return getSinkStream(nodes, markedEdges).collect(getLinkedHashSetCollector());
	}

	private static Collector<LayoutNode, ?, LinkedHashSet<LayoutNode>> getLinkedHashSetCollector() {
		return Collectors.toCollection(LinkedHashSet::new);
	}

	private static Stream<LayoutNode> getSinkStream(Collection<LayoutNode> nodes, Set<LayoutEdge> markedEdges) {
		return nodes.stream().filter(node -> isSink(node, markedEdges));
	}

	/**
	 * Checks whether the given node is a sink or not. A {@link LayoutNode} is a sink if all
	 * outgoing {@link LayoutEdge}s are marked.
	 * 
	 * @return True, if the given node is a sink otherwise false.
	 */
	public static boolean isSink(LayoutNode node, Set<LayoutEdge> markedEdges) {
		return getFilteredEdges(FilterFactory.not(new FilterMarkedEdge(markedEdges)), node.outgoingEdges()).isEmpty();
	}

	/**
	 * Filter sources for the given nodes. A {@link LayoutNode} is a source if all incoming
	 * {@link LayoutEdge} are marked.
	 * 
	 * @param nodes
	 *        {@link Collection} of {@link LayoutNode} to be checked.
	 * @return {@link Collection} of {@link LayoutNode} sources.
	 */
	public static LinkedHashSet<LayoutNode> getSources(Collection<LayoutNode> nodes, Set<LayoutEdge> markedEdges) {
		return getSourceStream(nodes, markedEdges).collect(getLinkedHashSetCollector());
	}

	private static Stream<LayoutNode> getSourceStream(Collection<LayoutNode> nodes, Set<LayoutEdge> markedEdges) {
		return nodes.stream().filter(node -> isSource(node, markedEdges));
	}

	/**
	 * Checks whether the given node is a source or not. A {@link LayoutNode} is a source if all
	 * incoming {@link LayoutEdge}s are marked.
	 * 
	 * @return True, if the given node is a source otherwise false.
	 */
	public static boolean isSource(LayoutNode node, Set<LayoutEdge> markedEdges) {
		return getFilteredEdges(FilterFactory.not(new FilterMarkedEdge(markedEdges)), node.incomingEdges()).isEmpty();
	}

	/**
	 * Filter not marked {@link LayoutEdge}s for the given edges.
	 */
	public static Set<LayoutEdge> getFilteredEdges(Filter<? super LayoutEdge> filter, Collection<LayoutEdge> edges) {
		return FilterUtil.filterInto(new LinkedHashSet<>(), filter, edges);
	}

	/**
	 * Filter not marked {@link LayoutNode}s for the given nodes.
	 */
	public static Set<LayoutNode> getFilteredNodes(Filter<? super LayoutNode> filter, Set<LayoutNode> nodes) {
		return FilterUtil.filterInto(new LinkedHashSet<>(), filter, nodes);
	}

	/**
	 * Filter not marked {@link LayoutNode}s for the given nodes.
	 */
	public static List<LayoutNode> getFilteredNodes(Filter<? super LayoutNode> filter, List<LayoutNode> nodes) {
		return FilterUtil.filterList(filter, nodes);
	}

	/**
	 * Calculates the size difference between two filtered {@link LayoutEdge} sets.
	 */
	public static int getSizeDiff(Set<LayoutEdge> edges1, Set<LayoutEdge> edges2, Filter<? super LayoutEdge> filter) {
		return getFilteredEdges(filter, edges1).size() - getFilteredEdges(filter, edges2).size();
	}

	/**
	 * Get all sinks from the given {@link LayoutNode}s.
	 */
	public static LinkedHashSet<LayoutNode> getSinks(Set<LayoutNode> nodes) {
		return getSinks(nodes, Collections.emptySet());
	}

	/**
	 * Get all sources from the given {@link LayoutNode}s.
	 */
	public static LinkedHashSet<LayoutNode> getSources(Set<LayoutNode> nodes) {
		return getSources(nodes, Collections.emptySet());
	}

	/**
	 * Get the maximal number of ports on one side.
	 */
	public static double getMaxPortSize(LayoutNode node) {
		return Math.max(node.getOutgoingPorts().size(), node.getIncomingPorts().size());
	}

	/**
	 * Sum of each {@link TLTypePart} property heights.
	 */
	public static Double getNodeAttributesHeight(LayoutContext context, TLType type) {
		return getNodeAttributesHeightStream(context, type).reduce(0., Double::sum);
	}

	/**
	 * Maximal {@link TLTypePart} property width, 0 if no property exists.
	 */
	public static Double getNodeAttributesWidth(LayoutContext context, TLType type) {
		return getNodeAttributesWidthStream(context, type).reduce(Double::max).orElse(0.);
	}

	/**
	 * Label of this {@link TLTypePart}. Property representation in form of "name :
	 *         typeName".
	 */
	public static String getLabel(LabelProvider labelProvider, TLTypePart part) {
		if (part instanceof TLClassProperty) {
			return labelProvider.getLabel(part) + " : " + labelProvider.getLabel(part.getType());
		}

		return labelProvider.getLabel(part);
	}

	/**
	 * Label of this {@link TLType}.
	 */
	public static String getLabel(LabelProvider labelProvider, TLType type) {
		return labelProvider.getLabel(type);
	}

	/**
	 * Stream of {@link TLTypePart} attributes for the given node.
	 */
	public static Stream<? extends TLTypePart> getNodeAttributesStream(TLType type, Collection<Object> hiddenElements) {
		if (type instanceof TLClass) {
			return ((TLClass) type).getLocalClassParts().stream()
				.filter(part -> isTLProperty(part) && !hiddenElements.contains(part));
		} else if (type instanceof TLEnumeration) {
			return ((TLEnumeration) type).getClassifiers().stream().filter(part -> !hiddenElements.contains(part));
		} else {
			return null;
		}
	}

	private static boolean isTLProperty(TLClassPart clazzPart) {
		return clazzPart.getModelKind() == ModelKind.PROPERTY;
	}

	private static Stream<Double> getNodeAttributesHeightStream(LayoutContext context, TLType type) {
		return getNodeAttributesStream(type, context.getHiddenElements()).map(AttributeHeightCalculater(context));
	}

	private static Stream<Double> getNodeAttributesWidthStream(LayoutContext context, TLType type) {
		return getNodeAttributesStream(type, context.getHiddenElements()).map(NodeAttributeWidthCalculater(context));
	}

	private static Function<? super TLTypePart, ? extends Double> AttributeHeightCalculater(LayoutContext context) {
		return attribute -> DiagramTextRenderingUtil.getTextHeight(getLabel(context.getLabelProvider(), attribute));
	}

	private static Function<? super TLTypePart, ? extends Double> NodeAttributeWidthCalculater(LayoutContext context) {
		return attribute -> DiagramTextRenderingUtil.getTextWidth(getLabel(context.getLabelProvider(), attribute));
	}

	/**
	 * String representation of the cardinality of this {@link TLStructuredTypePart}.
	 */
	public static String getCardinality(TLStructuredTypePart part) {
		boolean isMultiple = part.isMultiple();
		boolean isMandatory = part.isMandatory();

		if (isMultiple && isMandatory) {
			return ONE_CARDINALITY + CARDINALITY_POINTS + ARBITRARY_CARDINALITY;
		} else if (isMultiple && !isMandatory) {
			return ARBITRARY_CARDINALITY;
		} else if (!isMultiple && isMandatory) {
			return ONE_CARDINALITY;
		} else {
			return ZERO_CARDINALITY + CARDINALITY_POINTS + ONE_CARDINALITY;
		}
	}

	/**
	 * Get the maximal label width for the given outgoing {@link NodePort}.
	 */
	public static double getBottomPortLabelWidth(LayoutContext context, NodePort port, double defaultWidth) {
		if (context.getDirection() == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return getReferenceEdgesStream(port).map(edge -> {
				if (edge.isReversed()) {
					return getEdgeSourceLabelWidth(context, edge);
				} else {
					return getEdgeTargetLabelWidth(context, edge);
				}
			}).reduce(Double::max).orElse(defaultWidth);
		} else {
			return getReferenceEdgesStream(port).map(edge -> {
				if (edge.isReversed()) {
					return getEdgeTargetLabelWidth(context, edge);
				} else {
					return getEdgeSourceLabelWidth(context, edge);
				}
			}).reduce(Double::max).orElse(defaultWidth);
		}
	}

	/**
	 * Get the maximal label width for the given outgoing {@link NodePort}.
	 */
	public static double getOutgoingPortLabelWidth(LayoutContext context, NodePort port, double defaultWidth) {
		return getReferenceEdgesStream(port).map(edge -> {
			if (edge.isReversed()) {
				return getEdgeSourceLabelWidth(context, edge);
			} else {
				return getEdgeTargetLabelWidth(context, edge);
			}
		}).reduce(Double::max).orElse(defaultWidth);
	}

	private static double getEdgeSourceLabelWidth(LayoutContext context, LayoutEdge edge) {
		TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(((TLReference) edge.getBusinessObject()).getEnd());

		double textWidth = otherEnd.getReference() != null ? getLabelWidth(context, otherEnd.getReference()) : 0;

		return Math.max(getCardinalityWidth(otherEnd), textWidth);
	}
	
	/**
	 * Get the maximal label width for the given incoming {@link NodePort}.
	 */
	public static double getTopPortLabelWidth(LayoutContext context, NodePort port, double defaultWidth) {
		if (context.getDirection() == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return getReferenceEdgesStream(port).map(edge -> {
				if (edge.isReversed()) {
					return getEdgeTargetLabelWidth(context, edge);
				} else {
					return getEdgeSourceLabelWidth(context, edge);
				}
			}).reduce(Double::max).orElse(defaultWidth);
		} else {
			return getReferenceEdgesStream(port).map(edge -> {
				if (edge.isReversed()) {
					return getEdgeSourceLabelWidth(context, edge);
				} else {
					return getEdgeTargetLabelWidth(context, edge);
				}
			}).reduce(Double::max).orElse(defaultWidth);
		}
	}

	/**
	 * Get the maximal label width for the given incoming {@link NodePort}.
	 */
	public static double getIncomingPortLabelWidth(LayoutContext context, NodePort port, double defaultWidth) {
		return getReferenceEdgesStream(port).map(edge -> {
			if (edge.isReversed()) {
				return getEdgeTargetLabelWidth(context, edge);
			} else {
				return getEdgeSourceLabelWidth(context, edge);
			}
		}).reduce(Double::max).orElse(defaultWidth);
	}

	private static double getEdgeTargetLabelWidth(LayoutContext context, LayoutEdge edge) {
		TLStructuredTypePart part = (TLStructuredTypePart) edge.getBusinessObject();

		return Math.max(getCardinalityWidth(part), getLabelWidth(context, part));
	}

	/**
	 * Width of the {@link TLStructuredTypePart} label text.
	 */
	public static double getLabelWidth(LayoutContext context, TLStructuredTypePart part) {
		return DiagramTextRenderingUtil.getTextWidth(getLabel(context.getLabelProvider(), part));
	}

	/**
	 * Width of the {@link TLType} label text.
	 */
	public static double getLabelWidth(LayoutContext context, TLType type) {
		return DiagramTextRenderingUtil.getTextWidth(getLabel(context.getLabelProvider(), type));
	}

	/**
	 * Height of the {@link TLType} label text.
	 */
	public static double getLabelHeight(LayoutContext context, TLType type) {
		return DiagramTextRenderingUtil.getTextHeight(getLabel(context.getLabelProvider(), type));
	}

	private static double getCardinalityWidth(TLStructuredTypePart part) {
		return DiagramTextRenderingUtil.getTextWidth(LayoutGraphUtil.getCardinality(part));
	}

	private static Stream<LayoutEdge> getReferenceEdgesStream(NodePort port) {
		return port.getAttachedEdges().stream()
			.filter(edge -> edge.getBusinessObject() instanceof TLStructuredTypePart);
	}

	/**
	 * Created {@link LayoutEdge} between the given source and target.
	 */
	public static LayoutEdge createEdge(LayoutGraph graph, LayoutNode source, LayoutNode target, LayoutEdge edge) {
		LayoutEdge newEdge = graph.connect(source, target);

		newEdge.setBusinessObject(edge.getBusinessObject());
		newEdge.setReversed(edge.isReversed());

		return newEdge;
	}

	/**
	 * Created {@link LayoutEdge} between the given source port and target port.
	 */
	public static LayoutEdge createEdge(LayoutGraph graph, NodePort source, NodePort target, LayoutEdge edge) {
		LayoutEdge newEdge = graph.connect(source, target, edge.getBusinessObject());

		newEdge.setReversed(edge.isReversed());

		return newEdge;
	}

	/**
	 * Checks if the {@link LayoutGraph} is cyclic.
	 * 
	 * @return True, if the graph is cyclic otherwise false.
	 */
	public static boolean isCyclic(LayoutGraph graph) {
		Set<LayoutNode> visitedNodes = new LinkedHashSet<>();
		Set<LayoutNode> recursionStack = new LinkedHashSet<>();

		for (LayoutNode node : graph.nodes()) {
			if (hasCycle(node, visitedNodes, recursionStack)) {
				return true;
			}
		}

		return false;
	}

	private static boolean hasCycle(LayoutNode node, Set<LayoutNode> visitedNodes, Set<LayoutNode> recursionStack) {
		if (recursionStack.contains(node)) {
			return true;
		}

		if (visitedNodes.contains(node)) {
			return false;
		}

		visitedNodes.add(node);
		recursionStack.add(node);

		for (LayoutNode nextNode : node.outgoing()) {
			if (hasCycle(nextNode, visitedNodes, recursionStack)) {
				return true;
			}
		}

		recursionStack.remove(node);

		return false;
	}

	/**
	 * Width on the grid for the given incoming port inclusive label w.r.t. to the given
	 *         minimal width.
	 */
	public static double getTopPortGridLabelWidth(LayoutContext context, NodePort port, int gridStepSize,
			int minWidth) {
		int topPortLabelWidth = (int) getTopPortLabelWidth(context, port, 0);
	
		return MathUtil.roundUpperMultiple(Math.max(minWidth, topPortLabelWidth), gridStepSize);
	}

	/**
	 * Width on the grid for the given incoming port inclusive label w.r.t. to the given
	 *         minimal width.
	 */
	public static double getIncomingPortGridLabelWidth(LayoutContext context, NodePort port, int gridStepSize,
			int minWidth) {
		int topPortLabelWidth = (int) getIncomingPortLabelWidth(context, port, 0);

		return MathUtil.roundUpperMultiple(Math.max(minWidth, topPortLabelWidth), gridStepSize);
	}

	/**
	 * Width on the grid for all incoming ports inclusive label.
	 */
	public static double getTopPortsGridWidth(LayoutContext context, List<NodePort> ports) {
		return ports.stream()
			.map(port -> getTopPortGridLabelWidth(context, port, GraphConstants.SCALE, GraphConstants.SCALE))
				.reduce(Double::sum).orElse(DefaultNodeSizer.DEFAULT_PORT_WIDTH);
	}

	/**
	 * Width on the grid for all incoming ports inclusive label.
	 */
	public static double getIncomingPortsGridWidth(LayoutContext context, List<NodePort> ports) {
		return ports.stream()
			.map(port -> getIncomingPortGridLabelWidth(context, port, GraphConstants.SCALE, GraphConstants.SCALE))
			.reduce(Double::sum).orElse(DefaultNodeSizer.DEFAULT_PORT_WIDTH);
	}

	/**
	 * Width on the grid for all incoming ports inclusive label.
	 */
	public static double getBottomPortsGridWidth(LayoutContext context, List<NodePort> ports) {
		return ports.stream()
			.map(port -> getBottomPortGridLabelWidth(context, port, GraphConstants.SCALE, GraphConstants.SCALE))
			.reduce(Double::sum).orElse(DefaultNodeSizer.DEFAULT_PORT_WIDTH);
	}

	/**
	 * Width on the grid for all incoming ports inclusive label.
	 */
	public static double getOutgoingPortsGridWidth(LayoutContext context, List<NodePort> ports) {
		return ports.stream()
			.map(port -> getOutgoingPortGridLabelWidth(context, port, GraphConstants.SCALE, GraphConstants.SCALE))
			.reduce(Double::sum).orElse(DefaultNodeSizer.DEFAULT_PORT_WIDTH);
	}

	/**
	 * Width on the grid for the given outgoing port inclusive label w.r.t. to the given
	 *         minimal width.
	 */
	public static double getBottomPortGridLabelWidth(LayoutContext context, NodePort port, int gridStepSize,
			int minWidth) {
		int bottomPortLabelWidth = (int) getBottomPortLabelWidth(context, port, 0);
	
		return MathUtil.roundUpperMultiple(Math.max(minWidth, bottomPortLabelWidth), gridStepSize);
	}

	/**
	 * Width on the grid for the given outgoing port inclusive label w.r.t. to the given
	 *         minimal width.
	 */
	public static double getOutgoingPortGridLabelWidth(LayoutContext context, NodePort port, int gridStepSize,
			int minWidth) {
		int bottomPortLabelWidth = (int) getOutgoingPortLabelWidth(context, port, 0);

		return MathUtil.roundUpperMultiple(Math.max(minWidth, bottomPortLabelWidth), gridStepSize);
	}

	/**
	 * Width on the grid for the given {@link TLType}.
	 */
	public static double getNodeGridWidth(LayoutContext context, TLType type, int gridStepSize, double minWidth) {
		double typeWidth = getNodeWidth(context, type);

		return MathUtil.roundUpperMultiple(Math.max(typeWidth, minWidth), gridStepSize);
	}

	/**
	 * Width of the given {@link TLType}.
	 */
	public static double getNodeWidth(LayoutContext context, TLType type) {
		double nodeNameWidth = getLabelWidth(context, type);
		double nodeAttributesWidth = getNodeAttributesWidth(context, type);
		double nodeStereotypesWidth = getNodeStereotypesWidth(type);
	
		return Collections.max(Arrays.asList(nodeNameWidth, nodeAttributesWidth, nodeStereotypesWidth));
	}

	private static double getNodeStereotypesWidth(TLType type) {
		if (type instanceof TLEnumeration) {
			return DiagramTextRenderingUtil.getTextWidth(ENUMERATION_STEREOTYPE);
		}

		return 0;
	}

	/**
	 * Maximal vertical coordinate of the given graph.
	 */
	public static Optional<Double> getMaximalVerticalCoordinate(LayoutGraph graph) {
		return getNodesStream(graph).map(node -> node.getY()).max(Double::compare);
	}

	/**
	 * Maximal horizontal coordinate of the given graph.
	 */
	public static Optional<Double> getMaximalHorizontalCoordinate(LayoutGraph graph) {
		return getNodesStream(graph).map(node -> node.getX()).max(Double::compare);
	}

	/**
	 * Minimal vertical coordinate of the given graph.
	 */
	public static Optional<Double> getMinimalVerticalCoordinate(LayoutGraph graph) {
		return getNodesStream(graph).map(node -> node.getY()).min(Double::compare);
	}

	/**
	 * Minimal horizontal coordinate of the given graph.
	 */
	public static Optional<Double> getMinimalHorizontalCoordinate(LayoutGraph graph) {
		return getNodesStream(graph).map(node -> node.getX()).min(Double::compare);
	}

	/**
	 * Get all adjacent {@link LayoutNode}s from the layer above for the given
	 * {@link LayoutDirection} and {@link LayoutNode}.
	 */
	public static Set<LayoutNode> getTopNodes(LayoutDirection direction, LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return node.incoming();
		} else {
			return node.outgoing();
		}
	}

	/**
	 * Get the adjacent {@link LayoutNode} above for the given {@link LayoutEdge} and
	 * {@link LayoutDirection}.
	 */
	public static LayoutNode getTopNode(LayoutDirection direction, LayoutEdge edge) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return edge.source();
		} else {
			return edge.target();
		}
	}

	/**
	 * Get the adjacent {@link LayoutNode} above for the given {@link VirtualSegmentEdge} and
	 * {@link LayoutDirection}.
	 */
	public static Object getTopNode(LayoutDirection direction, VirtualSegmentEdge edge) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return edge.source();
		} else {
			return edge.target();
		}
	}

	/**
	 * Get all {@link LayoutEdge}s connected to the top {@link NodePort}s of the given
	 * {@link LayoutNode} for the given {@link LayoutDirection}.
	 */
	public static Collection<LayoutEdge> getTopEdges(LayoutDirection direction, LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return node.incomingEdges();
		} else {
			return node.outgoingEdges();
		}
	}

	/**
	 * Get all {@link LayoutGraph} {@link LayoutEdge edges}.
	 */
	public static Set<LayoutEdge> getEdges(LayoutGraph graph) {
		return getEdges(graph, x -> true);
	}

	/**
	 * Get all {@link LayoutGraph} {@link LayoutEdge edges} that matches the given {@link Predicate
	 * filter}.
	 */
	public static Set<LayoutEdge> getEdges(LayoutGraph graph, Predicate<LayoutEdge> filter) {
		return getNodesStream(graph)
			.flatMap(node -> getOutgoingEdgesStream(node))
			.filter(filter)
			.collect(Collectors.toSet());
	}

	/**
	 * Get all {@link LayoutEdge}s between the the given source and target w.r.t. to the given
	 * {@link LayoutDirection}.
	 */
	public static Collection<LayoutEdge> getEdges(LayoutDirection direction, LayoutGraph graph, LayoutNode source,
			LayoutNode target) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return graph.edges(source, target);
		} else {
			return graph.edges(target, source);
		}
	}

	/**
	 * Get all segments, see {@link LayoutEdge#isSegment()}, connected to the top {@link NodePort}s
	 * of the given {@link LayoutNode} for the given {@link LayoutDirection}.
	 */
	public static List<LayoutEdge> getTopSegments(LayoutDirection direction, LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return LayoutGraphUtil.getOutgoingSegments(node);
		} else {
			return LayoutGraphUtil.getIncomingSegments(node);
		}
	}

	/**
	 * Get all adjacent {@link LayoutNode}s from the layer below for the given
	 * {@link LayoutDirection} and {@link LayoutNode}.
	 */
	public static Set<LayoutNode> getBottomNodes(LayoutDirection direction, LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return node.outgoing();
		} else {
			return node.incoming();
		}
	}

	/**
	 * Get the adjacent {@link LayoutNode} below for the given {@link LayoutEdge} and
	 * {@link LayoutDirection}.
	 */
	public static LayoutNode getBottomNode(LayoutDirection direction, LayoutEdge edge) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return edge.target();
		} else {
			return edge.source();
		}
	}

	/**
	 * Get the adjacent {@link LayoutNode} below for the given {@link VirtualSegmentEdge} and
	 * {@link LayoutDirection}.
	 */
	public static Object getBottomNode(LayoutDirection direction, VirtualSegmentEdge edge) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return edge.target();
		} else {
			return edge.source();
		}
	}

	/**
	 * Get all {@link LayoutEdge}s connected to the bottom {@link NodePort}s of the given
	 * {@link LayoutNode} for the given {@link LayoutDirection}.
	 */
	public static Collection<LayoutEdge> getBottomEdges(LayoutDirection direction, LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return node.outgoingEdges();
		} else {
			return node.incomingEdges();
		}
	}

	/**
	 * Get all segments, see {@link LayoutEdge#isSegment()}, connected to the bottom
	 * {@link NodePort}s of the given {@link LayoutNode} for the given {@link LayoutDirection}.
	 */
	public static List<LayoutEdge> getBottomSegments(LayoutDirection direction, LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return LayoutGraphUtil.getIncomingSegments(node);
		} else {
			return LayoutGraphUtil.getOutgoingSegments(node);
		}
	}

	/**
	 * Creates a {@link VirtualSegmentEdge} for the sweep down crossing minimizer dependent of the
	 * given {@link LayoutDirection}.
	 */
	public static VirtualSegmentEdge getVirtualEdgeSweepDown(LayoutDirection direction, SegmentContainer container,
			LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return new VirtualSegmentEdge(container, node);
		} else {
			return new VirtualSegmentEdge(node, container);
		}
	}

	/**
	 * Creates a {@link VirtualSegmentEdge} for the sweep up crossing minimizer dependent of the
	 * given {@link LayoutDirection}.
	 */
	public static VirtualSegmentEdge getVirtualEdgeSweepUp(LayoutDirection direction, LayoutNode node,
			SegmentContainer container) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return new VirtualSegmentEdge(node, container);
		} else {
			return new VirtualSegmentEdge(container, node);
		}
	}

	/**
	 * Creates a {@link VirtualSegmentEdge} dependent of the given {@link LayoutDirection}.
	 */
	public static VirtualSegmentEdge getVirtualEdge(LayoutDirection direction, SegmentContainer container1,
			SegmentContainer container2) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return new VirtualSegmentEdge(container1, container2);
		} else {
			return new VirtualSegmentEdge(container2, container1);
		}
	}

	/**
	 * Get the index, position of the given top {@link NodePort} dependent of the given
	 * {@link LayoutDirection}.
	 */
	public static int getTopNodePortIndex(LayoutDirection direction, NodePort port) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return port.getNode().getIncomingPorts().indexOf(port);
		} else {
			return port.getNode().getOutgoingPorts().indexOf(port);
		}
	}

	/**
	 * Get the index, position of the given bottom {@link NodePort} dependent of the given
	 * {@link LayoutDirection}.
	 */
	public static int getBottomNodePortIndex(LayoutDirection direction, NodePort port) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return port.getNode().getOutgoingPorts().indexOf(port);
		} else {
			return port.getNode().getIncomingPorts().indexOf(port);
		}
	}

	/**
	 * Get the connected {@link NodePort} from above dependent of the given {@link LayoutDirection}.
	 */
	public static Optional<NodePort> getTopNodePort(LayoutDirection direction, LayoutEdge edge) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return edge.getSourceNodePort();
		} else {
			return edge.getTargetNodePort();
		}
	}

	/**
	 * Get the connected {@link NodePort} from below dependent of the given {@link LayoutDirection}.
	 */
	public static Optional<NodePort> getBottomNodePort(LayoutDirection direction, LayoutEdge edge) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return edge.getTargetNodePort();
		} else {
			return edge.getSourceNodePort();
		}
	}

	/**
	 * Get all connected {@link NodePort} from above for the given {@link LayoutNode} dependent of
	 * the given {@link LayoutDirection}.
	 */
	public static List<NodePort> getTopNodePorts(LayoutDirection direction, LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return node.getIncomingPorts();
		} else {
			return node.getOutgoingPorts();
		}
	}

	/**
	 * @see #getTopNodePorts(LayoutDirection, LayoutNode)
	 */
	public static void setTopNodePorts(LayoutDirection direction, LayoutNode node, List<NodePort> topPorts) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			node.setIncomingPorts(topPorts);
		} else {
			node.setOutgoingPorts(topPorts);
		}
	}

	/**
	 * Get all connected {@link NodePort} from below for the given {@link LayoutNode} dependent of
	 * the given {@link LayoutDirection}.
	 */
	public static List<NodePort> getBottomNodePorts(LayoutDirection direction, LayoutNode node) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return node.getOutgoingPorts();
		} else {
			return node.getIncomingPorts();
		}
	}

	/**
	 * @see #getBottomNodePorts(LayoutDirection, LayoutNode)
	 */
	public static void setBottomNodePorts(LayoutDirection direction, LayoutNode node, List<NodePort> bottomPorts) {
		if (direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			node.setOutgoingPorts(bottomPorts);
		} else {
			node.setIncomingPorts(bottomPorts);
		}
	}

	/**
	 * Priorities for the given {@link LayoutEdge}s.
	 */
	public static List<Integer> getPriorities(Collection<LayoutEdge> edges) {
		return getEdgePriorityStream(edges).collect(Collectors.toList());
	}

	private static Stream<Integer> getEdgePriorityStream(Collection<LayoutEdge> edges) {
		return edges.stream().map(edge -> edge.getPriority());
	}

	/**
	 * Ordered priorities for the given {@link LayoutEdge}s.
	 */
	public static List<Integer> getDescendingOrderedPriorities(Collection<LayoutEdge> edges) {
		return getEdgePriorityStream(edges).sorted(Collections.reverseOrder()).collect(Collectors.toList());
	}

}
