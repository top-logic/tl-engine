package com.top_logic.layout.react.state;

/**
 * State of a card: a container grouping its content visually, with an optional header, the
 * component {@code TLCard}.
 *
 * A card has no behaviour of its own (no minimizing, maximizing or popping out); it sends no
 * commands.
 */
public interface CardState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * How a card sets itself off from its surroundings.
	 */
	public enum Variant implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A thin border.
		 */
		OUTLINED("outlined"),

		/**
		 * A drop shadow.
		 */
		ELEVATED("elevated"),

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
				case "outlined": return OUTLINED;
				case "elevated": return ELEVATED;
			}
			return OUTLINED;
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
	 * The space between the edge of a card and its content.
	 */
	public enum Padding implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * The standard space.
		 */
		DEFAULT("default"),

		/**
		 * A reduced space.
		 */
		COMPACT("compact"),

		/**
		 * No space.
		 */
		NONE("none"),

		;

		private final String _protocolName;

		private Padding(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Padding} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Padding} constant by it's protocol name. */
		public static Padding valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "default": return DEFAULT;
				case "compact": return COMPACT;
				case "none": return NONE;
			}
			return DEFAULT;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Padding readPadding(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.CardState} instance.
	 */
	static com.top_logic.layout.react.state.CardState create() {
		return new com.top_logic.layout.react.state.impl.CardState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.CardState} type in JSON format. */
	String CARD_STATE__TYPE = "CardState";

	/** @see #getTitle() */
	String TITLE__PROP = "title";

	/** @see #getVariant() */
	String VARIANT__PROP = "variant";

	/** @see #getPadding() */
	String PADDING__PROP = "padding";

	/** @see #getHeaderActions() */
	String HEADER_ACTIONS__PROP = "headerActions";

	/** @see #getChild() */
	String CHILD__PROP = "child";

	/**
	 * The title shown in the header, plain text. Absent: no title. The header is shown while there
	 * is a title or {@link #getHeaderActions()}.
	 */
	String getTitle();

	/**
	 * @see #getTitle()
	 */
	com.top_logic.layout.react.state.CardState setTitle(String value);

	/**
	 * How the card sets itself off. Absent means {@link Variant#OUTLINED}, a thin border.
	 */
	com.top_logic.layout.react.state.CardState.Variant getVariant();

	/**
	 * @see #getVariant()
	 */
	com.top_logic.layout.react.state.CardState setVariant(com.top_logic.layout.react.state.CardState.Variant value);

	/**
	 * The space around the content. Absent means {@link Padding#DEFAULT}, the standard space.
	 */
	com.top_logic.layout.react.state.CardState.Padding getPadding();

	/**
	 * @see #getPadding()
	 */
	com.top_logic.layout.react.state.CardState setPadding(com.top_logic.layout.react.state.CardState.Padding value);

	/**
	 * The buttons in the header, in display order. Absent: none.
	 */
	java.util.List<com.top_logic.layout.react.state.ChildControl> getHeaderActions();

	/**
	 * @see #getHeaderActions()
	 */
	com.top_logic.layout.react.state.CardState setHeaderActions(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value);

	/**
	 * Adds a value to the {@link #getHeaderActions()} list.
	 */
	com.top_logic.layout.react.state.CardState addHeaderAction(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Removes a value from the {@link #getHeaderActions()} list.
	 */
	void removeHeaderAction(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * The content of the card.
	 */
	com.top_logic.layout.react.state.ChildControl getChild();

	/**
	 * @see #getChild()
	 */
	com.top_logic.layout.react.state.CardState setChild(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getChild()} has a value.
	 */
	boolean hasChild();

	@Override
	com.top_logic.layout.react.state.CardState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.CardState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.CardState readCardState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.CardState_Impl result = new com.top_logic.layout.react.state.impl.CardState_Impl();
		result.readContent(in);
		return result;
	}

}
