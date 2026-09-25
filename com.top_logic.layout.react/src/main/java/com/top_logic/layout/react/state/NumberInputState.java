package com.top_logic.layout.react.state;

/**
 * State of a number field, the component {@code TLNumberInput}.
 *
 * The {@link #getValue()} is the number written in the format of the field, a string; the text typed
 * is sent back as it is and parsed by the server.
 */
public interface NumberInputState extends com.top_logic.layout.react.state.TypingFieldState {

	/**
	 * The on-screen keyboard a number is typed on.
	 */
	public enum InputMode implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A whole number: digits and a sign.
		 */
		NUMERIC("numeric"),

		/**
		 * A number with a fraction: digits, a sign and a decimal separator.
		 */
		DECIMAL("decimal"),

		/**
		 * A number whose format writes words, a duration for instance.
		 */
		TEXT("text"),

		;

		private final String _protocolName;

		private InputMode(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link InputMode} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link InputMode} constant by it's protocol name. */
		public static InputMode valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "numeric": return NUMERIC;
				case "decimal": return DECIMAL;
				case "text": return TEXT;
			}
			return NUMERIC;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static InputMode readInputMode(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case NUMERIC: out.value(1); break;
				case DECIMAL: out.value(2); break;
				case TEXT: out.value(3); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static InputMode readInputMode(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return NUMERIC;
				case 2: return DECIMAL;
				case 3: return TEXT;
				default: return NUMERIC;
			}
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.NumberInputState} instance.
	 */
	static com.top_logic.layout.react.state.NumberInputState create() {
		return new com.top_logic.layout.react.state.impl.NumberInputState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.NumberInputState} type in JSON format. */
	String NUMBER_INPUT_STATE__TYPE = "NumberInputState";

	/** @see #getInputMode() */
	String INPUT_MODE__PROP = "inputMode";

	/**
	 * The on-screen keyboard the input asks for.
	 */
	com.top_logic.layout.react.state.NumberInputState.InputMode getInputMode();

	/**
	 * @see #getInputMode()
	 */
	com.top_logic.layout.react.state.NumberInputState setInputMode(com.top_logic.layout.react.state.NumberInputState.InputMode value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setDebounceMs(long value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setSendValueOnBlur(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setCommitOnBlur(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.NumberInputState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.NumberInputState readNumberInputState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.NumberInputState_Impl result = new com.top_logic.layout.react.state.impl.NumberInputState_Impl();
		result.readContent(in);
		return result;
	}

}
