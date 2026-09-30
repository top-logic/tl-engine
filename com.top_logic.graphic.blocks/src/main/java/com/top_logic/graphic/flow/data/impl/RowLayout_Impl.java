package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.RowLayout}.
 */
public abstract class RowLayout_Impl extends com.top_logic.graphic.flow.data.impl.Layout_Impl implements com.top_logic.graphic.flow.data.RowLayout {

	private double _gap = 0.0d;

	private com.top_logic.graphic.flow.data.SpaceDistribution _fill = com.top_logic.graphic.flow.data.SpaceDistribution.NONE;

	/**
	 * Creates a {@link RowLayout_Impl} instance.
	 */
	public RowLayout_Impl() {
		super();
	}

	@Override
	public final double getGap() {
		return _gap;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setGap(double value) {
		internalSetGap(value);
		return this;
	}

	/** Internal setter for {@link #getGap()} without chain call utility. */
	protected final void internalSetGap(double value) {
		_listener.beforeSet(this, GAP__PROP, value);
		_gap = value;
		_listener.afterChanged(this, GAP__PROP);
	}

	@Override
	public final com.top_logic.graphic.flow.data.SpaceDistribution getFill() {
		return _fill;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value) {
		internalSetFill(value);
		return this;
	}

	/** Internal setter for {@link #getFill()} without chain call utility. */
	protected final void internalSetFill(com.top_logic.graphic.flow.data.SpaceDistribution value) {
		if (value == null) throw new IllegalArgumentException("Property 'fill' cannot be null.");
		_listener.beforeSet(this, FILL__PROP, value);
		_fill = value;
		_listener.afterChanged(this, FILL__PROP);
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			GAP__PROP, 
			FILL__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Layout_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Layout_Impl.TRANSIENT_PROPERTIES);
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
			case GAP__PROP: return getGap();
			case FILL__PROP: return getFill();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case GAP__PROP: internalSetGap((double) value); break;
			case FILL__PROP: internalSetFill((com.top_logic.graphic.flow.data.SpaceDistribution) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(GAP__PROP);
		out.value(getGap());
		out.name(FILL__PROP);
		getFill().writeTo(out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case GAP__PROP: {
				out.value(getGap());
				break;
			}
			case FILL__PROP: {
				getFill().writeTo(out);
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case GAP__PROP: setGap(in.nextDouble()); break;
			case FILL__PROP: setFill(com.top_logic.graphic.flow.data.SpaceDistribution.readSpaceDistribution(in)); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public final <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Layout.Visitor<R,A,E> v, A arg) throws E {
		return visit((com.top_logic.graphic.flow.data.RowLayout.Visitor<R,A,E>) v, arg);
	}

}
