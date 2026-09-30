package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Point}.
 */
public class Point_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.graphic.flow.data.Point {

	private double _x = 0.0d;

	private double _y = 0.0d;

	/**
	 * Creates a {@link Point_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Point#create()
	 */
	public Point_Impl() {
		super();
	}

	@Override
	public final double getX() {
		return _x;
	}

	@Override
	public com.top_logic.graphic.flow.data.Point setX(double value) {
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
	public com.top_logic.graphic.flow.data.Point setY(double value) {
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
	public String jsonType() {
		return POINT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			X__PROP, 
			Y__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
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
			case X__PROP: return getX();
			case Y__PROP: return getY();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case X__PROP: internalSetX((double) value); break;
			case Y__PROP: internalSetY((double) value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(X__PROP);
		out.value(getX());
		out.name(Y__PROP);
		out.value(getY());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case X__PROP: {
				out.value(getX());
				break;
			}
			case Y__PROP: {
				out.value(getY());
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
			default: super.readField(scope, in, field);
		}
	}

}
