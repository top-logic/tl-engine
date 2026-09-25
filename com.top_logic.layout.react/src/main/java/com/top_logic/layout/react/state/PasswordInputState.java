package com.top_logic.layout.react.state;

/**
 * State of a password field, the component {@code TLPasswordInput}.
 *
 * The {@link #getValue()} is the password typed, a string.
 */
public interface PasswordInputState extends com.top_logic.layout.react.state.TypingFieldState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.PasswordInputState} instance.
	 */
	static com.top_logic.layout.react.state.PasswordInputState create() {
		return new com.top_logic.layout.react.state.impl.PasswordInputState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.PasswordInputState} type in JSON format. */
	String PASSWORD_INPUT_STATE__TYPE = "PasswordInputState";

	@Override
	com.top_logic.layout.react.state.PasswordInputState setDebounceMs(long value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setSendValueOnBlur(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setCommitOnBlur(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.PasswordInputState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.PasswordInputState readPasswordInputState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.PasswordInputState_Impl result = new com.top_logic.layout.react.state.impl.PasswordInputState_Impl();
		result.readContent(in);
		return result;
	}

}
