package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.DropRegion}.
 */
public class DropRegion_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.DropRegion {

	private transient com.top_logic.graphic.flow.callback.DropHandler _dropHandler = null;

	private transient com.top_logic.graphic.blocks.svg.event.Registration _handlerRegistration = null;

	/**
	 * Creates a {@link DropRegion_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.DropRegion#create()
	 */
	public DropRegion_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.DROP_REGION;
	}

	@Override
	public final com.top_logic.graphic.flow.callback.DropHandler getDropHandler() {
		return _dropHandler;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setDropHandler(com.top_logic.graphic.flow.callback.DropHandler value) {
		internalSetDropHandler(value);
		return this;
	}

	/** Internal setter for {@link #getDropHandler()} without chain call utility. */
	protected final void internalSetDropHandler(com.top_logic.graphic.flow.callback.DropHandler value) {
		_listener.beforeSet(this, DROP_HANDLER__PROP, value);
		_dropHandler = value;
		_listener.afterChanged(this, DROP_HANDLER__PROP);
	}

	@Override
	public final boolean hasDropHandler() {
		return _dropHandler != null;
	}

	@Override
	public final com.top_logic.graphic.blocks.svg.event.Registration getHandlerRegistration() {
		return _handlerRegistration;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setHandlerRegistration(com.top_logic.graphic.blocks.svg.event.Registration value) {
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
	public com.top_logic.graphic.flow.data.DropRegion setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.DropRegion setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DROP_REGION__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			DROP_HANDLER__PROP, 
			HANDLER_REGISTRATION__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Decoration_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Decoration_Impl.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				DROP_HANDLER__PROP, 
				HANDLER_REGISTRATION__PROP));
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
			case DROP_HANDLER__PROP: return getDropHandler();
			case HANDLER_REGISTRATION__PROP: return getHandlerRegistration();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case DROP_HANDLER__PROP: internalSetDropHandler((com.top_logic.graphic.flow.callback.DropHandler) value); break;
			case HANDLER_REGISTRATION__PROP: internalSetHandlerRegistration((com.top_logic.graphic.blocks.svg.event.Registration) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case DROP_HANDLER__PROP: {
				if (hasDropHandler()) {
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
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
