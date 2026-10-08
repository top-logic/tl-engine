package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.TextState}.
 */
public class TextState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.TextState {

	private String _text = "";

	private com.top_logic.layout.react.state.TextState.Overflow _overflow = com.top_logic.layout.react.state.TextState.Overflow.WRAP;

	private com.top_logic.layout.react.state.TextState.Variant _variant = com.top_logic.layout.react.state.TextState.Variant.BODY;

	private com.top_logic.layout.react.state.TextState.Tone _tone = com.top_logic.layout.react.state.TextState.Tone.PRIMARY;

	private com.top_logic.layout.react.state.TextState.Appearance _appearance = com.top_logic.layout.react.state.TextState.Appearance.TEXT;

	private String _role = "";

	private String _colorRole = "";

	private boolean _hasTooltip = false;

	/**
	 * Creates a {@link TextState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.TextState#create()
	 */
	public TextState_Impl() {
		super();
	}

	@Override
	public final String getText() {
		return _text;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	@Override
	public final com.top_logic.layout.react.state.TextState.Overflow getOverflow() {
		return _overflow;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setOverflow(com.top_logic.layout.react.state.TextState.Overflow value) {
		internalSetOverflow(value);
		return this;
	}

	/** Internal setter for {@link #getOverflow()} without chain call utility. */
	protected final void internalSetOverflow(com.top_logic.layout.react.state.TextState.Overflow value) {
		if (value == null) throw new IllegalArgumentException("Property 'overflow' cannot be null.");
		_overflow = value;
	}

	@Override
	public final com.top_logic.layout.react.state.TextState.Variant getVariant() {
		return _variant;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setVariant(com.top_logic.layout.react.state.TextState.Variant value) {
		internalSetVariant(value);
		return this;
	}

	/** Internal setter for {@link #getVariant()} without chain call utility. */
	protected final void internalSetVariant(com.top_logic.layout.react.state.TextState.Variant value) {
		if (value == null) throw new IllegalArgumentException("Property 'variant' cannot be null.");
		_variant = value;
	}

	@Override
	public final com.top_logic.layout.react.state.TextState.Tone getTone() {
		return _tone;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setTone(com.top_logic.layout.react.state.TextState.Tone value) {
		internalSetTone(value);
		return this;
	}

	/** Internal setter for {@link #getTone()} without chain call utility. */
	protected final void internalSetTone(com.top_logic.layout.react.state.TextState.Tone value) {
		if (value == null) throw new IllegalArgumentException("Property 'tone' cannot be null.");
		_tone = value;
	}

	@Override
	public final com.top_logic.layout.react.state.TextState.Appearance getAppearance() {
		return _appearance;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setAppearance(com.top_logic.layout.react.state.TextState.Appearance value) {
		internalSetAppearance(value);
		return this;
	}

	/** Internal setter for {@link #getAppearance()} without chain call utility. */
	protected final void internalSetAppearance(com.top_logic.layout.react.state.TextState.Appearance value) {
		if (value == null) throw new IllegalArgumentException("Property 'appearance' cannot be null.");
		_appearance = value;
	}

	@Override
	public final String getRole() {
		return _role;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setRole(String value) {
		internalSetRole(value);
		return this;
	}

	/** Internal setter for {@link #getRole()} without chain call utility. */
	protected final void internalSetRole(String value) {
		_role = value;
	}

	@Override
	public final String getColorRole() {
		return _colorRole;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setColorRole(String value) {
		internalSetColorRole(value);
		return this;
	}

	/** Internal setter for {@link #getColorRole()} without chain call utility. */
	protected final void internalSetColorRole(String value) {
		_colorRole = value;
	}

	@Override
	public final boolean isHasTooltip() {
		return _hasTooltip;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setHasTooltip(boolean value) {
		internalSetHasTooltip(value);
		return this;
	}

	/** Internal setter for {@link #isHasTooltip()} without chain call utility. */
	protected final void internalSetHasTooltip(boolean value) {
		_hasTooltip = value;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TextState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TEXT_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(OVERFLOW__PROP);
		getOverflow().writeTo(out);
		out.name(VARIANT__PROP);
		getVariant().writeTo(out);
		out.name(TONE__PROP);
		getTone().writeTo(out);
		out.name(APPEARANCE__PROP);
		getAppearance().writeTo(out);
		out.name(ROLE__PROP);
		out.value(getRole());
		out.name(COLOR_ROLE__PROP);
		out.value(getColorRole());
		out.name(HAS_TOOLTIP__PROP);
		out.value(isHasTooltip());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OVERFLOW__PROP: setOverflow(com.top_logic.layout.react.state.TextState.Overflow.readOverflow(in)); break;
			case VARIANT__PROP: setVariant(com.top_logic.layout.react.state.TextState.Variant.readVariant(in)); break;
			case TONE__PROP: setTone(com.top_logic.layout.react.state.TextState.Tone.readTone(in)); break;
			case APPEARANCE__PROP: setAppearance(com.top_logic.layout.react.state.TextState.Appearance.readAppearance(in)); break;
			case ROLE__PROP: setRole(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case COLOR_ROLE__PROP: setColorRole(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HAS_TOOLTIP__PROP: setHasTooltip(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
