package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttEdge}.
 */
public class GanttEdge_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.react.flow.data.GanttEdge {

	private String _id = "";

	private transient java.lang.Object _userObject = null;

	private String _sourceItemId = "";

	private com.top_logic.react.flow.data.GanttEndpoint _sourceEndpoint = com.top_logic.react.flow.data.GanttEndpoint.START;

	private String _targetItemId = "";

	private com.top_logic.react.flow.data.GanttEndpoint _targetEndpoint = com.top_logic.react.flow.data.GanttEndpoint.START;

	private com.top_logic.react.flow.data.GanttEnforce _enforce = com.top_logic.react.flow.data.GanttEnforce.STRICT;

	/**
	 * Creates a {@link GanttEdge_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttEdge#create()
	 */
	public GanttEdge_Impl() {
		super();
	}

	@Override
	public final String getId() {
		return _id;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setId(String value) {
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
	public final java.lang.Object getUserObject() {
		return _userObject;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	/** Internal setter for {@link #getUserObject()} without chain call utility. */
	protected final void internalSetUserObject(java.lang.Object value) {
		_listener.beforeSet(this, USER_OBJECT__PROP, value);
		_userObject = value;
		_listener.afterChanged(this, USER_OBJECT__PROP);
	}

	@Override
	public final boolean hasUserObject() {
		return _userObject != null;
	}

	@Override
	public final String getSourceItemId() {
		return _sourceItemId;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setSourceItemId(String value) {
		internalSetSourceItemId(value);
		return this;
	}

	/** Internal setter for {@link #getSourceItemId()} without chain call utility. */
	protected final void internalSetSourceItemId(String value) {
		_listener.beforeSet(this, SOURCE_ITEM_ID__PROP, value);
		_sourceItemId = value;
		_listener.afterChanged(this, SOURCE_ITEM_ID__PROP);
	}

	@Override
	public final com.top_logic.react.flow.data.GanttEndpoint getSourceEndpoint() {
		return _sourceEndpoint;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setSourceEndpoint(com.top_logic.react.flow.data.GanttEndpoint value) {
		internalSetSourceEndpoint(value);
		return this;
	}

	/** Internal setter for {@link #getSourceEndpoint()} without chain call utility. */
	protected final void internalSetSourceEndpoint(com.top_logic.react.flow.data.GanttEndpoint value) {
		if (value == null) throw new IllegalArgumentException("Property 'sourceEndpoint' cannot be null.");
		_listener.beforeSet(this, SOURCE_ENDPOINT__PROP, value);
		_sourceEndpoint = value;
		_listener.afterChanged(this, SOURCE_ENDPOINT__PROP);
	}

	@Override
	public final String getTargetItemId() {
		return _targetItemId;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setTargetItemId(String value) {
		internalSetTargetItemId(value);
		return this;
	}

	/** Internal setter for {@link #getTargetItemId()} without chain call utility. */
	protected final void internalSetTargetItemId(String value) {
		_listener.beforeSet(this, TARGET_ITEM_ID__PROP, value);
		_targetItemId = value;
		_listener.afterChanged(this, TARGET_ITEM_ID__PROP);
	}

	@Override
	public final com.top_logic.react.flow.data.GanttEndpoint getTargetEndpoint() {
		return _targetEndpoint;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setTargetEndpoint(com.top_logic.react.flow.data.GanttEndpoint value) {
		internalSetTargetEndpoint(value);
		return this;
	}

	/** Internal setter for {@link #getTargetEndpoint()} without chain call utility. */
	protected final void internalSetTargetEndpoint(com.top_logic.react.flow.data.GanttEndpoint value) {
		if (value == null) throw new IllegalArgumentException("Property 'targetEndpoint' cannot be null.");
		_listener.beforeSet(this, TARGET_ENDPOINT__PROP, value);
		_targetEndpoint = value;
		_listener.afterChanged(this, TARGET_ENDPOINT__PROP);
	}

	@Override
	public final com.top_logic.react.flow.data.GanttEnforce getEnforce() {
		return _enforce;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setEnforce(com.top_logic.react.flow.data.GanttEnforce value) {
		internalSetEnforce(value);
		return this;
	}

	/** Internal setter for {@link #getEnforce()} without chain call utility. */
	protected final void internalSetEnforce(com.top_logic.react.flow.data.GanttEnforce value) {
		if (value == null) throw new IllegalArgumentException("Property 'enforce' cannot be null.");
		_listener.beforeSet(this, ENFORCE__PROP, value);
		_enforce = value;
		_listener.afterChanged(this, ENFORCE__PROP);
	}

	@Override
	public String jsonType() {
		return GANTT_EDGE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			USER_OBJECT__PROP, 
			SOURCE_ITEM_ID__PROP, 
			SOURCE_ENDPOINT__PROP, 
			TARGET_ITEM_ID__PROP, 
			TARGET_ENDPOINT__PROP, 
			ENFORCE__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList(
				USER_OBJECT__PROP));
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
			case USER_OBJECT__PROP: return getUserObject();
			case SOURCE_ITEM_ID__PROP: return getSourceItemId();
			case SOURCE_ENDPOINT__PROP: return getSourceEndpoint();
			case TARGET_ITEM_ID__PROP: return getTargetItemId();
			case TARGET_ENDPOINT__PROP: return getTargetEndpoint();
			case ENFORCE__PROP: return getEnforce();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case USER_OBJECT__PROP: internalSetUserObject((java.lang.Object) value); break;
			case SOURCE_ITEM_ID__PROP: internalSetSourceItemId((String) value); break;
			case SOURCE_ENDPOINT__PROP: internalSetSourceEndpoint((com.top_logic.react.flow.data.GanttEndpoint) value); break;
			case TARGET_ITEM_ID__PROP: internalSetTargetItemId((String) value); break;
			case TARGET_ENDPOINT__PROP: internalSetTargetEndpoint((com.top_logic.react.flow.data.GanttEndpoint) value); break;
			case ENFORCE__PROP: internalSetEnforce((com.top_logic.react.flow.data.GanttEnforce) value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(ID__PROP);
		out.value(getId());
		out.name(SOURCE_ITEM_ID__PROP);
		out.value(getSourceItemId());
		out.name(SOURCE_ENDPOINT__PROP);
		getSourceEndpoint().writeTo(out);
		out.name(TARGET_ITEM_ID__PROP);
		out.value(getTargetItemId());
		out.name(TARGET_ENDPOINT__PROP);
		getTargetEndpoint().writeTo(out);
		out.name(ENFORCE__PROP);
		getEnforce().writeTo(out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: {
				out.value(getId());
				break;
			}
			case USER_OBJECT__PROP: {
				if (hasUserObject()) {
				} else {
					out.nullValue();
				}
				break;
			}
			case SOURCE_ITEM_ID__PROP: {
				out.value(getSourceItemId());
				break;
			}
			case SOURCE_ENDPOINT__PROP: {
				getSourceEndpoint().writeTo(out);
				break;
			}
			case TARGET_ITEM_ID__PROP: {
				out.value(getTargetItemId());
				break;
			}
			case TARGET_ENDPOINT__PROP: {
				getTargetEndpoint().writeTo(out);
				break;
			}
			case ENFORCE__PROP: {
				getEnforce().writeTo(out);
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SOURCE_ITEM_ID__PROP: setSourceItemId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SOURCE_ENDPOINT__PROP: setSourceEndpoint(com.top_logic.react.flow.data.GanttEndpoint.readGanttEndpoint(in)); break;
			case TARGET_ITEM_ID__PROP: setTargetItemId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TARGET_ENDPOINT__PROP: setTargetEndpoint(com.top_logic.react.flow.data.GanttEndpoint.readGanttEndpoint(in)); break;
			case ENFORCE__PROP: setEnforce(com.top_logic.react.flow.data.GanttEnforce.readGanttEnforce(in)); break;
			default: super.readField(scope, in, field);
		}
	}

}
