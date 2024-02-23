/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.impl;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.common.remote.shared.ScopeEvent;
import com.top_logic.common.remote.shared.ScopeEvent.Update;
import com.top_logic.common.remote.shared.ScopeListener;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphListener;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.common.model.layout.GraphLayout;

/**
 * {@link DefaultSharedObject} {@link GraphModel} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultGraphModel extends DefaultSharedObject implements SharedGraph, ScopeListener {

	private Map<Object, GraphPart> _graphPartByTag = new HashMap<>();

	private GraphListeners _listeners = new GraphListeners();

	private Object _tag;

	private boolean _internalOperation;

	/**
	 * Creates a {@link DefaultGraphModel}.
	 *
	 * @param scope
	 *        The {@link ObjectScope} to install this {@link GraphModel} and all its derived objects
	 *        in.
	 */
	public DefaultGraphModel(ObjectScope scope) {
		super(scope);
		scope.addListener(this);
	}

	@Override
	public Object getTag() {
		return _tag;
	}

	@Override
	public void setTag(Object value) {
		_tag = value;
		scope().notifyUpdate(this, TAG);
	}

	@Override
	public boolean isEditing() {
		return getDataBoolean(EDITING);
	}

	@Override
	public void setEditMode(boolean editing) {
		setData(EDITING, Boolean.valueOf(editing));
	}

	@Override
	public GraphLayout getLayout() {
		return getData(LAYOUT);
	}

	@Override
	public void setLayout(GraphLayout layout) {
		setData(LAYOUT, layout);
	}

	@Override
	public Collection<? extends Node> getNodes() {
		return filterNodes(getReferrers(DefaultGraphPart.class, DefaultGraphPart.GRAPH));
	}

	private Set<Node> filterNodes(Collection<?> unfiltered) {
		return unfiltered.stream()
			.filter(element -> element instanceof Node)
			.map(element -> (Node) element)
			.collect(toSet());
	}

	@Override
	public void handleObjectScopeEvent(ScopeEvent event) {
		if (_internalOperation) {
			return;
		}
		switch (event.getKind()) {
			case CREATE: {
				Object target = event.getObj();
				if (target instanceof Node) {
					sendNodeCreationEvent((Node) target);
				} else if (target instanceof Edge) {
					sendEdgeCreationEvent((Edge) target);
				} else if (target instanceof Label) {
					sendLabelCreationEvent((Label) target);
				}
				break;
			}
			case DELETE: {
				Object target = event.getObj();
				if (target instanceof Node) {
					_listeners.nodeDeleted(this, (Node) target);
				} else if (target instanceof Edge) {
					_listeners.edgeDeleted(this, (Edge) target);
				} else if (target instanceof Label) {
					_listeners.labelDeleted(this, (Label) target);
				}
				break;
			}
			case UPDATE: {
				Object target = event.getObj();
				String property = ((Update) event).getProperty();
				if (target instanceof Node) {
					_listeners.nodeChanged(this, (Node) target, property);
				} else if (target instanceof Edge) {
					_listeners.edgeChanged(this, (Edge) target, property);
				} else if (target instanceof Label) {
					_listeners.labelChanged(this, (Label) target, property);
				}
				break;
			}
			case POST_PROCESS:
				// Nothing to do on post process
				break;
			case PREPARE:
				// Nothing to do on prepare
				break;
		}
	}

	private void sendNodeCreationEvent(Node newNode) {
		_listeners.nodeCreated(this, newNode);
	}

	private void sendEdgeCreationEvent(Edge newEdge) {
		_listeners.edgeCreated(this, newEdge);
	}

	private void sendLabelCreationEvent(Label newLabel) {
		_listeners.labelCreated(this, newLabel);
	}

	@Override
	public Node createNode(Node parent, Object tag) {
		// Defer node creation event until the basic properties have been set.
		boolean before = _internalOperation;
		_internalOperation = true;
		try {
			DefaultNode newNode = createNodeInternal(scope());
			newNode.initGraph(this);
			newNode.setParent(parent);
			newNode.setTag(tag);
			sendNodeCreationEvent(newNode);
			return newNode;
		} finally {
			_internalOperation = before;
		}
	}

	/**
	 * Creates only the {@link Node} instance without any further initialization.
	 * <p>
	 * For subclasses that have to create a more specific type.
	 * </p>
	 */
	protected DefaultNode createNodeInternal(ObjectScope scope) {
		return new DefaultNode(scope);
	}

	@Override
	public Edge createEdge(Node source, Node dest, Object tag) {
		boolean before = _internalOperation;
		_internalOperation = true;
		try {
			Edge newEdge = createEdgeInternal(scope());
			newEdge.initGraph(this);
			newEdge.setSource(source);
			newEdge.setDestination(dest);
			newEdge.setTag(tag);
			sendEdgeCreationEvent(newEdge);
			return newEdge;
		} finally {
			_internalOperation = before;
		}
	}

	/**
	 * Creates only the {@link Edge} instance without any further initialization.
	 * <p>
	 * For subclasses that have to create a more specific type.
	 * </p>
	 */
	protected Edge createEdgeInternal(ObjectScope scope) {
		return new DefaultEdge(scope);
	}

	@Override
	public DefaultNode getNode(Object businessObject) {
		GraphPart graphPart = getGraphPart(businessObject);

		if (graphPart instanceof DefaultNode) {
			return (DefaultNode) graphPart;
		}

		return null;
	}

	@Override
	public Edge getEdge(Object businessObject) {
		GraphPart graphPart = getGraphPart(businessObject);

		if (graphPart instanceof Edge) {
			return (Edge) graphPart;
		}

		return null;
	}

	@Override
	public GraphPart getGraphPart(Object tag) {
		return _graphPartByTag.get(tag);
	}

	@Override
	public Collection<? extends GraphPart> getSelectedGraphParts() {
		Collection<GraphPart> selectedGraphParts = get(SELECTED_GRAPH_PARTS);

		if (selectedGraphParts == null) {
			return Collections.emptySet();
		}

		return selectedGraphParts;
	}

	@Override
	public void setSelectedGraphParts(Collection<? extends GraphPart> selectedGraphParts) {
		set(SELECTED_GRAPH_PARTS, Collections.unmodifiableCollection(new HashSet<>(selectedGraphParts)));
	}

	void updateBusinessObject(GraphPart graphPart, Object oldValue, Object newValue) {
		if (oldValue != null) {
			_graphPartByTag.remove(oldValue);
		}
		if (newValue != null) {
			_graphPartByTag.put(newValue, graphPart);
		}
	}

	@Override
	public void reset() {
		_listeners.clear();
		for (Node node : getNodes()) {
			node.delete();
		}
	}

	@Override
	public void remove(Node node) {
		node.delete();
	}

	void removeInternal(GraphPart part) {
		Object tag = part.getTag();
		if (tag != null) {
			_graphPartByTag.remove(tag);
		}
	}

	@Override
	public void removeGraphPart(GraphPart graphPart) {
		Collection<? extends GraphPart> selection = getSelectedGraphParts();

		if (selection.contains(graphPart)) {
			HashSet<GraphPart> newSelection = new HashSet<>(selection);
			newSelection.remove(graphPart);
			setSelectedGraphParts(newSelection);
		}

		graphPart.delete();
	}

	@Override
	public void remove(Edge edge) {
		edge.delete();
	}

	@Override
	public void addGraphListener(GraphListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeGraphListener(GraphListener listener) {
		_listeners.remove(listener);
	}

	@Override
	protected void onDelete() {
		throw new UnsupportedOperationException(
			"The graph itself cannot be deleted, as it is the root of the object tree.");
	}

}
