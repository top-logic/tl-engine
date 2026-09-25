package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.DatePickerState}.
 */
public class DatePickerState_Impl extends com.top_logic.layout.react.state.impl.FieldState_Impl implements com.top_logic.layout.react.state.DatePickerState {

	private com.top_logic.layout.react.state.DatePickerState.InputType _inputType = com.top_logic.layout.react.state.DatePickerState.InputType.DATE;

	private String _displayValue = "";

	/**
	 * Creates a {@link DatePickerState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.DatePickerState#create()
	 */
	public DatePickerState_Impl() {
		super();
	}

	@Override
	public final com.top_logic.layout.react.state.DatePickerState.InputType getInputType() {
		return _inputType;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setInputType(com.top_logic.layout.react.state.DatePickerState.InputType value) {
		internalSetInputType(value);
		return this;
	}

	/** Internal setter for {@link #getInputType()} without chain call utility. */
	protected final void internalSetInputType(com.top_logic.layout.react.state.DatePickerState.InputType value) {
		if (value == null) throw new IllegalArgumentException("Property 'inputType' cannot be null.");
		_inputType = value;
	}

	@Override
	public final String getDisplayValue() {
		return _displayValue;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setDisplayValue(String value) {
		internalSetDisplayValue(value);
		return this;
	}

	/** Internal setter for {@link #getDisplayValue()} without chain call utility. */
	protected final void internalSetDisplayValue(String value) {
		_displayValue = value;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DatePickerState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DATE_PICKER_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(INPUT_TYPE__PROP);
		getInputType().writeTo(out);
		out.name(DISPLAY_VALUE__PROP);
		out.value(getDisplayValue());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INPUT_TYPE__PROP: setInputType(com.top_logic.layout.react.state.DatePickerState.InputType.readInputType(in)); break;
			case DISPLAY_VALUE__PROP: setDisplayValue(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
