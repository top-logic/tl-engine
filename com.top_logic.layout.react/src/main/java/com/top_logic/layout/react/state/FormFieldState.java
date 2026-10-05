package com.top_logic.layout.react.state;

/**
 * State of the frame around a form field, the component {@code TLFormField}: the label, the
 * marks for a required and a modified field, the error and warning messages and the help text,
 * around the control of the input itself ({@link #getField()}).
 *
 * The frame is not a field of its own: the value, its editability and its validation belong to the
 * embedded {@link #getField()}. A label that names its input states this to the input through
 * {@code FieldLabelContext} of 'tl-react-bridge'. Messages, help and the mark for a required
 * field belong to editing: the component omits them inside a read-only form layout. Whether the
 * form layout around the field is read-only, and the label position it resolved for fields that
 * state none ({@link #getLabelPosition()}), are read with {@code useFormLayout} of 'tl-react-bridge'.
 * The component sends no commands.
 */
public interface FormFieldState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Where the label of a field stands relative to its input.
	 */
	public enum LabelPosition implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * The label beside the input.
		 */
		SIDE("side"),

		/**
		 * The label above the input.
		 */
		TOP("top"),

		/**
		 * The label after the input, e.g. trailing a checkbox.
		 */
		AFTER("after"),

		/**
		 * No visible label; the input spans the full width of the field. The label text still names
		 * the input for assistive technology.
		 */
		HIDDEN("hidden"),

		/**
		 * {@link #SIDE} or {@link #TOP}, chosen from the width available. A position of a form
		 * layout, which resolves it for the fields it contains; a field is not given it.
		 */
		AUTO("auto"),

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
				case "side": return SIDE;
				case "top": return TOP;
				case "after": return AFTER;
				case "hidden": return HIDDEN;
				case "auto": return AUTO;
			}
			return SIDE;
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
	 * Creates a {@link com.top_logic.layout.react.state.FormFieldState} instance.
	 */
	static com.top_logic.layout.react.state.FormFieldState create() {
		return new com.top_logic.layout.react.state.impl.FormFieldState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.FormFieldState} type in JSON format. */
	String FORM_FIELD_STATE__TYPE = "FormFieldState";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #isRequired() */
	String REQUIRED__PROP = "required";

	/** @see #getError() */
	String ERROR__PROP = "error";

	/** @see #getErrorIcon() */
	String ERROR_ICON__PROP = "errorIcon";

	/** @see #getWarnings() */
	String WARNINGS__PROP = "warnings";

	/** @see #getWarningIcon() */
	String WARNING_ICON__PROP = "warningIcon";

	/** @see #getHelpText() */
	String HELP_TEXT__PROP = "helpText";

	/** @see #getTooltipText() */
	String TOOLTIP_TEXT__PROP = "tooltipText";

	/** @see #isHasTooltip() */
	String HAS_TOOLTIP__PROP = "hasTooltip";

	/** @see #isDirty() */
	String DIRTY__PROP = "dirty";

	/** @see #getLabelPosition() */
	String LABEL_POSITION__PROP = "labelPosition";

	/** @see #isFullLine() */
	String FULL_LINE__PROP = "fullLine";

	/** @see #isVisible() */
	String VISIBLE__PROP = "visible";

	/** @see #getField() */
	String FIELD__PROP = "field";

	/**
	 * The label of the field, plain text. Absent or empty: no label, and the input is not named by
	 * one.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.FormFieldState setLabel(String value);

	/**
	 * Whether a value is required, marked next to the label.
	 */
	boolean isRequired();

	/**
	 * @see #isRequired()
	 */
	com.top_logic.layout.react.state.FormFieldState setRequired(boolean value);

	/**
	 * The message describing why the value of the {@link #getField()} is invalid. Absent: no error.
	 */
	String getError();

	/**
	 * @see #getError()
	 */
	com.top_logic.layout.react.state.FormFieldState setError(String value);

	/**
	 * The icon shown in front of the {@link #getError()} message, the encoded form of a theme image.
	 */
	String getErrorIcon();

	/**
	 * @see #getErrorIcon()
	 */
	com.top_logic.layout.react.state.FormFieldState setErrorIcon(String value);

	/**
	 * The messages describing why the value of the {@link #getField()} is questionable, shown while
	 * there is no {@link #getError()}. Absent: no warnings.
	 */
	java.util.List<String> getWarnings();

	/**
	 * @see #getWarnings()
	 */
	com.top_logic.layout.react.state.FormFieldState setWarnings(java.util.List<? extends String> value);

	/**
	 * Adds a value to the {@link #getWarnings()} list.
	 */
	com.top_logic.layout.react.state.FormFieldState addWarning(String value);

	/**
	 * Removes a value from the {@link #getWarnings()} list.
	 */
	void removeWarning(String value);

	/**
	 * The icon shown in front of each of the {@link #getWarnings()}, the encoded form of a theme image.
	 */
	String getWarningIcon();

	/**
	 * @see #getWarningIcon()
	 */
	com.top_logic.layout.react.state.FormFieldState setWarningIcon(String value);

	/**
	 * A text explaining the field, plain text, shown on demand below the input. Absent: no help.
	 */
	String getHelpText();

	/**
	 * @see #getHelpText()
	 */
	com.top_logic.layout.react.state.FormFieldState setHelpText(String value);

	/**
	 * A description of the field, plain text, offered as the tooltip of its label. Absent: none.
	 * A rich tooltip ({@link #isHasTooltip()}) takes precedence.
	 */
	String getTooltipText();

	/**
	 * @see #getTooltipText()
	 */
	com.top_logic.layout.react.state.FormFieldState setTooltipText(String value);

	/**
	 * Whether the server offers a rich tooltip for the label, HTML fetched from the control under
	 * the key {@code tooltip} (the attribute {@code data-tooltip} with the value
		 * {@code key:tooltip}). Takes precedence over {@link #getTooltipText()}.
	 */
	boolean isHasTooltip();

	/**
	 * @see #isHasTooltip()
	 */
	com.top_logic.layout.react.state.FormFieldState setHasTooltip(boolean value);

	/**
	 * Whether the value of the field has been modified and not yet saved.
	 */
	boolean isDirty();

	/**
	 * @see #isDirty()
	 */
	com.top_logic.layout.react.state.FormFieldState setDirty(boolean value);

	/**
	 * Where the label stands relative to the input. Absent: the position the enclosing form layout
	 * gives its fields.
	 */
	com.top_logic.layout.react.state.FormFieldState.LabelPosition getLabelPosition();

	/**
	 * @see #getLabelPosition()
	 */
	com.top_logic.layout.react.state.FormFieldState setLabelPosition(com.top_logic.layout.react.state.FormFieldState.LabelPosition value);

	/**
	 * Whether the field spans the full row of a form layout of several columns.
	 */
	boolean isFullLine();

	/**
	 * @see #isFullLine()
	 */
	com.top_logic.layout.react.state.FormFieldState setFullLine(boolean value);

	/**
	 * Whether the field is shown. Absent: shown. A field not shown keeps its {@link #getField()}
	 * mounted, so that the field control keeps receiving its updates.
	 */
	boolean isVisible();

	/**
	 * @see #isVisible()
	 */
	com.top_logic.layout.react.state.FormFieldState setVisible(boolean value);

	/**
	 * The control of the input.
	 */
	com.top_logic.layout.react.state.ChildControl getField();

	/**
	 * @see #getField()
	 */
	com.top_logic.layout.react.state.FormFieldState setField(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getField()} has a value.
	 */
	boolean hasField();

	@Override
	com.top_logic.layout.react.state.FormFieldState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.FormFieldState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.FormFieldState readFormFieldState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.FormFieldState_Impl result = new com.top_logic.layout.react.state.impl.FormFieldState_Impl();
		result.readContent(in);
		return result;
	}

}
