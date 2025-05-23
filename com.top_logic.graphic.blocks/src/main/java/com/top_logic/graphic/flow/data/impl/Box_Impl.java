package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Box}.
 */
public abstract class Box_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.Box {

	private com.top_logic.graphic.flow.data.Widget _parent = null;

	private double _x = 0.0d;

	private double _y = 0.0d;

	private double _width = 0.0d;

	private double _height = 0.0d;

	/**
	 * Creates a {@link Box_Impl} instance.
	 */
	public Box_Impl() {
		super();
	}

	@Override
	public final com.top_logic.graphic.flow.data.Widget getParent() {
		return _parent;
	}

	/**
	 * Internal setter for updating derived field.
	 */
	com.top_logic.graphic.flow.data.Box setParent(com.top_logic.graphic.flow.data.Widget value) {
		internalSetParent(value);
		return this;
	}

	/** Internal setter for {@link #getParent()} without chain call utility. */
	protected final void internalSetParent(com.top_logic.graphic.flow.data.Widget value) {
		_listener.beforeSet(this, PARENT__PROP, value);
		if (value != null && _parent != null) {
			throw new IllegalStateException("Object may not be part of two different containers.");
		}
		_parent = value;
		_listener.afterChanged(this, PARENT__PROP);
	}

	@Override
	public final boolean hasParent() {
		return _parent != null;
	}

	@Override
	public final double getX() {
		return _x;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setX(double value) {
		internalSetX(value);
		return this;
	}

	/** Internal setter for {@link #getX()} without chain call utility. */
	protected final void internalSetX(double value) {
		_listener.beforeSet(this, X__PROP, value);
		_x = value;
		_listener.afterChanged(this, X__PROP);
	}

	@Override
	public final double getY() {
		return _y;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setY(double value) {
		internalSetY(value);
		return this;
	}

	/** Internal setter for {@link #getY()} without chain call utility. */
	protected final void internalSetY(double value) {
		_listener.beforeSet(this, Y__PROP, value);
		_y = value;
		_listener.afterChanged(this, Y__PROP);
	}

	@Override
	public final double getWidth() {
		return _width;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	/** Internal setter for {@link #getWidth()} without chain call utility. */
	protected final void internalSetWidth(double value) {
		_listener.beforeSet(this, WIDTH__PROP, value);
		_width = value;
		_listener.afterChanged(this, WIDTH__PROP);
	}

	@Override
	public final double getHeight() {
		return _height;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	/** Internal setter for {@link #getHeight()} without chain call utility. */
	protected final void internalSetHeight(double value) {
		_listener.beforeSet(this, HEIGHT__PROP, value);
		_height = value;
		_listener.afterChanged(this, HEIGHT__PROP);
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			PARENT__PROP, 
			X__PROP, 
			Y__PROP, 
			WIDTH__PROP, 
			HEIGHT__PROP));

	private static java.util.Set<String> TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(
			java.util.Arrays.asList(
				)));

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
			case PARENT__PROP: return getParent();
			case X__PROP: return getX();
			case Y__PROP: return getY();
			case WIDTH__PROP: return getWidth();
			case HEIGHT__PROP: return getHeight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case X__PROP: internalSetX((double) value); break;
			case Y__PROP: internalSetY((double) value); break;
			case WIDTH__PROP: internalSetWidth((double) value); break;
			case HEIGHT__PROP: internalSetHeight((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(X__PROP);
		out.value(getX());
		out.name(Y__PROP);
		out.value(getY());
		out.name(WIDTH__PROP);
		out.value(getWidth());
		out.name(HEIGHT__PROP);
		out.value(getHeight());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case PARENT__PROP: {
				if (hasParent()) {
					getParent().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case X__PROP: {
				out.value(getX());
				break;
			}
			case Y__PROP: {
				out.value(getY());
				break;
			}
			case WIDTH__PROP: {
				out.value(getWidth());
				break;
			}
			case HEIGHT__PROP: {
				out.value(getHeight());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case X__PROP: setX(in.nextDouble()); break;
			case Y__PROP: setY(in.nextDouble()); break;
			case WIDTH__PROP: setWidth(in.nextDouble()); break;
			case HEIGHT__PROP: setHeight(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public final <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return visit((com.top_logic.graphic.flow.data.Box.Visitor<R,A,E>) v, arg);
	}

}
