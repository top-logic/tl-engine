package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Padding}.
 */
public class Padding_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.Padding {

	private double _top = 0.0d;

	private double _left = 0.0d;

	private double _bottom = 0.0d;

	private double _right = 0.0d;

	/**
	 * Creates a {@link Padding_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Padding#create()
	 */
	public Padding_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.PADDING;
	}

	@Override
	public final double getTop() {
		return _top;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setTop(double value) {
		internalSetTop(value);
		return this;
	}

	/** Internal setter for {@link #getTop()} without chain call utility. */
	protected final void internalSetTop(double value) {
		_listener.beforeSet(this, TOP__PROP, value);
		_top = value;
	}

	@Override
	public final double getLeft() {
		return _left;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setLeft(double value) {
		internalSetLeft(value);
		return this;
	}

	/** Internal setter for {@link #getLeft()} without chain call utility. */
	protected final void internalSetLeft(double value) {
		_listener.beforeSet(this, LEFT__PROP, value);
		_left = value;
	}

	@Override
	public final double getBottom() {
		return _bottom;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setBottom(double value) {
		internalSetBottom(value);
		return this;
	}

	/** Internal setter for {@link #getBottom()} without chain call utility. */
	protected final void internalSetBottom(double value) {
		_listener.beforeSet(this, BOTTOM__PROP, value);
		_bottom = value;
	}

	@Override
	public final double getRight() {
		return _right;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setRight(double value) {
		internalSetRight(value);
		return this;
	}

	/** Internal setter for {@link #getRight()} without chain call utility. */
	protected final void internalSetRight(double value) {
		_listener.beforeSet(this, RIGHT__PROP, value);
		_right = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return PADDING__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			TOP__PROP, 
			LEFT__PROP, 
			BOTTOM__PROP, 
			RIGHT__PROP));

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
			case TOP__PROP: return getTop();
			case LEFT__PROP: return getLeft();
			case BOTTOM__PROP: return getBottom();
			case RIGHT__PROP: return getRight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOP__PROP: internalSetTop((double) value); break;
			case LEFT__PROP: internalSetLeft((double) value); break;
			case BOTTOM__PROP: internalSetBottom((double) value); break;
			case RIGHT__PROP: internalSetRight((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(TOP__PROP);
		out.value(getTop());
		out.name(LEFT__PROP);
		out.value(getLeft());
		out.name(BOTTOM__PROP);
		out.value(getBottom());
		out.name(RIGHT__PROP);
		out.value(getRight());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case TOP__PROP: {
				out.value(getTop());
				break;
			}
			case LEFT__PROP: {
				out.value(getLeft());
				break;
			}
			case BOTTOM__PROP: {
				out.value(getBottom());
				break;
			}
			case RIGHT__PROP: {
				out.value(getRight());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOP__PROP: setTop(in.nextDouble()); break;
			case LEFT__PROP: setLeft(in.nextDouble()); break;
			case BOTTOM__PROP: setBottom(in.nextDouble()); break;
			case RIGHT__PROP: setRight(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
