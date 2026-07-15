package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.ElementReplacement}.
 */
public class ElementReplacement_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.ElementReplacement {

	private String _elementId = "";

	private String _html = "";

	/**
	 * Creates a {@link ElementReplacement_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.ElementReplacement#create()
	 */
	public ElementReplacement_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ELEMENT_REPLACEMENT;
	}

	@Override
	public final String getElementId() {
		return _elementId;
	}

	@Override
	public com.top_logic.layout.react.protocol.ElementReplacement setElementId(String value) {
		internalSetElementId(value);
		return this;
	}

	/** Internal setter for {@link #getElementId()} without chain call utility. */
	protected final void internalSetElementId(String value) {
		_elementId = value;
	}

	@Override
	public final String getHtml() {
		return _html;
	}

	@Override
	public com.top_logic.layout.react.protocol.ElementReplacement setHtml(String value) {
		internalSetHtml(value);
		return this;
	}

	/** Internal setter for {@link #getHtml()} without chain call utility. */
	protected final void internalSetHtml(String value) {
		_html = value;
	}

	@Override
	public String jsonType() {
		return ELEMENT_REPLACEMENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ELEMENT_ID__PROP);
		out.value(getElementId());
		out.name(HTML__PROP);
		out.value(getHtml());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ELEMENT_ID__PROP: setElementId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HTML__PROP: setHtml(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
