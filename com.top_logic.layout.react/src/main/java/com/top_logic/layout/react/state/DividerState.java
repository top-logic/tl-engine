package com.top_logic.layout.react.state;

/**
 * State of a line separating the content before it from the content after it, the component
 * {@code TLDivider}, drawn by the part {@code Divider}.
 *
 * The component sends no commands.
 */
public interface DividerState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * The direction of a divider.
	 */
	public enum Orientation implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A line across, between content stacked one above the other.
		 */
		HORIZONTAL("horizontal"),

		/**
		 * A line standing in a row and stretching to its height.
		 */
		VERTICAL("vertical"),

		;

		private final String _protocolName;

		private Orientation(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Orientation} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Orientation} constant by it's protocol name. */
		public static Orientation valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "horizontal": return HORIZONTAL;
				case "vertical": return VERTICAL;
			}
			return HORIZONTAL;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Orientation readOrientation(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Where the text of a divider stands in its line.
	 */
	public enum LabelPosition implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * At the start of the line.
		 */
		START("start"),

		/**
		 * In the middle, with a line on either side.
		 */
		CENTER("center"),

		/**
		 * At the end of the line.
		 */
		END("end"),

		;

		private final String _protocolName;

		private LabelPosition(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link LabelPosition} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link LabelPosition} constant by it's protocol name. */
		public static LabelPosition valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "start": return START;
				case "center": return CENTER;
				case "end": return END;
			}
			return START;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static LabelPosition readLabelPosition(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Where a divider begins.
	 */
	public enum Inset implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * The line spans the whole width.
		 */
		NONE("none"),

		/**
		 * The line is shortened at its start by the indent of an entry of a list.
		 */
		START("start"),

		;

		private final String _protocolName;

		private Inset(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Inset} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Inset} constant by it's protocol name. */
		public static Inset valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "none": return NONE;
				case "start": return START;
			}
			return NONE;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Inset readInset(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.DividerState} instance.
	 */
	static com.top_logic.layout.react.state.DividerState create() {
		return new com.top_logic.layout.react.state.impl.DividerState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.DividerState} type in JSON format. */
	String DIVIDER_STATE__TYPE = "DividerState";

	/** @see #getOrientation() */
	String ORIENTATION__PROP = "orientation";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #getLabelPosition() */
	String LABEL_POSITION__PROP = "labelPosition";

	/** @see #getInset() */
	String INSET__PROP = "inset";

	/**
	 * The direction of the line. Absent: {@code horizontal}.
	 */
	com.top_logic.layout.react.state.DividerState.Orientation getOrientation();

	/**
	 * @see #getOrientation()
	 */
	com.top_logic.layout.react.state.DividerState setOrientation(com.top_logic.layout.react.state.DividerState.Orientation value);

	/**
	 * A short text shown in the line, plain text; only a horizontal line carries one. Absent: a
	 * plain line.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.DividerState setLabel(String value);

	/**
	 * Where the {@link #getLabel()} stands in the line. Absent: {@code center}.
	 */
	com.top_logic.layout.react.state.DividerState.LabelPosition getLabelPosition();

	/**
	 * @see #getLabelPosition()
	 */
	com.top_logic.layout.react.state.DividerState setLabelPosition(com.top_logic.layout.react.state.DividerState.LabelPosition value);

	/**
	 * Where the line begins. Absent: {@code none}, the whole width.
	 */
	com.top_logic.layout.react.state.DividerState.Inset getInset();

	/**
	 * @see #getInset()
	 */
	com.top_logic.layout.react.state.DividerState setInset(com.top_logic.layout.react.state.DividerState.Inset value);

	@Override
	com.top_logic.layout.react.state.DividerState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.DividerState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.DividerState readDividerState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.DividerState_Impl result = new com.top_logic.layout.react.state.impl.DividerState_Impl();
		result.readContent(in);
		return result;
	}

}
