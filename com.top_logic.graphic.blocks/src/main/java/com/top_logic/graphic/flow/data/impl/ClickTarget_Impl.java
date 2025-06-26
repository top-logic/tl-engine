package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.ClickTarget}.
 */
public class ClickTarget_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.ClickTarget {

	private final java.util.List<com.top_logic.graphic.flow.data.MouseButton> _buttons = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.graphic.flow.data.MouseButton>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.MouseButton element) {
			_listener.beforeAdd(ClickTarget_Impl.this, BUTTONS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.MouseButton element) {
			_listener.afterRemove(ClickTarget_Impl.this, BUTTONS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(ClickTarget_Impl.this, BUTTONS__PROP);
		}
	};

	private transient com.top_logic.graphic.flow.callback.ClickHandler _clickHandler = null;

	private transient com.top_logic.graphic.blocks.svg.event.Registration _handlerRegistration = null;

	/**
	 * Creates a {@link ClickTarget_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.ClickTarget#create()
	 */
	public ClickTarget_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.CLICK_TARGET;
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.MouseButton> getButtons() {
		return _buttons;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setButtons(java.util.List<? extends com.top_logic.graphic.flow.data.MouseButton> value) {
		internalSetButtons(value);
		return this;
	}

	/** Internal setter for {@link #getButtons()} without chain call utility. */
	protected final void internalSetButtons(java.util.List<? extends com.top_logic.graphic.flow.data.MouseButton> value) {
		if (value == null) throw new IllegalArgumentException("Property 'buttons' cannot be null.");
		_buttons.clear();
		_buttons.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget addButton(com.top_logic.graphic.flow.data.MouseButton value) {
		internalAddButton(value);
		return this;
	}

	/** Implementation of {@link #addButton(com.top_logic.graphic.flow.data.MouseButton)} without chain call utility. */
	protected final void internalAddButton(com.top_logic.graphic.flow.data.MouseButton value) {
		_buttons.add(value);
	}

	@Override
	public final void removeButton(com.top_logic.graphic.flow.data.MouseButton value) {
		_buttons.remove(value);
	}

	@Override
	public final com.top_logic.graphic.flow.callback.ClickHandler getClickHandler() {
		return _clickHandler;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setClickHandler(com.top_logic.graphic.flow.callback.ClickHandler value) {
		internalSetClickHandler(value);
		return this;
	}

	/** Internal setter for {@link #getClickHandler()} without chain call utility. */
	protected final void internalSetClickHandler(com.top_logic.graphic.flow.callback.ClickHandler value) {
		_listener.beforeSet(this, CLICK_HANDLER__PROP, value);
		_clickHandler = value;
		_listener.afterChanged(this, CLICK_HANDLER__PROP);
	}

	@Override
	public final boolean hasClickHandler() {
		return _clickHandler != null;
	}

	@Override
	public final com.top_logic.graphic.blocks.svg.event.Registration getHandlerRegistration() {
		return _handlerRegistration;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setHandlerRegistration(com.top_logic.graphic.blocks.svg.event.Registration value) {
		internalSetHandlerRegistration(value);
		return this;
	}

	/** Internal setter for {@link #getHandlerRegistration()} without chain call utility. */
	protected final void internalSetHandlerRegistration(com.top_logic.graphic.blocks.svg.event.Registration value) {
		_listener.beforeSet(this, HANDLER_REGISTRATION__PROP, value);
		_handlerRegistration = value;
		_listener.afterChanged(this, HANDLER_REGISTRATION__PROP);
	}

	@Override
	public final boolean hasHandlerRegistration() {
		return _handlerRegistration != null;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ClickTarget setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return CLICK_TARGET__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			BUTTONS__PROP, 
			CLICK_HANDLER__PROP, 
			HANDLER_REGISTRATION__PROP));

	private static java.util.Set<String> TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(
			java.util.Arrays.asList(
				CLICK_HANDLER__PROP, 
				HANDLER_REGISTRATION__PROP)));

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
			case BUTTONS__PROP: return getButtons();
			case CLICK_HANDLER__PROP: return getClickHandler();
			case HANDLER_REGISTRATION__PROP: return getHandlerRegistration();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case BUTTONS__PROP: internalSetButtons(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.MouseButton.class, value)); break;
			case CLICK_HANDLER__PROP: internalSetClickHandler((com.top_logic.graphic.flow.callback.ClickHandler) value); break;
			case HANDLER_REGISTRATION__PROP: internalSetHandlerRegistration((com.top_logic.graphic.blocks.svg.event.Registration) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(BUTTONS__PROP);
		out.beginArray();
		for (com.top_logic.graphic.flow.data.MouseButton x : getButtons()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case BUTTONS__PROP: {
				out.beginArray();
				for (com.top_logic.graphic.flow.data.MouseButton x : getButtons()) {
					x.writeTo(out);
				}
				out.endArray();
				break;
			}
			case CLICK_HANDLER__PROP: {
				if (hasClickHandler()) {
				} else {
					out.nullValue();
				}
				break;
			}
			case HANDLER_REGISTRATION__PROP: {
				if (hasHandlerRegistration()) {
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
			case BUTTONS__PROP: {
				java.util.List<com.top_logic.graphic.flow.data.MouseButton> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.MouseButton.readMouseButton(in));
				}
				in.endArray();
				setButtons(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case BUTTONS__PROP: {
				((com.top_logic.graphic.flow.data.MouseButton) element).writeTo(out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case BUTTONS__PROP: {
				return com.top_logic.graphic.flow.data.MouseButton.readMouseButton(in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
