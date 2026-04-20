package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttLineDecoration}.
 */
public class GanttLineDecoration_Impl extends com.top_logic.react.flow.data.impl.GanttDecoration_Impl implements com.top_logic.react.flow.data.GanttLineDecoration {

	private double _at = 0.0d;

	private double _strokeWidth = 1.0;

	private final java.util.List<Double> _dashes = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(GanttLineDecoration_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(GanttLineDecoration_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttLineDecoration_Impl.this, DASHES__PROP);
		}
	};

	/**
	 * Creates a {@link GanttLineDecoration_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttLineDecoration#create()
	 */
	public GanttLineDecoration_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GANTT_LINE_DECORATION;
	}

	@Override
	public final double getAt() {
		return _at;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setAt(double value) {
		internalSetAt(value);
		return this;
	}

	/** Internal setter for {@link #getAt()} without chain call utility. */
	protected final void internalSetAt(double value) {
		_listener.beforeSet(this, AT__PROP, value);
		_at = value;
		_listener.afterChanged(this, AT__PROP);
	}

	@Override
	public final double getStrokeWidth() {
		return _strokeWidth;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setStrokeWidth(double value) {
		internalSetStrokeWidth(value);
		return this;
	}

	/** Internal setter for {@link #getStrokeWidth()} without chain call utility. */
	protected final void internalSetStrokeWidth(double value) {
		_listener.beforeSet(this, STROKE_WIDTH__PROP, value);
		_strokeWidth = value;
		_listener.afterChanged(this, STROKE_WIDTH__PROP);
	}

	@Override
	public final java.util.List<Double> getDashes() {
		return _dashes;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setDashes(java.util.List<? extends Double> value) {
		internalSetDashes(value);
		return this;
	}

	/** Internal setter for {@link #getDashes()} without chain call utility. */
	protected final void internalSetDashes(java.util.List<? extends Double> value) {
		_dashes.clear();
		_dashes.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration addDash(double value) {
		internalAddDash(value);
		return this;
	}

	/** Implementation of {@link #addDash(double)} without chain call utility. */
	protected final void internalAddDash(double value) {
		_dashes.add(value);
	}

	@Override
	public final void removeDash(double value) {
		_dashes.remove(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setRelevantForModels(java.util.List<? extends java.lang.Object> value) {
		internalSetRelevantForModels(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration addRelevantForModel(java.lang.Object value) {
		internalAddRelevantForModel(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setColor(String value) {
		internalSetColor(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setLabel(com.top_logic.react.flow.data.Box value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setCanMove(boolean value) {
		internalSetCanMove(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration setRelevantFor(java.util.List<? extends String> value) {
		internalSetRelevantFor(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLineDecoration addRelevantFor(String value) {
		internalAddRelevantFor(value);
		return this;
	}

	@Override
	public String jsonType() {
		return GANTT_LINE_DECORATION__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			AT__PROP, 
			STROKE_WIDTH__PROP, 
			DASHES__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.GanttDecoration_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.GanttDecoration_Impl.TRANSIENT_PROPERTIES);
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
			case AT__PROP: return getAt();
			case STROKE_WIDTH__PROP: return getStrokeWidth();
			case DASHES__PROP: return getDashes();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case AT__PROP: internalSetAt((double) value); break;
			case STROKE_WIDTH__PROP: internalSetStrokeWidth((double) value); break;
			case DASHES__PROP: internalSetDashes(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(AT__PROP);
		out.value(getAt());
		out.name(STROKE_WIDTH__PROP);
		out.value(getStrokeWidth());
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
			case AT__PROP: {
				out.value(getAt());
				break;
			}
			case STROKE_WIDTH__PROP: {
				out.value(getStrokeWidth());
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
			case AT__PROP: setAt(in.nextDouble()); break;
			case STROKE_WIDTH__PROP: setStrokeWidth(in.nextDouble()); break;
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
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.GanttDecoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
