package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.JSSnipplet}.
 */
public class JSSnipplet_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.JSSnipplet {

	private String _code = "";

	/**
	 * Creates a {@link JSSnipplet_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.JSSnipplet#create()
	 */
	public JSSnipplet_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.JSSNIPPLET;
	}

	@Override
	public final String getCode() {
		return _code;
	}

	@Override
	public com.top_logic.layout.react.protocol.JSSnipplet setCode(String value) {
		internalSetCode(value);
		return this;
	}

	/** Internal setter for {@link #getCode()} without chain call utility. */
	protected final void internalSetCode(String value) {
		_code = value;
	}

	@Override
	public String jsonType() {
		return JSSNIPPLET__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CODE__PROP);
		out.value(getCode());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CODE__PROP: setCode(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
