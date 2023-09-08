/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.col.iterator.AppendIterator;
import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * A utility for propagating "Directed Acyclic Graph" node properties down the graph.
 * <p>
 * For example for propagating annotations along a class inheritance hierarchy. The result would be
 * a {@link Map} of classes and their (indirectly) inherited annotations.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class DAGPropertyPropagation<N, P> {

	private final List<N> _startNodes;

	private List<N> _nodes;

	private Map<N, P> _properties;

	/**
	 * Creates a {@link DAGPropertyPropagation}.
	 * 
	 * @param startNodes
	 *        See: {@link #getStartNodes()}
	 */
	public DAGPropertyPropagation(Collection<? extends N> startNodes) {
		_startNodes = list(startNodes);
	}

	/**
	 * The nodes given in the constructor.
	 * <p>
	 * They don't have to be the first nodes in the graph: The successors and predecessors of all
	 * nodes are explored.
	 * </p>
	 * 
	 * @return A new, mutable and resizable {@link List}.
	 */
	public List<N> getStartNodes() {
		return list(_startNodes);
	}

	/**
	 * All the nodes in the graph.
	 * 
	 * @return A new, mutable and resizable {@link List}.
	 */
	public List<N> getNodes() {
		if (_nodes == null) {
			_nodes = findAllNodes();
		}
		return list(_nodes);
	}

	private List<N> findAllNodes() {
		Set<N> knownNodes = set();
		AppendIterator<N> nodes = new AppendIterator<>();
		nodes.appendAll(getStartNodes());
		knownNodes.addAll(getStartNodes());
		while (nodes.hasNext()) {
			N node = nodes.next();
			Set<N> newNodes = set();
			newNodes.addAll(getSuccessors(node));
			newNodes.addAll(getPredecessors(node));
			newNodes.removeAll(knownNodes);
			nodes.appendAll(newNodes);
			knownNodes.addAll(newNodes);
		}
		return nodes.copyUnderlyingCollection();
	}

	/**
	 * The properties of all the nodes in the graph.
	 * 
	 * @return A new, mutable and resizable {@link Map}.
	 */
	public Map<N, P> getProperties() {
		if (_properties == null) {
			_properties = propagateProperties();
		}
		return map(_properties);
	}

	/**
	 * The property of the given node.
	 * 
	 * @return Null, if there is no property.
	 */
	public P getProperty(N node) {
		if (_properties == null) {
			_properties = propagateProperties();
		}
		return _properties.get(node);
	}

	private Map<N, P> propagateProperties() {
		List<N> sortedNodes = CollectionUtilShared.topsort(this::getPredecessors, getNodes(), false);
		Map<N, P> properties = map();
		for (N node : sortedNodes) {
			P localProperty = getLocalProperty(node);
			List<P> inheritedProperties = getInheritedProperties(node, properties);
			P mergedProperty = mergePropertiesInternal(node, localProperty, inheritedProperties);
			properties.put(node, mergedProperty);
		}
		return properties;
	}

	private List<P> getInheritedProperties(N node, Map<N, P> properties) {
		return getPredecessors(node).stream()
			.map(properties::get)
			.filter(Objects::nonNull)
			.collect(toList());
	}

	private P mergePropertiesInternal(N node, P localProperty, List<P> inheritedProperties) {
		if (inheritedProperties.isEmpty()) {
			return localProperty;
		}
		return mergeProperties(node, localProperty, inheritedProperties);
	}

	/**
	 * The <em>direct</em> successors of the given node.
	 * 
	 * @param node
	 *        Never null.
	 * @return Is not allowed to be null.
	 */
	protected abstract Collection<? extends N> getSuccessors(N node);

	/**
	 * The <em>direct</em> predecessors of the given node.
	 * 
	 * @param node
	 *        Never null.
	 * @return Is not allowed to be null.
	 */
	protected abstract Collection<? extends N> getPredecessors(N node);

	/**
	 * The <em>local</em> properties of the given node.
	 * 
	 * @param node
	 *        Never null.
	 * @return Null, if there is no local property.
	 */
	protected abstract P getLocalProperty(N node);

	/**
	 * This method is called only when the inherited properties are not empty. Otherwise, the local
	 * property is used.
	 * 
	 * @param local
	 *        Null, if there is no local property.
	 * @param inherited
	 *        Never null. Never empty.
	 * @return Null, if the result of the merge is "no property".
	 */
	protected abstract P mergeProperties(N node, P local, List<P> inherited);

}
