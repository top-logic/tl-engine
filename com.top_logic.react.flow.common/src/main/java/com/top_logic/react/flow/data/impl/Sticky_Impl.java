package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.Sticky}.
 */
public class Sticky_Impl extends com.top_logic.react.flow.data.impl.Decoration_Impl implements com.top_logic.react.flow.data.Sticky {

	private transient double _shift = 0.0d;

	/**
	 * Creates a {@link Sticky_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.Sticky#create()
	 */
	public Sticky_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.STICKY;
	}

	@Override
	public final double getShift() {
		return _shift;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setShift(double value) {
		internalSetShift(value);
		return this;
	}

	/** Internal setter for {@link #getShift()} without chain call utility. */
	protected final void internalSetShift(double value) {
		_listener.beforeSet(this, SHIFT__PROP, value);
		_shift = value;
		_listener.afterChanged(this, SHIFT__PROP);
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setContent(com.top_logic.react.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.Sticky setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return STICKY__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SHIFT__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.Decoration_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.Decoration_Impl.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				SHIFT__PROP));
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
			case SHIFT__PROP: return getShift();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SHIFT__PROP: internalSetShift((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case SHIFT__PROP: {
				out.value(getShift());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
