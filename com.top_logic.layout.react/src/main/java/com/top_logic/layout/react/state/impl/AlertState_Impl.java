package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.AlertState}.
 */
public class AlertState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.AlertState {

	private com.top_logic.layout.react.state.SnackbarState.Variant _variant = com.top_logic.layout.react.state.SnackbarState.Variant.INFO;

	private String _title = "";

	private String _messageText = "";

	private String _icon = "";

	private boolean _closable = false;

	private final java.util.List<com.top_logic.layout.react.state.ChildControl> _actions = new java.util.ArrayList<>();

	private int _generation = 0;

	/**
	 * Creates a {@link AlertState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.AlertState#create()
	 */
	public AlertState_Impl() {
		super();
	}

	@Override
	public final com.top_logic.layout.react.state.SnackbarState.Variant getVariant() {
		return _variant;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setVariant(com.top_logic.layout.react.state.SnackbarState.Variant value) {
		internalSetVariant(value);
		return this;
	}

	/** Internal setter for {@link #getVariant()} without chain call utility. */
	protected final void internalSetVariant(com.top_logic.layout.react.state.SnackbarState.Variant value) {
		if (value == null) throw new IllegalArgumentException("Property 'variant' cannot be null.");
		_variant = value;
	}

	@Override
	public final String getTitle() {
		return _title;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	@Override
	public final String getMessageText() {
		return _messageText;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setMessageText(String value) {
		internalSetMessageText(value);
		return this;
	}

	/** Internal setter for {@link #getMessageText()} without chain call utility. */
	protected final void internalSetMessageText(String value) {
		_messageText = value;
	}

	@Override
	public final String getIcon() {
		return _icon;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setIcon(String value) {
		internalSetIcon(value);
		return this;
	}

	/** Internal setter for {@link #getIcon()} without chain call utility. */
	protected final void internalSetIcon(String value) {
		_icon = value;
	}

	@Override
	public final boolean isClosable() {
		return _closable;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setClosable(boolean value) {
		internalSetClosable(value);
		return this;
	}

	/** Internal setter for {@link #isClosable()} without chain call utility. */
	protected final void internalSetClosable(boolean value) {
		_closable = value;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.ChildControl> getActions() {
		return _actions;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setActions(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		internalSetActions(value);
		return this;
	}

	/** Internal setter for {@link #getActions()} without chain call utility. */
	protected final void internalSetActions(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		if (value == null) throw new IllegalArgumentException("Property 'actions' cannot be null.");
		_actions.clear();
		_actions.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.AlertState addAction(com.top_logic.layout.react.state.ChildControl value) {
		internalAddAction(value);
		return this;
	}

	/** Implementation of {@link #addAction(com.top_logic.layout.react.state.ChildControl)} without chain call utility. */
	protected final void internalAddAction(com.top_logic.layout.react.state.ChildControl value) {
		_actions.add(value);
	}

	@Override
	public final void removeAction(com.top_logic.layout.react.state.ChildControl value) {
		_actions.remove(value);
	}

	@Override
	public final int getGeneration() {
		return _generation;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setGeneration(int value) {
		internalSetGeneration(value);
		return this;
	}

	/** Internal setter for {@link #getGeneration()} without chain call utility. */
	protected final void internalSetGeneration(int value) {
		_generation = value;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.AlertState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ALERT_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(VARIANT__PROP);
		getVariant().writeTo(out);
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(MESSAGE_TEXT__PROP);
		out.value(getMessageText());
		out.name(ICON__PROP);
		out.value(getIcon());
		out.name(CLOSABLE__PROP);
		out.value(isClosable());
		out.name(ACTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.ChildControl x : getActions()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(GENERATION__PROP);
		out.value(getGeneration());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VARIANT__PROP: setVariant(com.top_logic.layout.react.state.SnackbarState.Variant.readVariant(in)); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MESSAGE_TEXT__PROP: setMessageText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ICON__PROP: setIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CLOSABLE__PROP: setClosable(in.nextBoolean()); break;
			case ACTIONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.ChildControl> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.ChildControl.readChildControl(in));
				}
				in.endArray();
				setActions(newValue);
			}
			break;
			case GENERATION__PROP: setGeneration(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
