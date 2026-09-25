package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.SelectState}.
 */
public class SelectState_Impl extends com.top_logic.layout.react.state.impl.FieldState_Impl implements com.top_logic.layout.react.state.SelectState {
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.SelectState.Option}.
	 */
	public static class Option_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.SelectState.Option {

		private Object _value = null;

		private String _label = "";

		/**
		 * Creates a {@link Option_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.SelectState.Option#create()
		 */
		public Option_Impl() {
			super();
		}

		@Override
		public final Object getValue() {
			return _value;
		}

		@Override
		public com.top_logic.layout.react.state.SelectState.Option setValue(Object value) {
			internalSetValue(value);
			return this;
		}

		/** Internal setter for {@link #getValue()} without chain call utility. */
		protected final void internalSetValue(Object value) {
			_value = value;
		}

		@Override
		public final boolean hasValue() {
			return _value != null;
		}

		@Override
		public final String getLabel() {
			return _label;
		}

		@Override
		public com.top_logic.layout.react.state.SelectState.Option setLabel(String value) {
			internalSetLabel(value);
			return this;
		}

		/** Internal setter for {@link #getLabel()} without chain call utility. */
		protected final void internalSetLabel(String value) {
			_label = value;
		}

		@Override
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			writeContent(out);
		}

		@Override
		protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			super.writeFields(out);
			if (hasValue()) {
				out.name(VALUE__PROP);
				de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getValue());
			}
			out.name(LABEL__PROP);
			out.value(getLabel());
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case VALUE__PROP: setValue(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
				case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				default: super.readField(in, field);
			}
		}

	}

	private final java.util.List<com.top_logic.layout.react.state.SelectState.Option> _options = new java.util.ArrayList<>();

	/**
	 * Creates a {@link SelectState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.SelectState#create()
	 */
	public SelectState_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.SelectState.Option> getOptions() {
		return _options;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setOptions(java.util.List<? extends com.top_logic.layout.react.state.SelectState.Option> value) {
		internalSetOptions(value);
		return this;
	}

	/** Internal setter for {@link #getOptions()} without chain call utility. */
	protected final void internalSetOptions(java.util.List<? extends com.top_logic.layout.react.state.SelectState.Option> value) {
		if (value == null) throw new IllegalArgumentException("Property 'options' cannot be null.");
		_options.clear();
		_options.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.SelectState addOption(com.top_logic.layout.react.state.SelectState.Option value) {
		internalAddOption(value);
		return this;
	}

	/** Implementation of {@link #addOption(com.top_logic.layout.react.state.SelectState.Option)} without chain call utility. */
	protected final void internalAddOption(com.top_logic.layout.react.state.SelectState.Option value) {
		_options.add(value);
	}

	@Override
	public final void removeOption(com.top_logic.layout.react.state.SelectState.Option value) {
		_options.remove(value);
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SelectState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return SELECT_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(OPTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.SelectState.Option x : getOptions()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OPTIONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.SelectState.Option> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.SelectState.Option.readOption(in));
				}
				in.endArray();
				setOptions(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
