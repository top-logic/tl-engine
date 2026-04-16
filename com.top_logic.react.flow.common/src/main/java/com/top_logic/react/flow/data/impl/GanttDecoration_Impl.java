package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttDecoration}.
 */
public abstract class GanttDecoration_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.react.flow.data.GanttDecoration {

	private String _id = "";

	private String _color = "";

	private String _label = "";

	private boolean _canMove = false;

	private final java.util.List<String> _relevantFor = new de.haumacher.msgbuf.util.ReferenceList<String>() {
		@Override
		protected void beforeAdd(int index, String element) {
			_listener.beforeAdd(GanttDecoration_Impl.this, RELEVANT_FOR__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, String element) {
			_listener.afterRemove(GanttDecoration_Impl.this, RELEVANT_FOR__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttDecoration_Impl.this, RELEVANT_FOR__PROP);
		}
	};

	/**
	 * Creates a {@link GanttDecoration_Impl} instance.
	 */
	public GanttDecoration_Impl() {
		super();
	}

	@Override
	public final String getId() {
		return _id;
	}

	@Override
	public com.top_logic.react.flow.data.GanttDecoration setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_listener.beforeSet(this, ID__PROP, value);
		_id = value;
		_listener.afterChanged(this, ID__PROP);
	}

	@Override
	public final String getColor() {
		return _color;
	}

	@Override
	public com.top_logic.react.flow.data.GanttDecoration setColor(String value) {
		internalSetColor(value);
		return this;
	}

	/** Internal setter for {@link #getColor()} without chain call utility. */
	protected final void internalSetColor(String value) {
		_listener.beforeSet(this, COLOR__PROP, value);
		_color = value;
		_listener.afterChanged(this, COLOR__PROP);
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.react.flow.data.GanttDecoration setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_listener.beforeSet(this, LABEL__PROP, value);
		_label = value;
		_listener.afterChanged(this, LABEL__PROP);
	}

	@Override
	public final boolean isCanMove() {
		return _canMove;
	}

	@Override
	public com.top_logic.react.flow.data.GanttDecoration setCanMove(boolean value) {
		internalSetCanMove(value);
		return this;
	}

	/** Internal setter for {@link #isCanMove()} without chain call utility. */
	protected final void internalSetCanMove(boolean value) {
		_listener.beforeSet(this, CAN_MOVE__PROP, value);
		_canMove = value;
		_listener.afterChanged(this, CAN_MOVE__PROP);
	}

	@Override
	public final java.util.List<String> getRelevantFor() {
		return _relevantFor;
	}

	@Override
	public com.top_logic.react.flow.data.GanttDecoration setRelevantFor(java.util.List<? extends String> value) {
		internalSetRelevantFor(value);
		return this;
	}

	/** Internal setter for {@link #getRelevantFor()} without chain call utility. */
	protected final void internalSetRelevantFor(java.util.List<? extends String> value) {
		_relevantFor.clear();
		_relevantFor.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttDecoration addRelevantFor(String value) {
		internalAddRelevantFor(value);
		return this;
	}

	/** Implementation of {@link #addRelevantFor(String)} without chain call utility. */
	protected final void internalAddRelevantFor(String value) {
		_relevantFor.add(value);
	}

	@Override
	public final void removeRelevantFor(String value) {
		_relevantFor.remove(value);
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			COLOR__PROP, 
			LABEL__PROP, 
			CAN_MOVE__PROP, 
			RELEVANT_FOR__PROP);
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
			case ID__PROP: return getId();
			case COLOR__PROP: return getColor();
			case LABEL__PROP: return getLabel();
			case CAN_MOVE__PROP: return isCanMove();
			case RELEVANT_FOR__PROP: return getRelevantFor();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case COLOR__PROP: internalSetColor((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case CAN_MOVE__PROP: internalSetCanMove((boolean) value); break;
			case RELEVANT_FOR__PROP: internalSetRelevantFor(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(ID__PROP);
		out.value(getId());
		out.name(COLOR__PROP);
		out.value(getColor());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(CAN_MOVE__PROP);
		out.value(isCanMove());
		out.name(RELEVANT_FOR__PROP);
		out.beginArray();
		for (String x : getRelevantFor()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: {
				out.value(getId());
				break;
			}
			case COLOR__PROP: {
				out.value(getColor());
				break;
			}
			case LABEL__PROP: {
				out.value(getLabel());
				break;
			}
			case CAN_MOVE__PROP: {
				out.value(isCanMove());
				break;
			}
			case RELEVANT_FOR__PROP: {
				out.beginArray();
				for (String x : getRelevantFor()) {
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
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case COLOR__PROP: setColor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CAN_MOVE__PROP: setCanMove(in.nextBoolean()); break;
			case RELEVANT_FOR__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setRelevantFor(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case RELEVANT_FOR__PROP: {
				out.value(((String) element));
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RELEVANT_FOR__PROP: {
				return de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

}
