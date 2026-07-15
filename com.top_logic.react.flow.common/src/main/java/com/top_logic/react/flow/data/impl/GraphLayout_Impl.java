package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GraphLayout}.
 */
public class GraphLayout_Impl extends com.top_logic.react.flow.data.impl.FloatingLayout_Impl implements com.top_logic.react.flow.data.GraphLayout {

	private final java.util.List<com.top_logic.react.flow.data.GraphEdge> _edges = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.GraphEdge>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.GraphEdge element) {
			_listener.beforeAdd(GraphLayout_Impl.this, EDGES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.GraphEdge element) {
			_listener.afterRemove(GraphLayout_Impl.this, EDGES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GraphLayout_Impl.this, EDGES__PROP);
		}
	};

	private double _layerGap = 0.0d;

	private double _nodeGap = 0.0d;

	/**
	 * Creates a {@link GraphLayout_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GraphLayout#create()
	 */
	public GraphLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GRAPH_LAYOUT;
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.GraphEdge> getEdges() {
		return _edges;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setEdges(java.util.List<? extends com.top_logic.react.flow.data.GraphEdge> value) {
		internalSetEdges(value);
		return this;
	}

	/** Internal setter for {@link #getEdges()} without chain call utility. */
	protected final void internalSetEdges(java.util.List<? extends com.top_logic.react.flow.data.GraphEdge> value) {
		if (value == null) throw new IllegalArgumentException("Property 'edges' cannot be null.");
		_edges.clear();
		_edges.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout addEdge(com.top_logic.react.flow.data.GraphEdge value) {
		internalAddEdge(value);
		return this;
	}

	/** Implementation of {@link #addEdge(com.top_logic.react.flow.data.GraphEdge)} without chain call utility. */
	protected final void internalAddEdge(com.top_logic.react.flow.data.GraphEdge value) {
		_edges.add(value);
	}

	@Override
	public final void removeEdge(com.top_logic.react.flow.data.GraphEdge value) {
		_edges.remove(value);
	}

	@Override
	public final double getLayerGap() {
		return _layerGap;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setLayerGap(double value) {
		internalSetLayerGap(value);
		return this;
	}

	/** Internal setter for {@link #getLayerGap()} without chain call utility. */
	protected final void internalSetLayerGap(double value) {
		_listener.beforeSet(this, LAYER_GAP__PROP, value);
		_layerGap = value;
		_listener.afterChanged(this, LAYER_GAP__PROP);
	}

	@Override
	public final double getNodeGap() {
		return _nodeGap;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setNodeGap(double value) {
		internalSetNodeGap(value);
		return this;
	}

	/** Internal setter for {@link #getNodeGap()} without chain call utility. */
	protected final void internalSetNodeGap(double value) {
		_listener.beforeSet(this, NODE_GAP__PROP, value);
		_nodeGap = value;
		_listener.afterChanged(this, NODE_GAP__PROP);
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setNodes(java.util.List<? extends com.top_logic.react.flow.data.Box> value) {
		internalSetNodes(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout addNode(com.top_logic.react.flow.data.Box value) {
		internalAddNode(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphLayout setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return GRAPH_LAYOUT__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			EDGES__PROP, 
			LAYER_GAP__PROP, 
			NODE_GAP__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.FloatingLayout_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.FloatingLayout_Impl.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case EDGES__PROP: return getEdges();
			case LAYER_GAP__PROP: return getLayerGap();
			case NODE_GAP__PROP: return getNodeGap();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case EDGES__PROP: internalSetEdges(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GraphEdge.class, value)); break;
			case LAYER_GAP__PROP: internalSetLayerGap((double) value); break;
			case NODE_GAP__PROP: internalSetNodeGap((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(EDGES__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GraphEdge x : getEdges()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(LAYER_GAP__PROP);
		out.value(getLayerGap());
		out.name(NODE_GAP__PROP);
		out.value(getNodeGap());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case EDGES__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.GraphEdge x : getEdges()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case LAYER_GAP__PROP: {
				out.value(getLayerGap());
				break;
			}
			case NODE_GAP__PROP: {
				out.value(getNodeGap());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case EDGES__PROP: {
				java.util.List<com.top_logic.react.flow.data.GraphEdge> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.GraphEdge.readGraphEdge(scope, in));
				}
				in.endArray();
				setEdges(newValue);
			}
			break;
			case LAYER_GAP__PROP: setLayerGap(in.nextDouble()); break;
			case NODE_GAP__PROP: setNodeGap(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case EDGES__PROP: {
				((com.top_logic.react.flow.data.GraphEdge) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case EDGES__PROP: {
				return com.top_logic.react.flow.data.GraphEdge.readGraphEdge(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
