package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.PopoverState}.
 */
public class PopoverState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.PopoverState {

	private boolean _open = false;

	private com.top_logic.layout.react.state.PopoverState.Placement _placement = com.top_logic.layout.react.state.PopoverState.Placement.TOP;

	private com.top_logic.layout.react.state.PopoverState.Mode _mode = com.top_logic.layout.react.state.PopoverState.Mode.CLICK;

	private String _label = "";

	private String _icon = "";

	private com.top_logic.layout.react.state.ChildControl _trigger = null;

	private com.top_logic.layout.react.state.ChildControl _content = null;

	/**
	 * Creates a {@link PopoverState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.PopoverState#create()
	 */
	public PopoverState_Impl() {
		super();
	}

	@Override
	public final boolean isOpen() {
		return _open;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setOpen(boolean value) {
		internalSetOpen(value);
		return this;
	}

	/** Internal setter for {@link #isOpen()} without chain call utility. */
	protected final void internalSetOpen(boolean value) {
		_open = value;
	}

	@Override
	public final com.top_logic.layout.react.state.PopoverState.Placement getPlacement() {
		return _placement;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setPlacement(com.top_logic.layout.react.state.PopoverState.Placement value) {
		internalSetPlacement(value);
		return this;
	}

	/** Internal setter for {@link #getPlacement()} without chain call utility. */
	protected final void internalSetPlacement(com.top_logic.layout.react.state.PopoverState.Placement value) {
		if (value == null) throw new IllegalArgumentException("Property 'placement' cannot be null.");
		_placement = value;
	}

	@Override
	public final com.top_logic.layout.react.state.PopoverState.Mode getMode() {
		return _mode;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setMode(com.top_logic.layout.react.state.PopoverState.Mode value) {
		internalSetMode(value);
		return this;
	}

	/** Internal setter for {@link #getMode()} without chain call utility. */
	protected final void internalSetMode(com.top_logic.layout.react.state.PopoverState.Mode value) {
		if (value == null) throw new IllegalArgumentException("Property 'mode' cannot be null.");
		_mode = value;
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public final String getIcon() {
		return _icon;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setIcon(String value) {
		internalSetIcon(value);
		return this;
	}

	/** Internal setter for {@link #getIcon()} without chain call utility. */
	protected final void internalSetIcon(String value) {
		_icon = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getTrigger() {
		return _trigger;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setTrigger(com.top_logic.layout.react.state.ChildControl value) {
		internalSetTrigger(value);
		return this;
	}

	/** Internal setter for {@link #getTrigger()} without chain call utility. */
	protected final void internalSetTrigger(com.top_logic.layout.react.state.ChildControl value) {
		_trigger = value;
	}

	@Override
	public final boolean hasTrigger() {
		return _trigger != null;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getContent() {
		return _content;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setContent(com.top_logic.layout.react.state.ChildControl value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(com.top_logic.layout.react.state.ChildControl value) {
		_content = value;
	}

	@Override
	public final boolean hasContent() {
		return _content != null;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.PopoverState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return POPOVER_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(OPEN__PROP);
		out.value(isOpen());
		out.name(PLACEMENT__PROP);
		getPlacement().writeTo(out);
		out.name(MODE__PROP);
		getMode().writeTo(out);
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(ICON__PROP);
		out.value(getIcon());
		if (hasTrigger()) {
			out.name(TRIGGER__PROP);
			getTrigger().writeTo(out);
		}
		if (hasContent()) {
			out.name(CONTENT__PROP);
			getContent().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OPEN__PROP: setOpen(in.nextBoolean()); break;
			case PLACEMENT__PROP: setPlacement(com.top_logic.layout.react.state.PopoverState.Placement.readPlacement(in)); break;
			case MODE__PROP: setMode(com.top_logic.layout.react.state.PopoverState.Mode.readMode(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ICON__PROP: setIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TRIGGER__PROP: setTrigger(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			case CONTENT__PROP: setContent(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			default: super.readField(in, field);
		}
	}

}
