package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Align}.
 */
public class Align_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.Align {

	private com.top_logic.graphic.flow.data.Alignment _xAlign = com.top_logic.graphic.flow.data.Alignment.START;

	private com.top_logic.graphic.flow.data.Alignment _yAlign = com.top_logic.graphic.flow.data.Alignment.START;

	/**
	 * Creates a {@link Align_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Align#create()
	 */
	public Align_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ALIGN;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Alignment getXAlign() {
		return _xAlign;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setXAlign(com.top_logic.graphic.flow.data.Alignment value) {
		internalSetXAlign(value);
		return this;
	}

	/** Internal setter for {@link #getXAlign()} without chain call utility. */
	protected final void internalSetXAlign(com.top_logic.graphic.flow.data.Alignment value) {
		if (value == null) throw new IllegalArgumentException("Property 'xAlign' cannot be null.");
		_listener.beforeSet(this, X_ALIGN__PROP, value);
		_xAlign = value;
		_listener.afterChanged(this, X_ALIGN__PROP);
	}

	@Override
	public final com.top_logic.graphic.flow.data.Alignment getYAlign() {
		return _yAlign;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setYAlign(com.top_logic.graphic.flow.data.Alignment value) {
		internalSetYAlign(value);
		return this;
	}

	/** Internal setter for {@link #getYAlign()} without chain call utility. */
	protected final void internalSetYAlign(com.top_logic.graphic.flow.data.Alignment value) {
		if (value == null) throw new IllegalArgumentException("Property 'yAlign' cannot be null.");
		_listener.beforeSet(this, Y_ALIGN__PROP, value);
		_yAlign = value;
		_listener.afterChanged(this, Y_ALIGN__PROP);
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ALIGN__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			X_ALIGN__PROP, 
			Y_ALIGN__PROP));

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
			case X_ALIGN__PROP: return getXAlign();
			case Y_ALIGN__PROP: return getYAlign();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case X_ALIGN__PROP: internalSetXAlign((com.top_logic.graphic.flow.data.Alignment) value); break;
			case Y_ALIGN__PROP: internalSetYAlign((com.top_logic.graphic.flow.data.Alignment) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(X_ALIGN__PROP);
		getXAlign().writeTo(out);
		out.name(Y_ALIGN__PROP);
		getYAlign().writeTo(out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case X_ALIGN__PROP: {
				getXAlign().writeTo(out);
				break;
			}
			case Y_ALIGN__PROP: {
				getYAlign().writeTo(out);
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case X_ALIGN__PROP: setXAlign(com.top_logic.graphic.flow.data.Alignment.readAlignment(in)); break;
			case Y_ALIGN__PROP: setYAlign(com.top_logic.graphic.flow.data.Alignment.readAlignment(in)); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
