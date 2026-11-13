package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.ClearableFieldState}.
 */
public class ClearableFieldState_Impl extends com.top_logic.layout.react.state.impl.TypingFieldState_Impl implements com.top_logic.layout.react.state.ClearableFieldState {

	private boolean _clearable = false;

	/**
	 * Creates a {@link ClearableFieldState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.ClearableFieldState#create()
	 */
	public ClearableFieldState_Impl() {
		super();
	}

	@Override
	public final boolean isClearable() {
		return _clearable;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setClearable(boolean value) {
		internalSetClearable(value);
		return this;
	}

	/** Internal setter for {@link #isClearable()} without chain call utility. */
	protected final void internalSetClearable(boolean value) {
		_clearable = value;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setDebounceMs(long value) {
		internalSetDebounceMs(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setSendValueOnBlur(boolean value) {
		internalSetSendValueOnBlur(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setCommitEdits(boolean value) {
		internalSetCommitEdits(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setDisabled(boolean value) {
		internalSetDisabled(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setCompact(boolean value) {
		internalSetCompact(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ClearableFieldState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return CLEARABLE_FIELD_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CLEARABLE__PROP);
		out.value(isClearable());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CLEARABLE__PROP: setClearable(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
