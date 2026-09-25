package com.top_logic.layout.react.state;

/**
 * State properties every form field carries.
 */
public interface FieldState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.FieldState} instance.
	 */
	static com.top_logic.layout.react.state.FieldState create() {
		return new com.top_logic.layout.react.state.impl.FieldState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.FieldState} type in JSON format. */
	String FIELD_STATE__TYPE = "FieldState";

	/** @see #getValue() */
	String VALUE__PROP = "value";

	/** @see #isEditable() */
	String EDITABLE__PROP = "editable";

	/** @see #isMandatory() */
	String MANDATORY__PROP = "mandatory";

	/** @see #isNullable() */
	String NULLABLE__PROP = "nullable";

	/** @see #isHasError() */
	String HAS_ERROR__PROP = "hasError";

	/** @see #getErrorMessage() */
	String ERROR_MESSAGE__PROP = "errorMessage";

	/** @see #isHasWarnings() */
	String HAS_WARNINGS__PROP = "hasWarnings";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #getTooltip() */
	String TOOLTIP__PROP = "tooltip";

	/** @see #getPlaceholder() */
	String PLACEHOLDER__PROP = "placeholder";

	/** @see #isSubmitOnEnter() */
	String SUBMIT_ON_ENTER__PROP = "submitOnEnter";

	/**
	 * The value of the field. Its JSON form depends on the kind of the field, see the state of the
	 * concrete field component.
	 */
	Object getValue();

	/**
	 * @see #getValue()
	 */
	com.top_logic.layout.react.state.FieldState setValue(Object value);

	/**
	 * Checks, whether {@link #getValue()} has a value.
	 */
	boolean hasValue();

	/**
	 * Whether the value can be edited. A field that cannot be edited displays its value only.
	 */
	boolean isEditable();

	/**
	 * @see #isEditable()
	 */
	com.top_logic.layout.react.state.FieldState setEditable(boolean value);

	/**
	 * Whether a value is required.
	 */
	boolean isMandatory();

	/**
	 * @see #isMandatory()
	 */
	com.top_logic.layout.react.state.FieldState setMandatory(boolean value);

	/**
	 * Whether the empty value is a legal value of the field.
	 */
	boolean isNullable();

	/**
	 * @see #isNullable()
	 */
	com.top_logic.layout.react.state.FieldState setNullable(boolean value);

	/**
	 * Whether the value of the field is invalid, see {@link #getErrorMessage()}.
	 */
	boolean isHasError();

	/**
	 * @see #isHasError()
	 */
	com.top_logic.layout.react.state.FieldState setHasError(boolean value);

	/**
	 * The message describing the error, while {@link #isHasError()} is set.
	 */
	String getErrorMessage();

	/**
	 * @see #getErrorMessage()
	 */
	com.top_logic.layout.react.state.FieldState setErrorMessage(String value);

	/**
	 * Whether the value of the field is questionable without being invalid.
	 */
	boolean isHasWarnings();

	/**
	 * @see #isHasWarnings()
	 */
	com.top_logic.layout.react.state.FieldState setHasWarnings(boolean value);

	/**
	 * The label of the field.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.FieldState setLabel(String value);

	/**
	 * The tooltip of the field (plain text).
	 */
	String getTooltip();

	/**
	 * @see #getTooltip()
	 */
	com.top_logic.layout.react.state.FieldState setTooltip(String value);

	/**
	 * The text shown while the field is empty and editable.
	 */
	String getPlaceholder();

	/**
	 * @see #getPlaceholder()
	 */
	com.top_logic.layout.react.state.FieldState setPlaceholder(String value);

	/**
	 * Whether pressing Enter in the field sends the submit command with the value typed.
	 */
	boolean isSubmitOnEnter();

	/**
	 * @see #isSubmitOnEnter()
	 */
	com.top_logic.layout.react.state.FieldState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.FieldState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.FieldState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.FieldState readFieldState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.FieldState_Impl result = new com.top_logic.layout.react.state.impl.FieldState_Impl();
		result.readContent(in);
		return result;
	}

}
