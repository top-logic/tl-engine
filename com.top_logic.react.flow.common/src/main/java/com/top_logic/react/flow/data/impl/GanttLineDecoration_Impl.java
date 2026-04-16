package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttLineDecoration}.
 */
public class GanttLineDecoration_Impl extends com.top_logic.react.flow.data.impl.GanttDecoration_Impl implements com.top_logic.react.flow.data.GanttLineDecoration {

	private double _at = 0.0d;

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
			AT__PROP);
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
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case AT__PROP: internalSetAt((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(AT__PROP);
		out.value(getAt());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case AT__PROP: {
				out.value(getAt());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case AT__PROP: setAt(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.GanttDecoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
