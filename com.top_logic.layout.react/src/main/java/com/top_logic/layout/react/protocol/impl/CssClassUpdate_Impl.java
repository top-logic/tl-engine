package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.CssClassUpdate}.
 */
public class CssClassUpdate_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.CssClassUpdate {

	private String _elementId = "";

	private String _cssClass = "";

	/**
	 * Creates a {@link CssClassUpdate_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.CssClassUpdate#create()
	 */
	public CssClassUpdate_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.CSS_CLASS_UPDATE;
	}

	@Override
	public final String getElementId() {
		return _elementId;
	}

	@Override
	public com.top_logic.layout.react.protocol.CssClassUpdate setElementId(String value) {
		internalSetElementId(value);
		return this;
	}

	/** Internal setter for {@link #getElementId()} without chain call utility. */
	protected final void internalSetElementId(String value) {
		_elementId = value;
	}

	@Override
	public final String getCssClass() {
		return _cssClass;
	}

	@Override
	public com.top_logic.layout.react.protocol.CssClassUpdate setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	/** Internal setter for {@link #getCssClass()} without chain call utility. */
	protected final void internalSetCssClass(String value) {
		_cssClass = value;
	}

	@Override
	public String jsonType() {
		return CSS_CLASS_UPDATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ELEMENT_ID__PROP);
		out.value(getElementId());
		out.name(CSS_CLASS__PROP);
		out.value(getCssClass());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ELEMENT_ID__PROP: setElementId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CSS_CLASS__PROP: setCssClass(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
