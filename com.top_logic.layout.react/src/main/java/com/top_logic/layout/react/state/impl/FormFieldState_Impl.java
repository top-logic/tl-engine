package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.FormFieldState}.
 */
public class FormFieldState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.FormFieldState {

	private String _label = "";

	private boolean _required = false;

	private String _error = "";

	private String _errorIcon = "";

	private final java.util.List<String> _warnings = new java.util.ArrayList<>();

	private String _warningIcon = "";

	private String _helpText = "";

	private String _tooltipText = "";

	private boolean _hasTooltip = false;

	private boolean _dirty = false;

	private com.top_logic.layout.react.state.FormFieldState.LabelPosition _labelPosition = com.top_logic.layout.react.state.FormFieldState.LabelPosition.SIDE;

	private boolean _fullLine = false;

	private boolean _visible = false;

	private com.top_logic.layout.react.state.ChildControl _field = null;

	/**
	 * Creates a {@link FormFieldState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.FormFieldState#create()
	 */
	public FormFieldState_Impl() {
		super();
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public final boolean isRequired() {
		return _required;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setRequired(boolean value) {
		internalSetRequired(value);
		return this;
	}

	/** Internal setter for {@link #isRequired()} without chain call utility. */
	protected final void internalSetRequired(boolean value) {
		_required = value;
	}

	@Override
	public final String getError() {
		return _error;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setError(String value) {
		internalSetError(value);
		return this;
	}

	/** Internal setter for {@link #getError()} without chain call utility. */
	protected final void internalSetError(String value) {
		_error = value;
	}

	@Override
	public final String getErrorIcon() {
		return _errorIcon;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setErrorIcon(String value) {
		internalSetErrorIcon(value);
		return this;
	}

	/** Internal setter for {@link #getErrorIcon()} without chain call utility. */
	protected final void internalSetErrorIcon(String value) {
		_errorIcon = value;
	}

	@Override
	public final java.util.List<String> getWarnings() {
		return _warnings;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setWarnings(java.util.List<? extends String> value) {
		internalSetWarnings(value);
		return this;
	}

	/** Internal setter for {@link #getWarnings()} without chain call utility. */
	protected final void internalSetWarnings(java.util.List<? extends String> value) {
		_warnings.clear();
		_warnings.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState addWarning(String value) {
		internalAddWarning(value);
		return this;
	}

	/** Implementation of {@link #addWarning(String)} without chain call utility. */
	protected final void internalAddWarning(String value) {
		_warnings.add(value);
	}

	@Override
	public final void removeWarning(String value) {
		_warnings.remove(value);
	}

	@Override
	public final String getWarningIcon() {
		return _warningIcon;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setWarningIcon(String value) {
		internalSetWarningIcon(value);
		return this;
	}

	/** Internal setter for {@link #getWarningIcon()} without chain call utility. */
	protected final void internalSetWarningIcon(String value) {
		_warningIcon = value;
	}

	@Override
	public final String getHelpText() {
		return _helpText;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setHelpText(String value) {
		internalSetHelpText(value);
		return this;
	}

	/** Internal setter for {@link #getHelpText()} without chain call utility. */
	protected final void internalSetHelpText(String value) {
		_helpText = value;
	}

	@Override
	public final String getTooltipText() {
		return _tooltipText;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setTooltipText(String value) {
		internalSetTooltipText(value);
		return this;
	}

	/** Internal setter for {@link #getTooltipText()} without chain call utility. */
	protected final void internalSetTooltipText(String value) {
		_tooltipText = value;
	}

	@Override
	public final boolean isHasTooltip() {
		return _hasTooltip;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setHasTooltip(boolean value) {
		internalSetHasTooltip(value);
		return this;
	}

	/** Internal setter for {@link #isHasTooltip()} without chain call utility. */
	protected final void internalSetHasTooltip(boolean value) {
		_hasTooltip = value;
	}

	@Override
	public final boolean isDirty() {
		return _dirty;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setDirty(boolean value) {
		internalSetDirty(value);
		return this;
	}

	/** Internal setter for {@link #isDirty()} without chain call utility. */
	protected final void internalSetDirty(boolean value) {
		_dirty = value;
	}

	@Override
	public final com.top_logic.layout.react.state.FormFieldState.LabelPosition getLabelPosition() {
		return _labelPosition;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setLabelPosition(com.top_logic.layout.react.state.FormFieldState.LabelPosition value) {
		internalSetLabelPosition(value);
		return this;
	}

	/** Internal setter for {@link #getLabelPosition()} without chain call utility. */
	protected final void internalSetLabelPosition(com.top_logic.layout.react.state.FormFieldState.LabelPosition value) {
		if (value == null) throw new IllegalArgumentException("Property 'labelPosition' cannot be null.");
		_labelPosition = value;
	}

	@Override
	public final boolean isFullLine() {
		return _fullLine;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setFullLine(boolean value) {
		internalSetFullLine(value);
		return this;
	}

	/** Internal setter for {@link #isFullLine()} without chain call utility. */
	protected final void internalSetFullLine(boolean value) {
		_fullLine = value;
	}

	@Override
	public final boolean isVisible() {
		return _visible;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setVisible(boolean value) {
		internalSetVisible(value);
		return this;
	}

	/** Internal setter for {@link #isVisible()} without chain call utility. */
	protected final void internalSetVisible(boolean value) {
		_visible = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getField() {
		return _field;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setField(com.top_logic.layout.react.state.ChildControl value) {
		internalSetField(value);
		return this;
	}

	/** Internal setter for {@link #getField()} without chain call utility. */
	protected final void internalSetField(com.top_logic.layout.react.state.ChildControl value) {
		_field = value;
	}

	@Override
	public final boolean hasField() {
		return _field != null;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.FormFieldState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return FORM_FIELD_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(REQUIRED__PROP);
		out.value(isRequired());
		out.name(ERROR__PROP);
		out.value(getError());
		out.name(ERROR_ICON__PROP);
		out.value(getErrorIcon());
		out.name(WARNINGS__PROP);
		out.beginArray();
		for (String x : getWarnings()) {
			out.value(x);
		}
		out.endArray();
		out.name(WARNING_ICON__PROP);
		out.value(getWarningIcon());
		out.name(HELP_TEXT__PROP);
		out.value(getHelpText());
		out.name(TOOLTIP_TEXT__PROP);
		out.value(getTooltipText());
		out.name(HAS_TOOLTIP__PROP);
		out.value(isHasTooltip());
		out.name(DIRTY__PROP);
		out.value(isDirty());
		out.name(LABEL_POSITION__PROP);
		getLabelPosition().writeTo(out);
		out.name(FULL_LINE__PROP);
		out.value(isFullLine());
		out.name(VISIBLE__PROP);
		out.value(isVisible());
		if (hasField()) {
			out.name(FIELD__PROP);
			getField().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REQUIRED__PROP: setRequired(in.nextBoolean()); break;
			case ERROR__PROP: setError(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ERROR_ICON__PROP: setErrorIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case WARNINGS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setWarnings(newValue);
			}
			break;
			case WARNING_ICON__PROP: setWarningIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HELP_TEXT__PROP: setHelpText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOLTIP_TEXT__PROP: setTooltipText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HAS_TOOLTIP__PROP: setHasTooltip(in.nextBoolean()); break;
			case DIRTY__PROP: setDirty(in.nextBoolean()); break;
			case LABEL_POSITION__PROP: setLabelPosition(com.top_logic.layout.react.state.FormFieldState.LabelPosition.readLabelPosition(in)); break;
			case FULL_LINE__PROP: setFullLine(in.nextBoolean()); break;
			case VISIBLE__PROP: setVisible(in.nextBoolean()); break;
			case FIELD__PROP: setField(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			default: super.readField(in, field);
		}
	}

}
