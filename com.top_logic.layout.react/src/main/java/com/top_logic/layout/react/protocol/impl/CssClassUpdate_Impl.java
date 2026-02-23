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
		_listener.beforeSet(this, ELEMENT_ID__PROP, value);
		_elementId = value;
		_listener.afterChanged(this, ELEMENT_ID__PROP);
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
		_listener.beforeSet(this, CSS_CLASS__PROP, value);
		_cssClass = value;
		_listener.afterChanged(this, CSS_CLASS__PROP);
	}

	@Override
	public String jsonType() {
		return CSS_CLASS_UPDATE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ELEMENT_ID__PROP, 
			CSS_CLASS__PROP);
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
			case CSS_CLASS__PROP: return getCssClass();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ELEMENT_ID__PROP: internalSetElementId((String) value); break;
			case CSS_CLASS__PROP: internalSetCssClass((String) value); break;
			default: super.set(field, value); break;
		}
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
