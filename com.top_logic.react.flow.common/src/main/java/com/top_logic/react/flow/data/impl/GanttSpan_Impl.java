package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttSpan}.
 */
public class GanttSpan_Impl extends com.top_logic.react.flow.data.impl.GanttItem_Impl implements com.top_logic.react.flow.data.GanttSpan {

	private double _start = 0.0d;

	private double _end = 0.0d;

	private boolean _canResizeStart = true;

	private boolean _canResizeEnd = true;

	/**
	 * Creates a {@link GanttSpan_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttSpan#create()
	 */
	public GanttSpan_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GANTT_SPAN;
	}

	@Override
	public final double getStart() {
		return _start;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setStart(double value) {
		internalSetStart(value);
		return this;
	}

	/** Internal setter for {@link #getStart()} without chain call utility. */
	protected final void internalSetStart(double value) {
		_listener.beforeSet(this, START__PROP, value);
		_start = value;
		_listener.afterChanged(this, START__PROP);
	}

	@Override
	public final double getEnd() {
		return _end;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setEnd(double value) {
		internalSetEnd(value);
		return this;
	}

	/** Internal setter for {@link #getEnd()} without chain call utility. */
	protected final void internalSetEnd(double value) {
		_listener.beforeSet(this, END__PROP, value);
		_end = value;
		_listener.afterChanged(this, END__PROP);
	}

	@Override
	public final boolean isCanResizeStart() {
		return _canResizeStart;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setCanResizeStart(boolean value) {
		internalSetCanResizeStart(value);
		return this;
	}

	/** Internal setter for {@link #isCanResizeStart()} without chain call utility. */
	protected final void internalSetCanResizeStart(boolean value) {
		_listener.beforeSet(this, CAN_RESIZE_START__PROP, value);
		_canResizeStart = value;
		_listener.afterChanged(this, CAN_RESIZE_START__PROP);
	}

	@Override
	public final boolean isCanResizeEnd() {
		return _canResizeEnd;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setCanResizeEnd(boolean value) {
		internalSetCanResizeEnd(value);
		return this;
	}

	/** Internal setter for {@link #isCanResizeEnd()} without chain call utility. */
	protected final void internalSetCanResizeEnd(boolean value) {
		_listener.beforeSet(this, CAN_RESIZE_END__PROP, value);
		_canResizeEnd = value;
		_listener.afterChanged(this, CAN_RESIZE_END__PROP);
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setRowId(String value) {
		internalSetRowId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setBox(com.top_logic.react.flow.data.Box value) {
		internalSetBox(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setCanMoveTime(boolean value) {
		internalSetCanMoveTime(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setCanMoveRow(boolean value) {
		internalSetCanMoveRow(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setCanBeEdgeSource(boolean value) {
		internalSetCanBeEdgeSource(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttSpan setCanBeEdgeTarget(boolean value) {
		internalSetCanBeEdgeTarget(value);
		return this;
	}

	@Override
	public String jsonType() {
		return GANTT_SPAN__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			START__PROP, 
			END__PROP, 
			CAN_RESIZE_START__PROP, 
			CAN_RESIZE_END__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.GanttItem_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.GanttItem_Impl.TRANSIENT_PROPERTIES);
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
			case START__PROP: return getStart();
			case END__PROP: return getEnd();
			case CAN_RESIZE_START__PROP: return isCanResizeStart();
			case CAN_RESIZE_END__PROP: return isCanResizeEnd();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case START__PROP: internalSetStart((double) value); break;
			case END__PROP: internalSetEnd((double) value); break;
			case CAN_RESIZE_START__PROP: internalSetCanResizeStart((boolean) value); break;
			case CAN_RESIZE_END__PROP: internalSetCanResizeEnd((boolean) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(START__PROP);
		out.value(getStart());
		out.name(END__PROP);
		out.value(getEnd());
		out.name(CAN_RESIZE_START__PROP);
		out.value(isCanResizeStart());
		out.name(CAN_RESIZE_END__PROP);
		out.value(isCanResizeEnd());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case START__PROP: {
				out.value(getStart());
				break;
			}
			case END__PROP: {
				out.value(getEnd());
				break;
			}
			case CAN_RESIZE_START__PROP: {
				out.value(isCanResizeStart());
				break;
			}
			case CAN_RESIZE_END__PROP: {
				out.value(isCanResizeEnd());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case START__PROP: setStart(in.nextDouble()); break;
			case END__PROP: setEnd(in.nextDouble()); break;
			case CAN_RESIZE_START__PROP: setCanResizeStart(in.nextBoolean()); break;
			case CAN_RESIZE_END__PROP: setCanResizeEnd(in.nextBoolean()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.GanttItem.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
