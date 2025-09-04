package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.EdgeDecoration}.
 */
public class EdgeDecoration_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.EdgeDecoration {

	private double _linePosition = 0.0d;

	private com.top_logic.graphic.flow.data.OffsetPosition _offsetPosition = com.top_logic.graphic.flow.data.OffsetPosition.CENTER;

	private com.top_logic.graphic.flow.data.Box _content = null;

	/**
	 * Creates a {@link EdgeDecoration_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.EdgeDecoration#create()
	 */
	public EdgeDecoration_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.EDGE_DECORATION;
	}

	@Override
	public final double getLinePosition() {
		return _linePosition;
	}

	@Override
	public com.top_logic.graphic.flow.data.EdgeDecoration setLinePosition(double value) {
		internalSetLinePosition(value);
		return this;
	}

	/** Internal setter for {@link #getLinePosition()} without chain call utility. */
	protected final void internalSetLinePosition(double value) {
		_listener.beforeSet(this, LINE_POSITION__PROP, value);
		_linePosition = value;
		_listener.afterChanged(this, LINE_POSITION__PROP);
	}

	@Override
	public final com.top_logic.graphic.flow.data.OffsetPosition getOffsetPosition() {
		return _offsetPosition;
	}

	@Override
	public com.top_logic.graphic.flow.data.EdgeDecoration setOffsetPosition(com.top_logic.graphic.flow.data.OffsetPosition value) {
		internalSetOffsetPosition(value);
		return this;
	}

	/** Internal setter for {@link #getOffsetPosition()} without chain call utility. */
	protected final void internalSetOffsetPosition(com.top_logic.graphic.flow.data.OffsetPosition value) {
		if (value == null) throw new IllegalArgumentException("Property 'offsetPosition' cannot be null.");
		_listener.beforeSet(this, OFFSET_POSITION__PROP, value);
		_offsetPosition = value;
		_listener.afterChanged(this, OFFSET_POSITION__PROP);
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getContent() {
		return _content;
	}

	@Override
	public com.top_logic.graphic.flow.data.EdgeDecoration setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(com.top_logic.graphic.flow.data.Box value) {
		com.top_logic.graphic.flow.data.impl.Box_Impl before = (com.top_logic.graphic.flow.data.impl.Box_Impl) _content;
		com.top_logic.graphic.flow.data.impl.Box_Impl after = (com.top_logic.graphic.flow.data.impl.Box_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.Widget oldContainer = after.getParent();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, CONTENT__PROP, value);
		if (before != null) {
			before.internalSetParent(null);
		}
		_content = value;
		if (after != null) {
			after.internalSetParent(this);
		}
		_listener.afterChanged(this, CONTENT__PROP);
	}

	@Override
	public final boolean hasContent() {
		return _content != null;
	}

	@Override
	public com.top_logic.graphic.flow.data.EdgeDecoration setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.EdgeDecoration setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.EdgeDecoration setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.EdgeDecoration setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return EDGE_DECORATION__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			LINE_POSITION__PROP, 
			OFFSET_POSITION__PROP, 
			CONTENT__PROP);
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
			case LINE_POSITION__PROP: return getLinePosition();
			case OFFSET_POSITION__PROP: return getOffsetPosition();
			case CONTENT__PROP: return getContent();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case LINE_POSITION__PROP: internalSetLinePosition((double) value); break;
			case OFFSET_POSITION__PROP: internalSetOffsetPosition((com.top_logic.graphic.flow.data.OffsetPosition) value); break;
			case CONTENT__PROP: internalSetContent((com.top_logic.graphic.flow.data.Box) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(LINE_POSITION__PROP);
		out.value(getLinePosition());
		out.name(OFFSET_POSITION__PROP);
		getOffsetPosition().writeTo(out);
		if (hasContent()) {
			out.name(CONTENT__PROP);
			getContent().writeTo(scope, out);
		}
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case LINE_POSITION__PROP: {
				out.value(getLinePosition());
				break;
			}
			case OFFSET_POSITION__PROP: {
				getOffsetPosition().writeTo(out);
				break;
			}
			case CONTENT__PROP: {
				if (hasContent()) {
					getContent().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LINE_POSITION__PROP: setLinePosition(in.nextDouble()); break;
			case OFFSET_POSITION__PROP: setOffsetPosition(com.top_logic.graphic.flow.data.OffsetPosition.readOffsetPosition(in)); break;
			case CONTENT__PROP: setContent(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
