package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.FieldState}.
 */
public class FieldState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.FieldState {

	private Object _value = null;

	private boolean _editable = false;

	private boolean _mandatory = false;

	private boolean _nullable = false;

	private boolean _hasError = false;

	private String _errorMessage = "";

	private boolean _hasWarnings = false;

	private String _label = "";

	private String _tooltip = "";

	private String _placeholder = "";

	private boolean _submitOnEnter = false;

	/**
	 * Creates a {@link FieldState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.FieldState#create()
	 */
	public FieldState_Impl() {
		super();
	}

	@Override
	public final Object getValue() {
		return _value;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setValue(Object value) {
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
	public final boolean isEditable() {
		return _editable;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	/** Internal setter for {@link #isEditable()} without chain call utility. */
	protected final void internalSetEditable(boolean value) {
		_editable = value;
	}

	@Override
	public final boolean isMandatory() {
		return _mandatory;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	/** Internal setter for {@link #isMandatory()} without chain call utility. */
	protected final void internalSetMandatory(boolean value) {
		_mandatory = value;
	}

	@Override
	public final boolean isNullable() {
		return _nullable;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	/** Internal setter for {@link #isNullable()} without chain call utility. */
	protected final void internalSetNullable(boolean value) {
		_nullable = value;
	}

	@Override
	public final boolean isHasError() {
		return _hasError;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	/** Internal setter for {@link #isHasError()} without chain call utility. */
	protected final void internalSetHasError(boolean value) {
		_hasError = value;
	}

	@Override
	public final String getErrorMessage() {
		return _errorMessage;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	/** Internal setter for {@link #getErrorMessage()} without chain call utility. */
	protected final void internalSetErrorMessage(String value) {
		_errorMessage = value;
	}

	@Override
	public final boolean isHasWarnings() {
		return _hasWarnings;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	/** Internal setter for {@link #isHasWarnings()} without chain call utility. */
	protected final void internalSetHasWarnings(boolean value) {
		_hasWarnings = value;
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public final String getTooltip() {
		return _tooltip;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	/** Internal setter for {@link #getTooltip()} without chain call utility. */
	protected final void internalSetTooltip(String value) {
		_tooltip = value;
	}

	@Override
	public final String getPlaceholder() {
		return _placeholder;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	/** Internal setter for {@link #getPlaceholder()} without chain call utility. */
	protected final void internalSetPlaceholder(String value) {
		_placeholder = value;
	}

	@Override
	public final boolean isSubmitOnEnter() {
		return _submitOnEnter;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	/** Internal setter for {@link #isSubmitOnEnter()} without chain call utility. */
	protected final void internalSetSubmitOnEnter(boolean value) {
		_submitOnEnter = value;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.FieldState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return FIELD_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasValue()) {
			out.name(VALUE__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getValue());
		}
		out.name(EDITABLE__PROP);
		out.value(isEditable());
		out.name(MANDATORY__PROP);
		out.value(isMandatory());
		out.name(NULLABLE__PROP);
		out.value(isNullable());
		out.name(HAS_ERROR__PROP);
		out.value(isHasError());
		out.name(ERROR_MESSAGE__PROP);
		out.value(getErrorMessage());
		out.name(HAS_WARNINGS__PROP);
		out.value(isHasWarnings());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(TOOLTIP__PROP);
		out.value(getTooltip());
		out.name(PLACEHOLDER__PROP);
		out.value(getPlaceholder());
		out.name(SUBMIT_ON_ENTER__PROP);
		out.value(isSubmitOnEnter());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VALUE__PROP: setValue(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case EDITABLE__PROP: setEditable(in.nextBoolean()); break;
			case MANDATORY__PROP: setMandatory(in.nextBoolean()); break;
			case NULLABLE__PROP: setNullable(in.nextBoolean()); break;
			case HAS_ERROR__PROP: setHasError(in.nextBoolean()); break;
			case ERROR_MESSAGE__PROP: setErrorMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HAS_WARNINGS__PROP: setHasWarnings(in.nextBoolean()); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOLTIP__PROP: setTooltip(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PLACEHOLDER__PROP: setPlaceholder(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SUBMIT_ON_ENTER__PROP: setSubmitOnEnter(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
