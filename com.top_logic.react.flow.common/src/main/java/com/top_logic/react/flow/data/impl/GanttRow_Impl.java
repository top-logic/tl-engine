package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttRow}.
 */
public class GanttRow_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.react.flow.data.GanttRow {

	private String _id = "";

	private transient java.lang.Object _userObject = null;

	private com.top_logic.react.flow.data.Box _label = null;

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

	private boolean _acceptsDrop = true;

	private double _rowPadding = 4.0;

	private double _minContentHeight = 24.0;

	private String _backgroundColor = null;

	private String _borderColor = null;

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
	public final java.lang.Object getUserObject() {
		return _userObject;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setUserObject(java.lang.Object value) {
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
	public final com.top_logic.react.flow.data.Box getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setLabel(com.top_logic.react.flow.data.Box value) {
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
	public final boolean isAcceptsDrop() {
		return _acceptsDrop;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setAcceptsDrop(boolean value) {
		internalSetAcceptsDrop(value);
		return this;
	}

	/** Internal setter for {@link #isAcceptsDrop()} without chain call utility. */
	protected final void internalSetAcceptsDrop(boolean value) {
		_listener.beforeSet(this, ACCEPTS_DROP__PROP, value);
		_acceptsDrop = value;
		_listener.afterChanged(this, ACCEPTS_DROP__PROP);
	}

	@Override
	public final double getRowPadding() {
		return _rowPadding;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setRowPadding(double value) {
		internalSetRowPadding(value);
		return this;
	}

	/** Internal setter for {@link #getRowPadding()} without chain call utility. */
	protected final void internalSetRowPadding(double value) {
		_listener.beforeSet(this, ROW_PADDING__PROP, value);
		_rowPadding = value;
		_listener.afterChanged(this, ROW_PADDING__PROP);
	}

	@Override
	public final double getMinContentHeight() {
		return _minContentHeight;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setMinContentHeight(double value) {
		internalSetMinContentHeight(value);
		return this;
	}

	/** Internal setter for {@link #getMinContentHeight()} without chain call utility. */
	protected final void internalSetMinContentHeight(double value) {
		_listener.beforeSet(this, MIN_CONTENT_HEIGHT__PROP, value);
		_minContentHeight = value;
		_listener.afterChanged(this, MIN_CONTENT_HEIGHT__PROP);
	}

	@Override
	public final String getBackgroundColor() {
		return _backgroundColor;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setBackgroundColor(String value) {
		internalSetBackgroundColor(value);
		return this;
	}

	/** Internal setter for {@link #getBackgroundColor()} without chain call utility. */
	protected final void internalSetBackgroundColor(String value) {
		_listener.beforeSet(this, BACKGROUND_COLOR__PROP, value);
		_backgroundColor = value;
		_listener.afterChanged(this, BACKGROUND_COLOR__PROP);
	}

	@Override
	public final boolean hasBackgroundColor() {
		return _backgroundColor != null;
	}

	@Override
	public final String getBorderColor() {
		return _borderColor;
	}

	@Override
	public com.top_logic.react.flow.data.GanttRow setBorderColor(String value) {
		internalSetBorderColor(value);
		return this;
	}

	/** Internal setter for {@link #getBorderColor()} without chain call utility. */
	protected final void internalSetBorderColor(String value) {
		_listener.beforeSet(this, BORDER_COLOR__PROP, value);
		_borderColor = value;
		_listener.afterChanged(this, BORDER_COLOR__PROP);
	}

	@Override
	public final boolean hasBorderColor() {
		return _borderColor != null;
	}

	@Override
	public String jsonType() {
		return GANTT_ROW__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			USER_OBJECT__PROP, 
			LABEL__PROP, 
			CHILDREN__PROP, 
			ACCEPTS_DROP__PROP, 
			ROW_PADDING__PROP, 
			MIN_CONTENT_HEIGHT__PROP, 
			BACKGROUND_COLOR__PROP, 
			BORDER_COLOR__PROP);
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
			case LABEL__PROP: return getLabel();
			case CHILDREN__PROP: return getChildren();
			case ACCEPTS_DROP__PROP: return isAcceptsDrop();
			case ROW_PADDING__PROP: return getRowPadding();
			case MIN_CONTENT_HEIGHT__PROP: return getMinContentHeight();
			case BACKGROUND_COLOR__PROP: return getBackgroundColor();
			case BORDER_COLOR__PROP: return getBorderColor();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case USER_OBJECT__PROP: internalSetUserObject((java.lang.Object) value); break;
			case LABEL__PROP: internalSetLabel((com.top_logic.react.flow.data.Box) value); break;
			case CHILDREN__PROP: internalSetChildren(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GanttRow.class, value)); break;
			case ACCEPTS_DROP__PROP: internalSetAcceptsDrop((boolean) value); break;
			case ROW_PADDING__PROP: internalSetRowPadding((double) value); break;
			case MIN_CONTENT_HEIGHT__PROP: internalSetMinContentHeight((double) value); break;
			case BACKGROUND_COLOR__PROP: internalSetBackgroundColor((String) value); break;
			case BORDER_COLOR__PROP: internalSetBorderColor((String) value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(ID__PROP);
		out.value(getId());
		if (hasLabel()) {
			out.name(LABEL__PROP);
			getLabel().writeTo(scope, out);
		}
		out.name(CHILDREN__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GanttRow x : getChildren()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(ACCEPTS_DROP__PROP);
		out.value(isAcceptsDrop());
		out.name(ROW_PADDING__PROP);
		out.value(getRowPadding());
		out.name(MIN_CONTENT_HEIGHT__PROP);
		out.value(getMinContentHeight());
		if (hasBackgroundColor()) {
			out.name(BACKGROUND_COLOR__PROP);
			out.value(getBackgroundColor());
		}
		if (hasBorderColor()) {
			out.name(BORDER_COLOR__PROP);
			out.value(getBorderColor());
		}
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
			case LABEL__PROP: {
				if (hasLabel()) {
					getLabel().writeTo(scope, out);
				} else {
					out.nullValue();
				}
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
			case ACCEPTS_DROP__PROP: {
				out.value(isAcceptsDrop());
				break;
			}
			case ROW_PADDING__PROP: {
				out.value(getRowPadding());
				break;
			}
			case MIN_CONTENT_HEIGHT__PROP: {
				out.value(getMinContentHeight());
				break;
			}
			case BACKGROUND_COLOR__PROP: {
				if (hasBackgroundColor()) {
					out.value(getBackgroundColor());
				} else {
					out.nullValue();
				}
				break;
			}
			case BORDER_COLOR__PROP: {
				if (hasBorderColor()) {
					out.value(getBorderColor());
				} else {
					out.nullValue();
				}
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(com.top_logic.react.flow.data.Box.readBox(scope, in)); break;
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
			case ACCEPTS_DROP__PROP: setAcceptsDrop(in.nextBoolean()); break;
			case ROW_PADDING__PROP: setRowPadding(in.nextDouble()); break;
			case MIN_CONTENT_HEIGHT__PROP: setMinContentHeight(in.nextDouble()); break;
			case BACKGROUND_COLOR__PROP: setBackgroundColor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case BORDER_COLOR__PROP: setBorderColor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
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
