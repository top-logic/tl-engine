package com.top_logic.layout.react.state;

/**
 * State of a field choosing one value from a short list, the component {@code TLSelect}.
 *
 * The {@link #getValue()} is the chosen value, in the JSON form of the {@link Option#getValue()} of the
 * option naming it.
 */
public interface SelectState extends com.top_logic.layout.react.state.FieldState {
	/**
	 * A value that can be chosen.
	 */
	public interface Option extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.SelectState.Option} instance.
		 */
		static com.top_logic.layout.react.state.SelectState.Option create() {
			return new com.top_logic.layout.react.state.impl.SelectState_Impl.Option_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.SelectState.Option} type in JSON format. */
		String OPTION__TYPE = "Option";

		/** @see #getValue() */
		String VALUE__PROP = "value";

		/** @see #getLabel() */
		String LABEL__PROP = "label";

		/**
		 * The value, in its JSON form.
		 */
		Object getValue();

		/**
		 * @see #getValue()
		 */
		com.top_logic.layout.react.state.SelectState.Option setValue(Object value);

		/**
		 * Checks, whether {@link #getValue()} has a value.
		 */
		boolean hasValue();

		/**
		 * The label of the value.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		com.top_logic.layout.react.state.SelectState.Option setLabel(String value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.SelectState.Option readOption(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.SelectState_Impl.Option_Impl result = new com.top_logic.layout.react.state.impl.SelectState_Impl.Option_Impl();
			result.readContent(in);
			return result;
		}

	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.SelectState} instance.
	 */
	static com.top_logic.layout.react.state.SelectState create() {
		return new com.top_logic.layout.react.state.impl.SelectState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.SelectState} type in JSON format. */
	String SELECT_STATE__TYPE = "SelectState";

	/** @see #getOptions() */
	String OPTIONS__PROP = "options";

	/**
	 * The values that can be chosen. A value the field holds is among them, also where it cannot
	 * be chosen anew.
	 */
	java.util.List<com.top_logic.layout.react.state.SelectState.Option> getOptions();

	/**
	 * @see #getOptions()
	 */
	com.top_logic.layout.react.state.SelectState setOptions(java.util.List<? extends com.top_logic.layout.react.state.SelectState.Option> value);

	/**
	 * Adds a value to the {@link #getOptions()} list.
	 */
	com.top_logic.layout.react.state.SelectState addOption(com.top_logic.layout.react.state.SelectState.Option value);

	/**
	 * Removes a value from the {@link #getOptions()} list.
	 */
	void removeOption(com.top_logic.layout.react.state.SelectState.Option value);

	@Override
	com.top_logic.layout.react.state.SelectState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.SelectState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.SelectState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.SelectState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.SelectState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.SelectState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.SelectState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.SelectState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.SelectState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.SelectState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.SelectState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.SelectState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.SelectState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.SelectState readSelectState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.SelectState_Impl result = new com.top_logic.layout.react.state.impl.SelectState_Impl();
		result.readContent(in);
		return result;
	}

}
