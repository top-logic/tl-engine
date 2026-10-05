package com.top_logic.layout.react.state;

/**
 * State of a read-only text, the component {@code TLText}.
 *
 * How the text is drawn is stated as roles rather than as font and color values: a typographic
 * role ({@link #getVariant()}), a color role ({@link #getTone()}) and the shape it takes
 * ({@link #getAppearance()}). The component sends no commands.
 */
public interface TextState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * How text longer than the available width is displayed.
	 */
	public enum Overflow implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * Wrapped onto several lines.
		 */
		WRAP("wrap"),

		/**
		 * Kept on a single line, the overflow truncated with an ellipsis.
		 */
		ELLIPSIS("ellipsis"),

		;

		private final String _protocolName;

		private Overflow(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Overflow} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Overflow} constant by it's protocol name. */
		public static Overflow valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "wrap": return WRAP;
				case "ellipsis": return ELLIPSIS;
			}
			return WRAP;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Overflow readOverflow(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * What a text is for, its typographic role.
	 */
	public enum Variant implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * Running text, the size a page is read at.
		 */
		BODY("body"),

		/**
		 * The heading of a section within a page.
		 */
		TITLE("title"),

		/**
		 * The heading a page is introduced with.
		 */
		HEADLINE("headline"),

		/**
		 * The largest text on a page, for the one statement a landing page is built around.
		 */
		DISPLAY("display"),

		/**
		 * The name of a value, shown above or beside what it names.
		 */
		LABEL("label"),

		/**
		 * A remark accompanying another text: a hint, a date, an attribution.
		 */
		CAPTION("caption"),

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
				case "body": return BODY;
				case "title": return TITLE;
				case "headline": return HEADLINE;
				case "display": return DISPLAY;
				case "label": return LABEL;
				case "caption": return CAPTION;
			}
			return BODY;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Variant readVariant(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * What the color of a text means, its color role.
	 */
	public enum Tone implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * The color text is read in.
		 */
		PRIMARY("primary"),

		/**
		 * Text of lesser weight than what stands beside it.
		 */
		SECONDARY("secondary"),

		/**
		 * An explanation of the value or the field it accompanies.
		 */
		HELPER("helper"),

		/**
		 * Text the application draws attention to, in the color interactive elements share.
		 */
		ACCENT("accent"),

		/**
		 * An outcome that went well.
		 */
		SUCCESS("success"),

		/**
		 * A condition the reader should act on before it becomes a failure.
		 */
		WARNING("warning"),

		/**
		 * A failure.
		 */
		ERROR("error"),

		/**
		 * Text on a filled surface, which brings its own background color.
		 */
		ON_COLOR("on-color"),

		;

		private final String _protocolName;

		private Tone(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Tone} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Tone} constant by it's protocol name. */
		public static Tone valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "primary": return PRIMARY;
				case "secondary": return SECONDARY;
				case "helper": return HELPER;
				case "accent": return ACCENT;
				case "success": return SUCCESS;
				case "warning": return WARNING;
				case "error": return ERROR;
				case "on-color": return ON_COLOR;
			}
			return PRIMARY;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Tone readTone(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * The shape a text is drawn in.
	 */
	public enum Appearance implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * Plain text, drawn as a pill when the text carries a color role of its own.
		 */
		TEXT("text"),

		/**
		 * A pill, whether or not the text carries a color role: a badge, a status, a tag. Its color
		 * is the color role of the text, or the color of its tone for a text without one.
		 */
		PILL("pill"),

		;

		private final String _protocolName;

		private Appearance(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Appearance} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Appearance} constant by it's protocol name. */
		public static Appearance valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "text": return TEXT;
				case "pill": return PILL;
			}
			return TEXT;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Appearance readAppearance(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.TextState} instance.
	 */
	static com.top_logic.layout.react.state.TextState create() {
		return new com.top_logic.layout.react.state.impl.TextState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.TextState} type in JSON format. */
	String TEXT_STATE__TYPE = "TextState";

	/** @see #getText() */
	String TEXT__PROP = "text";

	/** @see #getOverflow() */
	String OVERFLOW__PROP = "overflow";

	/** @see #getVariant() */
	String VARIANT__PROP = "variant";

	/** @see #getTone() */
	String TONE__PROP = "tone";

	/** @see #getAppearance() */
	String APPEARANCE__PROP = "appearance";

	/** @see #getRole() */
	String ROLE__PROP = "role";

	/** @see #getColorRole() */
	String COLOR_ROLE__PROP = "colorRole";

	/** @see #isHasTooltip() */
	String HAS_TOOLTIP__PROP = "hasTooltip";

	/**
	 * The text, plain text. Absent: empty. An empty text is never drawn as a pill.
	 */
	String getText();

	/**
	 * @see #getText()
	 */
	com.top_logic.layout.react.state.TextState setText(String value);

	/**
	 * How text longer than the available width is displayed. Absent means {@link Overflow#WRAP},
	 * wrapped onto several lines.
	 */
	com.top_logic.layout.react.state.TextState.Overflow getOverflow();

	/**
	 * @see #getOverflow()
	 */
	com.top_logic.layout.react.state.TextState setOverflow(com.top_logic.layout.react.state.TextState.Overflow value);

	/**
	 * The typographic role of the text. Absent means {@link Variant#BODY}, running text.
	 */
	com.top_logic.layout.react.state.TextState.Variant getVariant();

	/**
	 * @see #getVariant()
	 */
	com.top_logic.layout.react.state.TextState setVariant(com.top_logic.layout.react.state.TextState.Variant value);

	/**
	 * The color role of the text. Absent means {@link Tone#PRIMARY}, the color text is read in.
	 */
	com.top_logic.layout.react.state.TextState.Tone getTone();

	/**
	 * @see #getTone()
	 */
	com.top_logic.layout.react.state.TextState setTone(com.top_logic.layout.react.state.TextState.Tone value);

	/**
	 * The shape the text is drawn in. Absent means {@link Appearance#TEXT}, plain text.
	 */
	com.top_logic.layout.react.state.TextState.Appearance getAppearance();

	/**
	 * @see #getAppearance()
	 */
	com.top_logic.layout.react.state.TextState setAppearance(com.top_logic.layout.react.state.TextState.Appearance value);

	/**
	 * The ARIA role of the element (e.g. {@code alert} for a message announced when it appears).
	 * Absent: none.
	 */
	String getRole();

	/**
	 * @see #getRole()
	 */
	com.top_logic.layout.react.state.TextState setRole(String value);

	/**
	 * The color role the displayed value carries in the model (the external name of a value color:
	 * neutral, brand, error, …, category-8). A text with a color role is drawn as a pill of that
	 * role. Absent: none.
	 */
	String getColorRole();

	/**
	 * @see #getColorRole()
	 */
	com.top_logic.layout.react.state.TextState setColorRole(String value);

	/**
	 * Whether the server offers a rich tooltip for the text, HTML fetched from the control under
	 * the key {@code tooltip} (the attribute {@code data-tooltip} with the value
		 * {@code key:tooltip}).
	 */
	boolean isHasTooltip();

	/**
	 * @see #isHasTooltip()
	 */
	com.top_logic.layout.react.state.TextState setHasTooltip(boolean value);

	@Override
	com.top_logic.layout.react.state.TextState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.TextState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.TextState readTextState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.TextState_Impl result = new com.top_logic.layout.react.state.impl.TextState_Impl();
		result.readContent(in);
		return result;
	}

}
