package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.TextInputState}.
 */
public class TextInputState_Impl extends com.top_logic.layout.react.state.impl.TypingFieldState_Impl implements com.top_logic.layout.react.state.TextInputState {

	private com.top_logic.layout.react.state.TextInputState.InputType _inputType = com.top_logic.layout.react.state.TextInputState.InputType.TEXT;

	private String _icon = "";

	private boolean _clearable = false;

	private boolean _multiline = false;

	private int _rows = 0;

	/**
	 * Creates a {@link TextInputState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.TextInputState#create()
	 */
	public TextInputState_Impl() {
		super();
	}

	@Override
	public final com.top_logic.layout.react.state.TextInputState.InputType getInputType() {
		return _inputType;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setInputType(com.top_logic.layout.react.state.TextInputState.InputType value) {
		internalSetInputType(value);
		return this;
	}

	/** Internal setter for {@link #getInputType()} without chain call utility. */
	protected final void internalSetInputType(com.top_logic.layout.react.state.TextInputState.InputType value) {
		if (value == null) throw new IllegalArgumentException("Property 'inputType' cannot be null.");
		_inputType = value;
	}

	@Override
	public final String getIcon() {
		return _icon;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setIcon(String value) {
		internalSetIcon(value);
		return this;
	}

	/** Internal setter for {@link #getIcon()} without chain call utility. */
	protected final void internalSetIcon(String value) {
		_icon = value;
	}

	@Override
	public final boolean isClearable() {
		return _clearable;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setClearable(boolean value) {
		internalSetClearable(value);
		return this;
	}

	/** Internal setter for {@link #isClearable()} without chain call utility. */
	protected final void internalSetClearable(boolean value) {
		_clearable = value;
	}

	@Override
	public final boolean isMultiline() {
		return _multiline;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setMultiline(boolean value) {
		internalSetMultiline(value);
		return this;
	}

	/** Internal setter for {@link #isMultiline()} without chain call utility. */
	protected final void internalSetMultiline(boolean value) {
		_multiline = value;
	}

	@Override
	public final int getRows() {
		return _rows;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setRows(int value) {
		internalSetRows(value);
		return this;
	}

	/** Internal setter for {@link #getRows()} without chain call utility. */
	protected final void internalSetRows(int value) {
		_rows = value;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setDebounceMs(long value) {
		internalSetDebounceMs(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setSendValueOnBlur(boolean value) {
		internalSetSendValueOnBlur(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setCommitOnBlur(boolean value) {
		internalSetCommitOnBlur(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TEXT_INPUT_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(INPUT_TYPE__PROP);
		getInputType().writeTo(out);
		out.name(ICON__PROP);
		out.value(getIcon());
		out.name(CLEARABLE__PROP);
		out.value(isClearable());
		out.name(MULTILINE__PROP);
		out.value(isMultiline());
		out.name(ROWS__PROP);
		out.value(getRows());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INPUT_TYPE__PROP: setInputType(com.top_logic.layout.react.state.TextInputState.InputType.readInputType(in)); break;
			case ICON__PROP: setIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CLEARABLE__PROP: setClearable(in.nextBoolean()); break;
			case MULTILINE__PROP: setMultiline(in.nextBoolean()); break;
			case ROWS__PROP: setRows(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
