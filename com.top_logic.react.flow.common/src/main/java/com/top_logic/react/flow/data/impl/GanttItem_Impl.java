package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttItem}.
 */
public abstract class GanttItem_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.react.flow.data.GanttItem {

	private String _id = "";

	private String _rowId = "";

	private com.top_logic.react.flow.data.Box _box = null;

	private boolean _canMoveTime = true;

	private boolean _canMoveRow = true;

	private boolean _canBeEdgeSource = true;

	private boolean _canBeEdgeTarget = true;

	/**
	 * Creates a {@link GanttItem_Impl} instance.
	 */
	public GanttItem_Impl() {
		super();
	}

	@Override
	public final String getId() {
		return _id;
	}

	@Override
	public com.top_logic.react.flow.data.GanttItem setId(String value) {
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
	public final String getRowId() {
		return _rowId;
	}

	@Override
	public com.top_logic.react.flow.data.GanttItem setRowId(String value) {
		internalSetRowId(value);
		return this;
	}

	/** Internal setter for {@link #getRowId()} without chain call utility. */
	protected final void internalSetRowId(String value) {
		_listener.beforeSet(this, ROW_ID__PROP, value);
		_rowId = value;
		_listener.afterChanged(this, ROW_ID__PROP);
	}

	@Override
	public final com.top_logic.react.flow.data.Box getBox() {
		return _box;
	}

	@Override
	public com.top_logic.react.flow.data.GanttItem setBox(com.top_logic.react.flow.data.Box value) {
		internalSetBox(value);
		return this;
	}

	/** Internal setter for {@link #getBox()} without chain call utility. */
	protected final void internalSetBox(com.top_logic.react.flow.data.Box value) {
		_listener.beforeSet(this, BOX__PROP, value);
		_box = value;
		_listener.afterChanged(this, BOX__PROP);
	}

	@Override
	public final boolean hasBox() {
		return _box != null;
	}

	@Override
	public final boolean isCanMoveTime() {
		return _canMoveTime;
	}

	@Override
	public com.top_logic.react.flow.data.GanttItem setCanMoveTime(boolean value) {
		internalSetCanMoveTime(value);
		return this;
	}

	/** Internal setter for {@link #isCanMoveTime()} without chain call utility. */
	protected final void internalSetCanMoveTime(boolean value) {
		_listener.beforeSet(this, CAN_MOVE_TIME__PROP, value);
		_canMoveTime = value;
		_listener.afterChanged(this, CAN_MOVE_TIME__PROP);
	}

	@Override
	public final boolean isCanMoveRow() {
		return _canMoveRow;
	}

	@Override
	public com.top_logic.react.flow.data.GanttItem setCanMoveRow(boolean value) {
		internalSetCanMoveRow(value);
		return this;
	}

	/** Internal setter for {@link #isCanMoveRow()} without chain call utility. */
	protected final void internalSetCanMoveRow(boolean value) {
		_listener.beforeSet(this, CAN_MOVE_ROW__PROP, value);
		_canMoveRow = value;
		_listener.afterChanged(this, CAN_MOVE_ROW__PROP);
	}

	@Override
	public final boolean isCanBeEdgeSource() {
		return _canBeEdgeSource;
	}

	@Override
	public com.top_logic.react.flow.data.GanttItem setCanBeEdgeSource(boolean value) {
		internalSetCanBeEdgeSource(value);
		return this;
	}

	/** Internal setter for {@link #isCanBeEdgeSource()} without chain call utility. */
	protected final void internalSetCanBeEdgeSource(boolean value) {
		_listener.beforeSet(this, CAN_BE_EDGE_SOURCE__PROP, value);
		_canBeEdgeSource = value;
		_listener.afterChanged(this, CAN_BE_EDGE_SOURCE__PROP);
	}

	@Override
	public final boolean isCanBeEdgeTarget() {
		return _canBeEdgeTarget;
	}

	@Override
	public com.top_logic.react.flow.data.GanttItem setCanBeEdgeTarget(boolean value) {
		internalSetCanBeEdgeTarget(value);
		return this;
	}

	/** Internal setter for {@link #isCanBeEdgeTarget()} without chain call utility. */
	protected final void internalSetCanBeEdgeTarget(boolean value) {
		_listener.beforeSet(this, CAN_BE_EDGE_TARGET__PROP, value);
		_canBeEdgeTarget = value;
		_listener.afterChanged(this, CAN_BE_EDGE_TARGET__PROP);
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			ROW_ID__PROP, 
			BOX__PROP, 
			CAN_MOVE_TIME__PROP, 
			CAN_MOVE_ROW__PROP, 
			CAN_BE_EDGE_SOURCE__PROP, 
			CAN_BE_EDGE_TARGET__PROP);
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
			case ROW_ID__PROP: return getRowId();
			case BOX__PROP: return getBox();
			case CAN_MOVE_TIME__PROP: return isCanMoveTime();
			case CAN_MOVE_ROW__PROP: return isCanMoveRow();
			case CAN_BE_EDGE_SOURCE__PROP: return isCanBeEdgeSource();
			case CAN_BE_EDGE_TARGET__PROP: return isCanBeEdgeTarget();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case ROW_ID__PROP: internalSetRowId((String) value); break;
			case BOX__PROP: internalSetBox((com.top_logic.react.flow.data.Box) value); break;
			case CAN_MOVE_TIME__PROP: internalSetCanMoveTime((boolean) value); break;
			case CAN_MOVE_ROW__PROP: internalSetCanMoveRow((boolean) value); break;
			case CAN_BE_EDGE_SOURCE__PROP: internalSetCanBeEdgeSource((boolean) value); break;
			case CAN_BE_EDGE_TARGET__PROP: internalSetCanBeEdgeTarget((boolean) value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(ID__PROP);
		out.value(getId());
		out.name(ROW_ID__PROP);
		out.value(getRowId());
		if (hasBox()) {
			out.name(BOX__PROP);
			getBox().writeTo(scope, out);
		}
		out.name(CAN_MOVE_TIME__PROP);
		out.value(isCanMoveTime());
		out.name(CAN_MOVE_ROW__PROP);
		out.value(isCanMoveRow());
		out.name(CAN_BE_EDGE_SOURCE__PROP);
		out.value(isCanBeEdgeSource());
		out.name(CAN_BE_EDGE_TARGET__PROP);
		out.value(isCanBeEdgeTarget());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: {
				out.value(getId());
				break;
			}
			case ROW_ID__PROP: {
				out.value(getRowId());
				break;
			}
			case BOX__PROP: {
				if (hasBox()) {
					getBox().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case CAN_MOVE_TIME__PROP: {
				out.value(isCanMoveTime());
				break;
			}
			case CAN_MOVE_ROW__PROP: {
				out.value(isCanMoveRow());
				break;
			}
			case CAN_BE_EDGE_SOURCE__PROP: {
				out.value(isCanBeEdgeSource());
				break;
			}
			case CAN_BE_EDGE_TARGET__PROP: {
				out.value(isCanBeEdgeTarget());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ROW_ID__PROP: setRowId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case BOX__PROP: setBox(com.top_logic.react.flow.data.Box.readBox(scope, in)); break;
			case CAN_MOVE_TIME__PROP: setCanMoveTime(in.nextBoolean()); break;
			case CAN_MOVE_ROW__PROP: setCanMoveRow(in.nextBoolean()); break;
			case CAN_BE_EDGE_SOURCE__PROP: setCanBeEdgeSource(in.nextBoolean()); break;
			case CAN_BE_EDGE_TARGET__PROP: setCanBeEdgeTarget(in.nextBoolean()); break;
			default: super.readField(scope, in, field);
		}
	}

}
