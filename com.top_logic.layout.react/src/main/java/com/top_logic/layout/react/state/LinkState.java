package com.top_logic.layout.react.state;

/**
 * State of a text shown as a link opening an address - a web page, an e-mail to write, a number to
 * call - the component {@code TLLink}.
 *
 * The component draws the link with the part {@code FieldValue}. It computes the address the link
 * opens from the {@link #getAddress()} and its {@link #getKind()}, as a read-only text field of that kind
 * does. The component sends no commands.
 */
public interface LinkState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * What an address is, deciding the link it becomes.
	 */
	public enum Kind implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A web address, the link is the address itself.
		 */
		URL("url"),

		/**
		 * An e-mail address, the link writes an e-mail to it ({@code mailto:}).
		 */
		EMAIL("email"),

		/**
		 * A telephone number, the link calls it ({@code tel:}), the number without its spaces.
		 */
		TEL("tel"),

		;

		private final String _protocolName;

		private Kind(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Kind} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Kind} constant by it's protocol name. */
		public static Kind valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "url": return URL;
				case "email": return EMAIL;
				case "tel": return TEL;
			}
			return URL;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Kind readKind(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.LinkState} instance.
	 */
	static com.top_logic.layout.react.state.LinkState create() {
		return new com.top_logic.layout.react.state.impl.LinkState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.LinkState} type in JSON format. */
	String LINK_STATE__TYPE = "LinkState";

	/** @see #getText() */
	String TEXT__PROP = "text";

	/** @see #getAddress() */
	String ADDRESS__PROP = "address";

	/** @see #getKind() */
	String KIND__PROP = "kind";

	/** @see #isSameWindow() */
	String SAME_WINDOW__PROP = "sameWindow";

	/** @see #getTooltip() */
	String TOOLTIP__PROP = "tooltip";

	/**
	 * The shown text, plain text. Absent or empty: the {@link #getAddress()} is shown.
	 */
	String getText();

	/**
	 * @see #getText()
	 */
	com.top_logic.layout.react.state.LinkState setText(String value);

	/**
	 * The address the link opens, as the {@link #getKind()} writes it: a web address, an e-mail address
	 * or a telephone number without a scheme. Absent or empty: the text is shown without a link.
	 */
	String getAddress();

	/**
	 * @see #getAddress()
	 */
	com.top_logic.layout.react.state.LinkState setAddress(String value);

	/**
	 * What the {@link #getAddress()} is. Absent means {@link Kind#URL}, a web address.
	 */
	com.top_logic.layout.react.state.LinkState.Kind getKind();

	/**
	 * @see #getKind()
	 */
	com.top_logic.layout.react.state.LinkState setKind(com.top_logic.layout.react.state.LinkState.Kind value);

	/**
	 * Whether the link opens its address in the window of the page instead of a window of its own.
	 * Absent: a window of its own.
	 */
	boolean isSameWindow();

	/**
	 * @see #isSameWindow()
	 */
	com.top_logic.layout.react.state.LinkState setSameWindow(boolean value);

	/**
	 * The tooltip of the link, plain text. Absent or empty: none.
	 */
	String getTooltip();

	/**
	 * @see #getTooltip()
	 */
	com.top_logic.layout.react.state.LinkState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.LinkState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.LinkState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.LinkState readLinkState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.LinkState_Impl result = new com.top_logic.layout.react.state.impl.LinkState_Impl();
		result.readContent(in);
		return result;
	}

}
