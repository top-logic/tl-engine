/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

/**
 * A modification event in a {@link GraphModel}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GraphEvent<E extends GraphPart> {

	/**
	 * Visitor interface for {@link GraphEvent}s.
	 * 
	 * @param <R>
	 *        The result type of the visit.
	 * @param <A>
	 *        The type of the visit argument.
	 * 
	 * @see GraphEvent#visit
	 */
	public interface Visitor<R, A> {
		/**
		 * Visit case for {@link NodeCreated}.
		 */
		R visit(NodeCreated event, A arg);

		/**
		 * Visit case for {@link NodeDeleted}.
		 */
		R visit(NodeDeleted event, A arg);

		/**
		 * Visit case for {@link NodeChanged}.
		 */
		R visit(NodeChanged event, A arg);

		/**
		 * Visit case for {@link EdgeCreated}.
		 */
		R visit(EdgeCreated event, A arg);

		/**
		 * Visit case for {@link EdgeDeleted}.
		 */
		R visit(EdgeDeleted event, A arg);

		/**
		 * Visit case for {@link EdgeChanged}.
		 */
		R visit(EdgeChanged event, A arg);

		/**
		 * Visit case for {@link LabelCreated}.
		 */
		R visit(LabelCreated event, A arg);

		/**
		 * Visit case for {@link LabelDeleted}.
		 */
		R visit(LabelDeleted event, A arg);

		/**
		 * Visit case for {@link LabelChanged}.
		 */
		R visit(LabelChanged event, A arg);
	}

	private final GraphModel _graph;

	private E _element;

	/**
	 * Creates a {@link GraphEvent}.
	 *
	 * @param graph
	 *        See {@link #getGraph()}.
	 */
	public GraphEvent(GraphModel graph, E element) {
		_graph = graph;
		_element = element;
	}

	/**
	 * The modified {@link GraphModel}.
	 */
	public GraphModel getGraph() {
		return _graph;
	}

	/**
	 * The modified {@link GraphPart}.
	 */
	public E getElement() {
		return _element;
	}

	/**
	 * Visits this {@link GraphEvent} with the given visitor.
	 * 
	 * @param v
	 *        The visitor to invoke a callback on.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result returned from the visitor.
	 */
	public abstract <R, A> R visit(Visitor<R, A> v, A arg);

	/**
	 * A {@link Node} modification {@link GraphEvent}.
	 */
	public abstract static class NodeEvent extends GraphEvent<Node> {

		/**
		 * Creates a {@link GraphEvent.NodeEvent}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param node
		 *        See {@link #getNode()}.
		 */
		public NodeEvent(GraphModel graph, Node node) {
			super(graph, node);
		}

		/**
		 * The modified {@link Node}.
		 */
		public Node getNode() {
			return getElement();
		}

	}

	/**
	 * A node creation {@link GraphEvent}.
	 */
	public static class NodeCreated extends NodeEvent {

		/**
		 * Creates a {@link GraphEvent.NodeCreated}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param node
		 *        See {@link #getNode()}.
		 */
		public NodeCreated(GraphModel graph, Node node) {
			super(graph, node);
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

	/**
	 * A node deletion {@link GraphEvent}.
	 */
	public static class NodeDeleted extends NodeEvent {

		/**
		 * Creates a {@link GraphEvent.NodeDeleted}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param node
		 *        See {@link #getNode()}.
		 */
		public NodeDeleted(GraphModel graph, Node node) {
			super(graph, node);
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

	/**
	 * A node change {@link GraphEvent}.
	 */
	public static class NodeChanged extends NodeEvent {

		private String _property;

		/**
		 * Creates a {@link GraphEvent.NodeChanged}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param node
		 *        See {@link #getNode()}.
		 * @param property
		 *        See {@link #getProperty()}.
		 */
		public NodeChanged(GraphModel graph, Node node, String property) {
			super(graph, node);
			_property = property;
		}

		/**
		 * The changed property.
		 */
		public String getProperty() {
			return _property;
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

	/**
	 * An {@link Edge} changing {@link GraphEvent}.
	 */
	public abstract static class EdgeEvent extends GraphEvent<Edge> {

		/**
		 * Creates a {@link GraphEvent.EdgeEvent}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param edge
		 *        See {@link #getEdge()}.
		 */
		public EdgeEvent(GraphModel graph, Edge edge) {
			super(graph, edge);
		}

		/**
		 * The modified {@link Edge}.
		 */
		public Edge getEdge() {
			return getElement();
		}

	}

	/**
	 * An {@link Edge} creating {@link GraphEvent}.
	 */
	public static class EdgeCreated extends EdgeEvent {

		/**
		 * Creates a {@link GraphEvent.EdgeCreated}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param edge
		 *        See {@link #getEdge()}.
		 */
		public EdgeCreated(GraphModel graph, Edge edge) {
			super(graph, edge);
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

	/**
	 * An {@link Edge} deleting {@link GraphEvent}.
	 */
	public static class EdgeDeleted extends EdgeEvent {

		/**
		 * Creates a {@link GraphEvent.EdgeDeleted}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param edge
		 *        See {@link #getEdge()}.
		 */
		public EdgeDeleted(GraphModel graph, Edge edge) {
			super(graph, edge);
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

	/**
	 * An {@link Edge} changing {@link GraphEvent}.
	 */
	public static class EdgeChanged extends EdgeEvent {

		private String _property;

		/**
		 * Creates a {@link GraphEvent.EdgeChanged}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param edge
		 *        See {@link #getEdge()}.
		 * @param property
		 *        See {@link #getProperty()}.
		 */
		public EdgeChanged(GraphModel graph, Edge edge, String property) {
			super(graph, edge);
			_property = property;
		}

		/**
		 * The changed property.
		 */
		public String getProperty() {
			return _property;
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

	/**
	 * A {@link Label} changing {@link GraphEvent}.
	 */
	public abstract static class LabelEvent extends GraphEvent<Label> {

		/**
		 * Creates a {@link GraphEvent.EdgeEvent}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param label
		 *        See {@link #getLabel()}.
		 */
		public LabelEvent(GraphModel graph, Label label) {
			super(graph, label);
		}

		/**
		 * The modified {@link Label}.
		 */
		public Label getLabel() {
			return getElement();
		}

	}

	/**
	 * A {@link Label} deleting {@link GraphEvent}.
	 */
	public static class LabelDeleted extends LabelEvent {

		/**
		 * Creates a {@link GraphEvent.LabelDeleted}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param label
		 *        See {@link #getLabel()}.
		 */
		public LabelDeleted(GraphModel graph, Label label) {
			super(graph, label);
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

	/**
	 * A {@link Label} changing {@link GraphEvent}.
	 */
	public static class LabelChanged extends LabelEvent {

		private String _property;

		/**
		 * Creates a {@link GraphEvent.LabelChanged}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param label
		 *        See {@link #getLabel()}.
		 * @param property
		 *        See {@link #getProperty()}.
		 */
		public LabelChanged(GraphModel graph, Label label, String property) {
			super(graph, label);
			_property = property;
		}

		/**
		 * The changed property.
		 */
		public String getProperty() {
			return _property;
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

	/**
	 * A {@link Label} creating {@link GraphEvent}.
	 */
	public static class LabelCreated extends LabelEvent {

		/**
		 * Creates a {@link GraphEvent.EdgeCreated}.
		 *
		 * @param graph
		 *        See {@link #getGraph()}.
		 * @param label
		 *        See {@link #getLabel()}.
		 */
		public LabelCreated(GraphModel graph, Label label) {
			super(graph, label);
		}

		@Override
		public <R, A> R visit(Visitor<R, A> v, A arg) {
			return v.visit(this, arg);
		}
	}

}
