package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.ControlState}.
 */
public class ControlState_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.ControlState {

	private boolean _hidden = false;

	private String _cssClass = "";

	/**
	 * Creates a {@link ControlState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.ControlState#create()
	 */
	public ControlState_Impl() {
		super();
	}

	@Override
	public final boolean isHidden() {
		return _hidden;
	}

	@Override
	public com.top_logic.layout.react.state.ControlState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	/** Internal setter for {@link #isHidden()} without chain call utility. */
	protected final void internalSetHidden(boolean value) {
		_hidden = value;
	}

	@Override
	public final String getCssClass() {
		return _cssClass;
	}

	@Override
	public com.top_logic.layout.react.state.ControlState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	/** Internal setter for {@link #getCssClass()} without chain call utility. */
	protected final void internalSetCssClass(String value) {
		_cssClass = value;
	}

	/** The type identifier for this concrete subtype. */
	public String jsonType() {
		return CONTROL_STATE__TYPE;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(HIDDEN__PROP);
		out.value(isHidden());
		out.name(CSS_CLASS__PROP);
		out.value(getCssClass());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case HIDDEN__PROP: setHidden(in.nextBoolean()); break;
			case CSS_CLASS__PROP: setCssClass(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
