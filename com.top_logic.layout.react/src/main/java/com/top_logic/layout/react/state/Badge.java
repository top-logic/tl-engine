package com.top_logic.layout.react.state;

/**
 * A small marker at the corner of an element - a button, a tab, a navigation entry, an avatar - that
 * counts or marks something waiting there, drawn through the part {@code Badge} of
 * 'tl-react-bridge'.
 *
 * A state property holding a badge is absent where the element shows none. A badge shows either
 * the {@link #getContent()}, or - where {@link #isDot()} is set - a dot without content.
 *
 * The badge belongs to the accessible name of its element: beside a visible label (a button, a
 * tab) the content is visible text that is read with the label; on an element without visible
 * text (an icon, an avatar) and for a dot the badge is an image named by its {@link #getLabel()}, or by
 * its content where there is no label.
 */
public interface Badge extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * The color role of a badge.
	 */
	public enum Role implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * The color of the brand.
		 */
		BRAND("brand"),

		/**
		 * Something failed.
		 */
		ERROR("error"),

		/**
		 * Something calls for attention before it fails.
		 */
		WARNING("warning"),

		/**
		 * Something went well.
		 */
		SUCCESS("success"),

		/**
		 * Something to know.
		 */
		INFO("info"),

		;

		private final String _protocolName;

		private Role(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Role} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Role} constant by it's protocol name. */
		public static Role valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "brand": return BRAND;
				case "error": return ERROR;
				case "warning": return WARNING;
				case "success": return SUCCESS;
				case "info": return INFO;
			}
			return BRAND;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Role readRole(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.Badge} instance.
	 */
	static com.top_logic.layout.react.state.Badge create() {
		return new com.top_logic.layout.react.state.impl.Badge_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.Badge} type in JSON format. */
	String BADGE__TYPE = "Badge";

	/** @see #getContent() */
	String CONTENT__PROP = "content";

	/** @see #isDot() */
	String DOT__PROP = "dot";

	/** @see #getRole() */
	String ROLE__PROP = "role";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/**
	 * What the badge shows: a count of at most two digits, the overflow {@code 99+}, or a short
	 * text. Absent for a {@link #isDot()}.
	 */
	String getContent();

	/**
	 * @see #getContent()
	 */
	com.top_logic.layout.react.state.Badge setContent(String value);

	/**
	 * Whether the badge is a dot marking the element without counting.
	 */
	boolean isDot();

	/**
	 * @see #isDot()
	 */
	com.top_logic.layout.react.state.Badge setDot(boolean value);

	/**
	 * The color role. Absent means {@link Role#BRAND}.
	 */
	com.top_logic.layout.react.state.Badge.Role getRole();

	/**
	 * @see #getRole()
	 */
	com.top_logic.layout.react.state.Badge setRole(com.top_logic.layout.react.state.Badge.Role value);

	/**
	 * The accessible name of the badge where its content does not say it itself (a dot, a count
	 * standing alone at an icon). Absent: the content names the badge.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.Badge setLabel(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.Badge readBadge(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.Badge_Impl result = new com.top_logic.layout.react.state.impl.Badge_Impl();
		result.readContent(in);
		return result;
	}

}
