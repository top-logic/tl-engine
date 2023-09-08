/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.util;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.Node;

/**
 * Utility methods for working with {@link GraphModel}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GraphUtil {

	/**
	 * {@link Edge}s connecting the given source and target node.
	 * 
	 * @param source
	 *        {@link Node} source.
	 * @param target
	 *        {@link Node} target.
	 * 
	 * @return Edges connecting source and target.
	 */
	public static Set<Edge> edge(Node source, Node target) {
		return source.getOutgoingEdges().stream().filter(edge -> edge.getDestination() == target)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * The outgoing and the incoming {@link Edge}s connected to the given node.
	 * 
	 * @param node
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Set<Edge> allEdges(Node node) {
		Set<Edge> result = new HashSet<>();
		result.addAll(node.getOutgoingEdges());
		result.addAll(node.getIncomingEdges());
		return result;
	}

	/**
	 * The destinations of the outgoing {@link Edge}s.
	 * 
	 * @param node
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Set<Node> successors(Node node) {
		return destinations(node.getOutgoingEdges());
	}

	/**
	 * The sources of the incoming {@link Edge}s.
	 * 
	 * @param node
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Set<Node> predecessors(Node node) {
		return sources(node.getIncomingEdges());
	}

	/**
	 * The direct {@link #predecessors(Node)} and {@link #successors(Node)} of the given node.
	 * 
	 * @param node
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Set<Node> neighbors(Node node) {
		Set<Node> result = new HashSet<>();
		result.addAll(predecessors(node));
		result.addAll(successors(node));
		return result;
	}

	/**
	 * The sources of the given {@link Edge}s.
	 * 
	 * @param edges
	 *        If null, the result is the empty (immutable) set.
	 * @return Never null.
	 */
	public static Set<Node> sources(Collection<? extends Edge> edges) {
		if (edges == null) {
			return emptySet();
		}
		Set<Node> result = new HashSet<>();
		for (Edge edge : edges) {
			result.add(edge.getSource());
		}
		return result;
	}

	/**
	 * The destinations of the given {@link Edge}s.
	 * 
	 * @param edges
	 *        If null, the result is the empty (immutable) set.
	 * @return Never null.
	 */
	public static Set<Node> destinations(Collection<? extends Edge> edges) {
		if (edges == null) {
			return emptySet();
		}
		Set<Node> result = new HashSet<>();
		for (Edge edge : edges) {
			result.add(edge.getDestination());
		}
		return result;
	}

	/**
	 * Set the visibility of the given {@link Node}s to the given value.
	 * 
	 * @param nodes
	 *        If null, nothing happens.
	 */
	public static void setVisible(Collection<? extends Node> nodes, boolean visible) {
		if (nodes == null) {
			return;
		}
		for (Node node : nodes) {
			node.setVisible(visible);
		}
	}

	/**
	 * Count how many nodes have the searched visibility.
	 * 
	 * @param nodes
	 *        If null the result is 0.
	 */
	public static int countVisibility(Collection<? extends Node> nodes, boolean searchedVisibility) {
		if (nodes == null) {
			return 0;
		}
		int result = 0;
		for (Node node : nodes) {
			if (node.isVisible() == searchedVisibility) {
				result += 1;
			}
		}
		return result;
	}

	/**
	 * Checks if the given graphPart is a {@link Node} {@link Label}.
	 */
	public static boolean isNodeLabel(GraphPart graphPart) {
		return isLabel(graphPart) && ((Label) graphPart).getOwner() instanceof Node;
	}

	/**
	 * Checks if the given graphPart is a {@link Label}.
	 */
	public static boolean isLabel(GraphPart graphPart) {
		return graphPart instanceof Label;
	}

}
