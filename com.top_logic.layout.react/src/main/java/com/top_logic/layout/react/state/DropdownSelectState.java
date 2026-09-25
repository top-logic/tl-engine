package com.top_logic.layout.react.state;

/**
 * State of a field choosing one or more objects, the components {@code TLDropdownSelect} (a list
 * that opens on demand), {@code TLOptionChips} (every option a toggle of its own) and
 * {@code TLSegmentedChoice} (the options as the segments of one bar).
 *
 * The {@link #getValue()} is the list of the chosen objects, each an {@link Option} (also for a field
 * choosing one object). A change is sent as the command {@code valueChanged} with the list of the
 * {@link Option#getValue()}s of the chosen options as argument {@code value}.
 */
public interface DropdownSelectState extends com.top_logic.layout.react.state.FieldState {

	/**
	 * The shape the options are offered in.
	 *
	 * The control does not send {@link #DROPDOWN}: an absent display means a list that opens on
	 * demand.
	 */
	public enum Display implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A list that opens on demand.
		 */
		DROPDOWN("dropdown"),

		/**
		 * Every option a toggle of its own.
		 */
		CHIPS("chips"),

		/**
		 * The options as the segments of one bar.
		 */
		SEGMENTED("segmented"),

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
				case "dropdown": return DROPDOWN;
				case "chips": return CHIPS;
				case "segmented": return SEGMENTED;
			}
			return DROPDOWN;
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
				case DROPDOWN: out.value(1); break;
				case CHIPS: out.value(2); break;
				case SEGMENTED: out.value(3); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static Display readDisplay(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return DROPDOWN;
				case 2: return CHIPS;
				case 3: return SEGMENTED;
				default: return DROPDOWN;
			}
		}
	}
	/**
	 * An object that can be chosen.
	 */
	public interface Option extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.DropdownSelectState.Option} instance.
		 */
		static com.top_logic.layout.react.state.DropdownSelectState.Option create() {
			return new com.top_logic.layout.react.state.impl.DropdownSelectState_Impl.Option_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.DropdownSelectState.Option} type in JSON format. */
		String OPTION__TYPE = "Option";

		/** @see #getValue() */
		String VALUE__PROP = "value";

		/** @see #getLabel() */
		String LABEL__PROP = "label";

		/** @see #getImage() */
		String IMAGE__PROP = "image";

		/** @see #getColor() */
		String COLOR__PROP = "color";

		/** @see #isLink() */
		String LINK__PROP = "link";

		/**
		 * The ID of the option, by which the client names it to the server.
		 */
		String getValue();

		/**
		 * @see #getValue()
		 */
		com.top_logic.layout.react.state.DropdownSelectState.Option setValue(String value);

		/**
		 * The label of the object.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		com.top_logic.layout.react.state.DropdownSelectState.Option setLabel(String value);

		/**
		 * The icon of the object, the encoded form of a theme image.
		 */
		String getImage();

		/**
		 * @see #getImage()
		 */
		com.top_logic.layout.react.state.DropdownSelectState.Option setImage(String value);

		/**
		 * The CSS color the object carries in the model.
		 */
		String getColor();

		/**
		 * @see #getColor()
		 */
		com.top_logic.layout.react.state.DropdownSelectState.Option setColor(String value);

		/**
		 * Whether the option leads to the place the application displays the object at, when
		 * followed: the command {@code goto} with the {@link #getValue()} as argument {@code option}.
		 * Only set on the chosen options of a field that is not editable.
		 */
		boolean isLink();

		/**
		 * @see #isLink()
		 */
		com.top_logic.layout.react.state.DropdownSelectState.Option setLink(boolean value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.DropdownSelectState.Option readOption(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.DropdownSelectState_Impl.Option_Impl result = new com.top_logic.layout.react.state.impl.DropdownSelectState_Impl.Option_Impl();
			result.readContent(in);
			return result;
		}

	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.DropdownSelectState} instance.
	 */
	static com.top_logic.layout.react.state.DropdownSelectState create() {
		return new com.top_logic.layout.react.state.impl.DropdownSelectState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.DropdownSelectState} type in JSON format. */
	String DROPDOWN_SELECT_STATE__TYPE = "DropdownSelectState";

	/** @see #getDisplay() */
	String DISPLAY__PROP = "display";

	/** @see #getOptions() */
	String OPTIONS__PROP = "options";

	/** @see #isOptionsLoaded() */
	String OPTIONS_LOADED__PROP = "optionsLoaded";

	/** @see #isCustomOrder() */
	String CUSTOM_ORDER__PROP = "customOrder";

	/** @see #isMultiSelect() */
	String MULTI_SELECT__PROP = "multiSelect";

	/** @see #getEmptyOptionLabel() */
	String EMPTY_OPTION_LABEL__PROP = "emptyOptionLabel";

	/**
	 * The shape the options are offered in. Absent means {@link Display#DROPDOWN}, a list that
	 * opens on demand.
	 */
	com.top_logic.layout.react.state.DropdownSelectState.Display getDisplay();

	/**
	 * @see #getDisplay()
	 */
	com.top_logic.layout.react.state.DropdownSelectState setDisplay(com.top_logic.layout.react.state.DropdownSelectState.Display value);

	/**
	 * The objects that can be chosen, valid while {@link #isOptionsLoaded()} is set. A list that opens
	 * on demand asks for them with the command {@code loadOptions}.
	 */
	java.util.List<com.top_logic.layout.react.state.DropdownSelectState.Option> getOptions();

	/**
	 * @see #getOptions()
	 */
	com.top_logic.layout.react.state.DropdownSelectState setOptions(java.util.List<? extends com.top_logic.layout.react.state.DropdownSelectState.Option> value);

	/**
	 * Adds a value to the {@link #getOptions()} list.
	 */
	com.top_logic.layout.react.state.DropdownSelectState addOption(com.top_logic.layout.react.state.DropdownSelectState.Option value);

	/**
	 * Removes a value from the {@link #getOptions()} list.
	 */
	void removeOption(com.top_logic.layout.react.state.DropdownSelectState.Option value);

	/**
	 * Whether {@link #getOptions()} is up to date.
	 */
	boolean isOptionsLoaded();

	/**
	 * @see #isOptionsLoaded()
	 */
	com.top_logic.layout.react.state.DropdownSelectState setOptionsLoaded(boolean value);

	/**
	 * Whether the chosen objects are kept in the order the user gives them, instead of being
	 * sorted.
	 */
	boolean isCustomOrder();

	/**
	 * @see #isCustomOrder()
	 */
	com.top_logic.layout.react.state.DropdownSelectState setCustomOrder(boolean value);

	/**
	 * Whether more than one object can be chosen.
	 */
	boolean isMultiSelect();

	/**
	 * @see #isMultiSelect()
	 */
	com.top_logic.layout.react.state.DropdownSelectState setMultiSelect(boolean value);

	/**
	 * The label of the choice of no object.
	 */
	String getEmptyOptionLabel();

	/**
	 * @see #getEmptyOptionLabel()
	 */
	com.top_logic.layout.react.state.DropdownSelectState setEmptyOptionLabel(String value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.DropdownSelectState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.DropdownSelectState readDropdownSelectState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.DropdownSelectState_Impl result = new com.top_logic.layout.react.state.impl.DropdownSelectState_Impl();
		result.readContent(in);
		return result;
	}

}
