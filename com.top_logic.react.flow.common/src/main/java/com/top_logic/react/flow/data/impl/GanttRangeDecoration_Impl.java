package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttRangeDecoration}.
 */
public class GanttRangeDecoration_Impl extends com.top_logic.react.flow.data.impl.GanttDecoration_Impl implements com.top_logic.react.flow.data.GanttRangeDecoration {

	private double _from = 0.0d;

	private double _to = 0.0d;

	private boolean _canResize = false;

	/**
	 * Creates a {@link GanttRangeDecoration_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttRangeDecoration#create()
	 */
	public GanttRangeDecoration_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GANTT_RANGE_DECORATION;
	}

	@Override
	public final double getFrom() {
		return _from;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration setFrom(double value) {
		internalSetFrom(value);
		return this;
	}

	/** Internal setter for {@link #getFrom()} without chain call utility. */
	protected final void internalSetFrom(double value) {
		_listener.beforeSet(this, FROM__PROP, value);
		_from = value;
		_listener.afterChanged(this, FROM__PROP);
	}

	@Override
	public final double getTo() {
		return _to;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration setTo(double value) {
		internalSetTo(value);
		return this;
	}

	/** Internal setter for {@link #getTo()} without chain call utility. */
	protected final void internalSetTo(double value) {
		_listener.beforeSet(this, TO__PROP, value);
		_to = value;
		_listener.afterChanged(this, TO__PROP);
	}

	@Override
	public final boolean isCanResize() {
		return _canResize;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration setCanResize(boolean value) {
		internalSetCanResize(value);
		return this;
	}

	/** Internal setter for {@link #isCanResize()} without chain call utility. */
	protected final void internalSetCanResize(boolean value) {
		_listener.beforeSet(this, CAN_RESIZE__PROP, value);
		_canResize = value;
		_listener.afterChanged(this, CAN_RESIZE__PROP);
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration setColor(String value) {
		internalSetColor(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration setLabel(com.top_logic.react.flow.data.Box value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration setCanMove(boolean value) {
		internalSetCanMove(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration setRelevantFor(java.util.List<? extends String> value) {
		internalSetRelevantFor(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRangeDecoration addRelevantFor(String value) {
		internalAddRelevantFor(value);
		return this;
	}

	@Override
	public String jsonType() {
		return GANTT_RANGE_DECORATION__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			FROM__PROP, 
			TO__PROP, 
			CAN_RESIZE__PROP);
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
			case FROM__PROP: return getFrom();
			case TO__PROP: return getTo();
			case CAN_RESIZE__PROP: return isCanResize();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FROM__PROP: internalSetFrom((double) value); break;
			case TO__PROP: internalSetTo((double) value); break;
			case CAN_RESIZE__PROP: internalSetCanResize((boolean) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(FROM__PROP);
		out.value(getFrom());
		out.name(TO__PROP);
		out.value(getTo());
		out.name(CAN_RESIZE__PROP);
		out.value(isCanResize());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case FROM__PROP: {
				out.value(getFrom());
				break;
			}
			case TO__PROP: {
				out.value(getTo());
				break;
			}
			case CAN_RESIZE__PROP: {
				out.value(isCanResize());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FROM__PROP: setFrom(in.nextDouble()); break;
			case TO__PROP: setTo(in.nextDouble()); break;
			case CAN_RESIZE__PROP: setCanResize(in.nextBoolean()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.GanttDecoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
