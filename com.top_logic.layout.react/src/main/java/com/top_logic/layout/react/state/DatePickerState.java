package com.top_logic.layout.react.state;

/**
 * State of a field for a date, a time of day, or both, the component {@code TLDatePicker}.
 *
 * The {@link #getValue()} is the ISO form of the value for the HTML input of the {@link #getInputType()}, a
 * string (e.g. {@code 2026-06-01}, {@code 14:30}, {@code 2026-06-01T14:30}).
 */
public interface DatePickerState extends com.top_logic.layout.react.state.FieldState {

	/**
	 * The part of a point in time a field edits, the {@code type} of the HTML input.
	 */
	public enum InputType implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A date without a time of day.
		 */
		DATE("date"),

		/**
		 * A time of day without a date.
		 */
		TIME("time"),

		/**
		 * A date with a time of day.
		 */
		DATE_TIME("datetime-local"),

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
				case "date": return DATE;
				case "time": return TIME;
				case "datetime-local": return DATE_TIME;
			}
			return DATE;
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
				case DATE: out.value(1); break;
				case TIME: out.value(2); break;
				case DATE_TIME: out.value(3); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static InputType readInputType(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return DATE;
				case 2: return TIME;
				case 3: return DATE_TIME;
				default: return DATE;
			}
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.DatePickerState} instance.
	 */
	static com.top_logic.layout.react.state.DatePickerState create() {
		return new com.top_logic.layout.react.state.impl.DatePickerState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.DatePickerState} type in JSON format. */
	String DATE_PICKER_STATE__TYPE = "DatePickerState";

	/** @see #getInputType() */
	String INPUT_TYPE__PROP = "inputType";

	/** @see #getDisplayValue() */
	String DISPLAY_VALUE__PROP = "displayValue";

	/**
	 * The part of a point in time the field edits.
	 */
	com.top_logic.layout.react.state.DatePickerState.InputType getInputType();

	/**
	 * @see #getInputType()
	 */
	com.top_logic.layout.react.state.DatePickerState setInputType(com.top_logic.layout.react.state.DatePickerState.InputType value);

	/**
	 * The value written in the display format of the current user, shown while the field is not
	 * editable.
	 */
	String getDisplayValue();

	/**
	 * @see #getDisplayValue()
	 */
	com.top_logic.layout.react.state.DatePickerState setDisplayValue(String value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.DatePickerState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.DatePickerState readDatePickerState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.DatePickerState_Impl result = new com.top_logic.layout.react.state.impl.DatePickerState_Impl();
		result.readContent(in);
		return result;
	}

}
