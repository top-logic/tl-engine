package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.FragmentInsertion}.
 */
public class FragmentInsertion_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.FragmentInsertion {

	private String _elementId = "";

	private String _position = "";

	private String _html = "";

	/**
	 * Creates a {@link FragmentInsertion_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.FragmentInsertion#create()
	 */
	public FragmentInsertion_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.FRAGMENT_INSERTION;
	}

	@Override
	public final String getElementId() {
		return _elementId;
	}

	@Override
	public com.top_logic.layout.react.protocol.FragmentInsertion setElementId(String value) {
		internalSetElementId(value);
		return this;
	}

	/** Internal setter for {@link #getElementId()} without chain call utility. */
	protected final void internalSetElementId(String value) {
		_elementId = value;
	}

	@Override
	public final String getPosition() {
		return _position;
	}

	@Override
	public com.top_logic.layout.react.protocol.FragmentInsertion setPosition(String value) {
		internalSetPosition(value);
		return this;
	}

	/** Internal setter for {@link #getPosition()} without chain call utility. */
	protected final void internalSetPosition(String value) {
		_position = value;
	}

	@Override
	public final String getHtml() {
		return _html;
	}

	@Override
	public com.top_logic.layout.react.protocol.FragmentInsertion setHtml(String value) {
		internalSetHtml(value);
		return this;
	}

	/** Internal setter for {@link #getHtml()} without chain call utility. */
	protected final void internalSetHtml(String value) {
		_html = value;
	}

	@Override
	public String jsonType() {
		return FRAGMENT_INSERTION__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ELEMENT_ID__PROP);
		out.value(getElementId());
		out.name(POSITION__PROP);
		out.value(getPosition());
		out.name(HTML__PROP);
		out.value(getHtml());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ELEMENT_ID__PROP: setElementId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case POSITION__PROP: setPosition(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HTML__PROP: setHtml(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
