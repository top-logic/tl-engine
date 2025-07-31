package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Sized}.
 */
public class Sized_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.Sized {

	private Double _minWidth = null;

	private Double _maxWidth = null;

	private Double _minHeight = null;

	private Double _maxHeight = null;

	/**
	 * Creates a {@link Sized_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Sized#create()
	 */
	public Sized_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.SIZED;
	}

	@Override
	public final Double getMinWidth() {
		return _minWidth;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setMinWidth(Double value) {
		internalSetMinWidth(value);
		return this;
	}

	/** Internal setter for {@link #getMinWidth()} without chain call utility. */
	protected final void internalSetMinWidth(Double value) {
		_listener.beforeSet(this, MIN_WIDTH__PROP, value);
		_minWidth = value;
		_listener.afterChanged(this, MIN_WIDTH__PROP);
	}

	@Override
	public final boolean hasMinWidth() {
		return _minWidth != null;
	}

	@Override
	public final Double getMaxWidth() {
		return _maxWidth;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setMaxWidth(Double value) {
		internalSetMaxWidth(value);
		return this;
	}

	/** Internal setter for {@link #getMaxWidth()} without chain call utility. */
	protected final void internalSetMaxWidth(Double value) {
		_listener.beforeSet(this, MAX_WIDTH__PROP, value);
		_maxWidth = value;
		_listener.afterChanged(this, MAX_WIDTH__PROP);
	}

	@Override
	public final boolean hasMaxWidth() {
		return _maxWidth != null;
	}

	@Override
	public final Double getMinHeight() {
		return _minHeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setMinHeight(Double value) {
		internalSetMinHeight(value);
		return this;
	}

	/** Internal setter for {@link #getMinHeight()} without chain call utility. */
	protected final void internalSetMinHeight(Double value) {
		_listener.beforeSet(this, MIN_HEIGHT__PROP, value);
		_minHeight = value;
		_listener.afterChanged(this, MIN_HEIGHT__PROP);
	}

	@Override
	public final boolean hasMinHeight() {
		return _minHeight != null;
	}

	@Override
	public final Double getMaxHeight() {
		return _maxHeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setMaxHeight(Double value) {
		internalSetMaxHeight(value);
		return this;
	}

	/** Internal setter for {@link #getMaxHeight()} without chain call utility. */
	protected final void internalSetMaxHeight(Double value) {
		_listener.beforeSet(this, MAX_HEIGHT__PROP, value);
		_maxHeight = value;
		_listener.afterChanged(this, MAX_HEIGHT__PROP);
	}

	@Override
	public final boolean hasMaxHeight() {
		return _maxHeight != null;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Sized setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return SIZED__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			MIN_WIDTH__PROP, 
			MAX_WIDTH__PROP, 
			MIN_HEIGHT__PROP, 
			MAX_HEIGHT__PROP));

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
			case MIN_WIDTH__PROP: return getMinWidth();
			case MAX_WIDTH__PROP: return getMaxWidth();
			case MIN_HEIGHT__PROP: return getMinHeight();
			case MAX_HEIGHT__PROP: return getMaxHeight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MIN_WIDTH__PROP: internalSetMinWidth((Double) value); break;
			case MAX_WIDTH__PROP: internalSetMaxWidth((Double) value); break;
			case MIN_HEIGHT__PROP: internalSetMinHeight((Double) value); break;
			case MAX_HEIGHT__PROP: internalSetMaxHeight((Double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasMinWidth()) {
			out.name(MIN_WIDTH__PROP);
			out.value(getMinWidth());
		}
		if (hasMaxWidth()) {
			out.name(MAX_WIDTH__PROP);
			out.value(getMaxWidth());
		}
		if (hasMinHeight()) {
			out.name(MIN_HEIGHT__PROP);
			out.value(getMinHeight());
		}
		if (hasMaxHeight()) {
			out.name(MAX_HEIGHT__PROP);
			out.value(getMaxHeight());
		}
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case MIN_WIDTH__PROP: {
				if (hasMinWidth()) {
					out.value(getMinWidth());
				} else {
					out.nullValue();
				}
				break;
			}
			case MAX_WIDTH__PROP: {
				if (hasMaxWidth()) {
					out.value(getMaxWidth());
				} else {
					out.nullValue();
				}
				break;
			}
			case MIN_HEIGHT__PROP: {
				if (hasMinHeight()) {
					out.value(getMinHeight());
				} else {
					out.nullValue();
				}
				break;
			}
			case MAX_HEIGHT__PROP: {
				if (hasMaxHeight()) {
					out.value(getMaxHeight());
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
			case MIN_WIDTH__PROP: setMinWidth(in.nextDouble()); break;
			case MAX_WIDTH__PROP: setMaxWidth(in.nextDouble()); break;
			case MIN_HEIGHT__PROP: setMinHeight(in.nextDouble()); break;
			case MAX_HEIGHT__PROP: setMaxHeight(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
