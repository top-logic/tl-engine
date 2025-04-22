package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Empty}.
 */
public class Empty_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Empty {

	private double _minWidth = 0.0d;

	private double _minHeight = 0.0d;

	/**
	 * Creates a {@link Empty_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Empty#create()
	 */
	public Empty_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.EMPTY;
	}

	@Override
	public final double getMinWidth() {
		return _minWidth;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setMinWidth(double value) {
		internalSetMinWidth(value);
		return this;
	}

	/** Internal setter for {@link #getMinWidth()} without chain call utility. */
	protected final void internalSetMinWidth(double value) {
		_listener.beforeSet(this, MIN_WIDTH__PROP, value);
		_minWidth = value;
	}

	@Override
	public final double getMinHeight() {
		return _minHeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setMinHeight(double value) {
		internalSetMinHeight(value);
		return this;
	}

	/** Internal setter for {@link #getMinHeight()} without chain call utility. */
	protected final void internalSetMinHeight(double value) {
		_listener.beforeSet(this, MIN_HEIGHT__PROP, value);
		_minHeight = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return EMPTY__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			MIN_WIDTH__PROP, 
			MIN_HEIGHT__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case MIN_WIDTH__PROP: return getMinWidth();
			case MIN_HEIGHT__PROP: return getMinHeight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MIN_WIDTH__PROP: internalSetMinWidth((double) value); break;
			case MIN_HEIGHT__PROP: internalSetMinHeight((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(MIN_WIDTH__PROP);
		out.value(getMinWidth());
		out.name(MIN_HEIGHT__PROP);
		out.value(getMinHeight());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case MIN_WIDTH__PROP: {
				out.value(getMinWidth());
				break;
			}
			case MIN_HEIGHT__PROP: {
				out.value(getMinHeight());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MIN_WIDTH__PROP: setMinWidth(in.nextDouble()); break;
			case MIN_HEIGHT__PROP: setMinHeight(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
