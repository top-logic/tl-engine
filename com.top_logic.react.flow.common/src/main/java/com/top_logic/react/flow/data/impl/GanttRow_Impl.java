package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttRow}.
 */
public class GanttRow_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.react.flow.data.GanttRow {

	private String _id = "";

	private String _label = "";

	private final java.util.List<com.top_logic.react.flow.data.GanttRow> _children = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.GanttRow>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.GanttRow element) {
			_listener.beforeAdd(GanttRow_Impl.this, CHILDREN__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.GanttRow element) {
			_listener.afterRemove(GanttRow_Impl.this, CHILDREN__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttRow_Impl.this, CHILDREN__PROP);
		}
	};

	/**
	 * Creates a {@link GanttRow_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttRow#create()
	 */
	public GanttRow_Impl() {
		super();
	}

	@Override
	public final String getId() {
		return _id;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setId(String value) {
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
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setLabel(String value) {
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
	public final java.util.List<com.top_logic.react.flow.data.GanttRow> getChildren() {
		return _children;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setChildren(java.util.List<? extends com.top_logic.react.flow.data.GanttRow> value) {
		internalSetChildren(value);
		return this;
	}

	/** Internal setter for {@link #getChildren()} without chain call utility. */
	protected final void internalSetChildren(java.util.List<? extends com.top_logic.react.flow.data.GanttRow> value) {
		if (value == null) throw new IllegalArgumentException("Property 'children' cannot be null.");
		_children.clear();
		_children.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow addChildren(com.top_logic.react.flow.data.GanttRow value) {
		internalAddChildren(value);
		return this;
	}

	/** Implementation of {@link #addChildren(com.top_logic.react.flow.data.GanttRow)} without chain call utility. */
	protected final void internalAddChildren(com.top_logic.react.flow.data.GanttRow value) {
		_children.add(value);
	}

	@Override
	public final void removeChildren(com.top_logic.react.flow.data.GanttRow value) {
		_children.remove(value);
	}

	@Override
	public String jsonType() {
		return GANTT_ROW__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			LABEL__PROP, 
			CHILDREN__PROP);
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
			case LABEL__PROP: return getLabel();
			case CHILDREN__PROP: return getChildren();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case CHILDREN__PROP: internalSetChildren(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GanttRow.class, value)); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(ID__PROP);
		out.value(getId());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(CHILDREN__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GanttRow x : getChildren()) {
			x.writeTo(scope, out);
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
			case LABEL__PROP: {
				out.value(getLabel());
				break;
			}
			case CHILDREN__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.GanttRow x : getChildren()) {
					x.writeTo(scope, out);
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
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CHILDREN__PROP: {
				java.util.List<com.top_logic.react.flow.data.GanttRow> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.GanttRow.readGanttRow(scope, in));
				}
				in.endArray();
				setChildren(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case CHILDREN__PROP: {
				((com.top_logic.react.flow.data.GanttRow) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CHILDREN__PROP: {
				return com.top_logic.react.flow.data.GanttRow.readGanttRow(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

}
