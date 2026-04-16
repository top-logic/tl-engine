package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttTick}.
 */
public class GanttTick_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.react.flow.data.GanttTick {

	private double _position = 0.0d;

	private com.top_logic.react.flow.data.Box _label = null;

	private double _emphasis = 0.0d;

	/**
	 * Creates a {@link GanttTick_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttTick#create()
	 */
	public GanttTick_Impl() {
		super();
	}

	@Override
	public final double getPosition() {
		return _position;
	}

	@Override
	public com.top_logic.react.flow.data.GanttTick setPosition(double value) {
		internalSetPosition(value);
		return this;
	}

	/** Internal setter for {@link #getPosition()} without chain call utility. */
	protected final void internalSetPosition(double value) {
		_listener.beforeSet(this, POSITION__PROP, value);
		_position = value;
		_listener.afterChanged(this, POSITION__PROP);
	}

	@Override
	public final com.top_logic.react.flow.data.Box getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.react.flow.data.GanttTick setLabel(com.top_logic.react.flow.data.Box value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(com.top_logic.react.flow.data.Box value) {
		_listener.beforeSet(this, LABEL__PROP, value);
		_label = value;
		_listener.afterChanged(this, LABEL__PROP);
	}

	@Override
	public final boolean hasLabel() {
		return _label != null;
	}

	@Override
	public final double getEmphasis() {
		return _emphasis;
	}

	@Override
	public com.top_logic.react.flow.data.GanttTick setEmphasis(double value) {
		internalSetEmphasis(value);
		return this;
	}

	/** Internal setter for {@link #getEmphasis()} without chain call utility. */
	protected final void internalSetEmphasis(double value) {
		_listener.beforeSet(this, EMPHASIS__PROP, value);
		_emphasis = value;
		_listener.afterChanged(this, EMPHASIS__PROP);
	}

	@Override
	public String jsonType() {
		return GANTT_TICK__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			POSITION__PROP, 
			LABEL__PROP, 
			EMPHASIS__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
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
			case POSITION__PROP: return getPosition();
			case LABEL__PROP: return getLabel();
			case EMPHASIS__PROP: return getEmphasis();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case POSITION__PROP: internalSetPosition((double) value); break;
			case LABEL__PROP: internalSetLabel((com.top_logic.react.flow.data.Box) value); break;
			case EMPHASIS__PROP: internalSetEmphasis((double) value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(POSITION__PROP);
		out.value(getPosition());
		if (hasLabel()) {
			out.name(LABEL__PROP);
			getLabel().writeTo(scope, out);
		}
		out.name(EMPHASIS__PROP);
		out.value(getEmphasis());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case POSITION__PROP: {
				out.value(getPosition());
				break;
			}
			case LABEL__PROP: {
				if (hasLabel()) {
					getLabel().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case EMPHASIS__PROP: {
				out.value(getEmphasis());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case POSITION__PROP: setPosition(in.nextDouble()); break;
			case LABEL__PROP: setLabel(com.top_logic.react.flow.data.Box.readBox(scope, in)); break;
			case EMPHASIS__PROP: setEmphasis(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

}
