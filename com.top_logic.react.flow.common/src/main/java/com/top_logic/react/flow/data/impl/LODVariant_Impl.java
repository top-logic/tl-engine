package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.LODVariant}.
 */
public class LODVariant_Impl extends com.top_logic.react.flow.data.impl.Widget_Impl implements com.top_logic.react.flow.data.LODVariant {

	private com.top_logic.react.flow.data.Box _content = null;

	private double _minZoom = 0.0d;

	private double _minWidth = 0.0d;

	private double _minHeight = 0.0d;

	/**
	 * Creates a {@link LODVariant_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.LODVariant#create()
	 */
	public LODVariant_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.LODVARIANT;
	}

	@Override
	public final com.top_logic.react.flow.data.Box getContent() {
		return _content;
	}

	@Override
	public com.top_logic.react.flow.data.LODVariant setContent(com.top_logic.react.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(com.top_logic.react.flow.data.Box value) {
		com.top_logic.react.flow.data.impl.Box_Impl before = (com.top_logic.react.flow.data.impl.Box_Impl) _content;
		com.top_logic.react.flow.data.impl.Box_Impl after = (com.top_logic.react.flow.data.impl.Box_Impl) value;
		if (after != null) {
			com.top_logic.react.flow.data.Widget oldContainer = after.getParent();
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
	public final double getMinZoom() {
		return _minZoom;
	}

	@Override
	public com.top_logic.react.flow.data.LODVariant setMinZoom(double value) {
		internalSetMinZoom(value);
		return this;
	}

	/** Internal setter for {@link #getMinZoom()} without chain call utility. */
	protected final void internalSetMinZoom(double value) {
		_listener.beforeSet(this, MIN_ZOOM__PROP, value);
		_minZoom = value;
		_listener.afterChanged(this, MIN_ZOOM__PROP);
	}

	@Override
	public final double getMinWidth() {
		return _minWidth;
	}

	@Override
	public com.top_logic.react.flow.data.LODVariant setMinWidth(double value) {
		internalSetMinWidth(value);
		return this;
	}

	/** Internal setter for {@link #getMinWidth()} without chain call utility. */
	protected final void internalSetMinWidth(double value) {
		_listener.beforeSet(this, MIN_WIDTH__PROP, value);
		_minWidth = value;
		_listener.afterChanged(this, MIN_WIDTH__PROP);
	}

	@Override
	public final double getMinHeight() {
		return _minHeight;
	}

	@Override
	public com.top_logic.react.flow.data.LODVariant setMinHeight(double value) {
		internalSetMinHeight(value);
		return this;
	}

	/** Internal setter for {@link #getMinHeight()} without chain call utility. */
	protected final void internalSetMinHeight(double value) {
		_listener.beforeSet(this, MIN_HEIGHT__PROP, value);
		_minHeight = value;
		_listener.afterChanged(this, MIN_HEIGHT__PROP);
	}

	@Override
	public com.top_logic.react.flow.data.LODVariant setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LODVariant setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LODVariant setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LODVariant setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return LODVARIANT__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONTENT__PROP, 
			MIN_ZOOM__PROP, 
			MIN_WIDTH__PROP, 
			MIN_HEIGHT__PROP);
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
			case CONTENT__PROP: return getContent();
			case MIN_ZOOM__PROP: return getMinZoom();
			case MIN_WIDTH__PROP: return getMinWidth();
			case MIN_HEIGHT__PROP: return getMinHeight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTENT__PROP: internalSetContent((com.top_logic.react.flow.data.Box) value); break;
			case MIN_ZOOM__PROP: internalSetMinZoom((double) value); break;
			case MIN_WIDTH__PROP: internalSetMinWidth((double) value); break;
			case MIN_HEIGHT__PROP: internalSetMinHeight((double) value); break;
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
		out.name(MIN_ZOOM__PROP);
		out.value(getMinZoom());
		out.name(MIN_WIDTH__PROP);
		out.value(getMinWidth());
		out.name(MIN_HEIGHT__PROP);
		out.value(getMinHeight());
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
			case MIN_ZOOM__PROP: {
				out.value(getMinZoom());
				break;
			}
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
			case CONTENT__PROP: setContent(com.top_logic.react.flow.data.Box.readBox(scope, in)); break;
			case MIN_ZOOM__PROP: setMinZoom(in.nextDouble()); break;
			case MIN_WIDTH__PROP: setMinWidth(in.nextDouble()); break;
			case MIN_HEIGHT__PROP: setMinHeight(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
