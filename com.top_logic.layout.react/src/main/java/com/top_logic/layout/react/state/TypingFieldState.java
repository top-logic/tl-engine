package com.top_logic.layout.react.state;

/**
 * State properties of a field the user types a value into.
 */
public interface TypingFieldState extends com.top_logic.layout.react.state.FieldState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.TypingFieldState} instance.
	 */
	static com.top_logic.layout.react.state.TypingFieldState create() {
		return new com.top_logic.layout.react.state.impl.TypingFieldState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.TypingFieldState} type in JSON format. */
	String TYPING_FIELD_STATE__TYPE = "TypingFieldState";

	/** @see #getDebounceMs() */
	String DEBOUNCE_MS__PROP = "debounceMs";

	/** @see #isSendValueOnBlur() */
	String SEND_VALUE_ON_BLUR__PROP = "sendValueOnBlur";

	/** @see #isCommitOnBlur() */
	String COMMIT_ON_BLUR__PROP = "commitOnBlur";

	/**
	 * How long a typed value is held back before it is sent, in milliseconds. Absent: the default
	 * of the component.
	 */
	long getDebounceMs();

	/**
	 * @see #getDebounceMs()
	 */
	com.top_logic.layout.react.state.TypingFieldState setDebounceMs(long value);

	/**
	 * Whether a typed value is held back until the field loses focus, instead of being sent while
	 * the user is still typing. Takes precedence over {@link #getDebounceMs()}.
	 */
	boolean isSendValueOnBlur();

	/**
	 * @see #isSendValueOnBlur()
	 */
	com.top_logic.layout.react.state.TypingFieldState setSendValueOnBlur(boolean value);

	/**
	 * Whether the field sends the command {@code commit} when it loses focus.
	 */
	boolean isCommitOnBlur();

	/**
	 * @see #isCommitOnBlur()
	 */
	com.top_logic.layout.react.state.TypingFieldState setCommitOnBlur(boolean value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.TypingFieldState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.TypingFieldState readTypingFieldState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.TypingFieldState_Impl result = new com.top_logic.layout.react.state.impl.TypingFieldState_Impl();
		result.readContent(in);
		return result;
	}

}
