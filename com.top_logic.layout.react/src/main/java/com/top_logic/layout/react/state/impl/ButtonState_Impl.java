package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.ButtonState}.
 */
public class ButtonState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.ButtonState {

	private String _label = "";

	private boolean _disabled = false;

	private boolean _active = false;

	private String _image = "";

	private String _tooltip = "";

	private com.top_logic.layout.react.state.ButtonState.DisplayMode _displayMode = com.top_logic.layout.react.state.ButtonState.DisplayMode.ICON_ONLY;

	private String _cssClasses = "";

	private com.top_logic.layout.react.state.ButtonState.Appearance _appearance = com.top_logic.layout.react.state.ButtonState.Appearance.PRIMARY;

	private com.top_logic.layout.react.state.ButtonState.Tone _tone = com.top_logic.layout.react.state.ButtonState.Tone.DANGER;

	private com.top_logic.layout.react.state.ButtonState.Size _size = com.top_logic.layout.react.state.ButtonState.Size.SMALL;

	private String _navigateUrl = "";

	private boolean _navigateNewWindow = false;

	private String _keyGesture = "";

	/**
	 * Creates a {@link ButtonState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.ButtonState#create()
	 */
	public ButtonState_Impl() {
		super();
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public final boolean isDisabled() {
		return _disabled;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setDisabled(boolean value) {
		internalSetDisabled(value);
		return this;
	}

	/** Internal setter for {@link #isDisabled()} without chain call utility. */
	protected final void internalSetDisabled(boolean value) {
		_disabled = value;
	}

	@Override
	public final boolean isActive() {
		return _active;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setActive(boolean value) {
		internalSetActive(value);
		return this;
	}

	/** Internal setter for {@link #isActive()} without chain call utility. */
	protected final void internalSetActive(boolean value) {
		_active = value;
	}

	@Override
	public final String getImage() {
		return _image;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setImage(String value) {
		internalSetImage(value);
		return this;
	}

	/** Internal setter for {@link #getImage()} without chain call utility. */
	protected final void internalSetImage(String value) {
		_image = value;
	}

	@Override
	public final String getTooltip() {
		return _tooltip;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	/** Internal setter for {@link #getTooltip()} without chain call utility. */
	protected final void internalSetTooltip(String value) {
		_tooltip = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ButtonState.DisplayMode getDisplayMode() {
		return _displayMode;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setDisplayMode(com.top_logic.layout.react.state.ButtonState.DisplayMode value) {
		internalSetDisplayMode(value);
		return this;
	}

	/** Internal setter for {@link #getDisplayMode()} without chain call utility. */
	protected final void internalSetDisplayMode(com.top_logic.layout.react.state.ButtonState.DisplayMode value) {
		if (value == null) throw new IllegalArgumentException("Property 'displayMode' cannot be null.");
		_displayMode = value;
	}

	@Override
	public final String getCssClasses() {
		return _cssClasses;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setCssClasses(String value) {
		internalSetCssClasses(value);
		return this;
	}

	/** Internal setter for {@link #getCssClasses()} without chain call utility. */
	protected final void internalSetCssClasses(String value) {
		_cssClasses = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ButtonState.Appearance getAppearance() {
		return _appearance;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setAppearance(com.top_logic.layout.react.state.ButtonState.Appearance value) {
		internalSetAppearance(value);
		return this;
	}

	/** Internal setter for {@link #getAppearance()} without chain call utility. */
	protected final void internalSetAppearance(com.top_logic.layout.react.state.ButtonState.Appearance value) {
		if (value == null) throw new IllegalArgumentException("Property 'appearance' cannot be null.");
		_appearance = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ButtonState.Tone getTone() {
		return _tone;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setTone(com.top_logic.layout.react.state.ButtonState.Tone value) {
		internalSetTone(value);
		return this;
	}

	/** Internal setter for {@link #getTone()} without chain call utility. */
	protected final void internalSetTone(com.top_logic.layout.react.state.ButtonState.Tone value) {
		if (value == null) throw new IllegalArgumentException("Property 'tone' cannot be null.");
		_tone = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ButtonState.Size getSize() {
		return _size;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setSize(com.top_logic.layout.react.state.ButtonState.Size value) {
		internalSetSize(value);
		return this;
	}

	/** Internal setter for {@link #getSize()} without chain call utility. */
	protected final void internalSetSize(com.top_logic.layout.react.state.ButtonState.Size value) {
		if (value == null) throw new IllegalArgumentException("Property 'size' cannot be null.");
		_size = value;
	}

	@Override
	public final String getNavigateUrl() {
		return _navigateUrl;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setNavigateUrl(String value) {
		internalSetNavigateUrl(value);
		return this;
	}

	/** Internal setter for {@link #getNavigateUrl()} without chain call utility. */
	protected final void internalSetNavigateUrl(String value) {
		_navigateUrl = value;
	}

	@Override
	public final boolean isNavigateNewWindow() {
		return _navigateNewWindow;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setNavigateNewWindow(boolean value) {
		internalSetNavigateNewWindow(value);
		return this;
	}

	/** Internal setter for {@link #isNavigateNewWindow()} without chain call utility. */
	protected final void internalSetNavigateNewWindow(boolean value) {
		_navigateNewWindow = value;
	}

	@Override
	public final String getKeyGesture() {
		return _keyGesture;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setKeyGesture(String value) {
		internalSetKeyGesture(value);
		return this;
	}

	/** Internal setter for {@link #getKeyGesture()} without chain call utility. */
	protected final void internalSetKeyGesture(String value) {
		_keyGesture = value;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ButtonState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return BUTTON_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DISABLED__PROP);
		out.value(isDisabled());
		out.name(ACTIVE__PROP);
		out.value(isActive());
		out.name(IMAGE__PROP);
		out.value(getImage());
		out.name(TOOLTIP__PROP);
		out.value(getTooltip());
		out.name(DISPLAY_MODE__PROP);
		getDisplayMode().writeTo(out);
		out.name(CSS_CLASSES__PROP);
		out.value(getCssClasses());
		out.name(APPEARANCE__PROP);
		getAppearance().writeTo(out);
		out.name(TONE__PROP);
		getTone().writeTo(out);
		out.name(SIZE__PROP);
		getSize().writeTo(out);
		out.name(NAVIGATE_URL__PROP);
		out.value(getNavigateUrl());
		out.name(NAVIGATE_NEW_WINDOW__PROP);
		out.value(isNavigateNewWindow());
		out.name(KEY_GESTURE__PROP);
		out.value(getKeyGesture());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DISABLED__PROP: setDisabled(in.nextBoolean()); break;
			case ACTIVE__PROP: setActive(in.nextBoolean()); break;
			case IMAGE__PROP: setImage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOLTIP__PROP: setTooltip(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DISPLAY_MODE__PROP: setDisplayMode(com.top_logic.layout.react.state.ButtonState.DisplayMode.readDisplayMode(in)); break;
			case CSS_CLASSES__PROP: setCssClasses(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case APPEARANCE__PROP: setAppearance(com.top_logic.layout.react.state.ButtonState.Appearance.readAppearance(in)); break;
			case TONE__PROP: setTone(com.top_logic.layout.react.state.ButtonState.Tone.readTone(in)); break;
			case SIZE__PROP: setSize(com.top_logic.layout.react.state.ButtonState.Size.readSize(in)); break;
			case NAVIGATE_URL__PROP: setNavigateUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAVIGATE_NEW_WINDOW__PROP: setNavigateNewWindow(in.nextBoolean()); break;
			case KEY_GESTURE__PROP: setKeyGesture(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
