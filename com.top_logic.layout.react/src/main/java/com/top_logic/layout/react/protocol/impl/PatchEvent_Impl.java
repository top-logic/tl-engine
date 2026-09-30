package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.PatchEvent}.
 */
public class PatchEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.PatchEvent {

	private String _controlId = "";

	private String _patch = "";

	/**
	 * Creates a {@link PatchEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.PatchEvent#create()
	 */
	public PatchEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.PATCH_EVENT;
	}

	@Override
	public final String getControlId() {
		return _controlId;
	}

	@Override
	public com.top_logic.layout.react.protocol.PatchEvent setControlId(String value) {
		internalSetControlId(value);
		return this;
	}

	/** Internal setter for {@link #getControlId()} without chain call utility. */
	protected final void internalSetControlId(String value) {
		_controlId = value;
	}

	@Override
	public final String getPatch() {
		return _patch;
	}

	@Override
	public com.top_logic.layout.react.protocol.PatchEvent setPatch(String value) {
		internalSetPatch(value);
		return this;
	}

	/** Internal setter for {@link #getPatch()} without chain call utility. */
	protected final void internalSetPatch(String value) {
		_patch = value;
	}

	@Override
	public String jsonType() {
		return PATCH_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CONTROL_ID__PROP);
		out.value(getControlId());
		out.name(PATCH__PROP);
		out.value(getPatch());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTROL_ID__PROP: setControlId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PATCH__PROP: setPatch(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
