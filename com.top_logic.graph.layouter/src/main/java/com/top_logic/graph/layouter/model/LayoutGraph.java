/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import com.top_logic.basic.graph.ExplicitGraph;

/**
 * Graph with a layout structure, i.e. every node has a x and y coordinate.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutGraph extends ExplicitGraph<LayoutGraph.LayoutNode, LayoutGraph.LayoutEdge> {
	/**
	 * Type for a {@link LayoutNode} for a layered layout graph structure.
	 * 
	 * Additional type of nodes, dummy nodes, are need for the following use cases:
	 * 
	 * Edges that span over exactly three layers are separated into two edges. For such an edge
	 * (v,w) we need an additional dummy node (namely r). (v,w) is now separated into (v,r) and
	 * (r,w).
	 * 
	 * Edges that span over more than three layers are described using segments. For such an edge
	 * (v,w) we need two additional dummy nodes (namely p and q). (v,w) is now separated into (v,p),
	 * (p,q) and (q,w). The edge (p,q) is called the "inner" segment.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public enum NodeType {
		/**
		 * Dummy {@link LayoutNode} which is source of a segment.
		 */
		P_DUMMY_NODE,

		/**
		 * Dummy {@link LayoutNode} which is target of a segment.
		 */
		Q_DUMMY_NODE,

		/**
		 * Dummy {@link LayoutNode} for edges that span over exactly three layers where no segment
		 * is needed.
		 */
		R_DUMMY_NODE,

		/**
		 * Standard {@link LayoutNode}.
		 */
		GRAPH_NODE
	}

	/**
	 * Graph node suited for layouting. A node have a x and y coordinate and can be marked as a
	 * dummy node which is not shown in the final layout.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public class LayoutNode extends ExplicitGraph<LayoutNode, LayoutEdge>.Node {
		private double _x;

		private double _y;

		private double _height;

		private double _width;

		private final NodeType _type;

		private Object userObject;

		private List<NodePort> _incomingPorts = new LinkedList<>();

		private List<NodePort> _outgoingPorts = new LinkedList<>();

		/**
		 * Node with no specific position.
		 */
		public LayoutNode() {
			this(0, 0, NodeType.GRAPH_NODE);
		}

		@Override
		public String toString() {
			return "LayoutNode [_x=" + _x + ", _y=" + _y + ", _type=" + _type + ", userObject=" + userObject
				+ ", _width=" + _width + ", _height=" + _height + "]";
		}

		/**
		 * Node with no specific position.
		 */
		public LayoutNode(NodeType type) {
			this(0, 0, type);
		}

		/**
		 * @param x
		 *        X coordinate of this node.
		 * @param y
		 *        Y coordinate of this node.
		 */
		public LayoutNode(int x, int y) {
			this(x, y, NodeType.GRAPH_NODE);
		}

		/**
		 * @param x
		 *        Horizontal coordinate of this node.
		 * @param y
		 *        Vertical coordinate of this node.
		 */
		public LayoutNode(int x, int y, NodeType type) {
			_x = x;
			_y = y;

			_type = type;
		}

		/**
		 * True, if this node is not shown in the resulting layout, otherwise false.
		 */
		public boolean isDummy() {
			return !NodeType.GRAPH_NODE.equals(_type);
		}

		/**
		 * True, if this node is not shown in the resulting layout, otherwise false.
		 */
		public boolean isSourceDummy() {
			return NodeType.P_DUMMY_NODE.equals(_type);
		}

		/**
		 * True, if this node is not shown in the resulting layout, otherwise false.
		 */
		public boolean isTargetDummy() {
			return NodeType.Q_DUMMY_NODE.equals(_type);
		}

		/**
		 * True, if this node is not shown in the resulting layout, otherwise false.
		 */
		public boolean isSingleDummy() {
			return NodeType.R_DUMMY_NODE.equals(_type);
		}

		/**
		 * X coordinate of this node.
		 */
		public double getX() {
			return _x;
		}

		/**
		 * @param x
		 *        X coordinate of this node.
		 */
		public void setX(double x) {
			_x = x;
		}

		/**
		 * Y coordinate of this node.
		 */
		public double getY() {
			return _y;
		}

		/**
		 * Sets a new vertical coordinate of this node.
		 */
		public void setY(double y) {
			_y = y;
		}

		/**
		 * User object of this node.
		 */
		public Object getUserObject() {
			return userObject;
		}

		/**
		 * Sets a new user object for this node.
		 */
		public void setUserObject(Object userObject) {
			this.userObject = userObject;
		}

		/**
		 * All ports for incoming {@link LayoutEdge}s.
		 */
		public List<NodePort> getIncomingPorts() {
			return new LinkedList<>(_incomingPorts);
		}

		/**
		 * Set the new incoming ports.
		 */
		public void setIncomingPorts(List<NodePort> incomingPorts) {
			_incomingPorts = new LinkedList<>(incomingPorts);
		}

		/**
		 * All ports for outgoing {@link LayoutEdge}s.
		 */
		public List<NodePort> getOutgoingPorts() {
			return new LinkedList<>(_outgoingPorts);
		}

		/**
		 * Set the new outgoing ports.
		 */
		public void setOutgoingPorts(List<NodePort> outgoingPorts) {
			_outgoingPorts = new LinkedList<>(outgoingPorts);
		}

		/**
		 * Outgoing {@link NodePort} for the given edge.
		 */
		public Optional<NodePort> getOutgoingPort(LayoutEdge edge) {
			return getOutgoingPorts().stream().filter(port -> port.isAttached(edge)).findFirst();
		}

		/**
		 * Index of the given outgoing port if exists, otherwise -1.
		 */
		public int getOutgoingPortIndex(NodePort port) {
			return getOutgoingPorts().indexOf(port);
		}

		/**
		 * Incoming {@link NodePort} for the given edge.
		 */
		public Optional<NodePort> getIncomingPort(LayoutEdge edge) {
			return getIncomingPorts().stream().filter(port -> port.isAttached(edge)).findFirst();
		}

		/**
		 * Index of the given incoming port if exists, otherwise -1.
		 */
		public int getIncomingPortIndex(NodePort port) {
			return getIncomingPorts().indexOf(port);
		}

		/**
		 * Add the given incoming {@link NodePort}.
		 */
		public void addIncomingPort(NodePort port) {
			_incomingPorts.add(port);
		}

		/**
		 * Remove the given incoming {@link NodePort}.
		 */
		public void removeIncomingPort(NodePort port) {
			_incomingPorts.remove(port);
		}

		/**
		 * Add the given outgoing {@link NodePort}.
		 */
		public void addOutgoingPort(NodePort port) {
			_outgoingPorts.add(port);
		}

		/**
		 * Remove the given outgoing {@link NodePort}.
		 */
		public void removeOutgoingPort(NodePort port) {
			_outgoingPorts.remove(port);
		}

		/**
		 * Width of this {@link LayoutNode}.
		 */
		public double getWidth() {
			return _width;
		}

		/**
		 * Sets a new {@link LayoutNode} width.
		 */
		public void setWidth(double width) {
			_width = width;
		}

		/**
		 * Height of this {@link LayoutNode}.
		 */
		public double getHeight() {
			return _height;
		}

		/**
		 * Sets a new {@link LayoutNode} height.
		 */
		public void setHeight(double height) {
			_height = height;
		}

	}

	/**
	 * Graph edge for layouting.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public class LayoutEdge extends ExplicitGraph<LayoutNode, LayoutEdge>.Edge {
		private boolean _isSegment;

		private boolean _isReversed;

		private Object _businessObject;

		private int _priority;

		private Optional<List<Waypoint>> _waypoints = Optional.empty();

		/**
		 * @param source
		 *        Source {@link LayoutNode} for this {@link LayoutEdge}.
		 * @param target
		 *        Target {@link LayoutNode} for this {@link LayoutEdge}.
		 */
		protected LayoutEdge(LayoutNode source, LayoutNode target) {
			super(source, target);
		}

		/**
		 * Reversed {@link LayoutEdge}.
		 */
		public LayoutEdge reverse() {
			LayoutEdge reversedEdge = connect(_target, _source, getBusinessObject());

			reversedEdge.setReversed(true);
			reversedEdge.setWaypoints(_waypoints.map(waypoints -> Lists.reverse(waypoints)));

			remove();

			return reversedEdge;
		}

		/**
		 * True if this edge is a segment, otherwise false.
		 */
		public boolean isSegment() {
			return _isSegment;
		}

		/**
		 * @param isSegment
		 *        Flag to mark a {@link LayoutEdge} as a segment.
		 */
		public void setSegment(boolean isSegment) {
			_isSegment = isSegment;
		}

		@Override
		public String toString() {
			return "waypoints=" + getWaypoints() + ", source = " + _source + ", target = " + _target;
		}

		/**
		 * BusinessObject of this {@link LayoutEdge}.
		 */
		public Object getBusinessObject() {
			return _businessObject;
		}

		/**
		 * Sets a new business object to this edge.
		 */
		public void setBusinessObject(Object businessObject) {
			_businessObject = businessObject;
		}

		/**
		 * The target {@link NodePort} where this edge is attached to.
		 */
		public Optional<NodePort> getTargetNodePort() {
			return target().getIncomingPort(this);
		}

		/**
		 * The source {@link NodePort} where this edge is attached to.
		 */
		public Optional<NodePort> getSourceNodePort() {
			return source().getOutgoingPort(this);
		}

		/**
		 * All waypoints for this {@link LayoutEdge}.
		 */
		public List<Waypoint> getWaypoints() {
			return _waypoints.orElse(new LinkedList<>(Arrays.asList(new Waypoint(_source), new Waypoint(_target))));
		}

		/**
		 * Sets the waypoints for this {@link LayoutEdge}.
		 */
		public void setWaypoints(Optional<List<Waypoint>> newWaypoints) {
			_waypoints = newWaypoints.map(waypoints -> new LinkedList<>(waypoints));
		}

		/**
		 * Sets the waypoints for this {@link LayoutEdge}.
		 */
		public void setWaypoints(List<Waypoint> waypoints) {
			_waypoints = Optional.of(new LinkedList<>(waypoints));
		}

		/**
		 * True if this edge resulted in reversing another edge, otherwise false.
		 */
		public boolean isReversed() {
			return _isReversed;
		}

		/**
		 * Sets the reversed flag for this {@link LayoutEdge}.
		 */
		public void setReversed(boolean isReversed) {
			_isReversed = isReversed;
		}

		/**
		 * Edge priority for example to get the an acyclic graph.
		 */
		public int getPriority() {
			return _priority;
		}

		/**
		 * @see #getPriority()
		 */
		public void setPriority(int priority) {
			_priority = priority;
		}

	}

	@Override
	protected LayoutEdge newEdge(LayoutNode source, LayoutNode target) {
		return new LayoutEdge(source, target);
	}

	@Override
	public LayoutNode newNode() {
		return new LayoutNode();
	}

	/**
	 * Created {@link LayoutEdge} with the given businessObject between the source
	 *         {@link LayoutNode} and target {@link LayoutNode}.
	 */
	public LayoutEdge connect(final LayoutNode source, final LayoutNode target, Object businessObject) {
		LayoutEdge edge = connect(source, target);

		edge.setBusinessObject(businessObject);

		source.addOutgoingPort(new NodePort(source, edge));
		target.addIncomingPort(new NodePort(target, edge));

		return edge;
	}

	/**
	 * Created {@link LayoutEdge} with the given businessObject between the source
	 *         {@link NodePort} and target {@link NodePort}.
	 */
	public LayoutEdge connect(NodePort sourcePort, NodePort targetPort, Object businessObject) {
		LayoutEdge edge = connect(sourcePort.getNode(), targetPort.getNode());

		edge.setBusinessObject(businessObject);

		sourcePort.attach(edge);
		targetPort.attach(edge);

		return edge;
	}

	/**
	 * Created new {@link LayoutNode} with the given user object.
	 */
	public LayoutNode newNode(Object userObject) {
		LayoutNode node = new LayoutNode();
		node.setUserObject(userObject);

		return node;
	}

	/**
	 * Created new {@link LayoutNode} with the given {@link NodeType}.
	 */
	public LayoutNode newNode(NodeType type) {
		return new LayoutNode(type);
	}

}
