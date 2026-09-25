package com.top_logic.layout.react.state;

/**
 * State of a text field, the component {@code TLTextInput}.
 *
 * The {@link #getValue()} is the text, a string.
 */
public interface TextInputState extends com.top_logic.layout.react.state.TypingFieldState {

	/**
	 * The kind of value a text field edits, the {@code type} of the HTML input.
	 */
	public enum InputType implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * Plain text.
		 */
		TEXT("text"),

		/**
		 * A web address.
		 */
		URL("url"),

		/**
		 * An e-mail address.
		 */
		EMAIL("email"),

		/**
		 * A telephone number.
		 */
		TEL("tel"),

		/**
		 * A search term.
		 */
		SEARCH("search"),

		;

		private final String _protocolName;

		private InputType(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link InputType} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link InputType} constant by it's protocol name. */
		public static InputType valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "text": return TEXT;
				case "url": return URL;
				case "email": return EMAIL;
				case "tel": return TEL;
				case "search": return SEARCH;
			}
			return TEXT;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static InputType readInputType(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case TEXT: out.value(1); break;
				case URL: out.value(2); break;
				case EMAIL: out.value(3); break;
				case TEL: out.value(4); break;
				case SEARCH: out.value(5); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static InputType readInputType(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return TEXT;
				case 2: return URL;
				case 3: return EMAIL;
				case 4: return TEL;
				case 5: return SEARCH;
				default: return TEXT;
			}
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.TextInputState} instance.
	 */
	static com.top_logic.layout.react.state.TextInputState create() {
		return new com.top_logic.layout.react.state.impl.TextInputState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.TextInputState} type in JSON format. */
	String TEXT_INPUT_STATE__TYPE = "TextInputState";

	/** @see #getInputType() */
	String INPUT_TYPE__PROP = "inputType";

	/** @see #getIcon() */
	String ICON__PROP = "icon";

	/** @see #isClearable() */
	String CLEARABLE__PROP = "clearable";

	/** @see #isMultiline() */
	String MULTILINE__PROP = "multiline";

	/** @see #getRows() */
	String ROWS__PROP = "rows";

	/**
	 * The kind of value the field edits. Absent: plain text.
	 */
	com.top_logic.layout.react.state.TextInputState.InputType getInputType();

	/**
	 * @see #getInputType()
	 */
	com.top_logic.layout.react.state.TextInputState setInputType(com.top_logic.layout.react.state.TextInputState.InputType value);

	/**
	 * An icon shown inside the input ahead of the text, the encoded form of a theme image.
	 */
	String getIcon();

	/**
	 * @see #getIcon()
	 */
	com.top_logic.layout.react.state.TextInputState setIcon(String value);

	/**
	 * Whether the input offers a button that empties it, while it holds a value.
	 */
	boolean isClearable();

	/**
	 * @see #isClearable()
	 */
	com.top_logic.layout.react.state.TextInputState setClearable(boolean value);

	/**
	 * Whether the field is a text area of several lines, see {@link #getRows()}.
	 */
	boolean isMultiline();

	/**
	 * @see #isMultiline()
	 */
	com.top_logic.layout.react.state.TextInputState setMultiline(boolean value);

	/**
	 * The number of visible rows of a {@link #isMultiline()} field.
	 */
	int getRows();

	/**
	 * @see #getRows()
	 */
	com.top_logic.layout.react.state.TextInputState setRows(int value);

	@Override
	com.top_logic.layout.react.state.TextInputState setDebounceMs(long value);

	@Override
	com.top_logic.layout.react.state.TextInputState setSendValueOnBlur(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setCommitOnBlur(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.TextInputState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.TextInputState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.TextInputState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.TextInputState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.TextInputState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.TextInputState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.TextInputState readTextInputState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.TextInputState_Impl result = new com.top_logic.layout.react.state.impl.TextInputState_Impl();
		result.readContent(in);
		return result;
	}

}
