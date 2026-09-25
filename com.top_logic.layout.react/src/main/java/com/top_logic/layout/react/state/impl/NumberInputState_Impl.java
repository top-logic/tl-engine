package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.NumberInputState}.
 */
public class NumberInputState_Impl extends com.top_logic.layout.react.state.impl.TypingFieldState_Impl implements com.top_logic.layout.react.state.NumberInputState {

	private com.top_logic.layout.react.state.NumberInputState.InputMode _inputMode = com.top_logic.layout.react.state.NumberInputState.InputMode.NUMERIC;

	/**
	 * Creates a {@link NumberInputState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.NumberInputState#create()
	 */
	public NumberInputState_Impl() {
		super();
	}

	@Override
	public final com.top_logic.layout.react.state.NumberInputState.InputMode getInputMode() {
		return _inputMode;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setInputMode(com.top_logic.layout.react.state.NumberInputState.InputMode value) {
		internalSetInputMode(value);
		return this;
	}

	/** Internal setter for {@link #getInputMode()} without chain call utility. */
	protected final void internalSetInputMode(com.top_logic.layout.react.state.NumberInputState.InputMode value) {
		if (value == null) throw new IllegalArgumentException("Property 'inputMode' cannot be null.");
		_inputMode = value;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setDebounceMs(long value) {
		internalSetDebounceMs(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setSendValueOnBlur(boolean value) {
		internalSetSendValueOnBlur(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setCommitOnBlur(boolean value) {
		internalSetCommitOnBlur(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.NumberInputState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return NUMBER_INPUT_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(INPUT_MODE__PROP);
		getInputMode().writeTo(out);
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INPUT_MODE__PROP: setInputMode(com.top_logic.layout.react.state.NumberInputState.InputMode.readInputMode(in)); break;
			default: super.readField(in, field);
		}
	}

}
