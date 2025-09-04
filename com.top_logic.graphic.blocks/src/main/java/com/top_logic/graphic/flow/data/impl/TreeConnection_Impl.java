package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.TreeConnection}.
 */
public class TreeConnection_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.TreeConnection {

	private com.top_logic.graphic.flow.data.TreeLayout _owner = null;

	private com.top_logic.graphic.flow.data.TreeConnector _parent = null;

	private com.top_logic.graphic.flow.data.TreeConnector _child = null;

	private String _strokeStyle = "black";

	private Double _thickness = 1.0;

	private final java.util.List<Double> _dashes = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(TreeConnection_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(TreeConnection_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(TreeConnection_Impl.this, DASHES__PROP);
		}
	};

	private transient double _barPosition = 0.0d;

	/**
	 * Creates a {@link TreeConnection_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.TreeConnection#create()
	 */
	public TreeConnection_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.TREE_CONNECTION;
	}

	@Override
	public final com.top_logic.graphic.flow.data.TreeLayout getOwner() {
		return _owner;
	}

	/**
	 * Internal setter for updating derived field.
	 */
	com.top_logic.graphic.flow.data.TreeConnection setOwner(com.top_logic.graphic.flow.data.TreeLayout value) {
		internalSetOwner(value);
		return this;
	}

	/** Internal setter for {@link #getOwner()} without chain call utility. */
	protected final void internalSetOwner(com.top_logic.graphic.flow.data.TreeLayout value) {
		_listener.beforeSet(this, OWNER__PROP, value);
		if (value != null && _owner != null) {
			throw new IllegalStateException("Object may not be part of two different containers.");
		}
		_owner = value;
		_listener.afterChanged(this, OWNER__PROP);
	}

	@Override
	public final boolean hasOwner() {
		return _owner != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.TreeConnector getParent() {
		return _parent;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setParent(com.top_logic.graphic.flow.data.TreeConnector value) {
		internalSetParent(value);
		return this;
	}

	/** Internal setter for {@link #getParent()} without chain call utility. */
	protected final void internalSetParent(com.top_logic.graphic.flow.data.TreeConnector value) {
		com.top_logic.graphic.flow.data.impl.TreeConnector_Impl before = (com.top_logic.graphic.flow.data.impl.TreeConnector_Impl) _parent;
		com.top_logic.graphic.flow.data.impl.TreeConnector_Impl after = (com.top_logic.graphic.flow.data.impl.TreeConnector_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.TreeConnection oldContainer = after.getConnection();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, PARENT__PROP, value);
		if (before != null) {
			before.internalSetConnection(null);
		}
		_parent = value;
		if (after != null) {
			after.internalSetConnection(this);
		}
		_listener.afterChanged(this, PARENT__PROP);
	}

	@Override
	public final boolean hasParent() {
		return _parent != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.TreeConnector getChild() {
		return _child;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setChild(com.top_logic.graphic.flow.data.TreeConnector value) {
		internalSetChild(value);
		return this;
	}

	/** Internal setter for {@link #getChild()} without chain call utility. */
	protected final void internalSetChild(com.top_logic.graphic.flow.data.TreeConnector value) {
		com.top_logic.graphic.flow.data.impl.TreeConnector_Impl before = (com.top_logic.graphic.flow.data.impl.TreeConnector_Impl) _child;
		com.top_logic.graphic.flow.data.impl.TreeConnector_Impl after = (com.top_logic.graphic.flow.data.impl.TreeConnector_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.TreeConnection oldContainer = after.getConnection();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, CHILD__PROP, value);
		if (before != null) {
			before.internalSetConnection(null);
		}
		_child = value;
		if (after != null) {
			after.internalSetConnection(this);
		}
		_listener.afterChanged(this, CHILD__PROP);
	}

	@Override
	public final boolean hasChild() {
		return _child != null;
	}

	@Override
	public final String getStrokeStyle() {
		return _strokeStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setStrokeStyle(String value) {
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
	public final boolean hasStrokeStyle() {
		return _strokeStyle != null;
	}

	@Override
	public final Double getThickness() {
		return _thickness;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setThickness(Double value) {
		internalSetThickness(value);
		return this;
	}

	/** Internal setter for {@link #getThickness()} without chain call utility. */
	protected final void internalSetThickness(Double value) {
		_listener.beforeSet(this, THICKNESS__PROP, value);
		_thickness = value;
		_listener.afterChanged(this, THICKNESS__PROP);
	}

	@Override
	public final boolean hasThickness() {
		return _thickness != null;
	}

	@Override
	public final java.util.List<Double> getDashes() {
		return _dashes;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setDashes(java.util.List<? extends Double> value) {
		internalSetDashes(value);
		return this;
	}

	/** Internal setter for {@link #getDashes()} without chain call utility. */
	protected final void internalSetDashes(java.util.List<? extends Double> value) {
		_dashes.clear();
		_dashes.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection addDashe(double value) {
		internalAddDashe(value);
		return this;
	}

	/** Implementation of {@link #addDashe(double)} without chain call utility. */
	protected final void internalAddDashe(double value) {
		_dashes.add(value);
	}

	@Override
	public final void removeDashe(double value) {
		_dashes.remove(value);
	}

	@Override
	public final double getBarPosition() {
		return _barPosition;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setBarPosition(double value) {
		internalSetBarPosition(value);
		return this;
	}

	/** Internal setter for {@link #getBarPosition()} without chain call utility. */
	protected final void internalSetBarPosition(double value) {
		_listener.beforeSet(this, BAR_POSITION__PROP, value);
		_barPosition = value;
		_listener.afterChanged(this, BAR_POSITION__PROP);
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TREE_CONNECTION__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			OWNER__PROP, 
			PARENT__PROP, 
			CHILD__PROP, 
			STROKE_STYLE__PROP, 
			THICKNESS__PROP, 
			DASHES__PROP, 
			BAR_POSITION__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Widget_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Widget_Impl.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				BAR_POSITION__PROP));
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
			case OWNER__PROP: return getOwner();
			case PARENT__PROP: return getParent();
			case CHILD__PROP: return getChild();
			case STROKE_STYLE__PROP: return getStrokeStyle();
			case THICKNESS__PROP: return getThickness();
			case DASHES__PROP: return getDashes();
			case BAR_POSITION__PROP: return getBarPosition();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case PARENT__PROP: internalSetParent((com.top_logic.graphic.flow.data.TreeConnector) value); break;
			case CHILD__PROP: internalSetChild((com.top_logic.graphic.flow.data.TreeConnector) value); break;
			case STROKE_STYLE__PROP: internalSetStrokeStyle((String) value); break;
			case THICKNESS__PROP: internalSetThickness((Double) value); break;
			case DASHES__PROP: internalSetDashes(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			case BAR_POSITION__PROP: internalSetBarPosition((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasParent()) {
			out.name(PARENT__PROP);
			getParent().writeTo(scope, out);
		}
		if (hasChild()) {
			out.name(CHILD__PROP);
			getChild().writeTo(scope, out);
		}
		if (hasStrokeStyle()) {
			out.name(STROKE_STYLE__PROP);
			out.value(getStrokeStyle());
		}
		if (hasThickness()) {
			out.name(THICKNESS__PROP);
			out.value(getThickness());
		}
		out.name(DASHES__PROP);
		out.beginArray();
		for (double x : getDashes()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case OWNER__PROP: {
				if (hasOwner()) {
					getOwner().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case PARENT__PROP: {
				if (hasParent()) {
					getParent().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case CHILD__PROP: {
				if (hasChild()) {
					getChild().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case STROKE_STYLE__PROP: {
				if (hasStrokeStyle()) {
					out.value(getStrokeStyle());
				} else {
					out.nullValue();
				}
				break;
			}
			case THICKNESS__PROP: {
				if (hasThickness()) {
					out.value(getThickness());
				} else {
					out.nullValue();
				}
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
			case BAR_POSITION__PROP: {
				out.value(getBarPosition());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case PARENT__PROP: setParent(com.top_logic.graphic.flow.data.TreeConnector.readTreeConnector(scope, in)); break;
			case CHILD__PROP: setChild(com.top_logic.graphic.flow.data.TreeConnector.readTreeConnector(scope, in)); break;
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
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case DASHES__PROP: {
				out.value(((double) element));
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DASHES__PROP: {
				return in.nextDouble();
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
