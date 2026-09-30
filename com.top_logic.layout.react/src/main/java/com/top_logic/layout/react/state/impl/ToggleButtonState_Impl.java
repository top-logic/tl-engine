package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.ToggleButtonState}.
 */
public class ToggleButtonState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.ToggleButtonState {

	private String _label = "";

	private boolean _active = false;

	/**
	 * Creates a {@link ToggleButtonState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.ToggleButtonState#create()
	 */
	public ToggleButtonState_Impl() {
		super();
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.ToggleButtonState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public final boolean isActive() {
		return _active;
	}

	@Override
	public com.top_logic.layout.react.state.ToggleButtonState setActive(boolean value) {
		internalSetActive(value);
		return this;
	}

	/** Internal setter for {@link #isActive()} without chain call utility. */
	protected final void internalSetActive(boolean value) {
		_active = value;
	}

	@Override
	public com.top_logic.layout.react.state.ToggleButtonState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ToggleButtonState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TOGGLE_BUTTON_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(ACTIVE__PROP);
		out.value(isActive());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ACTIVE__PROP: setActive(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
