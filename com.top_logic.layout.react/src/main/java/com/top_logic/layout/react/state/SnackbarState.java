package com.top_logic.layout.react.state;

/**
 * State of a short message at the edge of the page, the component {@code TLSnackbar}.
 *
 * Dismissing the message sends the command {@code dismiss} with the {@link #getGeneration()} of the
 * message as argument {@code generation}.
 */
public interface SnackbarState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * The kind of a message.
	 */
	public enum Variant implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * An information.
		 */
		INFO("info"),

		/**
		 * The report of a success.
		 */
		SUCCESS("success"),

		/**
		 * A warning.
		 */
		WARNING("warning"),

		/**
		 * The report of an error.
		 */
		ERROR("error"),

		;

		private final String _protocolName;

		private Variant(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Variant} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Variant} constant by it's protocol name. */
		public static Variant valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "info": return INFO;
				case "success": return SUCCESS;
				case "warning": return WARNING;
				case "error": return ERROR;
			}
			return INFO;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Variant readVariant(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case INFO: out.value(1); break;
				case SUCCESS: out.value(2); break;
				case WARNING: out.value(3); break;
				case ERROR: out.value(4); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static Variant readVariant(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return INFO;
				case 2: return SUCCESS;
				case 3: return WARNING;
				case 4: return ERROR;
				default: return INFO;
			}
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.SnackbarState} instance.
	 */
	static com.top_logic.layout.react.state.SnackbarState create() {
		return new com.top_logic.layout.react.state.impl.SnackbarState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.SnackbarState} type in JSON format. */
	String SNACKBAR_STATE__TYPE = "SnackbarState";

	/** @see #getMessageText() */
	String MESSAGE_TEXT__PROP = "message";

	/** @see #getContent() */
	String CONTENT__PROP = "content";

	/** @see #getVariant() */
	String VARIANT__PROP = "variant";

	/** @see #getDuration() */
	String DURATION__PROP = "duration";

	/** @see #isVisible() */
	String VISIBLE__PROP = "visible";

	/** @see #getGeneration() */
	String GENERATION__PROP = "generation";

	/**
	 * The message, plain text. Shown where there is no {@link #getContent()}.
	 */
	String getMessageText();

	/**
	 * @see #getMessageText()
	 */
	com.top_logic.layout.react.state.SnackbarState setMessageText(String value);

	/**
	 * The message, HTML. Takes precedence over {@link #getMessageText()}.
	 */
	String getContent();

	/**
	 * @see #getContent()
	 */
	com.top_logic.layout.react.state.SnackbarState setContent(String value);

	/**
	 * The kind of the message.
	 */
	com.top_logic.layout.react.state.SnackbarState.Variant getVariant();

	/**
	 * @see #getVariant()
	 */
	com.top_logic.layout.react.state.SnackbarState setVariant(com.top_logic.layout.react.state.SnackbarState.Variant value);

	/**
	 * The time after which the message is dismissed, in milliseconds; zero for a message that stays
	 * until it is dismissed.
	 */
	int getDuration();

	/**
	 * @see #getDuration()
	 */
	com.top_logic.layout.react.state.SnackbarState setDuration(int value);

	/**
	 * Whether a message is shown.
	 */
	boolean isVisible();

	/**
	 * @see #isVisible()
	 */
	com.top_logic.layout.react.state.SnackbarState setVisible(boolean value);

	/**
	 * The number of the message shown, counting the messages this control has shown.
	 */
	int getGeneration();

	/**
	 * @see #getGeneration()
	 */
	com.top_logic.layout.react.state.SnackbarState setGeneration(int value);

	@Override
	com.top_logic.layout.react.state.SnackbarState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.SnackbarState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.SnackbarState readSnackbarState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.SnackbarState_Impl result = new com.top_logic.layout.react.state.impl.SnackbarState_Impl();
		result.readContent(in);
		return result;
	}

}
