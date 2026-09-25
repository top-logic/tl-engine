package com.top_logic.layout.react.state;

/**
 * State of a boolean field drawn as a box that is ticked or a switch that is flipped, the
 * component {@code TLCheckbox}.
 *
 * The {@link #getValue()} is {@code true}, {@code false}, or - for a {@link #isTriState()} field -
 * {@code null} for "no value". A change is sent as the command {@code valueChanged} with the
 * new value as argument {@code value}.
 */
public interface CheckboxState extends com.top_logic.layout.react.state.FieldState {

	/**
	 * The shape of the field.
	 */
	public enum Display implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A box that is ticked.
		 */
		CHECKBOX("checkbox"),

		/**
		 * A switch that is flipped.
		 */
		SWITCH("switch"),

		;

		private final String _protocolName;

		private Display(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Display} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Display} constant by it's protocol name. */
		public static Display valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "checkbox": return CHECKBOX;
				case "switch": return SWITCH;
			}
			return CHECKBOX;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Display readDisplay(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case CHECKBOX: out.value(1); break;
				case SWITCH: out.value(2); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static Display readDisplay(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return CHECKBOX;
				case 2: return SWITCH;
				default: return CHECKBOX;
			}
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.CheckboxState} instance.
	 */
	static com.top_logic.layout.react.state.CheckboxState create() {
		return new com.top_logic.layout.react.state.impl.CheckboxState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.CheckboxState} type in JSON format. */
	String CHECKBOX_STATE__TYPE = "CheckboxState";

	/** @see #isTriState() */
	String TRI_STATE__PROP = "triState";

	/** @see #getDisplay() */
	String DISPLAY__PROP = "display";

	/**
	 * Whether the field has a third state for "no value". The component then shows an unset
	 * field as indeterminate and cycles through the states on click: checked, unchecked, unset.
	 */
	boolean isTriState();

	/**
	 * @see #isTriState()
	 */
	com.top_logic.layout.react.state.CheckboxState setTriState(boolean value);

	/**
	 * The shape of the field. Absent: a box that is ticked.
	 */
	com.top_logic.layout.react.state.CheckboxState.Display getDisplay();

	/**
	 * @see #getDisplay()
	 */
	com.top_logic.layout.react.state.CheckboxState setDisplay(com.top_logic.layout.react.state.CheckboxState.Display value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.CheckboxState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.CheckboxState readCheckboxState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.CheckboxState_Impl result = new com.top_logic.layout.react.state.impl.CheckboxState_Impl();
		result.readContent(in);
		return result;
	}

}
