package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.CheckboxState}.
 */
public class CheckboxState_Impl extends com.top_logic.layout.react.state.impl.FieldState_Impl implements com.top_logic.layout.react.state.CheckboxState {

	private boolean _triState = false;

	private com.top_logic.layout.react.state.CheckboxState.Display _display = com.top_logic.layout.react.state.CheckboxState.Display.CHECKBOX;

	/**
	 * Creates a {@link CheckboxState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.CheckboxState#create()
	 */
	public CheckboxState_Impl() {
		super();
	}

	@Override
	public final boolean isTriState() {
		return _triState;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setTriState(boolean value) {
		internalSetTriState(value);
		return this;
	}

	/** Internal setter for {@link #isTriState()} without chain call utility. */
	protected final void internalSetTriState(boolean value) {
		_triState = value;
	}

	@Override
	public final com.top_logic.layout.react.state.CheckboxState.Display getDisplay() {
		return _display;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setDisplay(com.top_logic.layout.react.state.CheckboxState.Display value) {
		internalSetDisplay(value);
		return this;
	}

	/** Internal setter for {@link #getDisplay()} without chain call utility. */
	protected final void internalSetDisplay(com.top_logic.layout.react.state.CheckboxState.Display value) {
		if (value == null) throw new IllegalArgumentException("Property 'display' cannot be null.");
		_display = value;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CheckboxState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return CHECKBOX_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TRI_STATE__PROP);
		out.value(isTriState());
		out.name(DISPLAY__PROP);
		getDisplay().writeTo(out);
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TRI_STATE__PROP: setTriState(in.nextBoolean()); break;
			case DISPLAY__PROP: setDisplay(com.top_logic.layout.react.state.CheckboxState.Display.readDisplay(in)); break;
			default: super.readField(in, field);
		}
	}

}
