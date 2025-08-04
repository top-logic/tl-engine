package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.PolygonalChain}.
 */
public class PolygonalChain_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.PolygonalChain {

	private final java.util.List<com.top_logic.graphic.flow.data.Point> _points = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.graphic.flow.data.Point>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.Point element) {
			_listener.beforeAdd(PolygonalChain_Impl.this, POINTS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.Point element) {
			_listener.afterRemove(PolygonalChain_Impl.this, POINTS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(PolygonalChain_Impl.this, POINTS__PROP);
		}
	};

	private boolean _closed = false;

	private String _strokeStyle = null;

	private String _fillStyle = null;

	private double _thickness = 1.0;

	private final java.util.List<Double> _dashes = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(PolygonalChain_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(PolygonalChain_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(PolygonalChain_Impl.this, DASHES__PROP);
		}
	};

	/**
	 * Creates a {@link PolygonalChain_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.PolygonalChain#create()
	 */
	public PolygonalChain_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.POLYGONAL_CHAIN;
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.Point> getPoints() {
		return _points;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setPoints(java.util.List<? extends com.top_logic.graphic.flow.data.Point> value) {
		internalSetPoints(value);
		return this;
	}

	/** Internal setter for {@link #getPoints()} without chain call utility. */
	protected final void internalSetPoints(java.util.List<? extends com.top_logic.graphic.flow.data.Point> value) {
		if (value == null) throw new IllegalArgumentException("Property 'points' cannot be null.");
		_points.clear();
		_points.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain addPoint(com.top_logic.graphic.flow.data.Point value) {
		internalAddPoint(value);
		return this;
	}

	/** Implementation of {@link #addPoint(com.top_logic.graphic.flow.data.Point)} without chain call utility. */
	protected final void internalAddPoint(com.top_logic.graphic.flow.data.Point value) {
		_points.add(value);
	}

	@Override
	public final void removePoint(com.top_logic.graphic.flow.data.Point value) {
		_points.remove(value);
	}

	@Override
	public final boolean isClosed() {
		return _closed;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setClosed(boolean value) {
		internalSetClosed(value);
		return this;
	}

	/** Internal setter for {@link #isClosed()} without chain call utility. */
	protected final void internalSetClosed(boolean value) {
		_listener.beforeSet(this, CLOSED__PROP, value);
		_closed = value;
		_listener.afterChanged(this, CLOSED__PROP);
	}

	@Override
	public final String getStrokeStyle() {
		return _strokeStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setStrokeStyle(String value) {
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
	public final boolean hasStrokeStyle() {
		return _strokeStyle != null;
	}

	@Override
	public final String getFillStyle() {
		return _fillStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setFillStyle(String value) {
		internalSetFillStyle(value);
		return this;
	}

	/** Internal setter for {@link #getFillStyle()} without chain call utility. */
	protected final void internalSetFillStyle(String value) {
		_listener.beforeSet(this, FILL_STYLE__PROP, value);
		_fillStyle = value;
		_listener.afterChanged(this, FILL_STYLE__PROP);
	}

	@Override
	public final boolean hasFillStyle() {
		return _fillStyle != null;
	}

	@Override
	public final double getThickness() {
		return _thickness;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setThickness(double value) {
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
	public final java.util.List<Double> getDashes() {
		return _dashes;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setDashes(java.util.List<? extends Double> value) {
		internalSetDashes(value);
		return this;
	}

	/** Internal setter for {@link #getDashes()} without chain call utility. */
	protected final void internalSetDashes(java.util.List<? extends Double> value) {
		_dashes.clear();
		_dashes.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain addDashe(double value) {
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
	public com.top_logic.graphic.flow.data.PolygonalChain setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.PolygonalChain setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return POLYGONAL_CHAIN__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			POINTS__PROP, 
			CLOSED__PROP, 
			STROKE_STYLE__PROP, 
			FILL_STYLE__PROP, 
			THICKNESS__PROP, 
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
			case POINTS__PROP: return getPoints();
			case CLOSED__PROP: return isClosed();
			case STROKE_STYLE__PROP: return getStrokeStyle();
			case FILL_STYLE__PROP: return getFillStyle();
			case THICKNESS__PROP: return getThickness();
			case DASHES__PROP: return getDashes();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case POINTS__PROP: internalSetPoints(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.Point.class, value)); break;
			case CLOSED__PROP: internalSetClosed((boolean) value); break;
			case STROKE_STYLE__PROP: internalSetStrokeStyle((String) value); break;
			case FILL_STYLE__PROP: internalSetFillStyle((String) value); break;
			case THICKNESS__PROP: internalSetThickness((double) value); break;
			case DASHES__PROP: internalSetDashes(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(POINTS__PROP);
		out.beginArray();
		for (com.top_logic.graphic.flow.data.Point x : getPoints()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(CLOSED__PROP);
		out.value(isClosed());
		if (hasStrokeStyle()) {
			out.name(STROKE_STYLE__PROP);
			out.value(getStrokeStyle());
		}
		if (hasFillStyle()) {
			out.name(FILL_STYLE__PROP);
			out.value(getFillStyle());
		}
		out.name(THICKNESS__PROP);
		out.value(getThickness());
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
			case POINTS__PROP: {
				out.beginArray();
				for (com.top_logic.graphic.flow.data.Point x : getPoints()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case CLOSED__PROP: {
				out.value(isClosed());
				break;
			}
			case STROKE_STYLE__PROP: {
				if (hasStrokeStyle()) {
					out.value(getStrokeStyle());
				} else {
					out.nullValue();
				}
				break;
			}
			case FILL_STYLE__PROP: {
				if (hasFillStyle()) {
					out.value(getFillStyle());
				} else {
					out.nullValue();
				}
				break;
			}
			case THICKNESS__PROP: {
				out.value(getThickness());
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
			case POINTS__PROP: {
				java.util.List<com.top_logic.graphic.flow.data.Point> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.Point.readPoint(scope, in));
				}
				in.endArray();
				setPoints(newValue);
			}
			break;
			case CLOSED__PROP: setClosed(in.nextBoolean()); break;
			case STROKE_STYLE__PROP: setStrokeStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FILL_STYLE__PROP: setFillStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case THICKNESS__PROP: setThickness(in.nextDouble()); break;
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
			case POINTS__PROP: {
				((com.top_logic.graphic.flow.data.Point) element).writeTo(scope, out);
				break;
			}
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
			case POINTS__PROP: {
				return com.top_logic.graphic.flow.data.Point.readPoint(scope, in);
			}
			case DASHES__PROP: {
				return in.nextDouble();
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
