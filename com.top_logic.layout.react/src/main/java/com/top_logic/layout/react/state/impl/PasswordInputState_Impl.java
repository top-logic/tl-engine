package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.PasswordInputState}.
 */
public class PasswordInputState_Impl extends com.top_logic.layout.react.state.impl.TypingFieldState_Impl implements com.top_logic.layout.react.state.PasswordInputState {

	/**
	 * Creates a {@link PasswordInputState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.PasswordInputState#create()
	 */
	public PasswordInputState_Impl() {
		super();
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setDebounceMs(long value) {
		internalSetDebounceMs(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setSendValueOnBlur(boolean value) {
		internalSetSendValueOnBlur(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setCommitOnBlur(boolean value) {
		internalSetCommitOnBlur(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PasswordInputState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return PASSWORD_INPUT_STATE__TYPE;
	}

}
