package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.FloatingActionState}.
 */
public class FloatingActionState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.FloatingActionState {

	private final java.util.List<com.top_logic.layout.react.state.ChildControl> _actions = new java.util.ArrayList<>();

	private com.top_logic.layout.react.state.FloatingActionState.Position _position = com.top_logic.layout.react.state.FloatingActionState.Position.BOTTOM_END;

	private String _triggerImage = "";

	private String _triggerLabel = "";

	/**
	 * Creates a {@link FloatingActionState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.FloatingActionState#create()
	 */
	public FloatingActionState_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.ChildControl> getActions() {
		return _actions;
	}

	@Override
	public com.top_logic.layout.react.state.FloatingActionState setActions(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
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
	public com.top_logic.layout.react.state.FloatingActionState addAction(com.top_logic.layout.react.state.ChildControl value) {
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
	public final com.top_logic.layout.react.state.FloatingActionState.Position getPosition() {
		return _position;
	}

	@Override
	public com.top_logic.layout.react.state.FloatingActionState setPosition(com.top_logic.layout.react.state.FloatingActionState.Position value) {
		internalSetPosition(value);
		return this;
	}

	/** Internal setter for {@link #getPosition()} without chain call utility. */
	protected final void internalSetPosition(com.top_logic.layout.react.state.FloatingActionState.Position value) {
		if (value == null) throw new IllegalArgumentException("Property 'position' cannot be null.");
		_position = value;
	}

	@Override
	public final String getTriggerImage() {
		return _triggerImage;
	}

	@Override
	public com.top_logic.layout.react.state.FloatingActionState setTriggerImage(String value) {
		internalSetTriggerImage(value);
		return this;
	}

	/** Internal setter for {@link #getTriggerImage()} without chain call utility. */
	protected final void internalSetTriggerImage(String value) {
		_triggerImage = value;
	}

	@Override
	public final String getTriggerLabel() {
		return _triggerLabel;
	}

	@Override
	public com.top_logic.layout.react.state.FloatingActionState setTriggerLabel(String value) {
		internalSetTriggerLabel(value);
		return this;
	}

	/** Internal setter for {@link #getTriggerLabel()} without chain call utility. */
	protected final void internalSetTriggerLabel(String value) {
		_triggerLabel = value;
	}

	@Override
	public com.top_logic.layout.react.state.FloatingActionState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.FloatingActionState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return FLOATING_ACTION_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ACTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.ChildControl x : getActions()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(POSITION__PROP);
		getPosition().writeTo(out);
		out.name(TRIGGER_IMAGE__PROP);
		out.value(getTriggerImage());
		out.name(TRIGGER_LABEL__PROP);
		out.value(getTriggerLabel());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
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
			case POSITION__PROP: setPosition(com.top_logic.layout.react.state.FloatingActionState.Position.readPosition(in)); break;
			case TRIGGER_IMAGE__PROP: setTriggerImage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TRIGGER_LABEL__PROP: setTriggerLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
