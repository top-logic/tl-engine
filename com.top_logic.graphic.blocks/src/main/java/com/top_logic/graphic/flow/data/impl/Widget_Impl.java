package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Widget}.
 */
public abstract class Widget_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.graphic.flow.data.Widget {

	private String _cssClass = null;

	private transient java.lang.Object _userObject = null;

	private transient String _clientId = null;

	/**
	 * Creates a {@link Widget_Impl} instance.
	 */
	public Widget_Impl() {
		super();
	}

	@Override
	public final String getCssClass() {
		return _cssClass;
	}

	@Override
	public com.top_logic.graphic.flow.data.Widget setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	/** Internal setter for {@link #getCssClass()} without chain call utility. */
	protected final void internalSetCssClass(String value) {
		_listener.beforeSet(this, CSS_CLASS__PROP, value);
		_cssClass = value;
	}

	@Override
	public final boolean hasCssClass() {
		return _cssClass != null;
	}

	@Override
	public final java.lang.Object getUserObject() {
		return _userObject;
	}

	@Override
	public com.top_logic.graphic.flow.data.Widget setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	/** Internal setter for {@link #getUserObject()} without chain call utility. */
	protected final void internalSetUserObject(java.lang.Object value) {
		_listener.beforeSet(this, USER_OBJECT__PROP, value);
		_userObject = value;
	}

	@Override
	public final boolean hasUserObject() {
		return _userObject != null;
	}

	@Override
	public final String getClientId() {
		return _clientId;
	}

	@Override
	public com.top_logic.graphic.flow.data.Widget setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	/** Internal setter for {@link #getClientId()} without chain call utility. */
	protected final void internalSetClientId(String value) {
		_listener.beforeSet(this, CLIENT_ID__PROP, value);
		_clientId = value;
	}

	@Override
	public final boolean hasClientId() {
		return _clientId != null;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			CSS_CLASS__PROP, 
			USER_OBJECT__PROP, 
			CLIENT_ID__PROP));

	private static java.util.Set<String> TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(
			java.util.Arrays.asList(
				USER_OBJECT__PROP, 
				CLIENT_ID__PROP)));

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
			case CSS_CLASS__PROP: return getCssClass();
			case USER_OBJECT__PROP: return getUserObject();
			case CLIENT_ID__PROP: return getClientId();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CSS_CLASS__PROP: internalSetCssClass((String) value); break;
			case USER_OBJECT__PROP: internalSetUserObject((java.lang.Object) value); break;
			case CLIENT_ID__PROP: internalSetClientId((String) value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasCssClass()) {
			out.name(CSS_CLASS__PROP);
			out.value(getCssClass());
		}
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case CSS_CLASS__PROP: {
				if (hasCssClass()) {
					out.value(getCssClass());
				} else {
					out.nullValue();
				}
				break;
			}
			case USER_OBJECT__PROP: {
				if (hasUserObject()) {
				} else {
					out.nullValue();
				}
				break;
			}
			case CLIENT_ID__PROP: {
				if (hasClientId()) {
					out.value(getClientId());
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
			case CSS_CLASS__PROP: setCssClass(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(scope, in, field);
		}
	}

}
