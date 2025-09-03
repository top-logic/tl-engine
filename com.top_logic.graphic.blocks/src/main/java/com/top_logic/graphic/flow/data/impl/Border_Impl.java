package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Border}.
 */
public class Border_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.Border {

	private String _strokeStyle = "black";

	private double _thickness = 1.0;

	private boolean _top = true;

	private boolean _left = true;

	private boolean _bottom = true;

	private boolean _right = true;

	private final java.util.List<Double> _dashes = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(Border_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(Border_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(Border_Impl.this, DASHES__PROP);
		}
	};

	/**
	 * Creates a {@link Border_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Border#create()
	 */
	public Border_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.BORDER;
	}

	@Override
	public final String getStrokeStyle() {
		return _strokeStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setStrokeStyle(String value) {
		internalSetStrokeStyle(value);
		return this;
	}

	/** Internal setter for {@link #getStrokeStyle()} without chain call utility. */
	protected final void internalSetStrokeStyle(String value) {
		_listener.beforeSet(this, STROKE_STYLE__PROP, value);
		_strokeStyle = value;
		_listener.afterChanged(this, STROKE_STYLE__PROP);
	}

	@Override
	public final double getThickness() {
		return _thickness;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setThickness(double value) {
		internalSetThickness(value);
		return this;
	}

	/** Internal setter for {@link #getThickness()} without chain call utility. */
	protected final void internalSetThickness(double value) {
		_listener.beforeSet(this, THICKNESS__PROP, value);
		_thickness = value;
		_listener.afterChanged(this, THICKNESS__PROP);
	}

	@Override
	public final boolean isTop() {
		return _top;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setTop(boolean value) {
		internalSetTop(value);
		return this;
	}

	/** Internal setter for {@link #isTop()} without chain call utility. */
	protected final void internalSetTop(boolean value) {
		_listener.beforeSet(this, TOP__PROP, value);
		_top = value;
		_listener.afterChanged(this, TOP__PROP);
	}

	@Override
	public final boolean isLeft() {
		return _left;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setLeft(boolean value) {
		internalSetLeft(value);
		return this;
	}

	/** Internal setter for {@link #isLeft()} without chain call utility. */
	protected final void internalSetLeft(boolean value) {
		_listener.beforeSet(this, LEFT__PROP, value);
		_left = value;
		_listener.afterChanged(this, LEFT__PROP);
	}

	@Override
	public final boolean isBottom() {
		return _bottom;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setBottom(boolean value) {
		internalSetBottom(value);
		return this;
	}

	/** Internal setter for {@link #isBottom()} without chain call utility. */
	protected final void internalSetBottom(boolean value) {
		_listener.beforeSet(this, BOTTOM__PROP, value);
		_bottom = value;
		_listener.afterChanged(this, BOTTOM__PROP);
	}

	@Override
	public final boolean isRight() {
		return _right;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setRight(boolean value) {
		internalSetRight(value);
		return this;
	}

	/** Internal setter for {@link #isRight()} without chain call utility. */
	protected final void internalSetRight(boolean value) {
		_listener.beforeSet(this, RIGHT__PROP, value);
		_right = value;
		_listener.afterChanged(this, RIGHT__PROP);
	}

	@Override
	public final java.util.List<Double> getDashes() {
		return _dashes;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setDashes(java.util.List<? extends Double> value) {
		internalSetDashes(value);
		return this;
	}

	/** Internal setter for {@link #getDashes()} without chain call utility. */
	protected final void internalSetDashes(java.util.List<? extends Double> value) {
		_dashes.clear();
		_dashes.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Border addDashe(double value) {
		internalAddDashe(value);
		return this;
	}

	/** Implementation of {@link #addDashe(double)} without chain call utility. */
	protected final void internalAddDashe(double value) {
		_dashes.add(value);
	}

	@Override
	public final void removeDashe(double value) {
		_dashes.remove(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return BORDER__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			STROKE_STYLE__PROP, 
			THICKNESS__PROP, 
			TOP__PROP, 
			LEFT__PROP, 
			BOTTOM__PROP, 
			RIGHT__PROP, 
			DASHES__PROP));

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
			case STROKE_STYLE__PROP: return getStrokeStyle();
			case THICKNESS__PROP: return getThickness();
			case TOP__PROP: return isTop();
			case LEFT__PROP: return isLeft();
			case BOTTOM__PROP: return isBottom();
			case RIGHT__PROP: return isRight();
			case DASHES__PROP: return getDashes();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case STROKE_STYLE__PROP: internalSetStrokeStyle((String) value); break;
			case THICKNESS__PROP: internalSetThickness((double) value); break;
			case TOP__PROP: internalSetTop((boolean) value); break;
			case LEFT__PROP: internalSetLeft((boolean) value); break;
			case BOTTOM__PROP: internalSetBottom((boolean) value); break;
			case RIGHT__PROP: internalSetRight((boolean) value); break;
			case DASHES__PROP: internalSetDashes(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(STROKE_STYLE__PROP);
		out.value(getStrokeStyle());
		out.name(THICKNESS__PROP);
		out.value(getThickness());
		out.name(TOP__PROP);
		out.value(isTop());
		out.name(LEFT__PROP);
		out.value(isLeft());
		out.name(BOTTOM__PROP);
		out.value(isBottom());
		out.name(RIGHT__PROP);
		out.value(isRight());
		out.name(DASHES__PROP);
		out.beginArray();
		for (double x : getDashes()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case STROKE_STYLE__PROP: {
				out.value(getStrokeStyle());
				break;
			}
			case THICKNESS__PROP: {
				out.value(getThickness());
				break;
			}
			case TOP__PROP: {
				out.value(isTop());
				break;
			}
			case LEFT__PROP: {
				out.value(isLeft());
				break;
			}
			case BOTTOM__PROP: {
				out.value(isBottom());
				break;
			}
			case RIGHT__PROP: {
				out.value(isRight());
				break;
			}
			case DASHES__PROP: {
				out.beginArray();
				for (double x : getDashes()) {
					out.value(x);
				}
				out.endArray();
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case STROKE_STYLE__PROP: setStrokeStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case THICKNESS__PROP: setThickness(in.nextDouble()); break;
			case TOP__PROP: setTop(in.nextBoolean()); break;
			case LEFT__PROP: setLeft(in.nextBoolean()); break;
			case BOTTOM__PROP: setBottom(in.nextBoolean()); break;
			case RIGHT__PROP: setRight(in.nextBoolean()); break;
			case DASHES__PROP: {
				java.util.List<Double> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(in.nextDouble());
				}
				in.endArray();
				setDashes(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case DASHES__PROP: {
				out.value(((double) element));
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DASHES__PROP: {
				return in.nextDouble();
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
