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
		_listener.beforeSet(this, ELEMENT_ID__PROP, value);
		_elementId = value;
		_listener.afterChanged(this, ELEMENT_ID__PROP);
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
		_listener.beforeSet(this, POSITION__PROP, value);
		_position = value;
		_listener.afterChanged(this, POSITION__PROP);
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
		_listener.beforeSet(this, HTML__PROP, value);
		_html = value;
		_listener.afterChanged(this, HTML__PROP);
	}

	@Override
	public String jsonType() {
		return FRAGMENT_INSERTION__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ELEMENT_ID__PROP, 
			POSITION__PROP, 
			HTML__PROP);
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
			case ELEMENT_ID__PROP: return getElementId();
			case POSITION__PROP: return getPosition();
			case HTML__PROP: return getHtml();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ELEMENT_ID__PROP: internalSetElementId((String) value); break;
			case POSITION__PROP: internalSetPosition((String) value); break;
			case HTML__PROP: internalSetHtml((String) value); break;
			default: super.set(field, value); break;
		}
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
