package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.DropdownSelectState}.
 */
public class DropdownSelectState_Impl extends com.top_logic.layout.react.state.impl.FieldState_Impl implements com.top_logic.layout.react.state.DropdownSelectState {
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.DropdownSelectState.Option}.
	 */
	public static class Option_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.DropdownSelectState.Option {

		private String _value = "";

		private String _label = "";

		private String _image = "";

		private String _color = "";

		private boolean _link = false;

		/**
		 * Creates a {@link Option_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.DropdownSelectState.Option#create()
		 */
		public Option_Impl() {
			super();
		}

		@Override
		public final String getValue() {
			return _value;
		}

		@Override
		public com.top_logic.layout.react.state.DropdownSelectState.Option setValue(String value) {
			internalSetValue(value);
			return this;
		}

		/** Internal setter for {@link #getValue()} without chain call utility. */
		protected final void internalSetValue(String value) {
			_value = value;
		}

		@Override
		public final String getLabel() {
			return _label;
		}

		@Override
		public com.top_logic.layout.react.state.DropdownSelectState.Option setLabel(String value) {
			internalSetLabel(value);
			return this;
		}

		/** Internal setter for {@link #getLabel()} without chain call utility. */
		protected final void internalSetLabel(String value) {
			_label = value;
		}

		@Override
		public final String getImage() {
			return _image;
		}

		@Override
		public com.top_logic.layout.react.state.DropdownSelectState.Option setImage(String value) {
			internalSetImage(value);
			return this;
		}

		/** Internal setter for {@link #getImage()} without chain call utility. */
		protected final void internalSetImage(String value) {
			_image = value;
		}

		@Override
		public final String getColor() {
			return _color;
		}

		@Override
		public com.top_logic.layout.react.state.DropdownSelectState.Option setColor(String value) {
			internalSetColor(value);
			return this;
		}

		/** Internal setter for {@link #getColor()} without chain call utility. */
		protected final void internalSetColor(String value) {
			_color = value;
		}

		@Override
		public final boolean isLink() {
			return _link;
		}

		@Override
		public com.top_logic.layout.react.state.DropdownSelectState.Option setLink(boolean value) {
			internalSetLink(value);
			return this;
		}

		/** Internal setter for {@link #isLink()} without chain call utility. */
		protected final void internalSetLink(boolean value) {
			_link = value;
		}

		@Override
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			writeContent(out);
		}

		@Override
		protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			super.writeFields(out);
			out.name(VALUE__PROP);
			out.value(getValue());
			out.name(LABEL__PROP);
			out.value(getLabel());
			out.name(IMAGE__PROP);
			out.value(getImage());
			out.name(COLOR__PROP);
			out.value(getColor());
			out.name(LINK__PROP);
			out.value(isLink());
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case VALUE__PROP: setValue(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case IMAGE__PROP: setImage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case COLOR__PROP: setColor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case LINK__PROP: setLink(in.nextBoolean()); break;
				default: super.readField(in, field);
			}
		}

	}

	private com.top_logic.layout.react.state.DropdownSelectState.Display _display = com.top_logic.layout.react.state.DropdownSelectState.Display.DROPDOWN;

	private final java.util.List<com.top_logic.layout.react.state.DropdownSelectState.Option> _options = new java.util.ArrayList<>();

	private boolean _optionsLoaded = false;

	private boolean _customOrder = false;

	private boolean _multiSelect = false;

	private String _emptyOptionLabel = "";

	/**
	 * Creates a {@link DropdownSelectState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.DropdownSelectState#create()
	 */
	public DropdownSelectState_Impl() {
		super();
	}

	@Override
	public final com.top_logic.layout.react.state.DropdownSelectState.Display getDisplay() {
		return _display;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setDisplay(com.top_logic.layout.react.state.DropdownSelectState.Display value) {
		internalSetDisplay(value);
		return this;
	}

	/** Internal setter for {@link #getDisplay()} without chain call utility. */
	protected final void internalSetDisplay(com.top_logic.layout.react.state.DropdownSelectState.Display value) {
		if (value == null) throw new IllegalArgumentException("Property 'display' cannot be null.");
		_display = value;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.DropdownSelectState.Option> getOptions() {
		return _options;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setOptions(java.util.List<? extends com.top_logic.layout.react.state.DropdownSelectState.Option> value) {
		internalSetOptions(value);
		return this;
	}

	/** Internal setter for {@link #getOptions()} without chain call utility. */
	protected final void internalSetOptions(java.util.List<? extends com.top_logic.layout.react.state.DropdownSelectState.Option> value) {
		if (value == null) throw new IllegalArgumentException("Property 'options' cannot be null.");
		_options.clear();
		_options.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState addOption(com.top_logic.layout.react.state.DropdownSelectState.Option value) {
		internalAddOption(value);
		return this;
	}

	/** Implementation of {@link #addOption(com.top_logic.layout.react.state.DropdownSelectState.Option)} without chain call utility. */
	protected final void internalAddOption(com.top_logic.layout.react.state.DropdownSelectState.Option value) {
		_options.add(value);
	}

	@Override
	public final void removeOption(com.top_logic.layout.react.state.DropdownSelectState.Option value) {
		_options.remove(value);
	}

	@Override
	public final boolean isOptionsLoaded() {
		return _optionsLoaded;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setOptionsLoaded(boolean value) {
		internalSetOptionsLoaded(value);
		return this;
	}

	/** Internal setter for {@link #isOptionsLoaded()} without chain call utility. */
	protected final void internalSetOptionsLoaded(boolean value) {
		_optionsLoaded = value;
	}

	@Override
	public final boolean isCustomOrder() {
		return _customOrder;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setCustomOrder(boolean value) {
		internalSetCustomOrder(value);
		return this;
	}

	/** Internal setter for {@link #isCustomOrder()} without chain call utility. */
	protected final void internalSetCustomOrder(boolean value) {
		_customOrder = value;
	}

	@Override
	public final boolean isMultiSelect() {
		return _multiSelect;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setMultiSelect(boolean value) {
		internalSetMultiSelect(value);
		return this;
	}

	/** Internal setter for {@link #isMultiSelect()} without chain call utility. */
	protected final void internalSetMultiSelect(boolean value) {
		_multiSelect = value;
	}

	@Override
	public final String getEmptyOptionLabel() {
		return _emptyOptionLabel;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setEmptyOptionLabel(String value) {
		internalSetEmptyOptionLabel(value);
		return this;
	}

	/** Internal setter for {@link #getEmptyOptionLabel()} without chain call utility. */
	protected final void internalSetEmptyOptionLabel(String value) {
		_emptyOptionLabel = value;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DropdownSelectState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DROPDOWN_SELECT_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(DISPLAY__PROP);
		getDisplay().writeTo(out);
		out.name(OPTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.DropdownSelectState.Option x : getOptions()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(OPTIONS_LOADED__PROP);
		out.value(isOptionsLoaded());
		out.name(CUSTOM_ORDER__PROP);
		out.value(isCustomOrder());
		out.name(MULTI_SELECT__PROP);
		out.value(isMultiSelect());
		out.name(EMPTY_OPTION_LABEL__PROP);
		out.value(getEmptyOptionLabel());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DISPLAY__PROP: setDisplay(com.top_logic.layout.react.state.DropdownSelectState.Display.readDisplay(in)); break;
			case OPTIONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.DropdownSelectState.Option> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.DropdownSelectState.Option.readOption(in));
				}
				in.endArray();
				setOptions(newValue);
			}
			break;
			case OPTIONS_LOADED__PROP: setOptionsLoaded(in.nextBoolean()); break;
			case CUSTOM_ORDER__PROP: setCustomOrder(in.nextBoolean()); break;
			case MULTI_SELECT__PROP: setMultiSelect(in.nextBoolean()); break;
			case EMPTY_OPTION_LABEL__PROP: setEmptyOptionLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
