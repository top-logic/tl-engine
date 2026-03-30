package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GraphEdge}.
 */
public class GraphEdge_Impl extends com.top_logic.react.flow.data.impl.Widget_Impl implements com.top_logic.react.flow.data.GraphEdge {

	private com.top_logic.react.flow.data.Box _source = null;

	private com.top_logic.react.flow.data.Box _target = null;

	private final java.util.List<com.top_logic.react.flow.data.GraphWaypoint> _waypoints = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.GraphWaypoint>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.GraphWaypoint element) {
			_listener.beforeAdd(GraphEdge_Impl.this, WAYPOINTS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.GraphWaypoint element) {
			_listener.afterRemove(GraphEdge_Impl.this, WAYPOINTS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GraphEdge_Impl.this, WAYPOINTS__PROP);
		}
	};

	private String _strokeStyle = "black";

	private double _thickness = 1.0;

	private final java.util.List<Double> _dashes = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(GraphEdge_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(GraphEdge_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GraphEdge_Impl.this, DASHES__PROP);
		}
	};

	private final java.util.List<com.top_logic.react.flow.data.EdgeDecoration> _decorations = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.EdgeDecoration>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.EdgeDecoration element) {
			_listener.beforeAdd(GraphEdge_Impl.this, DECORATIONS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.EdgeDecoration element) {
			_listener.afterRemove(GraphEdge_Impl.this, DECORATIONS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GraphEdge_Impl.this, DECORATIONS__PROP);
		}
	};

	/**
	 * Creates a {@link GraphEdge_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GraphEdge#create()
	 */
	public GraphEdge_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GRAPH_EDGE;
	}

	@Override
	public final com.top_logic.react.flow.data.Box getSource() {
		return _source;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setSource(com.top_logic.react.flow.data.Box value) {
		internalSetSource(value);
		return this;
	}

	/** Internal setter for {@link #getSource()} without chain call utility. */
	protected final void internalSetSource(com.top_logic.react.flow.data.Box value) {
		_listener.beforeSet(this, SOURCE__PROP, value);
		_source = value;
		_listener.afterChanged(this, SOURCE__PROP);
	}

	@Override
	public final boolean hasSource() {
		return _source != null;
	}

	@Override
	public final com.top_logic.react.flow.data.Box getTarget() {
		return _target;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setTarget(com.top_logic.react.flow.data.Box value) {
		internalSetTarget(value);
		return this;
	}

	/** Internal setter for {@link #getTarget()} without chain call utility. */
	protected final void internalSetTarget(com.top_logic.react.flow.data.Box value) {
		_listener.beforeSet(this, TARGET__PROP, value);
		_target = value;
		_listener.afterChanged(this, TARGET__PROP);
	}

	@Override
	public final boolean hasTarget() {
		return _target != null;
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.GraphWaypoint> getWaypoints() {
		return _waypoints;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setWaypoints(java.util.List<? extends com.top_logic.react.flow.data.GraphWaypoint> value) {
		internalSetWaypoints(value);
		return this;
	}

	/** Internal setter for {@link #getWaypoints()} without chain call utility. */
	protected final void internalSetWaypoints(java.util.List<? extends com.top_logic.react.flow.data.GraphWaypoint> value) {
		if (value == null) throw new IllegalArgumentException("Property 'waypoints' cannot be null.");
		_waypoints.clear();
		_waypoints.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge addWaypoint(com.top_logic.react.flow.data.GraphWaypoint value) {
		internalAddWaypoint(value);
		return this;
	}

	/** Implementation of {@link #addWaypoint(com.top_logic.react.flow.data.GraphWaypoint)} without chain call utility. */
	protected final void internalAddWaypoint(com.top_logic.react.flow.data.GraphWaypoint value) {
		_waypoints.add(value);
	}

	@Override
	public final void removeWaypoint(com.top_logic.react.flow.data.GraphWaypoint value) {
		_waypoints.remove(value);
	}

	@Override
	public final String getStrokeStyle() {
		return _strokeStyle;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setStrokeStyle(String value) {
		internalSetStrokeStyle(value);
		return this;
	}

	/** Internal setter for {@link #getStrokeStyle()} without chain call utility. */
	protected final void internalSetStrokeStyle(String value) {
		_listener.beforeSet(this, STROKE_STYLE__PROP, value);
		_strokeStyle = value;
		_listener.afterChanged(this, STROKE_STYLE__PROP);
	}

	@Override
	public final double getThickness() {
		return _thickness;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setThickness(double value) {
		internalSetThickness(value);
		return this;
	}

	/** Internal setter for {@link #getThickness()} without chain call utility. */
	protected final void internalSetThickness(double value) {
		_listener.beforeSet(this, THICKNESS__PROP, value);
		_thickness = value;
		_listener.afterChanged(this, THICKNESS__PROP);
	}

	@Override
	public final java.util.List<Double> getDashes() {
		return _dashes;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setDashes(java.util.List<? extends Double> value) {
		internalSetDashes(value);
		return this;
	}

	/** Internal setter for {@link #getDashes()} without chain call utility. */
	protected final void internalSetDashes(java.util.List<? extends Double> value) {
		_dashes.clear();
		_dashes.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge addDash(double value) {
		internalAddDash(value);
		return this;
	}

	/** Implementation of {@link #addDash(double)} without chain call utility. */
	protected final void internalAddDash(double value) {
		_dashes.add(value);
	}

	@Override
	public final void removeDash(double value) {
		_dashes.remove(value);
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.EdgeDecoration> getDecorations() {
		return _decorations;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setDecorations(java.util.List<? extends com.top_logic.react.flow.data.EdgeDecoration> value) {
		internalSetDecorations(value);
		return this;
	}

	/** Internal setter for {@link #getDecorations()} without chain call utility. */
	protected final void internalSetDecorations(java.util.List<? extends com.top_logic.react.flow.data.EdgeDecoration> value) {
		if (value == null) throw new IllegalArgumentException("Property 'decorations' cannot be null.");
		_decorations.clear();
		_decorations.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge addDecoration(com.top_logic.react.flow.data.EdgeDecoration value) {
		internalAddDecoration(value);
		return this;
	}

	/** Implementation of {@link #addDecoration(com.top_logic.react.flow.data.EdgeDecoration)} without chain call utility. */
	protected final void internalAddDecoration(com.top_logic.react.flow.data.EdgeDecoration value) {
		_decorations.add(value);
	}

	@Override
	public final void removeDecoration(com.top_logic.react.flow.data.EdgeDecoration value) {
		_decorations.remove(value);
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GraphEdge setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return GRAPH_EDGE__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SOURCE__PROP, 
			TARGET__PROP, 
			WAYPOINTS__PROP, 
			STROKE_STYLE__PROP, 
			THICKNESS__PROP, 
			DASHES__PROP, 
			DECORATIONS__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.Widget_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.Widget_Impl.TRANSIENT_PROPERTIES);
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
			case SOURCE__PROP: return getSource();
			case TARGET__PROP: return getTarget();
			case WAYPOINTS__PROP: return getWaypoints();
			case STROKE_STYLE__PROP: return getStrokeStyle();
			case THICKNESS__PROP: return getThickness();
			case DASHES__PROP: return getDashes();
			case DECORATIONS__PROP: return getDecorations();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SOURCE__PROP: internalSetSource((com.top_logic.react.flow.data.Box) value); break;
			case TARGET__PROP: internalSetTarget((com.top_logic.react.flow.data.Box) value); break;
			case WAYPOINTS__PROP: internalSetWaypoints(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GraphWaypoint.class, value)); break;
			case STROKE_STYLE__PROP: internalSetStrokeStyle((String) value); break;
			case THICKNESS__PROP: internalSetThickness((double) value); break;
			case DASHES__PROP: internalSetDashes(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			case DECORATIONS__PROP: internalSetDecorations(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.EdgeDecoration.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasSource()) {
			out.name(SOURCE__PROP);
			getSource().writeTo(scope, out);
		}
		if (hasTarget()) {
			out.name(TARGET__PROP);
			getTarget().writeTo(scope, out);
		}
		out.name(WAYPOINTS__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GraphWaypoint x : getWaypoints()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(STROKE_STYLE__PROP);
		out.value(getStrokeStyle());
		out.name(THICKNESS__PROP);
		out.value(getThickness());
		out.name(DASHES__PROP);
		out.beginArray();
		for (double x : getDashes()) {
			out.value(x);
		}
		out.endArray();
		out.name(DECORATIONS__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.EdgeDecoration x : getDecorations()) {
			x.writeTo(scope, out);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case SOURCE__PROP: {
				if (hasSource()) {
					getSource().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case TARGET__PROP: {
				if (hasTarget()) {
					getTarget().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case WAYPOINTS__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.GraphWaypoint x : getWaypoints()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case STROKE_STYLE__PROP: {
				out.value(getStrokeStyle());
				break;
			}
			case THICKNESS__PROP: {
				out.value(getThickness());
				break;
			}
			case DASHES__PROP: {
				out.beginArray();
				for (double x : getDashes()) {
					out.value(x);
				}
				out.endArray();
				break;
			}
			case DECORATIONS__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.EdgeDecoration x : getDecorations()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SOURCE__PROP: setSource(com.top_logic.react.flow.data.Box.readBox(scope, in)); break;
			case TARGET__PROP: setTarget(com.top_logic.react.flow.data.Box.readBox(scope, in)); break;
			case WAYPOINTS__PROP: {
				java.util.List<com.top_logic.react.flow.data.GraphWaypoint> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.GraphWaypoint.readGraphWaypoint(scope, in));
				}
				in.endArray();
				setWaypoints(newValue);
			}
			break;
			case STROKE_STYLE__PROP: setStrokeStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case THICKNESS__PROP: setThickness(in.nextDouble()); break;
			case DASHES__PROP: {
				java.util.List<Double> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(in.nextDouble());
				}
				in.endArray();
				setDashes(newValue);
			}
			break;
			case DECORATIONS__PROP: {
				java.util.List<com.top_logic.react.flow.data.EdgeDecoration> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.EdgeDecoration.readEdgeDecoration(scope, in));
				}
				in.endArray();
				setDecorations(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case WAYPOINTS__PROP: {
				((com.top_logic.react.flow.data.GraphWaypoint) element).writeTo(scope, out);
				break;
			}
			case DASHES__PROP: {
				out.value(((double) element));
				break;
			}
			case DECORATIONS__PROP: {
				((com.top_logic.react.flow.data.EdgeDecoration) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case WAYPOINTS__PROP: {
				return com.top_logic.react.flow.data.GraphWaypoint.readGraphWaypoint(scope, in);
			}
			case DASHES__PROP: {
				return in.nextDouble();
			}
			case DECORATIONS__PROP: {
				return com.top_logic.react.flow.data.EdgeDecoration.readEdgeDecoration(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
