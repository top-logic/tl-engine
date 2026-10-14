package com.top_logic.layout.react.state;

/**
 * State of a circular avatar for a person or object, the component {@code TLAvatar}.
 *
 * The circle shows the picture of the one it represents. Where there is none, it shows the
 * initials of the {@link #getName()} on one of the eight category roles of the design system, which the
 * client derives from the name; without a name, the neutral circle. The component sends no
 * commands.
 */
public interface AvatarState extends com.top_logic.layout.react.state.ImageSourceState {

	/**
	 * The diameter of an avatar, chosen by where the avatar stands, independent of the density.
	 */
	public enum Size implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * An avatar in a table cell or a row.
		 */
		SMALL("small"),

		/**
		 * The rule: beside text, in lists and in comment headers.
		 */
		MEDIUM("medium"),

		/**
		 * An avatar in the header of a detail view.
		 */
		LARGE("large"),

		/**
		 * A profile page: the picture is the content.
		 */
		EXTRA_LARGE("x-large"),

		;

		private final String _protocolName;

		private Size(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Size} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Size} constant by it's protocol name. */
		public static Size valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "small": return SMALL;
				case "medium": return MEDIUM;
				case "large": return LARGE;
				case "x-large": return EXTRA_LARGE;
			}
			return SMALL;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Size readSize(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.AvatarState} instance.
	 */
	static com.top_logic.layout.react.state.AvatarState create() {
		return new com.top_logic.layout.react.state.impl.AvatarState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.AvatarState} type in JSON format. */
	String AVATAR_STATE__TYPE = "AvatarState";

	/** @see #getName() */
	String NAME__PROP = "name";

	/** @see #getSize() */
	String SIZE__PROP = "size";

	/**
	 * The display name of the one the avatar represents, plain text, the source of the initials,
	 * the category and the accessible name. Absent or {@code null}: the neutral circle.
	 */
	String getName();

	/**
	 * @see #getName()
	 */
	com.top_logic.layout.react.state.AvatarState setName(String value);

	/**
	 * The diameter of the circle. Absent means {@link Size#MEDIUM}.
	 */
	com.top_logic.layout.react.state.AvatarState.Size getSize();

	/**
	 * @see #getSize()
	 */
	com.top_logic.layout.react.state.AvatarState setSize(com.top_logic.layout.react.state.AvatarState.Size value);

	@Override
	com.top_logic.layout.react.state.AvatarState setUrl(String value);

	@Override
	com.top_logic.layout.react.state.AvatarState setHasData(boolean value);

	@Override
	com.top_logic.layout.react.state.AvatarState setDataRevision(int value);

	@Override
	com.top_logic.layout.react.state.AvatarState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.AvatarState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.AvatarState readAvatarState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.AvatarState_Impl result = new com.top_logic.layout.react.state.impl.AvatarState_Impl();
		result.readContent(in);
		return result;
	}

}
