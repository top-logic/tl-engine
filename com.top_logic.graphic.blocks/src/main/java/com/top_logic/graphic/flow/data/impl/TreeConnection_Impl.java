package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.TreeConnection}.
 */
public class TreeConnection_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.TreeConnection {

	private com.top_logic.graphic.flow.data.TreeLayout _owner = null;

	private com.top_logic.graphic.flow.data.TreeConnector _parent = null;

	private com.top_logic.graphic.flow.data.TreeConnector _child = null;

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
			case BAR_POSITION__PROP: return getBarPosition();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case PARENT__PROP: internalSetParent((com.top_logic.graphic.flow.data.TreeConnector) value); break;
			case CHILD__PROP: internalSetChild((com.top_logic.graphic.flow.data.TreeConnector) value); break;
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
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
