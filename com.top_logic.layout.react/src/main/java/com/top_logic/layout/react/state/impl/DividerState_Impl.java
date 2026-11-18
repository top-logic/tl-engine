package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.DividerState}.
 */
public class DividerState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.DividerState {

	private com.top_logic.layout.react.state.DividerState.Orientation _orientation = com.top_logic.layout.react.state.DividerState.Orientation.HORIZONTAL;

	private String _label = "";

	private com.top_logic.layout.react.state.DividerState.LabelPosition _labelPosition = com.top_logic.layout.react.state.DividerState.LabelPosition.START;

	private com.top_logic.layout.react.state.DividerState.Inset _inset = com.top_logic.layout.react.state.DividerState.Inset.NONE;

	/**
	 * Creates a {@link DividerState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.DividerState#create()
	 */
	public DividerState_Impl() {
		super();
	}

	@Override
	public final com.top_logic.layout.react.state.DividerState.Orientation getOrientation() {
		return _orientation;
	}

	@Override
	public com.top_logic.layout.react.state.DividerState setOrientation(com.top_logic.layout.react.state.DividerState.Orientation value) {
		internalSetOrientation(value);
		return this;
	}

	/** Internal setter for {@link #getOrientation()} without chain call utility. */
	protected final void internalSetOrientation(com.top_logic.layout.react.state.DividerState.Orientation value) {
		if (value == null) throw new IllegalArgumentException("Property 'orientation' cannot be null.");
		_orientation = value;
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.DividerState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public final com.top_logic.layout.react.state.DividerState.LabelPosition getLabelPosition() {
		return _labelPosition;
	}

	@Override
	public com.top_logic.layout.react.state.DividerState setLabelPosition(com.top_logic.layout.react.state.DividerState.LabelPosition value) {
		internalSetLabelPosition(value);
		return this;
	}

	/** Internal setter for {@link #getLabelPosition()} without chain call utility. */
	protected final void internalSetLabelPosition(com.top_logic.layout.react.state.DividerState.LabelPosition value) {
		if (value == null) throw new IllegalArgumentException("Property 'labelPosition' cannot be null.");
		_labelPosition = value;
	}

	@Override
	public final com.top_logic.layout.react.state.DividerState.Inset getInset() {
		return _inset;
	}

	@Override
	public com.top_logic.layout.react.state.DividerState setInset(com.top_logic.layout.react.state.DividerState.Inset value) {
		internalSetInset(value);
		return this;
	}

	/** Internal setter for {@link #getInset()} without chain call utility. */
	protected final void internalSetInset(com.top_logic.layout.react.state.DividerState.Inset value) {
		if (value == null) throw new IllegalArgumentException("Property 'inset' cannot be null.");
		_inset = value;
	}

	@Override
	public com.top_logic.layout.react.state.DividerState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DividerState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DIVIDER_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ORIENTATION__PROP);
		getOrientation().writeTo(out);
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(LABEL_POSITION__PROP);
		getLabelPosition().writeTo(out);
		out.name(INSET__PROP);
		getInset().writeTo(out);
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ORIENTATION__PROP: setOrientation(com.top_logic.layout.react.state.DividerState.Orientation.readOrientation(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL_POSITION__PROP: setLabelPosition(com.top_logic.layout.react.state.DividerState.LabelPosition.readLabelPosition(in)); break;
			case INSET__PROP: setInset(com.top_logic.layout.react.state.DividerState.Inset.readInset(in)); break;
			default: super.readField(in, field);
		}
	}

}
