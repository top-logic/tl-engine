package com.top_logic.layout.react.state;

/**
 * State properties of a field whose value the user empties by deleting what is typed: a text or a
 * number.
 */
public interface ClearableFieldState extends com.top_logic.layout.react.state.TypingFieldState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.ClearableFieldState} instance.
	 */
	static com.top_logic.layout.react.state.ClearableFieldState create() {
		return new com.top_logic.layout.react.state.impl.ClearableFieldState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.ClearableFieldState} type in JSON format. */
	String CLEARABLE_FIELD_STATE__TYPE = "ClearableFieldState";

	/** @see #isClearable() */
	String CLEARABLE__PROP = "clearable";

	/**
	 * Whether the input offers a button that empties it at once, while it holds a value - for a
	 * search field, for instance. Absent: the input is emptied by deleting what it holds.
	 */
	boolean isClearable();

	/**
	 * @see #isClearable()
	 */
	com.top_logic.layout.react.state.ClearableFieldState setClearable(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setDebounceMs(long value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setSendValueOnBlur(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setCommitEdits(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setDisabled(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setCompact(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.ClearableFieldState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.ClearableFieldState readClearableFieldState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.ClearableFieldState_Impl result = new com.top_logic.layout.react.state.impl.ClearableFieldState_Impl();
		result.readContent(in);
		return result;
	}

}
