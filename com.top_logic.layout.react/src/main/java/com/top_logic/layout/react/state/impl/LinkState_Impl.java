package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.LinkState}.
 */
public class LinkState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.LinkState {

	private String _text = "";

	private String _address = "";

	private com.top_logic.layout.react.state.LinkState.Kind _kind = com.top_logic.layout.react.state.LinkState.Kind.URL;

	private boolean _sameWindow = false;

	private String _tooltip = "";

	/**
	 * Creates a {@link LinkState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.LinkState#create()
	 */
	public LinkState_Impl() {
		super();
	}

	@Override
	public final String getText() {
		return _text;
	}

	@Override
	public com.top_logic.layout.react.state.LinkState setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	@Override
	public final String getAddress() {
		return _address;
	}

	@Override
	public com.top_logic.layout.react.state.LinkState setAddress(String value) {
		internalSetAddress(value);
		return this;
	}

	/** Internal setter for {@link #getAddress()} without chain call utility. */
	protected final void internalSetAddress(String value) {
		_address = value;
	}

	@Override
	public final com.top_logic.layout.react.state.LinkState.Kind getKind() {
		return _kind;
	}

	@Override
	public com.top_logic.layout.react.state.LinkState setKind(com.top_logic.layout.react.state.LinkState.Kind value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(com.top_logic.layout.react.state.LinkState.Kind value) {
		if (value == null) throw new IllegalArgumentException("Property 'kind' cannot be null.");
		_kind = value;
	}

	@Override
	public final boolean isSameWindow() {
		return _sameWindow;
	}

	@Override
	public com.top_logic.layout.react.state.LinkState setSameWindow(boolean value) {
		internalSetSameWindow(value);
		return this;
	}

	/** Internal setter for {@link #isSameWindow()} without chain call utility. */
	protected final void internalSetSameWindow(boolean value) {
		_sameWindow = value;
	}

	@Override
	public final String getTooltip() {
		return _tooltip;
	}

	@Override
	public com.top_logic.layout.react.state.LinkState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	/** Internal setter for {@link #getTooltip()} without chain call utility. */
	protected final void internalSetTooltip(String value) {
		_tooltip = value;
	}

	@Override
	public com.top_logic.layout.react.state.LinkState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.LinkState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return LINK_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(ADDRESS__PROP);
		out.value(getAddress());
		out.name(KIND__PROP);
		getKind().writeTo(out);
		out.name(SAME_WINDOW__PROP);
		out.value(isSameWindow());
		out.name(TOOLTIP__PROP);
		out.value(getTooltip());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ADDRESS__PROP: setAddress(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(com.top_logic.layout.react.state.LinkState.Kind.readKind(in)); break;
			case SAME_WINDOW__PROP: setSameWindow(in.nextBoolean()); break;
			case TOOLTIP__PROP: setTooltip(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
