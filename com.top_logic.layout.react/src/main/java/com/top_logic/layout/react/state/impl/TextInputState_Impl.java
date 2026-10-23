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

	private boolean _offersSuggestions = false;

	private final java.util.List<com.top_logic.layout.react.state.SuggestionEntry> _suggestions = new java.util.ArrayList<>();

	private String _suggestionsText = "";

	private int _suggestionsCaret = 0;

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
	public final boolean isOffersSuggestions() {
		return _offersSuggestions;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setOffersSuggestions(boolean value) {
		internalSetOffersSuggestions(value);
		return this;
	}

	/** Internal setter for {@link #isOffersSuggestions()} without chain call utility. */
	protected final void internalSetOffersSuggestions(boolean value) {
		_offersSuggestions = value;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.SuggestionEntry> getSuggestions() {
		return _suggestions;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setSuggestions(java.util.List<? extends com.top_logic.layout.react.state.SuggestionEntry> value) {
		internalSetSuggestions(value);
		return this;
	}

	/** Internal setter for {@link #getSuggestions()} without chain call utility. */
	protected final void internalSetSuggestions(java.util.List<? extends com.top_logic.layout.react.state.SuggestionEntry> value) {
		if (value == null) throw new IllegalArgumentException("Property 'suggestions' cannot be null.");
		_suggestions.clear();
		_suggestions.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState addSuggestion(com.top_logic.layout.react.state.SuggestionEntry value) {
		internalAddSuggestion(value);
		return this;
	}

	/** Implementation of {@link #addSuggestion(com.top_logic.layout.react.state.SuggestionEntry)} without chain call utility. */
	protected final void internalAddSuggestion(com.top_logic.layout.react.state.SuggestionEntry value) {
		_suggestions.add(value);
	}

	@Override
	public final void removeSuggestion(com.top_logic.layout.react.state.SuggestionEntry value) {
		_suggestions.remove(value);
	}

	@Override
	public final String getSuggestionsText() {
		return _suggestionsText;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setSuggestionsText(String value) {
		internalSetSuggestionsText(value);
		return this;
	}

	/** Internal setter for {@link #getSuggestionsText()} without chain call utility. */
	protected final void internalSetSuggestionsText(String value) {
		_suggestionsText = value;
	}

	@Override
	public final int getSuggestionsCaret() {
		return _suggestionsCaret;
	}

	@Override
	public com.top_logic.layout.react.state.TextInputState setSuggestionsCaret(int value) {
		internalSetSuggestionsCaret(value);
		return this;
	}

	/** Internal setter for {@link #getSuggestionsCaret()} without chain call utility. */
	protected final void internalSetSuggestionsCaret(int value) {
		_suggestionsCaret = value;
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
	public com.top_logic.layout.react.state.TextInputState setCommitEdits(boolean value) {
		internalSetCommitEdits(value);
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
	public com.top_logic.layout.react.state.TextInputState setDisabled(boolean value) {
		internalSetDisabled(value);
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
	public com.top_logic.layout.react.state.TextInputState setCompact(boolean value) {
		internalSetCompact(value);
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
		out.name(OFFERS_SUGGESTIONS__PROP);
		out.value(isOffersSuggestions());
		out.name(SUGGESTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.SuggestionEntry x : getSuggestions()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(SUGGESTIONS_TEXT__PROP);
		out.value(getSuggestionsText());
		out.name(SUGGESTIONS_CARET__PROP);
		out.value(getSuggestionsCaret());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INPUT_TYPE__PROP: setInputType(com.top_logic.layout.react.state.TextInputState.InputType.readInputType(in)); break;
			case ICON__PROP: setIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CLEARABLE__PROP: setClearable(in.nextBoolean()); break;
			case MULTILINE__PROP: setMultiline(in.nextBoolean()); break;
			case ROWS__PROP: setRows(in.nextInt()); break;
			case OFFERS_SUGGESTIONS__PROP: setOffersSuggestions(in.nextBoolean()); break;
			case SUGGESTIONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.SuggestionEntry> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.SuggestionEntry.readSuggestionEntry(in));
				}
				in.endArray();
				setSuggestions(newValue);
			}
			break;
			case SUGGESTIONS_TEXT__PROP: setSuggestionsText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SUGGESTIONS_CARET__PROP: setSuggestionsCaret(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
