package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.SelectableBox}.
 */
public class SelectableBox_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.SelectableBox {

	private boolean _selected = false;

	private transient com.top_logic.graphic.blocks.svg.event.Registration _clickHandler = null;

	/**
	 * Creates a {@link SelectableBox_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.SelectableBox#create()
	 */
	public SelectableBox_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.SELECTABLE_BOX;
	}

	@Override
	public final boolean isSelected() {
		return _selected;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setSelected(boolean value) {
		internalSetSelected(value);
		return this;
	}

	/** Internal setter for {@link #isSelected()} without chain call utility. */
	protected final void internalSetSelected(boolean value) {
		_listener.beforeSet(this, SELECTED__PROP, value);
		_selected = value;
	}

	@Override
	public final com.top_logic.graphic.blocks.svg.event.Registration getClickHandler() {
		return _clickHandler;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setClickHandler(com.top_logic.graphic.blocks.svg.event.Registration value) {
		internalSetClickHandler(value);
		return this;
	}

	/** Internal setter for {@link #getClickHandler()} without chain call utility. */
	protected final void internalSetClickHandler(com.top_logic.graphic.blocks.svg.event.Registration value) {
		_listener.beforeSet(this, CLICK_HANDLER__PROP, value);
		_clickHandler = value;
	}

	@Override
	public final boolean hasClickHandler() {
		return _clickHandler != null;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.SelectableBox setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return SELECTABLE_BOX__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			SELECTED__PROP, 
			CLICK_HANDLER__PROP));

	private static java.util.Set<String> TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(
			java.util.Arrays.asList(
				CLICK_HANDLER__PROP)));

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
			case SELECTED__PROP: return isSelected();
			case CLICK_HANDLER__PROP: return getClickHandler();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SELECTED__PROP: internalSetSelected((boolean) value); break;
			case CLICK_HANDLER__PROP: internalSetClickHandler((com.top_logic.graphic.blocks.svg.event.Registration) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(SELECTED__PROP);
		out.value(isSelected());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case SELECTED__PROP: {
				out.value(isSelected());
				break;
			}
			case CLICK_HANDLER__PROP: {
				if (hasClickHandler()) {
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
			case SELECTED__PROP: setSelected(in.nextBoolean()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
