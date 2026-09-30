package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.TypingFieldState}.
 */
public class TypingFieldState_Impl extends com.top_logic.layout.react.state.impl.FieldState_Impl implements com.top_logic.layout.react.state.TypingFieldState {

	private long _debounceMs = 0L;

	private boolean _sendValueOnBlur = false;

	private boolean _commitOnBlur = false;

	/**
	 * Creates a {@link TypingFieldState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.TypingFieldState#create()
	 */
	public TypingFieldState_Impl() {
		super();
	}

	@Override
	public final long getDebounceMs() {
		return _debounceMs;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setDebounceMs(long value) {
		internalSetDebounceMs(value);
		return this;
	}

	/** Internal setter for {@link #getDebounceMs()} without chain call utility. */
	protected final void internalSetDebounceMs(long value) {
		_debounceMs = value;
	}

	@Override
	public final boolean isSendValueOnBlur() {
		return _sendValueOnBlur;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setSendValueOnBlur(boolean value) {
		internalSetSendValueOnBlur(value);
		return this;
	}

	/** Internal setter for {@link #isSendValueOnBlur()} without chain call utility. */
	protected final void internalSetSendValueOnBlur(boolean value) {
		_sendValueOnBlur = value;
	}

	@Override
	public final boolean isCommitOnBlur() {
		return _commitOnBlur;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setCommitOnBlur(boolean value) {
		internalSetCommitOnBlur(value);
		return this;
	}

	/** Internal setter for {@link #isCommitOnBlur()} without chain call utility. */
	protected final void internalSetCommitOnBlur(boolean value) {
		_commitOnBlur = value;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TypingFieldState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TYPING_FIELD_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(DEBOUNCE_MS__PROP);
		out.value(getDebounceMs());
		out.name(SEND_VALUE_ON_BLUR__PROP);
		out.value(isSendValueOnBlur());
		out.name(COMMIT_ON_BLUR__PROP);
		out.value(isCommitOnBlur());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DEBOUNCE_MS__PROP: setDebounceMs(in.nextLong()); break;
			case SEND_VALUE_ON_BLUR__PROP: setSendValueOnBlur(in.nextBoolean()); break;
			case COMMIT_ON_BLUR__PROP: setCommitOnBlur(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
