package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.DialogState}.
 */
public class DialogState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.DialogState {

	private boolean _open = false;

	private boolean _closeOnBackdrop = false;

	private boolean _closable = false;

	private com.top_logic.layout.react.state.ChildControl _child = null;

	/**
	 * Creates a {@link DialogState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.DialogState#create()
	 */
	public DialogState_Impl() {
		super();
	}

	@Override
	public final boolean isOpen() {
		return _open;
	}

	@Override
	public com.top_logic.layout.react.state.DialogState setOpen(boolean value) {
		internalSetOpen(value);
		return this;
	}

	/** Internal setter for {@link #isOpen()} without chain call utility. */
	protected final void internalSetOpen(boolean value) {
		_open = value;
	}

	@Override
	public final boolean isCloseOnBackdrop() {
		return _closeOnBackdrop;
	}

	@Override
	public com.top_logic.layout.react.state.DialogState setCloseOnBackdrop(boolean value) {
		internalSetCloseOnBackdrop(value);
		return this;
	}

	/** Internal setter for {@link #isCloseOnBackdrop()} without chain call utility. */
	protected final void internalSetCloseOnBackdrop(boolean value) {
		_closeOnBackdrop = value;
	}

	@Override
	public final boolean isClosable() {
		return _closable;
	}

	@Override
	public com.top_logic.layout.react.state.DialogState setClosable(boolean value) {
		internalSetClosable(value);
		return this;
	}

	/** Internal setter for {@link #isClosable()} without chain call utility. */
	protected final void internalSetClosable(boolean value) {
		_closable = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getChild() {
		return _child;
	}

	@Override
	public com.top_logic.layout.react.state.DialogState setChild(com.top_logic.layout.react.state.ChildControl value) {
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
	public com.top_logic.layout.react.state.DialogState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DialogState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DIALOG_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(OPEN__PROP);
		out.value(isOpen());
		out.name(CLOSE_ON_BACKDROP__PROP);
		out.value(isCloseOnBackdrop());
		out.name(CLOSABLE__PROP);
		out.value(isClosable());
		if (hasChild()) {
			out.name(CHILD__PROP);
			getChild().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OPEN__PROP: setOpen(in.nextBoolean()); break;
			case CLOSE_ON_BACKDROP__PROP: setCloseOnBackdrop(in.nextBoolean()); break;
			case CLOSABLE__PROP: setClosable(in.nextBoolean()); break;
			case CHILD__PROP: setChild(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			default: super.readField(in, field);
		}
	}

}
