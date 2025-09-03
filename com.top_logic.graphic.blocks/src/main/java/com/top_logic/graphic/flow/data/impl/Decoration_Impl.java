package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Decoration}.
 */
public abstract class Decoration_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Decoration {

	private com.top_logic.graphic.flow.data.Box _content = null;

	/**
	 * Creates a {@link Decoration_Impl} instance.
	 */
	public Decoration_Impl() {
		super();
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getContent() {
		return _content;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setContent(com.top_logic.graphic.flow.data.Box value) {
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
	public com.top_logic.graphic.flow.data.Decoration setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			CONTENT__PROP));

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
			case CONTENT__PROP: return getContent();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTENT__PROP: internalSetContent((com.top_logic.graphic.flow.data.Box) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasContent()) {
			out.name(CONTENT__PROP);
			getContent().writeTo(scope, out);
		}
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
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
			case CONTENT__PROP: setContent(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public final <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return visit((com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E>) v, arg);
	}

}
