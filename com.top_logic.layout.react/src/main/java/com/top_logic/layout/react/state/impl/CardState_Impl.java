package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.CardState}.
 */
public class CardState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.CardState {

	private String _title = "";

	private com.top_logic.layout.react.state.CardState.Variant _variant = com.top_logic.layout.react.state.CardState.Variant.OUTLINED;

	private com.top_logic.layout.react.state.CardState.Padding _padding = com.top_logic.layout.react.state.CardState.Padding.DEFAULT;

	private final java.util.List<com.top_logic.layout.react.state.ChildControl> _headerActions = new java.util.ArrayList<>();

	private com.top_logic.layout.react.state.ChildControl _child = null;

	/**
	 * Creates a {@link CardState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.CardState#create()
	 */
	public CardState_Impl() {
		super();
	}

	@Override
	public final String getTitle() {
		return _title;
	}

	@Override
	public com.top_logic.layout.react.state.CardState setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	@Override
	public final com.top_logic.layout.react.state.CardState.Variant getVariant() {
		return _variant;
	}

	@Override
	public com.top_logic.layout.react.state.CardState setVariant(com.top_logic.layout.react.state.CardState.Variant value) {
		internalSetVariant(value);
		return this;
	}

	/** Internal setter for {@link #getVariant()} without chain call utility. */
	protected final void internalSetVariant(com.top_logic.layout.react.state.CardState.Variant value) {
		if (value == null) throw new IllegalArgumentException("Property 'variant' cannot be null.");
		_variant = value;
	}

	@Override
	public final com.top_logic.layout.react.state.CardState.Padding getPadding() {
		return _padding;
	}

	@Override
	public com.top_logic.layout.react.state.CardState setPadding(com.top_logic.layout.react.state.CardState.Padding value) {
		internalSetPadding(value);
		return this;
	}

	/** Internal setter for {@link #getPadding()} without chain call utility. */
	protected final void internalSetPadding(com.top_logic.layout.react.state.CardState.Padding value) {
		if (value == null) throw new IllegalArgumentException("Property 'padding' cannot be null.");
		_padding = value;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.ChildControl> getHeaderActions() {
		return _headerActions;
	}

	@Override
	public com.top_logic.layout.react.state.CardState setHeaderActions(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		internalSetHeaderActions(value);
		return this;
	}

	/** Internal setter for {@link #getHeaderActions()} without chain call utility. */
	protected final void internalSetHeaderActions(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		if (value == null) throw new IllegalArgumentException("Property 'headerActions' cannot be null.");
		_headerActions.clear();
		_headerActions.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.CardState addHeaderAction(com.top_logic.layout.react.state.ChildControl value) {
		internalAddHeaderAction(value);
		return this;
	}

	/** Implementation of {@link #addHeaderAction(com.top_logic.layout.react.state.ChildControl)} without chain call utility. */
	protected final void internalAddHeaderAction(com.top_logic.layout.react.state.ChildControl value) {
		_headerActions.add(value);
	}

	@Override
	public final void removeHeaderAction(com.top_logic.layout.react.state.ChildControl value) {
		_headerActions.remove(value);
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getChild() {
		return _child;
	}

	@Override
	public com.top_logic.layout.react.state.CardState setChild(com.top_logic.layout.react.state.ChildControl value) {
		internalSetChild(value);
		return this;
	}

	/** Internal setter for {@link #getChild()} without chain call utility. */
	protected final void internalSetChild(com.top_logic.layout.react.state.ChildControl value) {
		_child = value;
	}

	@Override
	public final boolean hasChild() {
		return _child != null;
	}

	@Override
	public com.top_logic.layout.react.state.CardState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.CardState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return CARD_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(VARIANT__PROP);
		getVariant().writeTo(out);
		out.name(PADDING__PROP);
		getPadding().writeTo(out);
		out.name(HEADER_ACTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.ChildControl x : getHeaderActions()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasChild()) {
			out.name(CHILD__PROP);
			getChild().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VARIANT__PROP: setVariant(com.top_logic.layout.react.state.CardState.Variant.readVariant(in)); break;
			case PADDING__PROP: setPadding(com.top_logic.layout.react.state.CardState.Padding.readPadding(in)); break;
			case HEADER_ACTIONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.ChildControl> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.ChildControl.readChildControl(in));
				}
				in.endArray();
				setHeaderActions(newValue);
			}
			break;
			case CHILD__PROP: setChild(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			default: super.readField(in, field);
		}
	}

}
