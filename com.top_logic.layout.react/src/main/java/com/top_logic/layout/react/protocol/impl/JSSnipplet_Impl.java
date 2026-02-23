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
		_listener.beforeSet(this, CODE__PROP, value);
		_code = value;
		_listener.afterChanged(this, CODE__PROP);
	}

	@Override
	public String jsonType() {
		return JSSNIPPLET__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CODE__PROP);
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
			case CODE__PROP: return getCode();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CODE__PROP: internalSetCode((String) value); break;
			default: super.set(field, value); break;
		}
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
