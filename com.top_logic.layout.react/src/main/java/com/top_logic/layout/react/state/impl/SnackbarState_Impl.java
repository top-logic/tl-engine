package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.SnackbarState}.
 */
public class SnackbarState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.SnackbarState {

	private String _messageText = "";

	private String _content = "";

	private com.top_logic.layout.react.state.SnackbarState.Variant _variant = com.top_logic.layout.react.state.SnackbarState.Variant.INFO;

	private int _duration = 0;

	private boolean _visible = false;

	private int _generation = 0;

	/**
	 * Creates a {@link SnackbarState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.SnackbarState#create()
	 */
	public SnackbarState_Impl() {
		super();
	}

	@Override
	public final String getMessageText() {
		return _messageText;
	}

	@Override
	public com.top_logic.layout.react.state.SnackbarState setMessageText(String value) {
		internalSetMessageText(value);
		return this;
	}

	/** Internal setter for {@link #getMessageText()} without chain call utility. */
	protected final void internalSetMessageText(String value) {
		_messageText = value;
	}

	@Override
	public final String getContent() {
		return _content;
	}

	@Override
	public com.top_logic.layout.react.state.SnackbarState setContent(String value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(String value) {
		_content = value;
	}

	@Override
	public final com.top_logic.layout.react.state.SnackbarState.Variant getVariant() {
		return _variant;
	}

	@Override
	public com.top_logic.layout.react.state.SnackbarState setVariant(com.top_logic.layout.react.state.SnackbarState.Variant value) {
		internalSetVariant(value);
		return this;
	}

	/** Internal setter for {@link #getVariant()} without chain call utility. */
	protected final void internalSetVariant(com.top_logic.layout.react.state.SnackbarState.Variant value) {
		if (value == null) throw new IllegalArgumentException("Property 'variant' cannot be null.");
		_variant = value;
	}

	@Override
	public final int getDuration() {
		return _duration;
	}

	@Override
	public com.top_logic.layout.react.state.SnackbarState setDuration(int value) {
		internalSetDuration(value);
		return this;
	}

	/** Internal setter for {@link #getDuration()} without chain call utility. */
	protected final void internalSetDuration(int value) {
		_duration = value;
	}

	@Override
	public final boolean isVisible() {
		return _visible;
	}

	@Override
	public com.top_logic.layout.react.state.SnackbarState setVisible(boolean value) {
		internalSetVisible(value);
		return this;
	}

	/** Internal setter for {@link #isVisible()} without chain call utility. */
	protected final void internalSetVisible(boolean value) {
		_visible = value;
	}

	@Override
	public final int getGeneration() {
		return _generation;
	}

	@Override
	public com.top_logic.layout.react.state.SnackbarState setGeneration(int value) {
		internalSetGeneration(value);
		return this;
	}

	/** Internal setter for {@link #getGeneration()} without chain call utility. */
	protected final void internalSetGeneration(int value) {
		_generation = value;
	}

	@Override
	public com.top_logic.layout.react.state.SnackbarState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SnackbarState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return SNACKBAR_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(MESSAGE_TEXT__PROP);
		out.value(getMessageText());
		out.name(CONTENT__PROP);
		out.value(getContent());
		out.name(VARIANT__PROP);
		getVariant().writeTo(out);
		out.name(DURATION__PROP);
		out.value(getDuration());
		out.name(VISIBLE__PROP);
		out.value(isVisible());
		out.name(GENERATION__PROP);
		out.value(getGeneration());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MESSAGE_TEXT__PROP: setMessageText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT__PROP: setContent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VARIANT__PROP: setVariant(com.top_logic.layout.react.state.SnackbarState.Variant.readVariant(in)); break;
			case DURATION__PROP: setDuration(in.nextInt()); break;
			case VISIBLE__PROP: setVisible(in.nextBoolean()); break;
			case GENERATION__PROP: setGeneration(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
